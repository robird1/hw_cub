# hw_cub

An Android app that shows daily trading data for every security listed on the
Taiwan Stock Exchange (TWSE), using the public
[TWSE OpenAPI](https://openapi.twse.com.tw/).

## Features

- **Daily quotes list**: opening, closing, highest and lowest price, price
  change, monthly average price, transactions, trade volume and trade value
  for each security on the latest trading day.
- **Price colors**: following the Taiwan market convention, red means up and
  green means down. The closing price is colored by comparison with the
  monthly average.
- **Search** by stock code (prefix) or name, ignoring case and surrounding
  spaces.
- **Sort** by stock code, ascending or descending, from the menu button.
- **Detail screen** with the P/E ratio, dividend yield and P/B ratio. ETFs and
  other securities without published ratios show an explanation instead.
- **Pull to refresh**, with clear error messages and Retry when the network or
  server fails.
- **Adaptive layout**: one column in portrait, two in landscape or on tablets.
- **Dark mode**, and Material You dynamic color on Android 12+.
- **State that survives** rotation and process death: the search text, sort
  order and current screen are restored.
- **English and Traditional Chinese** UI.

## Tech stack

| Area | Library |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM with a domain layer (use cases) |
| Navigation | Navigation 3 |
| Dependency injection | Koin |
| Async | Kotlin Coroutines and Flow (`StateFlow`) |
| Network | Retrofit, OkHttp, kotlinx.serialization |
| Testing | JUnit 4, kotlinx-coroutines-test, Compose UI test |

Minimum SDK 26 (Android 8.0), target SDK 37.

## Architecture

The stock feature is split into three layers. Dependencies point inward: the
domain layer knows nothing about Android, Retrofit or Compose.

```
app/src/main/java/com/example/hw_cub/
├── core/                  DataResult and DataError, shared by all layers
├── di/                    Koin modules
├── navigation/            Navigation 3 graph
├── ui/theme/              Material 3 theme and price colors
└── stock/
    ├── data/              DefaultStockRepository, DTO-to-domain mappers
    │   └── remote/        Retrofit TwseService and DTOs
    ├── domain/
    │   ├── model/         StockDay, StockValuation, PriceTrend, StockSortOrder
    │   ├── repository/    StockRepository interface
    │   └── usecase/       GetDailyQuotes, GetStockValuation, FilterAndSortStocks
    └── presentation/      ViewModels, UI state classes, Compose screens
```

- **Presentation**: each screen has a ViewModel that exposes one UI state
  class (`StockListUiState`, `StockDetailUiState`) as a `StateFlow`. Screens
  are stateless composables driven by that state.
- **Domain**: use cases hold the app's actions; models hold business rules
  such as the closing price versus the monthly average.
- **Data**: the repository calls the API, maps DTOs to domain models off the
  main thread, and converts network, HTTP and parsing failures into
  `DataError` so the UI never handles library exceptions.

## Data source

| Endpoint | Used for |
|---|---|
| `exchangeReport/STOCK_DAY_ALL` | Daily quotes for every security |
| `exchangeReport/STOCK_DAY_AVG_ALL` | Monthly average price, joined by code |
| `exchangeReport/BWIBBU_ALL` | P/E ratio, dividend yield and P/B ratio |

The base URL is set in `app/build.gradle.kts` as `TWSE_BASE_URL`. TWSE sends
every value as a string and dates in the ROC calendar (`1151016` is
2026-10-16); the mappers in `stock/data/StockDayMapper.kt` convert them to
typed values.
