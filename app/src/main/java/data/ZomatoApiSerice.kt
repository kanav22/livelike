package data

import model.SearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface ZomatoApiSerice {

    @GET("api/v2.1/search")
    suspend fun getSummary(@QueryMap queryMap: HashMap<String, String>): Response<SearchResponse>
}