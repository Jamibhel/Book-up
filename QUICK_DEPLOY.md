# 🚀 QUICK DEPLOYMENT REFERENCE

## Deploy in 3 Steps

### Step 1: Run Deployment
Pick ONE command:
```bash
# EASIEST: Use helper script
./deploy-firestore-rules.sh

# OR: Use Firebase CLI directly
firebase deploy --only firestore:rules

# OR: Manual (Firebase Console)
Visit firebaseconsole.com > Firestore > Rules > Paste & Publish
```

### Step 2: Wait for Confirmation
You'll see:
- ✅ Green checkmark / "Deploy complete"
- ✅ Takes ~30 seconds

### Step 3: Test It
```
Sign in > Tutor Profile > Write Review > Submit
Expected: Success ✅
```

---

## What You Get

✅ Users CAN submit reviews (with 1-5 rating validation)  
✅ Users CAN create/access chats (participants only)  
✅ Unauthenticated users see clear permission error  
✅ Server-side validation (extra security)  

---

## Files Created

- `firestore.rules` — The security rules
- `firebase.json` — Updated config
- `deploy-firestore-rules.sh` — Automated helper (executable)
- `FIRESTORE_RULES_DEPLOYMENT.md` — Full 50+ line guide
- `RULES_DEPLOYMENT_FINAL.md` — This summary

---

## Troubleshoot

**"firebase command not found"**
```bash
npm install -g firebase-tools
firebase login
```

**"Permission denied after deployment"**
- Check user is signed in
- Check UID is in participantIds array
- Check review includes userId == auth.uid

**"Rules didn't update"**
- Wait 1-2 minutes for cache refresh
- Reload app
- Check "Publish successful" message

---

## Full Documentation

Open `FIRESTORE_RULES_DEPLOYMENT.md` for:
- Detailed step-by-step guide
- All test cases (4 different scenarios)
- Security rule breakdown by collection
- Monitoring & troubleshooting guide

---

**Ready? Run: `./deploy-firestore-rules.sh`** 🎉
