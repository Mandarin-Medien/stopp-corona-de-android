package de.schwerin.stoppCoronaDE.di

import de.schwerin.stoppCoronaDE.model.entities.infection.message.MessageType
import de.schwerin.stoppCoronaDE.model.repositories.NearbyRepository
import de.schwerin.stoppCoronaDE.model.repositories.NearbyRepositoryImpl
import de.schwerin.stoppCoronaDE.screens.base.DebugViewModel
import de.schwerin.stoppCoronaDE.screens.dashboard.DashboardViewModel
import de.schwerin.stoppCoronaDE.screens.dashboard.dialog.MicrophoneExplanationDialogViewModel
import de.schwerin.stoppCoronaDE.screens.debug.events.DebugAutomaticEventsViewModel
import de.schwerin.stoppCoronaDE.screens.handshake.HandshakeViewModel
import de.schwerin.stoppCoronaDE.screens.history.ContactHistoryViewModel
import de.schwerin.stoppCoronaDE.screens.infection_info.InfectionInfoViewModel
import de.schwerin.stoppCoronaDE.screens.onboarding.OnboardingViewModel
import de.schwerin.stoppCoronaDE.screens.questionnaire.QuestionnaireViewModel
import de.schwerin.stoppCoronaDE.screens.questionnaire.guideline.QuestionnaireGuidelineViewModel
import de.schwerin.stoppCoronaDE.screens.questionnaire.hint.QuestionnaireHintViewModel
import de.schwerin.stoppCoronaDE.screens.questionnaire.selfmonitoring.QuestionnaireSelfMonitoringViewModel
import de.schwerin.stoppCoronaDE.screens.reporting.ReportingViewModel
import de.schwerin.stoppCoronaDE.screens.reporting.personalData.ReportingPersonalDataViewModel
import de.schwerin.stoppCoronaDE.screens.reporting.reportStatus.ReportingStatusViewModel
import de.schwerin.stoppCoronaDE.screens.reporting.reportStatus.guideline.CertificateReportGuidelinesViewModel
import de.schwerin.stoppCoronaDE.screens.routing.RouterViewModel
import de.schwerin.stoppCoronaDE.screens.webView.WebViewViewModel
import org.koin.androidx.viewmodel.ext.koin.viewModel
import org.koin.dsl.module.module

/**
 * Module for providing viewModels.
 */
val viewModelModule = module(override=true)  {

    viewModel {
        DebugViewModel(
            appDispatchers = get(),
            infectionMessengerRepository = get(),
            notificationsRepository = get(),
            quarantineRepository = get(),
            infectionMessageDao = get(),
            nearbyRecordDao = get()
        )
    }

    viewModel {
        DebugAutomaticEventsViewModel(
            appDispatchers = get(),
            automaticDiscoveryDao = get(),
            contextInteractor = get(),
            cryptoRepository = get()
        )
    }

    viewModel {
        DashboardViewModel(
            appDispatchers = get(),
            dashboardRepository = get(),
            infectionMessengerRepository = get(),
            quarantineRepository = get(),
            configurationRepository = get(),
            databaseCleanupManager = get(),
            nearbyRepository = get(),
            googleApiClientBuilder = get()
        )
    }

    viewModel {
        MicrophoneExplanationDialogViewModel(
            appDispatchers = get(),
            dashboardRepository = get()
        )
    }

    viewModel {
        InfectionInfoViewModel(
            appDispatchers = get(),
            infectionMessengerRepository = get(),
            quarantineRepository = get()
        )
    }

    viewModel {
        HandshakeViewModel(
            appDispatchers = get(),
            googleApiClientBuilder = get(),
            nearbyRepository = get()
        )
    }

    viewModel {
        RouterViewModel(
            appDispatchers = get(),
            onboardingRepository = get()
        )
    }

    viewModel {
        OnboardingViewModel(
            appDispatchers = get(),
            onboardingRepository = get(),
            dataPrivacyRepository = get()
        )
    }

    viewModel {
        QuestionnaireViewModel(
            appDispatchers = get(),
            apiInteractor = get()
        )
    }

    viewModel {
        QuestionnaireGuidelineViewModel(
            appDispatchers = get(),
            quarantineRepository = get()
        )
    }

    /**
     * The fragment which implements this viewModel needs to call
     * ```
     * override fun onCreate(savedInstanceState: Bundle?) {
     *     connectToScope(ReportingRepository.SCOPE_NAME)
     *     super.onCreate(savedInstanceState)
     * }
     * ```
     */
    viewModel {
        ReportingPersonalDataViewModel(
            appDispatchers = get(),
            reportingRepository = get()
        )
    }

    /**
     * The fragment which implements this viewModel needs to call
     * ```
     * override fun onCreate(savedInstanceState: Bundle?) {
     *     connectToScope(ReportingRepository.SCOPE_NAME)
     *     super.onCreate(savedInstanceState)
     * }
     * ```
     */
    viewModel {
        ReportingStatusViewModel(
            appDispatchers = get(),
            reportingRepository = get(),
            quarantineRepository = get(),
            contextInteractor = get()
        )
    }

    /**
     * The fragment which implements this viewModel needs to call
     * ```
     * override fun onCreate(savedInstanceState: Bundle?) {
     *     connectToScope(ReportingRepository.SCOPE_NAME)
     *     super.onCreate(savedInstanceState)
     * }
     * ```
     */
    viewModel { (messageType: MessageType) ->
        ReportingViewModel(
            appDispatchers = get(),
            reportingRepository = get(),
            messageType = messageType
        )
    }

    viewModel {
        ContactHistoryViewModel(
            appDispatchers = get(),
            nearbyRepository = get()
        )
    }

    viewModel {
        WebViewViewModel(
            appDispatchers = get()
        )
    }

    viewModel {
        CertificateReportGuidelinesViewModel(
            appDispatchers = get(),
            quarantineRepository = get()
        )
    }

    viewModel {
        QuestionnaireSelfMonitoringViewModel(
            appDispatchers = get(),
            quarantineRepository = get()
        )
    }

    viewModel {
        QuestionnaireHintViewModel(
            appDispatchers = get(),
            quarantineRepository = get()
        )
    }


    single<NearbyRepository> {
        NearbyRepositoryImpl(get(),get(),get(),get())
    }
}
