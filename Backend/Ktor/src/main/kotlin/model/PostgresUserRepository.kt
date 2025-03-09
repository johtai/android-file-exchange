package com.example.model

import com.example.db.UserDAO
import com.example.db.UserTable
import com.example.db.daoToModel
import com.example.db.suspendTransaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

class PostgresUserRepository : UserRepository{
    override suspend fun allUsers(): List<User> = suspendTransaction {
        UserDAO.all().map(::daoToModel)
    }


    override suspend fun userByName(username: String): User? = suspendTransaction {
        UserDAO
            .find{ (UserTable.username eq username)}
            .limit(1)
            .map(::daoToModel)
            .firstOrNull()
    }

    override suspend fun addUser(user: User): Unit = suspendTransaction {
        UserDAO.new{
            username = user.username
            password = user.password
        }
    }

    override suspend fun removeUser(username: String): Boolean = suspendTransaction {
        val rowsDeleted = UserTable.deleteWhere {
            UserTable.username eq username
        }
        rowsDeleted == 1
    }


}