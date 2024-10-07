package com.itolstoy.hearthstonecardviewer.presentation.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itolstoy.hearthstonecardviewer.domain.Card
import com.itolstoy.hearthstonecardviewer.domain.usecase.GetCardsUseCase
import com.itolstoy.hearthstonecardviewer.domain.common.Resource
import com.itolstoy.hearthstonecardviewer.domain.usecase.AddCardToFavouritesUseCase
import com.itolstoy.hearthstonecardviewer.domain.usecase.GetCardsByIdsUseCase
import com.itolstoy.hearthstonecardviewer.domain.usecase.GetFavouritesCardsUseCase
import com.itolstoy.hearthstonecardviewer.domain.usecase.RemoveCardFromFavouritesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CardFragmentState {
    data class Success(val list: List<Card>) : CardFragmentState()
    data class Error(val message: String) : CardFragmentState()
    object Loading : CardFragmentState()
}

@HiltViewModel
class CardFragmentViewModel @Inject constructor(
    private val getCardsUseCase: GetCardsUseCase,
    private val getFavouritesCardsUseCase: GetFavouritesCardsUseCase,
    private val addCardToFavouritesUseCase: AddCardToFavouritesUseCase,
    private val removeCardFromFavouritesUseCase: RemoveCardFromFavouritesUseCase,
    private val getCardsByIdsUseCase: GetCardsByIdsUseCase
) : ViewModel() {
    private val _stateFlow = MutableStateFlow<CardFragmentState>(CardFragmentState.Loading)
    val stateFlow: StateFlow<CardFragmentState>
        get() = _stateFlow.asStateFlow()

    fun getAllCards() {
        viewModelScope.launch(Dispatchers.IO) {
            getCardsUseCase.invoke().collect { result ->
                _stateFlow.update {
                    when(result) {
                        is Resource.Loading -> CardFragmentState.Loading
                        is Resource.Success -> CardFragmentState.Success(result.data ?: emptyList())
                        is Resource.Error -> CardFragmentState.Error(result.message ?: "")
                    }
                }
            }
        }
    }

    fun getCardsByIds(cardIds: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            getCardsByIdsUseCase.invoke(cardIds).collect { result ->
                _stateFlow.update {
                    when(result) {
                        is Resource.Loading -> CardFragmentState.Loading
                        is Resource.Success -> CardFragmentState.Success(result.data ?: emptyList())
                        is Resource.Error -> CardFragmentState.Error(result.message ?: "")
                    }
                }
            }
        }
    }

    fun getFavouritesCards() {
        viewModelScope.launch(Dispatchers.IO) {
            getFavouritesCardsUseCase.invoke().collect { result ->
                _stateFlow.update {
                    when(result) {
                        is Resource.Loading -> CardFragmentState.Loading
                        is Resource.Success -> CardFragmentState.Success(result.data ?: emptyList())
                        is Resource.Error -> CardFragmentState.Error(result.message ?: "")
                    }
                }
            }
        }
    }

    fun getCardsByCardIds(cardIds: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            getCardsByIdsUseCase.invoke(cardIds).collect()
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