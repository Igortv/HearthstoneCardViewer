package com.itolstoy.hearthstonecardviewer.domain.repository

import com.itolstoy.hearthstonecardviewer.domain.Card
import kotlinx.coroutines.flow.Flow

interface CardRepository {
    suspend fun getCards(): List<Card>
    fun observeFavouritesCardFromDatabase(): Flow<List<Card>>
    suspend fun getCardsByIds(cardIds: List<String>): List<Card>
    suspend fun getFavouritesCard(): List<Card>
    suspend fun addCardToFavourites(card: Card)
    suspend fun removeCardFromFavourites(card: Card)
}