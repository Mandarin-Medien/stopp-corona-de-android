package de.schwerin.stoppCoronaDE.di

import de.schwerin.stoppCoronaDE.model.repositories.*
import org.koin.dsl.module.module

/**
 * Module for providing repositories.
 */
val repositoryModule = module {

    single<FilesRepository> {
        FilesRepositoryImpl(
            appDispatchers = get(),
            contextInteractor = get()
        )
    }

    single<PushMessagingRepository> {
        PushMessagingRepositoryImpl(
            firebaseMessaging = get(),
            cryptoRepository = get(),
            dataPrivacyRepository = get(),
            infectionMessengerRepository = get()
        )
    }

    single<NearbyRepository> {
        NearbyRepositoryImpl(
            cryptoRepository = get(),
            appDispatchers = get(),
            nearbyRecordDao = get(),
            handshakeCodewordRepository = get()
        )
    }

    @Suppress("DEPRECATION")
    single<CryptoRepository> {
        CryptoRepositoryImpl(
            keyPairGeneratorSpecBuilder = get()
        )
    }

    single<OnboardingRepository> {
        OnboardingRepositoryImpl(
            preferences = get()
        )
    }

    single<ConfigurationRepository> {
        ConfigurationRepositoryImpl(
            appDispatchers = get(),
            apiInteractor = get(),
            configurationDao = get(),
            assetInteractor = get()
        )
    }

    single<DashboardRepository> {
        DashboardRepositoryImpl(
            nearbyRecordDao = get(),
            preferences = get()
        )
    }


    single<InfectionMessengerRepository> {
        InfectionMessengerRepositoryImpl(
            appDispatchers = get(),
            apiInteractor = get(),
            infectionMessageDao = get(),
            cryptoRepository = get(),
            notificationsRepository = get(),
            preferences = get(),
            quarantineRepository = get(),
            workManager = get(),
            databaseCleanupManager = get()
        )
    }

    single<NotificationsRepository> {
        NotificationsRepositoryImpl(
            appDispatchers = get(),
            contextInteractor = get(),
            dataPrivacyRepository = get()
        )
    }

    single<DataPrivacyRepository> {
        DataPrivacyRepositoryImpl(
            preferences = get()
        )
    }

    single<QuarantineRepository> {
        QuarantineRepositoryImpl(
            appDispatchers = get(),
            preferences = get(),
            configurationRepository = get(),
            workManager = get()
        )
    }


    single<HandshakeCodewordRepository> {
        HandshakeCodewordRepositoryImpl(
            contextInteractor = get()
        )
    }
}
