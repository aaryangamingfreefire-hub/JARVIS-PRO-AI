#!/data/data/com.termux/files/usr/bin/bash
set -e
cd "$(dirname "$0")"
if [ -z "$GEMINI_API_KEY" ]; then
  echo "Set your Google AI Studio key first:"
  echo "export GEMINI_API_KEY='YOUR_KEY'"
  exit 1
fi
node server.js
