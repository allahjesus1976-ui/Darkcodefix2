package com.irc.darkcode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServerScreen(onBack: () -> Unit, onSave: (ServerConfig) -> Unit) {
    var name by remember { mutableStateOf("") }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("6667") }
    var nick by remember { mutableStateOf("Guest") }
    var useSSL by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp)) {
        IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.Cyan)
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome do Servidor", color = Color.Gray) }, modifier = Modifier.fillMaxWidth())
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
        Button(onClick = {
            if (name.isNotBlank() && host.isNotBlank()) {
                val portNum = port.toIntOrNull() ?: 6667
                onSave(ServerConfig(name, host, portNum, nick, useSSL = useSSL))
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Salvar Servidor")
        }
    }
}