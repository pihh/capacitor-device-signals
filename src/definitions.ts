export interface SignalTriangulationPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
