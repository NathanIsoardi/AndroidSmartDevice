package fr.isen.Isoardi.androidsmartdevice

import android.Manifest
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import fr.isen.Isoardi.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class PageCoActivity : ComponentActivity() {

    private var deviceName: String = "Inconnu"
    private var deviceAddress: String = "Adresse inconnue"
    private var rssi: Int = 0
    private var bluetoothGatt: BluetoothGatt? = null

    // État de connexion
    private var isConnected by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Récupérer les données de l'intent
        deviceName = intent.getStringExtra("DEVICE_NAME") ?: "Inconnu"
        deviceAddress = intent.getStringExtra("DEVICE_ADDRESS") ?: "Adresse inconnue"
        rssi = intent.getIntExtra("DEVICE_RSSI", 0)

        setContent {
            AndroidSmartDeviceTheme {
                PageCoComposable(
                    deviceName = deviceName,
                    rssi = rssi,
                    isConnected = isConnected,
                    onConnectClick = { connectToDevice() },
                    onDisconnectClick = { disconnectFromDevice() },
                    onNavigateToLedControl = {
                        val intent = Intent(this, LedActivity::class.java).apply {
                            putExtra("DEVICE_NAME", deviceName)
                            putExtra("DEVICE_ADDRESS", deviceAddress)
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }

    private fun connectToDevice() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        val device = bluetoothAdapter.getRemoteDevice(deviceAddress)

        // Vérifier les permissions si nécessaire pour les versions d'Android >= 31
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), PERMISSION_REQUEST_CODE)
                return
            }
        }

        // Se connecter à l'appareil
        bluetoothGatt = device.connectGatt(this, false, gattCallback)
    }

    private fun disconnectFromDevice() {
        // Vérifier les permissions si nécessaire pour les versions d'Android >= 31
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT), PERMISSION_REQUEST_CODE)
                return
            }
        }

        bluetoothGatt?.let {
            it.disconnect()
            it.close()
            bluetoothGatt = null
            isConnected = false
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            super.onConnectionStateChange(gatt, status, newState)
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    runOnUiThread {
                        isConnected = true
                        Toast.makeText(this@PageCoActivity, "Connecté à l'appareil", Toast.LENGTH_SHORT).show()
                    }
                    // Vous pouvez découvrir les services si nécessaire
                    // gatt.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    runOnUiThread {
                        isConnected = false
                        Toast.makeText(this@PageCoActivity, "Déconnecté de l'appareil", Toast.LENGTH_SHORT).show()
                    }
                    bluetoothGatt = null
                }
            }
        }

        // Vous pouvez implémenter d'autres callbacks si nécessaire
    }

    override fun onDestroy() {
        super.onDestroy()
        disconnectFromDevice()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }
}
