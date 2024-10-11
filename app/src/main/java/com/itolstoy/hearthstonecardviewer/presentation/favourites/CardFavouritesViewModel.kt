package com.itolstoy.hearthstonecardviewer.presentation.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itolstoy.hearthstonecardviewer.domain.Card
import com.itolstoy.hearthstonecardviewer.domain.repository.CardRepository
import com.itolstoy.hearthstonecardviewer.domain.common.Resource
import com.itolstoy.hearthstonecardviewer.domain.usecase.GetFavouritesCardsUseCase
import com.itolstoy.hearthstonecardviewer.domain.usecase.SaveCardIdsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CardFavouritesFragmentState {
    data object OK : CardFavouritesFragmentState()
    data class Success(val list: List<Card>) : CardFavouritesFragmentState()
    data class CardIdsSavedAndReadyToShowDetails(val cardPosition: Int) : CardFavouritesFragmentState()
    data class Error(val message: String) : CardFavouritesFragmentState()
    data object Loading : CardFavouritesFragmentState()
}

@HiltViewModel
class CardFavouritesViewModel @Inject constructor(
    private val getCardsUseCase: GetFavouritesCardsUseCase,
    private val saveCardIdsUseCase: SaveCardIdsUseCase,
    private val repository: CardRepository
) : ViewModel() {
    private val _stateFlow = MutableStateFlow<CardFavouritesFragmentState>(
        CardFavouritesFragmentState.OK
    )
    val stateFlow: StateFlow<CardFavouritesFragmentState>
        get() = _stateFlow.asStateFlow()

    var cards: List<Card> = listOf()

    val cardsFlow: Flow<List<Card>> = repository.observeFavouritesCardFromDatabase()

    fun getFavouritesCards() {
        viewModelScope.launch(Dispatchers.IO) {
            getCardsUseCase.invoke().collect { result ->
                _stateFlow.value =
                    when(result) {
                        is Resource.Loading -> CardFavouritesFragmentState.Loading
                        is Resource.Success -> {
                            cards = result.data!!
                            CardFavouritesFragmentState.Success(
                                result.data

                            )
                        }
                        is Resource.Error -> CardFavouritesFragmentState.Error(result.message ?: "")
                    }
            }
        }
    }

    fun saveCards(cards: List<Card>) {
        this.cards = cards
    }


    fun saveCardIds(cardIds: List<String>, cardPosition: Int) {
        viewModelScope.launch {
            _stateFlow.value = CardFavouritesFragmentState.Loading
            saveCardIdsUseCase.invoke(cardIds)
            _stateFlow.value = CardFavouritesFragmentState.CardIdsSavedAndReadyToShowDetails(cardPosition)
        }
    }
}