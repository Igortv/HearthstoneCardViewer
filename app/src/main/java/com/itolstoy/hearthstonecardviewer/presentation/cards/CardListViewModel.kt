package com.itolstoy.hearthstonecardviewer.presentation.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itolstoy.hearthstonecardviewer.domain.Card
import com.itolstoy.hearthstonecardviewer.domain.usecase.GetCardsUseCase
import com.itolstoy.hearthstonecardviewer.domain.common.Resource
import com.itolstoy.hearthstonecardviewer.domain.usecase.GetCardIdsUseCase
import com.itolstoy.hearthstonecardviewer.domain.usecase.GetCardsByIdsUseCase
import com.itolstoy.hearthstonecardviewer.domain.usecase.SaveCardIdsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CardListFragmentState {
    data object OK : CardListFragmentState()
    data class Success(val list: List<Card>) : CardListFragmentState()
    data class FilteredCards(val list: List<Card>) : CardListFragmentState()
    data class AddedFromSearchList(val list: List<Card>) : CardListFragmentState()
    data class Error(val message: String) : CardListFragmentState()
    data object Loading : CardListFragmentState()
}

enum class SortOrder {
    ASCENDING, DESCENDING
}

enum class SortType {
    CLASS, COST
}

@HiltViewModel
class CardListViewModel @Inject constructor(
    private val getCardsUseCase: GetCardsUseCase,
    private val saveCardIdsUseCase: SaveCardIdsUseCase
) : ViewModel() {
    private val _stateFlow = MutableStateFlow<CardListFragmentState>(CardListFragmentState.OK)
    val stateFlow: StateFlow<CardListFragmentState>
        get() = _stateFlow.asStateFlow()

    private var sortOrder: SortOrder = SortOrder.ASCENDING
    private var sortType: SortType = SortType.CLASS

    private val _cardIdsSavedState = MutableStateFlow(false)
    val cardIdsSavedState: StateFlow<Boolean> get() = _cardIdsSavedState

    private val _cardsFlow = MutableStateFlow<List<Card>>(emptyList())
    val cardsFlow: StateFlow<List<Card>> get() = _cardsFlow

    var cards: List<Card> = mutableListOf()
    var filteredCards: List<Card> = mutableListOf()

    init {
        getCards()
    }

    fun searchCards(query: String) {
        viewModelScope.launch {
            val result = cards.filter { card ->
                card.name.contains(query, ignoreCase = true)
            }
            filteredCards = result
            _stateFlow.update { CardListFragmentState.FilteredCards(result) }
        }
    }

    fun getCards() {
        viewModelScope.launch(Dispatchers.IO) {
            getCardsUseCase.invoke()
                .collect { result ->
                _stateFlow.update {
                    when(result) {
                        is Resource.Loading -> CardListFragmentState.Loading
                        is Resource.Success -> {
                            val sortedCards = when(sortType) {
                                SortType.CLASS -> {
                                    if (sortOrder == SortOrder.ASCENDING) {
                                        result.data!!.sortedBy { it.playerClass }
                                    } else {
                                        result.data!!.sortedByDescending { it.playerClass }
                                    }
                                }
                                SortType.COST -> {
                                    if (sortOrder == SortOrder.ASCENDING) {
                                        result.data!!.sortedBy { it.cost }
                                    } else {
                                        result.data!!.sortedByDescending { it.cost }
                                    }
                                }
                            }
                            cards = sortedCards
                            _cardsFlow.value = sortedCards

                            CardListFragmentState.Success(
                                sortedCards
                            )
                        }
                        is Resource.Error -> CardListFragmentState.Error(result.message ?: "")
                    }
                }
            }
        }
    }

    fun setSortOrder(order: SortOrder) {
        sortOrder = order
    }

    fun sortCardsByClass() {
        viewModelScope.launch {
            val sortedCards = if (sortOrder == SortOrder.ASCENDING) {
                _cardsFlow.value.sortedBy { it.playerClass }
            } else {
                _cardsFlow.value.sortedByDescending { it.playerClass }
            }
            cards = sortedCards
            _cardsFlow.emit(sortedCards)
        }
    }

    fun sortCardsByCost() {
        viewModelScope.launch {
            val sortedCards = if (sortOrder == SortOrder.ASCENDING) {
                _cardsFlow.value.sortedBy { it.cost }
            } else {
                _cardsFlow.value.sortedByDescending { it.cost }
            }
            cards = sortedCards
            _cardsFlow.emit(sortedCards)
        }
    }

    fun updateFilteredList() {
        viewModelScope.launch(Dispatchers.IO) {
            getCardsUseCase.invoke()
                .collect { result ->
                    _stateFlow.update {
                        when(result) {
                            is Resource.Loading -> CardListFragmentState.Loading
                            is Resource.Success -> {
                                val cardsMap: MutableMap<String, Card> = result.data!!.associateBy { it.cardId }.toMutableMap()
                                val filteredCardsMap: Map<String, Card> = filteredCards.associateBy { it.cardId }

                                filteredCardsMap.forEach { (key, value) ->
                                    value.favStatus = cardsMap[key]!!.favStatus
                                }
                                filteredCards = filteredCardsMap.values.toList()
                                CardListFragmentState.AddedFromSearchList(
                                    filteredCards
                                )
                            }
                            is Resource.Error -> CardListFragmentState.Error(result.message ?: "")
                        }
                    }
                }
        }
    }

    fun saveCardIds(cardIds: List<String>) {
        viewModelScope.launch {
            saveCardIdsUseCase(cardIds)
            _cardIdsSavedState.value = true
        }
    }
}