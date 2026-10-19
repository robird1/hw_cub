package com.example.hw_cub.stock.data

import com.example.hw_cub.core.DataError
import com.example.hw_cub.core.DataResult
import com.example.hw_cub.stock.data.remote.TwseService
import com.example.hw_cub.stock.domain.StockDay
import com.example.hw_cub.stock.domain.StockRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Fetches daily quotes and monthly averages from TWSE in parallel, then
 * joins them by code off the main thread.
 *
 * This is the error boundary: Retrofit and OkHttp exceptions are translated
 * to [DataError] here so they never reach the presentation layer. Both
 * requests must succeed; a failure in either fails the whole load.
 */
class DefaultStockRepository(
    private val service: TwseService,
    private val mappingDispatcher: CoroutineDispatcher,
) : StockRepository {

    override suspend fun getDailyQuotes(): DataResult<List<StockDay>> =
        try {
            DataResult.Success(fetchQuotes())
        } catch (e: HttpException) {
            DataResult.Failure(DataError.Server(e.code()))
        } catch (_: SerializationException) {
            DataResult.Failure(DataError.InvalidResponse)
        } catch (_: IOException) {
            DataResult.Failure(DataError.Network)
        }

    private suspend fun fetchQuotes(): List<StockDay> = coroutineScope {
        val dailyRows = async { service.getStockDayAll() }
        val averageRows = async { service.getStockDayAvgAll() }
        val daily = dailyRows.await()
        val averages = averageRows.await()
        withContext(mappingDispatcher) {
            val averageByCode = averages.monthlyAveragesByCode()
            daily.mapNotNull { row ->
                row.toDomain(monthlyAveragePrice = row.code?.trim()?.let(averageByCode::get))
            }
        }
    }
}
