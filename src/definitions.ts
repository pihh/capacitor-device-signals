export interface SignalTriangulationPlugin {
  startScan(): Promise<void>;
  stopScan(): Promise<void>;
  addListener(
    eventName: 'wifiScanResult' | 'bluetoothScanResult' | 'cellSignalResult' | 'sensorData',
    listenerFunc: (data: any) => void
  ): Promise<void>;
  removeAllListeners(): Promise<void>;
}
