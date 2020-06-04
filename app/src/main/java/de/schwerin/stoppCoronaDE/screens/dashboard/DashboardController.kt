package de.schwerin.stoppCoronaDE.screens.dashboard

import android.content.Context
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.screens.base.epoxy.*
import de.schwerin.stoppCoronaDE.screens.base.epoxy.buttons.ButtonType2Model_
import de.schwerin.stoppCoronaDE.screens.base.epoxy.buttons.buttonType1
import de.schwerin.stoppCoronaDE.screens.dashboard.epoxy.*
import de.schwerin.stoppCoronaDE.skeleton.core.utils.adapterProperty
import de.schwerin.stoppCoronaDE.skeleton.core.utils.addTo
import de.schwerin.stoppCoronaDE.utils.string
import com.airbnb.epoxy.EpoxyController
import com.airbnb.epoxy.EpoxyModel

/**
 * Contents of the dashboard.
 */
class DashboardController(
    private val context: Context,
    private val onManualHandshakeClick: () -> Unit,
    private val onSavedEncountersClick: () -> Unit,
    private val onFeelingClick: () -> Unit,
    private val onReportClick: () -> Unit,
    private val onHealthStatusClick: (data: HealthStatusData) -> Unit,
    private val onRevokeSuspicionClick: () -> Unit,
    private val onPresentMedicalReportClick: () -> Unit,
    private val onCheckSymptomsAgainClick: () -> Unit,
    private val onSomeoneHasRecoveredCloseClick: () -> Unit,
    private val onQuarantineEndCloseClick: () -> Unit,
    private val onRevokeSicknessClick: () -> Unit,
    private val onShareAppClick: () -> Unit
) : EpoxyController() {

    var savedEncounters: Int by adapterProperty(0)

    var ownHealthStatus: HealthStatusData by adapterProperty(HealthStatusData.NoHealthStatus)
    var contactsHealthStatus: HealthStatusData by adapterProperty(HealthStatusData.NoHealthStatus)
    var showQuarantineEnd: Boolean by adapterProperty(false)
    var someoneHasRecoveredHealthStatus: HealthStatusData by adapterProperty(HealthStatusData.NoHealthStatus)

    override fun buildModels() {
        emptySpace(modelCountBuiltSoFar, 16)

        /**
         * Build all cards for own and contact health status as well as status updates
         */
        if (ownHealthStatus != HealthStatusData.NoHealthStatus ||
            contactsHealthStatus != HealthStatusData.NoHealthStatus ||
            showQuarantineEnd ||
            someoneHasRecoveredHealthStatus != HealthStatusData.NoHealthStatus) {

            /**
             * Build card for own health status if available
             */
            if (ownHealthStatus != HealthStatusData.NoHealthStatus) {
                buildOwnHealthStatus()
            }

            /**
             * Add single space if own AND contact health state are available
             */
            if (ownHealthStatus != HealthStatusData.NoHealthStatus &&
                contactsHealthStatus != HealthStatusData.NoHealthStatus) {
                emptySpace(modelCountBuiltSoFar, 16)
            }

            /**
             * Build card for contacts health status if available
             */
            if (contactsHealthStatus != HealthStatusData.NoHealthStatus) {
                buildContactHealthStatus()
            }

            /**
             * Add single space if own OR contact health state are available AND someone has recovered
             */
            if ((ownHealthStatus != HealthStatusData.NoHealthStatus ||
                    contactsHealthStatus != HealthStatusData.NoHealthStatus) &&
                someoneHasRecoveredHealthStatus == HealthStatusData.SomeoneHasRecovered) {
                emptySpace(modelCountBuiltSoFar, 16)
            }

            /**
             * Build card for someone has recovered if available
             */
            if (someoneHasRecoveredHealthStatus == HealthStatusData.SomeoneHasRecovered) {
                buildSomeoneHasRecoveredStatus()
            }

            /**
             * Add single space if own or contact health state are available or someone has recovered AND the quarantine should end
             */
            if ((ownHealthStatus != HealthStatusData.NoHealthStatus ||
                    contactsHealthStatus != HealthStatusData.NoHealthStatus ||
                    someoneHasRecoveredHealthStatus == HealthStatusData.SomeoneHasRecovered) &&
                showQuarantineEnd) {
                emptySpace(modelCountBuiltSoFar, 16)
            }

            /**
             * Build card for quarantine end if available
             */
            if (showQuarantineEnd) {
                buildQuarantineEndStatus()
            }

            /**
             * Add single space if a single card is shown
             */
            if (ownHealthStatus != HealthStatusData.NoHealthStatus ||
                contactsHealthStatus != HealthStatusData.NoHealthStatus ||
                someoneHasRecoveredHealthStatus == HealthStatusData.SomeoneHasRecovered ||
                showQuarantineEnd) {
                emptySpace(modelCountBuiltSoFar, 32)
            }
        }

        handshakeHeadlineWithHistory(onSavedEncountersClick) {
            id("handshake_title")
            title(context.string(R.string.main_body_contact_title))
            savedEncounters(savedEncounters)
        }

        emptySpace(modelCountBuiltSoFar, 16)

        handshakeImage {
            id("handshake_image_inactive")
            active(false)
        }

        emptySpace(modelCountBuiltSoFar, 16)

        smallDescription {
            id("automatic_handshake_description_enabled")
            description(
                if (ownHealthStatus is HealthStatusData.SicknessCertificate) {
                    context.string(R.string.main_automatic_handshake_description_disabled)
                }
                else {
                    context.string(R.string.main_body_contact_description)
                }
            )
        }

        emptySpace(modelCountBuiltSoFar, 24)

        buttonType1(onManualHandshakeClick) {
            id("handshake_button")
            text(context.string(R.string.main_button_start_handshake_button))
            enabled((ownHealthStatus is HealthStatusData.SicknessCertificate).not())
        }

        emptySpace(modelCountBuiltSoFar, 16)

        buildShareAppCard()

        if ((ownHealthStatus is HealthStatusData.SelfTestingSuspicionOfSickness).not()
            && (ownHealthStatus is HealthStatusData.SicknessCertificate).not()
        ) {
            emptySpace(modelCountBuiltSoFar, 16)

            verticalBackgroundModelGroup(
                listOf(
                    EmptySpaceModel_()
                        .id(modelCountBuiltSoFar)
                        .height(32),
                    DescriptionBlockModel_()
                        .id("feel")
                        .title(context.string(R.string.main_body_feeling_title))
                        .description(context.string(R.string.main_body_feeling_description)),
                    EmptySpaceModel_()
                        .id(modelCountBuiltSoFar)
                        .height(16),
                    ButtonType2Model_(onFeelingClick)
                        .id("feel_button")
                        .text(context.string(R.string.main_button_feel_today_button)),
                    EmptySpaceModel_()
                        .id(modelCountBuiltSoFar)
                        .height(40)
                )
            ) {
                backgroundColor(R.color.white)
            }
        }

        if ((ownHealthStatus is HealthStatusData.SicknessCertificate).not()) {

            emptySpace(modelCountBuiltSoFar, 24)

            verticalBackgroundModelGroup(
                listOf(
                    EmptySpaceModel_()
                        .id(modelCountBuiltSoFar)
                        .height(32),
                    DescriptionBlockModel_()
                        .id("report")
                        .title(context.string(R.string.main_body_report_title))
                        .description(context.string(R.string.main_body_report_description)),
                    EmptySpaceModel_()
                        .id(modelCountBuiltSoFar)
                        .height(16),
                    ButtonType2Model_(onReportClick)
                        .id("report_button")
                        .text(context.string(R.string.main_body_report_button)),
                    EmptySpaceModel_()
                        .id(modelCountBuiltSoFar)
                        .height(40)
                )
            ) {
                backgroundColor(R.color.background_gray)
            }
        } else {
            emptySpace(modelCountBuiltSoFar, 40)
        }
    }

    /**
     * Build card for own health status
     */
    private fun buildOwnHealthStatus() {
        val modelList = arrayListOf<EpoxyModel<out Any>>()

        EmptySpaceModel_()
            .id(modelCountBuiltSoFar)
            .height(32)
            .addTo(modelList)

        HealthStatusModel_(onHealthStatusClick)
            .id("own_health_status")
            .data(ownHealthStatus)
            .addTo(modelList)

        if (ownHealthStatus is HealthStatusData.SelfTestingSuspicionOfSickness) {
            EmptySpaceModel_()
                .id(modelCountBuiltSoFar)
                .height(16)
                .addTo(modelList)

            ButtonType2Model_(onRevokeSuspicionClick)
                .id("own_health_status_present_revoke_suspicion")
                .text(context.string(R.string.self_testing_suspicion_button_revoke))
                .addTo(modelList)

            EmptySpaceModel_()
                .id(modelCountBuiltSoFar)
                .height(16)
                .addTo(modelList)

            ButtonType2Model_(onPresentMedicalReportClick)
                .id("own_health_status_present_medical_report_button")
                .text(context.string(R.string.self_testing_suspicion_secondary_button))
                .addTo(modelList)
        }

        if (ownHealthStatus is HealthStatusData.SelfTestingSymptomsMonitoring) {
            EmptySpaceModel_()
                .id(modelCountBuiltSoFar)
                .height(16)
                .addTo(modelList)

            ButtonType2Model_(onCheckSymptomsAgainClick)
                .id("own_health_status_check_symptoms_button")
                .text(context.string(R.string.self_testing_symptoms_secondary_button))
                .addTo(modelList)
        }

        if (ownHealthStatus is HealthStatusData.SicknessCertificate) {
            EmptySpaceModel_()
                .id(modelCountBuiltSoFar)
                .height(16)
                .addTo(modelList)

            ButtonType2Model_(onRevokeSicknessClick)
                .id("own_health_status_revoke_sickness")
                .text(context.string(R.string.sickness_certificate_attest_revoke))
                .addTo(modelList)
        }

        EmptySpaceModel_()
            .id(modelCountBuiltSoFar)
            .height(32)
            .addTo(modelList)

        verticalBackgroundModelGroup(modelList) {
            id("vertical_model_group_own_health_status")
            backgroundColor(R.color.background_gray)
        }
    }

    /**
     * Build card for contacts health status
     */
    private fun buildContactHealthStatus() {
        val modelList = arrayListOf<EpoxyModel<out Any>>()

        EmptySpaceModel_()
            .id(modelCountBuiltSoFar)
            .height(32)
            .addTo(modelList)

        HealthStatusModel_(onHealthStatusClick)
            .id("contacts_health_status")
            .data(contactsHealthStatus)
            .ownHealthStatus(ownHealthStatus)
            .addTo(modelList)

        EmptySpaceModel_()
            .id(modelCountBuiltSoFar)
            .height(32)
            .addTo(modelList)

        verticalBackgroundModelGroup(modelList) {
            id("vertical_model_group_contact_health_status")
            backgroundColor(R.color.background_gray)
        }
    }

    /**
     * Build card for someone has recovered
     */
    private fun buildSomeoneHasRecoveredStatus() {
        val modelList = arrayListOf<EpoxyModel<out Any>>()

        EmptySpaceModel_()
            .id(modelCountBuiltSoFar)
            .height(32)
            .addTo(modelList)

        StatusUpdateModel_(onSomeoneHasRecoveredCloseClick)
            .id("someone_has_recovered_health_status")
            .title(context.string(R.string.main_status_update_headline))
            .description(context.string(R.string.main_status_update_contact_sickness_not_confirmed_message))
            .cardStatus(CardUpdateStatus.ContactUpdate)
            .addTo(modelList)

        EmptySpaceModel_()
            .id(modelCountBuiltSoFar)
            .height(32)
            .addTo(modelList)

        verticalBackgroundModelGroup(modelList) {
            id("vertical_model_group_someone_has_recovered")
            backgroundColor(R.color.background_gray)
        }
    }

    /**
     * Build card for quarantine end
     */
    private fun buildQuarantineEndStatus() {
        val modelList = arrayListOf<EpoxyModel<out Any>>()

        EmptySpaceModel_()
            .id(modelCountBuiltSoFar)
            .height(32)
            .addTo(modelList)

        StatusUpdateModel_(onQuarantineEndCloseClick)
            .id("quarantine_ended")
            .title(context.string(R.string.local_notification_quarantine_end_headline))
            .description(context.string(R.string.local_notification_quarantine_end_message))
            .cardStatus(CardUpdateStatus.EndOfQuarantine)
            .addTo(modelList)

        EmptySpaceModel_()
            .id(modelCountBuiltSoFar)
            .height(32)
            .addTo(modelList)

        verticalBackgroundModelGroup(modelList) {
            id("vertical_model_group_end_of_quarantine")
            backgroundColor(R.color.background_gray)
        }
    }

    /**
     * Build card for sharing the app
     */
    private fun buildShareAppCard() {
        val modelList = arrayListOf<EpoxyModel<out Any>>()

        EmptySpaceModel_()
            .id(modelCountBuiltSoFar)
            .height(32)
            .addTo(modelList)

        DashboardShareAppModel_(onShareAppClick)
            .id("share_app")
            .addTo(modelList)

        EmptySpaceModel_()
            .id(modelCountBuiltSoFar)
            .height(32)
            .addTo(modelList)

        verticalBackgroundModelGroup(modelList) {
            id("vertical_model_group_share_app")
            backgroundColor(R.color.background_gray)
        }
    }
}

sealed class CardUpdateStatus {

    object ContactUpdate : CardUpdateStatus()
    object EndOfQuarantine : CardUpdateStatus()
}
