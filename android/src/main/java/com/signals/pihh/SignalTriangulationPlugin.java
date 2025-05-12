package com.signals.pihh;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrength;
import android.telephony.TelephonyManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.RequiresApi;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import java.util.List;

@CapacitorPlugin(
    name = "SignalTriangulationPlugin",
    permissions = {
        @Permission(alias = "location", strings = { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION }),
        @Permission(alias = "bluetooth", strings = { Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT }),
        @Permission(alias = "wifi", strings = { Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE }),
        @Permission(alias = "phone", strings = { Manifest.permission.READ_PHONE_STATE })
    }
)
public class SignalTriangulationPlugin extends Plugin implements SensorEventListener {

    private WifiManager wifiManager;
    private BluetoothLeScanner bluetoothLeScanner;
    private TelephonyManager telephonyManager;
    private SensorManager sensorManager;

    @Override
    public void load() {
        Context context = getContext();
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    @PluginMethod
    public void startScan(PluginCall call) {
        if (!hasRequiredPermissions()) {
            requestAllPermissions(call, "permissionsCallback");
        } else {
            performScan(call);
        }
    }

    @PermissionCallback
    private void permissionsCallback(PluginCall call) {
        if (hasRequiredPermissions()) {
            performScan(call);
        } else {
            call.reject("Permissions not granted.");
        }
    }

    private void performScan(PluginCall call) {
        scanWifi();
        scanBluetooth();
        scanCellTowers();
        call.resolve();
    }

    private void scanWifi() {
        if (wifiManager != null) {
            int rssi = wifiManager.getConnectionInfo().getRssi();
            JSObject wifiObj = new JSObject();
            wifiObj.put("rssi", rssi);
            notifyListeners("wifiRssiUpdate", wifiObj);
        }
    }

    private void scanBluetooth() {
        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.startScan(new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    JSObject btObj = new JSObject();
                    btObj.put("deviceName", result.getDevice().getName());
                    btObj.put("rssi", result.getRssi());
                    notifyListeners("bluetoothSignalUpdate", btObj);
                }

                @Override
                public void onScanFailed(int errorCode) {
                    JSObject errorObj = new JSObject();
                    errorObj.put("error", "Bluetooth scan failed: " + errorCode);
                    notifyListeners("bluetoothScanError", errorObj);
                }
            });
        }
    }

    private void scanCellTowers() {
        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
        if (cellInfos != null) {
            for (CellInfo cellInfo : cellInfos) {
                CellSignalStrength strength = cellInfo.getCellSignalStrength();
                JSObject cellObj = new JSObject();
                cellObj.put("signalStrength", strength.getDbm());
                notifyListeners("cellSignalUpdate", cellObj);
            }
        }
    }

    @PluginMethod
public void stopScan(PluginCall call) {
    // Stop Bluetooth Scan
    if (bluetoothLeScanner != null) {
        bluetoothLeScanner.stopScan(new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {}

            @Override
            public void onScanFailed(int errorCode) {}
        });
    }

    // Resolve the call
    call.resolve();
}

@PluginMethod
public void startWifiRssiMonitor(PluginCall call) {
    if (wifiManager != null) {
        JSObject wifiObj = new JSObject();
        wifiObj.put("rssi", wifiManager.getConnectionInfo().getRssi());
        notifyListeners("wifiRssiUpdate", wifiObj);
    }
    call.resolve();
}


    @Override
    public void onSensorChanged(SensorEvent event) {}

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
}
