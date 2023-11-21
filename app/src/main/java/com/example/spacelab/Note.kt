package com.example.spacelab

data class Note(
    val title: String = "",
    val content: String = "",
    val color: String = "",
    val tag: String = "" // Add this line for the tag property
)
