#!/bin/bash
echo "[1/2] Stopping old process..."
kill $(pgrep -f "elderly-care-server-1.0.0.jar") 2>/dev/null
sleep 3
echo "Old process killed"

echo "[2/2] Starting new process..."
cd /opt/jxy
nohup java -Xms256m -Xmx512m -jar elderly-care-server-1.0.0.jar > jxy.log 2>&1 &
echo "New PID: $!"

echo "Waiting for startup..."
for i in $(seq 1 20); do
  if curl -s http://localhost:8080/public/appointment >/dev/null 2>&1; then
    echo "Backend is UP after ${i}s"
    exit 0
  fi
  sleep 1
done
echo "WARNING: Backend did not respond in 20s, check jxy.log"
tail -20 jxy.log
