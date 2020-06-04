package de.schwerin.stoppCoronaDE.screens.dashboard.epoxy

import android.widget.TextView
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyHolder
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass

@EpoxyModelClass(layout = R.layout.handshake_headline_with_history_epoxy_model)
abstract class HandshakeHeadlineWithHistory(
    private val onEncountersClick: () -> Unit
) : BaseEpoxyModel<HandshakeHeadlineWithHistory.Holder>() {

    @EpoxyAttribute
    var title: String? = null

    @EpoxyAttribute
    var savedEncounters: Int = 0

    override fun Holder.onBind() {
        txtTitle.text = title
        txtSavedEncounters.text = savedEncounters.toString()

        txtSavedEncounters.setOnClickListener {
            onEncountersClick()
        }
    }

    class Holder : BaseEpoxyHolder() {
        val txtTitle by bind<TextView>(R.id.txtTitle)
        val txtSavedEncounters by bind<TextView>(R.id.txtSavedEncounters)
    }
}
