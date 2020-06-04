package de.schwerin.stoppCoronaDE.screens.dashboard

import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.MessagesClient
import de.schwerin.stoppCoronaDE.model.entities.infection.message.MessageType
import de.schwerin.stoppCoronaDE.model.manager.DatabaseCleanupManager
import de.schwerin.stoppCoronaDE.model.repositories.*
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchers
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.viewmodel.ScopedViewModel
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import kotlinx.coroutines.launch
import org.threeten.bp.ZonedDateTime
import timber.log.Timber

/**
 * Handles the user interaction and provides data for [DashboardFragment].
 */
class DashboardViewModel(
    appDispatchers: AppDispatchers,
    private val dashboardRepository: DashboardRepository,
    private val nearbyRepository: NearbyRepository,
    private val infectionMessengerRepository: InfectionMessengerRepository,
    private val quarantineRepository: QuarantineRepository,
    private val configurationRepository: ConfigurationRepository,
    private val databaseCleanupManager: DatabaseCleanupManager,
    private val googleApiClientBuilder: GoogleApiClient.Builder
) : ScopedViewModel(appDispatchers) {

    fun observeConnection() = nearbyRepository.observeConnection()

    companion object {
        const val DEFAULT_RED_WARNING_QUARANTINE = 336 // hours
        const val DEFAULT_YELLOW_WARNING_QUARANTINE = 168 // hours
    }

    private var messagesClient: MessagesClient? = null
    private var googleApiClient: GoogleApiClient? = null


    fun permissionDenied() {
        messagesClient = null
        googleApiClient = null
    }


    fun startConnection(messagesClient: MessagesClient) {
        if (googleApiClient?.isConnected == true) {
            this.messagesClient = messagesClient
            publish()
            subscribe()
        }
    }

    private fun publish() {
        messagesClient?.publish(nearbyRepository.message, nearbyRepository.publishOptions)
            ?.addOnSuccessListener {
                Timber.e("publish success")
            }
            ?.addOnCanceledListener {
                Timber.e("publish canceled")
            }
            ?.addOnFailureListener {
                Timber.e("publish failed: $it")
            }
    }

    fun resume(activity: FragmentActivity) {
        googleApiClient = googleApiClientBuilder.addApi(Nearby.MESSAGES_API)
            .addConnectionCallbacks(nearbyRepository.connectionCallbacks)
            .enableAutoManage(activity, nearbyRepository.connectionFailedListener)
            .build()
    }

    fun pause(activity: FragmentActivity) {
        googleApiClient?.stopAutoManage(activity)
        googleApiClient?.disconnect()

        Timber.e("PAUSE")

    }

    fun stopConnection() {
        if (googleApiClient?.isConnected == true) {
            messagesClient?.unpublish(nearbyRepository.message)
            messagesClient?.unsubscribe(nearbyRepository.messageListener)
        }
    }


    private fun subscribe() {
        messagesClient?.subscribe(nearbyRepository.messageListener, nearbyRepository.subscribeOptions)
            ?.addOnSuccessListener {
                Timber.e("subscribe success")
            }
            ?.addOnCanceledListener {
                Timber.e("subscribe canceled")
            }
            ?.addOnFailureListener {
                Timber.e("subscribe failed: $it")
            }
    }

    val showMicrophoneExplanationDialog: Boolean
        get() = dashboardRepository.showMicrophoneExplanationDialog


    fun observeSavedEncounters(): Observable<Int> {
        return dashboardRepository.observeSavedEncountersNumber()
    }

    fun observeContactsHealthStatus(): Observable<HealthStatusData> {
        return Observables.combineLatest(
            infectionMessengerRepository.observeReceivedInfectionMessages(),
            quarantineRepository.observeQuarantineState(),
            configurationRepository.observeConfiguration()
        ).map { (infectionMessageList, quarantineStatus, configuration) ->
            val filteredInfectionMessages = infectionMessageList.filter { it.messageType != MessageType.Revoke.Suspicion }
            Triple(filteredInfectionMessages, quarantineStatus, configuration)
        }.map { (infectionMessageList, quarantineStatus, configuration) ->
            if (infectionMessageList.isNotEmpty()) {
                val redWarningQuarantineThreshold = ZonedDateTime.now().minusHours(
                    (configuration.redWarningQuarantine ?: DEFAULT_RED_WARNING_QUARANTINE).toLong()
                )
                val yellowWarningQuarantineThreshold = ZonedDateTime.now().minusHours(
                    (configuration.yellowWarningQuarantine ?: DEFAULT_YELLOW_WARNING_QUARANTINE).toLong()
                )
                HealthStatusData.ContactsSicknessInfo(
                    infectionMessageList
                        .filter { it.timeStamp > redWarningQuarantineThreshold }
                        .count { it.messageType == MessageType.InfectionLevel.Red },
                    infectionMessageList
                        .filter { it.timeStamp > yellowWarningQuarantineThreshold }
                        .count { it.messageType == MessageType.InfectionLevel.Yellow },
                    quarantineStatus
                )
            } else {
                HealthStatusData.NoHealthStatus
            }
        }
    }

    fun observeOwnHealthStatus(): Observable<HealthStatusData> {
        return quarantineRepository.observeQuarantineState()
            .map { quarantineStatus ->
                when (quarantineStatus) {
                    is QuarantineStatus.Jailed.Forever -> HealthStatusData.SicknessCertificate
                    is QuarantineStatus.Jailed.Limited -> {
                        when {
                            quarantineStatus.byContact -> HealthStatusData.NoHealthStatus
                            else -> HealthStatusData.SelfTestingSuspicionOfSickness(quarantineStatus)
                        }
                    }
                    is QuarantineStatus.Free -> {
                        when {
                            quarantineStatus.selfMonitoring -> HealthStatusData.SelfTestingSymptomsMonitoring
                            else -> HealthStatusData.NoHealthStatus
                        }
                    }
                }
            }
    }

    fun observeHandshakeState(): Observable<NearbyHandshakeState> {
        return nearbyRepository.observeHandshakeState()
            .map { state ->
                if (state == NearbyHandshakeState.Active) {
                    NearbyHandshakeState.Active
                } else {
                    NearbyHandshakeState.Expired
                }
            }
    }

    fun retry() {
        publish()
        subscribe()
    }

    fun observeShowQuarantineEnd(): Observable<Boolean> {
        return quarantineRepository.observeShowQuarantineEnd()
    }

    fun quarantineEndSeen() {
        quarantineRepository.quarantineEndSeen()
    }

    fun observeSomeoneHasRecoveredStatus(): Observable<HealthStatusData> {
        return infectionMessengerRepository.observeSomeoneHasRecoveredMessage()
            .map { shouldShow ->
                if (shouldShow) {
                    HealthStatusData.SomeoneHasRecovered
                } else {
                    HealthStatusData.NoHealthStatus
                }
            }
    }

    fun someoneHasRecoveredSeen() {
        infectionMessengerRepository.someoneHasRecoveredMessageSeen()

        launch {
            databaseCleanupManager.removeReceivedGreenMessages()
        }
    }
}

/**
 * Describes the states of the health state cards.
 */
sealed class HealthStatusData {

    /**
     * The user has successfully reported a sickness certificate to authorities.
     */
    object SicknessCertificate : HealthStatusData()

    /**
     * The user has successfully sent a self assessment to authorities with the result suspicion.
     */
    data class SelfTestingSuspicionOfSickness(
        val quarantineStatus: QuarantineStatus.Jailed.Limited
    ) : HealthStatusData()

    /**
     * The user has successfully sent a self assessment to authorities with the symptoms monitoring.
     */
    object SelfTestingSymptomsMonitoring : HealthStatusData()

    /**
     * The user has received sickness info his contacts.
     */
    class ContactsSicknessInfo(
        val confirmed: Int = 0,
        val suspicion: Int = 0,
        val quarantineStatus: QuarantineStatus
    ) : HealthStatusData()

    /**
     * Some of contacts has recovered.
     */
    object SomeoneHasRecovered : HealthStatusData()

    /**
     * No health status to be announced.
     */
    object NoHealthStatus : HealthStatusData()
}

