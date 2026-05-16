# Firebase Storage Rules Deployment Guide

This guide explains how to securely give GitHub Actions access to deploy your Firebase Storage rules.

## Quick Summary

You have **two options** to deploy `storage.rules` to Firebase:

1. **Option A: Local Deploy** — Run `firebase deploy` on your machine (fastest, no GitHub setup needed)
2. **Option B: GitHub Actions** — Add a secret to GitHub, then the workflow auto-deploys (recommended for team/CI)

---

## Option A: Deploy Locally (Fastest)

### Step 1: Install Firebase CLI
```bash
npm install -g firebase-tools
```

### Step 2: Authenticate
```bash
firebase login
```
This opens your browser. Sign in with your Google account (same account that created the Firebase project).

### Step 3: Verify Project
```bash
firebase projects:list
firebase use --add <YOUR_PROJECT_ID>
```
Replace `<YOUR_PROJECT_ID>` with your actual Firebase project ID (found in Firebase Console).

### Step 4: Deploy Storage Rules
```bash
cd /Users/user/AndroidStudioProjects/BookUp
firebase deploy --only storage
```

You should see output like:
```
✔ Deploy complete!

Project Console: https://console.firebase.google.com/project/YOUR_PROJECT_ID/overview
```

**Done!** Your `storage.rules` is now live in Firebase.

---

## Option B: GitHub Actions (Recommended for Team/CI)

This option lets GitHub Actions deploy rules automatically when you push changes.

### Step 1: Create a Firebase Service Account

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Select your Firebase project (same one from Firebase Console)
3. In the left sidebar, go to **APIs & Services** → **Credentials**
4. Click **Create Credentials** → **Service Account**
5. Fill in:
   - **Service account name**: `github-ci` (or any name)
   - **Service account ID**: auto-populated
   - **Description**: `GitHub Actions deployment for Firebase Storage rules`
6. Click **Create and Continue**
7. Grant roles (on the next screen):
   - Search for and add: **Firebase Admin** role (or **Cloud Storage Admin** + **Firebase Security Rules Admin** for least privilege)
8. Click **Continue** and then **Done**

### Step 2: Create and Download the JSON Key

1. In the **Credentials** page, find the service account you just created under **Service Accounts**
2. Click on it to open details
3. Go to the **Keys** tab
4. Click **Add Key** → **Create new key** → **JSON**
5. A JSON file downloads automatically (save it somewhere safe, e.g., `~/Downloads/firebase-key.json`)

⚠️ **IMPORTANT**: This JSON file contains your Firebase credentials. **Never commit it to Git** or share it publicly. Treat it like a password.

### Step 3: Add the Secret to GitHub

1. Go to your GitHub repo: https://github.com/Jamibhel/Book-up
2. Click **Settings** (top right)
3. In the left sidebar, click **Secrets and variables** → **Actions**
4. Click **New repository secret**
5. Create two secrets:

   **Secret 1:**
   - **Name**: `FIREBASE_SERVICE_ACCOUNT`
   - **Value**: Open the JSON file you downloaded and copy-paste its entire content into this field
   
   **Secret 2:**
   - **Name**: `FIREBASE_PROJECT_ID`
   - **Value**: Your Firebase project ID (e.g., `bookup-12345` — found in Firebase Console or the JSON file's `project_id` field)

6. Click **Add secret** for each one

✅ **Done!** GitHub now has secure access to deploy. The secrets are encrypted and only visible to workflows.

### Step 4: Trigger the Deployment

#### Option B1: Automatic (on push)
- Every time you push changes to `storage.rules` on the `main` branch, the workflow automatically runs.

#### Option B2: Manual (via GitHub UI)
1. Go to your repo: https://github.com/Jamibhel/Book-up
2. Click **Actions** (top menu)
3. Select **Deploy Firebase Storage Rules** (the workflow)
4. Click **Run workflow** → **Run workflow** button
5. Wait a few seconds; the workflow will start. Click on it to watch logs.

Expected success output:
```
✅ Storage rules deployed successfully!
Rules file: storage.rules
Deployment timestamp: [timestamp]
```

---

## Troubleshooting

### "Permission denied" or "403 Forbidden"
- **Cause**: Service account doesn't have the right role.
- **Fix**: Go to Google Cloud Console → IAM → Edit the service account and add **Firebase Admin** role.

### "Invalid JSON" in GitHub secret
- **Cause**: You copied the JSON incorrectly or added extra characters.
- **Fix**: Download the JSON file again and copy the entire raw content (should start with `{` and end with `}`).

### Workflow fails with "FIREBASE_SERVICE_ACCOUNT not found"
- **Cause**: Secret name is wrong or not added.
- **Fix**: Go to **Settings** → **Secrets and variables** → **Actions** and verify the secret name is exactly `FIREBASE_SERVICE_ACCOUNT`.

### Workflow runs but rules don't deploy
- **Cause**: The workflow completed but rules weren't actually published.
- **Fix**: Check the workflow logs in GitHub Actions. Look for error messages. Common issues:
  - Wrong `FIREBASE_PROJECT_ID`
  - Syntax errors in `storage.rules` (run `firebase rules:test storage` locally to validate)

---

## Next Steps

1. **Choose your deployment option:**
   - **Option A**: Run `firebase deploy --only storage` locally now (takes 30 seconds)
   - **Option B**: Set up GitHub Actions now (takes 5 minutes, then automatic future deploys)

2. **After deployment:**
   - Open Firebase Console → Storage → Rules and verify the new rules are active
   - Run end-to-end test: upload an image/video/audio/document from the chat app and verify it succeeds
   - Monitor Firestore Logs / Realtime Database Rules to see if uploads are now permitted

3. **Send logs if issues persist:**
   - If uploads still fail, run `adb logcat` on device and share error messages
   - If GitHub Action fails, copy the workflow run logs and share (no credentials will be exposed)

---

## Security Notes

- ✅ **Service account JSON** is private — store locally and never commit to Git
- ✅ **GitHub Secrets** are encrypted and only accessible during workflow runs
- ✅ Rotate the service account key periodically (delete old key, create new one)
- ⚠️ **Never** paste the JSON content anywhere except into the GitHub Secrets field

---

## Additional Resources

- [Firebase CLI Docs](https://firebase.google.com/docs/cli)
- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Google Cloud Service Accounts](https://cloud.google.com/docs/authentication/getting-started)
