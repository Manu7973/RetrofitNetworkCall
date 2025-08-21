package com.retrofit.network.retrofitapicall

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File

class ApiClient(
    private val baseUrl: String,
    private val isDebug: Boolean = false
) {
    private val client: OkHttpClient
    private val gson = Gson()

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = if (isDebug) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }

        client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    suspend fun callApi(
        requestType: RequestType,
        url: String,
        headers: Map<String, String> = emptyMap(),
        body: Any
    ): ApiResult<Response> {
        return try {
            val builder = Request.Builder().url(resolveUrl(url))

            headers.forEach { (k, v) -> builder.addHeader(k, v) }

            val jsonMediaType = "application/json; charset=utf-8".toMediaType()

            val requestBody = when (body) {
                is String -> body.toRequestBody(jsonMediaType)
                is Map<*, *> -> gson.toJson(body).toRequestBody(jsonMediaType)
                is File -> body.asRequestBody("application/octet-stream".toMediaType())
                else -> body?.let { gson.toJson(it).toRequestBody(jsonMediaType) }
            }

            when (requestType) {
                RequestType.GET -> builder.get()
                RequestType.POST -> builder.post(requestBody ?: "".toRequestBody(jsonMediaType))
                RequestType.PUT -> builder.put(requestBody ?: "".toRequestBody(jsonMediaType))
                RequestType.DELETE -> {
                    if (requestBody != null) builder.delete(requestBody) else builder.delete()
                }

                RequestType.MULTIPART -> {
                    val multipart = MultipartBody.Builder().setType(MultipartBody.FORM).apply {
                        when (body) {
                            is File -> addFormDataPart(
                                "file",
                                body.name,
                                body.asRequestBody("image/*".toMediaType())
                            )

                            is Map<*, *> -> body.forEach { (k, v) ->
                                addFormDataPart(k.toString(), v.toString())
                            }
                        }
                    }.build()
                    builder.post(multipart)
                }
            }

            val call = client.newCall(builder.build())
            val response = withContext(Dispatchers.IO) { call.execute() }

            if (response.isSuccessful) {
                ApiResult.Success(response)
            } else {
                ApiResult.Failure(response.code, response.message)
            }
        } catch (t: Throwable) {
            ApiResult.Failure(null, t.message, t)
        }
    }

    private fun resolveUrl(path: String): String {
        return if (path.startsWith("http")) path
        else baseUrl.trimEnd('/') + "/" + path.trimStart('/')
    }
}
