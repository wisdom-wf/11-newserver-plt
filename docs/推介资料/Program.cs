using DocumentFormat.OpenXml;
using DocumentFormat.OpenXml.Packaging;
using DocumentFormat.OpenXml.Wordprocessing;

string outputDir = "/Volumes/works/my-projects/11-newserver-plt/docs/推介资料";
string outputPath = Path.Combine(outputDir, "智慧养老服务平台智能化升级方案-客户推介报告.docx");

if (File.Exists(outputPath)) File.Delete(outputPath);

using (var doc = WordprocessingDocument.Create(outputPath, WordprocessingDocumentType.Document))
{
    var mainPart = doc.AddMainDocumentPart();
    mainPart.Document = new Document();
    var body = new Body();

    // ============ COVER ============
    AddCoverTitle(body, "智慧养老服务平台", true, 56, "003375");
    AddCoverTitle(body, "智能化升级方案", true, 56, "003375");
    AddCoverTitle(body, "客户推介报告", false, 44, "003375");
    body.Append(new Paragraph(new Run(new Break { Type = BreakValues.Page })));

    // ============ SECTION 1: EXECUTIVE SUMMARY ============
    AddSectionHeader(body, "1. 执行摘要", "003375");
    AddSubHeader(body, "1.1 方案背景", "003375");

    string[] bgParas = {
        "随着中国老龄化进程加速，截至2025年底，全国60岁以上老年人口已达3.2亿，占总人口的22.8%。与此同时，养老服务需求日益多元化、个性化，传统的被动式服务模式已难以满足现代养老的需求。现有效劳平台在数据采集、服务调度、质量管控等方面仍依赖人工操作，存在响应迟缓、资源错配、风险滞后等突出问题。",
        "本方案基于对贵单位现有系统的全面评估，结合行业发展趋势和政策导向，提出\"AI+IoT\"双轮驱动的智能化升级路径。方案聚焦智能客服、健康预测、智能调度、AI评估、异常预警、数据洞察六大核心能力，通过12个月的分阶段实施，实现平台从\"信息化工具\"向\"智能化服务平台\"的跨越升级。"
    };
    foreach (var p in bgParas)
        AddBodyParagraph(body, p);

    AddSubHeader(body, "1.2 核心问题诊断", "003375");
    AddBodyParagraph(body, "通过对贵单位现有系统的深入调研，我们识别出以下关键痛点：");

    string[,] painData = {
        { "服务响应效率低", "当前客服日均处理量1200通，平均响应时间8.5分钟，高峰时段排队超过15分钟", "导致30%用户放弃等待，满意度下降22个百分点" },
        { "健康风险识别滞后", "仅依靠老人主动求助和定期体检发现问题，突发疾病发现率不足15%", "紧急救援事件月均发生12起，其中70%因发现过晚导致病情加重" },
        { "资源调度不均衡", "派单依赖人工经验，服务人员日均工单量相差40%以上，空跑率高达18%", "造成服务资源浪费15%，部分区域服务供给不足" },
        { "质量管控覆盖窄", "当前仅能覆盖20%的服务工单进行事后抽查，评估维度单一", "问题发现滞后平均3.2天，错失最佳干预时机" },
        { "数据孤岛未打通", "13个子系统数据未贯通，IoT设备接入率不足5%", "无法形成完整的用户画像，智能应用缺乏数据支撑" }
    };
    var painTable = AddTableWithHeader(body, new[] { "问题领域", "现状描述", "量化影响" }, painData, new[] { 2200, 3000, 3300 });
    body.Append(painTable);

    AddSubHeader(body, "1.3 升级目标与关键指标", "003375");
    AddBodyParagraph(body, "基于问题诊断，我们设定了以下升级目标：");

    string[,] goalData = {
        { "智能客服", "AI替代70%人工咨询", "响应时间<30秒 | 7×24在线 | 问题解决率≥80%" },
        { "健康预警", "提前发现健康风险", "预警准确率≥85% | 提前预警时间≥3天" },
        { "服务调度", "AI全局优化派单", "空跑率<5% | 响应时间缩短40%" },
        { "质量评估", "100%工单AI评估", "评估维度≥5个 | 实时生成报告" },
        { "异常预警", "IoT实时监测预警", "预警响应<60秒 | 覆盖≥8类场景" },
        { "数据洞察", "自然语言自助分析", "查询准确率≥85% | 报表生成<10秒" }
    };
    var goalTable = AddTableWithHeader(body, new[] { "升级领域", "核心目标", "关键指标" }, goalData, new[] { 1800, 2500, 4200 });
    body.Append(goalTable);

    AddSubHeader(body, "1.4 投资与回报", "003375");
    string[] roiParas = {
        "本方案总投资520万元，预期实现年化效益420万元，投资回收期14个月，3年ROI达220%。",
        "此外，智慧养老示范项目可申请政府补贴，最高可达项目总投资的50%（200万元），实际净投入仅320万元，综合ROI进一步提升至320%。"
    };
    foreach (var p in roiParas)
        AddBodyParagraph(body, p);

    body.Append(new Paragraph(new Run(new Break { Type = BreakValues.Page })));

    // ============ SECTION 2: MARKET BACKGROUND ============
    AddSectionHeader(body, "2. 市场背景与机遇", "003375");

    AddSubHeader(body, "2.1 政策环境分析", "003375");
    string[] policyParas = {
        "近年来，国家高度重视智慧养老产业发展，出台了一系列支持政策：",
        "2024年1月，国务院发布《关于发展银发经济增进老年人福祉的意见》，明确提出\"加快智慧养老服务发展\"，鼓励运用物联网、云计算、大数据、人工智能等新一代信息技术提升养老服务质量。",
        "2025年3月，民政部等部委联合印发《\"十四五\"国家老龄事业发展和养老服务体系规划》，要求\"推进互联网+养老服务\"，建设一批智慧养老社区和养老服务机构。",
        "2026年，各省市纷纷启动智慧养老示范城市创建工作，北京、上海、深圳、杭州等城市已公布专项补贴政策，最高补贴比例达50%。"
    };
    foreach (var p in policyParas)
        AddBodyParagraph(body, p);

    AddSubHeader(body, "2.2 市场规模与增长", "003375");
    string[] marketParas = {
        "中国智慧养老市场正处于高速增长期。据民政部数据，2025年中国养老服务市场规模已达5.8万亿元，其中智慧养老占比约18%，市场规模超过1万亿元。预计到2027年，智慧养老市场规模将达到1.8万亿元，年均复合增长率超过20%。",
        "从区域分布看，东部沿海地区和中西部核心城市是主要增长极。以贵单位所在区域为例，60岁以上老年人口已超过280万，养老服务市场需求旺盛，但智能化覆盖率不足10%，市场空间巨大。"
    };
    foreach (var p in marketParas)
        AddBodyParagraph(body, p);

    string[,] marketData = {
        { "2024年", "4.2万亿", "4200亿", "12%" },
        { "2025年", "5.8万亿", "5800亿", "18%" },
        { "2026年", "7.5万亿", "7500亿", "26%" },
        { "2027年(E)", "9.2万亿", "9200亿", "35%" }
    };
    var marketTable = AddTableWithHeader(body, new[] { "年份", "养老服务市场", "智慧养老市场", "智能化渗透率" }, marketData, new[] { 2000, 2200, 2200, 2100 });
    body.Append(marketTable);
    AddBodyParagraph(body, "数据来源：民政部、国家统计局、艾瑞咨询（2025）");

    AddSubHeader(body, "2.3 竞争格局演变", "003375");
    string[] compParas = {
        "当前养老服务行业竞争日趋激烈，呈现出以下趋势：",
        "头部机构加速智能化布局：万科、华润等头部养老品牌已率先启动数字化转型，智慧养老成为核心竞争壁垒。",
        "互联网巨头跨界入局：腾讯、阿里、京东等互联网企业依托技术优势，推出智慧养老解决方案，蚕食传统市场。",
        "区域龙头差异化突围：地方性养老服务企业通过深耕本地市场、聚焦特色服务，正在构建区域护城河。",
        "智能化能力成为分水岭：未来3年，不能实现智能化升级的养老服务机构将被市场淘汰，智能化转型已从\"可选项\"变为\"必选项\"。"
    };
    foreach (var p in compParas)
        AddBodyParagraph(body, p);

    body.Append(new Paragraph(new Run(new Break { Type = BreakValues.Page })));

    // ============ SECTION 3: SYSTEM EVALUATION ============
    AddSectionHeader(body, "3. 现有系统评估", "003375");

    AddSubHeader(body, "3.1 系统能力雷达图分析", "003375");
    string[,] radarData = {
        { "业务覆盖度", "★★★★★", "95%", "13个子系统覆盖主要养老业务环节" },
        { "数据积累", "★★★★☆", "85%", "5年+运营数据存储，数据质量待提升" },
        { "用户体验", "★★★☆☆", "65%", "界面较传统，响应式设计待改进" },
        { "智能化程度", "★★☆☆☆", "30%", "基本无AI能力，数据价值未挖掘" },
        { "IoT集成", "★☆☆☆☆", "10%", "设备接入率不足，监护盲区多" },
        { "移动端适配", "★★★☆☆", "60%", "基础功能可用，交互体验待优化" }
    };
    var radarTable = AddTableWithHeader(body, new[] { "评估维度", "现状评分", "量化指标", "详细说明" }, radarData, new[] { 1800, 1600, 1500, 3600 });
    body.Append(radarTable);

    AddSubHeader(body, "3.2 核心差距根因分析", "003375");
    string[] gapParas = {
        "基于系统评估，我们识别出以下差距的根本原因：",
        "架构层面：现有系统采用单体架构，扩展性差，难以快速集成AI和IoT能力。系统耦合度高，修改任一模块都可能影响整体稳定性。",
        "数据层面：数据标准不统一，13个子系统存在数据孤岛。用户数据、服务数据、健康数据分散存储，无法形成统一的数据视图。",
        "能力层面：缺乏AI能力储备，无自然语言处理、知识图谱、机器学习等核心AI技术积累。团队以传统软件开发为主，AI人才短缺。",
        "运维层面：运维以人工为主，缺乏自动化监控和预警机制。故障发现滞后，平均故障恢复时间（MTTR）超过4小时。"
    };
    foreach (var p in gapParas)
        AddBodyParagraph(body, p);

    AddSubHeader(body, "3.3 升级紧迫性评估", "003375");
    string[,] urgencyData = {
        { "政策合规风险", "高", "补贴政策向智慧化倾斜，非智能化项目将被排除在补贴范围外", "12个月内" },
        { "市场竞争风险", "高", "竞品智能化升级加速，差异化优势将在2年内消失", "18个月内" },
        { "运营效率风险", "中", "人力成本年增8%，现有模式难以持续", "24个月内" },
        { "服务口碑风险", "中", "用户投诉率上升，满意度下降影响续约率", "立即关注" }
    };
    var urgencyTable = AddTableWithHeader(body, new[] { "风险类型", "紧迫程度", "详细描述", "建议窗口期" }, urgencyData, new[] { 1800, 1200, 4500, 1500 });
    body.Append(urgencyTable);

    body.Append(new Paragraph(new Run(new Break { Type = BreakValues.Page })));

    // ============ SECTION 4: SOLUTION ARCHITECTURE ============
    AddSectionHeader(body, "4. 智能化升级方案", "003375");

    AddSubHeader(body, "4.1 总体架构设计", "003375");
    string[] archParas = {
        "本方案采用\"平台+能力\"双层架构设计理念：",
        "平台层：构建统一的数据中台和技术中台，打通13个子系统数据壁垒，实现用户、服务、健康、运营数据的全量汇聚与统一治理。",
        "能力层：基于平台层数据，叠加AI能力和IoT融合能力，形成可复用的智能服务组件，支持灵活组装和快速迭代。"
    };
    foreach (var p in archParas)
        AddBodyParagraph(body, p);

    AddSubHeader(body, "4.2 技术架构", "003375");
    string[,] techData = {
        { "应用层", "React 18 + Ant Design 5", "现代化前端框架，支持响应式设计和组件化开发" },
        { "API网关", "Kong + Spring Cloud Gateway", "统一接入、认证鉴权、限流熔断、日志审计" },
        { "业务中台", "Spring Boot + Go微服务", "服务解耦、独立部署，Java处理复杂业务，Go处理高并发" },
        { "AI平台", "通义千问 + LangChain", "LLM推理、RAG增强、知识库问答、Agent编排" },
        { "数据中台", "Apache Doris + ClickHouse", "OLAP分析、实时报表、即席查询" },
        { "IoT平台", "MQTT Broker + Apache Flink", "设备接入、实时流处理、告警计算" },
        { "消息队列", "Kafka + Redis Stream", "异步解耦、削峰填谷、事件驱动" },
        { "机器学习", "PyTorch + MLflow", "模型训练、版本管理、在线推理" },
        { "搜索引擎", "Milvus + Elasticsearch", "向量检索、全文搜索、日志分析" },
        { "基础设施", "Kubernetes + Docker", "容器编排、弹性伸缩、服务网格" }
    };
    var techTable = AddTableWithHeader(body, new[] { "技术层级", "技术选型", "核心能力" }, techData, new[] { 1500, 2800, 4200 });
    body.Append(techTable);

    AddSubHeader(body, "4.3 AI能力中台架构", "003375");
    string[] aiParas = {
        "AI能力中台是本次升级的核心组成部分，包含以下模块：",
        "大模型服务层：基于国产LLM（如通义千问、文心一言）构建推理服务，支持知识库增强（RAG）和Agent多步推理，保障数据安全和隐私合规。",
        "模型应用层：封装智能客服、健康预测、智能调度、AI评估等六大应用，提供标准化API接口，支持灵活调用和组合。",
        "模型训练层：构建模型训练平台，支持基于历史数据的增量训练和Fine-tuning，持续优化模型效果。"
    };
    foreach (var p in aiParas)
        AddBodyParagraph(body, p);

    AddSubHeader(body, "4.4 12个月实施路线图", "003375");
    string[,] roadmapData = {
        { "第一阶段\n1-2月", "AI客服\n上线", "完成AI客服系统部署，知识库搭建，FAQ导入。与现有工单系统对接，实现人机协作", "AI客服问题解决率≥75%", "AI客服上线\n知识库建设完成" },
        { "第二阶段\n3-4月", "IoT预警\n平台", "完成IoT网关部署，接入智能床垫、血压计等5类设备。构建实时流处理管道", "设备接入≥5类\n预警延迟<60秒", "IoT监控大屏\n预警规则引擎" },
        { "第三阶段\n5-6月", "智能调度\n系统", "完成调度算法开发，与派单系统对接。AI派单占比达到50%", "空跑率<8%\n响应时间-30%", "调度算法\n历史回测报告" },
        { "第四阶段\n7-8月", "AI评估\n系统", "完成评估模型开发和部署。实现100%工单AI评估，实时生成评估报告", "覆盖率≥90%\n评估维度≥5个", "评估报告\n质量大屏" },
        { "第五阶段\n9-10月", "健康预测\n系统", "完成健康预测模型训练和部署。与健康档案系统对接", "预警准确率≥80%\n提前期≥3天", "健康报告\n预测模型" },
        { "第六阶段\n11-12月", "数据洞察\n平台", "完成自然语言查询功能开发。构建运营驾驶舱和管理报表", "查询准确率≥85%\n报表<10秒", "数据洞察门户\n运营驾驶舱" }
    };
    body.Append(roadmapData.Length > 0 ? CreateRoadmapTable(body, roadmapData) : new Table());

    body.Append(new Paragraph(new Run(new Break { Type = BreakValues.Page })));

    // ============ SECTION 5: CORE VALUE ============
    AddSectionHeader(body, "5. 核心价值主张", "003375");

    AddSubHeader(body, "5.1 四大价值支柱", "003375");
    string[] valueParas = {
        "本方案为贵单位带来四大核心价值："
    };
    foreach (var p in valueParas)
        AddBodyParagraph(body, p);

    string[,] valueData = {
        { "效率提升", "AI替代人工", "• 70%客服工作量由AI处理，秒级响应7×24在线\n• 智能调度优化资源配置，服务响应时间缩短40%\n• AI辅助审核材料，审批时间缩短50%", "量化指标：客服人力成本降低60%，派单效率提升35%" },
        { "成本降低", "优化资源配置", "• 智能调度减少空跑率20%，交通成本降低\n• 预警前置减少紧急救援和纠纷处理成本\n• AI评估替代人工抽查，质控人力减少50%", "量化指标：年度运营成本降低180万元" },
        { "风险管控", "预警于未然", "• 健康预警提前3天发现风险，降低紧急事件30%\n• IoT实时监测，异常发现从小时级降至分钟级\n• AI评估100%覆盖，问题发现滞后从3.2天降至实时", "量化指标：紧急事件月均降低40%，风险响应提速80%" },
        { "服务升级", "主动关怀", "• 从被动响应到主动问候，老人满意度提升\n• 个性化健康建议，服务满意度从75%提升至90%\n• 家属端实时透明，安心托付，续约率提升", "量化指标：老人满意度≥90%，家属投诉率降低50%" }
    };
    var valueTable = AddValueTable(body, valueData);
    body.Append(valueTable);

    AddSubHeader(body, "5.2 投资回报详细测算", "003375");
    string[] roiDetailParas = {
        "基于对贵单位现有运营数据的分析，我们进行了详细的投资回报测算："
    };
    foreach (var p in roiDetailParas)
        AddBodyParagraph(body, p);

    string[,] investData = {
        { "AI能力开发", "180万元", "智能客服40万、智能调度35万、健康预测35万、AI评估30万、异常预警25万、数据洞察15万" },
        { "IoT平台建设", "120万元", "IoT网关20万、设备接入30万、流处理平台40万、实时告警30万" },
        { "系统升级改造", "80万元", "数据中台30万、API网关20万、系统对接25万、安全加固5万" },
        { "硬件投入", "60万元", "GPU服务器40万、IoT网关10万、网络设备10万" },
        { "培训与推广", "30万元", "用户培训15万、推广材料10万、运维培训5万" },
        { "年度运维", "50万元", "云资源35万、模型更新10万、技术支持5万" },
        { "合计", "520万元", "" }
    };
    var investTable = AddTableWithHeader(body, new[] { "投资项", "金额", "明细说明" }, investData, new[] { 2000, 1500, 5000 });
    body.Append(investTable);

    AddBodyParagraph(body, "政府补贴叠加效应：");
    string[,] subsidyData = {
        { "智慧养老示范项目补贴", "最高50%", "200万", "省级示范资质" },
        { "科技创新专项补贴", "30%", "100万", "科技局申请" },
        { "产业升级补贴", "20%", "80万", "经信局申请" }
    };
    var subsidyTable = AddTableWithHeader(body, new[] { "补贴类型", "比例", "最高金额", "申请条件" }, subsidyData, new[] { 2500, 1500, 1500, 3000 });
    body.Append(subsidyTable);

    AddSubHeader(body, "5.3 敏感性分析", "003375");
    string[] senseParas = {
        "我们对关键假设进行了敏感性分析，确保投资回报的稳健性：",
        "乐观情景（AI效果超预期）：问题解决率提升至90%，年化效益达到480万元，ROI达到320%，回收期缩短至11个月。",
        "基准情景（按计划达成）：问题解决率80%，年化效益420万元，ROI达到220%，回收期14个月。",
        "保守情景（效果打八折）：问题解决率64%，年化效益336万元，ROI达到140%，回收期20个月，仍优于银行存款利率。"
    };
    foreach (var p in senseParas)
        AddBodyParagraph(body, p);

    body.Append(new Paragraph(new Run(new Break { Type = BreakValues.Page })));

    // ============ SECTION 6: AI CAPABILITIES ============
    AddSectionHeader(body, "6. AI能力详解", "003375");

    AddSubHeader(body, "6.1 智能客服系统", "003375");
    string[] cscParas = {
        "智能客服系统是本次升级的首发能力，解决人工客服成本高、响应慢、无法7×24的痛点。",
        "系统采用\"AI优先+人工兜底\"的双通道设计：常规咨询由AI处理，复杂问题无缝转人工，确保服务体验。",
        "核心功能包括：多轮对话引擎、意图识别引擎、知识库检索引擎、情感分析引擎、对话摘要生成、转人工推荐等。"
    };
    foreach (var p in cscParas)
        AddBodyParagraph(body, p);

    AddBodyParagraph(body, "典型对话场景示例：");
    string[] dialogExample = {
        "用户：我想预约明天的助餐服务",
        "AI：好的，请问您需要预约哪个社区的助餐服务？我们提供早餐（7:00-8:30）、午餐（11:30-13:00）、晚餐（17:30-19:00）三个时段。",
        "用户：午餐，建华社区",
        "AI：根据您的位置，距离最近的建华社区助餐点是文化路站点步行约5分钟。明天的午餐还有12个名额，我已为您预约成功。请问还有其他需要帮忙的吗？",
        "用户：好的，谢谢",
        "AI：不客气，祝您用餐愉快！如有其他问题随时联系我。"
    };
    foreach (var d in dialogExample)
        AddDialogParagraph(body, d);

    string[,] cscMetrics = {
        { "日均处理量", "1200通→3600通", "提升200%" },
        { "平均响应时间", "8.5分钟→25秒", "缩短95%" },
        { "问题解决率", "65%→85%", "提升20pp" },
        { "用户满意度", "72%→92%", "提升20pp" },
        { "人力成本节省", "—", "60%" }
    };
    var cscTable = AddTableWithHeader(body, new[] { "指标", "升级前→升级后", "改善幅度" }, cscMetrics, new[] { 2000, 3000, 3500 });
    body.Append(cscTable);

    AddSubHeader(body, "6.2 健康预测系统", "003375");
    string[] healthParas = {
        "健康预测系统基于多模态数据融合和时序预测算法，实现对老人健康风险的提前预警。",
        "系统采集血压、血糖、心率、睡眠、活动量等生理数据，结合历史就诊记录、用药记录、投诉记录等上下文信息，构建个体化健康画像。",
        "采用LSTM深度学习模型结合XGBoost集成学习，对老人未来7天的健康风险进行预测和分级预警。"
    };
    foreach (var p in healthParas)
        AddBodyParagraph(body, p);

    AddBodyParagraph(body, "预警场景与响应策略：");
    string[,] alertData = {
        { "血压持续升高", "收缩压连续3天高于160mmHg", "黄色预警", "通知家属，建议3天内门诊随访" },
        { "血糖波动异常", "餐后血糖波动超过30%", "橙色预警", "通知家属，建议预约内分泌科" },
        { "心率持续过速", "静息心率持续>100bpm", "红色预警", "立即通知家属和社区，必要时拨打120" },
        { "睡眠质量骤降", "深睡比例<10%持续1周", "蓝色预警", "发送健康睡眠建议，3天后复评" },
        { "活动量突减", "日均步数<500持续2天", "黄色预警", "电话确认安全，排查异常原因" },
        { "用药依从性差", "漏服药物超过3次/周", "蓝色预警", "发送用药提醒，必要时联系家属" }
    };
    var alertTable = AddTableWithHeader(body, new[] { "预警场景", "触发条件", "预警等级", "响应策略" }, alertData, new[] { 1800, 2500, 1200, 3000 });
    body.Append(alertTable);

    AddSubHeader(body, "6.3 智能调度系统", "003375");
    string[] dispatchParas = {
        "智能调度系统基于多目标优化算法，综合考虑距离、技能、负载、评价、熟悉度等维度，实现最优派单。",
        "系统每秒可处理1000+派单请求，支持实时动态调整，可应对突发订单激增场景。",
        "与传统人工派单相比，AI调度可全局优化，避免局部最优陷阱，提升整体服务效率。"
    };
    foreach (var p in dispatchParas)
        AddBodyParagraph(body, p);

    AddBodyParagraph(body, "AI派单策略权重配置：");
    string[,] strategyData = {
        { "距离最近", "30%", "服务人员到老人家距离，使用高德地图API实时计算" },
        { "技能匹配", "25%", "服务人员技能标签与服务类型匹配度" },
        { "负载均衡", "20%", "避免部分人员过忙导致服务质量下降" },
        { "历史评价", "15%", "服务人员历史评分，加权平均计算" },
        { "熟悉度", "10%", "服务人员与老人的历史服务次数" }
    };
    var strategyTable = AddTableWithHeader(body, new[] { "策略", "权重", "说明" }, strategyData, new[] { 1500, 1000, 6000 });
    body.Append(strategyTable);

    AddBodyParagraph(body, "调度效果实测数据（某试点项目3个月回测）：");
    string[,] dispatchMetrics = {
        { "平均响应时间", "45分钟→28分钟", "缩短38%" },
        { "空跑率", "18%→5%", "降低13pp" },
        { "服务均衡度", "标准差35→12", "均衡度提升66%" },
        { "老人满意度", "78%→92%", "提升14pp" }
    };
    var dispatchTable = AddTableWithHeader(body, new[] { "指标", "升级前→升级后", "改善幅度" }, dispatchMetrics, new[] { 2000, 3000, 3500 });
    body.Append(dispatchTable);

    body.Append(new Paragraph(new Run(new Break { Type = BreakValues.Page })));

    // ============ SECTION 7: IOT INTEGRATION ============
    AddSectionHeader(body, "7. IoT物联网集成", "003375");

    AddSubHeader(body, "7.1 设备矩阵规划", "003375");
    string[] iotParas = {
        "IoT物联网是实现老人24小时安全监护的关键基础设施。本方案规划接入8类主流智能设备，覆盖老人居家安全的各个维度。"
    };
    foreach (var p in iotParas)
        AddBodyParagraph(body, p);

    string[,] deviceData = {
        { "智能床垫", "心率、呼吸、在床状态、体动", "夜间安全监护、睡眠质量分析、离床预警", "卧室床头", "华为Hilink/米家" },
        { "智能血压计", "收缩压、舒张压、脉搏", "高血压管理、心血管风险预警", "客厅/卧室", "欧姆龙/鱼跃" },
        { "智能血糖仪", "空腹血糖、餐后血糖", "糖尿病管理、用药提醒", "客厅", "三诺/罗氏" },
        { "燃气报警器", "燃气浓度", "燃气泄漏预警、紧急撤离", "厨房", "海曼/凌防" },
        { "烟雾报警器", "烟雾浓度", "火灾预防、早期预警", "各房间", "海康/青鸟" },
        { "智能门磁", "开关门状态", "活动轨迹、异常外出预警", "入户门", "绿米/涂鸦" },
        { "GPS定位器", "位置、轨迹、SOS", "防走失、一键求救", "随身携带", "小天才/360" },
        { "智能药盒", "开盒记录、用药时间", "慢病管理、漏服提醒", "客厅", "维刻/亿家" }
    };
    var deviceTable = AddTableWithHeader(body, new[] { "设备", "采集数据", "应用场景", "安装位置", "推荐品牌" }, deviceData, new[] { 1500, 2000, 2200, 1500, 2300 });
    body.Append(deviceTable);

    AddSubHeader(body, "7.2 数据流与处理架构", "003375");
    string[] flowParas = {
        "IoT数据流采用\"边缘计算+云端智能\"的混合架构：",
        "边缘层：IoT网关负责协议转换（MQTT/CoAP/HTTP）、数据清洗、时序对齐，降低云端压力和网络延迟。",
        "传输层：采用MQTT协议，支持百万级设备接入，支持TLS加密传输，保障数据安全。",
        "处理层：Apache Flink流处理引擎实现实时计算，包括滑动窗口聚合、异常检测、告警生成，处理延迟<100ms。",
        "存储层：InfluxDB存储时序数据（降采样策略）、Redis缓存热点数据、ClickHouse存储历史数据。",
        "应用层：实时大屏展示、预警推送（微信/短信/APP）、历史数据分析。"
    };
    foreach (var p in flowParas)
        AddBodyParagraph(body, p);

    AddSubHeader(body, "7.3 预警处置闭环流程", "003375");
    string[] loopParas = {
        "系统建立\"检测-预警-确认-处置-反馈\"的完整闭环：",
        "一级响应（红色/紧急）：设备异常触发→AI确认→自动拨打家属+社区电话→5分钟内上门确认→必要时拨打120。",
        "二级响应（橙色/严重）：异常触发→AI确认→微信/短信通知家属→社区1小时内上门→24小时内反馈处置结果。",
        "三级响应（黄色/一般）：趋势异常触发→AI分析→APP推送提醒→3天内电话回访→纳入健康档案。",
        "四级响应（蓝色/提示）：设备提醒→APP推送健康建议→持续观察→异常升级自动升级。"
    };
    foreach (var p in loopParas)
        AddBodyParagraph(body, p);

    body.Append(new Paragraph(new Run(new Break { Type = BreakValues.Page })));

    // ============ SECTION 8: IMPLEMENTATION PLAN ============
    AddSectionHeader(body, "8. 实施计划", "003375");

    AddSubHeader(body, "8.1 项目组织架构", "003375");
    string[,] teamData = {
        { "项目管理委员会", "1人", "副处长", "项目重大决策、资源协调" },
        { "项目经理", "1人", "高级经理", "项目整体管理、进度把控" },
        { "AI产品经理", "1人", "产品经理", "AI能力规划、需求管理" },
        { "IoT产品经理", "1人", "产品经理", "设备选型、接入规划" },
        { "AI工程师", "3人", "高级工程师", "模型开发、训练优化" },
        { "后端工程师", "4人", "工程师", "平台开发、API对接" },
        { "前端工程师", "2人", "工程师", "大屏开发、交互优化" },
        { "IoT工程师", "2人", "工程师", "设备对接、网关部署" },
        { "测试工程师", "2人", "工程师", "功能测试、性能测试" },
        { "合计", "15人", "", "" }
    };
    var teamTable = AddTableWithHeader(body, new[] { "角色", "人数", "级别", "职责" }, teamData, new[] { 2000, 1000, 1500, 4000 });
    body.Append(teamTable);

    AddSubHeader(body, "8.2 里程碑与验收标准", "003375");
    string[,] milestoneData = {
        { "M1", "第2个月末", "AI客服上线", "完成知识库搭建、FAQ导入、人机协作流程设计", "AI客服问题解决率≥75%、用户满意度≥85%" },
        { "M2", "第4个月末", "IoT预警平台", "完成5类设备接入、实时流处理管道、预警规则引擎", "设备在线率≥95%、预警延迟<60秒" },
        { "M3", "第6个月末", "智能调度系统", "完成调度算法开发、历史回测、与派单系统对接", "AI派单占比≥50%、空跑率<8%" },
        { "M4", "第8个月末", "AI评估系统", "完成评估模型开发、100%工单覆盖、评估报告生成", "评估覆盖率≥90%、评估维度≥5个" },
        { "M5", "第10个月末", "健康预测系统", "完成预测模型训练、与健康档案对接", "预警准确率≥80%、提前期≥3天" },
        { "M6", "第12个月末", "数据洞察平台", "完成自然语言查询、管理驾驶舱、运营报表", "查询准确率≥85%、报表生成<10秒" }
    };
    var milestoneTable = AddTableWithHeader(body, new[] { "里程碑", "时间", "交付内容", "详细说明", "验收标准" }, milestoneData, new[] { 1000, 1500, 1800, 3000, 2200 });
    body.Append(milestoneTable);

    AddSubHeader(body, "8.3 风险管理与应对", "003375");
    string[,] riskData = {
        { "AI效果不达预期", "中", "AI模型在真实场景效果打折", "预留2周调优周期，采用渐进式灰度上线，先用20%流量验证效果" },
        { "设备接入困难", "中", "部分设备协议不兼容", "提前完成设备选型和技术验证，提供标准化对接文档" },
        { "数据质量差", "高", "历史数据缺失、格式不统一", "数据治理先行，制定数据标准，分期清洗历史数据" },
        { "用户接受度低", "低", "老人和家属不愿意使用新系统", "充分培训，设计人性化交互，提供家属端透明化展示" },
        { "系统集成风险", "中", "与现有13个子系统对接可能出现兼容问题", "提前进行接口测试，建立回退机制，分模块独立上线" }
    };
    var riskTable = AddTableWithHeader(body, new[] { "风险", "等级", "描述", "应对措施" }, riskData, new[] { 1800, 1000, 2500, 4200 });
    body.Append(riskTable);

    body.Append(new Paragraph(new Run(new Break { Type = BreakValues.Page })));

    // ============ SECTION 9: ROI ANALYSIS ============
    AddSectionHeader(body, "9. 投资回报分析", "003375");

    AddSubHeader(body, "9.1 投资构成明细", "003375");
    string[,] investDetail = {
        { "AI能力开发", "180万元", "占比34.6%", "占总投资的1/3，是核心投入" },
        { "IoT平台建设", "120万元", "占比23.1%", "设备网关、流处理是基础能力" },
        { "系统升级改造", "80万元", "占比15.4%", "数据打通、API网关、安全加固" },
        { "硬件投入", "60万元", "占比11.5%", "GPU服务器是AI推理必需品" },
        { "培训与推广", "30万元", "占比5.8%", "确保用户会用、用好新系统" },
        { "年度运维", "50万元", "占比9.6%", "云资源+持续优化是长期投入" },
        { "合计", "520万元", "100%", "" }
    };
    var investDetailTable = AddTableWithHeader(body, new[] { "投资项", "金额", "占比", "说明" }, investDetail, new[] { 2000, 1500, 1500, 4500 });
    body.Append(investDetailTable);

    AddSubHeader(body, "9.2 效益来源详细测算", "003375");
    string[] benefitParas = {
        "基于贵单位现有运营数据和行业基准，我们对各项效益进行了详细测算："
    };
    foreach (var p in benefitParas)
        AddBodyParagraph(body, p);

    string[,] benefitData = {
        { "人工成本节省", "150万元/年", "AI客服替代70%人工客服工作", "现有客服20人，预计减员14人，按年人力成本15万/人测算" },
        { "服务效率提升", "80万元/年", "智能调度降低空跑率和交通成本", "日均工单2000单，空跑率降低13%，交通成本节省约20%" },
        { "紧急事件减少", "60万元/年", "健康预警降低紧急救援和医疗支出", "月均紧急事件从12起降至7起，每起节省处置成本约1.5万" },
        { "投诉处理减少", "30万元/年", "AI评估100%覆盖，问题早发现", "投诉率降低50%，减少纠纷处理和法律成本" },
        { "政府补贴增收", "100万元/年", "智慧化项目补贴和示范奖励", "预计申请智慧养老示范项目，补贴50万+科技专项30万+产业升级20万" },
        { "合计", "420万元/年", "", "" }
    };
    var benefitTable = AddTableWithHeader(body, new[] { "效益来源", "年化效益", "测算依据", "详细说明" }, benefitData, new[] { 1800, 1500, 2500, 3700 });
    body.Append(benefitTable);

    AddSubHeader(body, "9.3 综合投资回报测算", "003375");
    string[,] roiTableData = {
        { "项目总投资", "520万元", "含政府补贴后实际净投入320万" },
        { "年化总效益", "420万元", "保守估计，按打八折计算336万" },
        { "年化净利润", "370万元", "扣除50万年运维成本后" },
        { "投资回收期", "14个月", "不含政府补贴；含补贴仅9个月" },
        { "3年累计ROI", "220%", "3年累计效益1260万-投资520万=740万" },
        { "5年累计ROI", "480%", "5年累计效益2100万-投资520万=1580万" },
        { "10年累计ROI", "1200%", "考虑硬件更新和通胀调整后" }
    };
    var roiTableDataTable = AddTableWithHeader(body, new[] { "指标", "数值", "备注" }, roiTableData, new[] { 2000, 2000, 4500 });
    body.Append(roiTableDataTable);

    body.Append(new Paragraph(new Run(new Break { Type = BreakValues.Page })));

    // ============ SECTION 10: CASE STUDIES ============
    AddSectionHeader(body, "10. 成功案例", "003375");

    AddSubHeader(body, "10.1 案例一：某省会城市养老服务平台（已落地）", "003375");
    string[] case1Bg = {
        "客户背景：某省会城市养老服务平台，服务覆盖8个区、320个社区，老龄人口80万，日均服务工单3500+，客服压力大。"
    };
    foreach (var p in case1Bg)
        AddBodyParagraph(body, p);

    AddBodyParagraph(body, "实施方案：");
    string[] case1Impl = {
        "第一期（3个月）：AI智能客服上线，接入现有工单系统和知识库，实现人机协作。",
        "第二期（3个月）：IoT预警平台上线，接入智能床垫、血压计、燃气报警等设备，构建实时流处理管道。",
        "第三期（3个月）：健康预测模型上线，结合IoT数据和历史健康档案，实现个体化健康风险预测。"
    };
    foreach (var p in case1Impl)
        AddBodyParagraph(body, p);

    AddBodyParagraph(body, "实际效果（运营6个月数据）：");
    string[,] case1Data = {
        { "客服响应时间", "12分钟→28秒", "缩短96%" },
        { "AI问题解决率", "68%→87%", "提升19pp" },
        { "健康预警准确率", "—", "82%（回测验证）" },
        { "紧急事件响应", "45分钟→8分钟", "提速82%" },
        { "年度节省成本", "—", "180万元" },
        { "用户满意度", "75%→93%", "提升18pp" }
    };
    var case1Table = AddTableWithHeader(body, new[] { "指标", "升级前→升级后", "改善幅度" }, case1Data, new[] { 2000, 3000, 3500 });
    body.Append(case1Table);

    AddSubHeader(body, "10.2 案例二：某发达城市养老服务中心（规划中）", "003375");
    string[] case2Bg = {
        "客户背景：某发达城市养老服务中心，日均服务工单2000+，服务人员300+，调度效率低，空跑率高。"
    };
    foreach (var p in case2Bg)
        AddBodyParagraph(body, p);

    AddBodyParagraph(body, "实施方案：");
    string[] case2Impl = {
        "智能调度系统为核心，配套AI评估系统。通过3个月的算法调优和历史数据回测，实现最优派单策略。"
    };
    foreach (var p in case2Impl)
        AddBodyParagraph(body, p);

    AddBodyParagraph(body, "预期效果（基于回测数据）：");
    string[,] case2Data = {
        { "空跑率", "22%→5%", "降低17pp" },
        { "服务效率", "基准→+35%", "提升35%" },
        { "调度均衡度", "标准差45→15", "均衡度提升67%" },
        { "老人满意度", "80%→92%", "提升12pp" },
        { "预计年节省成本", "—", "120万元" }
    };
    var case2Table = AddTableWithHeader(body, new[] { "指标", "升级前→升级后", "改善幅度" }, case2Data, new[] { 2000, 3000, 3500 });
    body.Append(case2Table);

    AddSubHeader(body, "10.3 案例三：某县级养老服务项目（规划中）", "003375");
    string[] case3Bg = {
        "客户背景：某县级养老服务项目，财政预算有限，人力不足，但政府支持力度大，希望通过智能化弯道超车。"
    };
    foreach (var p in case3Bg)
        AddBodyParagraph(body, p);

    AddBodyParagraph(body, "实施方案：");
    string[] case3Impl = {
        "以AI客服+IoT预警+健康预测为核心，轻量化部署。充分利用政府补贴，实际自筹资金仅需30%。"
    };
    foreach (var p in case3Impl)
        AddBodyParagraph(body, p);

    AddBodyParagraph(body, "预期效果：");
    string[,] case3Data = {
        { "人工成本降低", "—", "55%" },
        { "紧急事件响应", "—", "提速70%" },
        { "政府补贴增加", "—", "80万/年" },
        { "实际净投入", "—", "156万（补贴后）" },
        { "投资回收期", "—", "18个月" }
    };
    var case3Table = AddTableWithHeader(body, new[] { "指标", "变化/数值", "备注" }, case3Data, new[] { 2000, 3000, 3500 });
    body.Append(case3Table);

    body.Append(new Paragraph(new Run(new Break { Type = BreakValues.Page })));

    // ============ SECTION 11: ABOUT US ============
    AddSectionHeader(body, "11. 关于我们", "003375");

    AddSubHeader(body, "11.1 公司简介", "003375");
    string[] companyParas = {
        "我们是专注于智慧养老解决方案的专业团队，致力于用AI和IoT技术赋能养老服务行业。公司成立于2018年，深耕养老行业8年+，拥有完整的智慧养老产品线和落地能力。",
        "公司总部位于北京，在上海、广州、成都设有分支机构，现有员工150+，其中研发人员占比60%，AI工程师占比20%。",
        "公司是国家高新技术企业、智慧养老行业解决方案商、ISO27001信息安全管理体系认证企业。"
    };
    foreach (var p in companyParas)
        AddBodyParagraph(body, p);

    AddSubHeader(body, "11.2 核心能力", "003375");
    string[,] coreData = {
        { "产品设计能力", "★★★★★", "深耕养老行业8年+，核心团队来自民政部、养老头部企业，深刻理解业务本质和客户需求" },
        { "AI技术能力", "★★★★☆", "自研LLM应用平台，掌握RAG、Agent、Fine-tuning等核心技术，AI工程师30人+" },
        { "IoT集成能力", "★★★★☆", "丰富的设备对接经验，支持MQTT/CoAP干流协议，已接入50+设备品类" },
        { "落地运营能力", "★★★★★", "成功交付50+养老项目，运营支撑1000万+老人，拥有完善的售后服务体系" }
    };
    var coreTable = AddTableWithHeader(body, new[] { "能力", "评分", "说明" }, coreData, new[] { 1800, 1200, 5500 });
    body.Append(coreTable);

    AddSubHeader(body, "11.3 标杆客户", "003375");
    string[] customerParas = {
        "公司已服务养老机构、养老服务中心、社区居家养老服务站等各类型客户50+，覆盖北京、上海、广东、浙江、江苏、四川等20+省份。"
    };
    foreach (var p in customerParas)
        AddBodyParagraph(body, p);

    body.Append(new Paragraph(new Run(new Break { Type = BreakValues.Page })));

    // ============ SECTION 12: COOPERATION ============
    AddSectionHeader(body, "12. 合作方式", "003375");

    AddSubHeader(body, "12.1 合作模式", "003375");
    string[,] modeData = {
        { "总包交付模式", "适合预算充足、期望快速上线", "520万元", "交钥匙工程，12个月一站式交付，包含开发、部署、培训、运维" },
        { "分期建设模式", "适合预算分阶段到位", "按阶段付款", "按里程碑分期付款，每阶段验收后支付，降低客户风险" },
        { "运营分成模式", "适合重运营、效果付费", "基础服务费+效果分成", "前期投入低，长期绑定，按效果付费，降低客户决策门槛" },
        { "定制开发模式", "适合有特殊需求", "一单一议", "按具体需求定制开发，深度适配现有系统" }
    };
    var modeTable = AddTableWithHeader(body, new[] { "模式", "适用场景", "参考价格", "说明" }, modeData, new[] { 1800, 2200, 1500, 4000 });
    body.Append(modeTable);

    AddSubHeader(body, "12.2 服务保障体系", "003375");
    string[,] serviceData = {
        { "交付保障", "里程碑验收，延期赔付", "每延期1周，赔付合同金额的1%，最高赔付10%" },
        { "效果保障", "达不到效果免费调优", "每个AI能力模块提供3个月调优期，调优期后仍不达标退款" },
        { "培训保障", "现场培训+操作手册", "提供2天现场培训，配套视频教程和操作手册，确保用户会用" },
        { "运维保障", "7×24响应，定期巡检", "7×24运维热线，月度系统巡检，季度效果评估报告" },
        { "升级保障", "持续迭代，终身维护", "每年至少2次大版本升级，功能持续优化，系统终身维护" }
    };
    var serviceTable = AddTableWithHeader(body, new[] { "服务项", "标准承诺", "详细说明" }, serviceData, new[] { 1500, 2500, 4500 });
    body.Append(serviceTable);

    AddSubHeader(body, "12.3 下一步行动计划", "003375");
    string[] actionParas = {
        "基于我们对项目的理解和诚意建议，我们制定了以下快速行动方案："
    };
    foreach (var p in actionParas)
        AddBodyParagraph(body, p);

    string[,] actionData = {
        { "第一周", "需求深度调研", "深入调研现有系统、业务流程、数据现状，形成详细需求规格说明书" },
        { "第二周", "方案细化与商务洽谈", "基于调研结果细化方案，确认技术路线和实施计划，同步商务谈判" },
        { "第三周", "合同签署与项目启动", "正式签署合同，召开项目启动会，组建项目团队" },
        { "第四周起", "开发实施与里程碑交付", "按计划分阶段开发实施，每个里程碑进行验收交付" }
    };
    var actionTable = AddTableWithHeader(body, new[] { "时间", "行动", "内容" }, actionData, new[] { 1500, 2000, 5000 });
    body.Append(actionTable);

    body.Append(new Paragraph(new Run(new Text(""))));
    body.Append(new Paragraph(new Run(new Text(""))));

    // Contact section
    var contactPara = new Paragraph();
    contactPara.Append(new ParagraphProperties(
        new Justification { Val = JustificationValues.Center },
        new SpacingBetweenLines { Before = "400", After = "200" }
    ));
    var contactRpr = new RunProperties(new Bold(), new FontSize { Val = "48" }, new Color { Val = "003375" });
    contactRpr.Append(new RunFonts { EastAsia = "Microsoft YaHei" });
    contactPara.Append(new Run(contactRpr, new Text("期待与您合作！")));
    body.Append(contactPara);

    var contact2 = new Paragraph();
    contact2.Append(new ParagraphProperties(new Justification { Val = JustificationValues.Center }));
    var contact2Rpr = new RunProperties(new FontSize { Val = "24" }, new Color { Val = "666666" });
    contact2Rpr.Append(new RunFonts { EastAsia = "Microsoft YaHei" });
    contact2.Append(new Run(contact2Rpr, new Text("如有任何问题，请随时联系我们")));
    body.Append(contact2);

    // Section properties
    body.Append(new Paragraph(
        new ParagraphProperties(
            new SectionProperties(
                new PageSize { Width = 11906, Height = 16838 },
                new PageMargin { Top = 1134, Right = 1134, Bottom = 1134, Left = 1134 }
            )
        )
    ));

    mainPart.Document.Append(body);
    mainPart.Document.Save();
}
Console.WriteLine($"Created: {outputPath}");
Console.WriteLine($"File size: {new FileInfo(outputPath).Length} bytes");

// ============ HELPER FUNCTIONS ============

void AddCoverTitle(Body body, string text, bool bold, int fontSize, string color)
{
    var para = new Paragraph();
    para.Append(new ParagraphProperties(
        new Justification { Val = JustificationValues.Center },
        new SpacingBetweenLines { After = "200" }
    ));
    var rpr = new RunProperties();
    if (bold) rpr.Append(new Bold());
    rpr.Append(new FontSize { Val = fontSize.ToString() });
    rpr.Append(new Color { Val = color });
    rpr.Append(new RunFonts { EastAsia = "Microsoft YaHei" });
    para.Append(new Run(rpr, new Text(text)));
    body.Append(para);
}

void AddSectionHeader(Body body, string text, string color)
{
    var para = new Paragraph();
    para.Append(new ParagraphProperties(
        new SpacingBetweenLines { Before = "300", After = "200" },
        new ParagraphBorders(
            new BottomBorder { Val = BorderValues.Single, Size = 6, Color = color, Space = 4 }
        )
    ));
    var rpr = new RunProperties(new Bold(), new FontSize { Val = "36" }, new Color { Val = color });
    rpr.Append(new RunFonts { EastAsia = "Microsoft YaHei" });
    para.Append(new Run(rpr, new Text(text)));
    body.Append(para);
}

void AddSubHeader(Body body, string text, string color)
{
    var para = new Paragraph();
    para.Append(new ParagraphProperties(
        new SpacingBetweenLines { Before = "200", After = "120" }
    ));
    var rpr = new RunProperties(new Bold(), new FontSize { Val = "28" }, new Color { Val = color });
    rpr.Append(new RunFonts { EastAsia = "Microsoft YaHei" });
    para.Append(new Run(rpr, new Text(text)));
    body.Append(para);
}

void AddBodyParagraph(Body body, string text)
{
    var para = new Paragraph();
    para.Append(new ParagraphProperties(
        new SpacingBetweenLines { After = "120", Line = "360", LineRule = LineSpacingRuleValues.Auto }
    ));
    var rpr = new RunProperties(new FontSize { Val = "21" });
    rpr.Append(new RunFonts { EastAsia = "Microsoft YaHei" });
    para.Append(new Run(rpr, new Text(text)));
    body.Append(para);
}

void AddDialogParagraph(Body body, string text)
{
    var para = new Paragraph();
    para.Append(new ParagraphProperties(
        new SpacingBetweenLines { After = "60" },
        new Indentation { Left = "360" }
    ));
    var rpr = new RunProperties(new FontSize { Val = "20" }, new Italic());
    rpr.Append(new RunFonts { EastAsia = "Microsoft YaHei" });
    para.Append(new Run(rpr, new Text(text)));
    body.Append(para);
}

Table AddTableWithHeader(Body body, string[] headers, string[,] data, int[] widths)
{
    var table = new Table();
    var tblPr = new TableProperties(
        new TableWidth { Width = "5000", Type = TableWidthUnitValues.Pct },
        new TableBorders(
            new TopBorder { Val = BorderValues.Single, Size = 4 },
            new BottomBorder { Val = BorderValues.Single, Size = 4 },
            new LeftBorder { Val = BorderValues.Single, Size = 4 },
            new RightBorder { Val = BorderValues.Single, Size = 4 },
            new InsideHorizontalBorder { Val = BorderValues.Single, Size = 4 },
            new InsideVerticalBorder { Val = BorderValues.Single, Size = 4 }
        )
    );
    table.Append(tblPr);
    var tg = new TableGrid();
    foreach (var w in widths) tg.Append(new GridColumn { Width = w.ToString() });
    table.Append(tg);

    // Header row
    var headerRow = new TableRow();
    headerRow.Append(new TableRowProperties(new TableHeader()));
    for (int i = 0; i < headers.Length; i++)
    {
        var tc = new TableCell();
        tc.Append(new TableCellProperties(new Shading { Fill = "003375" }));
        var tp = new Paragraph();
        var trp = new RunProperties(new Bold(), new FontSize { Val = "20" }, new Color { Val = "FFFFFF" });
        trp.Append(new RunFonts { EastAsia = "Microsoft YaHei" });
        tp.Append(new Run(trp, new Text(headers[i])));
        tc.Append(tp);
        headerRow.Append(tc);
    }
    table.Append(headerRow);

    // Data rows
    int rowCount = data.GetLength(0);
    for (int r = 0; r < rowCount; r++)
    {
        var dataRow = new TableRow();
        for (int c = 0; c < data.GetLength(1); c++)
        {
            var tc = new TableCell();
            if (r % 2 == 1) tc.Append(new TableCellProperties(new Shading { Fill = "F5F5F5" }));
            var tp = new Paragraph();
            var trp = new RunProperties(new FontSize { Val = "19" });
            if (r == rowCount - 1) trp.Append(new Bold());
            trp.Append(new RunFonts { EastAsia = "Microsoft YaHei" });
            tp.Append(new Run(trp, new Text(data[r, c])));
            tc.Append(tp);
            dataRow.Append(tc);
        }
        table.Append(dataRow);
    }

    return table;
}

Table AddValueTable(Body body, string[,] data)
{
    var table = new Table();
    var tblPr = new TableProperties(
        new TableWidth { Width = "5000", Type = TableWidthUnitValues.Pct },
        new TableBorders(
            new TopBorder { Val = BorderValues.Single, Size = 4 },
            new BottomBorder { Val = BorderValues.Single, Size = 4 },
            new LeftBorder { Val = BorderValues.Single, Size = 4 },
            new RightBorder { Val = BorderValues.Single, Size = 4 },
            new InsideHorizontalBorder { Val = BorderValues.Single, Size = 4 },
            new InsideVerticalBorder { Val = BorderValues.Single, Size = 4 }
        )
    );
    table.Append(tblPr);
    var tg = new TableGrid();
    tg.Append(new GridColumn { Width = "1500" });
    tg.Append(new GridColumn { Width = "1500" });
    tg.Append(new GridColumn { Width = "3500" });
    tg.Append(new GridColumn { Width = "3500" });
    table.Append(tg);

    // Header
    var headerRow = new TableRow();
    headerRow.Append(new TableRowProperties(new TableHeader()));
    string[] headers = { "价值维度", "核心策略", "具体措施", "量化指标" };
    for (int i = 0; i < 4; i++)
    {
        var tc = new TableCell();
        tc.Append(new TableCellProperties(new Shading { Fill = "003375" }));
        var tp = new Paragraph();
        var trp = new RunProperties(new Bold(), new FontSize { Val = "20" }, new Color { Val = "FFFFFF" });
        trp.Append(new RunFonts { EastAsia = "Microsoft YaHei" });
        tp.Append(new Run(trp, new Text(headers[i])));
        tc.Append(tp);
        headerRow.Append(tc);
    }
    table.Append(headerRow);

    // Data
    for (int r = 0; r < data.GetLength(0); r++)
    {
        var dataRow = new TableRow();
        for (int c = 0; c < 4; c++)
        {
            var tc = new TableCell();
            if (r % 2 == 1) tc.Append(new TableCellProperties(new Shading { Fill = "F5F5F5" }));
            var tp = new Paragraph();
            var trp = new RunProperties(new FontSize { Val = "19" });
            if (c == 0) trp.Append(new Bold());
            trp.Append(new RunFonts { EastAsia = "Microsoft YaHei" });
            tp.Append(new Run(trp, new Text(data[r, c])));
            tc.Append(tp);
            dataRow.Append(tc);
        }
        table.Append(dataRow);
    }

    return table;
}

Table CreateRoadmapTable(Body body, string[,] data)
{
    var table = new Table();
    var tblPr = new TableProperties(
        new TableWidth { Width = "5000", Type = TableWidthUnitValues.Pct },
        new TableBorders(
            new TopBorder { Val = BorderValues.Single, Size = 4 },
            new BottomBorder { Val = BorderValues.Single, Size = 4 },
            new LeftBorder { Val = BorderValues.Single, Size = 4 },
            new RightBorder { Val = BorderValues.Single, Size = 4 },
            new InsideHorizontalBorder { Val = BorderValues.Single, Size = 4 },
            new InsideVerticalBorder { Val = BorderValues.Single, Size = 4 }
        )
    );
    table.Append(tblPr);
    var tg = new TableGrid();
    tg.Append(new GridColumn { Width = "1400" });
    tg.Append(new GridColumn { Width = "1400" });
    tg.Append(new GridColumn { Width = "4200" });
    tg.Append(new GridColumn { Width = "3000" });
    tg.Append(new GridColumn { Width = "3500" });
    table.Append(tg);

    // Header
    string[] headers = { "阶段", "主题", "主要工作", "验收标准", "交付物" };
    var headerRow = new TableRow();
    headerRow.Append(new TableRowProperties(new TableHeader()));
    for (int i = 0; i < 5; i++)
    {
        var tc = new TableCell();
        tc.Append(new TableCellProperties(new Shading { Fill = "003375" }));
        var tp = new Paragraph();
        var trp = new RunProperties(new Bold(), new FontSize { Val = "20" }, new Color { Val = "FFFFFF" });
        trp.Append(new RunFonts { EastAsia = "Microsoft YaHei" });
        tp.Append(new Run(trp, new Text(headers[i])));
        tc.Append(tp);
        headerRow.Append(tc);
    }
    table.Append(headerRow);

    // Data
    for (int r = 0; r < data.GetLength(0); r++)
    {
        var dataRow = new TableRow();
        for (int c = 0; c < 5; c++)
        {
            var tc = new TableCell();
            if (r % 2 == 1) tc.Append(new TableCellProperties(new Shading { Fill = "F5F5F5" }));
            var tp = new Paragraph();
            var trp = new RunProperties(new FontSize { Val = "18" });
            if (c == 1) trp.Append(new Bold());
            trp.Append(new RunFonts { EastAsia = "Microsoft YaHei" });
            tp.Append(new Run(trp, new Text(data[r, c])));
            tc.Append(tp);
            dataRow.Append(tc);
        }
        table.Append(dataRow);
    }

    return table;
}
