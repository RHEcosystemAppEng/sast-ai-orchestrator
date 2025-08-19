#!/bin/bash

# Test script to trigger batch processing with simplified parameters

# --- Configuration ---
# The URL where your application is running
BASE_URL="http://localhost:8080/api/v1"

# The Google Sheets URL from the original trigger script
# GOOGLE_SHEETS_URL="https://docs.google.com/spreadsheets/d/1GcJg8aHfpEGxrPbb2gYD-CadwoxAWEB91Er5q0RpOuE/edit?usp=sharing"
GOOGLE_SHEETS_URL="https://docs.google.com/spreadsheets/d/1GcJg8aHfpEGxrPbb2gYD-CadwoxAWEB91Er5q0RpOuE/edit?usp=sharing"

# --- Main ---
echo "🚀 Triggering batch processing pipeline with default settings..."

# Send the POST request using curl with the new parameter
curl -s -X POST \
  "${BASE_URL}/job-batches" \
  -H "Content-Type: application/json" \
  -d '{
    "batchGoogleSheetUrl": "'"${GOOGLE_SHEETS_URL}"'",
    "submittedBy": "test-script@example.com"
  }'

# Add a newline for cleaner terminal output
echo ""
echo "✅ Batch pipeline triggered with default settings"
echo "   All jobs will use default LLM settings and inferred parameters from NVR"
echo "   Check the application logs and Tekton pipeline for the job processing"

# Also test with true for comparison
# echo ""
# echo "🚀 Triggering batch processing pipeline with useKnownFalsePositiveFile=true (for comparison)..."

# curl -s -X POST \
#   "${BASE_URL}/job-batches" \
#   -H "Content-Type: application/json" \
#   -d '{
#     "batchGoogleSheetUrl": "'"${GOOGLE_SHEETS_URL}"'",
#     "submittedBy": "test-script@example.com",
#     "useKnownFalsePositiveFile": true
#   }'

# echo ""
# echo "✅ Batch pipeline triggered with useKnownFalsePositiveFile=true"

# Test without the parameter (should default to true)
# echo ""
# echo "🚀 Triggering batch processing pipeline without the parameter (should default to true)..."

# curl -s -X POST \
#   "${BASE_URL}/job-batches" \
#   -H "Content-Type: application/json" \
#   -d '{
#     "batchGoogleSheetUrl": "'"${GOOGLE_SHEETS_URL}"'",
#     "submittedBy": "test-script@example.com"
#   }'

# echo ""
# echo "✅ Batch pipeline triggered without parameter (should default to true)"
# echo ""
echo "🎯 Test Summary:"
echo "   - Batch 1: useKnownFalsePositiveFile=false (skips known false positives)"
echo "   - Batch 2: useKnownFalsePositiveFile=true (uses known false positives)" 
echo "   - Batch 3: no parameter (defaults to true)"
echo ""
echo "📋 To verify the implementation works:"
echo "   1. Check the orchestrator logs for the batch processing"
echo "   2. Verify Tekton pipeline runs receive USE_KNOWN_FALSE_POSITIVE_FILE=false/true"
echo "   3. Check that the workflow container gets the correct environment variable" 