# 智慧养老平台功能介绍系列视频

## 系列概览

| 期数 | 主题 | 时长 | 核心价值 |
|------|------|------|---------|
| 第1期 | 服务商入驻管理 | 90秒 | 规范入驻，保障服务质量 |
| 第2期 | 服务人员管理 | 90秒 | 权益保障，安心服务 |
| 第3期 | 服务质检管理 | 90秒 | 全程监督，持续提升 |
| 第4期 | 满意度调查 | 90秒 | 听见声音，温暖改进 |

## 设计规范

### 色彩系统
- **主色**: #F5A623 (暖橙)
- **背景**: #FFF8E7 (米白)
- **主文字**: #5D4E37 (深棕)
- **副文字**: #7A6B5A (灰棕)

### 视频规格
- 分辨率: 1920x1080
- 帧率: 30fps
- 格式: MP4 (H.264)

### 语音规范
- **音色**: `Chinese (Mandarin)_News_Anchor`
- **语速**: 0.85
- **语调**: 温暖、亲切、专业

## 品牌植入

### 统一元素
- 右上角半透明水印："🏠 丁峰养老"
- 结尾6秒品牌logo + Slogan强化
- 文案中自然提及品牌（每期2-3次）

### 植入原则
- 自然不刻意，随内容流动出现
- 全片控制在2-3次提及
- 文案对话式提及 + 视觉水印双重呈现

## 制作流程

### 1. 语音合成
```bash
# 设置 API Key
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

### 2. 渲染视频
```bash
# 预览（调试）
npx hyperframes preview

# 渲染输出
npx hyperframes render --output output.mp4
```

## 项目结构

```
video-series/
├── episode-01-provider/      # 第1期：服务商入驻管理
│   ├── index.html
│   ├── narration.txt
│   ├── audio/
│   │   └── narration_full.mp3
│   └── README.md
├── episode-02-staff/         # 第2期：服务人员管理
├── episode-03-quality/       # 第3期：服务质检管理
├── episode-04-satisfaction/  # 第4期：满意度调查
├── shared/                   # 共享资源
│   ├── assets/
│   │   └── qr-code.png
│   └── styles/
│       └── common.css
└── README.md
```

## 差异化设计

### 第1期（服务商入驻）
- **设计重点**: 专业性、规范性
- **配色强调**: 蓝色元素增加信任感
- **动画风格**: 线性流程动画

### 第2期（服务人员）
- **设计重点**: 人文关怀、温暖感
- **配色强调**: 暖色调和柔和元素
- **动画风格**: 柔和过渡

### 第3期（服务质检）
- **设计重点**: 严谨性、数据化
- **配色强调**: 科技感
- **动画风格**: 节奏感强

### 第4期（满意度调查）
- **设计重点**: 互动性、倾听感
- **配色强调**: 友好亲和
- **动画风格**: 轻松活泼

## 注意事项

1. **音频时长**: 必须精确匹配视频时长（90秒），否则会导致音频视频不同步
2. **品牌水印**: Scene 8 不显示水印（有独立的品牌logo）
3. **字幕同步**: 字幕入场时间需与语音节奏对齐
4. **场景过渡**: 使用 0.5秒 淡入淡出

## 成功指标

- 观看完成率 > 70%
- 点赞/分享率 > 5%
- 新用户功能使用率提升
- 客服咨询量减少

---

**文档版本**: v1.0
**创建日期**: 2026-05-23