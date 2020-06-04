package de.schwerin.stoppCoronaDE.screens.questionnaire.guideline

import de.schwerin.stoppCoronaDE.model.repositories.QuarantineRepository
import de.schwerin.stoppCoronaDE.model.repositories.QuarantineStatus
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchers
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.viewmodel.ScopedViewModel
import io.reactivex.Observable

/**
 * Handles the user interaction and provides data for [QuestionnaireGuidelineFragment].
 */
class QuestionnaireGuidelineViewModel(
    appDispatchers: AppDispatchers,
    private val quarantineRepository: QuarantineRepository
) : ScopedViewModel(appDispatchers) {

    fun observeQuarantineStatus(): Observable<QuarantineStatus> {
        return quarantineRepository.observeQuarantineState()
    }
}
