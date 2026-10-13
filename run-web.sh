#!/bin/bash
while true; do
  if ! pgrep -f "node /app/applet/server.js" > /dev/null; then
    echo "[$(date)] Starting Gangstream AI web server..." >> /tmp/gangstream.log
    node /app/applet/server.js >> /tmp/gangstream.log 2>&1
  fi
  sleep 3
done
