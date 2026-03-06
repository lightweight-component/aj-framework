CREATE TABLE `shop_spu` (
	`id` INT NOT NULL AUTO_INCREMENT COMMENT 'SPU 主键 id，自增',
	`name` VARCHAR(200) NOT NULL COMMENT 'SPU 名称 (e.g., "iPhone 15 Pro Max")' COLLATE 'utf8mb4_unicode_ci',
	`code` VARCHAR(200) NOT NULL COMMENT '商品编码' COLLATE 'utf8mb4_unicode_ci',
	`tags` VARCHAR(200) NOT NULL COMMENT '商品标签' COLLATE 'utf8mb4_unicode_ci',
	`brand_id` INT NULL DEFAULT NULL COMMENT '品牌ID（数据字典）',
	`category_id` INT NOT NULL COMMENT '所属分类ID（数据字典）',
	`description` TEXT NULL DEFAULT NULL COMMENT 'SPU级别描述' COLLATE 'utf8mb4_unicode_ci',
	`main_image_url` VARCHAR(500) NULL DEFAULT NULL COMMENT '主图URL' COLLATE 'utf8mb4_unicode_ci',
	`stat` TINYINT NULL DEFAULT '0' COMMENT '数据字典：状态',
	`creator` VARCHAR(50) NULL DEFAULT NULL COMMENT '创建人名称（可冗余的）' COLLATE 'utf8mb4_unicode_ci',
	`creator_id` INT NULL DEFAULT NULL COMMENT '创建人 id',
	`create_date` DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP) COMMENT '创建日期',
	`updater` VARCHAR(50) NULL DEFAULT NULL COMMENT '修改人名称（可冗余的）' COLLATE 'utf8mb4_unicode_ci',
	`updater_id` INT NULL DEFAULT NULL COMMENT '修改人 id',
	`update_date` DATETIME NULL DEFAULT (CURRENT_TIMESTAMP) ON UPDATE CURRENT_TIMESTAMP COMMENT '修改日期',
	PRIMARY KEY (`id`) USING BTREE,
	UNIQUE INDEX `idx_spu_id_unique` (`id`) USING BTREE
)
COMMENT='SPU (Standard Product Unit)'
COLLATE='utf8mb4_unicode_ci'

CREATE TABLE `shop_sku` (
	`id` INT NOT NULL AUTO_INCREMENT COMMENT '主键 id，自增',
	`spu_id` INT NOT NULL COMMENT '关联的 SPU ID',
	`name` VARCHAR(200) NOT NULL COMMENT 'SKU 名称 (e.g., "iPhone 15 Pro 128GB Black")' COLLATE 'utf8mb4_unicode_ci',
	`code` VARCHAR(200) NOT NULL COMMENT 'SKU 编码 (e.g., "IP15P-BLK-128")' COLLATE 'utf8mb4_unicode_ci',
	`price` INT NULL DEFAULT NULL COMMENT '销售价（单位：分）',
	`cost_price` INT NULL DEFAULT NULL COMMENT '成本价（单位：分）',
	`market_price` INT NULL DEFAULT NULL COMMENT '划线价/原价（单位：分）',
	`stock` INT NULL DEFAULT NULL COMMENT '总库存数量',
	`sale_stock` INT NULL DEFAULT NULL COMMENT '可售库存数量',
	`lock_stock` INT NULL DEFAULT NULL COMMENT '锁定库存数量',
	`weight` DECIMAL(10,2) NULL DEFAULT NULL COMMENT '重量 (kg)',
	`volume` VARCHAR(50) NULL DEFAULT NULL COMMENT '体积 (长x宽x高 cm)' COLLATE 'utf8mb4_unicode_ci',
	`image_url` VARCHAR(500) NULL DEFAULT NULL COMMENT 'SKU 图片URL' COLLATE 'utf8mb4_unicode_ci',
	`bar_code` VARCHAR(100) NULL DEFAULT NULL COMMENT '条形码/EAN码' COLLATE 'utf8mb4_unicode_ci',
	`stat` TINYINT NULL DEFAULT '0' COMMENT '数据字典：状态',
	`creator` VARCHAR(50) NULL DEFAULT NULL COMMENT '创建人名称（可冗余的）' COLLATE 'utf8mb4_bin',
	`creator_id` INT NULL DEFAULT NULL COMMENT '创建人 id',
	`create_date` DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP) COMMENT '创建日期',
	`updater` VARCHAR(50) NULL DEFAULT NULL COMMENT '修改人名称（可冗余的）' COLLATE 'utf8mb4_bin',
	`updater_id` INT NULL DEFAULT NULL COMMENT '修改人 id',
	`update_date` DATETIME NULL DEFAULT (CURRENT_TIMESTAMP) ON UPDATE CURRENT_TIMESTAMP COMMENT '修改日期',
	PRIMARY KEY (`id`) USING BTREE,
	UNIQUE INDEX `id_UNIQUE` (`id`) USING BTREE,
	UNIQUE INDEX `uk_sku_code` (`code`) USING BTREE,
	INDEX `idx_spu_id` (`spu_id`) USING BTREE
)
COMMENT='SKU (Stock Keeping Unit)'
COLLATE='utf8mb4_unicode_ci'

CREATE TABLE `shop_attribute_definition` (
	`id` INT NOT NULL AUTO_INCREMENT COMMENT '主键 id，自增',
	`name` VARCHAR(20) NOT NULL COMMENT '属性名称，如 "颜色", "尺寸"' COLLATE 'utf8mb4_unicode_ci',
	`content` VARCHAR(256) NULL DEFAULT NULL COMMENT '简介' COLLATE 'utf8mb4_unicode_ci',
	`category_id` INT NULL DEFAULT NULL COMMENT '所属分类 id（数据字典）',
	`creator` VARCHAR(50) NULL DEFAULT NULL COMMENT '创建人名称（可冗余的）' COLLATE 'utf8mb4_bin',
	`creator_id` INT NULL DEFAULT NULL COMMENT '创建人 id',
	`create_date` DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP) COMMENT '创建日期',
	PRIMARY KEY (`id`) USING BTREE,
	UNIQUE INDEX `id_UNIQUE` (`id`) USING BTREE,
	INDEX `idx_category_id` (`category_id`) USING BTREE
)
COMMENT='属性定义 - 如 "颜色", "尺寸"'
COLLATE='utf8mb4_unicode_ci'

CREATE TABLE `shop_attribute_value` (
	`id` INT NOT NULL AUTO_INCREMENT COMMENT '主键 id，自增',
	`attr_def_id` INT NULL DEFAULT NULL COMMENT '关联的属性定义 ID',
	`value` VARCHAR(200) NOT NULL COMMENT '属性值，如 "红色", "XL"' COLLATE 'utf8mb4_unicode_ci',
	`content` VARCHAR(256) NULL DEFAULT NULL COMMENT '简介' COLLATE 'utf8mb4_unicode_ci',
	`creator` VARCHAR(50) NULL DEFAULT NULL COMMENT '创建人名称（可冗余的）' COLLATE 'utf8mb4_bin',
	`creator_id` INT NULL DEFAULT NULL COMMENT '创建人 id',
	`create_date` DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP) COMMENT '创建日期',
	PRIMARY KEY (`id`) USING BTREE,
	UNIQUE INDEX `id_UNIQUE` (`id`) USING BTREE,
	INDEX `idx_attr_def_id` (`attr_def_id`) USING BTREE,
	CONSTRAINT `fk_attr_value_def` FOREIGN KEY (`attr_def_id`) REFERENCES `shop_attribute_definition` (`id`) ON UPDATE NO ACTION ON DELETE CASCADE
)
COMMENT='属性值 - 如 "红色", "XL"'
COLLATE='utf8mb4_unicode_ci'

CREATE TABLE `shop_sku_attribute_value` (
	`id` INT NOT NULL AUTO_INCREMENT COMMENT '主键 id，自增',
	`sku_id` INT NOT NULL COMMENT 'SKU ID',
	`attr_def_id` INT NULL DEFAULT NULL COMMENT '关联的属性定义 ID',
	`attr_def_name` VARCHAR(200) NOT NULL COMMENT '冗余存储属性定义名称，如 "名称"' COLLATE 'utf8mb4_unicode_ci',
	`attr_value_id` INT NULL DEFAULT NULL COMMENT '关联的属性值 ID',
	`attr_value_text` VARCHAR(200) NOT NULL COMMENT '冗余存储属性值文本，如 "黑色"' COLLATE 'utf8mb4_unicode_ci',
	`stat` TINYINT NULL DEFAULT '0' COMMENT '数据字典：状态',
	`creator` VARCHAR(50) NULL DEFAULT NULL COMMENT '创建人名称（可冗余的）' COLLATE 'utf8mb4_bin',
	`creator_id` INT NULL DEFAULT NULL COMMENT '创建人 id',
	`create_date` DATETIME NOT NULL DEFAULT (CURRENT_TIMESTAMP) COMMENT '创建日期',
	`updater` VARCHAR(50) NULL DEFAULT NULL COMMENT '修改人名称（可冗余的）' COLLATE 'utf8mb4_bin',
	`updater_id` INT NULL DEFAULT NULL COMMENT '修改人 id',
	`update_date` DATETIME NULL DEFAULT (CURRENT_TIMESTAMP) ON UPDATE CURRENT_TIMESTAMP COMMENT '修改日期',
	PRIMARY KEY (`id`) USING BTREE,
	UNIQUE INDEX `id_UNIQUE` (`id`) USING BTREE,
	INDEX `idx_sku_id` (`sku_id`) USING BTREE
)
COMMENT='SKU 与 属性值 的关联表'
COLLATE='utf8mb4_unicode_ci'