
package com.signals.pihh;


import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
//import android.net.wifi.ScanResult as WifiScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.util.Log;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import androidx.annotation.RequiresApi;
import androidx.annotation.RequiresPermission;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import java.util.List;
/*
permissions = {
@Permission(alias = "wifi", strings = { Manifest.permission.ACCESS_FINE_LOCATION }),
@Permission(alias = "bluetooth", strings = {
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
})
    }



@CapacitorPlugin(
        name = "SignalTriangulationPlugin",
        permissions = {
                @Permission(
                        alias = "location",
                        strings = {
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                        }
                ),
                @Permission(
                        alias = "bluetooth",
                        strings = {
                                android.Manifest.permission.BLUETOOTH_SCAN,
                                android.Manifest.permission.BLUETOOTH_CONNECT
                        }
                ),
                @Permission(
                        alias = "wifi",
                        strings = {
                                android.Manifest.permission.ACCESS_WIFI_STATE
                        }
                ),
                @Permission(
                        alias = "phone",
                        strings = {
                                android.Manifest.permission.READ_PHONE_STATE
                        }
                )
        }
)
@CapacitorPlugin(name = "SignalTriangulationPlugin", permissions = {
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.BLUETOOTH_SCAN,
    Manifest.permission.BLUETOOTH_CONNECT,
    Manifest.permission.ACCESS_WIFI_STATE,
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.READ_PHONE_STATE
})

permissions = {
@Permission(alias = "wifi", strings = { Manifest.permission.ACCESS_FINE_LOCATION }),
@Permission(alias = "bluetooth", strings = {
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
})
*/

@CapacitorPlugin(
        name = "SignalTriangulationPlugin",
        permissions = {
                @Permission(
                        alias = "location",
                        strings = {
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                        }
                ),
                @Permission(
                        alias = "bluetooth",
                        strings = {
                                android.Manifest.permission.BLUETOOTH_SCAN,
                                android.Manifest.permission.BLUETOOTH_CONNECT
                        }
                ),
                @Permission(
                        alias = "wifi",
                        strings = {
                                android.Manifest.permission.ACCESS_WIFI_STATE
                        }
                ),
                @Permission(
                        alias = "phone",
                        strings = {
                                android.Manifest.permission.READ_PHONE_STATE
                        }
                )
        }
)
public class SignalTriangulationPlugin extends Plugin implements SensorEventListener {

    private WifiManager wifiManager;
    private BluetoothAdapter bluetoothAdapter;
    private TelephonyManager telephonyManager;
    private SensorManager sensorManager;
    private Sensor magnetometer, accelerometer, lightSensor;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void load() {
        Context context = getContext();
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN})
    @PluginMethod
    public void startScan(PluginCall call) {
        if (!hasRequiredPermissions()) {
            requestAllPermissions(call, "permissionsCallback");
        } else {
            performScan(call);
        }

    }
    @RequiresPermission(allOf = {Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.ACCESS_FINE_LOCATION})
    private void performScan(PluginCall call) {
        scanWifi();
        scanBluetooth();
        scanCellTowers();
        startSensors();
        call.resolve();
    }

    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.BLUETOOTH_SCAN})
    @PermissionCallback
    private void permissionsCallback(PluginCall call) {
        if (hasRequiredPermissions()) {
            performScan(call);
        } else {
            call.reject("Required permissions not granted.");
        }
    }

    private void scanWifi() {
        if (wifiManager != null) {
            getContext().registerReceiver(new BroadcastReceiver() {
                @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION})
                @Override
                public void onReceive(Context context, Intent intent) {
                    @SuppressLint("MissingPermission") List<android.net.wifi.ScanResult> results = wifiManager.getScanResults();
                    for (android.net.wifi.ScanResult scanResult : results) {
                        JSObject wifiObj = new JSObject();
                        wifiObj.put("SSID", scanResult.SSID);
                        wifiObj.put("BSSID", scanResult.BSSID);
                        wifiObj.put("level", scanResult.level);
                        wifiObj.put("frequency", scanResult.frequency);
                        wifiObj.put("capabilities", scanResult.capabilities);
                        notifyListeners("wifiScanResult", wifiObj);
                    }
                }
            }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            wifiManager.startScan();
        }
    }
 
    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    private void scanBluetooth() {
        if (bluetoothAdapter != null && bluetoothAdapter.getBluetoothLeScanner() != null) {
            bluetoothAdapter.getBluetoothLeScanner().startScan(new ScanCallback() {
                @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    BluetoothDevice device = result.getDevice();
                    JSObject btObj = new JSObject();
                    btObj.put("name", device.getName());
                    btObj.put("address", device.getAddress());
                    btObj.put("rssi", result.getRssi());
                    notifyListeners("bluetoothScanResult", btObj);
                }
            });
        }
    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private void scanCellTowers() {
        List<CellInfo> cellInfos = telephonyManager.getAllCellInfo();
        if (cellInfos != null) {
            for (CellInfo info : cellInfos) {
                JSObject cellObj = new JSObject();
                CellSignalStrength strength = null;
                if (info instanceof CellInfoLte) {
                    strength = ((CellInfoLte) info).getCellSignalStrength();
                    cellObj.put("type", "LTE");
                } else if (info instanceof CellInfoGsm) {
                    strength = ((CellInfoGsm) info).getCellSignalStrength();
                    cellObj.put("type", "GSM");
                } else if (info instanceof CellInfoWcdma) {
                    strength = ((CellInfoWcdma) info).getCellSignalStrength();
                    cellObj.put("type", "WCDMA");
                }
                if (strength != null) {
                    cellObj.put("signalStrength", strength.getDbm());
                }
                notifyListeners("cellSignalResult", cellObj);
            }
        }
    }

    private void startSensors() {
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void stopSensors() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        JSObject sensorObj = new JSObject();
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            sensorObj.put("type", "magnetometer");
            sensorObj.put("x", event.values[0]);
            sensorObj.put("y", event.values[1]);
            sensorObj.put("z", event.values[2]);
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            sensorObj.put("type", "accelerometer");
            sensorObj.put("x", event.values[0]);
            sensorObj.put("y", event.values[1]);
            sensorObj.put("z", event.values[2]);
        } else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            sensorObj.put("type", "light");
            sensorObj.put("value", event.values[0]);
        }
        notifyListeners("sensorData", sensorObj);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    @PluginMethod
    public void stopScan(PluginCall call) {
        if (bluetoothAdapter != null && bluetoothAdapter.getBluetoothLeScanner() != null) {
            bluetoothAdapter.getBluetoothLeScanner().stopScan(new ScanCallback() {});
        }
        
        stopSensors();
        call.resolve();
    }
}
