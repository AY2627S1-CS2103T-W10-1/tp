#!/usr/bin/env bash
# Run inside xvfb-run so focus and minimization tests have a real window manager.
set -euo pipefail

wm_log=$(mktemp)
openbox --sm-disable > "$wm_log" 2>&1 &
wm_pid=$!

cleanup() {
    kill "$wm_pid" 2>/dev/null || true
    wait "$wm_pid" 2>/dev/null || true
    rm -f "$wm_log"
}
trap cleanup EXIT

# Xvfb provides a display, but a window manager must acknowledge window state requests.
wm_ready=false
for ((attempt = 0; attempt < 50; attempt++)); do
    if xprop -root _NET_SUPPORTING_WM_CHECK 2>/dev/null | grep -q 'window id #'; then
        wm_ready=true
        break
    fi
    if ! kill -0 "$wm_pid" 2>/dev/null; then
        break
    fi
    sleep 0.1
done

if [[ "$wm_ready" != true ]]; then
    echo "The test window manager failed to start." >&2
    cat "$wm_log" >&2
    exit 1
fi

./gradlew check coverage "$@"
