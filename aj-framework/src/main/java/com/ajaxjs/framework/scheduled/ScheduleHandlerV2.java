package com.ajaxjs.framework.scheduled;

import com.ajaxjs.framework.database.DataBaseConnection;
import com.ajaxjs.spring.DiContextUtil;
import com.ajaxjs.sqlman.Action;
import com.ajaxjs.sqlman.JdbcConnection;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.config.*;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Data
@Slf4j
public class ScheduleHandlerV2 {
    /**
     * ThreadPoolTaskExecutor 是 Spring 框架提供的一个线程池执行器，用于管理和执行异步任务
     */
    private ThreadPoolTaskExecutor executor;

    /**
     * 带有 @Scheduled 注解的处理器
     */
    private ScheduledAnnotationBeanPostProcessor scheduledProcessor;

    /**
     * ScheduledTaskRegistrar 是 Spring 框架中用于注册和管理定时任务的类。它是 Spring 内部的一个调度器，负责管理和执行定时任务。
     */
    private ScheduledTaskRegistrar scheduledTaskRegistrar;

    private static final String TASK_NAME = "JOB_TASK_";

    private static final AtomicLong ATOMIC_LONG = new AtomicLong(0L);

    /**
     * 所有被 @Scheduled 注解修饰的任务
     */
    private Set<ScheduledTask> scheduledTasks;

    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        log.info("初始化定时任务管理器");
        executor = DiContextUtil.getBeanNonNull(ThreadPoolTaskExecutor.class);
        scheduledProcessor = DiContextUtil.getBeanNonNull(ScheduledAnnotationBeanPostProcessor.class);

        try {
            Field registrar = scheduledProcessor.getClass().getDeclaredField("registrar");
            registrar.setAccessible(true);
            scheduledTaskRegistrar = (ScheduledTaskRegistrar) registrar.get(scheduledProcessor);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.warn("Failed to initialize the scheduled tasks", e);
        }

        init();
    }

    /**
     * 初始化
     */
    public void init() {
        scheduledTasks = scheduledProcessor.getScheduledTasks(); // 获取了所有被 @Scheduled 注解修饰的任务列表

        if (CollectionUtils.isEmpty(scheduledTasks))
            return;

        DataBaseConnection.initDb();

        try {
            for (ScheduledTask s : scheduledTasks) {
                Task task = s.getTask();

                if (task instanceof CronTask) {
                    CronTask cronTask = (CronTask) s.getTask();
                    ScheduledMethodRunnable scheduledMethodRunnable = (ScheduledMethodRunnable) cronTask.getRunnable();
                    String clzName = scheduledMethodRunnable.getMethod().getDeclaringClass().getName();// 类名
                    String sql = "SELECT id FROM sys_schedule_job WHERE class_name = ? AND express = ?";
                    Integer id = new Action(sql).query(clzName, cronTask.getExpression()).one(Integer.class);

                    if (id == null) { // 持久化
                        JobInfo info = new JobInfo();
                        info.setName(TASK_NAME + ATOMIC_LONG.getAndIncrement());
                        info.setExpress(cronTask.getExpression());
                        info.setClassName(clzName);
                        info.setMethod(scheduledMethodRunnable.getMethod().getName());
                        info.setStatus(JobInfo.ScheduledConstant.NORMAL_STATUS);

                        new Action(info).create().execute(true);
                    }

                    String _sql = ScheduledController.SQL + " WHERE `status` IN (1, 2)";
                    List<JobInfo> list = new Action(_sql).query().list(JobInfo.class);

                    if (!CollectionUtils.isEmpty(list)) {
                        for (JobInfo job : list)
                            cancel(job.getExpress(), job.getClassName(), job.getId(), false);
                    }

                } else if (task instanceof FixedRateTask)
                    log.info(task + "无法动态修改静态配置任务的状态、暂停/恢复任务，以及终止运行中任务");
            }
        } finally {
            JdbcConnection.closeDb();
        }
    }

    /**
     * 取消指定表达式和类的定时任务，并根据需要更新任务状态
     * 主要操作两个列表：scheduledTaskRegistrar.getCronTaskList() 和 scheduledTasks
     *
     * @param express  表达式
     * @param clz      类
     * @param id       任务 id
     * @param isUpdate 是否更新任务状态
     */
    public void cancel(String express, String clz, Integer id, boolean isUpdate) {
        Set<ScheduledTask> scheduledTasks0 = new LinkedHashSet<>(); // 需要取消的任务
        List<CronTask> cronTaskList = new ArrayList<>(scheduledTaskRegistrar.getCronTaskList()); // 获取了所有定时任务的列表

        for (ScheduledTask scheduledTask : scheduledTasks) {
            CronTask cronTask = (CronTask) scheduledTask.getTask();
            ScheduledMethodRunnable runnable = (ScheduledMethodRunnable) cronTask.getRunnable();

            if (express.equals(cronTask.getExpression()) && clz.equals(runnable.getMethod().getDeclaringClass().getName())) {
                scheduledTask.cancel();
                cronTaskList.remove(cronTask);
                scheduledTasks0.add(scheduledTask);

//                if (isUpdate)
//                    new Action(ScheduledController.updateStatus, JobInfo.ScheduledConstant.CANCEL_STATUS, id).update();
            }
        }

        scheduledTaskRegistrar.setCronTasksList(cronTaskList); // 设置新的

        if (!CollectionUtils.isEmpty(scheduledTasks0))
            scheduledTasks.removeAll(scheduledTasks0);
    }

    /**
     * 获取下次时间点列表
     *
     * @param cron  表达式
     * @param count 需要计算的数量
     * @return 返回日期集合
     */
    public static List<Date> calNextPoint(String cron, int count) {
        return calNextPoint(cron, new Date(), count);
    }

    /**
     * 获取下次时间点列表
     *
     * @param cron  表达式
     * @param date  当前日期
     * @param count 需要计算的数量
     * @return 返回日期集合
     */
    public static List<Date> calNextPoint(String cron, Date date, int count) {
        List<Date> points = new ArrayList<>();

        if (CronExpression.isValidExpression(cron)) { // 检验 Cron 表达式是否正确
            CronExpression csg = CronExpression.parse(cron);
            Date nextDate = date;

            for (int i = 0; i < count; i++) {
                Instant next = csg.next(nextDate.toInstant());

                if (next != null) {
                    nextDate = Date.from(next);
                    points.add(nextDate);
                }
            }
        }

        return points;
    }
}
