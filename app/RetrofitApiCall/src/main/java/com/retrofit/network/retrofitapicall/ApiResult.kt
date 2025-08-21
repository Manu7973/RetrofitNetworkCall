
package com.retrofit.network.retrofitapicall

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Failure(
        val code: Int? = null,
        val message: String? = null,
        val throwable: Throwable? = null
    ) : ApiResult<Nothing>()
}
