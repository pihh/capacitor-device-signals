import { WebPlugin } from '@capacitor/core';

import type { SignalTriangulationPlugin } from './definitions';

export class SignalTriangulationWeb extends WebPlugin implements SignalTriangulationPlugin {
  async startScan(): Promise<void> {
    throw this.unsupportedFeature('startScan');
  }

  async stopScan(): Promise<void> {
    throw this.unsupportedFeature('stopScan');
  }

  async startWifiRssiMonitor(): Promise<{ rssi: number; timestamp: number }> {
    throw this.unsupportedFeature('startWifiRssiMonitor');
  }

  async stopWifiRssiMonitor(): Promise<void> {
    throw this.unsupportedFeature('stopWifiRssiMonitor');
  }

  addListener(): any {
    throw this.unsupportedFeature('addListener');
  }

  removeAllListeners(): Promise<void> {
    return Promise.resolve();
  }

  private unsupportedFeature(method: string): Error {
    return new Error(`${method} is not supported on web.`);
  }
}
