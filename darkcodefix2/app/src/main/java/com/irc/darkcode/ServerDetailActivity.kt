package com.irc.darkcode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerDetailScreen(cfg: ServerConfig, onBack: () -> Unit) {
    val client = remember { IRCClient(cfg) }
    val messages = remember { mutableStateListOf<Message>() }
    var input by remember { mutableStateOf("") }
    var joinedChannel by remember { mutableStateOf("#general") }

    // Conectar ao servidor e entrar no canal
    LaunchedEffect(cfg) {
        client.onMessage = { msg ->
            try { messages.add(msg) } catch (_: Exception) {}
        }
        client.connect()
        client.joinChannel(joinedChannel)
    }

    // Desconectar quando a tela for destruída
    DisposableEffect(Unit) {
        onDispose { client.disconnect() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(cfg.name, color = if (client.isConnected) Color.Green else Color.Red)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.Cyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Lista de mensagens
            LazyColumn(
                modifier = Modifier.weight(1f).padding(12.dp),
                reverseLayout = true
            ) {
                itemsIndexed(messages) { _, msg ->
                    Text(
                        text = msg.text,
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            // Campo de envio
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Mensagem/Comando") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (input.isNotBlank()) {
                            if (client.isConnected) {
                                client.privmsg(joinedChannel, input)
                            } else {
                                messages.add(Message("Não conectado"))
                            }
                            input = ""
                        }
                    },
                    enabled = client.isConnected
                ) {
                    Text("Enviar")
                }
            }
        }
    }
}