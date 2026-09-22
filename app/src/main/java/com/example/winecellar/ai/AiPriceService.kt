package com.example.winecellar.ai

interface AiPriceService {
    fun estimatePrices(request: AiPriceRequest): AiPriceResponse
}