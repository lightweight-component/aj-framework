package com.ajaxjs.delayedtaskqueue;


import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 像轮子一样转动的队列； 环形队列；循环队列。
 */
@Slf4j
public class WheelQueue {
    private static final int DEFULT_QUEUE_SIZE = 3600;

    /**
     * 建立一个有3600个槽位的环形队列； 每秒轮询一个槽位，3600个就是3600秒=1小时
     */
    final Slot[] slotQueue = new Slot[DEFULT_QUEUE_SIZE];

    /**
     * 任务Id对应的槽位等任务熟悉
     */
    final Map<String, TaskAttribute> taskSlotMapping = new HashMap<>(1000, 1F);

    {
        for (int i = 0; i < DEFULT_QUEUE_SIZE; i++)
            this.slotQueue[i] = new Slot();
    }

    /**
     * 添加一个任务到环形队列
     *
     * @param task         任务
     * @param secondsLater 以当前时间点为基准，多少秒以后执行
     */
    public void add(final AbstractTask task, int secondsLater) {
        //设置任务熟悉
        int slotIndex = TaskAttributeUtil.setAttribute(secondsLater, task, taskSlotMapping);
        //加到对应槽位的集合中
        slotQueue[slotIndex].add(task);
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
            slotQueue[taskAttribute.getSoltIndex()].remove(taskId);
    }

    public Map<String, TaskAttribute> getTaskSlotMapping() {
        return taskSlotMapping;
    }

}
