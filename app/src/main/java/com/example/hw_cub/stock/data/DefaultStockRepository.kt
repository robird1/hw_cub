package com.example.hw_cub.stock.data

import com.example.hw_cub.core.DataError
import com.example.hw_cub.core.DataResult
import com.example.hw_cub.stock.data.remote.TwseService
import com.example.hw_cub.stock.domain.StockDay
import com.example.hw_cub.stock.domain.StockRepository
import com.example.hw_cub.stock.domain.StockValuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Fetches TWSE data and maps it off the main thread.
 *
 * This is the error boundary: Retrofit and OkHttp exceptions are translated
 * to [DataError] here so they never reach the presentation layer.
 */
class DefaultStockRepository(
    private val service: TwseService,
    private val mappingDispatcher: CoroutineDispatcher,
) : StockRepository {

    /** Both requests must succeed; a failure in either fails the whole load. */
    override suspend fun getDailyQuotes(): DataResult<List<StockDay>> =
        safeCall { fetchQuotes() }

    override suspend fun getValuation(code: String): DataResult<StockValuation?> =
        safeCall {
            val rows = service.getBwibbuAll()
            withContext(mappingDispatcher) {
                rows.firstOrNull { it.code?.trim() == code }?.toDomain()
            }
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

    private suspend fun <T> safeCall(block: suspend () -> T): DataResult<T> =
        try {
            DataResult.Success(block())
        } catch (e: HttpException) {
            DataResult.Failure(DataError.Server(e.code()))
        } catch (_: SerializationException) {
            DataResult.Failure(DataError.InvalidResponse)
        } catch (_: IOException) {
            DataResult.Failure(DataError.Network)
        }
}
