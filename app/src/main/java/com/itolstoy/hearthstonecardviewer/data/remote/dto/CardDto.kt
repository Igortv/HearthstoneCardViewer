package com.itolstoy.hearthstonecardviewer.data.remote.dto

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class CardDto(
    @SerializedName("cardId") val cardId: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String?,
    @SerializedName("playerClass") val playerClass: String?,
    @SerializedName("text") val description: String?,
    @SerializedName("cost") val cost: Int?,
    @SerializedName("img") val imgUrl: String?
)