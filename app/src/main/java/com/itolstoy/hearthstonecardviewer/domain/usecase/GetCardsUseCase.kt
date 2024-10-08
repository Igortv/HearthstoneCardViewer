package com.itolstoy.hearthstonecardviewer.domain.usecase

import android.util.Log
import com.itolstoy.hearthstonecardviewer.domain.Card
import com.itolstoy.hearthstonecardviewer.domain.repository.CardRepository
import com.itolstoy.hearthstonecardviewer.domain.common.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetCardsUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    operator fun invoke(): Flow<Resource<List<Card>>> = flow {
        Log.d("GetCardsUseCase", "invoke() called")
        try {
            emit(Resource.Loading())
            val cards = cardRepository.getCards()
            emit(Resource.Success(cards))
        } catch (e: HttpException) {
            Log.d("GetCardsUseCase", "catch called")
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch (e: IOException) {
            Log.d("GetCardsUseCase", "catch 2 called")
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}