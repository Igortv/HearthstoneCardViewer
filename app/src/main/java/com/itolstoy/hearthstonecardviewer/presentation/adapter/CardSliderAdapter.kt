package com.itolstoy.hearthstonecardviewer.presentation.adapter

import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.itolstoy.hearthstonecardviewer.R
import com.itolstoy.hearthstonecardviewer.databinding.ViewPagerItemBinding
import com.itolstoy.hearthstonecardviewer.domain.Card

class CardSliderAdapter(
    val notifyDatasetChangedCallback: () -> Unit,
    val addToFavouritesCallback: (Card) -> Unit,
    val removeFromFavouritesCallback: (Card) -> Unit
) : RecyclerView.Adapter<CardSliderAdapter.CardViewHolder>() {
    private var cards = mutableListOf<Card>()

    fun setCards(cards: List<Card>) {
        this.cards = cards.toMutableList()
        notifyDataSetChanged()
        notifyDatasetChangedCallback()

    }

    inner class CardViewHolder(private var view: ViewPagerItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bindCard(card: Card) {
            view.cardImage.load(card.imgUrl) {
                crossfade(true)
                placeholder(R.drawable.image_placeholder_loading)
                fallback(R.drawable.image_placeholder)
                error(R.drawable.image_placeholder)
            }
            val processedText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(card.description.replace("\\n", "<br>"), Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(card.description.replace("\\n", "<br>"))
            }
            view.cardDescription.text = processedText.toString().replace("[x]","")

            if (card.favStatus) {
                view.cardFavImage.setImageResource(R.drawable.ic_favourite_checked)
            } else {
                view.cardFavImage.setImageResource(R.drawable.ic_favourite_unchecked)
            }
            view.cardFavImage.setOnClickListener {
                if (card.favStatus) {
                    card.favStatus = false
                    removeFromFavouritesCallback(card)
                    view.cardFavImage.setImageResource(R.drawable.ic_favourite_unchecked)
                } else {
                    card.favStatus = true
                    addToFavouritesCallback(card)
                    view.cardFavImage.setImageResource(R.drawable.ic_favourite_checked)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = ViewPagerItemBinding.inflate(from, parent, false)
        return CardViewHolder(binding)
    }

    override fun getItemCount(): Int = cards.size

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bindCard(cards[position])
    }
}