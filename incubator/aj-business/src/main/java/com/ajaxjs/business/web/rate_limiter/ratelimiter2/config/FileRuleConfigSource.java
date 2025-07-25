package com.ajaxjs.business.web.rate_limiter.ratelimiter2.config;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 基于本地配置文件的资源类, yaml、yml、json等文件格式
 */
public class FileRuleConfigSource implements RuleConfigSource {
    private static final Map<String, RuleConfigParser> configParserMap = new HashMap<>();

    private static final String JSON_FILE_EXTENSION = "json";
    private static final String YAML_FILE_EXTENSION = "json";
    private static final String[] SUPPORTED_FILE_EXTENSIONS = new String[]{JSON_FILE_EXTENSION, YAML_FILE_EXTENSION};

    private static final String RULE_CONFIG_FILE_PREFIX = "sentinel-rule";

    static {
        configParserMap.put(JSON_FILE_EXTENSION, new JsonRuleConfigParser());
        configParserMap.put(YAML_FILE_EXTENSION, new YamlRuleConfigParser());
    }

    @Override
    public RuleConfig load() {
        for (String fileExtension : SUPPORTED_FILE_EXTENSIONS) {
            RuleConfig ruleConfig = loadFileSource(fileExtension);

            if (ruleConfig != null)
                return ruleConfig;
        }

        return null;
    }

    private RuleConfig loadFileSource(String fileExtension) {
        try (InputStream in = getClass().getResourceAsStream("/" + getFileNameByExt(fileExtension))) {
            if (in != null) {
                RuleConfigParser parser = configParserMap.get(fileExtension);
                return parser.parse(in);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

    private String getFileNameByExt(String fileExtension) {
        return RULE_CONFIG_FILE_PREFIX + "." + fileExtension;
    }
}