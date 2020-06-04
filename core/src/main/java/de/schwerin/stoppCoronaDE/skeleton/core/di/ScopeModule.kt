package de.schwerin.stoppCoronaDE.skeleton.core.di

import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchers
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchersImpl
import de.schwerin.stoppCoronaDE.skeleton.core.model.scope.ScopeConnector
import org.koin.dsl.module.module

/**
 * Module for providing scopes.
 */
internal val scopeModule = module {

    /**
     * Provide testable coroutine dispatchers.
     */
    single<AppDispatchers> {
        AppDispatchersImpl()
    }

    single { ScopeConnector() }
}