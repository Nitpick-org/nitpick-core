export type Row = { timestamp: string; value: number };

export type Anomaly = Row & { isAnomaly: boolean; zScore: number };

export function parseCsv(input: string): Row[] {
  const lines = input
    .trim()
    .split(/\r?\n/)
    .filter((line) => line.trim().length > 0);

  if (lines.length === 0) return [];

  const [header, ...rest] = lines;
  const columns = header.split(",").map((column) => column.trim().toLowerCase());
  const timestampIndex = columns.indexOf("timestamp");
  const valueIndex = columns.indexOf("value");

  if (timestampIndex === -1 || valueIndex === -1) {
    throw new Error("CSV must contain timestamp and value columns");
  }

  return rest.map((line, lineIndex) => {
    const cells = line.split(",").map((cell) => cell.trim());
    const rawValue = cells[valueIndex];
    const timestamp = cells[timestampIndex];

    if (!timestamp) {
      throw new Error(`Missing timestamp on line ${lineIndex + 2}`);
    }
    if (rawValue === undefined || rawValue === "") {
      throw new Error(`Invalid value on line ${lineIndex + 2}`);
    }

    const value = Number(rawValue);
    if (!Number.isFinite(value)) {
      throw new Error(`Invalid value on line ${lineIndex + 2}`);
    }

    return { timestamp, value };
  });
}

export function detectAnomalies(rows: Row[], threshold = 2): Anomaly[] {
  if (rows.length === 0) return [];

  const values = rows.map((row) => row.value);
  const mean = values.reduce((sum, value) => sum + value, 0) / values.length;
  const variance =
    values.reduce((sum, value) => sum + (value - mean) ** 2, 0) / values.length;
  const standardDeviation = Math.sqrt(variance);

  return rows.map((row) => {
    const zScore =
      standardDeviation === 0 ? 0 : (row.value - mean) / standardDeviation;

    return {
      ...row,
      isAnomaly: Math.abs(zScore) >= threshold,
      zScore,
    };
  });
}

export const SAMPLE_CSV = `timestamp,value
2026-11-16T10:00:00Z,42
2026-11-16T10:01:00Z,43
2026-11-16T10:02:00Z,41
2026-11-16T10:03:00Z,99
2026-11-16T10:04:00Z,42
2026-11-16T10:05:00Z,40
2026-11-16T10:06:00Z,98
2026-11-16T10:07:00Z,44
2026-11-16T10:08:00Z,43
2026-11-16T10:09:00Z,41`;
