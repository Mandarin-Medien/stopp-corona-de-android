package de.schwerin.stoppCoronaDE.screens.infection_info.epoxy

import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import android.widget.TextView
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyHolder
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyModel
import de.schwerin.stoppCoronaDE.utils.string
import com.airbnb.epoxy.EpoxyModelClass

/**
 * Model with guide info and important phone numbers.
 */
@EpoxyModelClass(layout = R.layout.guide_info_epoxy_model)
abstract class GuideInfoModel(
    private val onPhoneClick: (phoneNumber: String) -> Unit
) : BaseEpoxyModel<GuideInfoModel.Holder>() {

    override fun Holder.onBind() {
        val description4team = string(R.string.sickness_certificate_guidelines_fourth_team)
        val description4 = SpannableString(string(R.string.sickness_certificate_guidelines_fourth, description4team))
        val teamStartingIndex = description4.indexOf(description4team)
        println(description4team);
        println(description4);
        println(teamStartingIndex);

        description4.setSpan(StyleSpan(Typeface.BOLD), teamStartingIndex, teamStartingIndex + description4team.length, 0)
        txtDescription4.text = description4

       // txtDescription4Phone.startPhoneCallOnClick()
        txtConsultingFirstPhone.startPhoneCallOnClick()
        txtConsultingSecondPhone.startPhoneCallOnClick()
        txtUrgentNumber1.startPhoneCallOnClick()
        txtUrgentNumber2.startPhoneCallOnClick()
    }

    private fun TextView.startPhoneCallOnClick() {
        setOnClickListener {
            onPhoneClick(text.toString())
        }
    }

    class Holder : BaseEpoxyHolder() {
        val txtDescription4 by bind<TextView>(R.id.txtDescription4)
   //     val txtDescription4Phone by bind<TextView>(R.id.txtDescription4Phone)
        val txtConsultingFirstPhone by bind<TextView>(R.id.txtConsultingFirstPhone)
        val txtConsultingSecondPhone by bind<TextView>(R.id.txtConsultingSecondPhone)
        val txtUrgentNumber1 by bind<TextView>(R.id.txtUrgentNumber1)
        val txtUrgentNumber2 by bind<TextView>(R.id.txtUrgentNumber2)
    }
}