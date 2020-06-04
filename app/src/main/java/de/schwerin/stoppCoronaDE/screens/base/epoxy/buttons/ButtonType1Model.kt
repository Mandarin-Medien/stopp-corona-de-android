package de.schwerin.stoppCoronaDE.screens.base.epoxy.buttons

import android.widget.Button
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyHolder
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass

/**
 * Button type 1.
 */
@EpoxyModelClass(layout = R.layout.base_button_type1_epoxy_model)
abstract class ButtonType1Model(
    private val onClick: () -> Unit
) : BaseEpoxyModel<ButtonType1Model.Holder>() {

    @EpoxyAttribute
    var text: String? = null

    @EpoxyAttribute
    var enabled: Boolean = true

    override fun Holder.onBind() {
        btnType1.text = text
        btnType1.isEnabled = enabled
        btnType1.setOnClickListener { onClick() }
    }

    override fun Holder.onUnbind() {
        btnType1.setOnClickListener(null)
    }

    class Holder : BaseEpoxyHolder() {
        val btnType1 by bind<Button>(R.id.btnType1)
    }
}