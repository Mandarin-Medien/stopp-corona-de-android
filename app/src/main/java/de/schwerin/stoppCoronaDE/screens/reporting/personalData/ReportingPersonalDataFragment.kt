package de.schwerin.stoppCoronaDE.screens.reporting.personalData

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.model.entities.infection.message.MessageType
import de.schwerin.stoppCoronaDE.model.exceptions.handleBaseCoronaErrors
import de.schwerin.stoppCoronaDE.model.repositories.ReportingRepository
import de.schwerin.stoppCoronaDE.screens.base.dialog.GeneralErrorDialog
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.State
import de.schwerin.stoppCoronaDE.skeleton.core.model.scope.connectToScope
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.fragment.BaseFragment
import de.schwerin.stoppCoronaDE.skeleton.core.utils.dip
import de.schwerin.stoppCoronaDE.skeleton.core.utils.dipif
import de.schwerin.stoppCoronaDE.skeleton.core.utils.observeOnMainThread
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.fragment_reporting_personal_data.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Screen for entering personal data, part of the flow for reporting a medical certificate or
 * the result of a self-testing to the authorities.
 */
class ReportingPersonalDataFragment : BaseFragment(R.layout.fragment_reporting_personal_data) {

    companion object {
        const val CURRENT_SCREEN = 1
        const val TOTAL_NUMBER_OF_SCREENS = 2
        const val SCROLLED_DISTANCE_THRESHOLD = 2 // dp
    }

    private val viewModel: ReportingPersonalDataViewModel by viewModel()

    override val isToolbarVisible: Boolean = true

    override fun getTitle(): String? {
        return "" // blank, is depending on messageType
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        connectToScope(ReportingRepository.SCOPE_NAME)
        super.onCreate(savedInstanceState)
    }

    override fun onInitActionBar(actionBar: ActionBar?, toolbar: Toolbar?) {
        super.onInitActionBar(actionBar, toolbar)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar?.setNavigationIcon(R.drawable.ic_back)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtProgress.text = getString(R.string.certificate_personal_progress_label, CURRENT_SCREEN, TOTAL_NUMBER_OF_SCREENS)

        disposables += viewModel.observeMessageType()
            .observeOnMainThread()
            .subscribe { messageType ->
                when (messageType) {
                    is MessageType.Revoke.Sickness -> {
                        setTitle(R.string.revoke_sickness_title)
                        txtTitle.text = getString(R.string.revoke_sickness_headline)
                        txtDescription.text = getString(R.string.revoke_sickness_personal_data_description)
                    }
                    else -> {
                        setTitle(R.string.certificate_personal_data_title)
                        txtTitle.text = getString(R.string.certificate_personal_data_title)
                        txtDescription.text = getString(R.string.certificate_personal_data_description)
                    }
                }
            }

        disposables += viewModel.observeTanRequest()
            .observeOnMainThread()
            .subscribe { validationResult ->
                if (validationResult.success != null && validationResult.success.not()) {
                    GeneralErrorDialog(R.string.certificate_personal_data_invaild_identifier,
                        R.string.certificate_personal_data_invaild_identifier_description).show()
                }
            }

        disposables += viewModel.observeTanRequestState()
            .observeOnMainThread()
            .subscribe { state ->
                hideProgressDialog()
                when (state) {
                    is State.Loading -> {
                        showProgressDialog(R.string.general_loading)
                    }
                    is State.Error -> {
                        handleBaseCoronaErrors(state.error)
                    }
                }
            }

        btnNext.setOnClickListener {
            viewModel.validate(requireContext())
        }

        scrollViewContainer.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            transparentAppBar.elevation = if (scrollY > requireContext().dip(SCROLLED_DISTANCE_THRESHOLD)) {
                requireContext().dipif(4)
            } else {
                0f
            }
        }

    }

    override fun overrideOnBackPressed(): Boolean {
        activity?.finish()
        return true // the changing of fragments is managing parent activity
    }

}

