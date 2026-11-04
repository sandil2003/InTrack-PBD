package com.example.intrack.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object CryptoRateApiService {

    private const val API_URL =
        "https://api.coingecko.com/api/v3/simple/price?ids=tether,ethereum,bitcoin,solana,binancecoin,cardano,ripple,dogecoin&vs_currencies=lkr"

    suspend fun fetchLiveRatesToLkr(): Result<Map<String, Double>> = withContext(Dispatchers.IO) {
        try {
            val url = URL(API_URL)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("User-Agent", "InTrack-Android/2.4")
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val jsonString = connection.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonString)

                val ratesMap = mutableMapOf<String, Double>()

                fun extractLkr(jsonId: String, symbol: String) {
                    if (root.has(jsonId)) {
                        val obj = root.getJSONObject(jsonId)
                        if (obj.has("lkr")) {
                            ratesMap[symbol] = obj.getDouble("lkr")
                        }
                    }
                }

                extractLkr("tether", "USDT")
                extractLkr("ethereum", "ETH")
                extractLkr("bitcoin", "BTC")
                extractLkr("solana", "SOL")
                extractLkr("binancecoin", "BNB")
                extractLkr("cardano", "ADA")
                extractLkr("ripple", "XRP")
                extractLkr("dogecoin", "DOGE")

                Result.success(ratesMap)
            } else {
                Result.failure(Exception("HTTP Error ${connection.responseCode}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
