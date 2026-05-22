# 智慧养老平台介绍视频

基于 HyperFrames 框架构建的社区服务智慧养老平台介绍视频，支持 MiniMax TTS 语音合成。

## 项目结构

```
video-intro/
├── index.html          # 视频合成主文件
├── qr-code.png        # 预约二维码
├── audio/            # 音频文件目录
│   └── narration_full.mp3  # 完整旁白音频
├── .gitignore        # Git忽略规则
└── README.md         # 本文档
```

## 视频规格

| 参数 | 值 |
|------|-----|
| 分辨率 | 1920x1080 |
| 时长 | ~60秒 |
| 帧率 | 30fps |
| 场景数 | 7个 |
| 音频 | 单轨道完整旁白 |

## 时间轴设计标准

### 场景分配

| 场景 | 内容 | 时长 | 用途 |
|------|------|------|------|
| Scene 1 | 标题页 | 10秒 | 品牌展示 |
| Scene 2 | 服务入口 | 10秒 | 功能介绍 |
| Scene 3 | 步骤一 | 10秒 | 操作引导 |
| Scene 4 | 步骤二 | 10秒 | 操作引导 |
| Scene 5 | 步骤三 | 10秒 | 操作引导 |
| Scene 6 | 关怀信息 | 10秒 | 情感连接 |
| Scene 7 | 二维码 | 8秒 | 行动号召 |

### 时间轴公式

```
Scene N 开始时间 = (N-1) * 10秒
Scene N 结束时间 = N * 10秒
Transition: 0.5秒平滑过渡
```

## 语音合成标准

### 音色选择

| 场景类型 | 推荐音色 | 说明 |
|----------|----------|------|
| 养老/关怀类 | `Chinese (Mandarin)_News_Anchor` | 中年女性，播音腔，稳重专业 |
| 通用旁白 | `female-yujie` | 御姐音，成熟女性 |
| 活泼风格 | `female-tianmei` | 甜美女性 |

### 语速标准

| 受众 | 推荐语速 | 说明 |
|------|----------|------|
| 老年人 | 0.85-0.9 | 略慢，便于理解 |
| 中年 | 1.0 | 标准语速 |
| 年轻人 | 1.0-1.2 | 正常或略快 |

### 音频时长计算

```bash
ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 audio/narration_full.mp3
```

## HyperFrames 使用规范

### 元素命名

```html
<!-- 场景ID格式 -->
<div id="scene{N}">

<!-- 字幕格式 -->
<div id="cap{N}">

<!-- 音频格式 -->
<audio id="narration-{type}" data-start="0" data-duration="60">
```

### 时间轴编写规则

1. **入口动画**: 使用 `gsap.from()` 从不可见状态进入
2. **过渡动画**: 使用 `tl.to()` 控制场景切换
3. **字幕显示**: 进入后 `opacity: 1`，退出前 `opacity: 0`
4. **淡出效果**: 仅在音频结束后使用全局淡出

### 关键代码模板

```javascript
// 场景入口
tl.from("#element", { y: 60, opacity: 0, duration: 0.6, ease: "power3.out" }, startTime);

// 字幕显示
tl.from("#cap", { y: 120, opacity: 0, duration: 0.5, ease: "power2.out" }, captionTime);
tl.to("#cap", { opacity: 1, duration: 0.3 }, captionTime);
tl.to("#cap", { opacity: 0, duration: 0.4 }, endTime - 0.4);

// 场景过渡
tl.to("#sceneN", { opacity: 0, duration: 0.5, ease: "power2.inOut" }, transitionStart);
tl.to("#sceneN+1", { opacity: 1, duration: 0.5, ease: "power2.inOut" }, transitionStart + 0.4);
```

## 渲染命令

```bash
# 预览（本地开发）
cd video-intro
npx hyperframes preview

# 渲染视频
npx hyperframes render --output output.mp4
```

## MiniMax TTS 语音合成

### API Key 配置

```bash
export MINIMAX_API_KEY="your-api-key"
```

### 合成命令

```bash
bun run /Users/wisdom/.claude/skills/minimax-speech/scripts/tts.ts \
  "旁白文本内容" \
  --voice "Chinese (Mandarin)_News_Anchor" \
  --speed 0.85 \
  --output audio/narration_full.mp3
```

### 音色列表查询

```bash
bun run /Users/wisdom/.claude/skills/minimax-speech/scripts/voices.ts
```

## 品牌元素规范

### 品牌关键词
- 延安宝塔
- 丁峰养老
- 智慧养老

### 色彩规范

| 用途 | 色值 |
|------|------|
| 主色 | #F5A623 (暖橙) |
| 背景 | #FFF8E7 (米白) |
| 文字 | #5D4E37 (深棕) |
| 副文字 | #7A6B5A (灰棕) |

### 字体规范

```
font-family: -apple-system, BlinkMacSystemFont, "PingFang SC", "Microsoft YaHei", sans-serif;
```

## 常见问题

### Q: 如何确认音频时长？
```bash
ffprobe -v error -show_entries format=duration -of default=noprint_wrappers=1:nokey=1 audio/narration_full.mp3
```

### Q: 如何调整场景停留时间？
修改 `index.html` 中 GSAP timeline 的时间参数，每个场景的 `startTime` 和 `endTime` 需要同步调整。

### Q: 如何修改旁白内容？
1. 编辑文案
2. 重新生成音频：`bun run scripts/tts.ts "新文案" --voice "音色ID" --speed 0.85 --output audio/narration_full.mp3`
3. 确认音频时长
4. 如需要调整视频时长以匹配音频

### Q: 渲染失败常见原因？
1. GSAP CDN 无法下载 - 需要bundling或在本地
2. 音频文件路径错误 - 检查 `src="audio/xxx.mp3"`
3. 场景时间重叠 - 检查 timeline 时序