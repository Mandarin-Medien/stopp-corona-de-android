package de.schwerin.stoppCoronaDE

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import de.schwerin.stoppCoronaDE.constants.Constants
import de.schwerin.stoppCoronaDE.constants.isDebug
import de.schwerin.stoppCoronaDE.di.*
import de.schwerin.stoppCoronaDE.skeleton.core.BaseApp
import org.koin.dsl.module.Module

/**
 * Application class.
 */
class App : BaseApp() {

    override val strictModeEnabled: Boolean = isDebug && Constants.Debug.STRICT_MODE_ENABLED
    override val koinModules: List<Module> = listOf(
        persistenceModule,
        remoteModule,
        repositoryModule,
        viewModelModule,
        scopeModule,
        contextDependentModule,
        flavourDependentModule
    )

    override fun onPostCreate() {
        super.onPostCreate()

        onPostCreateFlavourDependent()

        // Create notification channels
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelInfectionMessage = NotificationChannel(
                Constants.NotificationChannels.CHANNEL_INFECTION_MESSAGE,
                getString(R.string.general_infection_messages_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channelInfectionMessage)

            val channelSelfRetest = NotificationChannel(
                Constants.NotificationChannels.CHANNEL_SELF_RETEST,
                getString(R.string.general_self_retest_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channelSelfRetest)

            val channelRecovered = NotificationChannel(
                Constants.NotificationChannels.CHANNEL_RECOVERED,
                getString(R.string.general_self_recovered_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channelRecovered)

            val channelQuarantine = NotificationChannel(
                Constants.NotificationChannels.CHANNEL_QUARANTINE,
                getString(R.string.general_self_quarantine_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channelQuarantine)

            val channelAutomaticDetection = NotificationChannel(
                Constants.NotificationChannels.CHANNEL_AUTOMATIC_DETECTION,
                getString(R.string.general_automatic_bt_detection_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            channelAutomaticDetection.setShowBadge(false)
            notificationManager.createNotificationChannel(channelAutomaticDetection)
        }
    }
}