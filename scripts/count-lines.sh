#!/usr/bin/env bash
# Count Kotlin lines per source set for the "shared vs platform-specific" demo point.
# Excludes build outputs, resources and blank lines; tests are reported separately.
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel 2>/dev/null)" || { echo "❌ Not a git repository"; exit 2; }
cd "$ROOT"

count_dir() {
    local dir="$1"
    if [ ! -d "$dir" ]; then
        echo 0
        return
    fi
    # awk 'NF' drops blank lines; find handles a missing/empty tree without xargs.
    find "$dir" -type f -name '*.kt' -exec awk 'NF' {} + | wc -l | tr -d ' '
}

COMMON_MAIN="$(count_dir shared/src/commonMain/kotlin)"
JVM_SHARED="$(count_dir shared/src/jvmSharedMain/kotlin)"
JVM_MAIN="$(count_dir shared/src/jvmMain/kotlin)"
ANDROID_MAIN="$(count_dir shared/src/androidMain/kotlin)"
DESKTOP_APP="$(count_dir desktopApp/src/main/kotlin)"
ANDROID_APP="$(count_dir androidApp/src/main/kotlin)"

SHARED=$((COMMON_MAIN + JVM_SHARED))
SPECIFIC=$((JVM_MAIN + ANDROID_MAIN + DESKTOP_APP + ANDROID_APP))
PRODUCTION=$((SHARED + SPECIFIC))
if [ "$PRODUCTION" -eq 0 ]; then
    PERCENT="0.0"
else
    PERCENT="$(awk -v s="$SHARED" -v t="$PRODUCTION" 'BEGIN { printf "%.1f", s * 100 / t }')"
fi

TESTS=0
for dir in \
    shared/src/commonTest/kotlin \
    shared/src/jvmTest/kotlin \
    shared/src/androidHostTest/kotlin \
    shared/src/androidDeviceTest/kotlin \
    androidApp/src/androidTest/kotlin; do
    TESTS=$((TESTS + $(count_dir "$dir")))
done

printf '%-34s %8s\n' "Source set" "Lines"
printf '%-34s %8s\n' "----------------------------------" "--------"
printf '%-34s %8s\n' "commonMain" "$COMMON_MAIN"
printf '%-34s %8s\n' "jvmSharedMain" "$JVM_SHARED"
printf '%-34s %8s\n' "jvmMain (desktop only)" "$JVM_MAIN"
printf '%-34s %8s\n' "androidMain" "$ANDROID_MAIN"
printf '%-34s %8s\n' "desktopApp" "$DESKTOP_APP"
printf '%-34s %8s\n' "androidApp" "$ANDROID_APP"
printf '%-34s %8s\n' "----------------------------------" "--------"
printf '%-34s %8s\n' "Shared (common + jvmShared)" "$SHARED"
printf '%-34s %8s\n' "Platform-specific" "$SPECIFIC"
printf '%-34s %8s\n' "Production total" "$PRODUCTION"
printf '%-34s %8s\n' "Tests (excluded)" "$TESTS"
printf '\nShared: %s / %s lines = %s%%\n' "$SHARED" "$PRODUCTION" "$PERCENT"
