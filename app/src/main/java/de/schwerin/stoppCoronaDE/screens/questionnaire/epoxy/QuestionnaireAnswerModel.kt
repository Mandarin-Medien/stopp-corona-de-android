package de.schwerin.stoppCoronaDE.screens.questionnaire.epoxy

import android.widget.RadioButton
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.model.entities.configuration.Decision
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyHolder
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyModel
import de.schwerin.stoppCoronaDE.utils.view.safeMap
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass

/**
 * Model for a single answer in the questionnaire flow
 * Has to be placed inside of [QuestionnaireRadioGroupModel]
 */
@EpoxyModelClass(layout = R.layout.questionnaire_answer_epoxy_model)
abstract class QuestionnaireAnswerModel(
    private val onAnswerSelected: (decision: Decision) -> Unit
) : BaseEpoxyModel<QuestionnaireAnswerModel.Holder>() {

    /**
     * Since [radioAnswer]-RadioButton in the layout has a fixed id, we need to set a unique id
     * so that the behaviour of the parent RadioGroup is working properly
     */
    @EpoxyAttribute
    var hash: Int = 0

    @EpoxyAttribute
    var answer: String? = null

    @EpoxyAttribute
    var decision: Decision? = null

    @EpoxyAttribute
    var textSize: Float = 20f // in sp

    override fun Holder.onBind() {
        radioAnswer.id = hash
        radioAnswer.text = answer
        radioAnswer.textSize = textSize

        radioAnswer.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                onAnswerSelected(decision.safeMap("No decision available!", Decision.NEXT))
            }
        }
    }

    class Holder : BaseEpoxyHolder() {
        val radioAnswer by bind<RadioButton>(R.id.radioAnswer)
    }
}
