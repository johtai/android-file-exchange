package com.example.db

import com.example.model.User
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction


object UserTable : IntIdTable("user"){
    val username = varchar("username", 50).uniqueIndex()
    val password = varchar("password", 50)
}


class UserDAO(id: EntityID<Int>): IntEntity(id){
    companion object : IntEntityClass<UserDAO>(UserTable)

    var username by UserTable.username
    var password by UserTable.password
}

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T = newSuspendedTransaction(Dispatchers.IO, statement = block)

fun daoToModel(dao: UserDAO) = User(
    dao.username,
    dao.password,
)