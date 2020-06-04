package de.schwerin.stoppCoronaDE.screens.reporting

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import de.schwerin.stoppCoronaDE.model.entities.infection.message.MessageType
import de.schwerin.stoppCoronaDE.model.exceptions.SilentError
import de.schwerin.stoppCoronaDE.model.repositories.ReportingRepository
import de.schwerin.stoppCoronaDE.model.repositories.ReportingState
import de.schwerin.stoppCoronaDE.screens.base.CoronaPortraitBaseActivity
import de.schwerin.stoppCoronaDE.screens.reporting.personalData.ReportingPersonalDataFragment
import de.schwerin.stoppCoronaDE.screens.reporting.reportStatus.ReportingStatusFragment
import de.schwerin.stoppCoronaDE.skeleton.core.model.scope.connectToScope
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.activity.argument
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.activity.startFragmentActivity
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.plusAssign
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import timber.log.Timber

/**
 * Certificate reporting container activity to hold and switch the content by the reporting state.
 */
class ReportingActivity : CoronaPortraitBaseActivity() {

    companion object {
        private const val ARGUMENT_MESSAGE_TYPE = "argument_message_type"

        fun args(messageType: MessageType): Bundle {
            return bundleOf(
                ARGUMENT_MESSAGE_TYPE to messageType
            )
        }
    }

    private val messageType: MessageType by argument(ARGUMENT_MESSAGE_TYPE)

    private val viewModel: ReportingViewModel by viewModel { parametersOf(messageType) }

    /**
     * Contains disposables that have to be disposed in [onStop].
     */
    private var disposablesWhileViewIsVisible: Disposable? = null

    override val navigateUpOnBackPress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        /**
         * When savedInstanceState is set, system probably kicked of the activity from memory, which
         * causes reset of ReportRepository and this activity has been restored.
         * To not confuse user by reset of flow progress, we just destroy it completely
         * and return him to the previous screen.
         * Reporting process must be started again as an user explicit action then.
         */
        if (savedInstanceState != null) {
            Timber.e(SilentError("Certificate reporting process is destroyed due to lost state in ReportingActivity."))
            finish()
        }

        connectToScope(ReportingRepository.SCOPE_NAME)
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        disposablesWhileViewIsVisible = viewModel.observeReportingState()
            .subscribe { reportSendingState ->
                when (reportSendingState) {
                    is ReportingState.PersonalDataEntry -> {
                        if (currentFragment !is ReportingPersonalDataFragment) {
                            replaceFragment(ReportingPersonalDataFragment(), addToBackStack = false)
                        }
                    }
                    is ReportingState.ReportingAgreement -> {
                        if (currentFragment !is ReportingStatusFragment) {
                            replaceFragment(ReportingStatusFragment(), addToBackStack = false)
                        }
                    }
                }
            }.also { disposables += it }
    }

    override fun onStop() {
        disposablesWhileViewIsVisible?.dispose()
        disposablesWhileViewIsVisible = null
        super.onStop()
    }
}

fun Fragment.startReportingActivity(messageType: MessageType) {
    startFragmentActivity<ReportingActivity>(
        fragmentName = ReportingPersonalDataFragment::class.java.name,
        activityBundle = ReportingActivity.args(messageType)
    )
}
