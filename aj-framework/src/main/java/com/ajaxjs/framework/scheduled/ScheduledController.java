package com.ajaxjs.framework.scheduled;

import com.ajaxjs.spring.DiContextUtil;
import com.ajaxjs.sqlman.Action;
import com.ajaxjs.sqlman.crud.page.PageResult;
import com.ajaxjs.util.reflect.Clazz;
import com.ajaxjs.util.reflect.Methods;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.TimeZone;

@Slf4j
@RestController
@RequestMapping("/scheduled")
@ConditionalOnProperty(
        name = "aj-framework.schedule_mgr.enabled", // 配置属性名
        havingValue = "true",                   // 期望的值，默认为 "true"
        matchIfMissing = false                  // 如果配置文件中没有此属性，默认是 false，即不加载组件
)

public class ScheduledController {
    @Autowired
    ScheduleHandler scheduleHandler;

    final static String SQL = "SELECT * FROM `sys_schedule_job`";

    final static String UPDATE_STATUS = "UPDATE sys_schedule_job SET `status` = ? WHERE id = ?";

    @GetMapping
    public PageResult<JobInfo> list(@RequestParam(required = false) String name) {
        String sql = SQL;

        if (StringUtils.hasText(name))
            sql += " WHERE job_name LIKE '%" + name + "%'";

        return new Action(sql).query().pageByStartLimit(DiContextUtil.getRequest(), JobInfo.class);
    }

    /**
     * 触发指定类中的指定方法
     *
     * @param id 任务 id
     * @return 是否成功
     */
    @PostMapping("/trigger/{id}")
    public boolean trigger(@PathVariable Integer id) {
        JobInfo info = getJobInfo(id);

        scheduleHandler.getExecutor().execute(() -> {
            try {
                Class<?> clazz = Clazz.getClassByName(info.getClassName());
//                Object bean = scheduleHandler.getBeanFactory().getBean(clazz);
                Object bean = DiContextUtil.getBean(clazz);
                clazz.getDeclaredMethod(info.getMethod()).invoke(bean);
            } catch (Exception e) {
                log.warn("Trigger Error", e);
            }
        });

        return true;
    }

    /**
     * 恢复指定类中指定方法的定时任务
     *
     * @param id 任务 id
     * @return 是否成功
     */
    @PostMapping("/resume/{id}")
    public boolean resume(@PathVariable Integer id) {
        JobInfo info = getJobInfo(id);

        Class<?> aClass = Clazz.getClassByName(info.getClassName());
//        Object bean = scheduleHandler.getBeanFactory().getBean(aClass);
        Object bean = DiContextUtil.getBean(aClass);
        Method method = Methods.getDeclaredMethod(aClass, info.getMethod());
        Assert.isTrue(method.getParameterCount() == 0, "Only no-arg methods may be annotated with @Scheduled");
        Method invocableMethod = AopUtils.selectInvocableMethod(method, bean.getClass());

        CronTask cronTask = new CronTask(new ScheduledMethodRunnable(bean, invocableMethod), new CronTrigger(info.getExpress(), TimeZone.getDefault()));
        scheduleHandler.getScheduledTasks().add(scheduleHandler.getScheduledTaskRegistrar().scheduleCronTask(cronTask));
        scheduleHandler.getScheduledTaskRegistrar().addCronTask(cronTask);

        return new Action(UPDATE_STATUS).update(JobInfo.ScheduledConstant.NORMAL_STATUS, id).execute().isOk();
    }

    /**
     * 暂停任务
     *
     * @param id 任务 id
     * @return 是否成功
     */
    @PostMapping("/pause/{id}")
    public boolean pause(@PathVariable Integer id) {
        JobInfo info = getJobInfo(id);
        scheduleHandler.cancel(info.getExpress(), info.getClassName(), id, true);

        return true;
    }

    /**
     * 删除任务
     *
     * @param id 任务 id
     * @return 是否成功
     */
    @PostMapping("/remove/{id}")
    public boolean remove(@PathVariable Integer id) {
        JobInfo info = getJobInfo(id);
        scheduleHandler.cancel(info.getExpress(), info.getClassName(), id, false);

        return new Action(UPDATE_STATUS).update(JobInfo.ScheduledConstant.DELETE_STATUS, id).execute().isOk();
    }

    private JobInfo getJobInfo(Integer id) {
        JobInfo info = new Action(SQL + " WHERE id = ?").query(id).one(JobInfo.class);

        if (info == null)
            throw new NullPointerException("不存在该任务 " + id);

        return info;
    }
}
