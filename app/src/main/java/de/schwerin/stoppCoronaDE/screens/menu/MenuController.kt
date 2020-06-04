package de.schwerin.stoppCoronaDE.screens.menu

import android.content.Context
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.screens.base.epoxy.emptySpace
import de.schwerin.stoppCoronaDE.screens.base.epoxy.headlineH2
import de.schwerin.stoppCoronaDE.screens.base.epoxy.verticalBackgroundModelGroup
import de.schwerin.stoppCoronaDE.screens.dashboard.HealthStatusData
import de.schwerin.stoppCoronaDE.screens.menu.epoxy.MenuItemModel_
import de.schwerin.stoppCoronaDE.screens.menu.epoxy.menuItem
import de.schwerin.stoppCoronaDE.screens.menu.epoxy.menuItemVersion
import de.schwerin.stoppCoronaDE.skeleton.core.utils.adapterProperty
import de.schwerin.stoppCoronaDE.skeleton.core.utils.addTo
import de.schwerin.stoppCoronaDE.utils.string
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyModel

/**
 * Menu content.
 */
class MenuController(
    private val context: Context,
    private val onOnboardingClick: () -> Unit,
    private val onExternalLinkClick: (url: String) -> Unit,
    private val onOpenSourceLicenceClick: () -> Unit,
    private val onPrivacyDataClick: () -> Unit,
    private val onImprintClick: () -> Unit,
    private val onVersionClick: () -> Unit,
    private val onHandshakeClick: () -> Unit,
    private val onCheckSymptomsClick: () -> Unit,
    private val onReportOfficialSicknessClick: () -> Unit,
    private val onShareAppClick: () -> Unit,
    private val onRevokeSicknessClick: () -> Unit
) : EpoxyController() {

    var ownHealthStatus: HealthStatusData by adapterProperty(HealthStatusData.NoHealthStatus)

    override fun buildModels() {

        emptySpace(modelCountBuiltSoFar, 22)

        headlineH2 {
            id("headline_functionality")
            title(context.string(R.string.start_menu_headline_3))
        }

        with(buildFunctionalityMenuItems()) {
            verticalBackgroundModelGroup(this) {
                id("vertical_model_group_functionality")
                backgroundColor(R.color.white)
            }
        }

        menuItem(onShareAppClick) {
            id("share_app")
            title(context.string(R.string.share_app_menu))
            externalLink(true)
        }

        emptySpace(modelCountBuiltSoFar, 48)

        headlineH2 {
            id("headline_info")
            title(context.string(R.string.start_menu_headline_1))
        }

        menuItem(onOnboardingClick) {
            id("onboarding")
            title(context.string(R.string.start_menu_item_1_1_what_does_the_app_do))
        }

        menuItem({ onExternalLinkClick(context.string(R.string.start_menu_item_1_2_germany_link_link_target)) }) {
            id("faq_link")
            title(context.string(R.string.start_menu_item_1_2_germany_link))
            externalLink(true)
        }

        menuItem({ onExternalLinkClick(context.string(R.string.start_menu_item_1_3_germany_link_link_target)) }) {
            id("red_cross_link")
            title(context.string(R.string.start_menu_item_1_3_germany_link))
            externalLink(true)
        }

        emptySpace(modelCountBuiltSoFar, 48)

        headlineH2 {
            id("headline_legal")
            title(context.string(R.string.start_menu_headline_2))
        }

        menuItem(onOpenSourceLicenceClick) {
            id("open_source")
            title(context.string(R.string.start_menu_item_2_1_open_source_licenses))
        }

        menuItem(onPrivacyDataClick) {
            id("privacy_data")
            title(context.string(R.string.start_menu_item_2_2_data_privacy))
        }

        menuItem(onImprintClick) {
            id("imprint")
            title(context.string(R.string.start_menu_item_2_3_imprint))
        }

        menuItemVersion(onVersionClick) {
            id("version")
        }

        emptySpace(modelCountBuiltSoFar, 40)
    }

    private fun buildFunctionalityMenuItems(): List<EpoxyModel<out Any>> {
        val modelList = arrayListOf<EpoxyModel<out Any>>()

        if ((ownHealthStatus is HealthStatusData.SicknessCertificate).not()) {
            MenuItemModel_(onHandshakeClick)
                .id("start_handshake")
                .title(context.string(R.string.start_menu_item_3_1))
                .addTo(modelList)
        }

        if ((ownHealthStatus is HealthStatusData.SelfTestingSuspicionOfSickness).not()
            && (ownHealthStatus is HealthStatusData.SicknessCertificate).not()
        ) {
            MenuItemModel_(onCheckSymptomsClick)
                .id("check_symptoms")
                .title(context.string(R.string.start_menu_item_3_2))
                .addTo(modelList)
        }

        if (ownHealthStatus is HealthStatusData.SicknessCertificate) {
            MenuItemModel_(onRevokeSicknessClick)
                .id("revoke_sickness")
                .title(context.string(R.string.start_menu_item_revoke_sickness))
                .addTo(modelList)
        } else {
            MenuItemModel_(onReportOfficialSicknessClick)
                .id("official_sickness")
                .title(context.string(R.string.start_menu_item_3_3))
                .addTo(modelList)
        }

        return modelList
    }
}