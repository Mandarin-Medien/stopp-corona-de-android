package de.schwerin.stoppCoronaDE.model.repositories

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.constants.Constants.NotificationChannels
import de.schwerin.stoppCoronaDE.model.entities.infection.info.WarningType
import de.schwerin.stoppCoronaDE.model.entities.infection.message.MessageType
import de.schwerin.stoppCoronaDE.model.repositories.other.ContextInteractor
import de.schwerin.stoppCoronaDE.screens.dashboard.getDashboardActivityIntent
import de.schwerin.stoppCoronaDE.screens.infection_info.getInfectionInfoFragmentIntent
import de.schwerin.stoppCoronaDE.screens.questionnaire.getQuestionnaireIntent
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchers
import de.schwerin.stoppCoronaDE.utils.string
import kotlinx.coroutines.CoroutineScope
import java.util.UUID
import kotlin.coroutines.CoroutineContext

/**
 * Repository that manages the notifications.
 */
interface NotificationsRepository {

    /**
     * Display notification about received infection by [infectionLevel].
     */
    fun displayInfectionNotification(infectionLevel: MessageType.InfectionLevel)

    /**
     * Display notification to remind a self test.
     */
    fun displaySelfRetestNotification()

    /**
     * Display notification that someone has recovered.
     */
    fun displaySomeoneHasRecoveredNotification()

    /**
     * Display notification when quarantine has ended.
     * It can happen in two cases:
     * - User has been quarantined for specific amount of time without symptoms.
     * - All contacts has been recovered and user don't have any symptoms.
     */
    fun displayEndQuarantineNotification()

    /**
     * Hide notification by [id].
     */
    fun hideNotification(id: Int)
}

class NotificationsRepositoryImpl(
    private val appDispatchers: AppDispatchers,
    private val contextInteractor: ContextInteractor,
    private val dataPrivacyRepository: DataPrivacyRepository
) : NotificationsRepository,
    CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = appDispatchers.Default

    private val context: Context
        get() = contextInteractor.applicationContext

    private val notificationManager: NotificationManager
        get() = contextInteractor.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Flags to have clear task.
     * It must be added to the first activity intent.
     */
    private val firstActivityFlags = Intent.FLAG_ACTIVITY_SINGLE_TOP or // call onNewIntent() on running activity
        Intent.FLAG_ACTIVITY_NEW_TASK or // can open activity from service
        Intent.FLAG_ACTIVITY_CLEAR_TOP // remove stack and place this activity on top (will continue)

    override fun displayInfectionNotification(infectionLevel: MessageType.InfectionLevel) {
        val (title, message) = when (infectionLevel.warningType) {
            WarningType.YELLOW -> {
                context.string(R.string.local_notification_suspected_sick_contact_headline) to
                    context.string(R.string.local_notification_suspected_sick_contact_message)
            }
            WarningType.RED -> {
                context.string(R.string.local_notification_sick_contact_headline) to
                    context.string(R.string.local_notification_sick_contact_message)
            }
            WarningType.REVOKE -> {
                context.string(R.string.local_notification_someone_has_recovered_headline) to
                    context.string(R.string.local_notification_quarantine_end_message)
            }
        }

        buildNotification(
            title = title,
            message = message,
            priority = NotificationCompat.PRIORITY_MAX,
            pendingIntent = buildPendingIntentWithActivityStack {
                addNextIntent(context.getDashboardActivityIntent().addFlags(firstActivityFlags))
                addNextIntent(context.getInfectionInfoFragmentIntent())
            },
            channelId = NotificationChannels.CHANNEL_INFECTION_MESSAGE
        ).show()
    }

    override fun displaySelfRetestNotification() {
        val title = context.string(R.string.local_notification_self_retest_headline)

        buildNotification(
            title = title,
            pendingIntent = buildPendingIntentWithActivityStack {
                addNextIntent(context.getDashboardActivityIntent().addFlags(firstActivityFlags))
                addNextIntent(context.getQuestionnaireIntent())
            },
            channelId = NotificationChannels.CHANNEL_SELF_RETEST
        ).show()
    }

    override fun displaySomeoneHasRecoveredNotification() {
        val title = context.string(R.string.local_notification_someone_has_recovered_headline)

        buildNotification(
            title = title,
            pendingIntent = buildPendingIntentWithActivityStack {
                addNextIntent(context.getDashboardActivityIntent().addFlags(firstActivityFlags))
            },
            channelId = NotificationChannels.CHANNEL_RECOVERED
        ).show()
    }

    override fun displayEndQuarantineNotification() {
        val title = context.string(R.string.local_notification_quarantine_end_headline)
        val message = context.string(R.string.local_notification_quarantine_end_message)

        buildNotification(
            title = title,
            message = message,
            pendingIntent = buildPendingIntentWithActivityStack {
                addNextIntent(context.getDashboardActivityIntent().addFlags(firstActivityFlags))
            },
            channelId = NotificationChannels.CHANNEL_QUARANTINE
        ).show()
    }

    override fun hideNotification(id: Int) {
        notificationManager.cancel(id)
    }

    private fun buildNotification(
        title: String,
        pendingIntent: PendingIntent,
        channelId: String,
        message: String? = null,
        priority: Int = NotificationCompat.PRIORITY_DEFAULT,
        ongoing: Boolean = false
    ): Notification {
        dataPrivacyRepository.assertDataPrivacyAccepted()

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.ic_red_cross)
            .setOngoing(ongoing)
            .let {
                if (message != null) {
                    it.setContentText(message)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                } else it
            }
            .build()
    }

    private fun buildPendingIntentWithActivityStack(activityStackBuilder: TaskStackBuilder.() -> Unit): PendingIntent {
        val requestCode = UUID.randomUUID().mostSignificantBits.toInt() // should be unique

        return TaskStackBuilder.create(context).apply {
            activityStackBuilder()
        }.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT)!! // !! because no FLAG_NO_CREATE
    }

    private fun Notification.show() {
        notificationManager.notify(hashCode(), this)
    }
}