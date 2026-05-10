package com.elderlycare.service.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 视频生成服务（wan2.2-i2v-plus 图生视频）
 * 预留接口，为后续培训视频自动生成流水线准备
 *
 * 当前状态：接口框架已定义，暂不实际调用
 * 触发条件：手动在后台管理界面点击"生成培训视频"
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VideoGenerationService {

    /**
     * 生成培训视频（目前是stub，保留接口）
     *
     * @param title        视频标题
     * @param script       视频脚本
     * @param coverImageUrl 封面图片URL（图生视频需要）
     * @param duration     目标时长（秒），推荐15-60秒
     * @return 任务ID（实际需从百炼轮询获取结果）
     */
    public String generateTrainingVideo(String title, String script, String coverImageUrl, int duration) {
        // TODO: 实际调用 wan2.2-i2v-plus
        // 百炼图生视频API格式：
        // POST https://dashscope.aliyuncs.com/compatible-mode/v1/video/generation
        // Body: { "model": "wan2.2-i2v-plus", "input": { "prompt": script, "image_url": coverImageUrl }, "parameters": { "duration": duration } }
        log.info("视频生成stub调用：title={}, duration={}s, cover={}", title, duration, coverImageUrl);
        return "TASK_PENDING_" + System.currentTimeMillis();
    }

    /**
     * 查询视频生成状态
     * @param taskId 任务ID
     * @return 状态：PENDING / PROCESSING / SUCCEEDED / FAILED
     */
    public String getTaskStatus(String taskId) {
        // TODO: 百炼视频生成是异步的，需要轮询task状态
        log.info("查询视频任务状态：taskId={}", taskId);
        return "PENDING";
    }
}
