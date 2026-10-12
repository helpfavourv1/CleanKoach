#!/bin/bash
# run_lang.sh <code> [folder]  : apply translation, build, OG image, checks, commit, push
set -e
code=$1; folder=${2:-$1}
cd "$(dirname "$0")"
python3 tr_tool.py apply "$code"
python3 check_lang.py "$code"
python3 build.py >/dev/null
python3 og.py "$code" | tail -1
python3 check.py
cd ..
git add -A
git commit -qm "Guides: add $code translation of best-phone-cleaner-app-android

Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01CLvGehfYbqx4iszFqUnXQJ"
git push -q origin main 2>&1 | tail -1
git log --oneline | head -1
