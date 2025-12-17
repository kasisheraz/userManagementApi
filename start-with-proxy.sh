#!/bin/sh
set -e

echo "Starting Cloud SQL Auth Proxy..."

# Start cloud-sql-proxy in the background
if [ -n "$CLOUDSQL_INSTANCE" ]; then
    echo "Connecting to Cloud SQL instance: $CLOUDSQL_INSTANCE"
    /usr/local/bin/cloud-sql-proxy --port 3306 "$CLOUDSQL_INSTANCE" &
    PROXY_PID=$!
    echo "Cloud SQL Proxy started with PID: $PROXY_PID"
    
    # Wait for proxy to be ready
    echo "Waiting for proxy to be ready..."
    for i in $(seq 1 30); do
        if nc -z localhost 3306 2>/dev/null; then
            echo "✓ Cloud SQL Proxy is ready!"
            break
        fi
        if [ $i -eq 30 ]; then
            echo "✗ Cloud SQL Proxy failed to start after 30 seconds"
            exit 1
        fi
        sleep 1
    done
else
    echo "WARNING: CLOUDSQL_INSTANCE not set, skipping proxy"
fi

echo "Starting Spring Boot application..."
exec java $JAVA_TOOL_OPTIONS -jar /app/app.jar "$@"
