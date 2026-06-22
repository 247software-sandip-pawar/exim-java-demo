#!/usr/bin/env bash
# Stop every service started by run-all.sh.
cd "$(dirname "$0")"
shopt -s nullglob
for pidfile in logs/*.pid; do
  pid="$(cat "$pidfile")"
  if kill "$pid" 2>/dev/null; then
    echo "stopped $(basename "$pidfile" .pid) (pid $pid)"
  fi
  rm -f "$pidfile"
done
echo "done"
