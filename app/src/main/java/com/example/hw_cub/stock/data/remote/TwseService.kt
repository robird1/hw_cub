package com.example.hw_cub.stock.data.remote

import retrofit2.http.GET

/** Taiwan Stock Exchange OpenAPI (https://openapi.twse.com.tw). */
interface TwseService {
    /** Latest trading day's summary for every listed security. */
    @GET("exchangeReport/STOCK_DAY_ALL")
    suspend fun getStockDayAll(): List<StockDayDto>

    /** Latest closing price and monthly average price for every listed security. */
    @GET("exchangeReport/STOCK_DAY_AVG_ALL")
    suspend fun getStockDayAvgAll(): List<StockDayAvgDto>

    /** Latest P/E ratio, dividend yield and P/B ratio for every listed company. */
    @GET("exchangeReport/BWIBBU_ALL")
    suspend fun getBwibbuAll(): List<StockValuationDto>
}
