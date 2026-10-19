package com.example.hw_cub.stock.data

import com.example.hw_cub.stock.data.remote.StockDayAvgDto
import com.example.hw_cub.stock.data.remote.StockDayDto
import com.example.hw_cub.stock.domain.StockDay
import java.math.BigDecimal
import java.time.DateTimeException
import java.time.LocalDate

private const val ROC_YEAR_OFFSET = 1911

/**
 * Maps a TWSE row to the domain model, or returns `null` when the row has no
 * security code and therefore no usable identity.
 *
 * [monthlyAveragePrice] comes from a separate endpoint and is joined by code.
 */
fun StockDayDto.toDomain(monthlyAveragePrice: BigDecimal? = null): StockDay? {
    val code = code?.trim().orEmpty()
    if (code.isEmpty()) return null
    return StockDay(
        code = code,
        name = name?.trim().orEmpty(),
        date = parseRocDate(date),
        tradeVolume = tradeVolume.toLongOrNullSafe(),
        tradeValue = tradeValue.toLongOrNullSafe(),
        openingPrice = openingPrice.toBigDecimalOrNullSafe(),
        highestPrice = highestPrice.toBigDecimalOrNullSafe(),
        lowestPrice = lowestPrice.toBigDecimalOrNullSafe(),
        closingPrice = closingPrice.toBigDecimalOrNullSafe(),
        change = change.toBigDecimalOrNullSafe(),
        monthlyAveragePrice = monthlyAveragePrice,
        transactions = transaction.toLongOrNullSafe(),
    )
}

/** Builds a code-to-monthly-average lookup, skipping rows without a code or value. */
fun List<StockDayAvgDto>.monthlyAveragesByCode(): Map<String, BigDecimal> =
    mapNotNull { row ->
        val code = row.code?.trim()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
        val average = row.monthlyAveragePrice.toBigDecimalOrNullSafe() ?: return@mapNotNull null
        code to average
    }.toMap()

/** Parses an ROC-calendar `yyyMMdd` date such as `1151016` (2026-10-16). */
internal fun parseRocDate(value: String?): LocalDate? {
    val digits = value?.trim().orEmpty()
    if (digits.length < 7 || !digits.all(Char::isDigit)) return null
    val rocYear = digits.dropLast(4).toInt()
    val month = digits.takeLast(4).take(2).toInt()
    val day = digits.takeLast(2).toInt()
    return try {
        LocalDate.of(rocYear + ROC_YEAR_OFFSET, month, day)
    } catch (_: DateTimeException) {
        null
    }
}

private fun String?.normalizedNumber(): String? =
    this?.replace(",", "")?.trim()?.takeIf { it.isNotEmpty() }

private fun String?.toLongOrNullSafe(): Long? = normalizedNumber()?.toLongOrNull()

private fun String?.toBigDecimalOrNullSafe(): BigDecimal? =
    normalizedNumber()?.toBigDecimalOrNull()
