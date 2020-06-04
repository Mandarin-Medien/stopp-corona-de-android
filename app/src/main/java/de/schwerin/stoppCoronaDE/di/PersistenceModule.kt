package de.schwerin.stoppCoronaDE.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import de.schwerin.stoppCoronaDE.constants.Constants
import de.schwerin.stoppCoronaDE.model.db.DefaultDatabase
import de.schwerin.stoppCoronaDE.model.manager.DatabaseCleanupManager
import de.schwerin.stoppCoronaDE.model.manager.DatabaseCleanupManagerImpl
import de.schwerin.stoppCoronaDE.model.manager.PreferencesMigrationManager
import de.schwerin.stoppCoronaDE.model.manager.PreferencesMigrationManagerImpl
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

/**
 * Module for providing persistence related dependencies.
 */
internal val persistenceModule = module {

    single<SharedPreferences> {
        androidApplication().getSharedPreferences(Constants.Prefs.FILE_NAME, Context.MODE_PRIVATE)
    }

    single {
        Room.databaseBuilder(androidContext(), DefaultDatabase::class.java, Constants.DB.FILE_NAME)
            .addMigrations(*DefaultDatabase.migrations)
            .build()
    }


    single<PreferencesMigrationManager>(createOnStart = true) {
        PreferencesMigrationManagerImpl(
            preferences = get()
        )
    }

    single<DatabaseCleanupManager>(createOnStart = true) {
        DatabaseCleanupManagerImpl(
            appDispatchers = get(),
            configurationRepository = get(),
            infectionMessageDao = get(),
            quarantineRepository = get(),
            nearbyRecordDao = get()
        )
    }

    // DAOs

    single {
        get<DefaultDatabase>().configurationDao()
    }

    single {
        get<DefaultDatabase>().nearbyRecordDao()
    }

    single {
        get<DefaultDatabase>().infectionMessageDao()
    }

    single {
        get<DefaultDatabase>().automaticDiscoveryDao()
    }
}