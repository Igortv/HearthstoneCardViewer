package com.itolstoy.hearthstonecardviewer.presentation.cards

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.itolstoy.hearthstonecardviewer.databinding.FragmentCardlistBinding
import com.itolstoy.hearthstonecardviewer.presentation.adapter.CardAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CardListFragment : Fragment() {
    private var _binding: FragmentCardlistBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CardListViewModel by viewModels()
    private var currentAdapterPosition = 0
    private var tempAdapterPosition = 0
    private var isQueryInitialized = false
    private var flag = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCardlistBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cardAdapter = CardAdapter { position->
            val cardIds = viewModel.cards.map { it.cardId }
            val action = CardListFragmentDirections.actionNavigationCardsToCardFragment(cardIds.toTypedArray())
                .setCardPosition(position)
                .setIsFavourites(false)
            findNavController().navigate(action)
        }

        val filteredCardAdapter = CardAdapter { position->
            /*val filteredCardIds = viewModel.cards.map { it.cardId }
            val action = CardListFragmentDirections.actionNavigationCardsToCardFragment(filteredCardIds.toTypedArray())
                .setCardPosition(position)
                .setIsFavourites(false)
            findNavController().navigate(action)*/
        }

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = cardAdapter
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.stateFlow.collect { uiState ->
                    when (uiState) {
                        is CardListFragmentState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is CardListFragmentState.Success -> {
                            cardAdapter.setCards(uiState.list)
                            binding.recyclerView.adapter = cardAdapter
                            binding.recyclerView.scrollToPosition(currentAdapterPosition)
                            binding.progressBar.visibility = View.GONE
                        }
                        is CardListFragmentState.FilteredCards -> {
                            filteredCardAdapter.setCards(uiState.list)
                            binding.recyclerView.adapter = filteredCardAdapter
                        }
                        is CardListFragmentState.Error -> {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }

        val layoutManager =
            binding.recyclerView.layoutManager as LinearLayoutManager

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!isQueryInitialized) {
                    isQueryInitialized = true
                    return false
                }
                if (newText.isNullOrEmpty()) {
                    currentAdapterPosition = tempAdapterPosition
                    flag = true
                    binding.recyclerView.adapter = cardAdapter
                    binding.recyclerView.scrollToPosition(currentAdapterPosition)
                } else {
                    currentAdapterPosition = layoutManager.findFirstVisibleItemPosition()
                    if (flag) {
                        tempAdapterPosition = currentAdapterPosition
                        flag = false
                    }
                    viewModel.searchCards(newText)
                }
                return true
            }
        })

        setFragmentResultListener("card_slider_changes_key") { key, bundle ->
            val changed = bundle.getBoolean("changed", false)
            if (changed) {
                viewModel.getCards()
            }
        }
        setFragmentResultListener("favourite_screen_changes_key") { key, bundle ->
            val changed = bundle.getBoolean("changed", false)
            if (changed) {
                viewModel.getCards()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}