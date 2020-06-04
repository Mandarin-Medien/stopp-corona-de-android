package de.schwerin.stoppCoronaDE.screens.base.epoxy

import android.view.Gravity
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyHolder
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyModel
import de.schwerin.stoppCoronaDE.skeleton.core.utils.dipfi
import de.schwerin.stoppCoronaDE.utils.color
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass

/**
 * Model for text formatted as Headline 1.
 */
@EpoxyModelClass(layout = R.layout.base_headline1_epoxy_model)
abstract class HeadlineH1Model : BaseEpoxyModel<HeadlineH1Model.Holder>() {

    @EpoxyAttribute
    var text: String? = null

    @ColorRes
    @EpoxyAttribute
    var textColor: Int = R.color.darkblue

    @EpoxyAttribute
    var marginHorizontal: Float = 24f // in dp

    @EpoxyAttribute
    var textSize: Float = 32f // in sp

    @EpoxyAttribute
    var gravity: Int = Gravity.CENTER_HORIZONTAL

    override fun Holder.onBind() {
        txtText.text = text
        txtText.updateLayoutParams<ConstraintLayout.LayoutParams> {
            this.marginStart = context.dipfi(marginHorizontal)
            this.marginEnd = context.dipfi(marginHorizontal)
        }
        txtText.textSize = textSize
        txtText.gravity = gravity
        txtText.setTextColor(color(textColor))
    }

    class Holder : BaseEpoxyHolder() {
        val txtText by bind<TextView>(R.id.txtText)
    }
}