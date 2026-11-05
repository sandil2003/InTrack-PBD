package com.example.intrack.data.model

import com.google.firebase.firestore.PropertyName

data class CryptoHoldingEntity(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("userId") @set:PropertyName("userId") var userId: String = "",
    @get:PropertyName("coinSymbol") @set:PropertyName("coinSymbol") var coinSymbol: String = "ETH",
    @get:PropertyName("coinName") @set:PropertyName("coinName") var coinName: String = "Ethereum",
    @get:PropertyName("coinQuantity") @set:PropertyName("coinQuantity") var coinQuantity: Double = 0.0,
    @get:PropertyName("investedLkr") @set:PropertyName("investedLkr") var investedLkr: Double = 0.0,
    @get:PropertyName("avgBuyPriceLkr") @set:PropertyName("avgBuyPriceLkr") var avgBuyPriceLkr: Double = 0.0,
    @get:PropertyName("lastUpdatedMillis") @set:PropertyName("lastUpdatedMillis") var lastUpdatedMillis: Long = System.currentTimeMillis()
)
