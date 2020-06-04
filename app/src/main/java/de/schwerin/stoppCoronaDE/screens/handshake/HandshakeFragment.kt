package de.schwerin.stoppCoronaDE.screens.handshake

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.model.repositories.ApiConnectionState
import de.schwerin.stoppCoronaDE.model.repositories.NearbyHandshakeState
import de.schwerin.stoppCoronaDE.screens.base.CoronaPortraitBaseActivity
import de.schwerin.stoppCoronaDE.screens.handshake.dialog.HandshakeExplanationDialog
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.activity.startFragmentActivity
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.fragment.BaseFragment
import de.schwerin.stoppCoronaDE.skeleton.core.utils.dipif
import de.schwerin.stoppCoronaDE.skeleton.core.utils.observeOnMainThread
import de.schwerin.stoppCoronaDE.skeleton.core.utils.visible
import de.schwerin.stoppCoronaDE.utils.backgroundColor
import de.schwerin.stoppCoronaDE.utils.shareApp
import de.schwerin.stoppCoronaDE.utils.view.AccurateScrollListener
import de.schwerin.stoppCoronaDE.utils.view.LinearLayoutManagerAccurateOffset
import com.airbnb.epoxy.EpoxyVisibilityTracker
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.nearby.messages.MessagesClient
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_handshake.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/**
 * Screen for discovering nearby contacts and executing handshakes
 */
class HandshakeFragment : BaseFragment(R.layout.fragment_handshake) {

    override val isToolbarVisible = true

    override fun getTitle(): String? {
        return getString(R.string.handshake_title)
    }

    private val viewModel: HandshakeViewModel by viewModel()

    private val messagesClient: MessagesClient by inject { parametersOf(requireActivity()) }
    private var handshakeStateDisposable: Disposable? = null

    private val controller: HandshakeController by lazy {
        HandshakeController(
            context = requireContext(),
            onInfoClicked = { HandshakeExplanationDialog().show() },
            onSelectAllContacts = viewModel::selectAllContacts,
            onContactSelected = viewModel::selectContact,
            onOpenSettingsClicked = ::openPlayServices,
            onShareAppClick = { shareApp() }
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

    override fun onInitActionBar(actionBar: ActionBar?, toolbar: Toolbar?) {
        super.onInitActionBar(actionBar, toolbar)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationIcon(R.drawable.ic_back)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(contentRecyclerView) {
            layoutManager = LinearLayoutManagerAccurateOffset(requireContext(), accurateScrollListener)
            addOnScrollListener(accurateScrollListener)
            setController(controller)
            EpoxyVisibilityTracker().attach(this)
        }

        controller.requestModelBuild()

        disposables += viewModel.observeContacts()
            .observeOnMainThread()
            .subscribe {
                controller.contactList = it
            }

        disposables += viewModel.observeSaveButtonState()
            .observeOnMainThread()
            .subscribe { state ->
                when (state) {
                    SaveButtonState.Enabled -> btnSave.isEnabled = true
                    SaveButtonState.Disabled -> btnSave.isEnabled = false
                }
            }

        disposables += viewModel.observeSelectAllButtonState()
            .observeOnMainThread()
            .subscribe { state ->
                when (state) {
                    SelectAllButtonState.Checked -> controller.selectAllChecked = true
                    SelectAllButtonState.Unchecked -> controller.selectAllChecked = false
                }
            }

        disposables += viewModel.observeLoadingIndicator()
            .observeOnMainThread()
            .subscribe { state ->
                when (state) {
                    LoadingIndicatorState.Visible -> controller.showLoadingIndicator = true
                    LoadingIndicatorState.Invisible -> controller.showLoadingIndicator = false
                }
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

        controller.identification = viewModel.personalIdentification

        btnSave.setOnClickListener {
            viewModel.saveSelectedContacts()
        }
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

    private fun permissionGranted(): Boolean {
        return requireContext().packageManager.checkPermission(Manifest.permission.RECORD_AUDIO,
            GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPlayServicesPermission() {
        if (permissionGranted()) {
            controller.permissionsGranted = true
            contentRecyclerView.backgroundColor(R.color.white)
            bottomSheet.visible = true
            btnSave.visible = true

            viewModel.resume(requireActivity())
        } else {
            controller.permissionsGranted = false
            contentRecyclerView.backgroundColor(R.color.whiteGray)
            bottomSheet.visible = false
            btnSave.visible = false

            viewModel.permissionDenied()
        }
    }

    private fun openPlayServices() {
        startActivity(Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE, null)
        ))
    }

    override fun onDestroyView() {
        contentRecyclerView.removeOnScrollListener(accurateScrollListener)
        controller.showLoadingIndicator  = true
        super.onDestroyView()
    }
}

fun Activity.startHandshakeFragment() {
    startFragmentActivity<CoronaPortraitBaseActivity>(
        fragmentName = HandshakeFragment::class.java.name
    )
}

fun Fragment.startHandshakeFragment() {
    activity?.startHandshakeFragment()
}

