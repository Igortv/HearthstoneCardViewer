package com.itolstoy.hearthstonecardviewer.domain.usecase

import com.itolstoy.hearthstonecardviewer.domain.Card
import com.itolstoy.hearthstonecardviewer.domain.repository.CardRepository
import com.itolstoy.hearthstonecardviewer.domain.common.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class RemoveCardFromFavouritesUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    operator fun invoke(card: Card): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())
            cardRepository.removeCardFromFavourites(card)
            emit(Resource.Success(Unit))
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}