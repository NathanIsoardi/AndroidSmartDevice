package fr.isen.Isoardi.androidsmartdevice

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import fr.isen.Isoardi.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme
import java.util.UUID
import androidx.compose.ui.graphics.Color
class LedActivity : ComponentActivity() {

    private var deviceName: String = "Inconnu"
    private var deviceAddress: String = "Adresse inconnue"
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothDevice: BluetoothDevice? = null

    // Variables pour les caractéristiques
    private var ledCharacteristic: BluetoothGattCharacteristic? = null
    private var button1Characteristic: BluetoothGattCharacteristic? = null
    private var button3Characteristic: BluetoothGattCharacteristic? = null

    // UUID du Client Characteristic Configuration Descriptor (CCCD)
    private val CCCD_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    // Permissions
    @RequiresApi(Build.VERSION_CODES.S)
    private val permissions = arrayOf(
        Manifest.permission.BLUETOOTH_CONNECT
    )

    private val PERMISSION_REQUEST_CODE = 1

    // Variables d'état pour l'interface utilisateur
    private var isConnected by mutableStateOf(false)
    private var notificationCountButton1 by mutableStateOf(0)
    private var notificationCountButton3 by mutableStateOf(0)
    private var isNotificationButton1Enabled by mutableStateOf(false)
    private var isNotificationButton3Enabled by mutableStateOf(false)
    private var shouldIgnoreFirstNotificationButton1 = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Récupérer les données de l'intent
        deviceName = intent.getStringExtra("DEVICE_NAME") ?: "Inconnu"
        deviceAddress = intent.getStringExtra("DEVICE_ADDRESS") ?: "Adresse inconnue"

        Log.d("LedActivity", "deviceName: $deviceName")
        Log.d("LedActivity", "deviceAddress: $deviceAddress")

        // Vérifier que l'adresse de l'appareil est valide
        if (deviceAddress == "Adresse inconnue") {
            Toast.makeText(this, "Adresse de l'appareil non valide", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        checkPermissions()

        // Démarrer l'interface utilisateur
        setContent {
            AndroidSmartDeviceTheme {
                DeviceScreen(
                    isConnected = isConnected,
                    notificationCountButton1 = notificationCountButton1,
                    notificationCountButton3 = notificationCountButton3,
                    isNotificationButton1Enabled = isNotificationButton1Enabled,
                    isNotificationButton3Enabled = isNotificationButton3Enabled,
                    onDisconnectClick = { disconnectFromDevice() },
                    onLedButtonClick = { ledState: LEDStateEnum -> turnOnLed(ledState) },
                    onButton1NotificationsToggle = { enabled ->
                        isNotificationButton1Enabled = enabled
                        if (enabled) {
                            enableNotifications(button1Characteristic)
                        } else {
                            disableNotifications(button1Characteristic)
                        }
                    },
                    onButton3NotificationsToggle = { enabled ->
                        isNotificationButton3Enabled = enabled
                        if (enabled) {
                            enableNotifications(button3Characteristic)
                        } else {
                            disableNotifications(button3Characteristic)
                        }
                    }
                )
            }
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (permissions.any { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }) {
                requestPermissions(permissions, PERMISSION_REQUEST_CODE)
            } else {
                // Les permissions sont accordées, continuez
                initializeBluetooth()
            }
        } else {
            // Les permissions sont accordées par défaut pour les versions antérieures, continuez
            initializeBluetooth()
        }
    }

    @SuppressLint("MissingPermission")
    private fun initializeBluetooth() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Le Bluetooth n'est pas activé", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Obtenir l'appareil Bluetooth
        try {
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress)
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, "Adresse de l'appareil invalide", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Démarrer la connexion
        connectToDevice()
    }

    @SuppressLint("MissingPermission")
    private fun connectToDevice() {
        bluetoothGatt = bluetoothDevice?.connectGatt(this, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    private fun disconnectFromDevice() {
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        isConnected = false
        Toast.makeText(this, "Déconnecté de l'appareil", Toast.LENGTH_SHORT).show()
        finish() // Retour à l'activité précédente
    }

    private val gattCallback = object : BluetoothGattCallback() {
        @SuppressLint("MissingPermission")
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("LedActivity", "Connected to GATT server.")
                gatt.discoverServices()
                runOnUiThread {
                    isConnected = true
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("LedActivity", "Disconnected from GATT server.")
                runOnUiThread {
                    isConnected = false
                }
            }
        }

        @SuppressLint("MissingPermission")
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Accéder aux caractéristiques par indices
                val services = gatt.services
                if (services.size > 3) {
                    val service3 = services[2]
                    val service4 = services[3]

                    // Caractéristique LED du service 3
                    if (service3.characteristics.size > 0) {
                        ledCharacteristic = service3.characteristics[0]
                        Log.d("LedActivity", "LED characteristic found.")
                    } else {
                        Log.d("LedActivity", "LED characteristic not found.")
                    }

                    // Caractéristique de notification du bouton 1
                    if (service3.characteristics.size > 1) {
                        button1Characteristic = service3.characteristics[1]
                        Log.d("LedActivity", "Button 1 characteristic found.")
                    } else {
                        Log.d("LedActivity", "Button 1 characteristic not found.")
                    }

                    // Caractéristique de notification du bouton 3
                    if (service4.characteristics.size > 0) {
                        button3Characteristic = service4.characteristics[0]
                        Log.d("LedActivity", "Button 3 characteristic found.")
                    } else {
                        Log.d("LedActivity", "Button 3 characteristic not found.")
                    }
                }
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic == button1Characteristic && isNotificationButton1Enabled) {
                runOnUiThread {
                    if (shouldIgnoreFirstNotificationButton1) {
                        // Ignorer la première notification
                        shouldIgnoreFirstNotificationButton1 = false
                        Log.d("LedActivity", "Première notification du bouton 1 ignorée.")
                    } else {
                        notificationCountButton1++
                        Log.d("LedActivity", "Notification reçue pour le bouton 1. Compteur : $notificationCountButton1")
                    }
                }
            } else if (characteristic == button3Characteristic && isNotificationButton3Enabled) {
                runOnUiThread {
                    notificationCountButton3++
                    Log.d("LedActivity", "Notification reçue pour le bouton 3. Compteur : $notificationCountButton3")
                }
            }
        }
    }

    private fun BluetoothGattCharacteristic?.getCCCDDescriptor(): BluetoothGattDescriptor? {
        if (this == null) return null
        return this.getDescriptor(CCCD_UUID)
    }

    @SuppressLint("MissingPermission")
    private fun enableNotifications(characteristic: BluetoothGattCharacteristic?) {
        val gatt = bluetoothGatt ?: return
        characteristic?.let {
            gatt.setCharacteristicNotification(it, true)
            val descriptor = it.getCCCDDescriptor()
            descriptor?.let { desc ->
                desc.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(desc)
                Log.d("LedActivity", "Notifications enabled for characteristic.")

                // Si c'est la caractéristique du bouton 1, définir le drapeau
                if (it == button1Characteristic) {
                    shouldIgnoreFirstNotificationButton1 = true
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun disableNotifications(characteristic: BluetoothGattCharacteristic?) {
        val gatt = bluetoothGatt ?: return
        characteristic?.let {
            gatt.setCharacteristicNotification(it, false)
            val descriptor = it.getCCCDDescriptor()
            descriptor?.let { desc ->
                desc.value = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(desc)
                Log.d("LedActivity", "Notifications disabled for characteristic.")
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun turnOnLed(ledState: LEDStateEnum) {
        val ledChar = ledCharacteristic
        ledChar?.let {
            it.value = ledState.hex
            bluetoothGatt?.writeCharacteristic(it)
            Log.d("LedActivity", "LED command sent: ${ledState.name}")
        }
    }

    @Composable
    fun DeviceScreen(
        isConnected: Boolean,
        notificationCountButton1: Int,
        notificationCountButton3: Int,
        isNotificationButton1Enabled: Boolean,
        isNotificationButton3Enabled: Boolean,
        onDisconnectClick: () -> Unit,
        onLedButtonClick: (LEDStateEnum) -> Unit,
        onButton1NotificationsToggle: (Boolean) -> Unit,
        onButton3NotificationsToggle: (Boolean) -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            Text(
                text = "Appareil : $deviceName",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (isConnected) {
                Text(
                    text = "Connecté",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Text(
                    text = "Déconnecté",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Bouton de déconnexion
            Button(
                onClick = onDisconnectClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Se déconnecter", color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Boutons pour contrôler les LEDs avec des couleurs personnalisées
            Button(
                onClick = { onLedButtonClick(LEDStateEnum.NONE) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Éteindre les LEDs", color = Color.White)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { onLedButtonClick(LEDStateEnum.LED_1) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5CABAB)) // Rouge
            ) {
                Text(text = "Allumer LED n°1", color = Color.White)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { onLedButtonClick(LEDStateEnum.LED_2) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5CABAB)) // Vert
            ) {
                Text(text = "Allumer LED n°2", color = Color.White)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = { onLedButtonClick(LEDStateEnum.LED_3) },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5CABAB)) // Bleu
            ) {
                Text(text = "Allumer LED n°3", color = Color.White)
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Switch pour le suivi du bouton 1
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Suivi du bouton 1")
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isNotificationButton1Enabled,
                    onCheckedChange = onButton1NotificationsToggle,
                    enabled = isConnected,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Affichage du compteur pour le bouton 1
            if (isNotificationButton1Enabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Nombre de clics sur le bouton 1 : $notificationCountButton1",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Switch pour le suivi du bouton 3
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Suivi du bouton 3")
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isNotificationButton3Enabled,
                    onCheckedChange = onButton3NotificationsToggle,
                    enabled = isConnected,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Affichage du compteur pour le bouton 3
            if (isNotificationButton3Enabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Nombre de clics sur le bouton 3 : $notificationCountButton3",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectFromDevice()
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission accordée, reprendre l'action initiale
                initializeBluetooth()
            } else {
                Toast.makeText(this, "Permission BLUETOOTH_CONNECT non accordée", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
