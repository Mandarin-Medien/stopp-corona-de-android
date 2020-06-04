package de.schwerin.stoppCoronaDE.screens.routing

import android.net.Uri
import de.schwerin.stoppCoronaDE.model.repositories.OnboardingRepository
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchers
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.viewmodel.ScopedViewModel

/**
 * Handles router actions.
 *
 * Routing strategies:
 * 1) deeplinking
 *      - at a later point of time
 * 2) onboarding on if has not been seen by user yet
 * 3) else dashboard.
 */
class RouterViewModel(
    appDispatchers: AppDispatchers,
    private val onboardingRepository: OnboardingRepository
) : ScopedViewModel(appDispatchers) {

    fun route(deepLinkUri: Uri?): RouterAction {
        return when {
            onboardingRepository.shouldShowOnboarding -> RouterAction.Onboarding
            else -> RouterAction.Dashboard
        }
    }
}

sealed class RouterAction {

    object Onboarding : RouterAction()
    object Dashboard : RouterAction()
}
