# 第1期：服务商入驻管理

## 视频信息
- **主题**: 服务商入驻管理
- **时长**: 102秒（实际100.5秒音频+2秒结尾停留）
- **核心价值**: 规范入驻，保障服务质量

## 场景结构

| 场景 | 时长 | 内容 | 字幕 | 品牌植入 |
|------|------|------|------|---------|
| Scene 1 | 8秒 | 标题页 + 问题引入 | "谁在提供服务？我们如何放心？" | 水印 |
| Scene 2 | 15秒 | 入驻流程展示 | "严格审核 · 资质认证 · 服务范围确认" | 水印 |
| Scene 3 | 15秒 | 实名认证展示 | "营业执照 · 健康证 · 服务合同 · 三重保障" | 文案提及 |
| Scene 4 | 12秒 | 服务类型配置 | "生活照料 · 康复护理 · 专业匹配" | 水印 |
| Scene 5 | 12秒 | 信用评价体系 | "服务评分 · 动态排名 · 优胜劣汰" | 水印 |
| Scene 6 | 12秒 | 持续监管 | "定期审核 · 实时监督 · 安全第一" | 水印 |
| Scene 7 | 10秒 | 总结 + CTA | "丁峰养老 · 专业服务商 · 安心选择" | 文案强调 |
| Scene 8 | 8秒 | 品牌结束 | "丁峰养老 · 用心服务" | 品牌logo |

## 制作命令

### 语音合成
```bash
# 设置 API Key（如需要）
export MINIMAX_API_KEY="your-api-key"

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
npx hyperframes render --output output.mp4
```

## 设计要点
- **品牌水印**: 右上角"🏠 丁峰养老"半透明水印
- **问题引导式开场**: 使用emoji 🤔 引发思考
- **流程可视化**: 左右布局的步骤卡片
- **信任感构建**: 盾牌、证书等信任图标