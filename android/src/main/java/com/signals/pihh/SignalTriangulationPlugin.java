package com.signals.pihh;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.telephony.CellInfo;
import android.telephony.CellSignalStrength;
import android.telephony.TelephonyManager;

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
        registerSensors();
    }

    private void registerSensors() {
        if (sensorManager != null) {
            registerSensor(Sensor.TYPE_ACCELEROMETER);
            registerSensor(Sensor.TYPE_GYROSCOPE);
            registerSensor(Sensor.TYPE_MAGNETIC_FIELD);
            registerSensor(Sensor.TYPE_GRAVITY);
            registerSensor(Sensor.TYPE_LIGHT);
            registerSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
            registerSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        }
    }

    private void registerSensor(int sensorType) {
        Sensor sensor = sensorManager.getDefaultSensor(sensorType);
        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @PluginMethod
    public void startScan(PluginCall call) {
        if (!hasRequiredPermissions()) {
            requestAllPermissions(call, "permissionsCallback");
        } else {
            performScan(call);
        }
    }

    @PluginMethod
    public void stopScan(PluginCall call) {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        call.resolve();
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

    @Override
    public void onSensorChanged(SensorEvent event) {
        JSObject sensorObj = new JSObject();
        sensorObj.put("type", event.sensor.getType());
        sensorObj.put("values", event.values);
        notifyListeners("sensorUpdate", sensorObj);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}


    @PluginMethod
public void startAccelerometerMonitor(PluginCall call) {
    Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    call.resolve();
}

@PluginMethod
public void stopAccelerometerMonitor(PluginCall call) {
    sensorManager.unregisterListener(this);
    call.resolve();
}

@PluginMethod
public void startGyroscopeMonitor(PluginCall call) {
    Sensor gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    call.resolve();
}

@PluginMethod
public void stopGyroscopeMonitor(PluginCall call) {
    sensorManager.unregisterListener(this);
    call.resolve();
}

@PluginMethod
public void startMagnetometerMonitor(PluginCall call) {
    Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    call.resolve();
}

@PluginMethod
public void stopMagnetometerMonitor(PluginCall call) {
    sensorManager.unregisterListener(this);
    call.resolve();
}

@PluginMethod
public void startGravityMonitor(PluginCall call) {
    Sensor gravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
    sensorManager.registerListener(this, gravity, SensorManager.SENSOR_DELAY_NORMAL);
    call.resolve();
}

@PluginMethod
public void stopGravityMonitor(PluginCall call) {
    sensorManager.unregisterListener(this);
    call.resolve();
}

}
