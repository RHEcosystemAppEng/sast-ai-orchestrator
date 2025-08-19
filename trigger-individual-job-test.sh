#!/bin/bash

# Test script to trigger individual job processing with simplified parameters and optional useKnownFalsePositiveFile

# --- Configuration ---
# The URL where your application is running
BASE_URL="http://localhost:8080/api/v1"

# --- Main ---
echo "🚀 Creating individual job with default settings (submittedBy defaults to 'unknown')..."

# Send the POST request using curl for individual job creation
curl -s -X POST \
  "${BASE_URL}/jobs/simple" \
  -H "Content-Type: application/json" \
  -d '{
    "packageNvr": "systemd-257-9.el10",
    "inputSourceUrl": "https://docs.google.com/spreadsheets/d/19wIC8ktql02LOPMzepcRaaJr7D0o2tZVMf3UVvzL3sw/edit?usp=sharing"
  }'

echo ""
echo "✅ Individual job created with default settings"
echo "   submittedBy defaults to 'unknown', useKnownFalsePositiveFile defaults to true"

# Test with submittedBy and useKnownFalsePositiveFile explicitly set
echo ""
echo "🚀 Creating second individual job with submittedBy='test-user' and useKnownFalsePositiveFile=false..."

curl -s -X POST \
  "${BASE_URL}/jobs/simple" \
  -H "Content-Type: application/json" \
  -d '{
    "packageNvr": "httpd-2.4.57-5.el9",
    "inputSourceUrl": "https://docs.google.com/spreadsheets/d/19wIC8ktql02LOPMzepcRaaJr7D0o2tZVMf3UVvzL3sw/edit?usp=sharing",
    "submittedBy": "test-user",
    "useKnownFalsePositiveFile": false
  }'

echo ""
echo "✅ Second individual job created with custom submittedBy and useKnownFalsePositiveFile=false"

echo ""
echo "🎯 Test Summary:"
echo "   - Job 1: systemd-257-9.el10 (submittedBy='unknown', useKnownFalsePositiveFile=true)"
echo "   - Job 2: httpd-2.4.57-5.el9 (submittedBy='test-user', useKnownFalsePositiveFile=false)"
echo ""
echo "📋 To verify the implementation works:"
echo "   1. Check that both jobs were created successfully"
echo "   2. Verify parameters were correctly inferred from NVR"
echo "   3. Confirm Job 1 has submittedBy='unknown' and Job 2 has submittedBy='test-user'"
echo "   4. Check Tekton pipeline parameters: USE_KNOWN_FALSE_POSITIVE_FILE should be true/false respectively" 