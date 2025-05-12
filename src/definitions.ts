/* export interface SignalTriangulationPlugin {
  startScan(): Promise<void>;
  stopScan(): Promise<void>;
  startWifiRssiMonitor():Promise<any>;
stopWifiRssiMonitor():Promise<any>;
  addListener(
    eventName: 'wifiScanResult' | 'bluetoothScanResult' | 'cellSignalResult' | 'sensorData' | "wifiRssiUpdate",
    listenerFunc: (data: any) => void
  ): Promise<void>;
  removeAllListeners(): Promise<void>;
}
 */
export interface SignalTriangulationPlugin {
  startScan(): Promise<void>;
  stopScan(): Promise<void>;
  startWifiRssiMonitor(): Promise<{ rssi: number; timestamp: number }>;
  stopWifiRssiMonitor(): Promise<void>;
  addListener(
    eventName: 'wifiScanResult' | 'bluetoothScanResult' | 'cellSignalResult' | 'sensorData' | 'wifiRssiUpdate',
    listenerFunc: (data: any) => void,
  ): Promise<void>;
  removeAllListeners(): Promise<void>;
}
