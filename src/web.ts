import { WebPlugin } from '@capacitor/core';

import type { SignalTriangulationPlugin } from './definitions';

export class SignalTriangulationWeb extends WebPlugin implements SignalTriangulationPlugin {
  startScan(): Promise<void> {
    throw new Error('startScan() not supported on web.');
  }

  stopScan(): Promise<void> {
    throw new Error('stopScan() not supported on web.');
  }

  addListener(): any {
    throw new Error('addListener() not supported on web.');
  }

  removeAllListeners(): Promise<void> {
    return Promise.resolve();
  }

  startWifiRssiMonitor(): Promise<any> {
    return Promise.resolve();
  }
stopWifiRssiMonitor(): Promise<any> {
  return Promise.resolve();
}
}
