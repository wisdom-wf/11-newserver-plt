# 视频生成标准SOP — 智慧养老平台

> 基于项目 video-intro/ 和 video-series/ 的实战经验总结

---

## 一、前置准备

### 1.1 环境要求

| 工具 | 版本/说明 |
|------|-----------|
| Node.js | ≥18 |
| Bun | 用于运行 TTS 脚本 |
| HyperFrames CLI | `npx hyperframes` |
| GSAP | 3.14.2（**本地化**，禁止用 CDN） |
| MiniMax API Key | 通过 `MINIMAX_API_KEY` 环境变量注入 |

### 1.2 目录结构规范

```
video-project/
├── index.html          # 主合成文件
├── narration.txt       # 旁白文案
├── audio/
│   ├── narration_full.mp3   # TTS 合成音频
│   └── .waveform-cache/      # 波形数据缓存
├── .thumbnails/        # 场景预览缩略图
├── shared/
│   └── styles/common.css  # 共享样式（多期复用）
└── combined/           # 合成输出目录
```

---

## 二、视频规划流程

### 2.1 确定视频规格

| 参数 | 推荐值 |
|------|--------|
| 分辨率 | 1920 × 1080 |
| 帧率 | 30fps |
| 每场景时长 | 8–12 秒 |
| 总时长 | 60–120 秒 |
| 场景数量 | 6–10 个 |

### 2.2 编写旁白文案

**原则：**
- 每场景 30–50 字，口语化，适合 TTS 朗读
- 避免超长复合句，TTS 对长难句停顿处理不佳
- 结尾加"感谢观看"等礼貌用语

**示例（服务商入驻场景）：**
```
欢迎使用智慧居家养老服务平台。本期介绍服务商如何完成入驻认证，
包括资料提交、资质审核和服务范围配置。一起看看吧。
```

### 2.3 场景分解规划表

| 序号 | 场景名称 | 核心内容 | 时长 | 过渡效果 |
|------|----------|----------|------|----------|
| 1 | 开场 | 品牌 Logo + 平台名称 | 3s | 淡入 |
| 2 | 主题介绍 | 本期内容概述 | 8s | 滑入 |
| 3 | 功能演示 | 操作流程 UI 动画 | 15s | 交叉淡化 |
| 4 | 亮点强调 | 关键特性展示 | 10s | 缩放 |
| 5 | 结尾 | CTA + 二维码 | 5s | 淡出 |

---

## 三、视频制作流程

### 3.1 语音合成（TTS）

**使用 MiniMax TTS：**

```bash
export MINIMAX_API_KEY="your-api-key"

bun run /Users/wisdom/.claude/skills/minimax-speech/scripts/tts.ts \
  "$(cat narration.txt)" \
  --voice "Chinese (Mandarin)_News_Anchor" \
  --speed 0.85 \
  --output audio/narration_full.mp3
```

**参数说明：**

| 参数 | 推荐值 | 说明 |
|------|--------|------|
| voice | `Chinese (Mandarin)_News_Anchor` | 新闻播音员音色 |
| speed | 0.80–0.90 | 语速，建议不高于 0.9 |

**注意事项：**
- 合成的 MP3 时长应与旁白文案朗读时长匹配
- 如有卡顿或断句异常，分段合成后拼接
- 保存路径使用 `audio/narration_full.mp3` 作为标准命名

### 3.2 HTML 场景定义

**文件：** `index.html`

**标准 HTML 结构：**

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=1920, height=1080">
  <title>智慧养老平台 — 功能介绍</title>
  <link rel="stylesheet" href="shared/styles/common.css">
  <!-- GSAP 3.14.2 -->
  <script src="https://cdnjs.cloudflare.com/ajax/libs/gsap/3.14.2/gsap.min.js"></script>
</head>
<body>
  <!-- 场景容器 -->
  <div id="composition">

    <!-- 场景 1: 开场 -->
    <div id="scene1" class="scene" style="opacity:1; visibility:visible;">
      <!-- 背景 -->
      <div class="bg" style="background:#FFF8E7;"></div>
      <!-- Logo -->
      <div id="logo" class="logo">
        <img src="logo.png" width="120">
      </div>
      <!-- 标题 -->
      <div id="title1" class="title">智慧居家养老服务平台</div>
      <!-- 字幕条 -->
      <div id="cap1" class="caption-bar">
        <span class="caption-text">欢迎使用智慧居家养老服务平台</span>
      </div>
    </div>

    <!-- 场景 2: 功能介绍 -->
    <div id="scene2" class="scene" style="opacity:0; visibility:hidden;">
      <!-- ... -->
    </div>

  </div>

  <script>
    window.__timelines = window.__timelines || {};
    const tl = gsap.timeline({ paused: true });

    // === 场景 1 动画 ===

    // Logo 入场
    tl.from("#logo", { y: -80, opacity: 0, duration: 0.8, ease: "power3.out" }, 0.3);

    // 标题入场
    tl.from("#title1", { y: 60, opacity: 0, duration: 0.6, ease: "back.out" }, 0.6);

    // 字幕显示（延迟 1.5s 入场，持续 6s 后退场）
    tl.from("#cap1", { y: 120, opacity: 0, duration: 0.5 }, 1.6);
    tl.to("#cap1", { opacity: 1, duration: 0.3 }, 1.6);
    tl.to("#cap1", { opacity: 0, duration: 0.4 }, 7.5);

    // 场景 1 → 场景 2 过渡（9.5s 开始淡出）
    tl.to("#scene1", { opacity: 0, duration: 0.5, ease: "power2.inOut" }, 9.5);
    tl.to("#scene2", { opacity: 1, duration: 0.5, ease: "power2.inOut" }, 9.9);

    // === 场景 2 动画 ===

    // ... 继续定义场景 2 动画 ...

    window.__timelines["main"] = tl;
  </script>
</body>
</html>
```

### 3.3 GSAP Timeline 编写规范

**核心原则：**

| 规范 | 要求 |
|------|------|
| 时间注册 | 必须注册到 `window.__timelines[id]` |
| 入口动画 | 使用 `from()` — 从不可见状态进入 |
| 出口动画 | 使用 `to()` — 退场到不可见 |
| 字幕同步 | 底部 120px 高条带，白色文字，GSAP 控制 opacity |
| 过渡时长 | 0.5s，使用 `power2.inOut` easing |
| repeat: -1 | 禁止！必须计算有限重复次数 |

**Easing 选用指南：**

| 场景 | Easing |
|------|--------|
| UI 元素入场 | `power3.out` |
| 标题强调 | `back.out(1.2)` |
| 字幕淡入 | `power2.out` |
| 场景过渡 | `power2.inOut` |
| 弹性动画 | `elastic.out(1, 0.5)` |

**字幕标准写法：**

```javascript
// 字幕入场：场景开始后 1.5-2s
tl.from("#cap", { y: 120, opacity: 0, duration: 0.5 }, 1.6);
tl.to("#cap", { opacity: 1, duration: 0.3 }, 1.6);
// 字幕退场：场景结束前 0.5s
tl.to("#cap", { opacity: 0, duration: 0.4 }, 9.0);
```

**字幕 CSS 样式：**

```css
.caption-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120px;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
}
.caption-text {
  font-size: 32px;
  color: #ffffff;
  letter-spacing: 2px;
}
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
  transition: none; /* 由 GSAP 控制，不用 CSS transition */
}
/* 首个场景 */
.scene:first-child {
  opacity: 1;
  visibility: visible;
}
```

**场景切换顺序：**
1. 当前场景元素退场动画（opacity → 0）
2. 当前场景 visibility → hidden
3. 下一场景 visibility → visible
4. 下一场景元素入场动画（opacity → 1）

---

## 四、视觉设计规范

### 4.1 字号标准（1920×1080）

| 元素 | 字号 |
|------|------|
| 主标题 | 72–120px |
| 副标题 | 48–64px |
| 正文 | 28–42px |
| 字幕 | 32–36px |
| 装饰文字 | 20–28px |

### 4.2 装饰透明度

| 元素 | 透明度 |
|------|--------|
| 背景装饰图形 | 12–25%（视频级比 Web 级更浓） |
| 分隔线 | 20–40% |
| 图标 | 70–90% |

### 4.3 边框与间距

| 元素 | 规范 |
|------|------|
| 边框宽度 | 2–4px（非 Web 级的 1px） |
| 内边距 | 60–140px（视频级更大） |
| 圆角 | 8–16px |

### 4.4 品牌色（养老平台标准色）

```
主色（暖橙）: #F5A623
背景（米白）: #FFF8E7
主文字（深棕）: #5D4E37
副文字（灰棕）: #7A6B5A
```

---

## 五、过渡效果选用

### 5.1 推荐过渡效果

| 效果 | 适用场景 | 实现方式 |
|------|----------|----------|
| **交叉淡化 Dissolve** | 功能切换 | GSAP opacity |
| **滑入 Push** | 列表/步骤展示 | CSS transform + translateX |
| **缩放 Zoom** | 强调/聚焦 | GSAP scale |
| **光漏 Light** | 场景切换 | CSS radial-gradient + opacity |
| **模糊 Blur** | 转场过渡 | CSS backdrop-filter |

### 5.2 过渡时长标准

| 过渡类型 | 时长 |
|----------|------|
| 同场景元素切换 | 0.3–0.5s |
| 场景间过渡 | 0.5–0.8s |
| 强调/聚焦动画 | 0.4–0.6s |

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
npx hyperframes validate    # WCAG 对比度验证
npx hyperframes inspect     # 布局检查
```

### 6.3 渲染输出

```bash
npx hyperframes render --output output.mp4
```

### 6.4 添加音频后的渲染（不推荐）

> 音频在渲染阶段容易出错。建议：HyperFrames 渲染不带音频，音频在 ffmpeg 倍速阶段合并。

### 6.5 倍速后处理（必须步骤）

渲染完成后，必须进行 1.3 倍速处理，这是标准流程的一部分。

**为什么必须是 1.3 倍速：**
- TTS speed 0.85，1.3倍后 ≈ 1.1 倍正常语速
- 信息密度适中，不急促，不昏昏欲睡
- speed 1.0 的 TTS，1.3倍后语速偏快

**ffmpeg 加速命令：**

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
- `setpts=0.769*PTS` — 视频时间轴同步压缩（1/1.3）
- `crf=22` — 质量与文件大小平衡
- `preset=fast` — 编码速度与质量平衡

**倍速后时长估算：**

| 原版时长 | 1.3x 后时长 |
|----------|-------------|
| 196s（3分16秒） | 151s（2分31秒） |
| 180s（3分） | 138s（2分18秒） |
| 210s（3分30秒） | 162s（2分42秒） |

---

## 七、字幕设计规范

### 7.1 字幕内容原则（核心）

**每条字幕不超过 20 个字。** 字幕是关键词/关键句展示，不是语音全文复述。

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

### 7.2 字幕条样式

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

## 八、联系信息场景规范（Scene 9）

### 8.1 作用

联系信息场景是视频的最后一帧，**必须定格，不能黑屏**。包含 Logo + 二维码 + 电话，是标准的 CTA 结尾。

### 8.2 必要元素

| 元素 | 文件 | 要求 |
|------|------|------|
| 公司 Logo | `logo.png` | 放在场景中央偏上 |
| 二维码 | `ewm.jpg` | 白底，居中展示 |
| 联系电话 | 直接写在 HTML 里 | 格式：136 XXXX XXXX |

### 8.3 时间规划

- 场景时长：5 秒
- 音频：在原版时长处结束，Scene 9 后面没有音频
- 动画：Logo → 二维码 → 联系方式依次入场
- **禁止全局 fade out** — Scene 9 定格到最后一帧

### 8.4 GSAP 写法模板

```javascript
// Transition 8→9（Scene 8 结尾淡出，Scene 9 定格）
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
// NO global fade out — Scene 9 holds until end
```

---

## 九、多期系列视频规范

### 9.1 共享样式分离

将通用样式提取到 `shared/styles/common.css`，各期复用：

```css
/* shared/styles/common.css */
:root {
  --brand-orange: #F5A623;
  --brand-cream: #FFF8E7;
  --text-brown: #5D4E37;
  --text-muted: #7A6B5A;
}

* { margin: 0; padding: 0; box-sizing: border-box; }

.scene {
  position: absolute;
  top: 0;
  left: 0;
  width: 1920px;
  height: 1080px;
  opacity: 0;
  visibility: hidden;
}

.logo { position: absolute; top: 60px; left: 60px; }
.caption-bar {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 120px;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
}
```

### 9.2 系列命名规范

| 期数 | 目录 | 主题 |
|------|------|------|
| 第1期 | `episode-01-provider/` | 服务商入驻管理 |
| 第2期 | `episode-02-staff/` | 服务人员管理 |
| 第3期 | `episode-03-quality/` | 服务质检管理 |
| 第4期 | `episode-04-satisfaction/` | 满意度调查 |

---

## 十、复盘经验（教训）

### 10.1 必须避免的问题

| 问题 | 后果 | 正确做法 |
|------|------|----------|
| `repeat: -1` | 动画无法配合场景切换 | 计算有限次数或用 `paused: true` timeline |
| CSS transition + GSAP 混用 | 动画冲突 | GSAP 全权控制，不用 CSS transition |
| 超长复合句旁白 | TTS 停顿异常 | 分段合成，每段 ≤ 50 字 |
| 装饰透明度 3–8% | 视频级看不清 | 视频级用 12–25% |
| 边框 1px | 视频级太细 | 视频级用 2–4px |
| 不注册 timeline | HyperFrames seek 失效 | 必须 `window.__timelines[id] = tl` |

---

## 十一、关键文件索引

| 文件 | 用途 |
|------|------|
| `docs/video-production-sop-redmud.md` | **项目实战版**（红泥数智，含1.3倍速所有细节） |
| `.agents/skills/hyperframes/SKILL.md` | HyperFrames 完整技能文档 |
| `.agents/skills/gsap/SKILL.md` | GSAP Timeline 编写规范 |
| `.agents/skills/hyperframes/references/transitions/` | 14 种过渡效果参考 |
| `.agents/skills/hyperframes/visual-styles.md` | 8 种视觉风格定义 |
| `redmud-data-asset/index.html` | **实战参考**（9场景，含Scene9联系信息） |
| `redmud-data-asset/shared/styles/common.css` | 共享样式参考 |
| `video-intro/index.html` | 完整视频合成示例 |
| `video-series/episode-01-provider/index.html` | 8 场景 102 秒示例 |

---

## 十二、质量检查清单

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
- [ ] 倍速后 ffmpeg 处理完成，最终文件为 `_1.3x.mp4`
- [ ] 输出 MP4 文件可正常播放