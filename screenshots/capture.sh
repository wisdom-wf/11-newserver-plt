#!/bin/bash
# 批量截图脚本 - 智慧养老服务管理平台各模块
cd /Volumes/works/my-projects/11-newserver-plt

BASE_URL='https://yspt.jinyangtong.cn/#/shecuntong'

# 模块列表：名称
MODULES=(
  "政府监管子系统"
  "养老服务中心子系统"
  "日间照料子系统"
  "门店管理子系统"
  "家床及适老化改造子系统"
  "时间超市子系统"
  "养老评估子系统"
  "服务商子系统"
  "助餐子系统"
  "商品商家子系统"
)

INDEX=(4 5 6 7 8 9 10 11 12 13)

for i in "${!MODULES[@]}"; do
  MODULE="${MODULES[$i]}"
  NUM=$((i + 4))
  FILENAME=$(printf "%02d_%s.png" "$NUM" "$MODULE")
  
  echo "=== 处理: $MODULE -> $FILENAME ==="
  
  # 重新获取快照
  playwright-cli snapshot > /dev/null 2>&1
  sleep 1
  
  # 点击模块（通过文本内容）
  playwright-cli run-code "async page => {
    const el = page.getByText('$MODULE', { exact: true });
    await el.click();
  }" 2>&1
  sleep 3
  
  # 截图
  playwright-cli screenshot --filename="screenshots/$FILENAME" 2>&1
  echo "截图保存: $FILENAME"
  
  # 返回首页
  playwright-cli go-back 2>&1
  sleep 2
done

echo "=== 所有模块截图完成 ==="
