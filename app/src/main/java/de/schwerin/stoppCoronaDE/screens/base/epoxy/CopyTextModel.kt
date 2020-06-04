package de.schwerin.stoppCoronaDE.screens.base.epoxy

import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.widget.TextView
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyHolder
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass

/**
 * Model with text in copy text style
 */
@EpoxyModelClass(layout = R.layout.dashboard_copy_text_epoxy_model)
abstract class CopyTextModel : BaseEpoxyModel<CopyTextModel.Holder>() {

    @EpoxyAttribute
    var text: SpannableString? = null

    override fun Holder.onBind() {
        txtText.text = text
        txtText.movementMethod = LinkMovementMethod()
    }

    class Holder : BaseEpoxyHolder() {
        val txtText by bind<TextView>(R.id.txtText)
    }
}