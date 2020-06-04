package de.schwerin.stoppCoronaDE.screens.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.model.entities.infection.message.MessageType
import de.schwerin.stoppCoronaDE.screens.dashboard.dialog.GooglePlayServicesNotAvailableDialog
import de.schwerin.stoppCoronaDE.screens.dashboard.dialog.MicrophoneExplanationDialog
import de.schwerin.stoppCoronaDE.screens.handshake.startHandshakeFragment
import de.schwerin.stoppCoronaDE.screens.history.startContactHistoryFragment
import de.schwerin.stoppCoronaDE.screens.infection_info.startInfectionInfoFragment
import de.schwerin.stoppCoronaDE.screens.menu.startMenuFragment
import de.schwerin.stoppCoronaDE.screens.questionnaire.guideline.startQuestionnaireGuidelineFragment
import de.schwerin.stoppCoronaDE.screens.questionnaire.selfmonitoring.startQuestionnaireSelfMonitoringWithSubmissionDataFragment
import de.schwerin.stoppCoronaDE.screens.questionnaire.startQuestionnaireFragment
import de.schwerin.stoppCoronaDE.screens.reporting.reportStatus.guideline.startCertificateReportGuidelinesFragment
import de.schwerin.stoppCoronaDE.screens.reporting.startReportingActivity
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.fragment.BaseFragment
import de.schwerin.stoppCoronaDE.skeleton.core.utils.PermissionChecker
import de.schwerin.stoppCoronaDE.skeleton.core.utils.dipif
import de.schwerin.stoppCoronaDE.skeleton.core.utils.observeOnMainThread
import de.schwerin.stoppCoronaDE.utils.shareApp
import de.schwerin.stoppCoronaDE.utils.view.AccurateScrollListener
import de.schwerin.stoppCoronaDE.utils.view.LinearLayoutManagerAccurateOffset
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.nearby.messages.MessagesClient
import de.schwerin.stoppCoronaDE.constants.Constants
import de.schwerin.stoppCoronaDE.model.repositories.ApiConnectionState
import de.schwerin.stoppCoronaDE.model.repositories.NearbyHandshakeState
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/**
 * Sample dashboard.
 */
class DashboardFragment : BaseFragment(R.layout.fragment_dashboard), PermissionChecker {

    companion object {
        private const val REQUEST_MICROPHONE_DIALOG = Constants.Request.REQUEST_DASHBOARD + 1
    }

    override val requiredPermissions: List<String>
        get() = listOf(Manifest.permission.ACCESS_COARSE_LOCATION)

    override val askForPermissionOnViewCreated: Boolean
        get() = false

    override val isToolbarVisible: Boolean = true

    override fun getTitle(): String? {
        return "" // blank
    }

    private val messagesClient: MessagesClient by inject { parametersOf(requireActivity()) }
    private var handshakeStateDisposable: Disposable? = null

    private val viewModel: DashboardViewModel by viewModel()

    private val controller: DashboardController by lazy {
        DashboardController(
            context = requireContext(),
            onManualHandshakeClick = {
                checkPlayServicesAvailabilityAndStartHandshakeFragment()
            },
            onSavedEncountersClick = {
                startContactHistoryFragment()
            },
            onFeelingClick = {
                startQuestionnaireFragment()
            },
            onReportClick = {
                startReportingActivity(MessageType.InfectionLevel.Red)
            },
            onHealthStatusClick = { healthStatusData ->
                when (healthStatusData) {
                    is HealthStatusData.SicknessCertificate -> {
                        startCertificateReportGuidelinesFragment()
                    }
                    HealthStatusData.SelfTestingSymptomsMonitoring -> {
                        startQuestionnaireSelfMonitoringWithSubmissionDataFragment()
                    }
                    is HealthStatusData.SelfTestingSuspicionOfSickness -> {
                        startQuestionnaireGuidelineFragment()
                    }
                    is HealthStatusData.ContactsSicknessInfo -> {
                        startInfectionInfoFragment()
                    }
                }
            },
            onRevokeSuspicionClick = {
                startReportingActivity(MessageType.Revoke.Suspicion)
            },
            onPresentMedicalReportClick = {
                startReportingActivity(MessageType.InfectionLevel.Red)
            },
            onCheckSymptomsAgainClick = {
                startQuestionnaireFragment()
            },
            onSomeoneHasRecoveredCloseClick = viewModel::someoneHasRecoveredSeen,
            onQuarantineEndCloseClick = viewModel::quarantineEndSeen,
            onShareAppClick = {
                shareApp()
            },
            onRevokeSicknessClick = {
                startReportingActivity(MessageType.Revoke.Sickness)
            }
        )
    }

    private val accurateScrollListener by lazy {
        AccurateScrollListener(
            onScroll = { scrolledDistance ->
                transparentAppBar.elevation = if (scrolledDistance > 0) {
                    requireContext().dipif(4)
                } else {
                    0f
                }
            }
        )
    }

    private fun checkPlayServicesPermission() {
        if (permissionGranted()) {
            viewModel.resume(requireActivity())
        } else {
            viewModel.permissionDenied()
        }
    }

    private fun permissionGranted(): Boolean {
        return requireContext().packageManager.checkPermission(Manifest.permission.RECORD_AUDIO,
            GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE) == PackageManager.PERMISSION_GRANTED
    }

    override fun onInitActionBar(actionBar: ActionBar?, toolbar: Toolbar?) {
        super.onInitActionBar(actionBar, toolbar)
        toolbar?.setNavigationIcon(R.drawable.ic_drawer)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(contentRecyclerView) {
            setController(controller)
            layoutManager = LinearLayoutManagerAccurateOffset(requireContext(), accurateScrollListener)
            addOnScrollListener(accurateScrollListener)
        }

        disposables += viewModel.observeConnection()
            .observeOnMainThread()
            .subscribe { state ->
                when (state) {
                    ApiConnectionState.Connected -> {
                        viewModel.startConnection(messagesClient)
                    }
                    ApiConnectionState.Suspended -> {
                    }
                    ApiConnectionState.Failed -> {
                    }
                }
            }

        handshakeStateDisposable = viewModel.observeHandshakeState()
            .observeOnMainThread()
            .subscribe { state ->
                if (state == NearbyHandshakeState.Expired) {
                    viewModel.retry()
                }
            }
        disposables += handshakeStateDisposable!!

        disposables += viewModel.observeSavedEncounters()
            .observeOnMainThread()
            .subscribe { savedEncounters ->
                controller.savedEncounters = savedEncounters
            }

        disposables += viewModel.observeOwnHealthStatus()
            .observeOnMainThread()
            .subscribe {
                controller.ownHealthStatus = it
            }

        disposables += viewModel.observeContactsHealthStatus()
            .observeOnMainThread()
            .subscribe {
                controller.contactsHealthStatus = it
            }

        disposables += viewModel.observeShowQuarantineEnd()
            .observeOnMainThread()
            .subscribe {
                controller.showQuarantineEnd = it
            }

        disposables += viewModel.observeSomeoneHasRecoveredStatus()
            .observeOnMainThread()
            .subscribe {
                controller.someoneHasRecoveredHealthStatus = it
            }

        controller.requestModelBuild()
    }

    override fun onResume() {
        super.onResume()

        checkPlayServicesPermission()
    }

    override fun onStop() {
        viewModel.stopConnection()
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        handshakeStateDisposable?.dispose()
        handshakeStateDisposable = null
        viewModel.pause(requireActivity())
    }

    override fun onDestroyView() {
        contentRecyclerView.removeOnScrollListener(accurateScrollListener)
        super.onDestroyView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                startMenuFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkPlayServicesAvailabilityAndStartHandshakeFragment() {
        when (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(requireContext())) {
            ConnectionResult.SUCCESS -> {
                if (viewModel.showMicrophoneExplanationDialog) {
                    // will call startHandshakeFragment() if OK
                    MicrophoneExplanationDialog(this).showForResult(REQUEST_MICROPHONE_DIALOG)
                } else {
                    startHandshakeFragment()
                }
            }
            else -> {
                GooglePlayServicesNotAvailableDialog().show()
            }
        }
    }

    override fun onPermissionGranted(permission: String) {

    }
}