package data

import com.indwealth.core.rest.data.Result
import com.indwealth.core.rest.data.api.RemoteSource
import com.indwealth.core.rest.data.api.RetrofitFactory
import com.indwealth.core.util.manager.SingletonHolder
import model.SearchResponse

class ZomatoRepository private constructor(apiFactory: RetrofitFactory) {

    private val apiService: ZomatoApiSerice =
        apiFactory.getApiService(ZomatoApiSerice::class.java) as ZomatoApiSerice

    suspend fun getSummary(
        q: String,
        sort: String,
        count: Int

    ): Result<SearchResponse> = RemoteSource.safeApiCall {

        val map = hashMapOf(
            "q" to q,
            "sort" to sort,
            "count" to count.toString()
        )


        apiService.getSummary(map)
    }

    companion object : SingletonHolder<ZomatoRepository, RetrofitFactory>(::ZomatoRepository)
}