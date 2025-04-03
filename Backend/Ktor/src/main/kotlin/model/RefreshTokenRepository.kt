package com.example.model

interface RefreshTokenRepository {
    suspend fun allTokens(): List<RefreshTokenModel>
    suspend fun findByToken(refreshToken: String): RefreshTokenModel?
    //suspend fun tokenByUsername(username: String): refresh_token?
    //suspend fun addRefreshToken(user: refresh_token)
    suspend fun findByName(name: String): RefreshTokenModel?
    suspend fun removeRefreshToken(username: String): Boolean


}