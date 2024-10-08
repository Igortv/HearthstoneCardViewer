package com.itolstoy.hearthstonecardviewer.domain.usecase

import com.itolstoy.hearthstonecardviewer.domain.repository.PreferencesRepository
import javax.inject.Inject

class SaveCardIdsUseCase @Inject constructor(
    private val repository: PreferencesRepository
) {
    suspend operator fun invoke(cardIds: List<String>) {
        repository.saveCardIds(cardIds)
    }
}