package com.itolstoy.hearthstonecardviewer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.itolstoy.hearthstonecardviewer.data.local.dbo.CardDbo
import com.itolstoy.hearthstonecardviewer.data.local.dbo.FavouriteCardDbo

@Database(entities = [CardDbo::class, FavouriteCardDbo::class], version = 1)
abstract class CardsRoomDatabase : RoomDatabase() {
    abstract fun cardDao(): CardsDao
}