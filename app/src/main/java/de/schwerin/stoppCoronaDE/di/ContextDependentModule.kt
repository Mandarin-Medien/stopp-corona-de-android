package de.schwerin.stoppCoronaDE.di

import android.app.Activity
import android.security.KeyPairGeneratorSpec
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.WorkManager
import de.schwerin.stoppCoronaDE.model.assets.AssetInteractor
import de.schwerin.stoppCoronaDE.model.assets.AssetInteractorImpl
import de.schwerin.stoppCoronaDE.model.repositories.other.ContextInteractor
import de.schwerin.stoppCoronaDE.model.repositories.other.ContextInteractorImpl
import de.schwerin.stoppCoronaDE.model.repositories.other.OfflineSyncer
import de.schwerin.stoppCoronaDE.model.repositories.other.OfflineSyncerImpl
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.nearby.Nearby
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

/**
 * Module for providing android context related dependencies.
 */
internal val contextDependentModule = module {

    single<ContextInteractor> {
        ContextInteractorImpl(
            context = androidContext()
        )
    }

    single<AssetInteractor> {
        AssetInteractorImpl(
            appDispatchers = get(),
            moshi = get(),
            filesRepository = get()
        )
    }

    single { (activity: Activity) ->
        Nearby.getMessagesClient(activity)
    }

    single {
        GoogleApiClient.Builder(androidContext())
    }

    @Suppress("DEPRECATION")
    single {
        KeyPairGeneratorSpec.Builder(androidContext())
    }

    single<OfflineSyncer>(createOnStart = true) {
        OfflineSyncerImpl(
            appDispatchers = get(),
            contextInteractor = get(),
            sharedPreferences = get(),
            processLifecycleOwner = ProcessLifecycleOwner.get(),
            configurationRepository = get(),
            dataPrivacyRepository = get(),
            infectionMessengerRepository = get(),
            pushMessagingRepository = get()
        )
    }

    single {
        WorkManager.getInstance(androidContext())
    }

    single {
        LocalBroadcastManager.getInstance(androidContext())
    }
}
