package com.example.modulelensmobile.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.modulelensmobile.ui.components.ModuleLensCard
import com.example.modulelensmobile.ui.components.ModuleLensTopBar

private val NavyColor = Color(0xFF102A43)

@Composable
fun ProfileScreen(
    onLogout: () -> Unit
) {
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
                    Text(
                        text = "Account",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = NavyColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Student account details will appear here once API integration is complete.", color = Color(0xFF64748B), fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            ModuleLensCard {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(
                        text = "App Settings",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = NavyColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Theme, notifications, and more – coming soon.", color = Color(0xFF64748B), fontSize = 13.sp)
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
            ) {
                Text("Log Out", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

