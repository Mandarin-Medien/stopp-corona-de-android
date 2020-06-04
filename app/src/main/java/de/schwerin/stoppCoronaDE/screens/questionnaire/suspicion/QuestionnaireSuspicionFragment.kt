package de.schwerin.stoppCoronaDE.screens.questionnaire.suspicion

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.screens.base.CoronaPortraitBaseActivity
import de.schwerin.stoppCoronaDE.screens.dashboard.goBackToDashboardActivity
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.activity.startFragmentActivity
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_questionnaire_hint.*

class QuestionnaireSuspicionFragment : BaseFragment(R.layout.fragment_questionnaire_suspicion) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnActionButton.setOnClickListener {
            goBackToDashboardActivity()
        }
    }
}

fun Activity.startQuestionnaireSuspicionFragment() {
    startFragmentActivity<CoronaPortraitBaseActivity>(
        fragmentName = QuestionnaireSuspicionFragment::class.java.name
    )
}

fun Fragment.startQuestionnaireSuspicionFragment() {
    activity?.startQuestionnaireSuspicionFragment()
}
