package com.itolstoy.hearthstonecardviewer.presentation.adapter

import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.itolstoy.hearthstonecardviewer.R
import com.itolstoy.hearthstonecardviewer.databinding.CardItemBinding
import com.itolstoy.hearthstonecardviewer.domain.Card

class CardAdapter(
    val onItemClickListener: (Int) -> Unit
): RecyclerView.Adapter<CardAdapter.CardViewHolder>(){
    private var cards = mutableListOf<Card>()

    fun setCards(cards: List<Card>) {
        this.cards = cards.toMutableList()
        notifyDataSetChanged()
        //differ.submitList(cards)
    }

    inner class CardViewHolder(private var view: CardItemBinding) : RecyclerView.ViewHolder(view.root) {
        fun bindCard(card: Card) {
            view.cardName.text = card.name
            view.cardImage.load(card.imgUrl) {
                crossfade(true)
                placeholder(R.drawable.image_placeholder_loading)
                fallback(R.drawable.image_placeholder)
                error(R.drawable.image_placeholder)
            }
            view.cardPlayerClass.text = card.playerClass

            val processedText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(card.description.replace("\\n", "<br>"), Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(card.description.replace("\\n", "<br>"))
            }
            view.cardDescription.text = processedText.toString().replace("[x]","")

            view.cardCost.text = card.cost
            if (card.favStatus) {
                view.cardFavImage.setImageResource(R.drawable.ic_favourite_checked)
            } else {
                view.cardFavImage.setImageResource(R.drawable.ic_favourite_unchecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val from = LayoutInflater.from(parent.context)
        val binding = CardItemBinding.inflate(from, parent, false)
        return CardViewHolder(binding)
    }

    override fun getItemCount(): Int = cards.size//differ.currentList.size

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        //holder.bindCard(differ.currentList[position])
        holder.bindCard(cards[position])
        holder.setIsRecyclable(false)
        holder.itemView.setOnClickListener {
            onItemClickListener(position)
        }
    }

    /*private val differCallback = object : DiffUtil.ItemCallback<Card>() {
        override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem.cardId == newItem.cardId
        }

        override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem == newItem
        }
    }

    val differ = AsyncListDiffer(this, differCallback)*/
}