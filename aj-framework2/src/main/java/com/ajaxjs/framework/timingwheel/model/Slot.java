package com.ajaxjs.framework.timingwheel.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 时间轮槽位，存储任务列表
 */
@Data
@Slf4j
public class Slot {
    private final int index;
    private final ConcurrentLinkedQueue<TimerTaskWrapper> tasks;
    private final AtomicInteger taskCount;
    private volatile long lastAccessTime;

    public Slot(int index) {
        this.index = index;
        this.tasks = new ConcurrentLinkedQueue<>();
        this.taskCount = new AtomicInteger(0);
        this.lastAccessTime = System.currentTimeMillis();
    }

    public void addTask(TimerTaskWrapper task) {
        tasks.offer(task);
        taskCount.incrementAndGet();
        lastAccessTime = System.currentTimeMillis();

        log.debug("Added task {} to slot {}", task.getTaskId(), index);
    }

    public boolean removeTask(String taskId) {
        boolean removed = tasks.removeIf(task -> task.getTaskId().equals(taskId));

        if (removed) {
            taskCount.decrementAndGet();
            lastAccessTime = System.currentTimeMillis();
        }

        return removed;
    }

    public void clear() {
        int removedCount = taskCount.get();
        tasks.clear();
        taskCount.set(0);
        lastAccessTime = System.currentTimeMillis();

        log.debug("Cleared slot {}, removed {} tasks", index, removedCount);
    }

    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    public int getTaskCount() {
        return taskCount.get();
    }

    public List<TimerTaskWrapper> getTasks() {
        return new ArrayList<>(tasks);
    }

    public List<TimerTaskWrapper> drainTasks() {
        List<TimerTaskWrapper> result = new ArrayList<>();
        TimerTaskWrapper task;

        while ((task = tasks.poll()) != null)
            result.add(task);

        taskCount.set(0);
        lastAccessTime = System.currentTimeMillis();

        return result;
    }

    public SlotInfo getSlotInfo() {
        return new SlotInfo(index, taskCount.get(), lastAccessTime, tasks.stream()
                .map(TimerTaskWrapper::getStatus)
                .collect(Collectors.groupingBy(status -> status, Collectors.counting()
                ))
        );
    }


}