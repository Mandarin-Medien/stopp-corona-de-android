package de.schwerin.stoppCoronaDE.model.repositories

import android.content.SharedPreferences
import de.schwerin.stoppCoronaDE.constants.Constants
import de.schwerin.stoppCoronaDE.model.db.dao.NearbyRecordDao
import de.schwerin.stoppCoronaDE.skeleton.core.utils.booleanSharedPreferencesProperty
import de.schwerin.stoppCoronaDE.utils.asDbObservable
import io.reactivex.Observable

/**
 * Repository for managing dashboard content.
 */
interface DashboardRepository {

    val showMicrophoneExplanationDialog: Boolean

    /**
     * Observes the number of met people.
     */
    fun observeSavedEncountersNumber(): Observable<Int>

    /**
     * Do not show explanation dialog again.
     */
    fun setMicrophoneExplanationDialogShown()
}

class DashboardRepositoryImpl(
    private val nearbyRecordDao: NearbyRecordDao,
    preferences: SharedPreferences
) : DashboardRepository {

    companion object {
        private const val PREF_MICROPHONE_EXPLANATION_DIALOG_SHOW_AGAIN =
            Constants.Prefs.DASHBOARD_PREFIX + "microphone_explanation_dialog_show_again"
    }

    override var showMicrophoneExplanationDialog: Boolean
        by preferences.booleanSharedPreferencesProperty(PREF_MICROPHONE_EXPLANATION_DIALOG_SHOW_AGAIN, true)
        private set

    override fun observeSavedEncountersNumber(): Observable<Int> {
        return nearbyRecordDao.observeNumberOfRecords().asDbObservable()
    }

    override fun setMicrophoneExplanationDialogShown() {
        showMicrophoneExplanationDialog = false
    }
}