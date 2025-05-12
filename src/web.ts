import { WebPlugin } from '@capacitor/core';
import type { SignalTriangulationPlugin } from './definitions';

export class SignalTriangulationWeb extends WebPlugin implements SignalTriangulationPlugin {
  private listeners: { [event: string]: Array<(data: any) => void> } = {};

  // Start WiFi scan (not supported on the web)
  async startScan(): Promise<void> {
    throw new Error('startScan() is not supported on the web.');
  }

  // Stop WiFi scan (not supported on the web)
  async stopScan(): Promise<void> {
    throw new Error('stopScan() is not supported on the web.');
  }

  // Start and Stop Wifi RSSI Monitoring
  async startWifiRssiMonitor(): Promise<{ rssi: number; timestamp: number }> {
    return Promise.resolve({ rssi: 0, timestamp: Date.now() });
  }

  async stopWifiRssiMonitor(): Promise<void> {
    return Promise.resolve();
  }

  // Start Accelerometer monitoring (Check if supported)
  async startAccelerometerMonitor(): Promise<void> {
    if ('DeviceMotionEvent' in window) {
      window.addEventListener('devicemotion', this.handleAccelerometerEvent.bind(this));
    } else {
      throw new Error('Accelerometer is not supported on this platform.');
    }
  }

  async stopAccelerometerMonitor(): Promise<void> {
    if ('DeviceMotionEvent' in window) {
      window.removeEventListener('devicemotion', this.handleAccelerometerEvent.bind(this));
    }
  }

  // Start Gyroscope monitoring (Check if supported)
  async startGyroscopeMonitor(): Promise<void> {
    if ('Gyroscope' in window) {
      const gyroscope = new Gyroscope();
      gyroscope.addEventListener('reading', this.handleGyroscopeEvent.bind(this));
      await gyroscope.start();
    } else {
      throw new Error('Gyroscope is not supported on this platform.');
    }
  }

  async stopGyroscopeMonitor(): Promise<void> {
    if ('Gyroscope' in window) {
      const gyroscope = new Gyroscope();
      await gyroscope.stop();
    }
  }

  // Start Magnetometer monitoring (Check if supported)
  async startMagnetometerMonitor(): Promise<void> {
    if ('Magnetometer' in window) {
      const magnetometer = new Magnetometer();
      magnetometer.addEventListener('reading', this.handleMagnetometerEvent.bind(this));
      await magnetometer.start();
    } else {
      throw new Error('Magnetometer is not supported on this platform.');
    }
  }

  async stopMagnetometerMonitor(): Promise<void> {
    if ('Magnetometer' in window) {
      const magnetometer = new Magnetometer();
      await magnetometer.stop();
    }
  }

  // Start Gravity monitoring (Check if supported)
  async startGravityMonitor(): Promise<void> {
    if ('DeviceMotionEvent' in window) {
      window.addEventListener('devicemotion', this.handleGravityEvent.bind(this));
    } else {
      throw new Error('Gravity sensor is not supported on this platform.');
    }
  }

  async stopGravityMonitor(): Promise<void> {
    if ('DeviceMotionEvent' in window) {
      window.removeEventListener('devicemotion', this.handleGravityEvent.bind(this));
    }
  }

  // Start Ambient Light monitoring (Check if supported)
  async startAmbientLightMonitor(): Promise<void> {
    if ('AmbientLightSensor' in window) {
      const lightSensor = new AmbientLightSensor();
      lightSensor.addEventListener('reading', this.handleAmbientLightEvent.bind(this));
      await lightSensor.start();
    } else {
      throw new Error('Ambient Light sensor is not supported on this platform.');
    }
  }

  async stopAmbientLightMonitor(): Promise<void> {
    if ('AmbientLightSensor' in window) {
      const lightSensor = new AmbientLightSensor();
      await lightSensor.stop();
    }
  }

  // Start Temperature monitoring (Check if supported)
  async startTemperatureMonitor(): Promise<void> {
    if ('TemperatureSensor' in window) {
      const temperatureSensor = new TemperatureSensor();
      temperatureSensor.addEventListener('reading', this.handleTemperatureEvent.bind(this));
      await temperatureSensor.start();
    } else {
      throw new Error('Temperature sensor is not supported on this platform.');
    }
  }

  async stopTemperatureMonitor(): Promise<void> {
    if ('TemperatureSensor' in window) {
      const temperatureSensor = new TemperatureSensor();
      await temperatureSensor.stop();
    }
  }

  // Start Humidity monitoring (Check if supported)
  async startHumidityMonitor(): Promise<void> {
    if ('RelativeHumiditySensor' in window) {
      const humiditySensor = new RelativeHumiditySensor();
      humiditySensor.addEventListener('reading', this.handleHumidityEvent.bind(this));
      await humiditySensor.start();
    } else {
      throw new Error('Humidity sensor is not supported on this platform.');
    }
  }

  async stopHumidityMonitor(): Promise<void> {
    if ('RelativeHumiditySensor' in window) {
      const humiditySensor = new RelativeHumiditySensor();
      await humiditySensor.stop();
    }
  }

  // Add a listener for a specific event
  addListener(eventName: string, listenerFunc: (data: any) => void): Promise<void> {
    if (!this.listeners[eventName]) {
      this.listeners[eventName] = [];
    }
    this.listeners[eventName]?.push(listenerFunc);
    return Promise.resolve();
  }

  // Remove all listeners
  removeAllListeners(): Promise<void> {
    this.listeners = {};
    return Promise.resolve();
  }

  // Emit event to all listeners
  private emit(eventName: string, data: any): void {
    if (this.listeners[eventName]) {
      this.listeners[eventName].forEach((listener) => listener(data));
    }
  }

  // Sensor event handlers
  private handleAccelerometerEvent(event: DeviceMotionEvent): void {
    const acceleration = event.acceleration;
    const data = {
      x: acceleration?.x || 0,
      y: acceleration?.y || 0,
      z: acceleration?.z || 0,
    };
    this.emit('accelerometerUpdate', data);
  }

  private handleGyroscopeEvent(event: GyroscopeReadingEvent): void {
    const data = {
      x: event.x,
      y: event.y,
      z: event.z,
    };
    this.emit('gyroscopeUpdate', data);
  }

  private handleMagnetometerEvent(event: MagnetometerReadingEvent): void {
    const data = {
      x: event.magneticField.x,
      y: event.magneticField.y,
      z: event.magneticField.z,
    };
    this.emit('magnetometerUpdate', data);
  }

  private handleGravityEvent(event: DeviceMotionEvent): void {
    const accelerationIncludingGravity = event.accelerationIncludingGravity;
    const data = {
      x: accelerationIncludingGravity?.x || 0,
      y: accelerationIncludingGravity?.y || 0,
      z: accelerationIncludingGravity?.z || 0,
    };
    this.emit('gravityUpdate', data);
  }

  private handleAmbientLightEvent(event: Event): void {
    const sensor = event.target as AmbientLightSensor;
    const data = { lux: sensor.illuminance };
    this.emit('ambientLightUpdate', data);
  }

  private handleTemperatureEvent(event: Event): void {
    const sensor = event.target as TemperatureSensor;
    const data = { temperature: sensor.temperature };
    this.emit('temperatureUpdate', data);
  }

  private handleHumidityEvent(event: Event): void {
    const sensor = event.target as RelativeHumiditySensor;
    const data = { humidity: sensor.relativeHumidity };
    this.emit('humidityUpdate', data);
  }
}
