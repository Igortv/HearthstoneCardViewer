package com.itolstoy.hearthstonecardviewer.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itolstoy.hearthstonecardviewer.domain.Card
import com.itolstoy.hearthstonecardviewer.domain.common.Resource
import com.itolstoy.hearthstonecardviewer.domain.usecase.AddCardToFavouritesUseCase
import com.itolstoy.hearthstonecardviewer.domain.usecase.GetCardIdsUseCase
import com.itolstoy.hearthstonecardviewer.domain.usecase.GetCardsByIdsUseCase
import com.itolstoy.hearthstonecardviewer.domain.usecase.RemoveCardFromFavouritesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CardFragmentState {
    data class Success(val list: List<Card>) : CardFragmentState()
    data class Error(val message: String) : CardFragmentState()
    data object Loading : CardFragmentState()
}

@HiltViewModel
class CardFragmentViewModel @Inject constructor(
    private val addCardToFavouritesUseCase: AddCardToFavouritesUseCase,
    private val removeCardFromFavouritesUseCase: RemoveCardFromFavouritesUseCase,
    private val getCardsByIdsUseCase: GetCardsByIdsUseCase,
    private val getCardIdsUseCase: GetCardIdsUseCase
) : ViewModel() {
    private val _stateFlow = MutableStateFlow<CardFragmentState>(CardFragmentState.Loading)
    val stateFlow: StateFlow<CardFragmentState>
        get() = _stateFlow.asStateFlow()

    private val _cardIdsFlow = MutableStateFlow<List<String>>(emptyList())
    val cardIdsFlow: StateFlow<List<String>> get() = _cardIdsFlow

    fun getCardIds() {
        viewModelScope.launch {
            getCardIdsUseCase.invoke().collect { cardIds ->
                _cardIdsFlow.value = cardIds
            }
        }
    }

    fun getCardsByIds(cardIds: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            getCardsByIdsUseCase.invoke(cardIds).collect { result ->
                _stateFlow.value =
                    when(result) {
                        is Resource.Loading -> CardFragmentState.Loading
                        is Resource.Success -> CardFragmentState.Success(result.data ?: emptyList())
                        is Resource.Error -> CardFragmentState.Error(result.message ?: "")
                    }
            }
        }
    }

    fun addCardToFavourites(card: Card) {
        viewModelScope.launch(Dispatchers.IO) {
            addCardToFavouritesUseCase.invoke(card).collect()
        }
    }

    fun removeCardFromFavourites(card: Card) {
        viewModelScope.launch(Dispatchers.IO) {
            removeCardFromFavouritesUseCase.invoke(card).collect()
        }
    }
}