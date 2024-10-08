package com.itolstoy.hearthstonecardviewer.domain.usecase

import com.itolstoy.hearthstonecardviewer.domain.Card
import com.itolstoy.hearthstonecardviewer.domain.repository.CardRepository
import com.itolstoy.hearthstonecardviewer.domain.common.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetFavouritesCardsUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    operator fun invoke(): Flow<Resource<List<Card>>> = flow {
        try {
            emit(Resource.Loading())
            val cards = cardRepository.getFavouritesCard()
            emit(Resource.Success(cards))
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}