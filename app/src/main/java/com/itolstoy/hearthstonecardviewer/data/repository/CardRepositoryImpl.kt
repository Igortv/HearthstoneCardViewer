package com.itolstoy.hearthstonecardviewer.data.repository

import com.itolstoy.hearthstonecardviewer.data.local.CardsRoomDatabase
import com.itolstoy.hearthstonecardviewer.data.remote.dto.CardDto
import com.itolstoy.hearthstonecardviewer.data.remote.CardsApi
import com.itolstoy.hearthstonecardviewer.data.toCard
import com.itolstoy.hearthstonecardviewer.data.toCardDbo
import com.itolstoy.hearthstonecardviewer.data.toDbo
import com.itolstoy.hearthstonecardviewer.data.toFavouriteDbo
import com.itolstoy.hearthstonecardviewer.domain.Card
import com.itolstoy.hearthstonecardviewer.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CardRepositoryImpl @Inject constructor(
    private val api: CardsApi,
    private val cardsRoomDatabase: CardsRoomDatabase
) : CardRepository {
    override suspend fun getCards(): List<Card> {
        val cachedCards = getAllFromDatabase()
        if (cachedCards.isEmpty()) {
            val remoteCards = api.getCards()
            val cards = remoteCards.values.toList().flatten()
            saveCardsToCache(cards)
            return cards.map { it.toCard() }
        } else {
            return cachedCards
        }

    }

    override suspend fun getCardsByIds(cardIds: List<String>): List<Card> {
        val cards = cardsRoomDatabase.cardDao().getCardsByIds(cardIds)
        val cardMap = cards.associateBy { it.cardId }
        val sortedCardsByIdsOrder = cardIds.mapNotNull { cardMap[it] }

        return sortedCardsByIdsOrder.map { it.toCard() }
    }

    override fun observeFavouritesCardFromDatabase(): Flow<List<Card>> {
        return cardsRoomDatabase.cardDao().observeFavouritesCardsFromDatabase().map { list -> list.map { it.toCard() } }
    }

    override suspend fun getFavouritesCard(): List<Card> {
        val favourites = getFavouritesCardsFromDatabase()
        return favourites
    }

    override suspend fun addCardToFavourites(card: Card) {
        val addFavouriteDbo = card.toFavouriteDbo()
        val cardDbo = card.toDbo()
        cardsRoomDatabase.cardDao().addToFavourites(addFavouriteDbo)
        cardsRoomDatabase.cardDao().updateCard(cardDbo)
    }

    override suspend fun removeCardFromFavourites(card: Card) {
        val cardDbo = card.toDbo()
        cardsRoomDatabase.cardDao().removeFromFavourites(card.cardId)
        cardsRoomDatabase.cardDao().updateCard(cardDbo)
    }

    private suspend fun saveCardsToCache(data: List<CardDto>) {
        val saveCardsDbo = data.map { cardDto -> cardDto.toCardDbo() }
        cardsRoomDatabase.cardDao().insert(saveCardsDbo)
    }

    private suspend fun getFavouritesCardsFromDatabase(): List<Card> {
        val favCardsDbos = cardsRoomDatabase.cardDao().getFavouritesCards()
        return favCardsDbos.map { it.toCard() }
    }

    private suspend fun getAllFromDatabase(): List<Card> {
        val cardsDbos = cardsRoomDatabase.cardDao().getAll()
        return cardsDbos.map { it.toCard() }
    }
}