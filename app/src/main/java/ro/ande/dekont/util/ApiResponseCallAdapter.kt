package ro.ande.dekont.util

import kotlinx.coroutines.Deferred
import okhttp3.Request
import retrofit2.*
import ro.ande.dekont.api.ApiResponse
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/** Taken from https://stackoverflow.com/a/57816819 */

/**
 * A [CallAdapter.Factory] for use with Kotlin coroutines and [ApiResponse].
 *
 * Adding this class to [Retrofit] allows you to return [ApiResponse] from
 * suspend service methods.
 *
 *     interface MyService {
 *       &#64;GET("user/me")
 *       suspend fun getUser(): ApiResponse&lt;User&gt;
 *     }
 *
 */
class ApiResponseCallAdapterFactory : CallAdapter.Factory() {
    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        return if (getRawType(returnType) == Call::class.java) {
            val callType = getParameterUpperBound(0, returnType as ParameterizedType)
            if (getRawType(callType) == ApiResponse::class.java) {
                ApiResponseCallAdapter(getParameterUpperBound(0, callType as ParameterizedType))
            } else null
        } else null
    }
}

private class ApiResponseCallAdapter(
        private val responseType: Type
) : CallAdapter<Type, Call<ApiResponse<Type>>> {

    override fun responseType() = responseType
    override fun adapt(call: Call<Type>): Call<ApiResponse<Type>> = ApiResponseCall(call)
}

/** Transforms Call<T> into Call<ApiResponse<T>> */
private class ApiResponseCall<T>(private val proxy: Call<T>): Call<ApiResponse<T>> {
    override fun enqueue(callback: Callback<ApiResponse<T>>) {
        proxy.enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, t: Throwable) {
                val result = ApiResponse.create<T>(t)
                callback.onResponse(this@ApiResponseCall, Response.success(result))
            }

            override fun onResponse(call: Call<T>, response: Response<T>) {
                val result = ApiResponse.create(response)
                callback.onResponse(this@ApiResponseCall, Response.success(result))
            }
        })
    }

    override fun clone(): Call<ApiResponse<T>> = ApiResponseCall(proxy.clone())
    override fun isExecuted(): Boolean = proxy.isExecuted
    override fun isCanceled(): Boolean = proxy.isCanceled
    override fun cancel() = proxy.cancel()
    override fun execute(): Response<ApiResponse<T>> = throw NotImplementedError("Don't try to execute this call")
    override fun request(): Request = proxy.request()
}
