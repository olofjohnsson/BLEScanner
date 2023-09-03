package com.example.blescanner

import android.Manifest
import android.Manifest.permission.BLUETOOTH_SCAN
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.blescanner.ui.theme.BleScannerTheme
import java.util.Locale
import kotlin.reflect.KProperty

class MainActivity : ComponentActivity() {

    companion object{
        private val TAG = "OLLEBTSCANNER"
        val BLUETOOTH_REQUEST_CODE = 1

    }

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        (getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BleScannerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        super.onResume()

        if (bluetoothAdapter.isEnabled){
            // start scanning
            startBLEScan()
        }
        else
        {
            Log.v(TAG, "BT is disabled")
            val btIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
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
            }
            startActivityForResult(btIntent, BLUETOOTH_REQUEST_CODE)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startBLEScan()
    {
        Log.v(TAG, "Start BLEScan")

        val byteArray = ByteArray(2)

        val manufacturerId = 0x0707 // Replace with the desired manufacturer ID
        val manufacturerData = byteArrayOf(0x00, 0x07, 0x00, 0x07)

        val scanFilter = ScanFilter.Builder()
            //.setDeviceAddress("F3:33:30:F8:63:C8")
            //.setDeviceName("B4E55336235030AEE9")
            //.setManufacturerData(manufacturerId, manufacturerData)
            .build()

        val scanFilters:MutableList<ScanFilter> = mutableListOf()
        scanFilters.add(scanFilter)

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setLegacy(false) // Enable extended scanning
            .build()

        Log.v(TAG, "Start scan...")

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.v(TAG, "missing Scan permission...")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_SCAN),
                BLUETOOTH_REQUEST_CODE
            )
            return
        }
        bluetoothAdapter.bluetoothLeScanner.startScan(scanFilters, scanSettings, bleScanCallback)
    }

    private val bleScanCallback : ScanCallback by lazy {
        object : ScanCallback(){
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                //super.onScanResult(callbackType, result)
                //Log.v(TAG, "onScanResult")

                val bluetoothDevice = result?.device
                val scanRecord = result?.scanRecord
                if (bluetoothDevice != null)
                {
                    if (ActivityCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.v(TAG, "Need permission BLUETOOTH_CONNECT")
                        return
                    }
                    if (bluetoothDevice != null && scanRecord != null) {
                        val manufacturerData = scanRecord.manufacturerSpecificData
                        val advertisedData = scanRecord.bytes // Raw advertised data
                        // Example: Parse manufacturer data (if it exists)
                        if (manufacturerData != null) {
                            val manufacturerId = manufacturerData.keyAt(0)
                            Log.v(TAG, "ID: ${manufacturerId.toString(16).uppercase()}")
                            val manufacturerBytes = manufacturerData.get(manufacturerId)
                            // Parse and use the manufacturer-specific data
                            if (manufacturerBytes == byteArrayOf(0x00, 0x07, 0x00, 0x07))
                            {
                                Log.v(TAG, "ExtAdvData: ${advertisedData.toString()}")
                                Log.v(TAG, "Device Name ${bluetoothDevice.name} Device UUIDS ${bluetoothDevice.uuids} Device Address ${bluetoothDevice.address}")
                            }
                        }

                        //Log.v(TAG, "Device Name ${bluetoothDevice.name} Device UUIDS ${bluetoothDevice.uuids} Device Address ${bluetoothDevice.address}")

                    }
                }

            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BleScannerTheme {
        Greeting("Android")
    }
}