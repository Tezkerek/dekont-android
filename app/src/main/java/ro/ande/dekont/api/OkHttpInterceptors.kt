package ro.ande.dekont.api

import android.app.Application
import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import ro.ande.dekont.ui.AuthActivity

/**
 * Retrieves the auth token, if present, and adds it to the request's Authorization header.
 */
class AuthTokenInterceptor(val app: Application) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token =
            app.getSharedPreferences(AuthActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)
                .getString("token", null)

        val request =
            if (token != null) originalRequest.newBuilder()
                .addHeader("Authorization", "Token $token")
                .build()
            else originalRequest

        return chain.proceed(request)
    }
}

class LoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        Log.d(
            "LoggingInterceptor",
            "Sending request ${request.url()} on ${chain.connection()} with ${request.headers()}"
        )
        val response = chain.proceed(request)
        Log.d(
            "LoggingInterceptor",
            "Received response for ${response.request().url()} with ${response.headers()}"
        )
        return response
    }

}
