import { describe, expect, it } from "vitest";
import { SAMPLE_CSV, detectAnomalies, parseCsv } from "./index";

describe("parseCsv", () => {
  it("parses timestamp and value columns", () => {
    expect(parseCsv("timestamp,value\n2026-01-01T00:00:00Z,10")).toEqual([
      { timestamp: "2026-01-01T00:00:00Z", value: 10 },
    ]);
  });

  it("rejects a missing timestamp column", () => {
    expect(() => parseCsv("time,value\n2026-01-01T00:00:00Z,10")).toThrow(
      /timestamp/,
    );
  });

  it("rejects a non-numeric value", () => {
    expect(() => parseCsv("timestamp,value\n2026-01-01T00:00:00Z,nope")).toThrow(
      /Invalid value/,
    );
  });

  it("rejects an empty value instead of treating it as zero", () => {
    expect(() => parseCsv("timestamp,value\n2026-01-01T00:00:00Z,")).toThrow(
      /Invalid value/,
    );
  });
});

describe("detectAnomalies", () => {
  it("flags the outlier", () => {
    const rows = parseCsv(
      "timestamp,value\n2026-01-01T00:00:00Z,10\n2026-01-01T00:01:00Z,10\n2026-01-01T00:02:00Z,10\n2026-01-01T00:03:00Z,100",
    );
    const result = detectAnomalies(rows, 1.5);

    expect(result.filter((row) => row.isAnomaly)).toHaveLength(1);
    expect(result.at(-1)?.value).toBe(100);
  });

  it("uses 1.5 as the default threshold on the sample", () => {
    const result = detectAnomalies(parseCsv(SAMPLE_CSV));
    expect(result.filter((row) => row.isAnomaly)).toHaveLength(2);
  });

  it("returns an empty list for empty input", () => {
    expect(detectAnomalies([])).toEqual([]);
  });

  it("does not flag anything when the standard deviation is zero", () => {
    const rows = parseCsv(
      "timestamp,value\n2026-01-01T00:00:00Z,5\n2026-01-01T00:01:00Z,5",
    );

    expect(detectAnomalies(rows).some((row) => row.isAnomaly)).toBe(false);
  });
});
