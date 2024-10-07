package com.itolstoy.hearthstonecardviewer.data.local.dbo

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "favourites",
    foreignKeys = [ForeignKey(
        entity = CardDbo::class,
        parentColumns = ["cardId"],
        childColumns = ["cardId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class FavouriteCardDbo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardId: String
)