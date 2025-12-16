#!/bin/sh
set -e

echo "Starting fincore API with Cloud SQL Proxy..."

# If CLOUDSQL_INSTANCE is set, start the proxy
if [ -n "$CLOUDSQL_INSTANCE" ]; then
  echo "Starting Cloud SQL Proxy for $CLOUDSQL_INSTANCE..."
  /cloud_sql_proxy -instances=$CLOUDSQL_INSTANCE=tcp:3306 &
  PROXY_PID=$!
  sleep 3
  echo "Cloud SQL Proxy started with PID $PROXY_PID"
fi

echo "Starting Spring Boot application..."
exec java -jar /app/app.jar "$@"
