package com.example.rickandmortyapp.data.remote

import com.example.rickandmortyapp.data.remote.dto.CharacterDto
import com.example.rickandmortyapp.data.remote.dto.CharacterResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RickAndMortyApiService {
    @GET("character")
    suspend fun getCharacters(@Query("page") page: Int): CharacterResponseDto

    @GET("character/{id}")
    suspend fun getCharacterById(@Path("id") id: Int): CharacterDto
}