package com.palmar.kurirapp.data.retrofit

import com.palmar.kurirapp.data.LocationSuggestion
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimApiService {
    @GET("search")
    suspend fun searchLocations(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("countrycodes") countryCode: String = "id"
    ): List<LocationSuggestion>
}