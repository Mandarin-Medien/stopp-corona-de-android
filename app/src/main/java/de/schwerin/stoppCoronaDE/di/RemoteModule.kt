package de.schwerin.stoppCoronaDE.di

import android.content.Context
import de.schwerin.stoppCoronaDE.constants.Constants
import de.schwerin.stoppCoronaDE.constants.Constants.API.CERTIFICATE_CHAIN_DEFAULT
import de.schwerin.stoppCoronaDE.constants.Constants.API.CERTIFICATE_CHAIN_TAN
import de.schwerin.stoppCoronaDE.constants.Constants.API.HOSTNAME
import de.schwerin.stoppCoronaDE.constants.Constants.API.HOSTNAME_TAN
import de.schwerin.stoppCoronaDE.constants.Constants.API.Header
import de.schwerin.stoppCoronaDE.constants.isBeta
import de.schwerin.stoppCoronaDE.constants.isDebug
import de.schwerin.stoppCoronaDE.di.CertificatePinnerTag.defaultCertificatePinnerTag
import de.schwerin.stoppCoronaDE.di.CertificatePinnerTag.tanCertificatePinnerTag
import de.schwerin.stoppCoronaDE.model.api.ApiDescription
import de.schwerin.stoppCoronaDE.model.api.ApiInteractor
import de.schwerin.stoppCoronaDE.model.api.ApiInteractorImpl
import de.schwerin.stoppCoronaDE.model.api.TanApiDescription
import de.schwerin.stoppCoronaDE.model.entities.infection.info.LocalDateNotIsoAdapter
import de.schwerin.stoppCoronaDE.skeleton.core.di.createApi
import de.schwerin.stoppCoronaDE.skeleton.core.di.createMoshi
import de.schwerin.stoppCoronaDE.skeleton.core.di.createOkHttpClient
import de.schwerin.stoppCoronaDE.skeleton.core.model.api.addHeaders
import com.google.firebase.messaging.FirebaseMessaging
import okhttp3.Cache
import okhttp3.CertificatePinner
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import okhttp3.logging.HttpLoggingInterceptor.Level.NONE
import org.koin.dsl.module.module

object CertificatePinnerTag {
    const val defaultCertificatePinnerTag = "default"
    const val tanCertificatePinnerTag = "TAN"
}

/**
 * Module for Rest Service.
 */
val remoteModule = module {

    single {
        Cache(get<Context>().cacheDir, Constants.API.HTTP_CACHE_SIZE)
    }

    single(name = defaultCertificatePinnerTag) {
        CertificatePinner.Builder().apply {
            CERTIFICATE_CHAIN_DEFAULT.forEach { pin ->
                add(HOSTNAME, pin)
            }
        }.build()
    }

    single(name = tanCertificatePinnerTag) {
        CertificatePinner.Builder().apply {
            CERTIFICATE_CHAIN_TAN.forEach { pin ->
                add(HOSTNAME_TAN, pin)
            }
        }.build()
    }

    single(name = defaultCertificatePinnerTag) {
        createOkHttpClient(if (isDebug || isBeta) BODY else NONE) {
            addHeaders(
                Header.AUTHORIZATION_KEY to Header.AUTHORIZATION_VALUE,
                Header.APP_ID_KEY to Header.APP_ID_VALUE
            )
            cache(get())
            certificatePinner(get(defaultCertificatePinnerTag))
        }
    }

    single(name = tanCertificatePinnerTag) {
        createOkHttpClient(if (isDebug || isBeta) BODY else NONE) {
            addHeaders(
                Header.AUTHORIZATION_KEY to Header.AUTHORIZATION_VALUE,
                Header.APP_ID_KEY to Header.APP_ID_VALUE
            )
            cache(get())
            certificatePinner(get(tanCertificatePinnerTag))
        }
    }

    single {
        createMoshi {
            add(LocalDateNotIsoAdapter)
        }
    }

    single {
        createApi<ApiDescription>(
            baseUrl = Constants.API.BASE_URL,
            okHttpClient = get(defaultCertificatePinnerTag),
            moshi = get()
        )
    }

    single {
        createApi<TanApiDescription>(
            baseUrl = Constants.API.BASE_URL_TAN,
            okHttpClient = get(tanCertificatePinnerTag),
            moshi = get()
        )
    }

    single<ApiInteractor> {
        ApiInteractorImpl(
            appDispatchers = get(),
            apiDescription = get(),
            tanApiDescription = get(),
            dataPrivacyRepository = get()
        )
    }

    single {
        FirebaseMessaging.getInstance()
    }
}