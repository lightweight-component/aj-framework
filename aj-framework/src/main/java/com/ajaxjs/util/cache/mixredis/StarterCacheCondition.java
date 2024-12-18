//package com.ajaxjs.util.cache.mixredis;
//
//import org.springframework.boot.bind.RelaxedPropertyResolver;
//import org.springframework.context.annotation.Condition;
//import org.springframework.context.annotation.ConditionContext;
//import org.springframework.core.type.AnnotatedTypeMetadata;
//
//public class StarterCacheCondition implements Condition {
//    @Override
//    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
//        RelaxedPropertyResolver resolver = new RelaxedPropertyResolver(context.getEnvironment(), "springext.cache.");
//        String env = resolver.getProperty("type");
//
//        if (env == null)
//            return false;
//
//        return "local2redis".equalsIgnoreCase(env);
//    }
//}