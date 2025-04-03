package com.example.db

import com.example.model.RefreshTokenModel
import com.example.model.User
//import com.sun.org.apache.xalan.internal.lib.ExsltDatetime.date
//import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDate
import org.jetbrains.exposed.sql.javatime.date
import java.time.ZoneId


import java.util.Date
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

//

object RefreshTokenTable : IntIdTable("refresh_token"){
    val username = varchar("username", 50).uniqueIndex()
    val token = varchar("token", 500)
    val expiresAt = date("expires_at")
}

class RefreshTokenDAO(id: EntityID<Int>): IntEntity(id){
    companion object :  IntEntityClass<RefreshTokenDAO>(RefreshTokenTable)

    var username by RefreshTokenTable.username
    var token by RefreshTokenTable.token
    var expiresAt by RefreshTokenTable.expiresAt

    fun toModel(): RefreshTokenModel = RefreshTokenModel(
        token = token,
        username = username,
        expiresAt = expiresAt
    )
}


fun refreshDaoToModel(dao: RefreshTokenDAO) = RefreshTokenModel(
    dao.token,
    dao.username,
    dao.expiresAt
)

// Конвертация при необходимости
fun Date.toLocalDate(): LocalDate = this.toInstant()
    .atZone(ZoneId.systemDefault())
    .toLocalDate()

fun LocalDate.toDate(): Date = Date.from(this.atStartOfDay()
    .atZone(ZoneId.systemDefault())
    .toInstant())
