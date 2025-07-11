package com.ajaxjs.framework.scheduled;


import com.ajaxjs.framework.model.BusinessException;
import com.ajaxjs.sqlman.Sql;
import com.ajaxjs.sqlman.model.PageResult;
import com.ajaxjs.util.reflect.Clazz;
import com.ajaxjs.util.reflect.Methods;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.TimeZone;

@RestController
@RequestMapping("/scheduled")
@Slf4j
public class ScheduledController {

    @Autowired
    ScheduleHandler scheduleHandler;

    final static String SQL = "SELECT * FROM `schedule_job`";

    final static String updateStatus = "UPDATE schedule_job SET `status` = ? WHERE id = ?";

    @GetMapping
    public PageResult<JobInfo> list(@RequestParam(required = false) String name) {
        String sql = SQL;

        if (StringUtils.hasText(name))
            sql += " WHERE job_name LIKE '%" + name + "%'";

        return Sql.instance().input(sql).page(JobInfo.class);
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
                assert clazz != null;
                clazz.getDeclaredMethod(info.getMethod()).invoke(scheduleHandler.getBeanFactory().getBean(clazz));
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
        assert aClass != null;
        Object bean = scheduleHandler.getBeanFactory().getBean(aClass);
        Method method = Methods.getDeclaredMethod(aClass, info.getMethod());
        assert method != null;
        Assert.isTrue(method.getParameterCount() == 0, "Only no-arg methods may be annotated with @Scheduled");
        Method invocableMethod = AopUtils.selectInvocableMethod(method, bean.getClass());

        CronTask cronTask = new CronTask(new ScheduledMethodRunnable(bean, invocableMethod), new CronTrigger(info.getExpress(), TimeZone.getDefault()));
        scheduleHandler.getScheduledTasks().add(scheduleHandler.getScheduledTaskRegistrar().scheduleCronTask(cronTask));
        scheduleHandler.getScheduledTaskRegistrar().addCronTask(cronTask);

        Sql.instance().input(updateStatus, JobInfo.ScheduledConstant.NORMAL_STATUS, id).update();

        return true;
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

        return Sql.instance().input(updateStatus, JobInfo.ScheduledConstant.DELETE_STATUS, id).update().isOk();
    }

    private JobInfo getJobInfo(Integer id) {
        JobInfo info = Sql.instance().input(SQL + " WHERE id = ?", id).query(JobInfo.class);

        if (info == null)
            throw new BusinessException("不存在该任务 " + id);

        return info;
    }
}
