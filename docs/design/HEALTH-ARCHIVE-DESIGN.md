---
version: alpha
name: Elderly Care Health Archive
description: "适老化健康档案仪表盘 — 温暖可信、层次清晰、高度可读。政务蓝+关怀橙双主色，大字高对比度，卡片分区明确。"
colors:
  primary: "#1E3A5F"
  secondary: "#4A6FA5"
  accent: "#E8833A"
  accent-light: "#FFF4EC"
  success: "#2E7D4A"
  warning: "#C0620A"
  danger: "#B91C1C"
  neutral-bg: "#F5F7FA"
  neutral-card: "#FFFFFF"
  text-primary: "#1A1A1A"
  text-secondary: "#5A6478"
  text-muted: "#94A3B8"
  border: "#E2E8F0"
typography:
  display:
    fontFamily: system-ui, "PingFang SC", "Microsoft YaHei", sans-serif
    fontSize: 2rem
    fontWeight: 700
    lineHeight: 1.2
    letterSpacing: "-0.01em"
  h1:
    fontFamily: system-ui, "PingFang SC", "Microsoft YaHei", sans-serif
    fontSize: 1.5rem
    fontWeight: 600
    lineHeight: 1.3
  h2:
    fontFamily: system-ui, "PingFang SC", "Microsoft YaHei", sans-serif
    fontSize: 1.25rem
    fontWeight: 600
    lineHeight: 1.4
  body-lg:
    fontFamily: system-ui, "PingFang SC", "Microsoft YaHei", sans-serif
    fontSize: 1.125rem
    fontWeight: 400
    lineHeight: 1.7
  body-md:
    fontFamily: system-ui, "PingFang SC", "Microsoft YaHei", sans-serif
    fontSize: 1rem
    fontWeight: 400
    lineHeight: 1.6
  label:
    fontFamily: system-ui, "PingFang SC", "Microsoft YaHei", sans-serif
    fontSize: 0.875rem
    fontWeight: 500
    lineHeight: 1.4
  caption:
    fontFamily: system-ui, "PingFang SC", "Microsoft YaHei", sans-serif
    fontSize: 0.8125rem
    fontWeight: 400
    lineHeight: 1.5
    color: "{colors.text-muted}"
rounded:
  sm: 8px
  md: 12px
  lg: 16px
  xl: 24px
spacing:
  xs: 4px
  sm: 8px
  md: 16px
  lg: 24px
  xl: 32px
  2xl: 48px
elevation:
  card: "0 2px 8px rgba(30,58,95,0.08)"
  card-hover: "0 4px 16px rgba(30,58,95,0.14)"
  drawer: "0 8px 32px rgba(30,58,95,0.16)"
components:
  health-summary-card:
    backgroundColor: "{colors.neutral-card}"
    borderRadius: "{rounded.lg}"
    padding: "{spacing.lg}"
    boxShadow: "{elevation.card}"
  health-stat-chip:
    backgroundColor: "{colors.accent-light}"
    textColor: "{colors.accent}"
    borderRadius: "{rounded.full}"
    padding: "6px 14px"
    fontSize: 0.9375rem
    fontWeight: 600
  tab-button:
    backgroundColor: transparent
    textColor: "{colors.text-secondary}"
    borderRadius: "{rounded.md}"
    padding: "10px 18px"
    fontSize: 1rem
    fontWeight: 500
  tab-button-active:
    backgroundColor: "{colors.primary}"
    textColor: "#FFFFFF"
    borderRadius: "{rounded.md}"
    padding: "10px 18px"
    fontSize: 1rem
    fontWeight: 600
  data-value-display:
    fontFamily: system-ui, "PingFang SC", sans-serif
    fontSize: 2.25rem
    fontWeight: 700
    lineHeight: 1
    color: "{colors.primary}"
  section-card:
    backgroundColor: "{colors.neutral-card}"
    borderRadius: "{rounded.lg}"
    padding: "{spacing.lg}"
    boxShadow: "{elevation.card}"
    borderLeft: "4px solid {colors.primary}"
  alert-high:
    backgroundColor: "#FEF2F2"
    borderColor: "{colors.danger}"
    textColor: "{colors.danger}"
    borderRadius: "{rounded.md}"
    padding: "{spacing.md}"
    fontSize: 1rem
  alert-warn:
    backgroundColor: "#FFFBEB"
    borderColor: "{colors.warning}"
    textColor: "{colors.warning}"
    borderRadius: "{rounded.md}"
    padding: "{spacing.md}"
    fontSize: 1rem
  primary-button:
    backgroundColor: "{colors.primary}"
    textColor: "#FFFFFF"
    borderRadius: "{rounded.md}"
    padding: "12px 24px"
    fontSize: 1.0625rem
    fontWeight: 600
    minHeight: 48px
  secondary-button:
    backgroundColor: "{colors.neutral-card}"
    textColor: "{colors.primary}"
    borderRadius: "{rounded.md}"
    border: "1.5px solid {colors.border}"
    padding: "12px 24px"
    fontSize: 1.0625rem
    fontWeight: 500
    minHeight: 48px
  voice-play-button:
    backgroundColor: "{colors.accent}"
    textColor: "#FFFFFF"
    borderRadius: "{rounded.full}"
    padding: "10px 20px"
    fontSize: 1rem
    fontWeight: 600
  measurement-input:
    backgroundColor: "{colors.neutral-bg}"
    borderRadius: "{rounded.md}"
    border: "1.5px solid {colors.border}"
    padding: "12px 16px"
    fontSize: 1.125rem
    minHeight: 52px
  image-thumbnail:
    borderRadius: "{rounded.md}"
    objectFit: cover
    border: "1px solid {colors.border}"
---

## Overview

智慧养老健康档案仪表盘服务于两类用户：
1. **服务人员** — 快速查看老人健康状态、记录测量数据
2. **老人家属** — 远程了解健康报告、AI建议

设计原则：
- **可读性优先**：字号≥16px，关键数据放大显示
- **信息分层**：一眼看清健康总览，深入可查详情
- **操作安全**：误触成本高的操作需二次确认
- **适老友好**：大触摸区域、高对比度、减少花哨动效

---

## Colors

- **Primary (#1E3A5F):** 政务蓝 — 页面主色、标题、Tab激活态、卡片左边框
- **Secondary (#4A6FA5):** 辅助蓝 — 次要文字、次级信息
- **Accent (#E8833A):** 关怀橙 — 重点数据（评分、异常值）、语音播报按钮
- **Accent-light (#FFF4EC):** 浅橙底 — 数据高亮标签背景
- **Success (#2E7D4A):** 正常/健康状态
- **Warning (#C0620A):** 中风险提示
- **Danger (#B91C1C):** 高风险/紧急状态
- **Neutral-bg (#F5F7FA):** 页面背景
- **Neutral-card (#FFFFFF):** 卡片背景
- **Text-primary (#1A1A1A):** 正文黑，WCAG AAA (14:1)
- **Text-secondary (#5A6478):** 次要文字，WCAG AA (5:1 on white)
- **Text-muted (#94A3B8):** 辅助说明文字
- **Border (#E2E8F0):** 分割线、输入框边框

---

## Typography

系统字体栈：`system-ui > "PingFang SC" > "Microsoft YaHei" > sans-serif`

| 层级 | 字号 | 字重 | 行高 | 用途 |
|------|------|------|------|------|
| display | 32px | 700 | 1.2 | 健康指标数值（血压/血糖等） |
| h1 | 24px | 600 | 1.3 | 卡片标题、姓名 |
| h2 | 20px | 600 | 1.4 | Tab名称、段落标题 |
| body-lg | 18px | 400 | 1.7 | AI建议正文（适老阅读） |
| body-md | 16px | 400 | 1.6 | 表格、列表正文 |
| label | 14px | 500 | 1.4 | 标签、小标题 |
| caption | 13px | 400 | 1.5 | 时间、备注 |

**字重使用规则**：标题和按钮必须 600+。正文 400/500 交替制造层次，避免全篇400导致视觉疲劳。

---

## Layout & Navigation

### 整体布局（宽屏 ≥1024px）

```
┌─────────────────────────────────────────────────────────────────┐
│  👤 老人姓名    76岁 女  护理等级: Ⅱ级    [生成报告] [返回列表]  │  ← 顶栏：档案概览+快捷操作
├─────────────────────────────────────────────────────────────────┤
│  [健康总览] [测量记录] [设备管理] [健康报告] [AI健康建议]        │  ← 横向Tab导航（ Pill式激活态）
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   ┌──────────────────────────────────────────────────────────┐   │
│   │  今日关键指标（横向大卡片，4宫格）                         │   │  ← 健康总览Tab
│   │  ┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐            │   │     优先展示关键数据
│   │  │血压     │ │血糖     │ │心率     │ │最近测量 │            │   │
│   │  │128/82  │ │ 5.6    │ │ 72     │ │今天 9:30│            │   │
│   │  │ mmHg   │ │mmol/L  │ │ 次/分  │ │         │            │   │
│   │  └────────┘ └────────┘ └────────┘ └────────┘            │   │
│   └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│   ┌─────────────────────┐  ┌────────────────────────────────┐  │
│   │  基础信息            │  │  风险预警面板                    │  │
│   │  血型: B型           │  │  ⚠ 跌倒风险: 中                  │  │
│   │  身高: 168cm         │  │  ⚠ 营养状态: 需关注             │  │
│   │  体重: 65kg          │  │  ✅ ADL评分: 72分（良好）       │  │
│   │  BMI: 23.0 正常       │  │  ✅ MMSE: 26分（正常）          │  │
│   └─────────────────────┘  └────────────────────────────────┘  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### Tab导航设计

- **横向排列**，不需要下拉菜单，符合老人使用习惯
- 激活态：`backgroundColor: #1E3A5F; color: #FFFFFF; border-radius: 8px`
- 非激活态：`color: #5A6478`，hover时轻微背景变化
- **5个Tab等宽分布**，保证每个Tab文字完整可见

### 响应式策略

| 屏幕宽度 | 布局变化 |
|----------|----------|
| ≥1280px | 双列卡片，指标卡片4列 |
| 1024-1279px | 双列卡片，指标卡片2列 |
| 768-1023px | 单列，卡片全宽 |
| <768px | 单列，字体不缩小（min 16px），触摸目标≥44px |

---

## Elevation & Depth

- **卡片默认**：`box-shadow: 0 2px 8px rgba(30,58,95,0.08)`
- **卡片悬停**：`box-shadow: 0 4px 16px rgba(30,58,95,0.14)` + `transform: translateY(-1px)`
- **抽屉面板**：`box-shadow: 0 8px 32px rgba(30,58,95,0.16)`（右滑抽屉）
- **避免过度阴影**：最多2层叠透，过多阴影让老人感到视觉疲劳

---

## Shapes

- **卡片**：`border-radius: 16px`，圆角适中，不过分圆润也不生硬
- **按钮**：`border-radius: 8px`，操作按钮四角分明
- **头像**：`border-radius: 50%`，圆形
- **标签/徽章**：`border-radius: 9999px`（药丸形），护理等级、风险级别用此形状
- **输入框**：`border-radius: 8px`，与按钮保持一致
- **卡片左边框强调**：`border-left: 4px solid #1E3A5F`，区分section归属

---

## Components

### 健康总览Tab

#### 关键指标卡片（4列横排）
- 卡片高度：120px，字号32px，数据值左对齐
- 顶部：指标名称（14px caption色）
- 中部：**数据值 display字号 32px** + 单位（14px）
- 底部：正常范围参考值（小字灰色）
- 异常值：数字变为 `color: #E8833A`（关怀橙），加粗
- 点击进入对应的测量记录详情

#### 基础信息卡片
- `border-left: 4px solid #1E3A5F`
- 2列网格：`身高/体重/BMI` | `血型/ADL/MMSE`
- 每项：label色 + 值，label在上值在下（纵向排列）

#### 风险预警面板
- 卡片标题左侧红色三角⚠图标
- 风险项：`backgroundColor: #FEF2F2; border-left: 3px solid #B91C1C`
- 正常项：`backgroundColor: #F0FDF4; border-left: 3px solid #2E7D4A`

---

### 测量记录Tab

#### 快速录入区（页面顶部）
- 大按钮「+ 记录测量」，48px高度，背景主色
- 录入抽屉包含：
  - 测量类型选择：巨大按钮式选择（不只用select下拉）
  - 数值输入：加大输入框，placeholder显示示例值
  - 拍照识别：相机图标按钮，触发 qwen-vl 图片理解
  - 提交按钮：48px高，橙色accent

#### 测量记录列表
- 每条记录为横向卡片（非表格行），高度≥72px
- 左侧：类型图标（血压计/血糖仪图标）48×48px
- 中部：数值（大字）+ 时间 + 测量方式标签
- 右侧：异常标记（红/绿/灰）或操作按钮
- 支持滑动删除（老人误触保护）

---

### 设备管理Tab

#### 设备列表卡片
- 每设备一个卡片，设备图标+名称+状态
- 状态徽章：在线（绿色）/ 离线（灰色）/ 低电量（橙色）
- 绑定新设备：大按钮居中，扫码图标

---

### 健康报告Tab

#### 报告列表（卡片网格，2列）
- 每张报告：封面图（AI生成，16:9比例）+ 标题 + 日期
- 封面图：80×80px缩略图，圆角4px，hover放大
- 操作按钮：下载PDF（主色按钮）、删除（红色文字按钮）
- 封面图为空时：显示淡蓝占位背景 + 报告类型图标

#### 报告详情抽屉
- 封面大图（宽度100%，高度200px）置顶
- PDF预览区（iframe或embed）
- AI建议摘要（来自wan2.2-t2i-plus封面图对应的报告解读）

---

### AI健康建议Tab

#### 护理建议卡片
- 标题：护理等级建议 + 语音播报按钮（橙色accent按钮）
- 风险预警高亮区：红色背景，优先显示
- 每条建议：
  - 类型标签（药丸形，不同颜色区分） + 优先级文字
  - 建议内容：18px正文字号（适老阅读）
  - 依据说明：小字灰色，解释AI给出这条建议的原因

#### 就医建议卡片
- 紧急程度徽章（大字红色/橙色/绿色）
- 建议科室 + 异常症状标签组
- 建议内容：18px正文

#### 语音播报
- 按钮：`backgroundColor: #E8833A; border-radius: 9999px; color: white`
- 播放中：按钮变为「⏸ 暂停播报」
- TTS来源：后端 `audioUrl` 字段，base64 data URI 或 OSS URL
- 不支持自动播放（浏览器限制），需用户点击触发

---

## Do's and Don'ts

### ✅ 推荐做法

- 关键数据（指标数值、风险提示）使用橙色/红色等高可见度颜色
- 每个Tab都有一个主要操作按钮，且位置固定（Tab下方/页面右上）
- 测量记录异常值高亮显示，不正常的数据必须比正常数据更醒目
- 语音播报按钮用橙色accent，与蓝色主色形成对比
- AI建议内容使用18px字号，符合老年人阅读习惯
- 卡片hover时有轻微阴影提升和上移效果，帮助用户感知可交互

### ❌ 避免做法

- **不要**用纯灰色展示异常数据，老年人对比度敏感
- **不要**把多个操作按钮挤在一起，至少间隔12px
- **不要**使用低于16px的正文字号
- **不要**在健康档案页面使用自动播放的动画或轮播图
- **不要**把风险提示折叠起来，必须平铺展示
- **不要**用图标替代文字标签（如用❤️代替"心率"），老年用户可能无法识别
