# 第2期：服务人员管理

## 视频信息
- **主题**: 服务人员管理
- **时长**: 85秒（待调整）
- **核心价值**: 权益保障，安心服务

## 场景结构

| 场景 | 时长 | 内容 | 字幕 | 品牌植入 |
|------|------|------|------|---------|
| Scene 1 | 8秒 | 标题页 + 人文引入 | "服务人员的辛苦，丁峰养老看在眼里" | 文案提及 |
| Scene 2 | 12秒 | 人员档案管理 | "实名认证 · 健康档案 · 专业证书" | 水印 |
| Scene 3 | 12秒 | 服务范围匹配 | "技能评估 · 服务区域 · 智能派单" | 水印 |
| Scene 4 | 15秒 | 权益保障展示 | "工时统计 · 薪资核算 · 保险保障" | 文案提及 |
| Scene 5 | 15秒 | 工作满意度 | "工作量 · 奖励机制 · 职业成长" | 水印 |
| Scene 6 | 12秒 | 安全措施 | "紧急联系人 · 实时定位 · 风险预警" | 水印 |
| Scene 7 | 10秒 | 总结 + CTA | "丁峰养老 · 保障权益 · 安心服务" | 文案强调 |
| Scene 8 | 8秒 | 品牌结束 | "丁峰养老 · 与你同行" | 品牌logo |

## 制作命令

### 语音合成
```bash
# 合成音频
bun run /Users/wisdom/.claude/skills/minimax-speech/scripts/tts.ts \
  "$(cat narration.txt)" \
  --voice "Chinese (Mandarin)_News_Anchor" \
  --speed 0.85 \
  --output audio/narration_full.mp3

# 验证时长
ffprobe -v error -show_entries format=duration \
  -of default=noprint_wrappers=1:nokey=1 audio/narration_full.mp3
```

### 渲染视频
```bash
# 预览
npx hyperframes preview

# 渲染
npx hyperframes render --output "平台功能介绍系列视频002.mp4"
```

## 设计要点
- **人文风格**: 使用握手 👋、温暖emoji
- **权益卡片**: 带有保护属性的图标设计
- **成长路径**: 上升箭头 ⬆️ + 奖杯 🏆 表达职业发展
- **安全防护**: 盾牌 🛡️ + 位置 📍 + 警告 ⚠️ 组合图标
- **品牌水印**: 右上角"🏠 丁峰养老"半透明水印