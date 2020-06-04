package de.schwerin.stoppCoronaDE.model.api

import android.content.Context
import android.content.SharedPreferences
import de.schwerin.stoppCoronaDE.model.entities.configuration.ApiConfiguration
import de.schwerin.stoppCoronaDE.model.entities.infection.info.ApiInfectionInfoRequest
import de.schwerin.stoppCoronaDE.model.entities.infection.message.ApiInfectionMessages
import de.schwerin.stoppCoronaDE.model.entities.tan.ApiRequestTan
import de.schwerin.stoppCoronaDE.model.entities.tan.ApiRequestTanBody
import de.schwerin.stoppCoronaDE.model.repositories.DataPrivacyRepository
import de.schwerin.stoppCoronaDE.skeleton.core.model.exceptions.ExceptionMapperHelper
import de.schwerin.stoppCoronaDE.skeleton.core.model.exceptions.GeneralServerException
import de.schwerin.stoppCoronaDE.skeleton.core.model.exceptions.NoInternetConnectionException
import de.schwerin.stoppCoronaDE.skeleton.core.model.exceptions.UnexpectedError
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.HttpURLConnection.*
import com.google.firebase.firestore.FirebaseFirestore
import de.schwerin.stoppCoronaDE.constants.Constants
import de.schwerin.stoppCoronaDE.model.entities.infection.message.ApiInfectionMessage
import de.schwerin.stoppCoronaDE.skeleton.core.utils.putAndApply
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.*
import kotlin.collections.ArrayList


/**
 * Interactor that communicates with API.
 */
interface ApiInteractor {

    /**
     * Get the current configuration.
     * @throws [ApiError]
     */
    suspend fun getConfiguration(): ApiConfiguration

    /**
     * Get infection messages.
     * Returns the last if [fromId] is null.
     * Otherwise it returns one page of 100 messages from [fromId] (not included).
     *
     * Messages are filtered by our supplied [addressPrefix]
     *
     * @throws [ApiError]
     */
    suspend fun getInfectionMessages(addressPrefix: String, fromId: Long? = null): ApiInfectionMessages

    /**
     * Upload infection information about user.
     * @throws [SicknessCertificateUploadException], [ApiError]
     */
    suspend fun setInfectionInfo(infectionInfoRequest: ApiInfectionInfoRequest)

    /**
     * Request the server to send a TAN via text message
     * @throws [ApiError]
     *
     * @param Context the calling context
     * @return
     */
    suspend fun requestTan(context : Context): ApiRequestTan
}

class ApiInteractorImpl(
    private val appDispatchers: AppDispatchers,
    private val apiDescription: ApiDescription,
    private val tanApiDescription: TanApiDescription,
    private val dataPrivacyRepository: DataPrivacyRepository
) : ApiInteractor, ExceptionMapperHelper{

    private val generalExceptionMapper: (HttpException) -> Exception? = {
        when (it.code()) {
            HTTP_FORBIDDEN -> ApiError.Critical.AuthorizationError
            HTTP_GONE -> ApiError.Critical.ForceUpdate
            else -> null
        }
    }
    override suspend fun getConfiguration(): ApiConfiguration {
        return withContext(appDispatchers.IO) {
            dataPrivacyRepository.assertDataPrivacyAccepted()
            apiDescription.configuration().configuration
        }
    }

    override suspend fun getInfectionMessages(
        addressPrefix: String,
        fromId: Long?
    ): ApiInfectionMessages {
        dataPrivacyRepository.assertDataPrivacyAccepted()
        val db = FirebaseFirestore.getInstance()

        var ref = db.collection("messages").whereEqualTo("pre", addressPrefix)

        if (fromId != null) {
            ref = ref.whereGreaterThan("cAt",fromId)
        }

        val documentsSnapshot = ref.get().await()
        val messages = ArrayList<ApiInfectionMessage>()

        for (document in documentsSnapshot.documents) {

            val createdAtNullable = document.getLong("cAt")
            val contentNullable = document.getString("content")

            if (createdAtNullable != null && contentNullable != null) {

                messages.add(
                    ApiInfectionMessage(
                        createdAtNullable,
                        contentNullable
                    )
                )
            }
        }


        return withContext(appDispatchers.IO) {
            ApiInfectionMessages(messages)
        }
    }

    private suspend fun <T> checkGeneralErrors(
        httpExceptionMapper: (HttpException) -> Exception? = generalExceptionMapper,
        criticalAction: suspend () -> T
    ): T {
        return try {
            criticalAction()
        } catch (e: Exception) {
            throw resolveException(e, httpExceptionMapper)
        }
    }

    override suspend fun setInfectionInfo(infectionInfoRequest: ApiInfectionInfoRequest) {
        withContext(appDispatchers.IO) {
            dataPrivacyRepository.assertDataPrivacyAccepted()
            checkGeneralErrors(
                { httpException ->
                    when (httpException.code()) {
                        HTTP_FORBIDDEN -> SicknessCertificateUploadException.TanInvalidException
                        HTTP_INTERNAL_ERROR -> SicknessCertificateUploadException.TanInvalidException
                        else -> generalExceptionMapper(httpException)
                    }
                },
                {
                    apiDescription.infectionInfo(infectionInfoRequest)
                }
            )
        }
    }

    override suspend fun requestTan(context : Context): ApiRequestTan {
        return withContext(appDispatchers.IO) {
            dataPrivacyRepository.assertDataPrivacyAccepted()
            val sharedPreferences : SharedPreferences = context.getSharedPreferences(Constants.Prefs.FILE_NAME, Context.MODE_PRIVATE)

            var guid = sharedPreferences.getString("GUID",null)

            if (guid == null) {
                guid =  UUID.randomUUID().toString()
                sharedPreferences.putAndApply("GUID",guid)
            }

            var sha = MessageDigest
                .getInstance("SHA256")
                .digest(guid.toByteArray())
                .fold("", { str, it -> str + "%02x".format(it) })

            for (i in 0 until sha.length-1) {
                val currentChar = sha[i]

                if (currentChar.isDigit()) {
                    val intVal = currentChar.toString().toInt()
                    var fC = ""

                    var charPosition = 0

                    for (j in 0 until sha.length-1) {

                        val currentChar = sha[j]
                        if ( currentChar.isLetter()) {

                            var charVal= currentChar.toInt() + intVal
                            if (charVal > 122) {
                                charVal -= 26
                            }
                            charPosition = j
                            fC = charVal.toChar().toString()
                            break
                        }
                    }
                    sha = StringBuilder(sha).insert(i + charPosition +  intVal, fC ).toString()
                    break
                }
            }
            tanApiDescription.requestTan(ApiRequestTanBody( sha))
        }
    }
}

//class ApiInteractorImpl(
//    private val appDispatchers: AppDispatchers,
//    private val apiDescription: ApiDescription,
//    private val tanApiDescription: TanApiDescription,
//    private val dataPrivacyRepository: DataPrivacyRepository
//) : ApiInteractor,
//    ExceptionMapperHelper {
//
//    private val generalExceptionMapper: (HttpException) -> Exception? = {
//        when (it.code()) {
//            HTTP_FORBIDDEN -> ApiError.Critical.AuthorizationError
//            HTTP_GONE -> ApiError.Critical.ForceUpdate
//            else -> null
//        }
//    }
//
//    /**
//     * Map http errors to application domain.
//     * @throws [ApiError]
//     */
//    private suspend fun <T> checkGeneralErrors(
//        httpExceptionMapper: (HttpException) -> Exception? = generalExceptionMapper,
//        criticalAction: suspend () -> T
//    ): T {
//        return try {
//            criticalAction()
//        } catch (e: Exception) {
//            throw resolveException(e, httpExceptionMapper)
//        }
//    }
//
//    override suspend fun getConfiguration(): ApiConfiguration {
//        return withContext(appDispatchers.IO) {
//            dataPrivacyRepository.assertDataPrivacyAccepted()
//            checkGeneralErrors {
//                apiDescription.configuration().configuration
//            }
//        }
//    }
//
//    override suspend fun getInfectionMessages(addressPrefix: String, fromId: Long?): ApiInfectionMessages {
//        return withContext(appDispatchers.IO) {
//            dataPrivacyRepository.assertDataPrivacyAccepted()
//            checkGeneralErrors {
//                apiDescription.infectionMessages(addressPrefix, fromId)
//            }
//        }
//    }
//
//    override suspend fun setInfectionInfo(infectionInfoRequest: ApiInfectionInfoRequest) {
//        withContext(appDispatchers.IO) {
//            dataPrivacyRepository.assertDataPrivacyAccepted()
//            checkGeneralErrors(
//                { httpException ->
//                    when (httpException.code()) {
//                        HTTP_FORBIDDEN -> SicknessCertificateUploadException.TanInvalidException
//                        else -> generalExceptionMapper(httpException)
//                    }
//                },
//                {
//                    apiDescription.infectionInfo(infectionInfoRequest)
//                }
//            )
//        }
//    }
//
//    override suspend fun requestTan(mobileNumber: String): ApiRequestTan {
//        return withContext(appDispatchers.IO) {
//            dataPrivacyRepository.assertDataPrivacyAccepted()
//            checkGeneralErrors({ httpException ->
//                when (httpException.code()) {
//                    HTTP_UNAUTHORIZED -> SicknessCertificateUploadException.PhoneNumberInvalidException
//                    HTTP_INTERNAL_ERROR -> SicknessCertificateUploadException.SMSGatewayException
//                    else -> generalExceptionMapper(httpException)
//                }
//            },
//                {
//                    tanApiDescription.requestTan(ApiRequestTanBody(mobileNumber))
//                })
//        }
//    }
//}

/**
 * Exception in application domain.
 *
 * Can be also [GeneralServerException], [NoInternetConnectionException], [UnexpectedError].
 */
sealed class ApiError : Exception() {

    sealed class Critical : ApiError() {
        /**
         * Correct authorization required in http header.
         */
        object AuthorizationError : Critical()

        /**
         * API version discontinued. Client must be updated.
         */
        object ForceUpdate : Critical()

        /**
         * API call when data privacy is not accepted yet, which is violation of GDPR.
         */
        object DataPrivacyNotAcceptedYet : Critical()
    }
}

/**
 * Exceptions triggered when uploading infection info.
 */
sealed class SicknessCertificateUploadException : Exception() {

    /**
     * Triggered when the TAN used to upload the infection is invalid.
     */
    object TanInvalidException : SicknessCertificateUploadException()


}