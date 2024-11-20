package fr.isen.Isoardi.androidsmartdevice

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import fr.isen.Isoardi.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

// Classe de données pour représenter les appareils BLE
data class BLEDevice(val name: String, val address: String, val rssi: Int)

class ScanActivity : ComponentActivity() {
    private val SCAN_PERIOD: Long = 10000 // 10 secondes

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bluetoothLeScanner: BluetoothLeScanner? by lazy(LazyThreadSafetyMode.NONE) {
        bluetoothAdapter?.bluetoothLeScanner
    }

    private val scanResults = mutableStateListOf<BLEDevice>() // Liste des appareils détectés
    private var isScanning by mutableStateOf(false) // État du scan

    private val requiredPermissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }

    private val enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != RESULT_OK) {
            Toast.makeText(this, "Le Bluetooth est requis pour scanner les appareils BLE", Toast.LENGTH_SHORT).show()
        } else {
            startScan()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidSmartDeviceTheme {
                ScanScreen(
                    scanResults = scanResults,
                    isScanning = isScanning,
                    onScanToggle = {
                        if (isScanning) {
                            stopScan()
                        } else {
                            startScan()
                        }
                    },
                    onDeviceClick = { device -> onDeviceClick(device) }
                )
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val permissionsToRequest = mutableListOf<String>()

        // Vérifie chaque permission requise
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADMIN)
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        // Si des permissions sont manquantes, les demander
        return if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
            false
        } else {
            true
        }
    }

    private fun startScan() {
        // Vérifier les permissions
        if (!checkPermissions()) {
            return
        }

        // Vérifier le Bluetooth
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth non disponible sur cet appareil", Toast.LENGTH_LONG).show()
            finish()
            return
        } else if (!bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
            return
        }

        // Démarrer le scan
        bluetoothLeScanner?.let { scanner ->
            isScanning = true
            scanResults.clear()

            // Vérifier la permission avant de démarrer le scan
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(this, "Permission BLUETOOTH_SCAN non accordée", Toast.LENGTH_SHORT).show()
                    return
                }
            }

            scanner.startScan(leScanCallback)

            // Arrêter le scan après SCAN_PERIOD
            Handler(Looper.getMainLooper()).postDelayed({
                stopScan()
            }, SCAN_PERIOD)
        } ?: run {
            Toast.makeText(this, "Scanner Bluetooth non disponible", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopScan() {
        if (isScanning) {
            // Vérifier la permission avant d'arrêter le scan
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    Toast.makeText(this, "Permission BLUETOOTH_SCAN non accordée", Toast.LENGTH_SHORT).show()
                    return
                }
            }

            bluetoothLeScanner?.stopScan(leScanCallback)
            isScanning = false
            Toast.makeText(this, "Scan arrêté", Toast.LENGTH_SHORT).show()
        }
    }

    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)

            val deviceName: String
            val deviceAddress: String
            val rssi: Int = result.rssi

            // Récupération du nom et de l'adresse de l'appareil
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(
                        this@ScanActivity,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    deviceName = "Permission requise"
                    deviceAddress = "Permission requise"
                } else {
                    deviceName = result.device.name ?: "Inconnu"
                    deviceAddress = result.device.address ?: "Adresse inconnue"
                }
            } else {
                deviceName = result.device.name ?: "Inconnu"
                deviceAddress = result.device.address ?: "Adresse inconnue"
            }

            // Ajout du filtre pour ignorer les appareils nommés "Inconnu"
            if (deviceName == "Inconnu") {
                // Ignorer cet appareil
                return
            }

            // Vérifier si l'appareil existe déjà dans la liste
            val existingDeviceIndex = scanResults.indexOfFirst { it.address == deviceAddress }
            if (existingDeviceIndex != -1) {
                // Mettre à jour le RSSI de l'appareil existant
                scanResults[existingDeviceIndex] = scanResults[existingDeviceIndex].copy(rssi = rssi)
            } else {
                // Ajouter le nouvel appareil à la liste
                val bleDevice = BLEDevice(name = deviceName, address = deviceAddress, rssi = rssi)
                scanResults.add(bleDevice)
            }

            // Optionnel : trier la liste par RSSI décroissant
            scanResults.sortByDescending { it.rssi }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            results.forEach { result ->
                val deviceName: String
                val deviceAddress: String
                val rssi: Int = result.rssi

                // Récupération du nom et de l'adresse de l'appareil
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (ActivityCompat.checkSelfPermission(
                            this@ScanActivity,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        deviceName = "Permission requise"
                        deviceAddress = "Permission requise"
                    } else {
                        deviceName = result.device.name ?: "Inconnu"
                        deviceAddress = result.device.address ?: "Adresse inconnue"
                    }
                } else {
                    deviceName = result.device.name ?: "Inconnu"
                    deviceAddress = result.device.address ?: "Adresse inconnue"
                }

                // Ajout du filtre pour ignorer les appareils nommés "Inconnu"
                if (deviceName == "Inconnu") {
                    // Ignorer cet appareil
                    return@forEach
                }

                // Vérifier si l'appareil existe déjà dans la liste
                val existingDeviceIndex = scanResults.indexOfFirst { it.address == deviceAddress }
                if (existingDeviceIndex != -1) {
                    // Mettre à jour le RSSI de l'appareil existant
                    scanResults[existingDeviceIndex] = scanResults[existingDeviceIndex].copy(rssi = rssi)
                } else {
                    // Ajouter le nouvel appareil à la liste
                    val bleDevice = BLEDevice(name = deviceName, address = deviceAddress, rssi = rssi)
                    scanResults.add(bleDevice)
                }
            }

            // Optionnel : trier la liste par RSSI décroissant
            scanResults.sortByDescending { it.rssi }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Toast.makeText(this@ScanActivity, "Erreur de scan : $errorCode", Toast.LENGTH_SHORT).show()
            isScanning = false
        }
    }

    private fun onDeviceClick(device: BLEDevice) {
        val intent = Intent(this, PageCoActivity::class.java).apply {
            putExtra("DEVICE_NAME", device.name)
            putExtra("DEVICE_ADDRESS", device.address)
            putExtra("DEVICE_RSSI", device.rssi)
        }
        startActivity(intent)
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val allPermissionsGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
            if (allPermissionsGranted) {
                startScan()
            } else {
                Toast.makeText(this, "Permissions requises pour scanner les appareils BLE", Toast.LENGTH_LONG).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }
}
