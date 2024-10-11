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
import androidx.recyclerview.widget.GridLayoutManager
import com.itolstoy.hearthstonecardviewer.MainActivity
import com.itolstoy.hearthstonecardviewer.R
import com.itolstoy.hearthstonecardviewer.databinding.FragmentFavouritesBinding
import com.itolstoy.hearthstonecardviewer.presentation.adapter.CardAdapter
import com.itolstoy.hearthstonecardviewer.presentation.details.CardFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CardFavouritesFragment : Fragment() {
    private var _binding: FragmentFavouritesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CardFavouritesViewModel by viewModels()
    private lateinit var cardAdapter: CardAdapter

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
        cardAdapter = CardAdapter { position->
            val cardIds = cardAdapter.getCards().map { it.cardId }
            viewModel.saveCardIds(cardIds)
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.cardIdsSavedState.collect { isSaved ->
                    if (isSaved) {
                        viewModel.resetCardIdsSavedState()
                        val cardFragment = CardFragment().apply {
                            arguments = bundleOf(CardFragment.POSITION_ARG to position)
                        }
                        requireActivity().supportFragmentManager.beginTransaction()
                            .add(R.id.nav_host_fragment_activity_main, cardFragment)
                            .addToBackStack(null)
                            .commit()
                        (requireActivity() as MainActivity).binding.navView.visibility = View.GONE
                    }
                }
            }

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
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateFlow.collect { uiState ->
                    when (uiState) {
                        is CardFavouritesFragmentState.OK -> {
                            binding.progressBar.visibility = View.GONE
                        }
                        is CardFavouritesFragmentState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is CardFavouritesFragmentState.Success -> {
                            binding.progressBar.visibility = View.GONE
                        }
                        is CardFavouritesFragmentState.Error -> {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.cardsFlow.distinctUntilChanged().collect { cards ->
                    cardAdapter.setCards(cards)
                    viewModel.saveCards(cards)
                    setFragmentResult("card_favourites_key", bundleOf("changed" to true))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}