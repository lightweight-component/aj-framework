-- 直播间表
CREATE TABLE `live_room` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL COMMENT '直播标题',
  `cover_url` varchar(255) DEFAULT NULL COMMENT '封面URL',
  `user_id` bigint NOT NULL COMMENT '主播用户ID',
  `stream_key` varchar(64) NOT NULL COMMENT '推流密钥',
  `stream_url` varchar(255) DEFAULT NULL COMMENT '推流地址',
  `hls_url` varchar(255) DEFAULT NULL COMMENT 'HLS播放地址',
  `flv_url` varchar(255) DEFAULT NULL COMMENT 'HTTP-FLV播放地址',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0未开播 1直播中 2直播结束',
  `view_count` bigint NOT NULL DEFAULT '0' COMMENT '观看人数',
  `like_count` bigint NOT NULL DEFAULT '0' COMMENT '点赞数',
  `start_time` datetime DEFAULT NULL COMMENT '开播时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stream_key` (`stream_key`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播间信息表';

-- 直播流表
CREATE TABLE `live_stream` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL COMMENT '直播间ID',
  `stream_id` varchar(64) NOT NULL COMMENT '流ID',
  `protocol` varchar(20) NOT NULL COMMENT '协议类型',
  `bitrate` int DEFAULT NULL COMMENT '码率',
  `resolution` varchar(20) DEFAULT NULL COMMENT '分辨率',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0未启动 1活跃 2已结束',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stream_id` (`stream_id`),
  KEY `idx_room_id` (`room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播流信息表';

-- 直播回放表
CREATE TABLE `live_recording` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `room_id` bigint NOT NULL COMMENT '直播间ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `file_url` varchar(255) COMMENT '文件URL',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
  `duration` int DEFAULT NULL COMMENT '时长(秒)',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0录制中 1录制完成 2处理中 3可用 4删除',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_room_id` (`room_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播回放表';
