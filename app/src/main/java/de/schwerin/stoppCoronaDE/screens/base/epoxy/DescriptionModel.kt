package de.schwerin.stoppCoronaDE.screens.base.epoxy

import android.text.SpannableString
import android.view.Gravity
import android.widget.TextView
import androidx.annotation.ColorRes
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyHolder
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyModel
import de.schwerin.stoppCoronaDE.utils.color
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass

/**
 * Model for a description block.
 */
@EpoxyModelClass(layout = R.layout.description_epoxy_model)
abstract class DescriptionModel : BaseEpoxyModel<DescriptionModel.Holder>() {

    @EpoxyAttribute
    var description: SpannableString? = null

    @EpoxyAttribute
    var gravity: Int = Gravity.START

    @EpoxyAttribute
    @ColorRes
    var textColor: Int = R.color.text_default_copy

    override fun Holder.onBind() {
        txtDescription.text = description
        txtDescription.setTextColor(color(textColor))
        txtDescription.gravity = gravity
    }

    class Holder : BaseEpoxyHolder() {
        val txtDescription by bind<TextView>(R.id.txtDescription)
    }
}