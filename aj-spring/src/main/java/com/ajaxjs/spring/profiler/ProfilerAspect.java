//package com.ajaxjs.spring.profiler;
//
//
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.reflect.MethodSignature;
//
///**
// * 用于埋点的拦截器
// */
//@Aspect
//@Slf4j
//public class ProfilerAspect {
//    @Around("@annotation(com.ajaxjs.profiler.ProfilerAnno) || @within(com.ajaxjs.profiler.ProfilerAnno)")
//    public Object invoke(ProceedingJoinPoint joinPoint) throws Throwable {
//        if (!ProfilerSwitch.getInstance().isOpenProfilerTree())
//            return joinPoint.proceed();
//
//        String methodName = this.getClassAndMethodName(joinPoint);
//
//        if (null == methodName)
//            return joinPoint.proceed();
//
//        try {
//            if (Profiler.getEntry() == null)
//                Profiler.start(methodName);
//            else
//                Profiler.enter(methodName);
//
//            return joinPoint.proceed();
//        } finally {
//            Profiler.release();
//            Profiler.Entry rootEntry = Profiler.getEntry(); // 当root entry为状态为release的时候，打印信息，并做reset操作
//
//            if (rootEntry != null) {
//                if (rootEntry.isReleased()) {
//                    if (rootEntry.getDuration() > ProfilerSwitch.getInstance().getInvokeTimeout())
//                        log.error(Profiler.dump());
//
//                    Profiler.reset();
//                }
//            }
//        }
//    }
//
//    private String getClassAndMethodName(ProceedingJoinPoint joinPoint) {
//        try {
//            MethodSignature sign = (MethodSignature) joinPoint.getSignature();
//            String clazzName = joinPoint.getTarget().toString();
//
//            return Profiler.split(clazzName, "@")[0] +
//                    ":" + sign.getMethod().getName() +
//                    "(param:" + sign.getMethod().getParameterTypes().length +
//                    ")";
//        } catch (Throwable e) {
//            return null;
//        }
//    }
//}