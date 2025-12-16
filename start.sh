#!/bin/sh
set -e

echo "Starting fincore API with Cloud SQL Proxy..."

# If CLOUDSQL_INSTANCE is set, start the proxy
if [ -n "$CLOUDSQL_INSTANCE" ]; then
  echo "Starting Cloud SQL Proxy for $CLOUDSQL_INSTANCE..."
  /cloud_sql_proxy -instances=$CLOUDSQL_INSTANCE=tcp:3306 &
  PROXY_PID=$!
  
  # Wait for proxy to be ready
  echo "Waiting for Cloud SQL Proxy to be ready..."
  for i in 1 2 3 4 5 6 7 8 9 10; do
    if nc -z localhost 3306 2>/dev/null; then
      echo "Cloud SQL Proxy is ready!"
      break
    fi
    echo "Attempt $i: Proxy not ready yet, waiting..."
    sleep 2
  done
  echo "Cloud SQL Proxy started with PID $PROXY_PID"
fi

echo "Starting Spring Boot application..."
exec java -jar /app/app.jar "$@"
