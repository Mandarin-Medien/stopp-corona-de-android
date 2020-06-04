package de.schwerin.stoppCoronaDE.screens.history

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.screens.base.CoronaPortraitBaseActivity
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.activity.startFragmentActivity
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.fragment.BaseFragment
import de.schwerin.stoppCoronaDE.skeleton.core.utils.dipif
import de.schwerin.stoppCoronaDE.skeleton.core.utils.observeOnMainThread
import de.schwerin.stoppCoronaDE.utils.view.AccurateScrollListener
import de.schwerin.stoppCoronaDE.utils.view.LinearLayoutManagerAccurateOffset
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_contact_history.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Screen to display the history of contact events with date and time.
 */
class ContactHistoryFragment : BaseFragment(R.layout.fragment_contact_history) {

    private val viewModel: ContactHistoryViewModel by viewModel()

    override val isToolbarVisible: Boolean = true

    private val controller: ContactHistoryController by lazy {
        ContactHistoryController(
            requireContext()
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(contentRecyclerView) {
            setController(controller)
            layoutManager = LinearLayoutManagerAccurateOffset(requireContext(), accurateScrollListener)
            addOnScrollListener(accurateScrollListener)
        }

        disposables += viewModel.observeAllNearbyRecords()
            .observeOnMainThread()
            .subscribe {
                controller.listOfNearbyRecords = it
            }
    }

    override fun onInitActionBar(actionBar: ActionBar?, toolbar: Toolbar?) {
        super.onInitActionBar(actionBar, toolbar)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationIcon(R.drawable.ic_back)
    }

    override fun getTitle(): String? {
        return getString(R.string.contact_history_title)
    }

    override fun onDestroyView() {
        contentRecyclerView.removeOnScrollListener(accurateScrollListener)
        super.onDestroyView()
    }
}

fun Fragment.startContactHistoryFragment() {
    startFragmentActivity<CoronaPortraitBaseActivity>(
        fragmentName = ContactHistoryFragment::class.java.name
    )
}

