package com.irc.darkcode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServersListScreen(
    servers: List<ServerConfig>,
    onAdd: () -> Unit,
    onOpen: (Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Servidores IRC", color = Color.Cyan) },
                actions = {
                    IconButton(onClick = onAdd) {
                        Icon(Icons.Default.Add, contentDescription = "Adicionar Servidor", tint = Color.Cyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                        Text("Nick: ${server.nick}", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}