package de.schwerin.stoppCoronaDE.screens.infection_info

import de.schwerin.stoppCoronaDE.model.entities.infection.message.DbReceivedInfectionMessage
import de.schwerin.stoppCoronaDE.model.entities.infection.message.MessageType
import de.schwerin.stoppCoronaDE.model.repositories.InfectionMessengerRepository
import de.schwerin.stoppCoronaDE.model.repositories.QuarantineRepository
import de.schwerin.stoppCoronaDE.model.repositories.QuarantineStatus
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchers
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.viewmodel.ScopedViewModel
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import org.threeten.bp.LocalDate

/**
 * Handles the user interaction and provides data for [InfectionInfoFragment].
 */
class InfectionInfoViewModel(
    appDispatchers: AppDispatchers,
    private val infectionMessengerRepository: InfectionMessengerRepository,
    private val quarantineRepository: QuarantineRepository
) : ScopedViewModel(appDispatchers) {

    fun observeInfectedContacts(): Observable<InfectedContactsViewState> {
        return Observables.combineLatest(
            infectionMessengerRepository.observeReceivedInfectionMessages(),
            quarantineRepository.observeQuarantineState()
        ).map { (messages, quarantineStatus) ->
            InfectedContactsViewState(
                messages = messages,
                quarantinedUntil = if (quarantineStatus is QuarantineStatus.Jailed.Limited) quarantineStatus.end.toLocalDate()
                else null
            )
        }
    }
}

data class InfectedContactsViewState(
    val messages: List<DbReceivedInfectionMessage>,
    val quarantinedUntil: LocalDate? = null
) {

    val yellowMessages by lazy { messages.filter { it.messageType == MessageType.InfectionLevel.Yellow } }
    val redMessages by lazy { messages.filter { it.messageType == MessageType.InfectionLevel.Red } }
}