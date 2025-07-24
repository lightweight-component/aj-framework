package com.ajaxjs.business.delayedtaskqueue;


import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 像轮子一样转动的队列； 环形队列；循环队列。
 */
@Slf4j
public class WheelQueue {
    private static final int DEFAULT_QUEUE_SIZE = 3600;

    /**
     * 建立一个有3600个槽位的环形队列； 每秒轮询一个槽位，3600个就是3600秒=1小时
     */
    final Slot[] slotQueue = new Slot[DEFAULT_QUEUE_SIZE];

    /**
     * 任务Id对应的槽位等任务熟悉
     */
    final Map<String, TaskAttribute> taskSlotMapping = new HashMap<>(1000, 1F);

    {
        for (int i = 0; i < DEFAULT_QUEUE_SIZE; i++)
            this.slotQueue[i] = new Slot();
    }

    /**
     * 添加一个任务到环形队列
     *
     * @param task         任务
     * @param secondsLater 以当前时间点为基准，多少秒以后执行
     */
    public void add(AbstractTask task, int secondsLater) {
        int slotIndex = setAttribute(secondsLater, task, taskSlotMapping);  // 设置任务熟悉

        slotQueue[slotIndex].add(task); // 加到对应槽位的集合中
        log.debug("join task. task={}, slotIndex={}", task, slotIndex);
    }


    /**
     * 根据指定索引获取槽位中的数据。但不删除。
     *
     * @param index
     * @return
     */
    public Slot peek(int index) {
        return slotQueue[index];
    }

    /**
     * 根据任务id移除一个任务
     *
     * @param taskId 任务id
     */
    public void remove(String taskId) {
        TaskAttribute taskAttribute = taskSlotMapping.get(taskId);

        if (taskAttribute != null)
            slotQueue[taskAttribute.getSlotIndex()].remove(taskId);
    }

    public Map<String, TaskAttribute> getTaskSlotMapping() {
        return taskSlotMapping;
    }

    /**
     * setAttribute。
     * <ul>
     * <li>计算任务所在槽位</li>
     * <li>记录任务的加入时间，应该几点执行</li>
     * <li>任务Id和槽位的映射记录到taskSlotMapping中</li>
     * </ul>
     *
     * @param secondsLater    以当前时间点为基准，多少秒以后执行
     * @param task
     * @param taskSlotMapping
     * @return 返回所在槽位索引
     */
    public static int setAttribute(int secondsLater, AbstractTask task, Map<String, TaskAttribute> taskSlotMapping) {
        TaskAttribute taskAttribute = new TaskAttribute();
        Calendar calendar = Calendar.getInstance();
        //把当前时间的分钟和秒加起来
        int currentSecond = calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.SECOND);
        int slotIndex = (currentSecond + secondsLater) % 3600;

        task.setCycleNum(secondsLater / 3600);
        calendar.add(Calendar.SECOND, 1);
        taskAttribute.setExecuteTime(calendar.getTime());
        taskAttribute.setSlotIndex(slotIndex);
        taskAttribute.setJoinTime(new Date());
        taskSlotMapping.put(task.getId(), taskAttribute);

        return slotIndex;
    }
}
