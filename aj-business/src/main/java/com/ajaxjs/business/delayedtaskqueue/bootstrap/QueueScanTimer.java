package com.ajaxjs.business.delayedtaskqueue.bootstrap;

import com.ajaxjs.business.delayedtaskqueue.AbstractTask;
import com.ajaxjs.business.delayedtaskqueue.Slot;
import com.ajaxjs.business.delayedtaskqueue.WheelQueue;
import lombok.extern.slf4j.Slf4j;

import java.util.Calendar;
import java.util.Iterator;
import java.util.TimerTask;
import java.util.concurrent.*;

/**
 * 类似钟表的秒针，队列是表盘，这里有个类似秒针的循环器，每秒循环一次；就类似秒针再走。
 */
@Slf4j
public class QueueScanTimer extends TimerTask {
    /**
     * 环形队列
     */
    private final WheelQueue queue;

    private static final ThreadFactory slotThreadFactory = new MyDefaultThreadFactory("slotThreadGroup");

    private static final ThreadFactory taskThreadFactory = new MyDefaultThreadFactory("taskThreadGroup");

    /**
     * 处理每个槽位的线程，循环到这个槽位，立即丢到一个线程去处理，然后继续循环队列。
     */
    private final ThreadPoolExecutor slotPool = new ThreadPoolExecutor(60, 60,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), slotThreadFactory);

    /**
     * 处理每一个槽位中task集合的线程， 集合中的每个任务一个线程
     */
    private final ThreadPoolExecutor taskPool = new ThreadPoolExecutor(1000, 1000,
            0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), taskThreadFactory);

    public QueueScanTimer(WheelQueue queue) {
        super();
        this.queue = queue;
    }

    @Override
    public void run() {
        if (queue == null)
            return;
        try {
            Calendar calendar = Calendar.getInstance();
            int currentSecond = calendar.get(Calendar.MINUTE) * 60 + calendar.get(Calendar.SECOND);
            Slot slot = queue.peek(currentSecond);
            log.debug("current slot:{}", currentSecond);

            slotPool.execute(new SlotTask(slot.getTasks(), currentSecond));
        } catch (Exception e) {
            //这里一个槽位的屏蔽异常，继续执行。
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 槽位任务
     */
    final class SlotTask implements Runnable {
        ConcurrentLinkedQueue<AbstractTask> tasks;

        int currentSecond;

        public SlotTask(ConcurrentLinkedQueue<AbstractTask> tasks, int currentSecond) {
            super();
            this.tasks = tasks;
            this.currentSecond = currentSecond;
        }

        @Override
        public void run() {
            if (tasks == null)
                return;

            String taskId;
            Iterator<AbstractTask> it = tasks.iterator();

            while (it.hasNext()) {
                AbstractTask task = it.next();
                log.debug("running_current_slot:currentSecond={}, task={}, taskQueueSize={}", currentSecond, task.toString(), tasks.size());
                taskId = task.getId();

                if (task.getCycleNum() <= 0) {
                    taskPool.execute(task);
                    it.remove();
                    queue.getTaskSlotMapping().remove(taskId);
                } else {
                    log.debug("countDown#running_current_solt:currentSecond={}, task={}", currentSecond, task.toString());
                    task.countDown();
                }
            }
        }
    }
}
