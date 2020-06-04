package de.schwerin.stoppCoronaDE.screens.questionnaire.hint

import de.schwerin.stoppCoronaDE.model.repositories.QuarantineRepository
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchers
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.viewmodel.ScopedViewModel

class QuestionnaireHintViewModel(
    appDispatchers: AppDispatchers,
    private val quarantineRepository: QuarantineRepository
) : ScopedViewModel(appDispatchers) {

    fun revokeSelfMonitoring() {
        quarantineRepository.revokeSelfMonitoring()
    }
}
