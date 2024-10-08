package com.itolstoy.hearthstonecardviewer.domain.usecase

import com.itolstoy.hearthstonecardviewer.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCardIdsUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    suspend operator fun invoke(): Flow<List<String>> {
        return repository.getCardIds()
    }
}