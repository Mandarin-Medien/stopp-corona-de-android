package de.schwerin.stoppCoronaDE.screens.handshake.epoxy

import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyHolder
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyModel
import com.airbnb.epoxy.EpoxyModelClass

/**
 * UI component to display closable status update box.
 */
@EpoxyModelClass(layout = R.layout.handshake_share_app_epoxy_model)
abstract class HandshakeShareAppModel(
    private val onShareClick: () -> Unit
) : BaseEpoxyModel<HandshakeShareAppModel.Holder>() {

    override fun Holder.onBind() {
        view.setOnClickListener { onShareClick() }
    }

    override fun Holder.onUnbind() {
        view.setOnClickListener(null)
    }

    class Holder : BaseEpoxyHolder()
}
