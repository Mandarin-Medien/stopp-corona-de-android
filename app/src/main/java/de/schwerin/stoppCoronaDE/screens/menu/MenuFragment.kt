package de.schwerin.stoppCoronaDE.screens.menu

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.model.entities.infection.message.MessageType
import de.schwerin.stoppCoronaDE.screens.base.CoronaPortraitBaseActivity
import de.schwerin.stoppCoronaDE.screens.dashboard.DashboardViewModel
import de.schwerin.stoppCoronaDE.screens.handshake.startHandshakeFragment
import de.schwerin.stoppCoronaDE.screens.onboarding.startOnboardingFragment
import de.schwerin.stoppCoronaDE.screens.questionnaire.startQuestionnaireFragment
import de.schwerin.stoppCoronaDE.screens.reporting.startReportingActivity
import de.schwerin.stoppCoronaDE.screens.webView.startWebView
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.activity.startFragmentActivity
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.fragment.BaseFragment
import de.schwerin.stoppCoronaDE.skeleton.core.utils.dipif
import de.schwerin.stoppCoronaDE.skeleton.core.utils.observeOnMainThread
import de.schwerin.stoppCoronaDE.utils.shareApp
import de.schwerin.stoppCoronaDE.utils.startDefaultBrowser
import de.schwerin.stoppCoronaDE.utils.view.AccurateScrollListener
import de.schwerin.stoppCoronaDE.utils.view.LinearLayoutManagerAccurateOffset
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import io.reactivex.rxkotlin.plusAssign
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Screen with menu content.
 */
class MenuFragment : BaseFragment(R.layout.menu_fragment) {

    override val isToolbarVisible: Boolean = true

    override fun getTitle(): String? {
        return "" // blank
    }

    private val versionClickSubject = PublishSubject.create<Unit>()

    /**
     * Use the DashboardViewModel to not have ownHealthState observation logic twice
     */
    private val viewModel: DashboardViewModel by viewModel()

    private val controller: MenuController by lazy {
        MenuController(
            context = requireContext(),
            onOnboardingClick = {
                activity?.startOnboardingFragment(skipRouter = true)
            },
            onExternalLinkClick = { url ->
                startDefaultBrowser(url)
            },
            onOpenSourceLicenceClick = {
                OssLicensesMenuActivity.setActivityTitle(getString(R.string.start_menu_item_2_1_open_source_licenses))
                startActivity(Intent(activity, OssLicensesMenuActivity::class.java))
            },
            onPrivacyDataClick = {
                startWebView(R.string.start_menu_item_2_2_data_privacy, "privacy-and-terms-of-use")
            },
            onImprintClick = {
                startWebView(R.string.start_menu_item_2_3_imprint, "imprint")
            },
            onVersionClick = {

            },
            onHandshakeClick = {
                startHandshakeFragment()
            },
            onCheckSymptomsClick = {
                startQuestionnaireFragment()
            },
            onReportOfficialSicknessClick = {
                startReportingActivity(MessageType.InfectionLevel.Red)
            },
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

    override fun onInitActionBar(actionBar: ActionBar?, toolbar: Toolbar?) {
        super.onInitActionBar(actionBar, toolbar)
        toolbar?.setNavigationIcon(R.drawable.ic_clear)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(contentRecyclerView) {
            setController(controller)
            layoutManager = LinearLayoutManagerAccurateOffset(requireContext(), accurateScrollListener)
            addOnScrollListener(accurateScrollListener)
        }

        disposables += viewModel.observeOwnHealthStatus()
            .observeOnMainThread()
            .subscribe {
                controller.ownHealthStatus = it
            }

        controller.requestModelBuild()
    }

    override fun onDestroyView() {
        contentRecyclerView.removeOnScrollListener(accurateScrollListener)
        super.onDestroyView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}

fun Fragment.startMenuFragment() {
    startFragmentActivity<CoronaPortraitBaseActivity>(
        fragmentName = MenuFragment::class.java.name
    )
}
