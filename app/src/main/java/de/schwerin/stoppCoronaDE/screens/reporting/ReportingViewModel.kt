package de.schwerin.stoppCoronaDE.screens.reporting

import de.schwerin.stoppCoronaDE.model.entities.infection.message.MessageType
import de.schwerin.stoppCoronaDE.model.repositories.ReportingState
import de.schwerin.stoppCoronaDE.model.repositories.ReportingRepository
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchers
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.viewmodel.ScopedViewModel
import io.reactivex.Observable

/**
 * Handles the user interaction and provides data for [ReportingActivity].
 */
class ReportingViewModel(
    appDispatchers: AppDispatchers,
    private val reportingRepository: ReportingRepository,
    messageType: MessageType
) : ScopedViewModel(appDispatchers) {

    init {
        reportingRepository.setMessageType(messageType)
    }

    fun observeReportingState(): Observable<ReportingState> {
        return reportingRepository.observeReportingState()
    }
}
