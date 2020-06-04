package de.schwerin.stoppCoronaDE.screens.menu.epoxy

import android.widget.TextView
import de.schwerin.stoppCoronaDE.BuildConfig
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyHolder
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyModel
import de.schwerin.stoppCoronaDE.utils.string
import com.airbnb.epoxy.EpoxyModelClass

/**
 * Model as menu item with app version.
 */
@EpoxyModelClass(layout = R.layout.menu_item_version_epoxy_model)
abstract class MenuItemVersionModel(
    private val onClick: () -> Unit
) : BaseEpoxyModel<MenuItemVersionModel.Holder>() {

    override fun Holder.onBind() {
        txtVersion.text = string(R.string.start_menu_item_version, BuildConfig.VERSION_NAME)
        view.setOnClickListener {
            onClick()
        }
    }

    override fun Holder.onUnbind() {
        view.setOnClickListener(null)
    }

    class Holder : BaseEpoxyHolder() {
        val txtVersion by bind<TextView>(R.id.txtVersion)
    }
}