package de.schwerin.stoppCoronaDE.screens.base.epoxy.buttons

import android.widget.Button
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyHolder
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass

/**
 * Button type 2.
 */
@EpoxyModelClass(layout = R.layout.base_button_type2_epoxy_model)
abstract class ButtonType2Model(
    private val onClick: () -> Unit
) : BaseEpoxyModel<ButtonType2Model.Holder>() {

    @EpoxyAttribute
    var text: String? = null

    override fun Holder.onBind() {
        btnType2.text = text
        btnType2.setOnClickListener { onClick() }
    }

    override fun Holder.onUnbind() {
        btnType2.setOnClickListener(null)
    }

    class Holder : BaseEpoxyHolder() {
        val btnType2 by bind<Button>(R.id.btnType2)
    }
}