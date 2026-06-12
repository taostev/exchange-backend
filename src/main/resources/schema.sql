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

ALTER TABLE sys_user ADD COLUMN profile VARCHAR(255) DEFAULT NULL COMMENT '个人简介';

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

CREATE TABLE IF NOT EXISTS busi_exchange_order (
    order_id       BIGINT      NOT NULL AUTO_INCREMENT COMMENT '意向订单唯一主键',
    initiator_id   BIGINT      NOT NULL COMMENT '发起请求方用户ID',
    target_id      BIGINT      NOT NULL COMMENT '目标物品主人ID',
    offer_item_id  BIGINT      NOT NULL COMMENT '发起方愿意交换的物品ID',
    target_item_id BIGINT      NOT NULL COMMENT '发起方想换取的目标物品ID',
    remark         VARCHAR(255) DEFAULT NULL COMMENT '交换备注',
    status         TINYINT     NOT NULL DEFAULT 0 COMMENT '0-待确认,1-已同意,2-已拒绝,3-已完成,4-已取消',
    create_time    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '意向发起时间',
    finish_time    DATETIME    DEFAULT NULL COMMENT '交易完成时间',
    PRIMARY KEY (order_id),
    KEY idx_initiator_id (initiator_id),
    KEY idx_target_id (target_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交换意向订单表';

CREATE TABLE IF NOT EXISTS sys_category (
    category_id BIGINT      NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    parent_id   BIGINT      DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
    name        VARCHAR(64) NOT NULL COMMENT '分类名称',
    sort        INT         NOT NULL DEFAULT 0 COMMENT '排序值',
    PRIMARY KEY (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品分类字典表';

CREATE TABLE IF NOT EXISTS user_favorite (
    favorite_id BIGINT   NOT NULL AUTO_INCREMENT COMMENT '关注记录ID',
    user_id     BIGINT   NOT NULL COMMENT '会员ID',
    item_id     BIGINT   NOT NULL COMMENT '物品ID',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (favorite_id),
    UNIQUE KEY uk_user_item (user_id, item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物品关注表';
