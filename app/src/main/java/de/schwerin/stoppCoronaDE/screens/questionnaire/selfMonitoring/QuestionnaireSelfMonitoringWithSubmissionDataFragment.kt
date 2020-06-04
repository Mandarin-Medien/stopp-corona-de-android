package de.schwerin.stoppCoronaDE.screens.questionnaire.selfmonitoring

import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.screens.base.CoronaPortraitBaseActivity
import de.schwerin.stoppCoronaDE.screens.questionnaire.startQuestionnaireFragment
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.activity.startFragmentActivity
import de.schwerin.stoppCoronaDE.skeleton.core.utils.dip
import de.schwerin.stoppCoronaDE.skeleton.core.utils.visible
import de.schwerin.stoppCoronaDE.utils.backgroundColor
import de.schwerin.stoppCoronaDE.utils.formatDayAndMonthAndYearAndTime
import de.schwerin.stoppCoronaDE.utils.tint
import kotlinx.android.synthetic.main.fragment_questionnaire_hint.btnActionButton
import kotlinx.android.synthetic.main.fragment_questionnaire_hint.txtHeadline1
import kotlinx.android.synthetic.main.fragment_questionnaire_self_monitoring.*
import org.threeten.bp.ZonedDateTime

/**
 * Screen to display the information about symptoms monitoring, it's opened
 * from the health status card in dashboard.
 */
class QuestionnaireSelfMonitoringWithSubmissionDataFragment : QuestionnaireSelfMonitoringFragment(){
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txtHeadline1.visible = false
        txtStepsHeadline.visible = true
        txtFormFilledDate.visible = true

        val formedFilledDate = ZonedDateTime.now()

        txtHeadline2.text = getString(R.string.questionnaire_examine_observe_sub_headline_1)
        txtDescription1.text = getString(R.string.questionnaire_examine_observe_recommendation_1)
        txtDescription2.text = getString(R.string.questionnaire_examine_observe_recommendation_2)
        txtDescription3.text = getString(R.string.questionnaire_examine_observe_recommendation_3)
        txtDescription.text = getString(R.string.questionnaire_examine_observe_headline_2)
        txtSubDescription.text = getString(R.string.questionnaire_examine_observe_description)
        txtStepsHeadline.text = getString(R.string.questionnaire_observe_symptoms_next_steps)
        btnActionButton.text = getString(R.string.questionnaire_observe_symptoms_button)
        txtFormFilledDate.text =
            getString(R.string.questionnaire_observe_symptoms_form_filled_date, formedFilledDate.formatDayAndMonthAndYearAndTime(requireContext()))
        stepsContainer.backgroundColor(R.color.questionnaire_self_monitoring_container)
        imgCircle1.tint(R.color.questionnaire_self_monitoring_circle_tint)
        imgCircle2.tint(R.color.questionnaire_self_monitoring_circle_tint)
        imgCircle3.tint(R.color.questionnaire_self_monitoring_circle_tint)
        stepsContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
            topMargin = requireContext().dip(32)
        }
        txtDescription.updateLayoutParams<ConstraintLayout.LayoutParams> {
            topMargin = requireContext().dip(32)
        }

        btnActionButton.setOnClickListener {
            startQuestionnaireFragment()
            activity?.finish()
        }
    }

    override fun getTitle(): String? {
        return getString(R.string.questionnaire_examine_observe_title)
    }

}

fun Fragment.startQuestionnaireSelfMonitoringWithSubmissionDataFragment() {
    startFragmentActivity<CoronaPortraitBaseActivity>(
        fragmentName = QuestionnaireSelfMonitoringWithSubmissionDataFragment::class.java.name
    )
}