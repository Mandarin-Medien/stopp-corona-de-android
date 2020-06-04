package de.schwerin.stoppCoronaDE.screens.handshake.epoxy

import android.widget.TextView
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyHolder
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass

/**
 * Model for the random identification number
 */
@EpoxyModelClass(layout = R.layout.handshake_identification_epoxy_model)
abstract class HandshakeIdentificationModel : BaseEpoxyModel<HandshakeIdentificationModel.Holder>() {

    @EpoxyAttribute
    var identification: String? = null

    override fun Holder.onBind() {
        txtIdentification.text = identification
    }

    class Holder : BaseEpoxyHolder() {
        val txtIdentification by bind<TextView>(R.id.txtIdentification)
    }
}
