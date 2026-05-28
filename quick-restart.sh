#!/bin/bash
echo "[1/3] Stopping old process via systemctl..."
# 使用 systemctl 停止服务，避免 systemd 自动重启导致的多实例问题
systemctl stop jxy 2>/dev/null || true
sleep 2

echo "[2/3] Killing any remaining Java processes..."
# 确保所有残留的 Java 进程都被杀光，防止多实例问题
pkill -9 -f 'elderly-care-server' 2>/dev/null || true
sleep 1

echo "[3/3] Starting new process..."
systemctl start jxy
echo "New PID: $(pgrep -f 'elderly-care-server-1.0.0.jar')"

echo "Waiting for startup..."
for i in $(seq 1 20); do
  if curl -s http://localhost:8080/public/appointment >/dev/null 2>&1; then
    echo "Backend is UP after ${i}s"
    exit 0
  fi
  sleep 1
done
echo "WARNING: Backend did not respond in 20s, check jxy.log"
systemctl status jxy --no-pager | head -10
