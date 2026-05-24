#!/bin/bash
# 养老平台前端部署脚本
# 原则：只追加不删除，保留旧 chunk 文件避免浏览器缓存 404
set -e

SERVER="ubuntu@43.153.213.134"
PASS="w00135950F"
DIST_DIR="/Users/works/my-projects/11-newserver-plt/dingfeng-work/dist"

echo "[1/2] 构建前端..."
cd /Users/works/my-projects/11-newserver-plt/dingfeng-work
npm run build 2>&1 | tail -3

echo "[2/2] 部署到服务器（追加模式）..."
cd "$DIST_DIR"
tar cf - . | sshpass -p "$PASS" ssh -o StrictHostKeyChecking=no $SERVER '
  sudo mkdir -p /var/www/jxy
  cd /tmp && rm -rf jxy_dist && tar xf -
  # 只复制新文件，不删除旧文件
  sudo cp -rn jxy_dist/* /var/www/jxy/ 2>/dev/null || sudo cp -r jxy_dist/* /var/www/jxy/
  echo "部署完成: $(ls /var/www/jxy/assets/*.js 2>/dev/null | wc -l) 个JS文件"
'
echo "✅ 前端部署完成"
