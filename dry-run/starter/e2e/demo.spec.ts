import { expect, test } from "@playwright/test";

test("the demo path: the sample CSV reveals two anomalies", async ({ page }) => {
  await page.goto("/");

  await expect(
    page.getByRole("heading", { name: /spike before it pages you/i }),
  ).toBeVisible();

  await page.getByRole("button", { name: "Analyze" }).click();

  await expect(page.getByTestId("anomaly-count")).toHaveText("2 anomalies");
  await expect(page.locator("tr.anomaly")).toHaveCount(2);
});

test("invalid CSV shows a useful error", async ({ page }) => {
  await page.goto("/");

  await page.locator("#csv").fill("time,value\n2026-01-01T00:00:00Z,10");
  await page.getByRole("button", { name: "Analyze" }).click();

  await expect(page.getByRole("alert")).toContainText(/timestamp/);
});
