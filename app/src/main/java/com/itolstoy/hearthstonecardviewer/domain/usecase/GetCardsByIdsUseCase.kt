package com.itolstoy.hearthstonecardviewer.domain.usecase

import com.itolstoy.hearthstonecardviewer.domain.Card
import com.itolstoy.hearthstonecardviewer.domain.CardRepository
import com.itolstoy.hearthstonecardviewer.domain.common.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class GetCardsByIdsUseCase @Inject constructor(
    private val cardRepository: CardRepository
) {
    operator fun invoke(cardIds: List<String>): Flow<Resource<List<Card>>> = flow {
        try {
            emit(Resource.Loading())
            val batchSize = 1000
            val cards = cardRepository.getCardsByIdsInBatches(cardIds, batchSize)
            emit(Resource.Success(cards))
        } catch (e: HttpException) {
            emit(Resource.Error(e.localizedMessage ?: "An unexpected error occured"))
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't reach server. Check your internet connection."))
        }
    }
}