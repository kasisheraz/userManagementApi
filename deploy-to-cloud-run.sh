#!/bin/bash
set -e

# Cloud Run Deployment Script for User Management API
# This script handles building and deploying the application to Google Cloud Run

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_ID="${GCP_PROJECT_ID}"
SERVICE_NAME="${CLOUD_RUN_SERVICE_NAME:-user-management-api}"
REGION="${CLOUD_RUN_REGION:-us-central1}"
IMAGE_NAME="${IMAGE_NAME:-user-management-api}"
DB_INSTANCE="${DB_INSTANCE}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD}"
DB_NAME="${DB_NAME:-my_auth_db}"
JWT_SECRET="${JWT_SECRET}"

# Functions
print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
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

check_prerequisites() {
    print_header "Checking Prerequisites"
    
    # Check if gcloud is installed
    if ! command -v gcloud &> /dev/null; then
        print_error "gcloud CLI is not installed. Please install it first."
    fi
    print_success "gcloud CLI found"
    
    # Check if docker is installed
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed. Please install it first."
    fi
    print_success "Docker found"
    
    # Check if Maven is installed
    if ! command -v mvn &> /dev/null; then
        print_error "Maven is not installed. Please install it first."
    fi
    print_success "Maven found"
    
    # Check if variables are set
    if [ -z "$PROJECT_ID" ]; then
        print_error "GCP_PROJECT_ID environment variable is not set"
    fi
    print_success "GCP Project ID: $PROJECT_ID"
    
    if [ -z "$DB_INSTANCE" ]; then
        print_error "DB_INSTANCE environment variable is not set"
    fi
    print_success "Database Instance: $DB_INSTANCE"
}

authenticate_gcp() {
    print_header "Authenticating with GCP"
    gcloud auth application-default login
    gcloud config set project "$PROJECT_ID"
    print_success "Authenticated with GCP"
}

build_application() {
    print_header "Building Application"
    mvn clean package -DskipTests -q
    if [ $? -ne 0 ]; then
        print_error "Maven build failed"
    fi
    print_success "Application built successfully"
}

build_docker_image() {
    print_header "Building Docker Image"
    
    IMAGE_TAG="gcr.io/${PROJECT_ID}/${IMAGE_NAME}:latest"
    
    docker build -t "$IMAGE_TAG" .
    if [ $? -ne 0 ]; then
        print_error "Docker build failed"
    fi
    print_success "Docker image built: $IMAGE_TAG"
    
    echo "$IMAGE_TAG"
}

push_to_gcr() {
    print_header "Pushing Image to Google Container Registry"
    
    IMAGE_TAG="$1"
    
    docker push "$IMAGE_TAG"
    if [ $? -ne 0 ]; then
        print_error "Failed to push image to GCR"
    fi
    print_success "Image pushed to GCR"
}

deploy_to_cloud_run() {
    print_header "Deploying to Cloud Run"
    
    IMAGE_TAG="$1"
    
    gcloud run deploy "$SERVICE_NAME" \
        --image "$IMAGE_TAG" \
        --platform managed \
        --region "$REGION" \
        --allow-unauthenticated \
        --set-env-vars="SPRING_PROFILES_ACTIVE=gcp,DB_USER=${DB_USER},DB_NAME=${DB_NAME},JWT_SECRET=${JWT_SECRET},LOG_LEVEL=INFO" \
        --add-cloudsql-instances="${PROJECT_ID}:${REGION}:${DB_INSTANCE}" \
        --service-account="${SERVICE_NAME}-sa@${PROJECT_ID}.iam.gserviceaccount.com" \
        --memory 512Mi \
        --cpu 1 \
        --max-instances 10 \
        --min-instances 1 \
        --timeout 3600 \
        --port 8080
    
    if [ $? -ne 0 ]; then
        print_error "Failed to deploy to Cloud Run"
    fi
    print_success "Application deployed to Cloud Run"
}

get_service_url() {
    print_header "Service Information"
    
    SERVICE_URL=$(gcloud run services describe "$SERVICE_NAME" \
        --platform managed \
        --region "$REGION" \
        --format='value(status.url)')
    
    print_success "Service URL: $SERVICE_URL"
    echo "$SERVICE_URL"
}

test_deployment() {
    print_header "Testing Deployment"
    
    SERVICE_URL="$1"
    
    # Test health endpoint
    echo "Testing health endpoint..."
    HEALTH_RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" "${SERVICE_URL}/actuator/health")
    
    if [ "$HEALTH_RESPONSE" = "200" ]; then
        print_success "Health check passed"
    else
        print_error "Health check failed with status code: $HEALTH_RESPONSE"
    fi
}

# Main execution
main() {
    print_header "Starting Cloud Run Deployment"
    
    check_prerequisites
    authenticate_gcp
    build_application
    
    IMAGE_TAG=$(build_docker_image)
    push_to_gcr "$IMAGE_TAG"
    deploy_to_cloud_run "$IMAGE_TAG"
    
    SERVICE_URL=$(get_service_url)
    test_deployment "$SERVICE_URL"
    
    print_header "Deployment Complete!"
    print_success "Application is now running at: $SERVICE_URL"
}

# Run main function
main "$@"
