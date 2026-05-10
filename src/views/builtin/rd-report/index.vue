<script setup lang="ts">
import { ref, onMounted } from 'vue';

defineOptions({ name: 'RdReport' });

// 功能上线路线图数据（来源：git log --since="2026-04-10"）
const roadmapItems = ref([
  // ===== 2026-05 ===== //
  {
    date: '2026-05-10',
    version: 'v2.4',
    tag: '健康档案',
    type: 'feature',
    title: '健康档案卡片化布局重构',
    desc: '横向Tab导航（5个Tab）+ 顶栏老人信息概览 + 4宫格指标卡 + 语音播报',
    commits: ['健康档案v2横向Tab导航+卡片化布局'],
    color: '#1E3A5F'
  },
  {
    date: '2026-05-09',
    version: 'v2.3',
    tag: '流程优化',
    type: 'feature',
    title: '订单取消退回机制优化',
    desc: '订单取消→回到CREATED状态而非CANCELLED，清空派单信息可重新派单',
    commits: ['订单取消退回预约'],
    color: '#4A6FA5'
  },
  {
    date: '2026-05-09',
    version: 'v2.3',
    tag: 'UI优化',
    type: 'improvement',
    title: '超级管理员登录优化',
    desc: '点击"超级管理员"按钮直接登录，省去输入步骤',
    commits: ['UI优化'],
    color: '#E8833A'
  },
  {
    date: '2026-05-08',
    version: 'v2.3',
    tag: '合同',
    type: 'feature',
    title: '腾讯电子签完整集成',
    desc: '企业信息配置化 + 合同增强 + 真实API集成，支持所有状态查看原文',
    commits: ['腾讯电子签完整跑通', '企业信息配置化'],
    color: '#2E7D4A'
  },
  // ===== 2026-04 ===== //
  {
    date: '2026-04-26',
    version: 'v2.2',
    tag: '设备',
    type: 'feature',
    title: '设备管理模块上线',
    desc: '设备绑定+数据推送+健康档案创建按钮，扫码绑定设备自动推送数据',
    commits: ['设备管理模块'],
    color: '#1E3A5F'
  },
  {
    date: '2026-04-25',
    version: 'v2.2',
    tag: '数据',
    type: 'improvement',
    title: '测试数据补充',
    desc: '补充10个老人档案+5个服务商主数据，覆盖不同护理等级',
    commits: ['补充测试数据'],
    color: '#4A6FA5'
  },
  {
    date: '2026-04-24',
    version: 'v2.2',
    tag: 'AI',
    type: 'feature',
    title: 'AI健康建议引擎',
    desc: '阿里百炼通义千问集成，护理建议+就医建议+语音播报（TTS）',
    commits: ['AI健康建议'],
    color: '#E8833A'
  },
  {
    date: '2026-04-22',
    version: 'v2.1',
    tag: '合同',
    type: 'feature',
    title: '腾讯电子签合同管理',
    desc: '合同创建+签署链接+回调处理，支持模板20个必填控件',
    commits: ['腾讯电子签合同管理模块'],
    color: '#2E7D4A'
  },
  {
    date: '2026-04-20',
    version: 'v2.1',
    tag: '业务',
    type: 'feature',
    title: '预约+订单+服务日志+质检+评价业务链路串联',
    desc: '各环节互相引用，用户在任意环节都能顺畅流转到下一步',
    commits: ['业务链路串联'],
    color: '#1E3A5F'
  },
  {
    date: '2026-04-18',
    version: 'v2.0',
    tag: '平台',
    type: 'feature',
    title: '智慧居家养老服务管理平台正式上线',
    desc: '服务商管理+服务人员+老人档案+订单+预约+财务结算+驾驶舱',
    commits: ['平台上线'],
    color: '#E8833A'
  }
]);

const activeSection = ref('design');

function scrollTo(id: string) {
  activeSection.value = id;
  document.getElementById(id)?.scrollIntoView({ behavior: 'smooth', block: 'start' });
}
</script>

<template>
  <div class="rd-report-page">
    <!-- ===== 顶部导航 ===== -->
    <header class="rd-header">
      <div class="rd-header-inner">
        <div class="rd-logo">
          <span class="rd-logo-icon">📋</span>
          <span class="rd-logo-text">研发报告</span>
        </div>
        <nav class="rd-nav">
          <button
            v-for="item in [
              { id: 'design', label: '系统设计' },
              { id: 'modules', label: '模块构成' },
              { id: 'users', label: '用户群体' },
              { id: 'flow', label: '业务流程' },
              { id: 'roadmap', label: '功能路线图' }
            ]"
            :key="item.id"
            :class="['rd-nav-btn', { active: activeSection === item.id }]"
            @click="scrollTo(item.id)"
          >
            {{ item.label }}
          </button>
        </nav>
        <div class="rd-header-right">
          <span class="rd-version">智慧养老平台 v2.4</span>
          <span class="rd-date">2026-05-10</span>
        </div>
      </div>
    </header>

    <!-- ===== 主内容 ===== -->
    <main class="rd-main">

      <!-- ==================== 系统设计 ==================== -->
      <section id="design" class="rd-section">
        <h1 class="rd-section-title">
          <span class="rd-section-num">01</span>
          系统设计思路
        </h1>

        <div class="rd-card-grid">
          <div class="rd-card">
            <h3>🎯 项目定位</h3>
            <p>智慧居家养老服务管理平台是陕西红泥数智科技打造的AI+政务数字化产品，聚焦养老服务全流程数字化管理。</p>
            <div class="rd-tag-list">
              <span class="rd-tag">数据资产入表</span>
              <span class="rd-tag">AI+政务</span>
              <span class="rd-tag">智慧养老</span>
            </div>
          </div>
          <div class="rd-card">
            <h3>🛠 技术架构</h3>
            <p>前后端分离架构，支持灵活部署与扩展。</p>
            <div class="rd-tech-stack">
              <div class="rd-tech-item"><span class="rd-tech-label">前端</span><span>Vue3 + NaiveUI + TypeScript</span></div>
              <div class="rd-tech-item"><span class="rd-tech-label">后端</span><span>Spring Boot + MyBatis-Plus</span></div>
              <div class="rd-tech-item"><span class="rd-tech-label">数据库</span><span>MySQL 8.0</span></div>
              <div class="rd-tech-item"><span class="rd-tech-label">AI</span><span>阿里百炼（通义千问/文生图/TTS）</span></div>
              <div class="rd-tech-item"><span class="rd-tech-label">电子签</span><span>腾讯电子签ESS</span></div>
              <div class="rd-tech-item"><span class="rd-tech-label">部署</span><span>Nginx + Docker</span></div>
            </div>
          </div>
          <div class="rd-card">
            <h3>📐 核心设计原则</h3>
            <ul class="rd-principle-list">
              <li><strong>ID串联</strong>：orderNo是业务链核心，跨页面跳转传用户级标识</li>
              <li><strong>PROVIDER隔离</strong>：服务商数据严格隔离，查询必须带providerId</li>
              <li><strong>null优先</strong>：无数据时返回null而非0，前端显示"--"</li>
              <li><strong>联表注解</strong>：Entity联表字段加@TableField(exist=false)</li>
            </ul>
          </div>
          <div class="rd-card">
            <h3>🔐 权限体系</h3>
            <p>双层权限体系：</p>
            <ul class="rd-principle-list">
              <li><strong>菜单级</strong>：t_menu + t_role_menu，控制页面可见性</li>
              <li><strong>按钮级</strong>：t_permission + t_role_permission（历史兼容）</li>
              <li><strong>用户类型</strong>：SYSTEM（超级管理员）、ADMIN（管理员）、PROVIDER（服务商）</li>
            </ul>
          </div>
        </div>
      </section>

      <!-- ==================== 模块构成 ==================== -->
      <section id="modules" class="rd-section">
        <h1 class="rd-section-title">
          <span class="rd-section-num">02</span>
          模块构成
        </h1>

        <div class="rd-modules-table">
          <div class="rd-module-row header">
            <span>模块</span><span>路径</span><span>说明</span><span>主要实体</span>
          </div>
          <div class="rd-module-row"><span class="rd-module-name">服务商管理</span><span>/business/provider</span><span>服务商注册、审核、评级</span><span>Provider</span></div>
          <div class="rd-module-row"><span class="rd-module-name">服务人员</span><span>/business/staff</span><span>员工管理、照片上传、账户同步</span><span>Staff</span></div>
          <div class="rd-module-row"><span class="rd-module-name">客户档案</span><span>/business/elder</span><span>老人档案、护理等级、补贴类型</span><span>Elder</span></div>
          <div class="rd-module-row"><span class="rd-module-name">健康档案</span><span>/business/health-archive</span><span>健康总览、测量记录、设备、报告、AI建议</span><span>ElderHealth, HealthMeasurement</span></div>
          <div class="rd-module-row"><span class="rd-module-name">预约管理</span><span>/appointment</span><span>预约创建、状态流转（待确认→已确认→服务中→已完成）</span><span>Appointment</span></div>
          <div class="rd-module-row"><span class="rd-module-name">订单管理</span><span>/business/order</span><span>订单派单、状态管理、取消退回</span><span>Order</span></div>
          <div class="rd-module-row"><span class="rd-module-name">服务日志</span><span>/business/service-log</span><span>服务人员提交日志、审核流程</span><span>ServiceLog</span></div>
          <div class="rd-module-row"><span class="rd-module-name">质检管理</span><span>/business/quality</span><span>质量检查、整改流程</span><span>QualityCheck</span></div>
          <div class="rd-module-row"><span class="rd-module-name">满意度评价</span><span>/business/evaluation</span><span>满意度调查、评分统计</span><span>ServiceEvaluation</span></div>
          <div class="rd-module-row"><span class="rd-module-name">合同管理</span><span>/business/contract</span><span>腾讯电子签集成、模板签署、查看原文</span><span>Contract</span></div>
          <div class="rd-module-row"><span class="rd-module-name">财务结算</span><span>/business/financial</span><span>结算单生成、账单管理</span><span>Settlement</span></div>
          <div class="rd-module-row"><span class="rd-module-name">运营驾驶舱</span><span>/dashboard</span><span>数据大屏、统计图表</span><span>-</span></div>
          <div class="rd-module-row"><span class="rd-module-name">系统管理</span><span>/system</span><span>用户、角色、菜单、字典管理</span><span>User, Role, Menu, Dict</span></div>
        </div>
      </section>

      <!-- ==================== 用户群体 ==================== -->
      <section id="users" class="rd-section">
        <h1 class="rd-section-title">
          <span class="rd-section-num">03</span>
          用户群体
        </h1>

        <div class="rd-user-grid">
          <div class="rd-user-card" v-for="u in [
            { icon: '🏛', name: '市级政府', role: 'MUNICIPAL', desc: '最高监管视角，查看全市养老数据运营概况、统计报表、服务质量排名', tasks: ['数据驾驶舱', '服务商监管', '服务质量统计'] },
            { icon: '🏢', name: '区/县管理员', role: 'ADMIN', desc: '区域管理，负责本区服务商准入审核、订单协调、数据汇总', tasks: ['服务商审核', '订单协调', '财务结算审批'] },
            { icon: '🏠', name: '街道/乡镇', role: 'ADMIN', desc: '本地协调，链接社区与服务商，组织服务落地', tasks: ['服务分配', '社区协调', '投诉处理'] },
            { icon: '🏘', name: '社区', role: 'ADMIN', desc: '一线协调，收集老人需求，对接服务商资源', tasks: ['老人档案管理', '需求收集', '服务对接'] },
            { icon: '🏪', name: '服务商', role: 'PROVIDER', desc: '服务交付方，管理服务人员、接收派单、提交服务日志和质检', tasks: ['服务人员管理', '接收派单', '提交服务日志'] },
            { icon: '👤', name: '服务人员', role: 'STAFF', desc: '一线服务者，执行上门服务，记录服务内容和质量', tasks: ['执行服务', '提交服务日志', '接受质量检查'] },
            { icon: '👴', name: '老人/家属', role: 'ELDER', desc: '服务对象，查看健康档案、预约服务、满意度评价', tasks: ['预约服务', '查看健康报告', '满意度评价'] }
          ]" :key="u.name">
            <div class="rd-user-icon">{{ u.icon }}</div>
            <div class="rd-user-info">
              <div class="rd-user-name">{{ u.name }} <span class="rd-user-role">{{ u.role }}</span></div>
              <div class="rd-user-desc">{{ u.desc }}</div>
              <div class="rd-user-tasks">
                <span v-for="t in u.tasks" :key="t" class="rd-task-tag">{{ t }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ==================== 业务流程 ==================== -->
      <section id="flow" class="rd-section">
        <h1 class="rd-section-title">
          <span class="rd-section-num">04</span>
          业务流程
        </h1>

        <div class="rd-flow-diagram">
          <div class="rd-flow-step" v-for="(step, i) in [
            { n: '1', title: '预约', icon: '📅', color: '#4A6FA5', desc: '老人/社区提交服务预约，填写服务类型、时间、地址' },
            { n: '2', title: '审核确认', icon: '✅', color: '#4A6FA5', desc: '街道/社区审核预约，确认后通知服务商' },
            { n: '3', title: '派单', icon: '📋', color: '#1E3A5F', desc: '管理员/服务商指派服务人员，订单状态→已派单' },
            { n: '4', title: '服务', icon: '🛠', color: '#1E3A5F', desc: '服务人员上门服务，提交服务日志（含照片/描述）' },
            { n: '5', title: '质检', icon: '🔍', color: '#E8833A', desc: '服务商或政府进行质量检查，评分' },
            { n: '6', title: '评价', icon: '⭐', color: '#E8833A', desc: '老人/家属对服务进行满意度评价' },
            { n: '7', title: '结算', icon: '💰', color: '#2E7D4A', desc: '系统生成结算单，政府补贴+自付金额分开计算' }
          ]" :key="step.n">
            <div class="rd-flow-node" :style="{ borderColor: step.color, background: step.color + '15' }">
              <div class="rd-flow-num" :style="{ background: step.color }">{{ step.n }}</div>
              <div class="rd-flow-icon">{{ step.icon }}</div>
              <div class="rd-flow-title" :style="{ color: step.color }">{{ step.title }}</div>
              <div class="rd-flow-desc">{{ step.desc }}</div>
            </div>
            <div v-if="i < 6" class="rd-flow-arrow">→</div>
          </div>
        </div>

        <div class="rd-flow-note">
          <strong>订单取消/退回：</strong>订单取消后回到CREATED状态，清空派单信息可重新派单；预约退回后回到PENDING状态
        </div>
      </section>

      <!-- ==================== 功能路线图 ==================== -->
      <section id="roadmap" class="rd-section">
        <h1 class="rd-section-title">
          <span class="rd-section-num">05</span>
          功能上线路线图
          <span class="rd-roadmap-updated">（每次部署后自动更新）</span>
        </h1>

        <div class="rd-timeline">
          <div
            v-for="(item, index) in roadmapItems"
            :key="item.date + item.title"
            class="rd-timeline-item"
          >
            <div class="rd-timeline-marker" :style="{ background: item.color }">
              <span class="rd-timeline-num">{{ index + 1 }}</span>
            </div>
            <div class="rd-timeline-content">
              <div class="rd-timeline-header">
                <span class="rd-timeline-date">{{ item.date }}</span>
                <span class="rd-timeline-version">{{ item.version }}</span>
                <span class="rd-timeline-tag" :style="{ background: item.color + '20', color: item.color, borderColor: item.color }">
                  {{ item.tag }}
                </span>
                <span class="rd-timeline-type" :class="'type-' + item.type">
                  {{ item.type === 'feature' ? '✨ 新功能' : item.type === 'improvement' ? '⚡ 优化' : '🐛 修复' }}
                </span>
              </div>
              <h3 class="rd-timeline-title">{{ item.title }}</h3>
              <p class="rd-timeline-desc">{{ item.desc }}</p>
              <div v-if="item.commits" class="rd-timeline-commits">
                <span v-for="c in item.commits" :key="c" class="rd-commit-chip">{{ c }}</span>
              </div>
            </div>
          </div>
        </div>
      </section>

    </main>

    <!-- ===== 底部 ===== -->
    <footer class="rd-footer">
      <span>© 2026 陕西红泥数智科技有限公司</span>
      <span>智慧居家养老服务管理平台</span>
      <span>版本 v2.4 · 2026-05-10</span>
    </footer>
  </div>
</template>

<style scoped>
.rd-report-page {
  min-height: 100vh;
  background: #f5f7fa;
  font-family: system-ui, "PingFang SC", "Microsoft YaHei", sans-serif;
  color: #1a1a1a;
}

/* ===== 顶部导航 ===== */
.rd-header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: #1e3a5f;
  box-shadow: 0 2px 12px rgba(30,58,95,0.2);
}
.rd-header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  height: 60px;
  display: flex;
  align-items: center;
  gap: 32px;
}
.rd-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  color: white;
  font-weight: 700;
  font-size: 1.125rem;
  flex-shrink: 0;
}
.rd-logo-icon { font-size: 1.4rem; }
.rd-nav { display: flex; gap: 4px; flex: 1; }
.rd-nav-btn {
  background: transparent;
  border: none;
  color: rgba(255,255,255,0.7);
  padding: 6px 14px;
  border-radius: 6px;
  font-size: 0.9375rem;
  cursor: pointer;
  transition: all 0.2s;
  font-family: inherit;
}
.rd-nav-btn:hover { background: rgba(255,255,255,0.1); color: white; }
.rd-nav-btn.active { background: rgba(255,255,255,0.2); color: white; font-weight: 600; }
.rd-header-right { display: flex; gap: 16px; align-items: center; flex-shrink: 0; }
.rd-version { color: #e8833a; font-weight: 600; font-size: 0.9375rem; }
.rd-date { color: rgba(255,255,255,0.6); font-size: 0.875rem; }

/* ===== 主内容 ===== */
.rd-main {
  max-width: 1200px;
  margin: 0 auto;
  padding: 48px 24px 80px;
}

.rd-section { margin-bottom: 72px; }
.rd-section-title {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1e3a5f;
  margin-bottom: 32px;
  display: flex;
  align-items: center;
  gap: 12px;
  border-bottom: 3px solid #1e3a5f;
  padding-bottom: 12px;
}
.rd-section-num {
  background: #1e3a5f;
  color: white;
  width: 42px;
  height: 42px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.125rem;
  font-weight: 700;
}

/* ===== 系统设计卡片 ===== */
.rd-card-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.rd-card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(30,58,95,0.08);
  border: 1px solid #e2e8f0;
}
.rd-card h3 { font-size: 1.0625rem; font-weight: 600; color: #1e3a5f; margin-bottom: 12px; }
.rd-card p { font-size: 0.9375rem; color: #5a6478; line-height: 1.7; }
.rd-card ul { font-size: 0.9375rem; color: #5a6478; line-height: 1.8; padding-left: 18px; }
.rd-card li { margin-bottom: 4px; }
.rd-card li strong { color: #1e3a5f; }
.rd-tag-list { display: flex; gap: 8px; flex-wrap: wrap; margin-top: 12px; }
.rd-tag { background: #f0f4f8; color: #1e3a5f; padding: 4px 12px; border-radius: 20px; font-size: 0.875rem; font-weight: 500; }
.rd-tech-stack { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-top: 12px; }
.rd-tech-item { display: flex; gap: 8px; font-size: 0.9375rem; }
.rd-tech-label { color: #94a3b8; min-width: 40px; }
.rd-tech-item span:last-child { color: #1e3a5f; font-weight: 500; }
.rd-principle-list { list-style: none; padding: 0; }
.rd-principle-list li { padding-left: 16px; border-left: 3px solid #e8833a; margin-bottom: 10px; }

/* ===== 模块表格 ===== */
.rd-modules-table { background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 2px 8px rgba(30,58,95,0.08); }
.rd-module-row {
  display: grid;
  grid-template-columns: 1.5fr 1.5fr 2.5fr 2fr;
  padding: 14px 20px;
  border-bottom: 1px solid #f0f4f8;
  font-size: 0.9375rem;
  align-items: center;
}
.rd-module-row:last-child { border-bottom: none; }
.rd-module-row.header { background: #1e3a5f; color: white; font-weight: 600; font-size: 0.875rem; }
.rd-module-name { font-weight: 600; color: #1e3a5f; }
.rd-modules-table .rd-module-row:not(.header):hover { background: #f8fafc; }

/* ===== 用户群体 ===== */
.rd-user-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.rd-user-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(30,58,95,0.08);
  border: 1px solid #e2e8f0;
  align-items: flex-start;
}
.rd-user-icon { font-size: 2rem; flex-shrink: 0; width: 48px; text-align: center; }
.rd-user-name { font-weight: 700; font-size: 1.0625rem; color: #1e3a5f; margin-bottom: 6px; }
.rd-user-role { font-size: 0.75rem; background: #f0f4f8; color: #4a6fa5; padding: 2px 8px; border-radius: 10px; font-weight: 500; margin-left: 6px; }
.rd-user-desc { font-size: 0.875rem; color: #5a6478; line-height: 1.6; margin-bottom: 10px; }
.rd-user-tasks { display: flex; gap: 6px; flex-wrap: wrap; }
.rd-task-tag { background: #fff4ec; color: #e8833a; padding: 3px 10px; border-radius: 12px; font-size: 0.8125rem; font-weight: 500; }

/* ===== 业务流程 ===== */
.rd-flow-diagram { display: flex; align-items: center; gap: 0; overflow-x: auto; padding: 16px 0; }
.rd-flow-step { display: flex; align-items: center; flex-shrink: 0; }
.rd-flow-node {
  width: 130px;
  border-radius: 12px;
  padding: 16px 12px;
  border: 2px solid;
  text-align: center;
  position: relative;
}
.rd-flow-num {
  position: absolute;
  top: -12px;
  left: 50%;
  transform: translateX(-50%);
  width: 24px;
  height: 24px;
  border-radius: 50%;
  color: white;
  font-size: 0.8125rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.rd-flow-icon { font-size: 1.5rem; margin-bottom: 6px; }
.rd-flow-title { font-weight: 700; font-size: 0.9375rem; margin-bottom: 4px; }
.rd-flow-desc { font-size: 0.75rem; color: #5a6478; line-height: 1.5; }
.rd-flow-arrow { font-size: 1.5rem; color: #94a3b8; margin: 0 8px; }
.rd-flow-note { background: #fff4ec; border-left: 4px solid #e8833a; padding: 14px 18px; border-radius: 0 8px 8px 0; font-size: 0.9375rem; color: #5a6478; margin-top: 24px; }

/* ===== 时间轴 ===== */
.rd-roadmap-updated { font-size: 0.875rem; color: #94a3b8; font-weight: 400; margin-left: 8px; }
.rd-timeline { position: relative; padding-left: 48px; }
.rd-timeline::before {
  content: '';
  position: absolute;
  left: 18px;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(to bottom, #1e3a5f, #4a6fa5, #e8833a, #2e7d4a);
  border-radius: 2px;
}
.rd-timeline-item { position: relative; margin-bottom: 32px; }
.rd-timeline-marker {
  position: absolute;
  left: -42px;
  top: 4px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.rd-timeline-num { color: white; font-weight: 700; font-size: 0.8125rem; }
.rd-timeline-content {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(30,58,95,0.08);
  border: 1px solid #e2e8f0;
  transition: box-shadow 0.2s;
}
.rd-timeline-content:hover { box-shadow: 0 4px 16px rgba(30,58,95,0.14); }
.rd-timeline-header { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; margin-bottom: 10px; }
.rd-timeline-date { font-size: 0.875rem; color: #94a3b8; font-weight: 500; }
.rd-timeline-version { font-size: 0.75rem; background: #f0f4f8; color: #4a6fa5; padding: 2px 8px; border-radius: 10px; font-weight: 600; }
.rd-timeline-tag { font-size: 0.8125rem; padding: 3px 10px; border-radius: 10px; border: 1px solid; font-weight: 600; }
.rd-timeline-type { font-size: 0.8125rem; font-weight: 600; }
.type-feature { color: #2e7d4a; }
.type-improvement { color: #e8833a; }
.type-bugfix { color: #b91c1c; }
.rd-timeline-title { font-size: 1.0625rem; font-weight: 700; color: #1e3a5f; margin-bottom: 8px; }
.rd-timeline-desc { font-size: 0.9375rem; color: #5a6478; line-height: 1.6; margin-bottom: 12px; }
.rd-timeline-commits { display: flex; gap: 6px; flex-wrap: wrap; }
.rd-commit-chip { background: #f5f7fa; color: #4a6fa5; padding: 3px 10px; border-radius: 12px; font-size: 0.8125rem; font-family: monospace; }

/* ===== 底部 ===== */
.rd-footer {
  background: #1e3a5f;
  color: rgba(255,255,255,0.6);
  padding: 20px 24px;
  display: flex;
  justify-content: center;
  gap: 32px;
  font-size: 0.875rem;
}

/* ===== 响应式 ===== */
@media (max-width: 768px) {
  .rd-card-grid, .rd-user-grid { grid-template-columns: 1fr; }
  .rd-module-row { grid-template-columns: 1fr 1fr; font-size: 0.875rem; }
  .rd-module-row span:nth-child(3), .rd-module-row span:nth-child(4) { display: none; }
  .rd-flow-diagram { flex-direction: column; align-items: flex-start; }
  .rd-flow-step { flex-direction: column; align-items: flex-start; }
  .rd-flow-arrow { transform: rotate(90deg); margin: 8px 0; }
  .rd-nav { display: none; }
  .rd-header-right { display: none; }
}
</style>
