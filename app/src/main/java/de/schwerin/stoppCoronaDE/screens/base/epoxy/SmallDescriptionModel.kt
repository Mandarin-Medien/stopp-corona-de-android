package de.schwerin.stoppCoronaDE.screens.base.epoxy

import android.widget.TextView
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyHolder
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass

/**
 * Model for a description block.
 */
@EpoxyModelClass(layout = R.layout.description_small_epoxy_model)
abstract class SmallDescriptionModel : BaseEpoxyModel<SmallDescriptionModel.Holder>() {

    @EpoxyAttribute
    var description: String? = null

    override fun Holder.onBind() {
        txtDescription.text = description
    }

    class Holder : BaseEpoxyHolder() {
        val txtDescription by bind<TextView>(R.id.txtDescription)
    }
}
