package de.schwerin.stoppCoronaDE.screens.history.epoxy

import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyHolder
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyModel
import com.airbnb.epoxy.EpoxyModelClass

/**
 * Header of the table with contact events.
 */
@EpoxyModelClass(layout = R.layout.contact_history_table_header_epoxy_model)
abstract class ContactHistoryTableHeaderModel :
    BaseEpoxyModel<ContactHistoryTableHeaderModel.Holder>() {

    override fun Holder.onBind() {
        // Do nothing.
    }

    class Holder : BaseEpoxyHolder() {
        // Bind nothing.
    }
}