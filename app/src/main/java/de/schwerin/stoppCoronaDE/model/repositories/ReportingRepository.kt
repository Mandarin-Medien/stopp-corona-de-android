package de.schwerin.stoppCoronaDE.model.repositories

import android.content.Context
import android.util.Base64
import de.schwerin.stoppCoronaDE.constants.Constants.Misc.EMPTY_STRING
import de.schwerin.stoppCoronaDE.model.api.ApiInteractor
import de.schwerin.stoppCoronaDE.model.db.dao.InfectionMessageDao
import de.schwerin.stoppCoronaDE.model.db.dao.NearbyRecordDao
import de.schwerin.stoppCoronaDE.model.entities.infection.info.ApiAddressedInfectionMessage
import de.schwerin.stoppCoronaDE.model.entities.infection.info.ApiInfectionInfoRequest
import de.schwerin.stoppCoronaDE.model.entities.infection.message.InfectionMessageContent
import de.schwerin.stoppCoronaDE.model.entities.infection.message.MessageType
import de.schwerin.stoppCoronaDE.model.manager.DatabaseCleanupManager
import de.schwerin.stoppCoronaDE.model.repositories.ReportingRepository.Companion.SCOPE_NAME
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchers
import de.schwerin.stoppCoronaDE.skeleton.core.model.scope.Scope
import de.schwerin.stoppCoronaDE.utils.NonNullableBehaviorSubject
import de.schwerin.stoppCoronaDE.utils.view.safeMap
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import org.threeten.bp.ZonedDateTime
import kotlin.coroutines.CoroutineContext

/**
 * Scoped repository for handling data during uploading the result of a self-testing,
 * a sickness certificate or a self-test revoke.
 */
interface ReportingRepository {

    companion object {
        const val SCOPE_NAME = "ReportingRepositoryScope"
    }

    /**
     * Sets the messageType that will be reported to authorities at the end of the reporting flow.
     */
    fun setMessageType(messageType: MessageType)

    /**
     * Request a TAN for authentication.
     */
    suspend fun requestTan(context: Context)

    /**
     * Upload the report information with the upload infection request.
     * @throws InvalidConfigurationException
     *
     * @return Returns the messageType  the user sent to his contacts
     */
    suspend fun uploadReportInformation(): MessageType

    /**
     * Set the TAN introduced by the user.
     */
    fun setTan(tan: String)

    /**
     * Set the TAN Request successfull
     */
    fun setTanRequestSuccessful()

    /**
     * Set the latest agreement of the user about data reporting.
     */
    fun setUserAgreement(agreement: Boolean)

    /**
     * Navigate back from the reporting agreement screen.
     */
    fun goBackFromReportingAgreementScreen()

    /**
     * Observe the state of the reporting.
     */
    fun observeReportingState(): Observable<ReportingState>

    /**
     * Observe the personal data.
     */
    fun observeTanRequest(): Observable<TanRequest>

    /**
     * Observe the TAN related data.
     */
    fun observeTanData(): Observable<TanData>

    /**
     * Observe the data related to user agreement.
     */
    fun observeAgreementData(): Observable<AgreementData>

    /**
     * Observe the messageType that will reported in this flow.
     * @throws [InvalidConfigurationException]
     */
    fun observeMessageType(): Observable<MessageType>
}

class ReportingRepositoryImpl(
    private val appDispatchers: AppDispatchers,
    private val apiInteractor: ApiInteractor,
    private val configurationRepository: ConfigurationRepository,
    private val nearbyRecordDao: NearbyRecordDao,
    private val infectionMessageDao: InfectionMessageDao,
    private val cryptoRepository: CryptoRepository,
    private val infectionMessengerRepository: InfectionMessengerRepository,
    private val quarantineRepository: QuarantineRepository,
    private val databaseCleanupManager: DatabaseCleanupManager
) : Scope(SCOPE_NAME),
    ReportingRepository,
    CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = appDispatchers.Default

    private val tanRequestSubject = NonNullableBehaviorSubject(
        TanRequest()
    )

    private val tanDataSubject = NonNullableBehaviorSubject(
        TanData()
    )

    private val agreementDataSubject = NonNullableBehaviorSubject(AgreementData())
    private val messageTypeSubject = BehaviorSubject.create<MessageType>()
    private var tanUuid: String? = null

    override fun setMessageType(messageType: MessageType) {
        messageTypeSubject.onNext(messageType)
    }

    override fun setTanRequestSuccessful() {
        tanRequestSubject.onNext(
            TanRequest(true)
        )
    }

    override suspend fun requestTan(context: Context) {
        tanUuid = apiInteractor.requestTan(context).uuid
    }

    override suspend fun uploadReportInformation(): MessageType {
        return when (messageTypeSubject.value) {
            MessageType.Revoke.Suspicion -> uploadRevokeSuspicionInfo()
            MessageType.Revoke.Sickness -> uploadRevokeSicknessInfo()
            else -> uploadInfectionInfo()
        }
    }

    private suspend fun uploadInfectionInfo(): MessageType.InfectionLevel {
        return withContext(coroutineContext) {
            val infectionLevel = messageTypeSubject.value as? MessageType.InfectionLevel ?: throw InvalidConfigurationException.InfectionLevelNotSet

            val configuration = configurationRepository.observeConfiguration().blockingFirst()
            val warnBeforeSymptoms = configuration.warnBeforeSymptoms ?: throw InvalidConfigurationException.NullWarnBeforeSymptoms
            var thresholdTime = ZonedDateTime.now().minusHours(warnBeforeSymptoms.toLong())

            val infectionMessages = mutableListOf<Pair<ByteArray, InfectionMessageContent>>()

            if (infectionLevel == MessageType.InfectionLevel.Red) {
                infectionMessages.addAll(
                    infectionMessageDao.getSentInfectionMessagesByMessageType(MessageType.InfectionLevel.Yellow)
                        .map { message ->
                            message.publicKey to InfectionMessageContent(
                                MessageType.InfectionLevel.Red,
                                message.timeStamp,
                                message.uuid
                            )
                        }
                )

                if (infectionMessages.isNotEmpty()) {
                    infectionMessages.sortByDescending { (_, content) -> content.timeStamp }
                    thresholdTime = infectionMessages.first().second.timeStamp
                }
            } else if (infectionLevel == MessageType.InfectionLevel.Yellow) {
                val resetMessages = infectionMessageDao.getSentInfectionMessagesByMessageType(MessageType.InfectionLevel.Yellow)
                    .map { message ->
                        message.publicKey to InfectionMessageContent(
                            MessageType.Revoke.Suspicion,
                            message.timeStamp,
                            message.uuid
                        )
                    }

                infectionMessengerRepository.storeSentInfectionMessages(resetMessages)
            }

            infectionMessages.addAll(nearbyRecordDao.observeRecordsRecentThan(thresholdTime)
                .blockingFirst()
                .map { nearbyRecord ->
                    nearbyRecord.publicKey to InfectionMessageContent(infectionLevel, nearbyRecord.timestamp)
                }
            )

            apiInteractor.setInfectionInfo(
                ApiInfectionInfoRequest(
                    tanUuid.safeMap(defaultValue = EMPTY_STRING),
                    tanDataSubject.value.tan,
                    encryptInfectionMessages(infectionMessages)
                )
            )

            infectionMessengerRepository.storeSentInfectionMessages(infectionMessages)

            when (infectionLevel) {
                MessageType.InfectionLevel.Red -> {
                    quarantineRepository.reportMedicalConfirmation()
                    quarantineRepository.revokePositiveSelfDiagnose(backup = true)
                    quarantineRepository.revokeSelfMonitoring()
                }
                MessageType.InfectionLevel.Yellow -> {
                    quarantineRepository.reportPositiveSelfDiagnose()
                    quarantineRepository.revokeSelfMonitoring()
                }
            }

            infectionLevel
        }
    }

    private suspend fun uploadRevokeSuspicionInfo(): MessageType.Revoke.Suspicion {
        return withContext(coroutineContext) {
            val infectionMessages = infectionMessageDao.getSentInfectionMessagesByMessageType(MessageType.InfectionLevel.Yellow)
                .map { message ->
                    message.publicKey to InfectionMessageContent(
                        MessageType.Revoke.Suspicion,
                        message.timeStamp,
                        message.uuid
                    )
                }

            apiInteractor.setInfectionInfo(
                ApiInfectionInfoRequest(
                    tanUuid.safeMap(defaultValue = EMPTY_STRING),
                    tanDataSubject.value.tan,
                    encryptInfectionMessages(infectionMessages)
                )
            )

            quarantineRepository.revokePositiveSelfDiagnose(backup = false)
            databaseCleanupManager.removeSentYellowMessages()

            MessageType.Revoke.Suspicion
        }
    }

    private suspend fun uploadRevokeSicknessInfo(): MessageType.Revoke.Sickness {
        return withContext(coroutineContext) {

            val updateStatus = when {
                quarantineRepository.hasSelfDiagnoseBackup -> MessageType.InfectionLevel.Yellow
                else -> MessageType.Revoke.Suspicion
            }

            val infectionMessages = infectionMessageDao.getSentInfectionMessagesByMessageType(MessageType.InfectionLevel.Red)
                .map { message ->
                    message.publicKey to InfectionMessageContent(
                        updateStatus,
                        message.timeStamp,
                        message.uuid
                    )
                }

            apiInteractor.setInfectionInfo(
                ApiInfectionInfoRequest(
                    tanUuid.safeMap(defaultValue = EMPTY_STRING),
                    tanDataSubject.value.tan,
                    encryptInfectionMessages(infectionMessages)
                )
            )

            infectionMessengerRepository.storeSentInfectionMessages(infectionMessages)

            quarantineRepository.revokeMedicalConfirmation()

            when (updateStatus) {
                is MessageType.InfectionLevel.Yellow -> {
                    quarantineRepository.reportPositiveSelfDiagnoseFromBackup()
                }
                is MessageType.Revoke.Suspicion -> {
                    quarantineRepository.revokePositiveSelfDiagnose(backup = false)
                }
            }

            MessageType.Revoke.Sickness
        }
    }

    private fun encryptInfectionMessages(infectionMessages: List<Pair<ByteArray, InfectionMessageContent>>): List<ApiAddressedInfectionMessage> {
        return infectionMessages.map { (publicKey, infectionMessage) ->
            Pair(
                cryptoRepository.encrypt(infectionMessage.toByteArray(), publicKey),
                cryptoRepository.getPublicKeyPrefix(publicKey)
            )
        }.filter { (encryptedInfectionMessage, addressPrefix) ->
            encryptedInfectionMessage != null
        }.map { (encryptedInfectionMessage, addressPrefix) ->
            val encodedEncryptedInfectionMessage = Base64.encodeToString(encryptedInfectionMessage, Base64.NO_WRAP)
            ApiAddressedInfectionMessage(encodedEncryptedInfectionMessage, addressPrefix)
        }
    }

    override fun setTan(tan: String) {
        tanDataSubject.onNext(TanData(tan, tanIsFilled = true))
    }

    override fun setUserAgreement(agreement: Boolean) {
        agreementDataSubject.onNext(AgreementData(agreement))
    }

    override fun observeReportingState(): Observable<ReportingState> {
        return Observables.combineLatest(
            tanRequestSubject,
            agreementDataSubject
        ).map { (tanRequest, tanData) ->
            println(tanData)
            when {
                tanRequest.success == null || tanRequest.success.not() -> {
                    return@map ReportingState.PersonalDataEntry
                }
                else -> {
                    return@map ReportingState.ReportingAgreement
                }
            }
        }
    }

    override fun goBackFromReportingAgreementScreen() {
        tanRequestSubject.onNext(TanRequest())
        tanDataSubject.onNext(tanDataSubject.value.copy(tan = EMPTY_STRING))
        agreementDataSubject.onNext(agreementDataSubject.value.copy(userHasAgreed = false))
    }

    override fun observeTanRequest(): Observable<TanRequest> {
        return tanRequestSubject
    }

    override fun observeTanData(): Observable<TanData> {
        return tanDataSubject
    }

    override fun observeAgreementData(): Observable<AgreementData> {
        return agreementDataSubject
    }

    override fun observeMessageType(): Observable<MessageType> {
        return messageTypeSubject
    }
}

data class AgreementData(val userHasAgreed: Boolean = false)
data class TanRequest( val success: Boolean? = null)

data class TanData(val tan: String = EMPTY_STRING, val tanIsFilled: Boolean = false)

/**
 * Automaton definition of the report sending process.
 */
sealed class ReportingState {

    /**
     * User has to enter his personal data.
     */
    object PersonalDataEntry : ReportingState()

    /**
     * User has to enter the TAN received via SMS.
     */
    object TanEntry : ReportingState()

    /**
     * User has to agree that his data will be reported to authorities.
     */
    object ReportingAgreement : ReportingState()
}

/**
 * Exceptions caused by invalid data in configuration.
 */
sealed class InvalidConfigurationException(override val message: String) : Exception(message) {

    object NullWarnBeforeSymptoms : InvalidConfigurationException("warnBeforeSymptoms is null")

    object InfectionLevelNotSet : InvalidConfigurationException("messageType is null")
}
