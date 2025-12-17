#!/bin/sh
set -e

echo "[PROXY] Starting Cloud SQL Auth Proxy..."

# Start cloud-sql-proxy in the background on port 5432 (doesn't require root)
if [ -n "$CLOUDSQL_INSTANCE" ]; then
    echo "[PROXY] Connecting to Cloud SQL instance: $CLOUDSQL_INSTANCE"
    /usr/local/bin/cloud-sql-proxy --port 5432 "$CLOUDSQL_INSTANCE" > /tmp/proxy.log 2>&1 &
    PROXY_PID=$!
    echo "[PROXY] Cloud SQL Proxy started with PID: $PROXY_PID"
    
    # Wait for proxy to be ready
    echo "[PROXY] Waiting for proxy to be ready..."
    sleep 2
    
    for i in $(seq 1 30); do
        if nc -z localhost 5432 2>/dev/null; then
            echo "[PROXY] ✓ Cloud SQL Proxy is ready on port 5432!"
            break
        fi
        if [ $i -eq 30 ]; then
            echo "[PROXY] ✗ Cloud SQL Proxy failed to start after 30 seconds"
            echo "[PROXY] Proxy logs:"
            cat /tmp/proxy.log 2>/dev/null || echo "No logs available"
            exit 1
        fi
        sleep 1
    done
else
    echo "[PROXY] WARNING: CLOUDSQL_INSTANCE not set, skipping proxy"
fi

echo "[APP] Starting Spring Boot application..."
exec java $JAVA_TOOL_OPTIONS -jar /app/app.jar "$@"
