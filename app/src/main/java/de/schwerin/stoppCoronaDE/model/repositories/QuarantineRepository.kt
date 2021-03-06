package de.schwerin.stoppCoronaDE.model.repositories

import android.content.SharedPreferences
import androidx.work.WorkManager
import de.schwerin.stoppCoronaDE.constants.Constants.Prefs
import de.schwerin.stoppCoronaDE.model.entities.configuration.DbConfiguration
import de.schwerin.stoppCoronaDE.model.entities.infection.info.WarningType
import de.schwerin.stoppCoronaDE.model.exceptions.SilentError
import de.schwerin.stoppCoronaDE.model.workers.EndQuarantineNotifierWorker.Companion.cancelEndQuarantineReminder
import de.schwerin.stoppCoronaDE.model.workers.EndQuarantineNotifierWorker.Companion.enqueueEndQuarantineReminder
import de.schwerin.stoppCoronaDE.model.workers.SelfRetestNotifierWorker.Companion.cancelSelfRetestingReminder
import de.schwerin.stoppCoronaDE.model.workers.SelfRetestNotifierWorker.Companion.enqueueSelfRetestingReminder
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchers
import de.schwerin.stoppCoronaDE.skeleton.core.utils.*
import de.schwerin.stoppCoronaDE.utils.view.safeMap
import com.github.dmstocking.optional.java.util.Optional
import io.reactivex.Observable
import io.reactivex.rxkotlin.Observables
import kotlinx.coroutines.CoroutineScope
import org.threeten.bp.ZonedDateTime
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

/**
 * Repository for controlling quarantine status of the user.
 */
interface QuarantineRepository {

    /**
     * Indicator if the user was in yellow state before turning red.
     */
    val hasSelfDiagnoseBackup: Boolean

    /**
     * Observe date of the first medical confirmation.
     */
    fun observeDateOfFirstMedicalConfirmation(): Observable<Optional<ZonedDateTime>>

    /**
     * Observe date of the first self diagnose.
     */
    fun observeDateOfFirstSelfDiagnose(): Observable<Optional<ZonedDateTime>>

    /**
     * Observe current quarantine status.
     */
    fun observeQuarantineState(): Observable<QuarantineStatus>

    /**
     * User has reported himself as red case.
     */
    fun reportMedicalConfirmation(timeOfReport: ZonedDateTime = ZonedDateTime.now())

    /**
     * User has reported himself as yellow case.
     */
    fun reportPositiveSelfDiagnose(timeOfReport: ZonedDateTime = ZonedDateTime.now())

    /**
     * User has revoked his official sickness status and goes back to yellow case.
     */
    fun reportPositiveSelfDiagnoseFromBackup()

    /**
     * User revoked the state of his red case.
     */
    fun revokeMedicalConfirmation()

    /**
     * User revoked the state of his yellow case.
     *
     * Use [backup] to store the self diagnose timestamps in a backup.
     */
    fun revokePositiveSelfDiagnose(backup: Boolean)

    /**
     * Some contact has reported [warningType].
     */
    fun receivedWarning(warningType: WarningType, timeOfContact: ZonedDateTime = ZonedDateTime.now())

    /**
     * User completed the questionnaire and has to monitor his symptoms
     */
    fun reportSelfMonitoring(timeOfReport: ZonedDateTime = ZonedDateTime.now())

    /**
     * User revoked the state of symptom monitoring
     */
    fun revokeSelfMonitoring()

    /**
     * Show information about quarantine end.
     */
    fun setShowQuarantineEnd()

    /**
     * Hide information about quarantine end.
     */
    fun quarantineEndSeen()

    /**
     * Observe if quarantine end information can be shown.
     */
    fun observeShowQuarantineEnd(): Observable<Boolean>

    /**
     * Resets the last red contact date.
     */
    fun revokeLastRedContactDate()
}

class QuarantineRepositoryImpl(
    private val appDispatchers: AppDispatchers,
    private val preferences: SharedPreferences,
    configurationRepository: ConfigurationRepository,
    private val workManager: WorkManager
) : QuarantineRepository,
    CoroutineScope {

    companion object {
        private const val PREF_DATE_OF_FIRST_MEDICAL_CONFIRMATION = Prefs.QUARANTINE_REPOSITORY_PREFIX + "date_of_first_medical_confirmation"
        private const val PREF_DATE_OF_FIRST_SELF_DIAGNOSE = Prefs.QUARANTINE_REPOSITORY_PREFIX + "date_of_first_self_diagnose"
        private const val PREF_DATE_OF_FIRST_SELF_DIAGNOSE_BACKUP = Prefs.QUARANTINE_REPOSITORY_PREFIX + "date_of_first_self_diagnose_backup"
        private const val PREF_DATE_OF_LAST_SELF_DIAGNOSE = Prefs.QUARANTINE_REPOSITORY_PREFIX + "date_of_last_self_diagnose"
        private const val PREF_DATE_OF_LAST_SELF_DIAGNOSE_BACKUP = Prefs.QUARANTINE_REPOSITORY_PREFIX + "date_of_last_self_diagnose_backup"
        private const val PREF_DATE_OF_LAST_RED_CONTACT = Prefs.QUARANTINE_REPOSITORY_PREFIX + "date_of_last_red_contact"
        private const val PREF_DATE_OF_LAST_YELLOW_CONTACT = Prefs.QUARANTINE_REPOSITORY_PREFIX + "date_of_last_yellow_contact"
        private const val PREF_DATE_OF_LAST_SELF_MONITORING = Prefs.QUARANTINE_REPOSITORY_PREFIX + "date_opf_last_self_monitoring"
        private const val PREF_SHOW_QUARANTINE_END = Prefs.QUARANTINE_REPOSITORY_PREFIX + "show_quarantine_end"
    }

    private var dateOfFirstMedicalConfirmation: ZonedDateTime?
        by preferences.nullableZonedDateTimeSharedPreferencesProperty(PREF_DATE_OF_FIRST_MEDICAL_CONFIRMATION)

    override fun observeDateOfFirstMedicalConfirmation(): Observable<Optional<ZonedDateTime>> {
        return preferences.observeNullableZonedDateTime(PREF_DATE_OF_FIRST_MEDICAL_CONFIRMATION)
    }

    private var dateOfFirstSelfDiagnose: ZonedDateTime?
        by preferences.nullableZonedDateTimeSharedPreferencesProperty(PREF_DATE_OF_FIRST_SELF_DIAGNOSE)

    override fun observeDateOfFirstSelfDiagnose(): Observable<Optional<ZonedDateTime>> {
        return preferences.observeNullableZonedDateTime(PREF_DATE_OF_FIRST_SELF_DIAGNOSE)
    }

    private var dateOfFirstSelfDiagnoseBackup: ZonedDateTime?
        by preferences.nullableZonedDateTimeSharedPreferencesProperty(PREF_DATE_OF_FIRST_SELF_DIAGNOSE_BACKUP)

    private var dateOfLastSelfDiagnose: ZonedDateTime?
        by preferences.nullableZonedDateTimeSharedPreferencesProperty(PREF_DATE_OF_LAST_SELF_DIAGNOSE)

    private var dateOfLastSelfDiagnoseBackup: ZonedDateTime?
        by preferences.nullableZonedDateTimeSharedPreferencesProperty(PREF_DATE_OF_LAST_SELF_DIAGNOSE_BACKUP)

    private var dateOfLastRedContact: ZonedDateTime?
        by preferences.nullableZonedDateTimeSharedPreferencesProperty(PREF_DATE_OF_LAST_RED_CONTACT)

    private var dateOfLastYellowContact: ZonedDateTime?
        by preferences.nullableZonedDateTimeSharedPreferencesProperty(PREF_DATE_OF_LAST_YELLOW_CONTACT)

    private var dateOfLastSelfMonitoringInstruction: ZonedDateTime?
        by preferences.nullableZonedDateTimeSharedPreferencesProperty(PREF_DATE_OF_LAST_SELF_MONITORING)

    private var showQuarantineEnd: Boolean
        by preferences.booleanSharedPreferencesProperty(PREF_SHOW_QUARANTINE_END, false)

    override val coroutineContext: CoroutineContext
        get() = appDispatchers.Default

    override val hasSelfDiagnoseBackup: Boolean
        get() = dateOfFirstSelfDiagnoseBackup != null && dateOfLastSelfDiagnoseBackup != null

    private val quarantineStateObservable = Observables.combineLatest(
        configurationRepository.observeConfiguration(),
        preferences.observeNullableZonedDateTime(PREF_DATE_OF_FIRST_MEDICAL_CONFIRMATION),
        preferences.observeNullableZonedDateTime(PREF_DATE_OF_LAST_SELF_DIAGNOSE),
        preferences.observeNullableZonedDateTime(PREF_DATE_OF_LAST_RED_CONTACT),
        preferences.observeNullableZonedDateTime(PREF_DATE_OF_LAST_YELLOW_CONTACT),
        preferences.observeNullableZonedDateTime(PREF_DATE_OF_LAST_SELF_MONITORING)
    ) { configuration,
        medicalConfirmationFirstDateTime,
        selfDiagnoseLastDateTime,
        redContactLastDateTime,
        yellowContactLastDateTime,
        selfMonitoringLastDateTime ->
        QuarantinePrerequisitesHolder(
            configuration,
            medicalConfirmationFirstDateTime.orElse(null),
            selfDiagnoseLastDateTime.orElse(null),
            redContactLastDateTime.orElse(null),
            yellowContactLastDateTime.orElse(null),
            selfMonitoringLastDateTime.orElse(null)
        )
    }.subscribeOnComputation()
        .debounce(50, TimeUnit.MILLISECONDS) // some of the shared prefs can be changed together
        .map {
            it.mapToQuarantineStatus()
        }
        // don't notify again if nothing has changed
        .distinctUntilChanged { oldState, newState ->
            // display end quarantine info if quarantine has ended
            if (oldState is QuarantineStatus.Jailed.Limited && newState is QuarantineStatus.Free) {
                setShowQuarantineEnd()
            }
            oldState == newState
        }
        .doOnNext {
            updateNotifications(it)
        }

    /**
     * Logic of making quarantine status by prerequisites.
     */
    private fun QuarantinePrerequisitesHolder.mapToQuarantineStatus(): QuarantineStatus {
        return when {
            /**
             * Own health state is Red.
             * Quarantine never ends.
             */
            medicalConfirmationFirstDateTime != null -> {
                QuarantineStatus.Jailed.Forever
            }
            /**
             * Quarantine end date computation as maximum of possible ends.
             */
            redContactLastDateTime != null || yellowContactLastDateTime != null || selfDiagnoseLastDateTime != null -> {
                val redWarningQuarantinedHours = configuration.redWarningQuarantine?.toLong()
                    .safeMap("redWarningQuarantine is null", defaultValue = TimeUnit.DAYS.toHours(14L))
                val yellowWarningQuarantinedHours = configuration.yellowWarningQuarantine?.toLong()
                    .safeMap("yellowWarningQuarantine is null", defaultValue = TimeUnit.DAYS.toHours(7L))
                val selfDiagnoseQuarantinedHours = configuration.selfDiagnosedQuarantine?.toLong()
                    .safeMap("selfDiagnosedQuarantine is null", defaultValue = TimeUnit.DAYS.toHours(7L))

                // Quarantine end in contact time + 14 days
                val redWarningQuarantineUntil = redContactLastDateTime?.plusHours(redWarningQuarantinedHours)
                // Quarantine end in contact time + 7 days
                val yellowWarningQuarantineUntil = yellowContactLastDateTime?.plusHours(yellowWarningQuarantinedHours)
                // Quarantine end in diagnose time + 7 days
                val selfDiagnoseQuarantineUntil = selfDiagnoseLastDateTime?.plusHours(selfDiagnoseQuarantinedHours)

                val quarantinedUntil = listOfNotNull(redWarningQuarantineUntil, yellowWarningQuarantineUntil, selfDiagnoseQuarantineUntil)
                    .max()

                if (quarantinedUntil != null) {
                    QuarantineStatus.Jailed.Limited(quarantinedUntil,
                        (redContactLastDateTime != null || yellowContactLastDateTime != null) && selfDiagnoseLastDateTime == null)
                } else {
                    Timber.e(SilentError("quarantinedUntil is null"))
                    QuarantineStatus.Free()
                }
            }
            selfMonitoringLastDateTime != null -> {
                QuarantineStatus.Free(true)
            }
            else -> QuarantineStatus.Free()
        }
    }

    /**
     * Schedule, reschedule or cancel notifications by [quarantineStatus].
     * Notifications:
     * - each 6 hours for self retesting
     * - at the end of the quarantine
     */
    private fun updateNotifications(quarantineStatus: QuarantineStatus) {
        when (quarantineStatus) {
            is QuarantineStatus.Jailed.Limited -> {
                enqueueSelfRetestingReminder(workManager)
                enqueueEndQuarantineReminder(workManager, quarantineStatus.end)
            }
            QuarantineStatus.Jailed.Forever -> {
                enqueueSelfRetestingReminder(workManager)
                cancelEndQuarantineReminder(workManager)
            }
            is QuarantineStatus.Free -> {
                if (quarantineStatus.selfMonitoring.not()) {
                    cancelSelfRetestingReminder(workManager)
                }
                cancelEndQuarantineReminder(workManager)
            }
        }
    }

    override fun observeQuarantineState(): Observable<QuarantineStatus> {
        return quarantineStateObservable
    }

    override fun reportMedicalConfirmation(timeOfReport: ZonedDateTime) {
        dateOfFirstMedicalConfirmation = timeOfReport
    }

    override fun reportPositiveSelfDiagnose(timeOfReport: ZonedDateTime) {
        val diagnosedTime = dateOfFirstSelfDiagnose
        if (diagnosedTime == null) {
            dateOfFirstSelfDiagnose = timeOfReport
        }
        dateOfLastSelfDiagnose = timeOfReport
    }

    override fun reportPositiveSelfDiagnoseFromBackup() {
        dateOfFirstSelfDiagnose = dateOfFirstSelfDiagnoseBackup
        dateOfLastSelfDiagnose = dateOfLastSelfDiagnoseBackup
        dateOfFirstSelfDiagnoseBackup = null
        dateOfLastSelfDiagnoseBackup = null
    }

    override fun revokeMedicalConfirmation() {
        dateOfFirstMedicalConfirmation = null
    }

    override fun revokePositiveSelfDiagnose(backup: Boolean) {
        if (backup) {
            dateOfFirstSelfDiagnoseBackup = dateOfFirstSelfDiagnose
            dateOfLastSelfDiagnoseBackup = dateOfLastSelfDiagnose
        }

        dateOfFirstSelfDiagnose = null
        dateOfLastSelfDiagnose = null
    }

    override fun reportSelfMonitoring(timeOfReport: ZonedDateTime) {
        dateOfLastSelfMonitoringInstruction = timeOfReport
    }

    override fun revokeSelfMonitoring() {
        dateOfLastSelfMonitoringInstruction = null
    }

    override fun receivedWarning(warningType: WarningType, timeOfContact: ZonedDateTime) {
        when (warningType) {
            WarningType.YELLOW -> dateOfLastYellowContact = timeOfContact
            WarningType.RED -> dateOfLastRedContact = timeOfContact
        }
    }

    override fun setShowQuarantineEnd() {
        showQuarantineEnd = true
    }

    override fun quarantineEndSeen() {
        showQuarantineEnd = false
    }

    override fun observeShowQuarantineEnd(): Observable<Boolean> {
        return preferences.observeBoolean(PREF_SHOW_QUARANTINE_END, false)
    }

    override fun revokeLastRedContactDate() {
        dateOfLastRedContact = null
    }
}

private data class QuarantinePrerequisitesHolder(
    val configuration: DbConfiguration,
    val medicalConfirmationFirstDateTime: ZonedDateTime?,
    val selfDiagnoseLastDateTime: ZonedDateTime?,
    val redContactLastDateTime: ZonedDateTime?,
    val yellowContactLastDateTime: ZonedDateTime?,
    val selfMonitoringLastDateTime: ZonedDateTime?
)

/**
 * Status about quarantine of the user.
 */
sealed class QuarantineStatus {

    /**
     * User must be in the quarantine.
     */
    sealed class Jailed : QuarantineStatus() {

        /**
         * Quarantine ends at [end] time.
         * After this time user's state is [Free].
         */
        data class Limited(val end: ZonedDateTime, val byContact: Boolean) : Jailed()

        /**
         * Quarantine never ends.
         */
        object Forever : Jailed()
    }

    /**
     * User doesn't have to be in the quarantine.
     */
    data class Free(val selfMonitoring: Boolean = false) : QuarantineStatus()
}