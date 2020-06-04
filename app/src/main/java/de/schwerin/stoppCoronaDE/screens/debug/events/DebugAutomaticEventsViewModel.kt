package de.schwerin.stoppCoronaDE.screens.debug.events

import de.schwerin.stoppCoronaDE.model.db.dao.AutomaticDiscoveryDao
import de.schwerin.stoppCoronaDE.model.entities.discovery.DbAutomaticDiscoveryEvent
import de.schwerin.stoppCoronaDE.model.repositories.CryptoRepository
import de.schwerin.stoppCoronaDE.model.repositories.other.ContextInteractor
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchers
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.viewmodel.ScopedViewModel
import de.schwerin.stoppCoronaDE.utils.asDbObservable
import de.schwerin.stoppCoronaDE.utils.formatDayAndMonthAndYearAndTime
import io.reactivex.Observable

class DebugAutomaticEventsViewModel(
    appDispatchers: AppDispatchers,
    private val automaticDiscoveryDao: AutomaticDiscoveryDao,
    private val contextInteractor: ContextInteractor,
    private val cryptoRepository: CryptoRepository
) : ScopedViewModel(appDispatchers) {


    fun observe(): Observable<String> {
        return automaticDiscoveryDao.observeAllEvents().asDbObservable()
            .map {
                it.sortedByDescending { it.startTime }
                    .joinToString("\n") { it.asPrintable() }
            }
    }

    private fun DbAutomaticDiscoveryEvent.asPrintable(): String {
        return "${cryptoRepository.getPublicKeyPrefix(publicKey)}\n" +
            "startTime = ${startTime.formatDayAndMonthAndYearAndTime(contextInteractor.applicationContext)}\n" +
            "endTime = ${endTime?.formatDayAndMonthAndYearAndTime(contextInteractor.applicationContext)}\n" +
            "proximity = $proximity\n"
    }
}