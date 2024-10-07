package com.itolstoy.hearthstonecardviewer.domain

import kotlinx.coroutines.flow.Flow

interface CardRepository {
    suspend fun getCards(): List<Card>
    fun observeFavouritesCardFromDatabase(): Flow<List<Card>>
    suspend fun getCardsByIds(cardIds: List<String>): List<Card>
    suspend fun getCardsByIdsInBatches(cardIds: List<String>, batchSize: Int): List<Card>
    suspend fun getFavouritesCard(): List<Card>
    suspend fun addCardToFavourites(card: Card)
    suspend fun removeCardFromFavourites(card: Card)
}