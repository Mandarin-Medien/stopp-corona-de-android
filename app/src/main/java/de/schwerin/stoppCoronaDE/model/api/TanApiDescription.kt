package de.schwerin.stoppCoronaDE.model.api

import de.schwerin.stoppCoronaDE.model.entities.tan.ApiRequestTan
import de.schwerin.stoppCoronaDE.model.entities.tan.ApiRequestTanBody
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Description of REST Api for TAN-endpoint
 */
interface TanApiDescription {

    @POST("request-tan")
    suspend fun requestTan(@Body body: ApiRequestTanBody): ApiRequestTan
}
