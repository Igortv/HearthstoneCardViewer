package com.itolstoy.hearthstonecardviewer.presentation.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itolstoy.hearthstonecardviewer.domain.Card
import com.itolstoy.hearthstonecardviewer.domain.usecase.GetCardsUseCase
import com.itolstoy.hearthstonecardviewer.domain.common.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CardListFragmentState {
    class Success(val list: List<Card>) : CardListFragmentState()
    class FilteredCards(val list: List<Card>) : CardListFragmentState()
    class Error(val message: String) : CardListFragmentState()
    object Loading : CardListFragmentState()
}

@HiltViewModel
class CardListViewModel @Inject constructor(
    private val getCardsUseCase: GetCardsUseCase
) : ViewModel() {
    private val _stateFlow = MutableStateFlow<CardListFragmentState>(CardListFragmentState.Loading)
    val stateFlow: StateFlow<CardListFragmentState>
        get() = _stateFlow.asStateFlow()

    var cards: List<Card> = mutableListOf()

    init {
        getCards()
    }

    fun searchCards(query: String) {
        viewModelScope.launch {
            val result = cards.filter { card ->
                card.name.contains(query, ignoreCase = true)
            }
            _stateFlow.update { CardListFragmentState.FilteredCards(result) }
        }
    }

    fun getCards() {
        viewModelScope.launch(Dispatchers.IO) {
            getCardsUseCase.invoke().collect { result ->
                _stateFlow.update {
                    when(result) {
                        is Resource.Loading -> CardListFragmentState.Loading
                        is Resource.Success -> {
                            cards = result.data!!
                            CardListFragmentState.Success(
                                result.data
                            )
                        }
                        is Resource.Error -> CardListFragmentState.Error(result.message ?: "")
                    }
                }
            }
        }
    }
}