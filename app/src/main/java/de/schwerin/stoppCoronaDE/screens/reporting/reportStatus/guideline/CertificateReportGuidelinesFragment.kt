package de.schwerin.stoppCoronaDE.screens.reporting.reportStatus.guideline

import android.os.Bundle
import android.text.SpannableString
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.screens.base.CoronaPortraitBaseActivity
import de.schwerin.stoppCoronaDE.screens.reporting.personalData.ReportingPersonalDataFragment
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.activity.startFragmentActivity
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.fragment.BaseFragment
import de.schwerin.stoppCoronaDE.skeleton.core.utils.dip
import de.schwerin.stoppCoronaDE.skeleton.core.utils.dipif
import de.schwerin.stoppCoronaDE.skeleton.core.utils.observeOnMainThread
import de.schwerin.stoppCoronaDE.skeleton.core.utils.visible
import de.schwerin.stoppCoronaDE.utils.formatDayAndMonth
import de.schwerin.stoppCoronaDE.utils.startPhoneCallOnClick
import io.reactivex.rxkotlin.plusAssign
import kotlinx.android.synthetic.main.certificate_report_guidelines_fragment.*
import kotlinx.android.synthetic.main.guide_info_epoxy_model.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Screen displaying guideline of certificate reporting.
 */
class CertificateReportGuidelinesFragment : BaseFragment(R.layout.certificate_report_guidelines_fragment) {

    private val viewModel: CertificateReportGuidelinesViewModel by viewModel()

    override val isToolbarVisible: Boolean = true

    override fun onInitActionBar(actionBar: ActionBar?, toolbar: Toolbar?) {
        super.onInitActionBar(actionBar, toolbar)
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scrollViewContainer.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            transparentAppBar.elevation = if (scrollY > requireContext().dip(ReportingPersonalDataFragment.SCROLLED_DISTANCE_THRESHOLD)) {
                requireContext().dipif(4)
            } else {
                0f
            }
        }

        txtTopDescription.visible = false
        disposables += viewModel.observeDateOfFirstMedicalConfirmation()
            .observeOnMainThread()
            .subscribe { dateOfFirstMedicalConfirmation ->
                if (dateOfFirstMedicalConfirmation.isPresent) {
                    val date = dateOfFirstMedicalConfirmation.get().toLocalDate().formatDayAndMonth(requireContext())
                    txtTopDescription.visible = true
                    txtTopDescription.text = getString(R.string.sickness_certificate_guidelines_top_description, date)
                }
            }

        val description4team = getString(R.string.sickness_certificate_guidelines_fourth_team)
        val description4 = SpannableString(getString(R.string.sickness_certificate_guidelines_fourth, description4team))
        txtDescription4.text = description4

        txtConsultingFirstPhone.startPhoneCallOnClick()
        txtConsultingSecondPhone.startPhoneCallOnClick()
        txtUrgentNumber1.startPhoneCallOnClick()
        txtUrgentNumber2.startPhoneCallOnClick()
    }

    override fun getTitle(): String? {
        return getString(R.string.sickness_certificate_guidelines_title)
    }
}

fun Fragment.startCertificateReportGuidelinesFragment() {
    startFragmentActivity<CoronaPortraitBaseActivity>(
        fragmentName = CertificateReportGuidelinesFragment::class.java.name
    )
}

