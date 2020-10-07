package ro.ande.dekont.di

import android.app.Application
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import org.threeten.bp.LocalDate
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ro.ande.dekont.api.AuthTokenInterceptor
import ro.ande.dekont.api.DekontService
import ro.ande.dekont.api.LoggingInterceptor
import ro.ande.dekont.util.*
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class ApiModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(app: Application): OkHttpClient =
            OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .addInterceptor(AuthTokenInterceptor(app))
                    .addInterceptor(LoggingInterceptor())
                    .build()

    @Provides
    @Singleton
    fun provideDekontService(client: OkHttpClient, gson: Gson): DekontService =
            Retrofit.Builder()
                    .baseUrl(DEKONT_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(LiveDataCallAdapterFactory())
                    .addCallAdapterFactory(ApiResponseCallAdapterFactory())
                    .addCallAdapterFactory(CoroutineCallAdapterFactory())
                    .build()
                    .create(DekontService::class.java)

    @Provides
    fun provideGson(): Gson =
            GsonBuilder()
                    .registerTypeAdapter(LocalDate::class.java, GsonLocalDateSerializer())
                    .registerTypeAdapter(LocalDate::class.java, GsonLocalDateDeserializer())
                    .registerTypeAdapter(Currency::class.java, GsonCurrencySerializer())
                    .registerTypeAdapter(Currency::class.java, GsonCurrencyDeserializer())
                    .create()

    companion object {
        const val DEKONT_BASE_URL = "http://192.168.1.102:8080"
    }
}