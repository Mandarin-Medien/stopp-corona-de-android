package de.schwerin.stoppCoronaDE.screens.infection_info

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.screens.base.CoronaPortraitBaseActivity
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.activity.getFragmentActivityIntent
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.activity.startFragmentActivity
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.fragment.BaseFragment
import de.schwerin.stoppCoronaDE.skeleton.core.utils.dipif
import de.schwerin.stoppCoronaDE.skeleton.core.utils.observeOnMainThread
import de.schwerin.stoppCoronaDE.utils.startCallWithPhoneNumber
import de.schwerin.stoppCoronaDE.utils.view.AccurateScrollListener
import de.schwerin.stoppCoronaDE.utils.view.LinearLayoutManagerAccurateOffset
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_dashboard.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Info about infected contacts and added guide.
 */
class InfectionInfoFragment : BaseFragment(R.layout.infection_info_fragment) {

    override val isToolbarVisible: Boolean = true

    private val viewModel: InfectionInfoViewModel by viewModel()

    private val controller: InfectionInfoController by lazy {
        InfectionInfoController(
            context = requireContext(),
            onPhoneNumberClick = { phoneNumber ->
                requireContext().startCallWithPhoneNumber(phoneNumber)
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
        toolbar?.setNavigationIcon(R.drawable.ic_back)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(contentRecyclerView) {
            setController(controller)
            layoutManager = LinearLayoutManagerAccurateOffset(requireContext(), accurateScrollListener)
            addOnScrollListener(accurateScrollListener)
        }

        disposables += viewModel.observeInfectedContacts()
            .observeOnMainThread()
            .subscribe { infectedContactsViewState ->
                if (infectedContactsViewState.redMessages.isNotEmpty()) {
                    setTitle(R.string.infection_info_title)
                } else {
                    setTitle(R.string.infection_info_warning_title)
                }

                controller.setData(infectedContactsViewState)
            }
    }

    override fun onDestroyView() {
        contentRecyclerView.removeOnScrollListener(accurateScrollListener)
        super.onDestroyView()
    }
}

fun Fragment.startInfectionInfoFragment() {
    startFragmentActivity<CoronaPortraitBaseActivity>(
        fragmentName = InfectionInfoFragment::class.java.name
    )
}

fun Context.getInfectionInfoFragmentIntent(): Intent {
    return getFragmentActivityIntent<CoronaPortraitBaseActivity>(
        this,
        fragmentName = InfectionInfoFragment::class.java.name
    )
}