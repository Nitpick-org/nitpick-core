#!/usr/bin/env bash
# Verifies the demo path, not just the unit tests.
# Run it at H6, H12, H18, and before the code freeze.
#
# Usage: ./scripts/demo-check.sh
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel)"
exec "$ROOT/scripts/check.sh" --demo
