package com.itolstoy.hearthstonecardviewer.domain.repository

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {
    suspend fun saveCardIds(cardIds: List<String>)
    suspend fun getCardIds(): Flow<List<String>>
}