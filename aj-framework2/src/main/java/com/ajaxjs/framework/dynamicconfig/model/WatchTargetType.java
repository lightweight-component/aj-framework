package com.ajaxjs.framework.dynamicconfig.model;

/**
 * The watch target type, from spring.config.location or import:configtree / file
 */
public enum WatchTargetType {
    /**
     * When using spring.config.location
     */
    CONFIG_LOCATION,

    /**
     * When using spring.config.import=file:
     */
    CONFIG_IMPORT_FILE,

    /**
     * When using spring.config.import=configtree:
     */
    CONFIG_IMPORT_TREE
}