package com.ajaxjs.business.web.rate_limiter.ratelimiter2.config;

//import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;

public class YamlRuleConfigParser implements RuleConfigParser {
    @Override
    public RuleConfig parse(InputStream in) {
//        if (in != null)
//            return new Yaml().loadAs(in, RuleConfig.class);

        return null;
    }
}