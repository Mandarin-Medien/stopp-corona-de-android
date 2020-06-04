package de.schwerin.stoppCoronaDE.screens.history.epoxy

import android.widget.TextView
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyHolder
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyModel
import de.schwerin.stoppCoronaDE.utils.formatDayAndMonthAndYear
import de.schwerin.stoppCoronaDE.utils.formatHandshakeShortVersion
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import org.threeten.bp.ZonedDateTime

/**
 * A row of the contact events history table.
 */
@EpoxyModelClass(layout = R.layout.contact_event_table_item_epoxy_model)
abstract class ContactEventTableItemModel : BaseEpoxyModel<ContactEventTableItemModel.Holder>() {

    @EpoxyAttribute
    var timestamp: ZonedDateTime? = null

    @EpoxyAttribute
    var index: Int? = null


    @EpoxyAttribute
    var backgroundColor: Int = R.color.white

    override fun Holder.onBind() {
        val timestamp = timestamp
        if (timestamp != null) {
            txtDate.text = timestamp.formatDayAndMonthAndYear(context, monthAsText = false)
            txtTime.text = timestamp.formatHandshakeShortVersion(context)
        } else {
            txtDate.text = null
            txtTime.text = null
        }
    }

    class Holder : BaseEpoxyHolder() {
        val txtDate by bind<TextView>(R.id.txtDate)
        val txtTime by bind<TextView>(R.id.txtTime)
    }
}
