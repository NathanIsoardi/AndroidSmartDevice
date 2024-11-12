package fr.isen.Isoardi.androidsmartdevice

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import fr.isen.Isoardi.androidsmartdevice.ui.theme.AndroidSmartDeviceTheme

class ScanActivity : ComponentActivity() {
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }
    private val bluetoothLeScanner: BluetoothLeScanner? by lazy {
        bluetoothAdapter?.bluetoothLeScanner
    }
    private val scanResults = mutableStateListOf<ScanResult>() // List to hold scan results
    private var isScanning by mutableStateOf(false) // Track scanning state

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
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AndroidSmartDeviceTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ScanScreen(
                        modifier = Modifier.padding(innerPadding),
                        scanResults = scanResults,
                        isScanning = isScanning,
                        onScanToggle = {
                            if (isScanning) {
                                stopScan()
                            } else {
                                startScan()
                            }
                        }
                    )
                }
            }
        }
    }

    private fun checkPermissions(): Boolean {
        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toTypedArray(), PERMISSION_REQUEST_CODE)
            return false
        }
        return true
    }

    private fun startScan() {
        // Vérifier si le Bluetooth est disponible et activé
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth non disponible sur cet appareil", Toast.LENGTH_LONG).show()
            finish()
            return
        } else if (!bluetoothAdapter!!.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
            return
        }

        // Vérifier les permissions avant de commencer le scan
        if (!checkPermissions()) {
            // Si les permissions sont manquantes, elles seront demandées dans checkPermissions
            return
        }

        // Si les permissions sont accordées, démarrer le scan
        if (bluetoothLeScanner != null) {
            isScanning = true
            scanResults.clear()
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            bluetoothLeScanner?.startScan(leScanCallback)
        } else {
            Toast.makeText(this, "Scanner Bluetooth non disponible", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopScan() {
        if (hasBluetoothScanPermission()) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            bluetoothLeScanner?.stopScan(leScanCallback)
            isScanning = false
        }
    }

    private fun hasBluetoothScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permissions BLE non requises avant Android 12
        }
    }

    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            if (!scanResults.contains(result)) {
                scanResults.add(result) // Add new result to the list
            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)
            results.forEach { result ->
                if (!scanResults.contains(result)) {
                    scanResults.add(result)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            Toast.makeText(this@ScanActivity, "Erreur de scan : $errorCode", Toast.LENGTH_SHORT).show()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = permissions.zip(grantResults.toTypedArray())
                .filter { (_, result) -> result != PackageManager.PERMISSION_GRANTED }
                .map { (permission, _) -> permission }
            if (deniedPermissions.isNotEmpty()) {
                Toast.makeText(this, "Permissions requises pour scanner les appareils BLE", Toast.LENGTH_LONG).show()
            } else {
                // Les permissions sont accordées, relancer le scan
                startScan()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    modifier: Modifier = Modifier,
    scanResults: List<ScanResult>,
    isScanning: Boolean,
    onScanToggle: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AndroidSmartDevice") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.LightGray,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Lancer le Scan BLE",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Image(
                    painter = painterResource(id = R.drawable.fleche),
                    contentDescription = "Arrow Icon",
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onScanToggle) {
                Text(text = if (isScanning) "Arrêter le scan BLE" else "Lancer le scan BLE")
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (isScanning) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 16.dp)) {
                items(scanResults) { result ->
                    val deviceName = if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return
                    } else {
                        true
                    }
                    result.device.name ?: "Inconnu"
                    val deviceAddress = result.device.address ?: "Adresse inconnue"
                    Text(text = "Appareil : $deviceName\nAdresse: $deviceAddress")
                    Divider(color = Color.LightGray, thickness = 1.dp)
                }
            }
        }
    }
}
