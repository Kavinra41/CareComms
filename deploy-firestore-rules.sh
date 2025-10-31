#!/bin/bash

# Deploy Firestore rules
# Make sure you have Firebase CLI installed: npm install -g firebase-tools
# And you're logged in: firebase login

echo "Deploying Firestore security rules..."

# For development (more permissive)
echo "Deploying development rules (permissive)..."
firebase deploy --only firestore:rules --project your-project-id

# Uncomment the line below to deploy production rules instead
# firebase deploy --only firestore:rules --project your-project-id

echo "Firestore rules deployed successfully!"