package de.schwerin.stoppCoronaDE.screens.dashboard.dialog

import de.schwerin.stoppCoronaDE.model.repositories.DashboardRepository
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchers
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.viewmodel.ScopedViewModel

/**
 * Handles the user interaction for [MicrophoneExplanationDialog].
 */
class MicrophoneExplanationDialogViewModel(
    appDispatchers: AppDispatchers,
    private val dashboardRepository: DashboardRepository
) : ScopedViewModel(appDispatchers) {

    fun doNotShowAgain() {
        dashboardRepository.setMicrophoneExplanationDialogShown()
    }
}
