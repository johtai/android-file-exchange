package com.example.model

interface UserRepository {
    suspend fun allUsers(): List<User>
    suspend fun userByName(username: String): User?
    suspend fun addUser(user: User)
    suspend fun removeUser(username: String): Boolean
}