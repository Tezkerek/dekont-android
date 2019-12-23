package ro.ande.dekont.util

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import retrofit2.*
import ro.ande.dekont.api.ApiResponse
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/** Taken from https://github.com/JakeWharton/retrofit2-kotlin-coroutines-adapter/ and modified to use ApiResponse. */

/**
 * A [CallAdapter.Factory] for use with Kotlin coroutines.
 *
 * Adding this class to [Retrofit] allows you to return [Deferred] from
 * service methods.
 *
 *     interface MyService {
 *       &#64;GET("user/me")
 *       Deferred&lt;User&gt; getUser()
 *     }
 *
 * There are two configurations supported for the [Deferred] type parameter:
 *
 * * Direct body (e.g., `Deferred<User>`) returns the deserialized body for 2XX responses, throws
 * [HttpException] errors for non-2XX responses, and throws [IOException][java.io.IOException] for
 * network errors.
 * * ApiResponse wrapped body (e.g., `Deferred<ApiResponse<User>>`) returns a [ApiResponse] object for all
 * HTTP responses and throws [IOException][java.io.IOException] for network errors
 */
class CoroutineCallAdapterFactory : CallAdapter.Factory() {
    override fun get(returnType: Type, annotations: Array<Annotation>, retrofit: Retrofit): CallAdapter<*, *>? {
        if (Deferred::class.java != getRawType(returnType)) {
            return null
        }
        if (returnType !is ParameterizedType) {
            throw IllegalStateException(
                    "Deferred return type must be parameterized as Deferred<Foo> or Deferred<out Foo>")
        }
        val responseType = getParameterUpperBound(0, returnType)

        val rawDeferredType = getRawType(responseType)
        return if (rawDeferredType == ApiResponse::class.java) {
            if (responseType !is ParameterizedType) {
                throw IllegalStateException(
                        "Response must be parameterized as Response<Foo> or Response<out Foo>")
            }
            DeferredApiResponseCallAdapter<Any>(getParameterUpperBound(0, responseType))
        } else {
            BodyCallAdapter<Any>(responseType)
        }
    }
}

private class BodyCallAdapter<T>(
        private val responseType: Type
) : CallAdapter<T, Deferred<T>> {

    override fun responseType(): Type = responseType

    override fun adapt(call: Call<T>): Deferred<T> {
        val deferred = CompletableDeferred<T>()

        deferred.invokeOnCompletion {
            if (deferred.isCancelled) {
                call.cancel()
            }
        }

        call.enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, t: Throwable) {
                deferred.completeExceptionally(t)
            }

            override fun onResponse(call: Call<T>, response: Response<T>) {
                if (response.isSuccessful) {
                    deferred.complete(response.body()!!)
                } else {
                    deferred.completeExceptionally(HttpException(response))
                }
            }
        })

        return deferred
    }
}

private class DeferredApiResponseCallAdapter<T>(
        private val responseType: Type
) : CallAdapter<T, Deferred<ApiResponse<T>>> {

    override fun responseType() = responseType

    override fun adapt(call: Call<T>): Deferred<ApiResponse<T>> {
        val deferred = CompletableDeferred<ApiResponse<T>>()

        deferred.invokeOnCompletion {
            if (deferred.isCancelled) {
                call.cancel()
            }
        }

        call.enqueue(object : Callback<T> {
            override fun onFailure(call: Call<T>, t: Throwable) {
                deferred.complete(ApiResponse.create(t))
            }

            override fun onResponse(call: Call<T>, response: Response<T>) {
                deferred.complete(ApiResponse.create(response))
            }
        })

        return deferred
    }
}
