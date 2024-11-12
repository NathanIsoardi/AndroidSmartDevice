package fr.isen.Isoardi.androidsmartdevice

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.Isoardi.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidSmartDeviceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onScanButtonClick = {
                            // Code pour lancer ScanActivity
                            val intent = Intent(this, ScanActivity::class.java)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onScanButtonClick: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("AndroidSmartDevice")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.LightGray, // Couleur de fond du TopAppBar
                    titleContentColor = Color.White // Couleur du texte dans le TopAppBar
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Titre
            Text(
                text = "Bienvenue dans votre application Smart Device",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Description
            Text(
                text = "Pour démarrer vos interactions avec les appareils BLE environnants cliquer sur commencer",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Image Bluetooth
            Image(
                painter = painterResource(id = R.drawable.logob),
                contentDescription = "logo Bluetooth",
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Bouton pour démarrer le scan
            Button(onClick = onScanButtonClick) {
                Text("Commencer")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    AndroidSmartDeviceTheme {
        MainScreen(onScanButtonClick = {})
    }
}
