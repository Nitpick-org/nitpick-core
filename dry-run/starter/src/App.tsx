import { useMemo, useState } from "react";
import { detectAnomalies, parseCsv, SAMPLE_CSV } from "./modules/anomaly";

export default function App() {
  const [csv, setCsv] = useState(SAMPLE_CSV);
  const [submitted, setSubmitted] = useState(false);

  const { rows, error } = useMemo(() => {
    try {
      return { rows: detectAnomalies(parseCsv(csv), 1.5), error: "" };
    } catch (caught) {
      return {
        rows: [],
        error: caught instanceof Error ? caught.message : "Invalid CSV",
      };
    }
  }, [csv]);

  const anomalyCount = rows.filter((row) => row.isAnomaly).length;

  return (
    <main className="page">
      <header className="hero">
        <p className="eyebrow">Pulse · dry run</p>
        <h1>Find the spike before it pages you.</h1>
        <p className="lede">
          Paste a CSV of metrics. Pulse flags the outliers before they become an
          incident.
        </p>
      </header>

      <section className="panel">
        <label htmlFor="csv">Metrics CSV</label>
        <textarea
          id="csv"
          value={csv}
          onChange={(event) => setCsv(event.target.value)}
          rows={10}
          spellCheck={false}
        />
        <div className="actions">
          <button onClick={() => setSubmitted(true)}>Analyze</button>
          <button
            className="ghost"
            onClick={() => {
              setCsv(SAMPLE_CSV);
              setSubmitted(false);
            }}
          >
            Reset sample
          </button>
        </div>

        {error ? (
          <p className="error" role="alert">
            {error}
          </p>
        ) : null}

        {submitted && !error ? (
          <div className="results">
            <p className="count" data-testid="anomaly-count">
              {anomalyCount} anomalies
            </p>
            <table>
              <thead>
                <tr>
                  <th>Timestamp</th>
                  <th>Value</th>
                  <th>z-score</th>
                  <th>Flag</th>
                </tr>
              </thead>
              <tbody>
                {rows.map((row, index) => (
                  <tr
                    key={`${row.timestamp}-${index}`}
                    className={row.isAnomaly ? "anomaly" : ""}
                  >
                    <td>{row.timestamp}</td>
                    <td>{row.value}</td>
                    <td>{row.zScore.toFixed(2)}</td>
                    <td>{row.isAnomaly ? "⚠️" : "—"}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}
      </section>
    </main>
  );
}
