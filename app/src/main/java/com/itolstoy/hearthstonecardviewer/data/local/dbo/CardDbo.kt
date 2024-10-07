package com.itolstoy.hearthstonecardviewer.data.local.dbo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cards")
data class CardDbo(
    @PrimaryKey val cardId: String,
    @ColumnInfo("name") val name: String,
    @ColumnInfo("type") val type: String?,
    @ColumnInfo("playerClass") val playerClass: String?,
    @ColumnInfo("text") val description: String?,
    @ColumnInfo("cost") val cost: String?,
    @ColumnInfo("img") val imgUrl: String?,
    @ColumnInfo("favStatus") val favStatus: Boolean = false
)