#!/bin/bash

set -e

echo "=========================================="
echo "  智慧养老系统 - 编译重启脚本"
echo "=========================================="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="/Volumes/works/my-projects/11-newserver-plt"
BACKEND_DIR="$PROJECT_ROOT/elderly-care-server"
FRONTEND_DIR="$PROJECT_ROOT/dingfeng-work"

# 日志目录
LOG_DIR="/tmp/elderly-care-logs"
mkdir -p "$LOG_DIR"
BACKEND_LOG="$LOG_DIR/backend.log"
FRONTEND_LOG="$LOG_DIR/frontend.log"

# 杀掉旧进程
echo -e "${YELLOW}[1/6] 停止旧进程...${NC}"
pkill -f "spring-boot:run" 2>/dev/null || true
pkill -f "java.*elderly-care" 2>/dev/null || true
pkill -f "vite" 2>/dev/null || true
pkill -f "node.*vite" 2>/dev/null || true
sleep 2
echo -e "${GREEN}  旧进程已停止${NC}"
echo ""

# 编译后端
echo -e "${YELLOW}[2/6] 编译后端 (Maven)...${NC}"
cd "$BACKEND_DIR"
mvn clean compile -q
if [ $? -eq 0 ]; then
    echo -e "${GREEN}  后端编译成功${NC}"
else
    echo -e "${RED}  后端编译失败${NC}"
    exit 1
fi
echo ""

# 编译前端
echo -e "${YELLOW}[3/6] 编译前端 (Vite)...${NC}"
cd "$FRONTEND_DIR"
npm run build 2>&1 | grep -E "Build successful|error|Error" || true
if [ -d "$FRONTEND_DIR/dist" ]; then
    echo -e "${GREEN}  前端编译成功${NC}"
else
    echo -e "${RED}  前端编译失败${NC}"
    exit 1
fi
echo ""

# 启动后端
echo -e "${YELLOW}[4/6] 启动后端 (端口 8080)...${NC}"
cd "$BACKEND_DIR"
nohup mvn spring-boot:run > "$BACKEND_LOG" 2>&1 &
BACKEND_PID=$!
echo "  后端进程 PID: $BACKEND_PID"

# 等待后端启动
echo -n "  等待后端启动"
for i in {1..30}; do
    if curl -s http://localhost:8080/api/public/token -X POST > /dev/null 2>&1; then
        echo ""
        echo -e "${GREEN}  后端启动成功${NC}"
        break
    fi
    echo -n "."
    sleep 2
done
echo ""

# 启动前端
echo -e "${YELLOW}[5/6] 启动前端 (端口 9528)...${NC}"
cd "$FRONTEND_DIR"
nohup npm run dev > "$FRONTEND_LOG" 2>&1 &
FRONTEND_PID=$!
echo "  前端进程 PID: $FRONTEND_PID"

# 等待前端启动
echo -n "  等待前端启动"
for i in {1..15}; do
    if curl -s http://localhost:9528 > /dev/null 2>&1; then
        echo ""
        echo -e "${GREEN}  前端启动成功${NC}"
        break
    fi
    echo -n "."
    sleep 2
done
echo ""

# 验证服务
echo -e "${YELLOW}[6/6] 验证服务状态...${NC}"
echo ""

# 检查后端
BACKEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/public/token -X POST 2>/dev/null || echo "000")
if [ "$BACKEND_STATUS" = "200" ]; then
    echo -e "${GREEN}✓${NC} 后端服务:  http://localhost:8080"
else
    echo -e "${RED}✗${NC} 后端服务:  启动失败 (HTTP $BACKEND_STATUS)"
fi

# 检查前端
FRONTEND_STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9528 2>/dev/null || echo "000")
if [ "$FRONTEND_STATUS" = "200" ]; then
    echo -e "${GREEN}✓${NC} 前端服务:  http://localhost:9528"
else
    echo -e "${RED}✗${NC} 前端服务:  启动失败 (HTTP $FRONTEND_STATUS)"
fi

echo ""
echo "=========================================="
echo -e "${GREEN}  启动完成${NC}"
echo "=========================================="
echo ""
echo "  后端日志: $BACKEND_LOG"
echo "  前端日志: $FRONTEND_LOG"
echo ""
echo "  公开API测试:"
echo "    curl -X POST 'http://localhost:8080/api/public/token?type=cockpit'"
echo ""
echo "  公开页面:"
echo "    大屏: http://localhost:9528/public/dashboard"
echo "    移动: http://localhost:9528/public/mobile"
echo ""
