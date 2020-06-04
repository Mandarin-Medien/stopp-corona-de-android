package de.schwerin.stoppCoronaDE.screens.base.epoxy

import androidx.annotation.ColorRes
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.utils.backgroundColor
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModel
import com.airbnb.epoxy.EpoxyModelGroup
import com.airbnb.epoxy.ModelGroupHolder

/**
 * Group box with background color.
 */
open class VerticalBackgroundModelGroup(
    models: List<EpoxyModel<*>>
) : EpoxyModelGroup(R.layout.base_background_epoxy_group, models) {

    @EpoxyAttribute
    @ColorRes
    var backgroundColor: Int = R.color.background_gray

    override fun bind(holder: ModelGroupHolder) {
        super.bind(holder)
        holder.rootView.backgroundColor(backgroundColor)
    }
}