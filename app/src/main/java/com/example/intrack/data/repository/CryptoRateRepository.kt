package com.example.intrack.data.repository

import com.example.intrack.data.remote.CryptoRateApiService
import javax.inject.Inject
import javax.inject.Singleton

interface CryptoRateRepository {
    suspend fun getLiveRatesToLkr(): Map<String, Double>
    fun getFallbackRates(): Map<String, Double>
}

@Singleton
class CryptoRateRepositoryImpl @Inject constructor() : CryptoRateRepository {

    private var cachedRates: MutableMap<String, Double> = mutableMapOf()

    override fun getFallbackRates(): Map<String, Double> {
        return mapOf(
            "USDT" to 332.40,
            "ETH" to 765516.00,
            "BTC" to 26450000.00,
            "SOL" to 51800.00,
            "BNB" to 186200.00,
            "ADA" to 142.50,
            "XRP" to 182.00,
            "DOGE" to 48.20
        )
    }

    override suspend fun getLiveRatesToLkr(): Map<String, Double> {
        val result = CryptoRateApiService.fetchLiveRatesToLkr()
        val rates = if (result.isSuccess) {
            val live = result.getOrNull() ?: emptyMap()
            cachedRates.putAll(live)
            cachedRates
        } else {
            if (cachedRates.isNotEmpty()) {
                cachedRates
            } else {
                getFallbackRates()
            }
        }

        val merged = getFallbackRates().toMutableMap()
        merged.putAll(rates)
        return merged
    }
}
