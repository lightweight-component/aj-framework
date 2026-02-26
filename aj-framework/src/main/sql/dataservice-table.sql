CREATE TABLE `ds_datasource` (
	`id` INT NOT NULL AUTO_INCREMENT COMMENT '主键 id，自增',
	`name` VARCHAR(45) NOT NULL COMMENT '名称' COLLATE 'utf8mb4_unicode_ci',
	`url_dir` VARCHAR(50) NOT NULL COMMENT '数据源编码，唯一' COLLATE 'utf8mb4_unicode_ci',
	`type` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '数据源类型' COLLATE 'utf8mb4_unicode_ci',
	`url` VARCHAR(255) NOT NULL COMMENT '连接地址' COLLATE 'utf8mb4_unicode_ci',
	`username` VARCHAR(255) NULL DEFAULT NULL COMMENT '登录用户' COLLATE 'utf8mb4_unicode_ci',
	`password` VARCHAR(255) NULL DEFAULT NULL COMMENT '登录密码' COLLATE 'utf8mb4_unicode_ci',
	`connect_ok` TINYINT(1) NULL DEFAULT NULL COMMENT '是否连接验证成功',
	`stat` TINYINT NULL DEFAULT NULL COMMENT '数据字典：状态',
	`cross_db` TINYINT(1) NULL DEFAULT NULL COMMENT '是否跨库',
	`uid` BIGINT NULL DEFAULT NULL COMMENT '唯一 id，通过 uuid 生成不重复 id',
	`creator` VARCHAR(50) NULL DEFAULT NULL COMMENT '创建人名称（可冗余的）' COLLATE 'utf8mb4_unicode_ci',
	`creator_id` INT NULL DEFAULT NULL COMMENT '创建人 id',
	`create_date` DATETIME NOT NULL DEFAULT (now()) COMMENT '创建日期',
	`updater` VARCHAR(50) NULL DEFAULT NULL COMMENT '修改人名称（可冗余的）' COLLATE 'utf8mb4_unicode_ci',
	`updater_id` INT NULL DEFAULT NULL COMMENT '修改人 id',
	`update_date` DATETIME NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP COMMENT '修改日期',
	PRIMARY KEY (`id`) USING BTREE,
	UNIQUE INDEX `id_UNIQUE` (`id`) USING BTREE
)
COMMENT='数据源'
COLLATE='utf8mb4_unicode_ci'

CREATE TABLE `ds_namespace` (
	`id` INT NOT NULL AUTO_INCREMENT COMMENT '主键 id，自增',
	`namespace` VARCHAR(45) NOT NULL COMMENT '名称 KEY' COLLATE 'utf8mb4_unicode_ci',
	`table_name` VARCHAR(45) NOT NULL COMMENT '表名' COLLATE 'utf8mb4_unicode_ci',
	`list_order_by_date` TINYINT NULL DEFAULT '1' COMMENT 'Whether to automatically add sorting by date',
	`tenant_isolation` TINYINT NULL DEFAULT '0' COMMENT 'Whether to add tenant data isolation',
	`current_user_only` TINYINT NULL DEFAULT '0' COMMENT 'Whether to restrict the query results to include only data belonging to the current user.',
	`filter_deleted` TINYINT NULL DEFAULT '1' COMMENT 'Whether to filter deleted data',
	`table_join` JSON NULL DEFAULT NULL COMMENT 'Config for joining tables',
	`stat` SMALLINT NULL DEFAULT '0' COMMENT '状态',
	`creator` VARCHAR(50) NULL DEFAULT NULL COMMENT '创建人名称（可冗余的）' COLLATE 'utf8mb4_unicode_ci',
	`creator_id` INT NULL DEFAULT NULL COMMENT '创建人 id',
	`create_date` DATETIME NOT NULL DEFAULT (now()) COMMENT '创建日期',
	`updater` VARCHAR(50) NULL DEFAULT NULL COMMENT '修改人名称（可冗余的）' COLLATE 'utf8mb4_unicode_ci',
	`updater_id` INT NULL DEFAULT NULL COMMENT '修改人 id',
	`update_date` DATETIME NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP COMMENT '修改日期',
	PRIMARY KEY (`id`) USING BTREE,
	UNIQUE INDEX `id_UNIQUE` (`id`) USING BTREE
)
COMMENT='Fast CRUD 配置'
COLLATE='utf8mb4_unicode_ci'

CREATE TABLE `ds_widget_config` (
	`id` INT NOT NULL AUTO_INCREMENT COMMENT '主键 id，自增',
	`name` VARCHAR(45) NULL DEFAULT NULL COMMENT '名称、描述' COLLATE 'utf8_general_ci',
	`datasource_id` INT NULL DEFAULT NULL COMMENT '数据源 id',
	`datasource_name` VARCHAR(50) NULL DEFAULT NULL COMMENT '数据源 名称' COLLATE 'utf8_general_ci',
	`table_name` VARCHAR(50) NULL DEFAULT NULL COMMENT '关联的表' COLLATE 'utf8_general_ci',
	`config` JSON NULL DEFAULT NULL COMMENT 'JSON 配置',
	`type` VARCHAR(10) NOT NULL COMMENT '类型' COLLATE 'utf8_general_ci',
	`stat` TINYINT NOT NULL DEFAULT '0' COMMENT '数据字典：状态',
	`creator` INT NULL DEFAULT NULL COMMENT '创建者 id',
	`create_date` DATETIME NOT NULL DEFAULT (now()) COMMENT '创建日期',
	`update_date` DATETIME NOT NULL DEFAULT (now()) ON UPDATE CURRENT_TIMESTAMP COMMENT '修改日期',
	PRIMARY KEY (`id`) USING BTREE
)
COMMENT='UI组件配置器'
COLLATE='utf8_general_ci'