package com.itolstoy.hearthstonecardviewer.domain

data class Card(
    val cardId: String,
    val name: String,
    val type: String,
    val playerClass: String,
    val description: String,
    val cost: String,
    val imgUrl: String,
    var favStatus: Boolean = false
)
