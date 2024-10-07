package com.itolstoy.hearthstonecardviewer.presentation.favourites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.itolstoy.hearthstonecardviewer.domain.Card
import com.itolstoy.hearthstonecardviewer.domain.CardRepository
import com.itolstoy.hearthstonecardviewer.domain.common.Resource
import com.itolstoy.hearthstonecardviewer.domain.usecase.GetFavouritesCardsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class CardFavouritesFragmentState {
    data class Success(val list: List<Card>) : CardFavouritesFragmentState()
    data class Error(val message: String) : CardFavouritesFragmentState()
    object Loading : CardFavouritesFragmentState()
}

@HiltViewModel
class CardFavouritesViewModel @Inject constructor(
    private val getCardsUseCase: GetFavouritesCardsUseCase,
    private val repository: CardRepository
) : ViewModel() {
    private val _stateFlow = MutableStateFlow<CardFavouritesFragmentState>(
        CardFavouritesFragmentState.Loading
    )
    val stateFlow: StateFlow<CardFavouritesFragmentState>
        get() = _stateFlow.asStateFlow()

    val cardsFlow: Flow<List<Card>> = repository.observeFavouritesCardFromDatabase()
    var cards: List<Card> = mutableListOf()

    fun getFavouritesCards() {
        viewModelScope.launch(Dispatchers.IO) {
            getCardsUseCase.invoke().collect { result ->
                _stateFlow.update {
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
    }
}