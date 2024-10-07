package com.itolstoy.hearthstonecardviewer.data

import com.itolstoy.hearthstonecardviewer.data.local.dbo.CardDbo
import com.itolstoy.hearthstonecardviewer.data.local.dbo.FavouriteCardDbo
import com.itolstoy.hearthstonecardviewer.data.remote.dto.CardDto
import com.itolstoy.hearthstonecardviewer.domain.Card

fun CardDto.toCard(): Card {
    return Card(
        cardId = cardId,
        name = name,
        type = type ?: "",
        playerClass = playerClass ?: "",
        description = description ?: "",
        cost = cost?.toString() ?: "",
        imgUrl = imgUrl ?: ""
    )
}

fun CardDbo.toCard(): Card {
    return Card(
        cardId = cardId,
        name = name,
        type = type  ?: "",
        playerClass = playerClass ?: "",
        description = description ?: "",
        cost = cost ?: "",
        imgUrl = imgUrl ?: "",
        favStatus = favStatus
    )
}

fun CardDto.toCardDbo(): CardDbo {
    return CardDbo(
        cardId = cardId,
        name = name,
        type = type  ?: "",
        playerClass = playerClass  ?: "",
        description = description  ?: "",
        cost = cost?.toString() ?: "",
        imgUrl = imgUrl  ?: ""
    )
}

fun Card.toDbo (): CardDbo {
    return CardDbo(
        cardId = cardId,
        name = name,
        type = type,
        playerClass = playerClass,
        description = description,
        cost = cost,
        imgUrl = imgUrl,
        favStatus = favStatus
    )
}

fun Card.toFavouriteDbo(): FavouriteCardDbo {
    return FavouriteCardDbo(
        cardId = cardId
    )
}