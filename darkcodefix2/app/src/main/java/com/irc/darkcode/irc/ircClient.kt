package com.irc.darkcode

import kotlinx.coroutines.*
import androidx.compose.runtime.*
import java.io.*
import java.net.Socket
import javax.net.ssl.SSLSocketFactory

class IRCClient(private val server: ServerConfig) {

    private var socket: Socket? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null
    var onMessage: ((Message) -> Unit)? = null
    var isConnected by mutableStateOf(false)
    var channels = mutableStateListOf<Channel>()
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun connect() {
        if (isConnected) return
        scope.launch {
            try {
                socket = if (server.useSSL) {
                    SSLSocketFactory.getDefault().createSocket(server.host, server.port) as Socket
                } else {
                    Socket(server.host, server.port)
                }
                writer = BufferedWriter(OutputStreamWriter(socket!!.getOutputStream()))
                reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                isConnected = true

                // Login
                send("PASS ${server.password ?: ""}")
                send("NICK ${server.nick}")
                send("USER ${server.user} 0 * :${server.realName}")

                while (isConnected) {
                    val line = try { reader?.readLine() } catch (_: Exception) { null } ?: break
                    handleLine(line)
                }
            } catch (e: Exception) {
                postMessage("Erro de conexão: ${e.message ?: "Desconhecido"}")
                disconnect()
            }
        }
    }

    fun send(text: String) {
        try {
            if (isConnected) {
                writer?.write("$text\r\n")
                writer?.flush()
            }
        } catch (_: Exception) {}
    }

    fun joinChannel(name: String, password: String? = null) {
        if (!channels.any { it.name == name }) {
            channels.add(Channel(name, password))
            send("JOIN $name ${password ?: ""}")
        }
    }

    fun partChannel(name: String) {
        channels.removeAll { it.name == name }
        send("PART $name")
    }

    fun privmsg(target: String, message: String) {
        if (message.isNotBlank()) send("PRIVMSG $target :$message")
    }

    fun disconnect() {
        isConnected = false
        scope.cancel()
        try { writer?.close() } catch (_: Exception) {}
        try { reader?.close() } catch (_: Exception) {}
        try { socket?.close() } catch (_: Exception) {}
    }

    private fun handleLine(line: String) {
        postMessage(line)
        if (line.startsWith("PING ")) {
            try { send("PONG ${line.substringAfter("PING ")}") } catch (_: Exception) {}
        }
    }

    private fun postMessage(text: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try { onMessage?.invoke(Message(text)) } catch (_: Exception) {}
        }
    }
}