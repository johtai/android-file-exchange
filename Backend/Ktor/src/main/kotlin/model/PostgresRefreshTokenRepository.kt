package com.example.model

import com.example.db.RefreshTokenDAO
import com.example.db.RefreshTokenTable
import com.example.db.refreshDaoToModel
import com.example.db.suspendTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

class PostgresRefreshTokenRepository : RefreshTokenRepository  {

    override suspend fun allTokens(): List<RefreshTokenModel> = suspendTransaction{
        RefreshTokenDAO.all().map(::refreshDaoToModel)
    }

    override suspend fun removeRefreshToken(username: String): Boolean = suspendTransaction {
        val rd = RefreshTokenTable.deleteWhere {
            RefreshTokenTable.username eq username
        }
        rd == 1
    }

    override suspend fun findByToken(refreshToken: String): RefreshTokenModel? = suspendTransaction {
            RefreshTokenDAO.find { RefreshTokenTable.token eq refreshToken }
                .firstOrNull()
                ?.toModel()
    }

    override suspend fun findByName(name: String): RefreshTokenModel? =  suspendTransaction{
        RefreshTokenDAO.find{RefreshTokenTable.username eq name}.firstOrNull()?.toModel()

    }
}