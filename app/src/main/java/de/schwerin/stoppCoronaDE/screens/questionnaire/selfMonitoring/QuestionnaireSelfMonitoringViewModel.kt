package de.schwerin.stoppCoronaDE.screens.questionnaire.selfmonitoring

import de.schwerin.stoppCoronaDE.model.repositories.QuarantineRepository
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchers
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.viewmodel.ScopedViewModel

class QuestionnaireSelfMonitoringViewModel(
    appDispatchers: AppDispatchers,
    private val quarantineRepository: QuarantineRepository
) : ScopedViewModel(appDispatchers) {

    fun reportSelfMonitoring() {
        quarantineRepository.reportSelfMonitoring()
    }
}
