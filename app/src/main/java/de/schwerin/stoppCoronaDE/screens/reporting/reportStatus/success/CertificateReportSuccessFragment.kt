package de.schwerin.stoppCoronaDE.screens.reporting.reportStatus.success

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.screens.base.CoronaPortraitBaseActivity
import de.schwerin.stoppCoronaDE.screens.dashboard.goBackToDashboardActivity
import de.schwerin.stoppCoronaDE.screens.reporting.reportStatus.guideline.startCertificateReportGuidelinesFragment
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.activity.startFragmentActivity
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.fragment.BaseFragment
import kotlinx.android.synthetic.main.certificate_report_success_fragment.*

/**
 * Screen displaying success of certificate reporting.
 */
class CertificateReportSuccessFragment : BaseFragment(R.layout.certificate_report_success_fragment) {

    override val isToolbarVisible: Boolean = true

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnQuarantineGuideline.setOnClickListener {
            startCertificateReportGuidelinesFragment()
        }

        btnBackToDashboard.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    override fun overrideOnBackPressed(): Boolean {
        goBackToDashboardActivity()
        return true
    }

    override fun getTitle(): String? {
        return getString(R.string.sickness_certificate_confirmation_title)
    }
}

fun Fragment.startCertificateReportSuccessFragment() {
    startFragmentActivity<CoronaPortraitBaseActivity>(
        fragmentName = CertificateReportSuccessFragment::class.java.name
    )
}
