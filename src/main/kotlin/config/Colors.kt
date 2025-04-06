package org.example.config

data class Colors (
    val error: String,
    val primary: String,
    val string: String,
    val comment: String,
    val themes: Map<String, Theme>
)