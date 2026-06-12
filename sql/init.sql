-- exchange-backend 本地开发数据库初始化脚本
USE exchange_db;

CREATE TABLE IF NOT EXISTS sys_user (
    id           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户唯一主键',
    username     VARCHAR(64)  NOT NULL COMMENT '登录账号',
    password     VARCHAR(128) NOT NULL COMMENT '登录密码',
    nickname     VARCHAR(64)  DEFAULT NULL COMMENT '前台展示昵称',
    contact_info VARCHAR(128) DEFAULT NULL COMMENT '线下联系方式',
    role         TINYINT      NOT NULL DEFAULT 0 COMMENT '角色：0-普通会员，1-系统管理员',
    status       TINYINT      NOT NULL DEFAULT 1 COMMENT '账号状态：1-正常，0-限制登录',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

CREATE TABLE IF NOT EXISTS busi_item (
    item_id       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '物品唯一主键',
    user_id       BIGINT       NOT NULL COMMENT '发布者ID',
    category_id   INT          DEFAULT NULL COMMENT '二级分类ID',
    title         VARCHAR(128) NOT NULL COMMENT '物品标题',
    description   TEXT         DEFAULT NULL COMMENT '物品详细描述',
    exchange_wish VARCHAR(256) DEFAULT NULL COMMENT '期望换到的物品说明',
    images        VARCHAR(512) DEFAULT NULL COMMENT '物品图片URL',
    status        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1-在架，2-交换中，3-已换出，4-下架',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    PRIMARY KEY (item_id),
    KEY idx_user_id (user_id),
    KEY idx_category_status (category_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品信息表';
