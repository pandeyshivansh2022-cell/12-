#!/bin/bash
while true; do
  if ! pgrep -f "server.js" > /dev/null; then
    echo "[$(date)] Starting Gangstream AI web server..." >> /tmp/gangstream.log
    cd /app/applet && node server.js >> /tmp/gangstream.log 2>&1
  fi
  sleep 2
done
