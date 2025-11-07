package com.irc.darkcode

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.*
import java.io.*
import java.net.Socket
import javax.net.ssl.SSLSocketFactory

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

data class Message(val text: String, val from: String = "")

// ------------------- IRC Client -------------------

class IRCClient(private val server: ServerConfig) {
    private var socket: Socket? = null
    private var writer: BufferedWriter? = null
    private var reader: BufferedReader? = null
    var onMessage: ((Message) -> Unit)? = null
    var isConnected by mutableStateOf(false)
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

                send("PASS ${server.password ?: ""}")
                send("NICK ${server.nick}")
                send("USER ${server.user} 0 * :${server.realName}")

                while (isConnected) {
                    val line = reader?.readLine() ?: break
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
            send("PONG ${line.substringAfter("PING ")}")
        }
    }

    private fun postMessage(text: String) {
        CoroutineScope(Dispatchers.Main).launch {
            onMessage?.invoke(Message(text))
        }
    }
}

// ------------------- Main Activity -------------------

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IRCApp()
        }
    }
}

// ------------------- App Principal -------------------

@Composable
fun IRCApp() {
    var currentScreen by remember { mutableStateOf("list") }
    var selectedServer by remember { mutableStateOf<ServerConfig?>(null) }
    val servers = remember {
        mutableStateListOf(
            ServerConfig("DarkCode IRC", "irc.darkcode.net", 6667, "Guest")
        )
    }

    when (currentScreen) {
        "list" -> ServersListScreen(
            servers = servers,
            onAdd = { currentScreen = "add" },
            onOpen = { selectedServer = servers[it]; currentScreen = "chat" }
        )
        "add" -> AddServerScreen(
            onBack = { currentScreen = "list" },
            onSave = { newServer ->
                servers.add(newServer)
                currentScreen = "list"
            }
        )
        "chat" -> selectedServer?.let { server ->
            ServerChatScreen(server = server, onBack = { currentScreen = "list" })
        }
    }
}

// ------------------- Lista de Servidores -------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersListScreen(servers: List<ServerConfig>, onAdd: () -> Unit, onOpen: (Int) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Servidores IRC", color = Color.Cyan) },
                actions = {
                    IconButton(onClick = onAdd) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar", tint = Color.Cyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {
            itemsIndexed(servers) { index, server ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onOpen(index) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF101010))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(server.name, color = Color.White, fontSize = 18.sp)
                        Text("${server.host}:${server.port}", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ------------------- Adicionar Servidor -------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServerScreen(onBack: () -> Unit, onSave: (ServerConfig) -> Unit) {
    var name by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("6667") }
    var nick by remember { mutableStateOf("Guest") }
    var useSSL by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Adicionar Servidor", color = Color.Cyan) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.Cyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome", color = Color.Gray) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = host, onValueChange = { host = it }, label = { Text("Host", color = Color.Gray) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = port, onValueChange = { port = it }, label = { Text("Porta", color = Color.Gray) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = nick, onValueChange = { nick = it }, label = { Text("Nickname", color = Color.Gray) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = useSSL, onCheckedChange = { useSSL = it }, colors = CheckboxDefaults.colors(checkedColor = Color.Cyan))
                Text("Usar SSL", color = Color.White)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (name.isNotBlank() && host.isNotBlank()) {
                        val portNum = port.toIntOrNull() ?: 6667
                        onSave(ServerConfig(name, host, portNum, nick, useSSL = useSSL))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salvar Servidor")
            }
        }
    }
}

// ------------------- Chat IRC -------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerChatScreen(server: ServerConfig, onBack: () -> Unit) {
    val client = remember { IRCClient(server) }
    val messages = remember { mutableStateListOf<Message>() }
    var input by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        client.onMessage = { messages.add(it) }
        client.connect()
    }

    DisposableEffect(Unit) {
        onDispose { client.disconnect() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(server.name, color = if (client.isConnected) Color.Green else Color.Red) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.Cyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                itemsIndexed(messages) { _, msg ->
                    Text(msg.text, color = Color.White, fontSize = 14.sp)
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Mensagem") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    if (input.isNotBlank()) {
                        client.privmsg("#general", input)
                        input = ""
                    }
                }, enabled = client.isConnected) {
                    Text("Enviar")
                }
            }
        }
    }
}