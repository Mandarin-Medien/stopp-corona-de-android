package de.schwerin.stoppCoronaDE.screens.history

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.screens.base.epoxy.copyText
import de.schwerin.stoppCoronaDE.screens.base.epoxy.emptySpace
import de.schwerin.stoppCoronaDE.screens.base.epoxy.listItemText
import de.schwerin.stoppCoronaDE.screens.history.epoxy.contactEventTableItem
import de.schwerin.stoppCoronaDE.screens.history.epoxy.contactHistoryTableHeader
import de.schwerin.stoppCoronaDE.skeleton.core.utils.adapterProperty
import de.schwerin.stoppCoronaDE.utils.getBoldSpan
import com.airbnb.epoxy.EpoxyController

/**
 * Contains the UI components of the contact events history screen.
 */
class ContactHistoryController(
    private val context: Context
) : EpoxyController() {

    var listOfNearbyRecords: List<NearbyRecordWrapper>? by adapterProperty(null as List<NearbyRecordWrapper>?)

    override fun buildModels() {
        emptySpace(modelCountBuiltSoFar, 20)

        copyText {
            id("contact_history_description")
            val builder = SpannableStringBuilder()
            builder.append(context.getBoldSpan(R.string.contact_history_description_1, insertLeadingSpace = false))
            builder.append(context.getString(R.string.contact_history_description_2))
            text(SpannableString.valueOf(builder))
        }

        emptySpace(modelCountBuiltSoFar, 32)

        contactHistoryTableHeader {
            id("contact_history_header")
        }

        if (listOfNearbyRecords?.isNotEmpty() == true) {
            listOfNearbyRecords?.forEachIndexed { index, nearbyRecord ->
                contactEventTableItem {
                    id("contact_history_table_item_$index")
                    timestamp(nearbyRecord.record.timestamp)
                    backgroundColor(
                        when (index % 2) {
                            0 -> R.color.white
                            else -> R.color.whiteGray
                        }
                    )
                }
            }
        } else {
            emptySpace(modelCountBuiltSoFar, 16)

            listItemText {
                id("description_no_records")
                text(R.string.contact_history_no_records)
            }
        }
    }
}