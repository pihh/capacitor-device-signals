export interface SignalTriangulationPlugin {
  // Existing methods for WiFi and Bluetooth monitoring
  startScan(): Promise<void>;
  stopScan(): Promise<void>;

  // Methods for monitoring WiFi RSSI (Received Signal Strength Indicator)
  startWifiRssiMonitor(): Promise<{ rssi: number; timestamp: number }>;
  stopWifiRssiMonitor(): Promise<void>;

  // Methods for sensor monitoring
  startAccelerometerMonitor(): Promise<void>;
  stopAccelerometerMonitor(): Promise<void>;

  startGyroscopeMonitor(): Promise<void>;
  stopGyroscopeMonitor(): Promise<void>;

  startMagnetometerMonitor(): Promise<void>;
  stopMagnetometerMonitor(): Promise<void>;

  startGravityMonitor(): Promise<void>;
  stopGravityMonitor(): Promise<void>;

  startAmbientLightMonitor(): Promise<void>;
  stopAmbientLightMonitor(): Promise<void>;

  startTemperatureMonitor(): Promise<void>;
  stopTemperatureMonitor(): Promise<void>;

  startHumidityMonitor(): Promise<void>;
  stopHumidityMonitor(): Promise<void>;

  // Listener management
  addListener(
    eventName:
      | 'wifiScanResult'
      | 'bluetoothScanResult'
      | 'cellSignalResult'
      | 'sensorData'
      | 'wifiRssiUpdate'
      | 'accelerometerUpdate'
      | 'gyroscopeUpdate'
      | 'magnetometerUpdate'
      | 'gravityUpdate'
      | 'ambientLightUpdate'
      | 'temperatureUpdate'
      | 'humidityUpdate',
    listenerFunc: (data: any) => void,
  ): Promise<void>;

  // Removes all listeners
  removeAllListeners(): Promise<void>;
}

declare global {
  interface Magnetometer {
    start(): Promise<void>;
    stop(): Promise<void>;
    addEventListener(event: string, callback: (event: any) => void): void;
  }

  interface TemperatureSensor {
    start(): Promise<void>;
    stop(): Promise<void>;
    addEventListener(event: string, callback: (event: Event) => void): void;
  }

  interface HumiditySensor {
    start(): Promise<void>;
    stop(): Promise<void>;
    addEventListener(event: string, callback: (event: Event) => void): void;
  }

  // You can also add other sensors in the same way, for example:
  interface AmbientLightSensor {
    start(): Promise<void>;
    stop(): Promise<void>;
    addEventListener(event: string, callback: (event: Event) => void): void;
  }
}
