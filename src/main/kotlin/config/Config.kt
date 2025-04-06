package org.example.config

data class Config(
    val command: Command,
    val errors: Errors,
    val keywordsJson: String,
    val errorPrefix: String,
    val colors: Colors
)
