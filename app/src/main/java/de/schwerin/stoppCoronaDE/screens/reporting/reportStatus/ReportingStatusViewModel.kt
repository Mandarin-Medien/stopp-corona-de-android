package de.schwerin.stoppCoronaDE.screens.reporting.reportStatus

import de.schwerin.stoppCoronaDE.model.entities.infection.message.MessageType
import de.schwerin.stoppCoronaDE.model.repositories.AgreementData
import de.schwerin.stoppCoronaDE.model.repositories.QuarantineRepository
import de.schwerin.stoppCoronaDE.model.repositories.ReportingRepository
import de.schwerin.stoppCoronaDE.model.repositories.other.ContextInteractor
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchers
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.DataState
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.DataStateObserver
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.viewmodel.ScopedViewModel
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import kotlinx.coroutines.launch
import org.threeten.bp.ZonedDateTime

/**
 * Handles the user interaction and provides data for [ReportingStatusFragment].
 */
class ReportingStatusViewModel(
    appDispatchers: AppDispatchers,
    private val reportingRepository: ReportingRepository,
    private val quarantineRepository: QuarantineRepository,
    private val contextInteractor: ContextInteractor
) : ScopedViewModel(appDispatchers) {

    private val uploadReportDataStateObserver = DataStateObserver<MessageType>()

    fun setUserAgreement(agreement: Boolean) {
        reportingRepository.setUserAgreement(agreement)
    }

    fun uploadInfectionInformation() {
        uploadReportDataStateObserver.loading()
        launch {
            try {
                val reportedInfectionLevel = reportingRepository.uploadReportInformation()
                uploadReportDataStateObserver.loaded(reportedInfectionLevel)
            } catch (ex: Exception) {
                uploadReportDataStateObserver.error(ex)
            } finally {
                uploadReportDataStateObserver.idle()
            }
        }
    }

    fun goBack() {
        reportingRepository.goBackFromReportingAgreementScreen()
    }

    fun observeUploadReportDataState(): Observable<DataState<MessageType>> {
        return uploadReportDataStateObserver.observe().map { dataState ->
            dataState
        }
    }

    fun observeReportingStatusData(): Observable<ReportingStatusData> {
        return Observables.combineLatest(
            reportingRepository.observeAgreementData(),
            reportingRepository.observeMessageType(),
            quarantineRepository.observeDateOfFirstSelfDiagnose(),
            quarantineRepository.observeDateOfFirstMedicalConfirmation()
        ) { agreementData, infectionLevel, dateOfFirstSelfDiagnose, dateOfFirstMedicalConfirmation ->
            ReportingStatusData(
                agreementData,
                infectionLevel,
                dateOfFirstSelfDiagnose.orElse(null),
                dateOfFirstMedicalConfirmation.orElse(null)
            )
        }
    }

    fun observeMessageType(): Observable<MessageType> {
        return reportingRepository.observeMessageType()
    }
}

data class ReportingStatusData(
    val agreementData: AgreementData,
    val messageType: MessageType,
    val dateOfFirstSelfDiagnose: ZonedDateTime?,
    val dateOfFirstMedicalConfirmation: ZonedDateTime?
)
