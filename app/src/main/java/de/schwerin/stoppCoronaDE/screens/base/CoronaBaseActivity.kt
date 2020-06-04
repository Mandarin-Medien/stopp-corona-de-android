package de.schwerin.stoppCoronaDE.screens.base

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.LayoutRes
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.constants.isDebug
import de.schwerin.stoppCoronaDE.model.entities.infection.message.MessageType
import de.schwerin.stoppCoronaDE.screens.debug.discovery.startDebugDiscoveryFragment
import de.schwerin.stoppCoronaDE.screens.debug.events.startDebugAutomaticEventsFragment
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.activity.BaseActivity
import org.koin.androidx.viewmodel.ext.android.getViewModel

/**
 * Base activity specific for Corona project.
 */
open class CoronaBaseActivity(@LayoutRes layout: Int = R.layout.framelayout) : BaseActivity(layout) {

    private lateinit var debugViewModel: DebugViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // instantiated only on debug build
        if (isDebug) {
            debugViewModel = getViewModel()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (isDebug) {
            menuInflater.inflate(R.menu.debug, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.debugMenuNotificationInfectionRed -> {
                debugViewModel.displayInfectionNotification(MessageType.InfectionLevel.Red)
                true
            }
            R.id.debugMenuNotificationInfectionYellow -> {
                debugViewModel.displayInfectionNotification(MessageType.InfectionLevel.Yellow)
                true
            }
            R.id.debugMenuNotificationSelfRetest -> {
                debugViewModel.displaySelfRetestNotification()
                true
            }
            R.id.debugMenuNotificationSomeoneRecovered -> {
                debugViewModel.displaySomeoneHasRecoveredNotification()
                true
            }
            R.id.debugMenuNotificationEndQuarantine -> {
                debugViewModel.displayEndQuarantineNotification()
                true
            }
            R.id.debugMenuQuarantineStatus -> {
                val quarantineStatus = debugViewModel.getQuarantineStatus()
                Toast.makeText(this, quarantineStatus.toString(), Toast.LENGTH_LONG).show()
                true
            }
            R.id.debugMenuInfectionMessagesOutgoingRed -> {
                debugViewModel.addOutgoingMessageRed()
                true
            }
            R.id.debugMenuInfectionMessagesOutgoingYellow -> {
                debugViewModel.addOutgoingMessageYellow()
                true
            }
            R.id.debugMenuInfectionMessagesIncomingRed -> {
                debugViewModel.addIncomingMessageRed()
                true
            }
            R.id.debugMenuInfectionMessagesIncomingYellow -> {
                debugViewModel.addIncomingMessageYellow()
                true
            }
            R.id.debugMenuInfectionMessagesIncomingGreen -> {
                debugViewModel.addIncomingMessageGreen()
                true
            }
            R.id.debugMenuDiscovery -> {
                startDebugDiscoveryFragment()
                true
            }
            R.id.debugMenuEvents -> {
                startDebugAutomaticEventsFragment()
                true
            }
            R.id.debugAddContact -> {
                debugViewModel.addRandomContact()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}