package com.itolstoy.hearthstonecardviewer.data.remote

import com.itolstoy.hearthstonecardviewer.data.remote.dto.CardDto
import retrofit2.http.GET

interface CardsApi {
    @GET("/cards")
    suspend fun getCards(): Map<String, List<CardDto>>
}