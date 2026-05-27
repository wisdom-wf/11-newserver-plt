#!/bin/bash
# ============================================================================
# 智慧养老平台 - 前端可靠部署脚本 v9（稳定版）
# ============================================================================
set -e

FRONTEND_DIR="/Users/works/my-projects/11-newserver-plt/dingfeng-work"
SERVER="ubuntu@43.153.213.134"
: "${DEPLOY_PASSWORD:?请先设置 DEPLOY_PASSWORD 环境变量}"
export SSHPASS="$DEPLOY_PASSWORD"
REMOTE_WEB_DIR="/var/www/jxy"

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
log_info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

cd "$FRONTEND_DIR"

# 1. 清理 + 构建
log_info "=== 清理 + 构建 ==="
rm -rf dist
npm run build 2>&1 | tail -3
[ -d "dist/assets" ] && [ -f "dist/index.html" ] || { log_error "构建失败"; exit 1; }
log_info "构建完成，JS 文件数: $(ls dist/assets/*.js | wc -l | tr -d ' ')"

# 2. 记录构建信息
BUILD_TIME=$(grep -o 'content="202[^"]*"' dist/index.html | tr -d '"' | grep -v '^/' | head -1)
INDEX_JS=$(grep -o 'index-[^.]*\.js' dist/index.html | head -1)
SAMPLE_JS=$(ls dist/assets/*.js | grep -v "index-" | grep -v "runtime" | grep -v "vue" | grep -v "css" | head -1)
SAMPLE_NAME=$(basename "$SAMPLE_JS")
log_info "build time: ${BUILD_TIME}"
log_info "index.js:   ${INDEX_JS}"

# 3. 打包
TEMP_TAR="/tmp/jxy_deploy_$(date +%s).tar.gz"
cd dist
COPYFILE_DISABLE=1 tar cfz "$TEMP_TAR" . 2>/dev/null
cd ..
log_info "打包完成: $(du -sh $TEMP_TAR | cut -f1)"

# 4. 上传
log_info "=== 上传 ==="
sshpass -e scp -o StrictHostKeyChecking=no "$TEMP_TAR" "${SERVER}:/tmp/jxy_new.tar.gz" 2>&1
rm -f "$TEMP_TAR"
log_info "上传完成"

# 5. 远程部署 - 简单命令序列，不用 heredoc
log_info "=== 远程部署 ==="

# 5.1 解压到临时目录
sshpass -e ssh -o StrictHostKeyChecking=no "$SERVER" "sudo rm -rf /tmp/jxy_extract && sudo mkdir -p /tmp/jxy_extract && sudo tar xfz /tmp/jxy_new.tar.gz -C /tmp/jxy_extract 2>/dev/null && sudo ls /tmp/jxy_extract/" 2>&1

# 5.2 验证解压结果
DEPLOYED_INDEX=$(sshpass -e ssh -o StrictHostKeyChecking=no "$SERVER" "sudo ls /tmp/jxy_extract/assets/index-*.js 2>/dev/null | head -1 | xargs basename" 2>&1)
if [ -z "$DEPLOYED_INDEX" ]; then
  log_error "远程解压失败：index.js 不存在"
  exit 1
fi
log_info "远程解压成功: ${DEPLOYED_INDEX}"

# 5.3 强制覆盖 webroot
sshpass -e ssh -o StrictHostKeyChecking=no "$SERVER" "sudo rm -rf ${REMOTE_WEB_DIR}/assets && sudo cp -r /tmp/jxy_extract/assets ${REMOTE_WEB_DIR}/ && sudo cp /tmp/jxy_extract/index.html ${REMOTE_WEB_DIR}/ && sudo rm -rf /tmp/jxy_extract /tmp/jxy_new.tar.gz" 2>&1
log_info "文件覆盖完成"

# 6. 验证
log_info "=== 验证 ==="

REMOTE_BUILD_TIME=$(curl -s "https://wisdomdance.cn/jxy/index.html" | grep -o 'content="202[^"]*"' | tr -d '"' | grep -v '^/' | head -1)
if [ "${BUILD_TIME}" = "${REMOTE_BUILD_TIME}" ]; then
  log_info "✓ buildTime 一致: ${REMOTE_BUILD_TIME}"
else
  log_error "✗ buildTime 不一致！本地=${BUILD_TIME} 服务器=${REMOTE_BUILD_TIME}"
  exit 1
fi

REMOTE_INDEX_SIZE=$(curl -s -I "https://wisdomdance.cn/jxy/assets/${INDEX_JS}" | grep -i "^content-length" | awk '{print $2}' | tr -d '\r\n')
if [ -n "$REMOTE_INDEX_SIZE" ] && [ "$REMOTE_INDEX_SIZE" != "0" ]; then
  log_info "✓ index.js 非空: ${REMOTE_INDEX_SIZE} bytes"
else
  log_error "✗ index.js 异常: content-length=${REMOTE_INDEX_SIZE}"
  exit 1
fi

REMOTE_SAMPLE_SIZE=$(curl -s -I "https://wisdomdance.cn/jxy/assets/${SAMPLE_NAME}" | grep -i "^content-length" | awk '{print $2}' | tr -d '\r\n')
[ -n "$REMOTE_SAMPLE_SIZE" ] && [ "$REMOTE_SAMPLE_SIZE" != "0" ] && log_info "✓ sample: ${SAMPLE_NAME} (${REMOTE_SAMPLE_SIZE} bytes)"

echo ""
log_info "========================================"
log_info "  部署完成 & 验证通过"
log_info "========================================"
log_info "访问地址: https://wisdomdance.cn/jxy/home"
log_info "强制刷新: Ctrl+Shift+R"
log_info "构建时间: ${BUILD_TIME}"
echo ""
exit 0
