package de.schwerin.stoppCoronaDE.screens.questionnaire

import android.content.Context
import android.view.Gravity
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.constants.Constants.Questionnaire.COUNTRY_CODE_CZ
import de.schwerin.stoppCoronaDE.constants.Constants.Questionnaire.COUNTRY_CODE_DE
import de.schwerin.stoppCoronaDE.constants.Constants.Questionnaire.COUNTRY_CODE_EN
import de.schwerin.stoppCoronaDE.constants.Constants.Questionnaire.COUNTRY_CODE_FR
import de.schwerin.stoppCoronaDE.constants.Constants.Questionnaire.COUNTRY_CODE_HU
import de.schwerin.stoppCoronaDE.constants.Constants.Questionnaire.COUNTRY_CODE_SK
import de.schwerin.stoppCoronaDE.model.entities.configuration.ApiConfiguration
import de.schwerin.stoppCoronaDE.model.entities.configuration.Decision
import de.schwerin.stoppCoronaDE.screens.base.epoxy.EmptySpaceModel_
import de.schwerin.stoppCoronaDE.screens.base.epoxy.HeadlineH1Model_
import de.schwerin.stoppCoronaDE.screens.questionnaire.epoxy.QuestionnaireAnswerModel_
import de.schwerin.stoppCoronaDE.screens.questionnaire.epoxy.QuestionnaireRadioGroupModel_
import de.schwerin.stoppCoronaDE.screens.questionnaire.epoxy.questionnairePage
import de.schwerin.stoppCoronaDE.skeleton.core.utils.adapterProperty
import de.schwerin.stoppCoronaDE.skeleton.core.utils.addTo
import de.schwerin.stoppCoronaDE.skeleton.core.utils.rawDimen
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyModel

class QuestionnaireController(
    private val context: Context,
    private val onEnterPage: (pageNumber: Int) -> Unit,
    private val onAnswerSelected: (pageNumber: Int, decision: Decision) -> Unit
) : EpoxyController() {

    var apiConfiguration: ApiConfiguration? by adapterProperty(null as ApiConfiguration?)

    override fun buildModels() {
        val questions = when (context.getString(R.string.current_language)) {
            COUNTRY_CODE_CZ -> {
                apiConfiguration?.diagnosticQuestionnaire?.cz
            }
            COUNTRY_CODE_DE -> {
                apiConfiguration?.diagnosticQuestionnaire?.de
            }
            COUNTRY_CODE_EN -> {
                apiConfiguration?.diagnosticQuestionnaire?.en
            }
            COUNTRY_CODE_FR -> {
                apiConfiguration?.diagnosticQuestionnaire?.fr
            }
            COUNTRY_CODE_HU -> {
                apiConfiguration?.diagnosticQuestionnaire?.hu
            }
            COUNTRY_CODE_SK -> {
                apiConfiguration?.diagnosticQuestionnaire?.sk
            }
            else -> apiConfiguration?.diagnosticQuestionnaire?.en
        }

        questions?.forEachIndexed { questionIndex, question ->
            val questionPageContent = mutableListOf<EpoxyModel<*>>()

            EmptySpaceModel_()
                .id(modelCountBuiltSoFar)
                .height(context.rawDimen(R.dimen.questionnaire_headline_top_margin).toInt())
                .addTo(questionPageContent)

            HeadlineH1Model_()
                .id("question_${questionIndex}_headline")
                .text(context.getString(R.string.questionnaire_headline_1, (questionIndex + 1)))
                .textColor(R.color.black)
                .textSize(context.rawDimen(R.dimen.questionnaire_headline))
                .marginHorizontal(0f)
                .addTo(questionPageContent)

            HeadlineH1Model_()
                .id("question_${questionIndex}")
                .text(question.questionText)
                .textSize(context.rawDimen(R.dimen.questionnaire_question_text_size))
                .gravity(Gravity.START)
                .marginHorizontal(0f)
                .addTo(questionPageContent)

            EmptySpaceModel_()
                .id(modelCountBuiltSoFar)
                .height(40)
                .addTo(questionPageContent)

            val answerList = mutableListOf<EpoxyModel<*>>()

            question.answers?.forEachIndexed { answerIndex, answer ->
                val id = "answer_${questionIndex}_${answerIndex}"
                QuestionnaireAnswerModel_ { decision -> onAnswerSelected(questionIndex, decision) }
                    .id(id)
                    .hash(id.hashCode())
                    .answer(answer.text)
                    .decision(answer.decision)
                    .textSize(context.rawDimen(R.dimen.questionnaire_answer))
                    .addTo(answerList)

                if (answerIndex < question.answers.size - 1) {
                    EmptySpaceModel_()
                        .id(modelCountBuiltSoFar)
                        .height(context.rawDimen(R.dimen.questionnaire_answer_space).toInt())
                        .addTo(answerList)
                }
            }

            QuestionnaireRadioGroupModel_(answerList)
                .id("question_answer_group_$questionIndex")
                .addTo(questionPageContent)

            EmptySpaceModel_()
                .id(modelCountBuiltSoFar)
                .height(32)
                .addTo(questionPageContent)

            questionnairePage(onEnterPage, questionPageContent) {
                id("question_group_$questionIndex")
                pageNumber(questionIndex)
            }
        }
    }
}
