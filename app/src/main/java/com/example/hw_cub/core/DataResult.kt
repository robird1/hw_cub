package com.example.hw_cub.core

/** Why a data request failed, independent of the networking library. */
sealed interface DataError {
    /** No connection, timeout, or another I/O failure. */
    data object Network : DataError

    /** The server answered with a non-2xx [code]. */
    data class Server(val code: Int) : DataError

    /** The response arrived but could not be decoded. */
    data object InvalidResponse : DataError
}

/** Outcome of a repository call: either [Success] data or a [Failure]. */
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Failure(val error: DataError) : DataResult<Nothing>
}
