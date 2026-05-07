package com.elderlycare.common;

import java.util.Map;
import java.util.HashMap;

/**
 * 延安地区枚举
 */
public enum YananArea {
    BAOTA("宝塔区", "610602"),
    ANSAI("安塞区", "610603"),
    YANCHANG("延长县", "610621"),
    YICHUAN("宜川县", "610630"),
    HUANGlong("黄龙县", "610631"),
    HUANGLING("黄陵县", "610632"),
    FUXIAN("富县", "610628"),
    ZHILU("直罗县", "610629"),
    WUAQI("吴起县", "610626"),
    ZICHANG("子长县", "610623"),
    MIDIAN("米脂县", "610627"),
    ANDING("安帝县", "610624");

    private final String areaName;
    private final String areaId;
    private static final Map<String, YananArea> BY_NAME = new HashMap<>();
    private static final Map<String, YananArea> BY_ID = new HashMap<>();

    static {
        for (YananArea area : values()) {
            BY_NAME.put(area.areaName, area);
            BY_ID.put(area.areaId, area);
        }
    }

    YananArea(String areaName, String areaId) {
        this.areaName = areaName;
        this.areaId = areaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public String getAreaId() {
        return areaId;
    }

    public static YananArea fromName(String name) {
        if (name == null) return null;
        return BY_NAME.get(name);
    }

    public static YananArea fromId(String id) {
        if (id == null) return null;
        return BY_ID.get(id);
    }
}