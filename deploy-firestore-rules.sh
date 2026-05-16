#!/bin/bash

# Firestore Rules Deployment Helper Script
# This script helps validate and deploy Firestore security rules

echo "=== Firestore Rules Deployment Helper ==="
echo ""

# Check if firebase-tools is installed
if ! command -v firebase &> /dev/null; then
    echo "❌ firebase-tools not found. Install with:"
    echo "   npm install -g firebase-tools"
    echo ""
    exit 1
fi

echo "✅ firebase-tools is installed"
echo ""

# Check if user is logged in
firebase projects:list > /dev/null 2>&1
if [ $? -ne 0 ]; then
    echo "❌ Not logged into Firebase. Running: firebase login"
    firebase login
fi

echo ""
echo "=== Available Projects ==="
firebase projects:list
echo ""

# Check if firestore.rules file exists
if [ ! -f "firestore.rules" ]; then
    echo "❌ firestore.rules not found in current directory"
    echo "   Current directory: $(pwd)"
    exit 1
fi

echo "✅ firestore.rules found"
echo ""

# Show current rules
echo "=== Current Rules (Preview) ==="
head -20 firestore.rules
echo "..."
echo ""

# Get current project
PROJECT=$(firebase projects:list | grep "BookUp\|book-up" | awk '{print $1}')

if [ -z "$PROJECT" ]; then
    echo "⚠️  Could not auto-detect project. Available projects:"
    firebase projects:list
    echo ""
    read -p "Enter your Firebase project ID: " PROJECT
fi

echo "Using project: $PROJECT"
echo ""

read -p "Deploy Firestore rules to $PROJECT? (y/n) " -n 1 -r
echo ""

if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo ""
    echo "🚀 Deploying rules..."
    firebase deploy --only firestore:rules --project "$PROJECT"
    
    if [ $? -eq 0 ]; then
        echo ""
        echo "✅ Rules deployed successfully!"
        echo ""
        echo "=== What's Now Enabled ==="
        echo "✓ Authenticated users can submit reviews"
        echo "✓ Authenticated users can create chat channels"
        echo "✓ Participants can access their chats"
        echo "✓ Users can only edit/delete their own content"
        echo ""
        echo "=== Next Steps ==="
        echo "1. Test review submission in the app"
        echo "2. Test chat message sending"
        echo "3. Verify unauthenticated users see permission error"
        echo ""
    else
        echo ""
        echo "❌ Deployment failed. Check the error above."
        exit 1
    fi
else
    echo "Deployment cancelled."
fi
