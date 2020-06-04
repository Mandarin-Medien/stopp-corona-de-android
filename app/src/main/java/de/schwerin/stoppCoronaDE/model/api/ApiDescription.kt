package de.schwerin.stoppCoronaDE.model.api

import de.schwerin.stoppCoronaDE.model.entities.configuration.ApiConfigurationHolder
import de.schwerin.stoppCoronaDE.model.entities.infection.info.ApiInfectionInfoRequest
import de.schwerin.stoppCoronaDE.model.entities.infection.message.ApiInfectionMessages
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * Description of REST Api for Retrofit.
 */
interface ApiDescription {

    @GET("configuration")
    suspend fun configuration(): ApiConfigurationHolder

    @GET("infection-messages")
    suspend fun infectionMessages(@Query("addressPrefix") addressPrefix: String, @Query("fromId") fromId: Long? = null): ApiInfectionMessages

    @PUT("infection-info")
    suspend fun infectionInfo(@Body infectionInfoRequest: ApiInfectionInfoRequest)
}
