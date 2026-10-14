#!/bin/bash
# run_pages.sh <code> : validate translated site pages, build, check, commit, push
set -e
code=$1
cd "$(dirname "$0")"
python3 pages_tool.py validate "$code"
python3 build.py >/dev/null
python3 check.py
cd ..
git add -A
git commit -qm "Site pages: add $code translation of home, about, contact, privacy and terms

Co-Authored-By: Claude Sonnet 5.5 <noreply@anthropic.com>
Claude-Session: https://claude.ai/code/session_01CLvGehfYbqx4iszFqUnXQJ"
git push -q origin main 2>&1 | tail -1
git log --oneline | head -1
