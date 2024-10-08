package com.itolstoy.hearthstonecardviewer.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.itolstoy.hearthstonecardviewer.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
): PreferencesRepository {

    companion object {
        val CARD_IDS_KEY = stringPreferencesKey("card_ids_key")
    }

    override suspend fun saveCardIds(cardIds: List<String>) {
        val cardIdsString = cardIds.joinToString(separator = ",")
        dataStore.edit { preferences ->
            preferences[CARD_IDS_KEY] = cardIdsString
        }
    }
    override suspend fun getCardIds(): Flow<List<String>> {
        return dataStore.data
            .map { preferences ->
                preferences[CARD_IDS_KEY]?.split(",") ?: emptyList()
            }
    }
}