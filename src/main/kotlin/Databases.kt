package com.example

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database


fun Application.configureDatabases() {
    Database.connect(
        "jdbc:postgresql://localhost:5432/postgres",
        user = "postgres",
        password = "postgres" //pass is incorrect
		
    )
}