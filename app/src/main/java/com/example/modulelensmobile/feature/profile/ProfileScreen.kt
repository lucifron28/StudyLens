package com.example.modulelensmobile.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.modulelensmobile.data.mock.MockData
import com.example.modulelensmobile.ui.components.ModuleLensCard
import com.example.modulelensmobile.ui.components.ModuleLensTopBar

@Composable
fun ProfileScreen(
    onLogout: () -> Unit
) {
    val user = MockData.currentUser

    Scaffold(
        topBar = { ModuleLensTopBar(title = "Profile") }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            ModuleLensCard {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(text = "User Profile Card")
                    Text(text = "Name: ${user.name}")
                    Text(text = "Email: ${user.email}")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            ModuleLensCard {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(text = "Stats")
                    Text(text = "Settings list...")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
                Text("Logout Placeholder")
            }
        }
    }
}
