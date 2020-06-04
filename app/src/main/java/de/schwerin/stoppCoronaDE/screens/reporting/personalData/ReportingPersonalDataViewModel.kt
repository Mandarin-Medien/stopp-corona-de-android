package de.schwerin.stoppCoronaDE.screens.reporting.personalData

import android.content.Context
import androidx.annotation.StringRes
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.model.entities.infection.message.MessageType
import de.schwerin.stoppCoronaDE.model.repositories.ReportingRepository
import de.schwerin.stoppCoronaDE.model.repositories.TanRequest
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchers
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.State
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.StateObserver
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.viewmodel.ScopedViewModel
import io.reactivex.Observable
import kotlinx.coroutines.launch

/**
 * Handles the user interaction and provides data for [ReportingPersonalDataFragment].
 */
class ReportingPersonalDataViewModel(
    appDispatchers: AppDispatchers,
    private val reportingRepository: ReportingRepository
) : ScopedViewModel(appDispatchers) {

    private val tanRequestStateObserver = StateObserver()



    fun validate(context: Context) {
        requestTan(context)
    }

    private fun requestTan(context: Context) {
        tanRequestStateObserver.loading()
        launch {
            try {
                reportingRepository.requestTan(context)
                // Request is successful, save the personal data and mark the tan request as successful.
                reportingRepository.setTanRequestSuccessful()
            } catch (ex: Exception) {
                tanRequestStateObserver.error(ex)
            } finally {
                tanRequestStateObserver.idle()
            }
        }
    }

    fun observeTanRequestState(): Observable<State> {
        return tanRequestStateObserver.observe()
    }

    fun observeTanRequest(): Observable<TanRequest> {
        return reportingRepository.observeTanRequest()
    }


    fun observeMessageType(): Observable<MessageType> {
        return reportingRepository.observeMessageType()
    }
}

fun validateNotEmpty(text: String?): ValidationError? {
    return if (text?.isNotEmpty() == true) {
        null
    } else {
        return ValidationError.FieldEmpty
    }
}


/**
 * Describes the errors that can appear in the form.
 */
sealed class ValidationError(@StringRes val error: Int) {

    /**
     * A mandatory field is empty.
     */
    object FieldEmpty : ValidationError(R.string.certificate_personal_data_field_mandatory)
}
