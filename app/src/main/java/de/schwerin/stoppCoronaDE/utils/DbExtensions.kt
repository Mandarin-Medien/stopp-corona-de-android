package de.schwerin.stoppCoronaDE.utils

import de.schwerin.stoppCoronaDE.skeleton.core.utils.subscribeOnIO
import io.reactivex.Flowable
import io.reactivex.Observable

/**
 * Extensions related to Room database.
 */

/**
 * Transform [Flowable] from observing database entries to [Observable].
 * Thread is changed to IO and same emitted objects are ignored.
 */
fun <T> Flowable<T>.asDbObservable(): Observable<T> {
    return subscribeOnIO()
        .distinctUntilChanged()
        .toObservable()
}