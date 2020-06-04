package de.schwerin.stoppCoronaDE.skeleton.core.screens.base.fragment.list

import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchers
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.BehaviourSubjectObservable
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.State
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.viewmodel.ScopedViewModel
import io.reactivex.Observable

/**
 * Actions for observing generic data.
 */
abstract class BaseDataObserverViewModel<Data>(
    appDispatchers: AppDispatchers
) : ScopedViewModel(appDispatchers) {

    private val dataObserver = BehaviourSubjectObservable { observeDataInternal() }

    open fun fetchData() {
        // default implementation does nothing
    }

    protected abstract fun observeDataInternal(): Observable<Data>

    fun observeData(): Observable<Data> = dataObserver

    fun getLastData(): Data? = dataObserver.currentData

    open fun observeState(): Observable<State> {
        return Observable.just(State.Idle) // data are without loading by default
    }
}