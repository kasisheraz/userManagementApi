# EASIEST METHOD: Use Cloud Shell

## Step 1: Open Cloud Shell
1. Look at the very top right of your Google Cloud Console browser window
2. Find the icon that looks like this: **>_** (terminal icon)
3. Click it
4. A terminal window will open at the bottom of your screen

## Step 2: Connect to Database
Copy and paste this command into Cloud Shell:

```bash
gcloud sql connect fincore-npe-db --user=root --database=fincore_db --project=project-07a61357-b791-4255-a9e
```

Press ENTER

## Step 3: When Prompted
- It may ask "Do you want to continue (Y/n)?" - Type: **Y** and press ENTER
- It may ask for password - Get it from Secret Manager or just press ENTER if it connects without password

## Step 4: Run the Diagnostic Query
Once you see `mysql>` prompt, paste this:

```sql
USE fincore_db;
SHOW VARIABLES LIKE 'lower_case_table_names';
SHOW TABLES;
```

Press ENTER after each line.

## Step 5: Copy the Results
Copy everything you see and send it to me.

---

**Alternative if Cloud Shell doesn't work:**
Just tell me and I'll give you a different approach using local MySQL client or phpMyAdmin.
