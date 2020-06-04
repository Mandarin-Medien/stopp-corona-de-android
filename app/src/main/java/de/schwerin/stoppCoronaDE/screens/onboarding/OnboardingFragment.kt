package de.schwerin.stoppCoronaDE.screens.onboarding

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.screens.base.FullScreenPortraitBaseActivity
import de.schwerin.stoppCoronaDE.screens.routing.startRouterActivity
import de.schwerin.stoppCoronaDE.screens.webView.startWebView
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.activity.startFragmentActivity
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.fragment.BaseFragment
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.fragment.argument
import de.schwerin.stoppCoronaDE.skeleton.core.utils.color
import de.schwerin.stoppCoronaDE.skeleton.core.utils.dip
import de.schwerin.stoppCoronaDE.skeleton.core.utils.observeOnMainThread
import de.schwerin.stoppCoronaDE.skeleton.core.utils.onViewReady
import de.schwerin.stoppCoronaDE.utils.darkTextInStatusBar
import de.schwerin.stoppCoronaDE.utils.view.CirclePagerIndicatorDecoration
import com.airbnb.epoxy.EpoxyVisibilityTracker
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_onboarding.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Screen that onboards the user into the app functionality.
 */
class OnboardingFragment : BaseFragment(R.layout.fragment_onboarding) {

    companion object {
        private const val ARGUMENT_SKIP_ROUTER = "skip_router"

        fun args(skipRouter: Boolean): Bundle {
            return bundleOf(
                ARGUMENT_SKIP_ROUTER to skipRouter
            )
        }
    }

    private val skipRouter: Boolean by argument(ARGUMENT_SKIP_ROUTER, false)

    private val viewModel: OnboardingViewModel by viewModel()

    private val controller: OnboardingController by lazy {
        OnboardingController(
            context = requireContext(),
            dataPrivacyAccepted = viewModel.dataPrivacyAccepted,
            onEnterLastPage = { pageNumber ->
                viewModel.currentPage = pageNumber
            },
            onDataPrivacyCheckBoxChanged = viewModel::setDataPrivacyChecked,
            onTermsAndConditionsClick = { startWebView(R.string.onboarding_headline_terms_of_use, "terms-of-use") },
            onDataPrivacyClick = { startWebView(R.string.onboarding_headline_data_privacy, "privacy") }
        )
    }

    private val pagerIndicator: CirclePagerIndicatorDecoration by lazy {
        CirclePagerIndicatorDecoration(
            context = requireContext(),
            colorActive = requireContext().color(R.color.onboarding_indicator_active),
            colorInactive = requireContext().color(R.color.onboarding_indicator_inactive),
            paddingBottom = requireContext().dip(6),
            indicatorItemPadding = requireContext().dip(8),
            indicatorItemDiameter = requireContext().dip(8)
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.darkTextInStatusBar()

        with(contentRecyclerView) {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            setController(controller)
            addItemDecoration(pagerIndicator)
            EpoxyVisibilityTracker().attach(this)
        }
        controller.requestModelBuild()

        btnNext.setOnClickListener {
            if (viewModel.isLastPage(viewModel.currentPage)) {
                if (viewModel.dataPrivacyChecked || viewModel.dataPrivacyAccepted) {
                    viewModel.onboardingFinished()
                    if (skipRouter.not()) {
                        activity?.startRouterActivity(skipSplashscreenDelay = true)
                    }
                    activity?.finish()
                }
            } else {
                onViewReady {
                    contentRecyclerView.smoothScrollToPosition(viewModel.getNextPage())
                }
            }
        }

        disposables += viewModel.observeButtonEnabledState()
            .observeOnMainThread()
            .subscribe { buttonEnabled ->
                btnNext.isEnabled = buttonEnabled
            }

        disposables += viewModel.observeDataPrivacyPageShown()
            .observeOnMainThread()
            .subscribe { dataPrivacyPageShown ->
                when (dataPrivacyPageShown) {
                    true -> {
                        with(contentRecyclerView) {
                            removeItemDecoration(pagerIndicator)
                            setHeight(ConstraintLayout.LayoutParams.MATCH_PARENT)
                        }
                    }
                    false -> {
                        with(contentRecyclerView) {
                            if (itemDecorationCount == 1) {
                                addItemDecoration(pagerIndicator)
                                setHeight(requireContext().dip(512))
                            }
                        }
                    }
                }
            }

        disposables += viewModel.observeLastPage()
            .observeOnMainThread()
            .subscribe { isLastPage ->
                btnNext.text = if (isLastPage) {
                    getString(R.string.onboarding_finish_button)
                } else {
                    getString(R.string.onboarding_next_button)
                }
            }
    }

    override fun overrideOnBackPressed(): Boolean {
        return if (viewModel.isFirstPage()) {
            super.overrideOnBackPressed()
        } else {
            onViewReady {
                contentRecyclerView.smoothScrollToPosition(viewModel.getPreviousPage())
            }
            true
        }
    }
}

fun Activity.startOnboardingFragment(skipRouter: Boolean = false, options: Bundle? = null) {
    startFragmentActivity<FullScreenPortraitBaseActivity>(
        fragmentName = OnboardingFragment::class.java.name,
        fragmentArgs = OnboardingFragment.args(skipRouter),
        options = options
    )
}

private fun RecyclerView.setHeight(height: Int) {
    val params = layoutParams as ConstraintLayout.LayoutParams
    params.matchConstraintMaxHeight = height
    layoutParams = params
}

