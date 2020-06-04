package de.schwerin.stoppCoronaDE.screens.history

import de.schwerin.stoppCoronaDE.model.entities.nearby.DbNearbyRecord
import de.schwerin.stoppCoronaDE.model.repositories.NearbyRepository
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchers
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.viewmodel.ScopedViewModel
import io.reactivex.Observable

/**
 * Handles the user interaction and provides data for [ContactHistoryFragment].
 */
class ContactHistoryViewModel(
    appDispatchers: AppDispatchers,
    private val nearbyRepository: NearbyRepository
) : ScopedViewModel(appDispatchers) {

    fun observeAllNearbyRecords(): Observable<List<NearbyRecordWrapper>> {
        return nearbyRepository.observeAllNearbyRecords().map { nearbyRecordsList ->
            nearbyRecordsList
                .sortedBy { it.timestamp }
                .mapIndexed { index, nearby ->
                    NearbyRecordWrapper(nearby, index)
                }
                .sortedByDescending { it.index }
        }
    }

}

data class NearbyRecordWrapper(val record: DbNearbyRecord, val index: Int)

