package com.itolstoy.hearthstonecardviewer.di

import android.content.Context
import androidx.room.Room
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.itolstoy.hearthstonecardviewer.data.local.CardsDao
import com.itolstoy.hearthstonecardviewer.data.local.CardsRoomDatabase
import com.itolstoy.hearthstonecardviewer.domain.common.Constants
import com.itolstoy.hearthstonecardviewer.data.remote.ApiKeyInterceptor
import com.itolstoy.hearthstonecardviewer.data.remote.CardsApi
import com.itolstoy.hearthstonecardviewer.data.repository.CardRepositoryImpl
import com.itolstoy.hearthstonecardviewer.domain.CardRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideBaseUrl(): String {
        return Constants.BASE_URL
    }

    @Provides
    fun provideGson(): Gson {
        return GsonBuilder()
            .create()
    }

    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .callTimeout(2, TimeUnit.MINUTES)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(
                provideAuthInterceptor()
            )
            .build()
    }

    @Provides
    fun provideAuthInterceptor(): ApiKeyInterceptor {
        return ApiKeyInterceptor(Constants.API_KEY)
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        baseUrl: String,
        gson: Gson,
        okHttpClient: OkHttpClient? = null
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): CardsApi =
        retrofit.create(CardsApi::class.java)

    @Provides
    @Singleton
    fun provideCardsDatabase(@ApplicationContext context: Context): CardsRoomDatabase {
        return Room.databaseBuilder(
            context,
            CardsRoomDatabase::class.java,
            "cards.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideDao(cardsRoomDatabase: CardsRoomDatabase): CardsDao {
        return cardsRoomDatabase.cardDao()
    }

    @Provides
    @Singleton
    fun provideCardRepository(
        cardsApi: CardsApi,
        cardsRoomDatabase: CardsRoomDatabase
    ): CardRepository {
        return CardRepositoryImpl(cardsApi, cardsRoomDatabase)
    }
}