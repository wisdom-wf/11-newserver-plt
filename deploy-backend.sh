#!/bin/bash
# 养老平台后端部署脚本
set -e

SERVER="ubuntu@43.153.213.134"
: "${DEPLOY_PASSWORD:?请先设置 DEPLOY_PASSWORD 环境变量}"
export SSHPASS="$DEPLOY_PASSWORD"
JAR="/Users/works/my-projects/11-newserver-plt/elderly-care-server/target/elderly-care-server-1.0.0.jar"

echo "[1/3] 编译后端..."
cd /Users/works/my-projects/11-newserver-plt/elderly-care-server
mvn clean package -DskipTests -q 2>&1 | tail -3

echo "[2/3] 上传 JAR..."
cd "$(dirname "$JAR")"
tar cf - elderly-care-server-1.0.0.jar | sshpass -e ssh -o StrictHostKeyChecking=no "$SERVER" '
  rm -f /opt/jxy/elderly-care-server-1.0.0.jar
  cd /opt/jxy && tar xf - && echo "JAR OK"
'

echo "[3/3] 重启后端..."
cat /Users/works/my-projects/11-newserver-plt/quick-restart.sh | sshpass -e ssh -o StrictHostKeyChecking=no "$SERVER" 'bash /opt/jxy/quick-restart.sh'

echo "✅ 后端部署完成"
