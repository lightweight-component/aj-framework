package com.ajaxjs.framework.timingwheel;

import com.ajaxjs.framework.timingwheel.model.TaskExecutionStats;
import com.ajaxjs.framework.timingwheel.model.TimerTaskWrapper;
import com.ajaxjs.framework.timingwheel.model.TimingWheelStats;
import com.ajaxjs.framework.timingwheel.model.vo.BatchTasks;
import com.ajaxjs.framework.timingwheel.model.vo.CancelTask;
import com.ajaxjs.framework.timingwheel.model.vo.CleanupTasks;
import com.ajaxjs.framework.timingwheel.model.vo.CustomTask;
import com.ajaxjs.framework.timingwheel.model.vo.SampleTask;
import com.ajaxjs.framework.timingwheel.model.vo.StressTest;
import com.ajaxjs.framework.timingwheel.model.vo.SystemInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 时间轮控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/timingwheel")
@CrossOrigin(origins = "*")
@ConditionalOnProperty(name = "TimingWheel.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "TimingWheel")
public class TimingWheelController {
    @Autowired
    private TimingWheelService timingWheelService;

    /**
     * 获取时间轮统计信息
     */
    @GetMapping("/stats")
    public TimingWheelStats getStats() {
        return timingWheelService.getStats();
    }

    /**
     * 获取任务执行统计
     */
    @GetMapping("/execution-stats")
    public TaskExecutionStats getExecutionStats() {
        return timingWheelService.getExecutionStats();
    }

    /**
     * 获取所有活跃任务
     */
    @GetMapping("/tasks")
    public List<TimerTaskWrapper> getActiveTasks() {
        return timingWheelService.getActiveTasks();
    }

    /**
     * 获取特定任务信息
     */
    @GetMapping("/tasks/{taskId}")
    public TimerTaskWrapper getTask(@PathVariable String taskId) {
        return timingWheelService.getTaskInfo(taskId);
    }

    /**
     * 创建示例任务
     */
    @PostMapping("/tasks/sample")
    public SampleTask createSampleTask(@RequestBody Map<String, Object> request) {
        String type = (String) request.getOrDefault("type", "simple");
        long delay = ((Number) request.getOrDefault("delay", 1000)).longValue();

        String taskId = timingWheelService.createSampleTask(type, delay);

        SampleTask response = new SampleTask();
        response.setTaskId(taskId);
        response.setType(type);
        response.setDelay(delay);

        return response;
    }

    /**
     * 批量创建任务
     */
    @PostMapping("/tasks/batch")
    public BatchTasks createBatchTasks(@RequestBody Map<String, Object> request) {
        int count = (Integer) request.getOrDefault("count", 10);
        long minDelay = ((Number) request.getOrDefault("minDelay", 1000)).longValue();
        long maxDelay = ((Number) request.getOrDefault("maxDelay", 10000)).longValue();

        List<String> taskIds = timingWheelService.createBatchTasks(count, minDelay, maxDelay);

        BatchTasks response = new BatchTasks();
        response.setTaskIds(taskIds);
        response.setCount(taskIds.size());

        return response;
    }

    /**
     * 取消任务
     */
    @DeleteMapping("/tasks/{taskId}")
    public CancelTask cancelTask(@PathVariable String taskId) {
        boolean cancelled = timingWheelService.cancelTask(taskId);
        CancelTask response = new CancelTask();
        response.setTaskId(taskId);
        response.setCancelled(cancelled);
        response.setMessage(cancelled ? "Task cancelled successfully" : "Task not found or already completed");

        return response;
    }

    /**
     * 清理已完成的任务
     */
    @PostMapping("/cleanup")
    public CleanupTasks cleanupTasks() {
        int removedCount = timingWheelService.cleanupCompletedTasks();
        CleanupTasks response = new CleanupTasks();
        response.setRemovedCount(removedCount);

        return response;
    }

    /**
     * 创建自定义任务
     */
    @PostMapping("/tasks/custom")
    public CustomTask createCustomTask(@RequestBody Map<String, Object> request) {
        String description = (String) request.getOrDefault("description", "Custom task");
        long delay = ((Number) request.getOrDefault("delay", 1000)).longValue();
        String action = (String) request.getOrDefault("action", "log");

        String taskId = timingWheelService.scheduleTask(() -> {
            switch (action.toLowerCase()) {
                case "log":
                    log.info("Custom task executed: {} at {}", description, java.time.LocalDateTime.now());
                    break;
                case "calc":
                    performCalculation(description);
                    break;
                case "sleep":
                    performSleep(description);
                    break;
                default:
                    log.info("Unknown action: {} for task: {}", action, description);
            }
        }, delay, description);

        CustomTask response = new CustomTask();
        response.setTaskId(taskId);
        response.setDescription(description);
        response.setDelay(delay);
        response.setAction(action);
        response.setMessage("Custom task created successfully");

        return response;
    }

    /**
     * 压力测试
     */
    @PostMapping("/stress-test")
    public StressTest stressTest(@RequestBody Map<String, Object> request) {
        int taskCount = (Integer) request.getOrDefault("taskCount", 1000);
        long minDelay = ((Number) request.getOrDefault("minDelay", 100)).longValue();
        long maxDelay = ((Number) request.getOrDefault("maxDelay", 5000)).longValue();
        long startTime = System.currentTimeMillis();
        long endTime = System.currentTimeMillis();
        List<String> taskIds = timingWheelService.createBatchTasks(taskCount, minDelay, maxDelay);

        StressTest response = new StressTest();
        response.setTaskCount(taskIds.size());
        response.setCreationTime(endTime - startTime);
        response.setThroughput(taskIds.size() * 1000.0 / (endTime - startTime));
        response.setMessage("Stress test completed successfully");

        return response;
    }

    /**
     * 获取系统信息
     */
    @GetMapping("/system-info")
    public SystemInfo getSystemInfo() {
        Runtime runtime = Runtime.getRuntime();
        SystemInfo info = new SystemInfo();
        info.setAvailableProcessors(runtime.availableProcessors());
        info.setFreeMemory(runtime.freeMemory());
        info.setTotalMemory(runtime.totalMemory());
        info.setMaxMemory(runtime.maxMemory());
        info.setUsedMemory(runtime.totalMemory() - runtime.freeMemory());
        info.setCurrentTime(java.time.LocalDateTime.now());

        return info;
    }

    private void performCalculation(String description) {
        long result = 0;
        for (int i = 0; i < 1000000; i++)
            result += i;

        log.info("Calculation task '{}' completed, result: {}", description, result);
    }

    private void performSleep(String description) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("Sleep task '{}' completed", description);
    }
}