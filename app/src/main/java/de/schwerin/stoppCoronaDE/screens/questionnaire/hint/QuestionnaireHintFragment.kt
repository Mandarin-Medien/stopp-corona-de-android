package de.schwerin.stoppCoronaDE.screens.questionnaire.hint

import android.app.Activity
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.screens.base.CoronaPortraitBaseActivity
import de.schwerin.stoppCoronaDE.screens.routing.startRouterActivity
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.activity.startFragmentActivity
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.fragment.BaseFragment
import kotlinx.android.synthetic.main.fragment_questionnaire_hint.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class QuestionnaireHintFragment : BaseFragment(R.layout.fragment_questionnaire_hint) {

    private val viewModel: QuestionnaireHintViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.revokeSelfMonitoring()

        btnActionButton.setOnClickListener {
            activity?.startRouterActivity()
        }
    }
}

fun Activity.startQuestionnaireHintFragment() {
    startFragmentActivity<CoronaPortraitBaseActivity>(
        fragmentName = QuestionnaireHintFragment::class.java.name
    )
}

fun Fragment.startQuestionnaireHintFragment() {
    activity?.startQuestionnaireHintFragment()
}
