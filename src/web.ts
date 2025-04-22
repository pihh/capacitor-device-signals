import { WebPlugin } from '@capacitor/core';

import type { SignalTriangulationPlugin } from './definitions';

export class SignalTriangulationWeb extends WebPlugin implements SignalTriangulationPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
