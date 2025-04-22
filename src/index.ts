import { registerPlugin } from '@capacitor/core';

import type { SignalTriangulationPlugin } from './definitions';

const SignalTriangulation = registerPlugin<SignalTriangulationPlugin>('SignalTriangulationPlugin', {
  web: () => import('./web').then(m => new m.SignalTriangulationWeb()),
});

export * from './definitions';
export { SignalTriangulation };
