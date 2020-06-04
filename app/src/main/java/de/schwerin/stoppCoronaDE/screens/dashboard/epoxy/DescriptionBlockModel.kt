package de.schwerin.stoppCoronaDE.screens.dashboard.epoxy

import android.widget.TextView
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyHolder
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass

/**
 * Model with one title and one description text below.
 */
@EpoxyModelClass(layout = R.layout.dashboard_description_block_epoxy_model)
abstract class DescriptionBlockModel : BaseEpoxyModel<DescriptionBlockModel.Holder>() {

    @EpoxyAttribute
    var title: String? = null

    @EpoxyAttribute
    var description: String? = null

    override fun Holder.onBind() {
        txtTitle.text = title
        txtDescription.text = description
    }

    class Holder : BaseEpoxyHolder() {
        val txtTitle by bind<TextView>(R.id.txtTitle)
        val txtDescription by bind<TextView>(R.id.txtDescription)
    }
}