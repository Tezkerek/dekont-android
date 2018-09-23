package ro.ande.dekont.di

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ro.ande.dekont.LoginActivity
import ro.ande.dekont.api.DekontService
import ro.ande.dekont.R
import ro.ande.dekont.util.LiveDataCallAdapterFactory
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(app: Application): OkHttpClient =
            OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .addInterceptor { chain ->
                        val token = app.getSharedPreferences(LoginActivity.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).getString("token", null)

                        val originalRequest = chain.request()

                        val request =
                                if (token != null) originalRequest.newBuilder()
                                        .addHeader("Authorization", "Token $token")
                                        .build()
                                else originalRequest

                        chain.proceed(request)
                    }
                    .addInterceptor { chain ->
                        val request = chain.request()

                        Log.d("LoggingInterceptor", "Sending request ${request.url()} on ${chain.connection()} with ${request.headers()}")

                        val response = chain.proceed(request)

                        Log.d("LoggingInterceptor", "Received response for ${response.request().url()} with ${response.headers()}")

                        response
                    }
                    .addInterceptor { chain ->
                        // Throw exception if internet is not available
                        val connectivityManager = app.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                        if (connectivityManager.activeNetworkInfo?.isConnected == false)
                            throw IOException(app.getString(R.string.error_missing_internet_connection))

                        chain.proceed(chain.request())
                    }
                    .addInterceptor { chain: Interceptor.Chain ->
                        val request = chain.request()
                        val response = chain.proceed(request)

                        if (response.code() == 500) {
                            Toast.makeText(app, R.string.error_internal_server_error, Toast.LENGTH_SHORT).show()
                        }

                        response
                    }
                    .build()

    @Provides
    @Singleton
    fun provideDekontService(client: OkHttpClient, liveDataCallAdapterFactory: LiveDataCallAdapterFactory, gsonConverterFactory: GsonConverterFactory): DekontService =
            Retrofit.Builder()
                    .baseUrl("http://192.168.0.53:8000")
                    .client(client)
                    .addConverterFactory(gsonConverterFactory)
                    .addCallAdapterFactory(liveDataCallAdapterFactory)
                    .build()
                    .create(DekontService::class.java)

    @Provides
    fun provideLiveDataCallAdapterFactory(): LiveDataCallAdapterFactory = LiveDataCallAdapterFactory()

    @Provides
    fun provideGsonConverterFactory(): GsonConverterFactory = GsonConverterFactory.create()
}
