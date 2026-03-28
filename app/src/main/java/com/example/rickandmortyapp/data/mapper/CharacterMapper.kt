package com.example.rickandmortyapp.data.mapper

import com.example.rickandmortyapp.data.local.entity.CharacterEntity
import com.example.rickandmortyapp.data.remote.dto.CharacterDto
import com.example.rickandmortyapp.domain.model.CharacterModel

fun CharacterDto.toEntity(): CharacterEntity {
    return CharacterEntity(
        id = id,
        name = name,
        status = status,
        species = species,
        gender = gender,
        image = image
    )
}

fun CharacterEntity.toDomain(): CharacterModel {
    return CharacterModel(
        id = id,
        name = name,
        status = status,
        species = species,
        gender = gender,
        image = image
    )
}