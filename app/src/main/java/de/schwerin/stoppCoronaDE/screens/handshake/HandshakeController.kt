package de.schwerin.stoppCoronaDE.screens.handshake

import android.content.Context
import android.text.SpannableString
import android.view.Gravity
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.model.repositories.NearbyResult
import de.schwerin.stoppCoronaDE.screens.base.epoxy.additionalInformation
import de.schwerin.stoppCoronaDE.screens.base.epoxy.buttons.buttonType1
import de.schwerin.stoppCoronaDE.screens.base.epoxy.description
import de.schwerin.stoppCoronaDE.screens.base.epoxy.emptySpace
import de.schwerin.stoppCoronaDE.screens.base.epoxy.headlineH2
import de.schwerin.stoppCoronaDE.screens.handshake.epoxy.handshakeContactIdentification
import de.schwerin.stoppCoronaDE.screens.handshake.epoxy.handshakeIdentification
import de.schwerin.stoppCoronaDE.screens.handshake.epoxy.handshakeResultListHeadline
import de.schwerin.stoppCoronaDE.screens.handshake.epoxy.handshakeShareApp
import de.schwerin.stoppCoronaDE.skeleton.core.utils.adapterProperty
import de.schwerin.stoppCoronaDE.utils.string
import com.airbnb.epoxy.EpoxyController

/**
 * Describes the content of the handshake screen
 */
class HandshakeController(
    private val context: Context,
    private val onInfoClicked: () -> Unit,
    private val onSelectAllContacts: (selected: Boolean) -> Unit,
    private val onContactSelected: (contactSelected: Boolean, result: NearbyResult) -> Unit,
    private val onOpenSettingsClicked: () -> Unit,
    private val onShareAppClick: () -> Unit
) : EpoxyController() {

    var identification: String? by adapterProperty(null as String?)
    var contactList: List<NearbyResult> by adapterProperty(ArrayList())
    var selectAllChecked: Boolean by adapterProperty(false)
    var showLoadingIndicator: Boolean by adapterProperty(false)
    var permissionsGranted: Boolean by adapterProperty(true)

    override fun buildModels() {
        if (permissionsGranted) {
            emptySpace(modelCountBuiltSoFar, 24)

            headlineH2 {
                id("handshake_headline")
                title(context.string(R.string.handshake_headline))
            }

            additionalInformation(onInfoClicked) {
                id("handshake_information")
                title(context.getString(R.string.handshake_information))
                iconRes(R.drawable.ic_info)
            }

            emptySpace(modelCountBuiltSoFar, 16)

            handshakeShareApp(onShareAppClick) {
                id("handshake_share_app")
            }

            emptySpace(modelCountBuiltSoFar, 24)

            handshakeResultListHeadline {
                id("handshake_result_list_headline")
                title(context.getString(R.string.handshake_results_headline))
                showLoadingIndicator(showLoadingIndicator)
            }

            emptySpace(modelCountBuiltSoFar, 20)

            handshakeIdentification {
                id("handshake_identification")
                identification(identification)
            }

            emptySpace(modelCountBuiltSoFar, 20)

            if (contactList.isNotEmpty()) {
                handshakeContactIdentification({ selectAll -> onSelectAllContacts(selectAll) }) {
                    id("handshake_select_all_contacts")
                    contactIdentification(context.getString(R.string.handshake_select_all_contacts))
                    backgroundColor(R.color.whiteGray)
                    selected(selectAllChecked)
                }
            }

            contactList.forEachIndexed { index, result ->
                handshakeContactIdentification({ checked -> onContactSelected(checked, result) }) {
                    id("handshake_contact_$index")
                    contactIdentification(result.identification)
                    selected(result.selected)
                    contactSaved(result.saved)
                    backgroundColor(
                        when (index % 2) {
                            0 -> R.color.white
                            else -> R.color.whiteGray
                        }
                    )

                }
            }
        } else {
            emptySpace(modelCountBuiltSoFar, 180)

            headlineH2 {
                id("handshake_missing_permission_title")
                title(context.string(R.string.handshake_missing_permissions_title))
                gravity(Gravity.CENTER_HORIZONTAL)
            }

            emptySpace(modelCountBuiltSoFar, 8)

            description {
                id("handshake_missing_permission_message_1")
                description(SpannableString(context.string(R.string.handshake_missing_permissions_message)))
                gravity(Gravity.CENTER_HORIZONTAL)
            }

            emptySpace(modelCountBuiltSoFar, 32)

            headlineH2 {
                id("handshake_missing_permission_microphone")
                title(context.string(R.string.handshake_missing_permissions_microphone))
                gravity(Gravity.CENTER_HORIZONTAL)
            }

            emptySpace(modelCountBuiltSoFar, 32)

            description {
                id("handshake_missing_permission_message_2")
                description(SpannableString(context.string(R.string.handshake_missing_permissions_message_settings)))
                gravity(Gravity.CENTER_HORIZONTAL)
            }

            emptySpace(modelCountBuiltSoFar, 24)

            buttonType1(onOpenSettingsClicked) {
                id("handshake_missing_permission_open_settings")
                text(context.string(R.string.handshake_missing_permissions_open_settings))
            }
        }
    }
}
