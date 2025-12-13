#!/bin/bash
# GCP Project Setup Script for Cloud Run Deployment
# This script automates the GCP infrastructure setup

set -e

# Color codes
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Configuration
PROJECT_ID="${1:-}"
REGION="${2:-us-central1}"
SERVICE_NAME="user-management-api"
DB_INSTANCE="${SERVICE_NAME}-db"
DB_NAME="my_auth_db"
DB_USER="root"

print_section() {
    echo -e "\n${BLUE}========================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}========================================${NC}\n"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
    exit 1
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Validate inputs
if [ -z "$PROJECT_ID" ]; then
    print_error "Usage: ./setup-gcp-infrastructure.sh <PROJECT_ID> [REGION]"
fi

print_section "GCP Infrastructure Setup"
echo "Project ID: $PROJECT_ID"
echo "Region: $REGION"
echo "Database Instance: $DB_INSTANCE"
echo "Database Name: $DB_NAME"
echo "Service Name: $SERVICE_NAME"

# Confirm
read -p "Continue with setup? (y/n) " -n 1 -r
echo
if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    exit 1
fi

# Check gcloud CLI
if ! command -v gcloud &> /dev/null; then
    print_error "gcloud CLI not found. Please install it first."
fi

print_section "Authenticating with GCP"
gcloud auth login
gcloud config set project "$PROJECT_ID"
print_success "Authenticated to project $PROJECT_ID"

print_section "Enabling Required APIs"
echo "Enabling Cloud Run API..."
gcloud services enable run.googleapis.com --quiet
print_success "Cloud Run API enabled"

echo "Enabling Cloud SQL Admin API..."
gcloud services enable sqladmin.googleapis.com --quiet
print_success "Cloud SQL Admin API enabled"

echo "Enabling Container Registry API..."
gcloud services enable containerregistry.googleapis.com --quiet
print_success "Container Registry API enabled"

echo "Enabling Cloud Build API..."
gcloud services enable cloudbuild.googleapis.com --quiet
print_success "Cloud Build API enabled"

echo "Enabling Cloud Logging API..."
gcloud services enable logging.googleapis.com --quiet
print_success "Cloud Logging API enabled"

echo "Enabling Cloud Monitoring API..."
gcloud services enable monitoring.googleapis.com --quiet
print_success "Cloud Monitoring API enabled"

print_section "Creating Cloud SQL Instance"
echo "Creating MySQL instance: $DB_INSTANCE..."

gcloud sql instances create "$DB_INSTANCE" \
    --database-version=MYSQL_8_0 \
    --tier=db-f1-micro \
    --region="$REGION" \
    --availability-type=regional \
    --enable-bin-log \
    --backup-start-time=03:00 \
    --quiet || print_warning "Cloud SQL instance may already exist"

print_success "Cloud SQL instance created/verified"

print_section "Creating Database"
gcloud sql databases create "$DB_NAME" \
    --instance="$DB_INSTANCE" \
    --quiet || print_warning "Database may already exist"

print_success "Database created/verified"

print_section "Setting Database Root Password"
read -s -p "Enter password for database user '$DB_USER': " DB_PASSWORD
echo
read -s -p "Confirm password: " DB_PASSWORD_CONFIRM
echo

if [ "$DB_PASSWORD" != "$DB_PASSWORD_CONFIRM" ]; then
    print_error "Passwords do not match"
fi

gcloud sql users set-password "$DB_USER" \
    --instance="$DB_INSTANCE" \
    --password="$DB_PASSWORD" \
    --quiet

print_success "Database user password set"

print_section "Creating Service Account"
echo "Creating service account: ${SERVICE_NAME}-sa..."

gcloud iam service-accounts create "${SERVICE_NAME}-sa" \
    --display-name="User Management API Service Account" \
    --quiet || print_warning "Service account may already exist"

print_success "Service account created/verified"

print_section "Granting IAM Roles"
echo "Granting Cloud SQL Client role..."
gcloud projects add-iam-policy-binding "$PROJECT_ID" \
    --member="serviceAccount:${SERVICE_NAME}-sa@${PROJECT_ID}.iam.gserviceaccount.com" \
    --role="roles/cloudsql.client" \
    --quiet
print_success "Cloud SQL Client role granted"

echo "Granting Cloud Run Service Agent role..."
gcloud projects add-iam-policy-binding "$PROJECT_ID" \
    --member="serviceAccount:${SERVICE_NAME}-sa@${PROJECT_ID}.iam.gserviceaccount.com" \
    --role="roles/run.invoker" \
    --quiet
print_success "Cloud Run Invoker role granted"

echo "Granting Cloud Logging Log Writer role..."
gcloud projects add-iam-policy-binding "$PROJECT_ID" \
    --member="serviceAccount:${SERVICE_NAME}-sa@${PROJECT_ID}.iam.gserviceaccount.com" \
    --role="roles/logging.logWriter" \
    --quiet
print_success "Cloud Logging Log Writer role granted"

print_section "Configuration Summary"
cat > gcp-config.env << EOF
# GCP Configuration for Cloud Run Deployment
export GCP_PROJECT_ID="$PROJECT_ID"
export CLOUD_RUN_SERVICE_NAME="$SERVICE_NAME"
export CLOUD_RUN_REGION="$REGION"
export DB_INSTANCE="$DB_INSTANCE"
export DB_USER="$DB_USER"
export DB_PASSWORD="$DB_PASSWORD"
export DB_NAME="$DB_NAME"
export JWT_SECRET="404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
export SERVICE_ACCOUNT="${SERVICE_NAME}-sa@${PROJECT_ID}.iam.gserviceaccount.com"
EOF

print_success "Configuration saved to gcp-config.env"

echo ""
echo "Cloud SQL Instance Details:"
gcloud sql instances describe "$DB_INSTANCE" --format="table(name,databaseVersion,currentDiskSize,settings.tier,region)"

print_section "Setup Complete!"
echo "To use this configuration in your deployment scripts, run:"
echo -e "${BLUE}source ./gcp-config.env${NC}"
echo ""
echo "Or manually set these environment variables:"
echo "  GCP_PROJECT_ID=$PROJECT_ID"
echo "  CLOUD_RUN_SERVICE_NAME=$SERVICE_NAME"
echo "  CLOUD_RUN_REGION=$REGION"
echo "  DB_INSTANCE=$DB_INSTANCE"
echo "  DB_USER=$DB_USER"
echo "  DB_NAME=$DB_NAME"
echo ""
echo "Next steps:"
echo "1. Store the database password securely"
echo "2. Review CLOUD_RUN_DEPLOYMENT.md for deployment instructions"
echo "3. Run ./deploy-to-cloud-run.sh when ready to deploy"
