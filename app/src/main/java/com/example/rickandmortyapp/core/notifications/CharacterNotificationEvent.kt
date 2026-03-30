package com.example.rickandmortyapp.core.notifications

sealed class CharacterNotificationEvent {
    data object ShowSuccess : CharacterNotificationEvent()

    data object ShowError : CharacterNotificationEvent()
}
