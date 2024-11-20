package fr.isen.Isoardi.androidsmartdevice

import android.Manifest
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import fr.isen.Isoardi.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class LedActivity : ComponentActivity() {

    private var deviceName: String = "Inconnu"
    private var deviceAddress: String = "Adresse inconnue"
    private var bluetoothGatt: BluetoothGatt? = null
    private var bluetoothDevice: BluetoothDevice? = null

    // État de connexion
    private var isConnected by mutableStateOf(false)

    // Caractéristique pour contrôler les LEDs
    private var ledCharacteristic: BluetoothGattCharacteristic? = null

    // Indique si les services ont été découverts
    private var servicesDiscovered by mutableStateOf(false)

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

        setContent {
            AndroidSmartDeviceTheme {
                LedComposable(
                    deviceName = deviceName,
                    isConnected = isConnected && servicesDiscovered,
                    onConnectClick = { connectToDevice() },
                    onDisconnectClick = { disconnectFromDevice() },
                    onLedButtonClick = { ledState -> controlLed(ledState) }
                )
            }
        }

        checkPermissions()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private val permissions = arrayOf(Manifest.permission.BLUETOOTH_CONNECT
    )

    private val PERMISSION_REQUEST_CODE = 1

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

    private fun initializeBluetooth() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            Toast.makeText(this, "Le Bluetooth n'est pas activé", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Obtenez l'appareil Bluetooth
        try {
            bluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress)
        } catch (e: IllegalArgumentException) {
            Toast.makeText(this, "Adresse de l'appareil invalide", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun connectToDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Les permissions ne sont pas accordées, retournez ou demandez les permissions
                return
            }
        }
        bluetoothGatt = bluetoothDevice?.connectGatt(this, false, gattCallback)
    }

    private fun disconnectFromDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Les permissions ne sont pas accordées, retournez ou demandez les permissions
                return
            }
        }
        bluetoothGatt?.disconnect()
        bluetoothGatt?.close()
        bluetoothGatt = null
        isConnected = false
        servicesDiscovered = false
        Toast.makeText(this, "Déconnecté de l'appareil", Toast.LENGTH_SHORT).show()
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            super.onConnectionStateChange(gatt, status, newState)
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("LedActivity", "Connecté à l'appareil")
                runOnUiThread {
                    isConnected = true
                    Toast.makeText(this@LedActivity, "Connecté à l'appareil", Toast.LENGTH_SHORT).show()
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(
                            this@LedActivity,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // Les permissions ne sont pas accordées
                        return
                    }
                }
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("LedActivity", "Déconnecté de l'appareil")
                runOnUiThread {
                    isConnected = false
                    servicesDiscovered = false
                    Toast.makeText(this@LedActivity, "Déconnecté de l'appareil", Toast.LENGTH_SHORT).show()
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("LedActivity", "Services découverts")
                // Parcourir les services et caractéristiques pour trouver une caractéristique écrivable
                for (service in gatt.services) {
                    Log.d("LedActivity", "Service UUID : ${service.uuid}")
                    for (characteristic in service.characteristics) {
                        Log.d("LedActivity", "Caractéristique UUID : ${characteristic.uuid}")
                        Log.d(
                            "LedActivity",
                            "Propriétés : ${characteristic.properties}"
                        )
                        // Vérifier si la caractéristique est écrivable
                        val writeType = characteristic.properties and
                                (BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)
                        if (writeType > 0) {
                            ledCharacteristic = characteristic
                            runOnUiThread {
                                servicesDiscovered = true
                            }
                            Log.d("LedActivity", "Caractéristique écrivable trouvée : ${characteristic.uuid}")
                            runOnUiThread {
                                Toast.makeText(this@LedActivity, "Caractéristique pour les LEDs trouvée", Toast.LENGTH_SHORT).show()
                            }
                            break
                        }
                    }
                    if (servicesDiscovered) {
                        break
                    }
                }

                if (!servicesDiscovered) {
                    Log.d("LedActivity", "Aucune caractéristique écrivable trouvée")
                    runOnUiThread {
                        Toast.makeText(this@LedActivity, "Aucune caractéristique écrivable trouvée", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.d("LedActivity", "Échec de la découverte des services")
                runOnUiThread {
                    Toast.makeText(this@LedActivity, "Échec de la découverte des services", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun controlLed(ledState: LEDStateEnum) {
        // Vérifier que la caractéristique est disponible
        val characteristic = ledCharacteristic ?: run {
            Toast.makeText(this, "Caractéristique LED non disponible", Toast.LENGTH_SHORT).show()
            return
        }

        // Préparer la valeur à écrire
        characteristic.value = ledState.hex

        // Vérifier les permissions pour Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Permission BLUETOOTH_CONNECT non accordée", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Écrire la valeur sur la caractéristique
        val success = bluetoothGatt?.writeCharacteristic(characteristic) ?: false

        if (!success) {
            Toast.makeText(this, "Échec de l'envoi de la commande", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Commande envoyée", Toast.LENGTH_SHORT).show()
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
