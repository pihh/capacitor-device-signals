# capacitor-signal-triangulation

signal analysis for object positioning and movement detection using ionic capacitor and tensorflow

## Install

```bash
npm install capacitor-signal-triangulation
npx cap sync
```

## API

<docgen-index>

* [`startScan()`](#startscan)
* [`stopScan()`](#stopscan)
* [`startWifiRssiMonitor()`](#startwifirssimonitor)
* [`stopWifiRssiMonitor()`](#stopwifirssimonitor)
* [`addListener('wifiScanResult' | 'bluetoothScanResult' | 'cellSignalResult' | 'sensorData' | 'wifiRssiUpdate', ...)`](#addlistenerwifiscanresult--bluetoothscanresult--cellsignalresult--sensordata--wifirssiupdate-)
* [`removeAllListeners()`](#removealllisteners)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### startScan()

```typescript
startScan() => Promise<void>
```

--------------------


### stopScan()

```typescript
stopScan() => Promise<void>
```

--------------------


### startWifiRssiMonitor()

```typescript
startWifiRssiMonitor() => Promise<any>
```

**Returns:** <code>Promise&lt;any&gt;</code>

--------------------


### stopWifiRssiMonitor()

```typescript
stopWifiRssiMonitor() => Promise<any>
```

**Returns:** <code>Promise&lt;any&gt;</code>

--------------------


### addListener('wifiScanResult' | 'bluetoothScanResult' | 'cellSignalResult' | 'sensorData' | 'wifiRssiUpdate', ...)

```typescript
addListener(eventName: 'wifiScanResult' | 'bluetoothScanResult' | 'cellSignalResult' | 'sensorData' | "wifiRssiUpdate", listenerFunc: (data: any) => void) => Promise<void>
```

| Param              | Type                                                                                                             |
| ------------------ | ---------------------------------------------------------------------------------------------------------------- |
| **`eventName`**    | <code>'wifiScanResult' \| 'bluetoothScanResult' \| 'cellSignalResult' \| 'sensorData' \| 'wifiRssiUpdate'</code> |
| **`listenerFunc`** | <code>(data: any) =&gt; void</code>                                                                              |

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

--------------------

</docgen-api>



package com.signals.pihh;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
//import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;

import com.getcapacitor.*;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@CapacitorPlugin(
    name = "SignalTriangulation",
    permissions = {
        @Permission(alias = "wifi", strings = { Manifest.permission.ACCESS_FINE_LOCATION }),
        @Permission(alias = "bluetooth", strings = {
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        })
    }
)
public class SignalTriangulationPlugin extends Plugin {

    private WifiManager wifiManager;
    private BluetoothAdapter bluetoothAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void load() {
        Context context = getContext().getApplicationContext();
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    // === WIFI ===

    @PluginMethod
    public void startScan(PluginCall call) {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            call.reject("Missing ACCESS_FINE_LOCATION permission");
            return;
        }

        wifiManager.startScan();
        call.resolve();
    }

    @PluginMethod
    public void getWifiNetworks(PluginCall call) {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            call.reject("Missing ACCESS_FINE_LOCATION permission");
            return;
        }

        @SuppressLint("MissingPermission")
        List<android.net.wifi.ScanResult> results = wifiManager.getScanResults();
        JSArray networks = new JSArray();

        for (android.net.wifi.ScanResult r : results) {
            JSObject net = new JSObject();
            net.put("ssid", r.SSID);
            net.put("bssid", r.BSSID);
            net.put("capabilities", r.capabilities);
            net.put("frequency", r.frequency);
            net.put("level", r.level);
            net.put("timestamp", r.timestamp);
            networks.put(net);
        }

        JSObject ret = new JSObject();
        ret.put("networks", networks);
        call.resolve(ret);
    }

    // === BLUETOOTH ===

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @PluginMethod
    public void getBluetoothDevices(final PluginCall call) {
        // Check if Bluetooth and WiFi permissions are granted
        if (!hasRequiredPermissions()) {
            requestPermissions(call); // Request permissions if not granted
            return;
        }

        if (bluetoothAdapter == null || bluetoothAdapter.getBluetoothLeScanner() == null) {
            call.reject("Bluetooth LE not supported or unavailable");
            return;
        }

        final List<JSObject> deviceList = new ArrayList<>();

        final ScanCallback scanCallback = new ScanCallback() {
            @SuppressLint("NewApi")
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                BluetoothDevice device = result.getDevice();
                if (device != null) {
                    JSObject btObj = new JSObject();
                    btObj.put("name", device.getName());
                    btObj.put("address", device.getAddress());
                    btObj.put("rssi", result.getRssi());
                    btObj.put("timestampNanos", result.getTimestampNanos());
                    btObj.put("txPower", result.getTxPower());
                    deviceList.add(btObj);
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                call.reject("Scan failed with error code: " + errorCode);
            }
        };

        bluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);

        // Stop scanning after 3 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
            JSArray devices = new JSArray(deviceList);
            JSObject ret = new JSObject();
            ret.put("devices", devices);
            call.resolve(ret);
        }, 3000); // 3 seconds
    }

    public void requestPermissions(PluginCall call) {
        // Request permissions and pass the callback method to handle the result
        requestPermissions(new String[] {
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
        }, call, "handlePermissionResult");
    }

    private void requestPermissions(String[] strings, PluginCall call, String handlePermissionResult) {

    }

    // @PermissionCallback to handle permission results
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @PermissionCallback
    public void handlePermissionResult(PluginCall call) {
        // Check if the permissions were granted
        if (getPermissionState("bluetooth") == PermissionState.GRANTED &&
                getPermissionState("wifi") == PermissionState.GRANTED) {
            // Permissions granted, proceed with Bluetooth scanning
            getBluetoothDevices(call);
        } else {
            // If permissions are denied, reject the call
            call.reject("Required permissions not granted.");
        }
    }

    public boolean hasRequiredPermissions() {
        return getPermissionState("bluetooth") == PermissionState.GRANTED &&
                getPermissionState("wifi") == PermissionState.GRANTED;
    }



    @PluginMethod
    public void stopScan(PluginCall call) {
        // Not tracking scans persistently, but you can extend this to stop early if needed
        call.resolve();
    }

    // === PERMISSIONS ===
/*
    @Override
    public boolean hasRequiredPermissions() {
        return getPermissionState("bluetooth") == PermissionState.GRANTED &&
                getPermissionState("wifi") == PermissionState.GRANTED;
    }
    @Override
    public boolean hasPermission(String permission){
        return ActivityCompat.checkSelfPermission(getContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }*/
}


    // private WifiManager wifiManager;
    // private BluetoothAdapter bluetoothAdapter;
    // private TelephonyManager telephonyManager;
    // private SensorManager sensorManager;
    // private Sensor magnetometer, accelerometer, lightSensor;
    // private Handler handler = new Handler(Looper.getMainLooper());
#   c a p a c i t o r - d e v i c e - s i g n a l s  
 