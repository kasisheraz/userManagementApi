# SIMPLE METHOD: Import SQL via Cloud Console

## Step 1: Upload SQL File to Cloud Storage

1. Go to Cloud Storage: https://console.cloud.google.com/storage/browser?project=project-07a61357-b791-4255-a9e
2. Click "CREATE BUCKET" (if you don't have one) or select an existing bucket
3. If creating new bucket:
   - Name: `fincore-sql-imports` (or any name you want)
   - Location: `europe-west2` (same region as database)
   - Click "CREATE"
4. Click "UPLOAD FILES"
5. Select `complete-entity-schema.sql` from your computer
6. Wait for upload to complete
7. Click on the uploaded file
8. Click the three dots (...) → "Copy gsutil URI"
   - It will look like: `gs://fincore-sql-imports/complete-entity-schema.sql`

## Step 2: Import to Cloud SQL

1. Go to Cloud SQL: https://console.cloud.google.com/sql/instances/fincore-npe-db?project=project-07a61357-b791-4255-a9e
2. Click the **"IMPORT"** button at the top
3. In the Import dialog:
   - **Browse**: Click and select the SQL file you just uploaded (or paste the gsutil URI)
   - **Database**: Select or type `fincore_db`
   - **User** (optional): Leave as default or select `fincore_app`
4. Click **"IMPORT"** button
5. Wait for the import to complete (you'll see a notification)

## Step 3: Test

Once import completes, go back to your terminal and test:

```bash
curl https://fincore-npe-api-994490239798.europe-west2.run.app/actuator/health
```

Then test authentication in Postman!

---

## Alternative: Direct SQL Import via Cloud Console

If you don't want to use Cloud Storage:

1. Open the SQL file in a text editor
2. Copy ALL the contents
3. Try this link: https://console.cloud.google.com/sql/instances/fincore-npe-db/import?project=project-07a61357-b791-4255-a9e
4. See if there's an option to paste SQL directly

Which method do you want to try first?
