#!/bin/bash

# GitHub Actions Setup Script
# This script sets up the GCP service account for GitHub Actions CI/CD

set -e

PROJECT_ID="project-07a61357-b791-4255-a9e"
SERVICE_ACCOUNT_NAME="github-actions"
REGION="europe-west2"

echo "🔧 Setting up GitHub Actions CI/CD for Google Cloud Run..."
echo "Project ID: $PROJECT_ID"
echo ""

# Check if gcloud is installed
if ! command -v gcloud &> /dev/null; then
    echo "❌ gcloud CLI not found. Please install it first:"
    echo "   https://cloud.google.com/sdk/docs/install"
    exit 1
fi

# Check if service account already exists
if gcloud iam service-accounts describe $SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com --project=$PROJECT_ID &> /dev/null; then
    echo "✅ Service account already exists: $SERVICE_ACCOUNT_NAME"
else
    echo "📝 Creating service account..."
    gcloud iam service-accounts create $SERVICE_ACCOUNT_NAME \
        --display-name="GitHub Actions CI/CD" \
        --project=$PROJECT_ID
    echo "✅ Service account created"
fi

SERVICE_ACCOUNT_EMAIL="$SERVICE_ACCOUNT_NAME@$PROJECT_ID.iam.gserviceaccount.com"

echo ""
echo "📋 Granting IAM roles to service account..."

# Define required roles
ROLES=(
    "roles/run.admin"
    "roles/storage.admin"
    "roles/cloudsql.client"
    "roles/iam.serviceAccountUser"
    "roles/editor"
)

for role in "${ROLES[@]}"; do
    echo "  - Granting $role..."
    gcloud projects add-iam-policy-binding $PROJECT_ID \
        --member=serviceAccount:$SERVICE_ACCOUNT_EMAIL \
        --role=$role \
        --condition=None \
        --quiet 2>/dev/null || echo "    (already granted or failed)"
done

echo "✅ IAM roles configured"

echo ""
echo "🔑 Creating service account key..."

# Check if key already exists
KEY_FILE="github-actions-key.json"
if [ -f "$KEY_FILE" ]; then
    echo "⚠️  Key file already exists: $KEY_FILE"
    echo "   Do you want to regenerate it? (y/n)"
    read -r response
    if [ "$response" != "y" ]; then
        echo "Skipping key generation"
        echo ""
        echo "📌 Next steps:"
        echo "1. Use the existing $KEY_FILE"
        echo "2. Go to GitHub repository → Settings → Secrets and variables → Actions"
        echo "3. Add secret GCP_SA_KEY with contents of $KEY_FILE"
        exit 0
    fi
fi

# Delete old keys if they exist
echo "Checking for existing keys..."
EXISTING_KEYS=$(gcloud iam service-accounts keys list \
    --iam-account=$SERVICE_ACCOUNT_EMAIL \
    --project=$PROJECT_ID \
    --filter="validAfterTime<2025-12-16T00:00:00Z" \
    --format="value(name)" 2>/dev/null || echo "")

if [ ! -z "$EXISTING_KEYS" ]; then
    echo "Removing old keys..."
    echo "$EXISTING_KEYS" | while read key; do
        gcloud iam service-accounts keys delete $key \
            --iam-account=$SERVICE_ACCOUNT_EMAIL \
            --project=$PROJECT_ID \
            --quiet 2>/dev/null || true
    done
fi

# Create new key
gcloud iam service-accounts keys create $KEY_FILE \
    --iam-account=$SERVICE_ACCOUNT_EMAIL \
    --project=$PROJECT_ID

echo "✅ Service account key created: $KEY_FILE"

echo ""
echo "════════════════════════════════════════════════════════════════"
echo "✅ GitHub Actions setup complete!"
echo "════════════════════════════════════════════════════════════════"
echo ""
echo "📌 Next steps:"
echo ""
echo "1️⃣  Configure GitHub Secrets:"
echo "   Go to: https://github.com/kasisheraz/userManagementApi/settings/secrets/actions"
echo ""
echo "   Add these secrets:"
echo "   - GCP_PROJECT_ID = project-07a61357-b791-4255-a9e"
echo "   - GCP_SA_KEY = (paste contents of $KEY_FILE)"
echo "   - GCP_SERVICE_ACCOUNT = fincore-npe-cloudrun@$PROJECT_ID.iam.gserviceaccount.com"
echo ""
echo "2️⃣  Copy the JSON key file:"
cat $KEY_FILE | jq . 1>/dev/null
echo "   File: $KEY_FILE"
echo "   (This file should be kept secure and not committed to git)"
echo ""
echo "3️⃣  Add .gitignore entry:"
echo "   echo 'github-actions-key.json' >> .gitignore"
echo ""
echo "4️⃣  Push workflow files to GitHub:"
echo "   git add .github/workflows/"
echo "   git commit -m 'Add GitHub Actions CI/CD workflow'"
echo "   git push origin main"
echo ""
echo "5️⃣  Verify workflow runs:"
echo "   Go to: https://github.com/kasisheraz/userManagementApi/actions"
echo ""
echo "For detailed setup instructions, see: GITHUB_ACTIONS_SETUP.md"
echo ""
