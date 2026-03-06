@echo off
echo ========================================
echo Cloud Run Service Information
echo ========================================
echo.

echo === Current Environment Variables ===
gcloud run services describe fincore-npe-api --region=europe-west2 --format="table(spec.template.spec.containers[0].env[].name, spec.template.spec.containers[0].env[].value)"
echo.

echo === Current Image ===
gcloud run services describe fincore-npe-api --region=europe-west2 --format="value(spec.template.spec.containers[0].image)"
echo.

echo === Service Account ===
gcloud run services describe fincore-npe-api --region=europe-west2 --format="value(spec.template.spec.serviceAccountName)"
echo.

echo === Health Probes ===
gcloud run services describe fincore-npe-api --region=europe-west2 --format="yaml(spec.template.spec.containers[0].startupProbe, spec.template.spec.containers[0].livenessProbe)"
echo.

echo === Recent Logs (Last 20 lines) ===
gcloud logging read "resource.type=cloud_run_revision AND resource.labels.service_name=fincore-npe-api" --limit=20 --format="table(timestamp, textPayload)" --order="~timestamp"
echo.

echo Done!
