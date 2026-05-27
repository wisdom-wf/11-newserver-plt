# 视频生成标准SOP — 红泥数智科技·项目实战版

> 基于 redmud-data-asset 项目完整实战经验总结，含所有控制参数、调整原则、复盘教训

---

## 一、前置准备

### 1.1 环境要求

| 工具 | 版本/说明 |
|------|-----------|
| Node.js | ≥18 |
| Bun | 用于运行 TTS 脚本 |
| HyperFrames CLI | `npx hyperframes` |
| GSAP | 3.14.2（**本地化**，禁止用 CDN） |
| ffmpeg | 用于1.3倍速后处理 |
| MiniMax API Key | 通过 `MINIMAX_API_KEY` 环境变量注入 |

**重要：GSAP 必须本地化**

无头浏览器无法访问外网 CDN，会导致 `gsap is not defined` 错误。

```bash
# 下载 GSAP 到本地
curl -sL "https://cdn.jsdelivr.net/npm/gsap@3.14.2/dist/gsap.min.js" -o gsap.min.js

# index.html 中引用本地文件（禁止 CDN）
<script src="gsap.min.js"></script>
```

### 1.2 目录结构规范

```
video-project/
├── index.html              # 主合成文件
├── gsap.min.js             # GSAP 本地化文件（必须）
├── narration_script.txt     # 旁白文案原文
├── narration_full.mp3      # TTS 语音旁白
├── logo.png                # 公司 Logo
├── ewm.jpg                 # 二维码
├── audio/
│   └── narration_full.mp3
├── shared/
│   └── styles/
│       └── common.css      # 共享样式
└── combined/              # 输出目录
    ├── [原版视频].mp4
    └── [1.3x加速版].mp4   # 最终交付版本
```

---

## 二、视频规划流程

### 2.1 确定视频规格

| 参数 | 推荐值 | 说明 |
|------|--------|------|
| 分辨率 | 1920 × 1080 | 固定 |
| 帧率 | 30fps | 固定 |
| 每场景时长 | 10–25 秒 | 含入场动画+字幕展示+过渡 |
| 总时长（旁白） | 180–210 秒 | 正常语速1.3倍后约138–161秒 |
| 场景数量 | 6–10 个 | 根据旁白段落数划分 |

### 2.2 编写旁白文案

**原则：**
- 每场景 30–50 字，口语化，适合 TTS 朗读
- 避免超长复合句，TTS 对长难句停顿处理不佳
- 结尾加"感谢观看"等礼貌用语

### 2.3 场景分解规划表

| 序号 | 场景名称 | 核心内容 | 时长范围 | 过渡效果 |
|------|----------|----------|----------|----------|
| 1 | 开场 | 品牌 Logo + 平台名称 | 15–22s | 淡入 |
| 2 | 主题介绍 | 本期内容概述 | 20–26s | 交叉淡化 |
| 3 | 功能/流程展示 | 操作流程 UI 动画 | 20–30s | 交叉淡化 |
| 4 | 案例/优势 | 关键案例或核心优势 | 25–35s | 交叉淡化 |
| 5 | 核心竞争力 | 数据/指标/保障 | 20–30s | 交叉淡化 |
| 6 | 服务全景 | 多步骤概览 | 25–35s | 交叉淡化 |
| 7 | 展望/愿景 | 未来展望 | 20–26s | 交叉淡化 |
| 8 | 结尾CTA | 感谢/品牌 | 4–5s | 淡出 |
| 9 | 联系信息 | Logo+二维码+电话 | 5s | **定格无黑屏** |

### 2.4 时间轴计算公式

```
场景 N 开始时间 = sum(场景1到N-1的时长) + (N-1) × 0.5s 过渡
字幕入场延迟 = 场景开始时间 + 1.5–2s
字幕退场时间 = 场景开始时间 + 场景时长 - 1s
```

---

## 三、视频制作流程

### 3.1 语音合成（TTS）

**使用 MiniMax TTS：**

```bash
export MINIMAX_API_KEY="your-api-key"

bun run /Users/wisdom/.claude/skills/minimax-speech/scripts/tts.ts \
  "$(cat narration_script.txt)" \
  --voice "Chinese (Mandarin)_News_Anchor" \
  --speed 0.85 \
  --output audio/narration_full.mp3
```

**参数说明：**

| 参数 | 推荐值 | 说明 |
|------|--------|------|
| voice | `Chinese (Mandarin)_News_Anchor` | 新闻播音员音色，正式感 |
| speed | 0.80–0.90 | 语速慢于正常（为后续1.3倍留空间） |

**注意事项：**
- speed 0.85 经过1.3倍加速后约为正常语速的 1.1 倍，信息密度适中
- 如 speed 1.0，1.3倍后语速偏快，1.0 语速人群会觉得急促
- 合成后用 ffprobe 确认音频时长：

```bash
ffprobe -v error -show_entries format=duration \
  -of default=noprint_wrappers=1:nokey=1 audio/narration_full.mp3
```

### 3.2 HTML 场景定义

**文件：** `index.html`

**标准 HTML 结构骨架：**

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=1920, height=1080">
  <title>项目名称</title>
  <link rel="stylesheet" href="shared/styles/common.css">
  <script src="gsap.min.js"></script>
</head>
<body>
<div id="root"
  data-composition-id="project-id"
  data-width="1920"
  data-height="1080"
  data-start="0"
  data-duration="[总时长秒数]">

  <!-- Scene 1 -->
  <div id="scene1" class="scene" style="... z-index:1; opacity:1; visibility:visible;">
    <!-- 背景装饰、Logo、标题、内容 -->
    <div id="cap1" class="caption-bar">
      <span class="caption-text">字幕内容（≤20字）</span>
    </div>
  </div>

  <!-- Scene 2...8 同理 -->

  <!-- Scene 9: 联系信息（定格场景） -->
  <div id="scene9" class="scene" style="... z-index:9; opacity:0; visibility:hidden;">
    <!-- Logo + 二维码 + 电话 -->
    <div id="cap9" class="caption-bar">
      <span class="caption-text">公司名 · 定位语</span>
    </div>
  </div>

  <!-- Audio -->
  <audio id="narration-full"
    data-start="0"
    data-duration="[总时长秒数]"
    data-track-index="10"
    src="audio/narration_full.mp3"
    data-volume="1">
  </audio>
</div>
```

### 3.3 GSAP Timeline 编写规范

**核心原则：**

| 规范 | 要求 |
|------|------|
| 时间注册 | 必须注册到 `window.__timelines[id]` |
| 入口动画 | 使用 `from()` — 从不可见状态进入 |
| 出口动画 | 使用 `to()` — 退场到不可见 |
| 字幕同步 | 底部 130px 高条带，白色文字，GSAP 控制 opacity |
| 过渡时长 | 0.5s，使用 `power2.inOut` easing |
| repeat: -1 | **禁止**！必须计算有限次数或用 `paused: true` |
| 全局黑屏 fade | **禁止**！结尾场景必须自然收尾 |

**字幕标准写法：**

```javascript
// 字幕入场：场景开始后 1.5–2s
tl.from("#cap", { y: 130, opacity: 0, duration: 0.5, ease: "power2.out" }, captionStart);
tl.to("#cap", { opacity: 1, duration: 0.3 }, captionStart);
// 字幕退场：场景结束前 0.5s
tl.to("#cap", { opacity: 0, duration: 0.4 }, captionEnd - 0.4);
```

**场景切换标准写法：**

```javascript
// Transition N→N+1
tl.to("#sceneN", { opacity: 0, duration: 0.5, ease: "power2.inOut" }, sceneNEnd - 0.5);
tl.to("#sceneN+1", { opacity: 1, duration: 0.5, ease: "power2.inOut" }, sceneNEnd - 0.1);
tl.set("#sceneN", { visibility: "hidden" }, sceneNEnd);
tl.set("#sceneN+1", { visibility: "visible" }, sceneNEnd - 0.1);
```

**联系信息场景（Scene 9）写法：**

```javascript
// Transition 8→9
tl.to("#scene8", { opacity: 0, duration: 0.5, ease: "power2.inOut" }, scene8End);
tl.to("#scene9", { opacity: 1, duration: 0.5, ease: "power2.inOut" }, scene8End + 0.4);
tl.set("#scene8", { visibility: "hidden" }, scene8End + 0.5);
tl.set("#scene9", { visibility: "visible" }, scene8End + 0.4);

// Scene 9 入场动画
tl.from("#s9-logo", { scale: 0.8, opacity: 0, duration: 0.6, ease: "back.out(1.4)" }, scene9Start + 0.2);
tl.from("#s9-qr", { scale: 0.8, opacity: 0, duration: 0.6, ease: "back.out(1.4)" }, scene9Start + 0.6);
tl.from("#s9-contact", { y: 30, opacity: 0, duration: 0.5, ease: "power3.out" }, scene9Start + 1.2);
// 字幕
tl.from("#cap9", { y: 130, opacity: 0, duration: 0.5, ease: "power2.out" }, scene9Start + 1.5);
tl.to("#cap9", { opacity: 1, duration: 0.3 }, scene9Start + 1.5);
// NO global fade out — scene 9 holds until end
```

### 3.4 场景切换模式

**标准模式（opacity + visibility）：**

```css
.scene {
  position: absolute;
  top: 0;
  left: 0;
  width: 1920px;
  height: 1080px;
  opacity: 0;
  visibility: hidden;
  transition: none; /* GSAP 全权控制，不用 CSS transition */
}
/* 首个场景 */
.scene:first-child {
  opacity: 1;
  visibility: visible;
}
```

---

## 四、字幕设计规范

### 4.1 字幕内容原则

**核心原则：每条字幕不超过 20 个字**

| 问题 | 后果 | 正确做法 |
|------|------|----------|
| 字幕堆满语音文字 | 遮挡内容、阅读困难、破坏布局 | 只显示关键句/关键词 |
| 完整复述旁白 | 观众来不及看就切换 | 提炼核心信息，≤20字 |

**示例对比：**

| 场景 | ❌ 错误（堆字） | ✅ 正确（精简） |
|------|----------------|-----------------|
| 开场 | 在数字经济时代，数据已成为第五大生产要素... | 数据 · 第五大生产要素 |
| 案例 | 我们已成功完成延安水务环保集团的数据资产入表项目... | 陕西首批 · 深交所成功上市 |
| 优势 | 为什么选择红泥数智？十六年政企信息化积淀... | 十六年积淀 · 1.8亿交付 · 7×24服务 |

### 4.2 字幕条样式

```css
.caption-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 130px;
  background: rgba(44, 62, 80, 0.92);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.caption-text {
  font-size: 38px;
  color: #ffffff;
  font-weight: 600;
  text-align: center;
  padding: 0 60px;
  letter-spacing: 3px;
  line-height: 1.4;
}
```

---

## 五、视觉设计规范

### 5.1 字号标准（1920×1080）

| 元素 | 字号 |
|------|------|
| 主标题 | 72–96px |
| 副标题 | 48–64px |
| 正文 | 28–36px |
| 字幕 | 36–42px |
| 装饰文字 | 20–28px |

### 5.2 装饰透明度

| 元素 | 透明度 |
|------|--------|
| 背景装饰图形 | 8–15%（视频级不追求淡雅） |
| 分隔线 | 20–40% |
| 图标 | 70–90% |

### 5.3 边框与间距

| 元素 | 规范 |
|------|------|
| 边框宽度 | 2–4px |
| 内边距 | 40–80px |
| 圆角 | 8–20px |

### 5.4 品牌色（红泥数智标准色）

```
主色（橙色）: #E67E22
背景（米白）: #FDF6E3
深色背景（蓝）: #2C3E50
文字（深）: #2C3E50
副文字（灰）: #7F8C8D
强调（金）: #D4A84B
```

---

## 六、预览与渲染

### 6.1 本地预览

```bash
cd video-project
npx hyperframes preview
```

### 6.2 语法检查

```bash
npx hyperframes lint
npx hyperframes validate
```

### 6.3 渲染

```bash
npx hyperframes render --output combined/output.mp4
```

**注意：** 渲染时不带 `--audio` 参数，音频会在 ffmpeg 加速阶段合并。

---

## 七、倍速后处理（必须步骤）

### 7.1 为什么必须是 1.3 倍速

| speed | 1.3倍后实际语速 | 效果 |
|-------|----------------|------|
| 0.85 | ≈1.1 倍正常 | 信息密度适中，不急促 |
| 1.0 | ≈1.3 倍正常 | 语速偏快，易疲劳 |

**结论：TTS 用 speed 0.85，渲染后必须 1.3 倍加速**

### 7.2 ffmpeg 加速命令

```bash
ffmpeg -i combined/output_original.mp4 \
  -filter:a "atempo=1.3" \
  -filter:v "setpts=0.769*PTS" \
  -c:v libx264 -preset fast -crf 22 \
  -c:a aac -b:a 128k \
  combined/output_1.3x.mp4
```

**参数说明：**
- `atempo=1.3` — 音频加速 1.3 倍
- `setpts=0.769*PTS` — 视频时间轴同步压缩（1/1.3 ≈ 0.769）
- `crf=22` — 质量与文件大小平衡
- `preset=fast` — 编码速度与质量平衡

### 7.3 倍速后时长估算

| 原版时长 | 1.3x 后时长 |
|----------|-------------|
| 196s（3分16秒） | 151s（2分31秒） |
| 180s（3分） | 138s（2分18秒） |
| 210s（3分30秒） | 162s（2分42秒） |

---

## 八、复盘经验（教训）

### 8.1 必须避免的问题

| 问题 | 后果 | 正确做法 |
|------|------|----------|
| GSAP 用 CDN | `gsap is not defined` 黑屏 | 下载到本地用相对路径 |
| `repeat: -1` | 动画无法配合场景切换 | 计算有限次数 |
| CSS transition + GSAP 混用 | 动画冲突 | GSAP 全权控制 |
| 超长复合句旁白 | TTS 停顿异常 | 分段合成，每段 ≤ 50 字 |
| 装饰透明度 3–8% | 视频级看不清 | 视频级用 8–15% |
| 边框 1px | 视频级太细 | 视频级用 2–4px |
| 不注册 timeline | HyperFrames seek 失效 | 必须 `window.__timelines[id] = tl` |
| 全局 fade to black | 结尾黑屏 | 联系信息场景定格收尾 |
| 字幕堆满语音全文 | 破坏布局、难阅读 | 精简到 ≤ 20 字关键词 |
| TTS speed=1.0 | 1.3倍后语速急促 | TTS 用 speed 0.85 |

### 8.2 Scene 9 联系信息规范

联系信息场景是视频的最后一帧，**必须定格，不能黑屏**。

**必要元素：**
- 公司 Logo（logo.png）
- 二维码（ewm.jpg）
- 联系电话（格式：136 XXXX XXXX 或 136XXXXXXXX）

**时间规划：**
- 场景时长：5 秒
- 音频时长：与原版一致（音频结束后 Scene 9 定格）
- 无全局 fade out

---

## 九、质量检查清单

渲染前逐项核对：

- [ ] 分辨率为 1920×1080
- [ ] GSAP 使用本地文件 gsap.min.js（无 CDN）
- [ ] 每场景有明确的时间轴标记
- [ ] 所有 timeline 注册到 `window.__timelines`
- [ ] 字幕为关键词/关键句（≤20字），非语音全文
- [ ] 字幕有入场（1.5–2s 延迟）和退场（场景结束前 0.5s）
- [ ] 过渡时长 0.5s，使用 `power2.inOut`
- [ ] 无 `repeat: -1`
- [ ] 装饰透明度 ≥ 8%
- [ ] 边框宽度 ≥ 2px
- [ ] TTS speed 为 0.85（为1.3倍留余量）
- [ ] Scene 9 联系信息场景包含 Logo + 二维码 + 电话
- [ ] Scene 9 无全局 fade out，定格到最后一帧
- [ ] `hyperframes lint` 无 error（warning 可忽略）
- [ ] 输出 MP4 文件可正常播放

---

## 十、交付标准

| 项目 | 标准 |
|------|------|
| 文件命名 | `项目名_1.3x.mp4` |
| 分辨率 | 1920×1080 |
| 帧率 | 30fps |
| 时长 | 2分–3分（1.3x后） |
| 视频编码 | H.264 |
| 音频编码 | AAC |
| 文件大小 | 建议 5–15MB |

---

## 十一、关键文件索引

| 文件 | 用途 |
|------|------|
| `redmud-data-asset/index.html` | 完整视频合成示例（9场景，196秒原版） |
| `redmud-data-asset/shared/styles/common.css` | 共享样式示例 |
| `.agents/skills/hyperframes/SKILL.md` | HyperFrames 完整技能文档 |
| `.agents/skills/gsap/SKILL.md` | GSAP Timeline 编写规范 |
| `.agents/skills/hyperframes/references/transitions/` | 14 种过渡效果参考 |
| `docs/video-production-sop.md` | 通用版视频SOP（智慧养老平台） |

---

## 十二、TTS 音色选用参考

| 场景类型 | 推荐 voice_id | 说明 |
|----------|--------------|------|
| 正式商务/政务/数据 | `Chinese (Mandarin)_News_Anchor` | 新闻主播，播音腔，稳重专业 |
| 通用旁白 | `female-tianmei` | 甜美女声 |
| 讲故事/情感 | `female-yujie` | 御姐音，成熟女性 |
| 青年男声 | `male-qn-qingse` | 通用男声 |
| 客服/接待 | `female-yujie` | 温暖专业 |

**本项目选用：`Chinese (Mandarin)_News_Anchor`（正式、数据类宣传片首选）**