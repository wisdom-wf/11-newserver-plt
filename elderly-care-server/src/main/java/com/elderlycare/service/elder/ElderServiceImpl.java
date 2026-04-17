package com.elderlycare.service.elder;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.BusinessException;
import com.elderlycare.dto.elder.*;
import com.elderlycare.entity.config.Area;
import com.elderlycare.entity.elder.*;
import com.elderlycare.entity.provider.Provider;
import com.elderlycare.mapper.config.AreaMapper;
import com.elderlycare.mapper.elder.*;
import com.elderlycare.mapper.provider.ProviderMapper;
import com.elderlycare.vo.elder.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 老人档案Service实现类
 */
@Service
@RequiredArgsConstructor
public class ElderServiceImpl implements ElderService {

    private final ElderMapper elderMapper;
    private final ElderFamilyMapper elderFamilyMapper;
    private final ElderHealthMapper elderHealthMapper;
    private final ElderDemandMapper elderDemandMapper;
    private final ElderSubsidyMapper elderSubsidyMapper;
    private final ProviderMapper providerMapper;
    private final AreaMapper areaMapper;

    // ==================== 老人档案管理 ====================

    @Override
    @Transactional
    public Elder addElder(ElderDTO dto) {
        Elder elder = new Elder();
        BeanUtils.copyProperties(dto, elder);
        elder.setStatus("PENDING"); // 待审核
        elderMapper.insert(elder);
        return elder;
    }

    @Override
    public IPage<ElderVO> getElderPage(ElderPageDTO dto) {
        Page<Elder> page = new Page<>(dto.getCurrent(), dto.getPageSize());
        IPage<Elder> elderPage = elderMapper.selectElderPage(page, dto);

        // 转换为VO
        Page<ElderVO> voPage = new Page<>(elderPage.getCurrent(), elderPage.getSize(), elderPage.getTotal());
        List<ElderVO> voList = elderPage.getRecords().stream()
                .map(this::convertToElderVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public ElderVO getElderById(String elderId) {
        Elder elder = elderMapper.selectById(elderId);
        if (elder == null) {
            throw new BusinessException(404, "老人档案不存在");
        }
        return convertToElderVO(elder);
    }

    @Override
    @Transactional
    public Elder updateElder(String elderId, ElderDTO dto) {
        Elder elder = elderMapper.selectById(elderId);
        if (elder == null) {
            throw new BusinessException(404, "老人档案不存在");
        }
        BeanUtils.copyProperties(dto, elder);
        elder.setElderId(elderId);
        elderMapper.updateById(elder);
        return elder;
    }

    @Override
    @Transactional
    public void deleteElder(String elderId) {
        Elder elder = elderMapper.selectById(elderId);
        if (elder == null) {
            throw new BusinessException(404, "老人档案不存在");
        }
        elderMapper.deleteById(elderId);
    }

    @Override
    @Transactional
    public void updateElderStatus(ElderStatusDTO dto) {
        Elder elder = elderMapper.selectById(dto.getElderId());
        if (elder == null) {
            throw new BusinessException(404, "老人档案不存在");
        }
        elder.setStatus(dto.getStatus());
        elderMapper.updateById(elder);
    }

    // ==================== 老人家庭信息管理 ====================

    @Override
    @Transactional
    public ElderFamily addFamilyMember(String elderId, ElderFamilyDTO dto) {
        // 验证老人是否存在
        Elder elder = elderMapper.selectById(elderId);
        if (elder == null) {
            throw new BusinessException(404, "老人档案不存在");
        }

        ElderFamily family = new ElderFamily();
        BeanUtils.copyProperties(dto, family);
        family.setElderId(elderId);
        elderFamilyMapper.insert(family);
        return family;
    }

    @Override
    public List<ElderFamilyVO> getFamilyList(String elderId) {
        LambdaQueryWrapper<ElderFamily> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ElderFamily::getElderId, elderId);
        List<ElderFamily> familyList = elderFamilyMapper.selectList(wrapper);

        return familyList.stream()
                .map(this::convertToElderFamilyVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ElderFamily updateFamilyMember(String familyId, ElderFamilyDTO dto) {
        ElderFamily family = elderFamilyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException(404, "家庭成员不存在");
        }
        BeanUtils.copyProperties(dto, family);
        family.setFamilyId(familyId);
        elderFamilyMapper.updateById(family);
        return family;
    }

    @Override
    @Transactional
    public void deleteFamilyMember(String familyId) {
        ElderFamily family = elderFamilyMapper.selectById(familyId);
        if (family == null) {
            throw new BusinessException(404, "家庭成员不存在");
        }
        elderFamilyMapper.deleteById(familyId);
    }

    // ==================== 老人健康信息管理 ====================

    @Override
    @Transactional
    public ElderHealth saveHealth(String elderId, ElderHealthDTO dto) {
        // 验证老人是否存在
        Elder elder = elderMapper.selectById(elderId);
        if (elder == null) {
            throw new BusinessException(404, "老人档案不存在");
        }

        // 查询是否已有健康档案
        LambdaQueryWrapper<ElderHealth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ElderHealth::getElderId, elderId);
        ElderHealth existingHealth = elderHealthMapper.selectOne(wrapper);

        ElderHealth health = new ElderHealth();
        BeanUtils.copyProperties(dto, health);
        health.setElderId(elderId);

        if (existingHealth != null) {
            // 更新
            health.setHealthId(existingHealth.getHealthId());
            elderHealthMapper.updateById(health);
        } else {
            // 新增
            elderHealthMapper.insert(health);
        }
        return health;
    }

    @Override
    public ElderHealthVO getHealthByElderId(String elderId) {
        LambdaQueryWrapper<ElderHealth> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ElderHealth::getElderId, elderId);
        ElderHealth health = elderHealthMapper.selectOne(wrapper);

        if (health == null) {
            return null;
        }
        return convertToElderHealthVO(health);
    }

    // ==================== 老人服务需求管理 ====================

    @Override
    @Transactional
    public ElderDemand addDemand(String elderId, ElderDemandDTO dto) {
        // 验证老人是否存在
        Elder elder = elderMapper.selectById(elderId);
        if (elder == null) {
            throw new BusinessException(404, "老人档案不存在");
        }

        ElderDemand demand = new ElderDemand();
        BeanUtils.copyProperties(dto, demand);
        demand.setElderId(elderId);
        demand.setStatus(0); // 待评估
        elderDemandMapper.insert(demand);
        return demand;
    }

    @Override
    public List<ElderDemandVO> getDemandList(String elderId) {
        LambdaQueryWrapper<ElderDemand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ElderDemand::getElderId, elderId);
        wrapper.orderByDesc(ElderDemand::getCreateTime);
        List<ElderDemand> demands = elderDemandMapper.selectList(wrapper);

        return demands.stream()
                .map(this::convertToElderDemandVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ElderDemand updateDemand(String demandId, ElderDemandDTO dto) {
        ElderDemand demand = elderDemandMapper.selectById(demandId);
        if (demand == null) {
            throw new BusinessException(404, "服务需求不存在");
        }
        BeanUtils.copyProperties(dto, demand);
        demand.setDemandId(demandId);
        elderDemandMapper.updateById(demand);
        return demand;
    }

    @Override
    @Transactional
    public void deleteDemand(String demandId) {
        ElderDemand demand = elderDemandMapper.selectById(demandId);
        if (demand == null) {
            throw new BusinessException(404, "服务需求不存在");
        }
        elderDemandMapper.deleteById(demandId);
    }

    // ==================== 老人补贴管理 ====================

    @Override
    @Transactional
    public ElderSubsidy applySubsidy(String elderId, ElderSubsidyDTO dto) {
        // 验证老人是否存在
        Elder elder = elderMapper.selectById(elderId);
        if (elder == null) {
            throw new BusinessException(404, "老人档案不存在");
        }

        ElderSubsidy subsidy = new ElderSubsidy();
        BeanUtils.copyProperties(dto, subsidy);
        subsidy.setElderId(elderId);
        subsidy.setAuditStatus(0); // 待审核
        subsidy.setGrantStatus(0); // 未发放

        // 计算剩余额度
        if (subsidy.getTotalQuota() != null && subsidy.getUsedQuota() != null) {
            subsidy.setRemainingQuota(subsidy.getTotalQuota().subtract(subsidy.getUsedQuota()));
        } else {
            subsidy.setRemainingQuota(subsidy.getTotalQuota());
        }

        elderSubsidyMapper.insert(subsidy);
        return subsidy;
    }

    @Override
    public List<ElderSubsidyVO> getSubsidyList(String elderId) {
        LambdaQueryWrapper<ElderSubsidy> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ElderSubsidy::getElderId, elderId);
        wrapper.orderByDesc(ElderSubsidy::getCreateTime);
        List<ElderSubsidy> subsidies = elderSubsidyMapper.selectList(wrapper);

        return subsidies.stream()
                .map(this::convertToElderSubsidyVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void auditSubsidy(SubsidyAuditDTO dto) {
        ElderSubsidy subsidy = elderSubsidyMapper.selectById(dto.getSubsidyId());
        if (subsidy == null) {
            throw new BusinessException(404, "补贴记录不存在");
        }
        subsidy.setAuditStatus(dto.getAuditStatus());
        subsidy.setAuditRemark(dto.getAuditRemark());
        subsidy.setAuditTime(LocalDateTime.now());
        elderSubsidyMapper.updateById(subsidy);
    }

    @Override
    public SubsidyBalanceVO getSubsidyBalance(String subsidyId) {
        ElderSubsidy subsidy = elderSubsidyMapper.selectById(subsidyId);
        if (subsidy == null) {
            throw new BusinessException(404, "补贴记录不存在");
        }

        Elder elder = elderMapper.selectById(subsidy.getElderId());

        SubsidyBalanceVO vo = new SubsidyBalanceVO();
        vo.setSubsidyId(subsidyId);
        vo.setElderId(subsidy.getElderId());
        vo.setElderName(elder != null ? elder.getName() : null);
        vo.setSubsidyTypeName(getSubsidyTypeName(subsidy.getSubsidyType()));
        vo.setSubsidyName(subsidy.getSubsidyName());
        vo.setTotalQuota(subsidy.getTotalQuota());
        vo.setUsedQuota(subsidy.getUsedQuota());
        vo.setRemainingQuota(subsidy.getRemainingQuota());
        vo.setStartDate(subsidy.getStartDate());
        vo.setEndDate(subsidy.getEndDate());

        // 计算使用比例
        if (subsidy.getTotalQuota() != null && subsidy.getTotalQuota().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal usedRatio = subsidy.getUsedQuota()
                    .divide(subsidy.getTotalQuota(), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            vo.setUsedRatio(usedRatio);
        }

        // 判断状态
        LocalDate today = LocalDate.now();
        if (subsidy.getEndDate() != null && today.isAfter(subsidy.getEndDate())) {
            vo.setStatus(2); // 已过期
        } else if (subsidy.getRemainingQuota() != null && subsidy.getRemainingQuota().compareTo(BigDecimal.ZERO) <= 0) {
            vo.setStatus(1); // 已用完
        } else {
            vo.setStatus(0); // 有效
        }

        return vo;
    }

    // ==================== 转换方法 ====================

    private ElderVO convertToElderVO(Elder elder) {
        ElderVO vo = new ElderVO();
        BeanUtils.copyProperties(elder, vo);
        vo.setGenderName(getGenderNameString(elder.getGender()));
        vo.setCareLevelName(getCareLevelNameString(elder.getCareLevel()));
        vo.setStatusName(getElderStatusNameString(elder.getStatus()));

        // 计算年龄
        if (elder.getBirthDate() != null) {
            vo.setAge(Period.between(elder.getBirthDate(), LocalDate.now()).getYears());
        }

        // 回填区域名称
        if (elder.getAreaId() != null && !elder.getAreaId().isEmpty()) {
            Area area = areaMapper.selectById(elder.getAreaId());
            if (area != null) {
                vo.setAreaName(area.getAreaName());
            }
        }

        // 回填服务商名称
        if (elder.getProviderId() != null && !elder.getProviderId().isEmpty()) {
            Provider provider = providerMapper.selectById(elder.getProviderId());
            if (provider != null) {
                vo.setProviderName(provider.getProviderName());
            }
        }

        return vo;
    }

    private ElderFamilyVO convertToElderFamilyVO(ElderFamily family) {
        ElderFamilyVO vo = new ElderFamilyVO();
        BeanUtils.copyProperties(family, vo);
        vo.setGenderName(getGenderName(family.getGender()));
        vo.setRelationName(getRelationName(family.getRelation()));
        vo.setIsPrimaryName(family.getIsPrimary() != null && family.getIsPrimary() == 1 ? "是" : "否");
        vo.setCanDecideName(family.getCanDecide() != null && family.getCanDecide() == 1 ? "是" : "否");
        return vo;
    }

    private ElderHealthVO convertToElderHealthVO(ElderHealth health) {
        ElderHealthVO vo = new ElderHealthVO();
        BeanUtils.copyProperties(health, vo);
        vo.setBloodTypeName(getBloodTypeName(health.getBloodType()));
        vo.setFallRiskName(getFallRiskName(health.getFallRisk()));
        vo.setPressureSoreRiskName(getPressureSoreRiskName(health.getPressureSoreRisk()));
        vo.setNutritionStatusName(getNutritionStatusName(health.getNutritionStatus()));
        vo.setVisionStatusName(getVisionStatusName(health.getVisionStatus()));
        vo.setHearingStatusName(getHearingStatusName(health.getHearingStatus()));
        vo.setOralHealthName(getOralHealthName(health.getOralHealth()));
        vo.setMobilityStatusName(getMobilityStatusName(health.getMobilityStatus()));
        return vo;
    }

    private ElderDemandVO convertToElderDemandVO(ElderDemand demand) {
        ElderDemandVO vo = new ElderDemandVO();
        BeanUtils.copyProperties(demand, vo);

        Elder elder = elderMapper.selectById(demand.getElderId());
        if (elder != null) {
            vo.setElderName(elder.getName());
            vo.setElderPhone(elder.getPhone());
        }

        vo.setServiceTypeName(getServiceTypeName(demand.getServiceType()));
        vo.setServiceFrequencyName(getServiceFrequencyName(demand.getServiceFrequency()));
        vo.setDemandSourceName(getDemandSourceName(demand.getDemandSource()));
        vo.setUrgencyLevelName(getUrgencyLevelName(demand.getUrgencyLevel()));
        vo.setStatusName(getDemandStatusName(demand.getStatus()));
        return vo;
    }

    private ElderSubsidyVO convertToElderSubsidyVO(ElderSubsidy subsidy) {
        ElderSubsidyVO vo = new ElderSubsidyVO();
        BeanUtils.copyProperties(subsidy, vo);

        Elder elder = elderMapper.selectById(subsidy.getElderId());
        if (elder != null) {
            vo.setElderName(elder.getName());
            vo.setElderPhone(elder.getPhone());
        }

        vo.setSubsidyTypeName(getSubsidyTypeName(subsidy.getSubsidyType()));
        vo.setAuditStatusName(getAuditStatusName(subsidy.getAuditStatus()));
        vo.setGrantStatusName(getGrantStatusName(subsidy.getGrantStatus()));
        vo.setGrantCycleName(getGrantCycleName(subsidy.getGrantCycle()));
        return vo;
    }

    // ==================== 名称转换方法 ====================

    private String getGenderName(Integer gender) {
        if (gender == null) return "";
        return switch (gender) {
            case 0 -> "男";
            case 1 -> "女";
            default -> "未知";
        };
    }

    private String getHouseholdTypeName(Integer type) {
        if (type == null) return "";
        return switch (type) {
            case 0 -> "本地户籍";
            case 1 -> "外地户籍";
            default -> "未知";
        };
    }

    private String getMaritalStatusName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "未婚";
            case 1 -> "已婚";
            case 2 -> "离异";
            case 3 -> "丧偶";
            default -> "未知";
        };
    }

    private String getLivingSituationName(Integer situation) {
        if (situation == null) return "";
        return switch (situation) {
            case 0 -> "独居";
            case 1 -> "与配偶同住";
            case 2 -> "与子女同住";
            case 3 -> "与保姆同住";
            case 4 -> "养老机构";
            default -> "未知";
        };
    }

    private String getEconomicSourceName(Integer source) {
        if (source == null) return "";
        return switch (source) {
            case 0 -> "退休金";
            case 1 -> "子女供养";
            case 2 -> "低保";
            case 3 -> "其他";
            default -> "未知";
        };
    }

    private String getMedicalInsuranceTypeName(Integer type) {
        if (type == null) return "";
        return switch (type) {
            case 0 -> "城镇职工医保";
            case 1 -> "城乡居民医保";
            case 2 -> "商业医保";
            default -> "未知";
        };
    }

    private String getDisabilityLevelName(Integer level) {
        if (level == null) return "无残疾";
        return switch (level) {
            case 0 -> "无残疾";
            case 1 -> "一级";
            case 2 -> "二级";
            case 3 -> "三级";
            case 4 -> "四级";
            default -> "未知";
        };
    }

    private String getCareLevelName(Integer level) {
        if (level == null) return "";
        return switch (level) {
            case 0 -> "不需要护理";
            case 1 -> "轻度失能";
            case 2 -> "中度失能";
            case 3 -> "重度失能";
            default -> "未知";
        };
    }

    private String getElderStatusName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "正常";
            case 2 -> "暂停服务";
            case 3 -> "注销";
            default -> "未知";
        };
    }

    private String getRelationName(Integer relation) {
        if (relation == null) return "";
        return switch (relation) {
            case 0 -> "配偶";
            case 1 -> "子女";
            case 2 -> "兄弟姐妹";
            case 3 -> "孙子女";
            case 4 -> "其他亲属";
            case 5 -> "朋友";
            case 6 -> "保姆";
            case 7 -> "其他";
            default -> "未知";
        };
    }

    private String getBloodTypeName(Integer bloodType) {
        if (bloodType == null) return "";
        return switch (bloodType) {
            case 0 -> "A型";
            case 1 -> "B型";
            case 2 -> "O型";
            case 3 -> "AB型";
            default -> "未知";
        };
    }

    private String getFallRiskName(Integer risk) {
        if (risk == null) return "";
        return switch (risk) {
            case 0 -> "低风险";
            case 1 -> "中风险";
            case 2 -> "高风险";
            default -> "未知";
        };
    }

    private String getPressureSoreRiskName(Integer risk) {
        if (risk == null) return "";
        return switch (risk) {
            case 0 -> "无";
            case 1 -> "低风险";
            case 2 -> "中风险";
            case 3 -> "高风险";
            default -> "未知";
        };
    }

    private String getNutritionStatusName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "正常";
            case 1 -> "营养不良风险";
            case 2 -> "营养不良";
            default -> "未知";
        };
    }

    private String getVisionStatusName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "正常";
            case 1 -> "轻度障碍";
            case 2 -> "中度障碍";
            case 3 -> "重度障碍/失明";
            default -> "未知";
        };
    }

    private String getHearingStatusName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "正常";
            case 1 -> "轻度障碍";
            case 2 -> "中度障碍";
            case 3 -> "重度障碍/失聪";
            default -> "未知";
        };
    }

    private String getOralHealthName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "正常";
            case 1 -> "有问题";
            case 2 -> "严重问题";
            default -> "未知";
        };
    }

    private String getMobilityStatusName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "正常";
            case 1 -> "轻度受限";
            case 2 -> "中度受限";
            case 3 -> "重度受限";
            default -> "未知";
        };
    }

    private String getServiceTypeName(Integer type) {
        if (type == null) return "";
        return switch (type) {
            case 0 -> "生活照料";
            case 1 -> "日间照料";
            case 2 -> "助餐服务";
            case 3 -> "助洁服务";
            case 4 -> "助浴服务";
            case 5 -> "健康监测";
            case 6 -> "康复护理";
            case 7 -> "精神慰藉";
            case 8 -> "信息咨询";
            case 9 -> "紧急救援";
            default -> "未知";
        };
    }

    private String getServiceFrequencyName(Integer frequency) {
        if (frequency == null) return "";
        return switch (frequency) {
            case 0 -> "每日";
            case 1 -> "每周数次";
            case 2 -> "每周一次";
            case 3 -> "每月数次";
            case 4 -> "按需";
            default -> "未知";
        };
    }

    private String getDemandSourceName(Integer source) {
        if (source == null) return "";
        return switch (source) {
            case 0 -> "本人申请";
            case 1 -> "家属申请";
            case 2 -> "社区转介";
            case 3 -> "街道转介";
            default -> "未知";
        };
    }

    private String getUrgencyLevelName(Integer level) {
        if (level == null) return "";
        return switch (level) {
            case 0 -> "普通";
            case 1 -> "紧急";
            case 2 -> "非常紧急";
            default -> "未知";
        };
    }

    private String getDemandStatusName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "待评估";
            case 1 -> "已评估待匹配";
            case 2 -> "已匹配";
            case 3 -> "服务中";
            case 4 -> "已结束";
            case 5 -> "已取消";
            default -> "未知";
        };
    }

    private String getSubsidyTypeName(Integer type) {
        if (type == null) return "";
        return switch (type) {
            case 0 -> "政府补贴";
            case 1 -> "社区补贴";
            case 2 -> "企业捐赠";
            case 3 -> "其他";
            default -> "未知";
        };
    }

    private String getAuditStatusName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "审核通过";
            case 2 -> "审核不通过";
            case 3 -> "已取消";
            default -> "未知";
        };
    }

    private String getGrantStatusName(Integer status) {
        if (status == null) return "";
        return switch (status) {
            case 0 -> "未发放";
            case 1 -> "发放中";
            case 2 -> "已发放";
            case 3 -> "发放失败";
            default -> "未知";
        };
    }

    private String getGrantCycleName(Integer cycle) {
        if (cycle == null) return "";
        return switch (cycle) {
            case 0 -> "月度";
            case 1 -> "季度";
            case 2 -> "年度";
            default -> "未知";
        };
    }

    private String getGenderNameString(String gender) {
        if (gender == null) return "";
        return switch (gender) {
            case "MALE" -> "男";
            case "FEMALE" -> "女";
            default -> "未知";
        };
    }

    private String getCareLevelNameString(String level) {
        if (level == null) return "";
        return switch (level) {
            case "HIGH" -> "一级护理";
            case "MEDIUM" -> "二级护理";
            case "NORMAL" -> "三级护理";
            default -> "未知";
        };
    }

    private String getElderStatusNameString(String status) {
        if (status == null) return "";
        return switch (status) {
            case "ACTIVE" -> "正常";
            case "PENDING" -> "待审核";
            case "SUSPENDED" -> "暂停服务";
            case "CANCELLED" -> "注销";
            default -> "未知";
        };
    }
}
