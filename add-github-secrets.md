# Add GitHub Secrets - Quick Guide

## Required Secrets for MySQL Deployment

Your GitHub Actions are failing because these secrets are missing:

### Add These Secrets:

1. **Go to**: https://github.com/kasisheraz/userManagementApi/settings/secrets/actions

2. **Click**: "New repository secret" button

3. **Add each secret**:

   | Name | Value |
   |------|-------|
   | `DB_PASSWORD` | `FinCore2024Secure` |
   | `CLOUDSQL_INSTANCE` | `project-07a61357-b791-4255-a9e:europe-west2:fincore-npe-db` |

### Verify Existing Secrets Are Present:

These should already exist (if not, add them):

   | Name | Value |
   |------|-------|
   | `GCP_PROJECT_ID` | `project-07a61357-b791-4255-a9e` |
   | `GCP_SA_KEY` | (Your service account JSON key - check existing) |
   | `GCP_SERVICE_ACCOUNT` | `fincore-npe-cloudrun@project-07a61357-b791-4255-a9e.iam.gserviceaccount.com` |

## After Adding Secrets

The next push to `main` will trigger deployment automatically.

Or manually trigger: https://github.com/kasisheraz/userManagementApi/actions
