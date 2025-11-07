package com.irc.darkcode

// ------------------- Data Classes -------------------
data class ServerConfig(
    var name: String,
    var host: String,
    var port: Int,
    var nick: String,
    var user: String = "guest",
    var realName: String = "Dark IRC User",
    var password: String? = null,
    var useSSL: Boolean = false
)

data class Channel(val name: String, val password: String? = null)
data class Message(val text: String, val from: String = "")