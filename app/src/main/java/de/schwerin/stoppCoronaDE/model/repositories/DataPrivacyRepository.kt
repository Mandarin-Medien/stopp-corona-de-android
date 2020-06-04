package de.schwerin.stoppCoronaDE.model.repositories

import android.content.SharedPreferences
import de.schwerin.stoppCoronaDE.constants.Constants.Prefs.DATA_PRIVACY_REPOSITORY_PREFIX
import de.schwerin.stoppCoronaDE.model.api.ApiError
import de.schwerin.stoppCoronaDE.skeleton.core.utils.nullableZonedDateTimeSharedPreferencesProperty
import de.schwerin.stoppCoronaDE.skeleton.core.utils.observeNullableZonedDateTime
import kotlinx.coroutines.suspendCancellableCoroutine
import org.threeten.bp.ZonedDateTime
import kotlin.coroutines.resume

/**
 * Repository for managing the state of the data privacy check.
 */
interface DataPrivacyRepository {

    /**
     * Indicated if data privacy was fully accepted.
     */
    val dataPrivacyAccepted: Boolean

    /**
     * Data privacy was seen and accepted so we don't want to show it anymore.
     */
    fun setDataPrivacyAccepted()

    /**
     * Check if data privacy is accepted.
     * If not, error is thrown.
     * @throws [ApiError.Critical.DataPrivacyNotAcceptedYet]
     */
    fun assertDataPrivacyAccepted()

    /**
     * Block the current coroutine flow until we have accepted data privacy.
     * When coroutine canceled, waiting is canceled as well.
     */
    suspend fun awaitForAcceptanceState()
}

class DataPrivacyRepositoryImpl(
    private val preferences: SharedPreferences
) : DataPrivacyRepository {

    companion object {
        private const val PREF_DATA_PRIVACY_ACCEPTED_TIMESTAMP = DATA_PRIVACY_REPOSITORY_PREFIX + "_data_privacy_timestamp"
        private const val PREF_DATA_PRIVACY_ACCEPTED_TIMESTAMP_V1_1 = DATA_PRIVACY_REPOSITORY_PREFIX + "data_privacy_timestamp_v1.1"
    }

    private var dataPrivacyAcceptedTimestamp: ZonedDateTime?
        by preferences.nullableZonedDateTimeSharedPreferencesProperty(PREF_DATA_PRIVACY_ACCEPTED_TIMESTAMP)

    @Suppress("PrivatePropertyName")
    private var dataPrivacyAcceptedTimestampV1_1: ZonedDateTime?
        by preferences.nullableZonedDateTimeSharedPreferencesProperty(PREF_DATA_PRIVACY_ACCEPTED_TIMESTAMP_V1_1)

    override val dataPrivacyAccepted: Boolean
        get() = dataPrivacyAcceptedTimestamp != null && dataPrivacyAcceptedTimestampV1_1 != null

    override fun setDataPrivacyAccepted() {
        /**
         * Don't override the v1.0 timestamp when accepting terms and conditions for v1.1
         */
        if (dataPrivacyAcceptedTimestamp == null) {
            dataPrivacyAcceptedTimestamp = ZonedDateTime.now()
        }

        dataPrivacyAcceptedTimestampV1_1 = ZonedDateTime.now()
    }

    override fun assertDataPrivacyAccepted() {
        if (dataPrivacyAccepted.not()) {
            throw ApiError.Critical.DataPrivacyNotAcceptedYet
        }
    }

    override suspend fun awaitForAcceptanceState() {
        suspendCancellableCoroutine<Unit> { continuation ->
            val disposable = preferences.observeNullableZonedDateTime(PREF_DATA_PRIVACY_ACCEPTED_TIMESTAMP_V1_1)
                .filter { it.isPresent } // wait for an approval
                .firstOrError()
                .subscribe({
                    continuation.resume(Unit)
                }, {
                    continuation.resume(Unit)
                })
            continuation.invokeOnCancellation {
                disposable.dispose()
            }
        }
    }
}

