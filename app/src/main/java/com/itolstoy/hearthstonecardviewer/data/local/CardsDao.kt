package com.itolstoy.hearthstonecardviewer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.itolstoy.hearthstonecardviewer.data.local.dbo.CardDbo
import com.itolstoy.hearthstonecardviewer.data.local.dbo.FavouriteCardDbo
import kotlinx.coroutines.flow.Flow

@Dao
interface CardsDao {

    @Query("SELECT * FROM cards")
    suspend fun getAll(): List<CardDbo>

    @Query("SELECT cards.* FROM cards INNER JOIN favourites ON cards.cardId = favourites.cardId")
    fun observeFavouritesCardsFromDatabase(): Flow<List<CardDbo>>

    @Query("SELECT cards.* FROM cards INNER JOIN favourites ON cards.cardId = favourites.cardId")
    suspend fun getFavouritesCards(): List<CardDbo>

    @Query("SELECT * FROM cards WHERE cardId IN (:cardIds)")
    suspend fun getCardsByIds(cardIds: List<String>): List<CardDbo>

    @Insert
    suspend fun addToFavourites(favouriteCard: FavouriteCardDbo)

    @Query("DELETE FROM favourites WHERE cardId = :cardId")
    suspend fun removeFromFavourites(cardId: String)

    @Update
    suspend fun updateCard(card: CardDbo)

    @Query("SELECT * FROM cards WHERE cardId = :cardId")
    suspend fun getCardByCardId(cardId: String): CardDbo

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cards: List<CardDbo>)

    @Query("DELETE FROM cards")
    suspend fun clean()
}