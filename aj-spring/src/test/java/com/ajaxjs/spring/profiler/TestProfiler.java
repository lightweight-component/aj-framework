package com.ajaxjs.spring.profiler;

import org.junit.jupiter.api.Test;

public class TestProfiler {
    @Test
    void test() {
        ProfilerSwitch.getInstance().setOpenProfilerNanoTime(true);
        TestProfiler t = new TestProfiler();
        t.rootMethod();
    }

    public void rootMethod(){
        Profiler.start("rootMethod");
        firstMethod();
        Profiler.release();
        System.out.println(Profiler.dump());
        Profiler.reset();

    }
    public void firstMethod(){
        Profiler.enter("first");
        secondMethod();
        Profiler.release();

    }
    public void secondMethod(){
        Profiler.enter("second");
        Profiler.release();
    }
}
