#!/bin/sh
set -e

echo "Starting fincore API with Cloud SQL Proxy..."

# If CLOUDSQL_INSTANCE is set, start the proxy
if [ -n "$CLOUDSQL_INSTANCE" ]; then
  echo "Starting Cloud SQL Proxy for $CLOUDSQL_INSTANCE..."
  /cloud_sql_proxy -instances=$CLOUDSQL_INSTANCE=tcp:3306 &
  PROXY_PID=$!
  
  # Wait for proxy to be ready (up to 60 seconds)
  echo "Waiting for Cloud SQL Proxy to be ready..."
  for i in $(seq 1 30); do
    if nc -z localhost 3306 2>/dev/null; then
      echo "✓ Cloud SQL Proxy is ready after $((i*2)) seconds!"
      break
    fi
    echo "Attempt $i/30: Proxy not ready yet, waiting..."
    sleep 2
  done
  
  # Final check
  if ! nc -z localhost 3306 2>/dev/null; then
    echo "ERROR: Cloud SQL Proxy failed to start after 60 seconds"
    kill $PROXY_PID 2>/dev/null || true
    exit 1
  fi
  
  echo "Cloud SQL Proxy started with PID $PROXY_PID"
fi

echo "Starting Spring Boot application..."
exec java -jar /app/app.jar "$@"
