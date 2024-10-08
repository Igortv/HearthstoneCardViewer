package com.itolstoy.hearthstonecardviewer.presentation.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.navArgs
import com.itolstoy.hearthstonecardviewer.MainActivity
import com.itolstoy.hearthstonecardviewer.databinding.FragmentCardBinding
import com.itolstoy.hearthstonecardviewer.presentation.adapter.CardSliderAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CardFragment : Fragment() {
    private var _binding: FragmentCardBinding? = null
    private val binding get() = _binding!!
    val args: CardFragmentArgs by navArgs()
    private val viewModel: CardFragmentViewModel by viewModels()

    companion object {
        const val POSITION_ARG = "cardPosition"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val cardPosition = arguments?.getInt(POSITION_ARG) ?: 0

        val cardSliderAdapter = CardSliderAdapter (
            notifyDatasetChangedCallback = {
                binding.viewPager.setCurrentItem(cardPosition, false)
            },
            addToFavouritesCallback = { card ->
                viewModel.addCardToFavourites(card)
                setFragmentResult("card_slider_changes_key", bundleOf("changed" to true))
            },
            removeFromFavouritesCallback = { card ->
                viewModel.removeCardFromFavourites(card)
                setFragmentResult("card_slider_changes_key", bundleOf("changed" to true))
            }
        )
        binding.viewPager.adapter = cardSliderAdapter

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.CREATED) {
                viewModel.stateFlow.collect { uiState ->
                    when (uiState) {
                        is CardFragmentState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is CardFragmentState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            val result = uiState.list
                            cardSliderAdapter.setCards(result)
                        }
                        is CardFragmentState.Error -> {
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }

        viewModel.getCardIds()

        lifecycleScope.launch {
            viewModel.cardIdsFlow.collect { cardIds ->
                if (cardIds.isNotEmpty()) {
                    viewModel.getCardsByIds(cardIds)
                }
            }
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackButtonPressed()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleBackButtonPressed() {
        if (requireActivity().supportFragmentManager.backStackEntryCount > 0) {
            requireActivity().supportFragmentManager.popBackStack()
            (requireActivity() as MainActivity).binding.navView.visibility = View.VISIBLE
        } else {
            requireActivity().finish()
        }
    }
}