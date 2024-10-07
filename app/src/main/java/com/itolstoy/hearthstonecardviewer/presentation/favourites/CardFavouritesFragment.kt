package com.itolstoy.hearthstonecardviewer.presentation.favourites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.itolstoy.hearthstonecardviewer.databinding.FragmentFavouritesBinding
import com.itolstoy.hearthstonecardviewer.presentation.adapter.CardAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CardFavouritesFragment : Fragment() {
    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CardFavouritesViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavouritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cardAdapter = CardAdapter { position->
            val cardIds = viewModel.cards.map { it.cardId }
            val action = CardFavouritesFragmentDirections.actionNavigationFavouritesToCardFragment(cardIds.toTypedArray())
                .setCardPosition(position)
                .setIsFavourites(true)
            findNavController().navigate(action)
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = cardAdapter
        }

        binding.swipeToRefresh.apply {
            setOnRefreshListener {
                viewModel.getFavouritesCards()
                isRefreshing = false
            }
        }
        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.cardsFlow.collect { cards ->
                    cardAdapter.setCards(cards)
                    setFragmentResult(
                        "favourite_screen_changes_key",
                        bundleOf("changed" to true)
                    )
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}