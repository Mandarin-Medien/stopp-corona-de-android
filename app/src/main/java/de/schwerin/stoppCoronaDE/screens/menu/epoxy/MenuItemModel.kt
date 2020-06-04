package de.schwerin.stoppCoronaDE.screens.menu.epoxy

import android.widget.ImageView
import android.widget.TextView
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyHolder
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass

/**
 * Model as menu item with text and icon on the right.
 */
@EpoxyModelClass(layout = R.layout.menu_item_epoxy_model)
abstract class MenuItemModel(
    private val onClick: () -> Unit
) : BaseEpoxyModel<MenuItemModel.Holder>() {

    @EpoxyAttribute
    var title: String? = null

    @EpoxyAttribute
    var externalLink: Boolean = false

    override fun Holder.onBind() {
        txtTitle.text = title
        if (externalLink) {
            imgIcon.setImageResource(R.drawable.ic_external_link)
            imgIcon.rotation = 0f
        } else {
            imgIcon.setImageResource(R.drawable.ic_back)
            imgIcon.rotation = 180f
        }
        view.setOnClickListener {
            onClick()
        }
    }

    override fun Holder.onUnbind() {
        view.setOnClickListener(null)
    }

    class Holder : BaseEpoxyHolder() {
        val txtTitle by bind<TextView>(R.id.txtTitle)
        val imgIcon by bind<ImageView>(R.id.imgIcon)
    }
}
