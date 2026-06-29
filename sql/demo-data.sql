USE exchange_db;
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM user_favorite;
DELETE FROM busi_exchange_order;
DELETE FROM busi_item;

ALTER TABLE user_favorite AUTO_INCREMENT = 1;
ALTER TABLE busi_exchange_order AUTO_INCREMENT = 1;
ALTER TABLE busi_item AUTO_INCREMENT = 1;

UPDATE sys_user
SET nickname = '小林同学',
    contact_info = '微信：xiaolin_exchange',
    profile = '摄影爱好者，喜欢纸质书和有故事的小物件。'
WHERE username = 'xiaolin_demo2026';

UPDATE sys_user
SET nickname = '陈宇',
    contact_info = 'QQ：20260625',
    profile = '数码产品爱好者，也喜欢收集校园纪念品。'
WHERE username = 'chenyu_demo2026';

SET @admin_id = (SELECT id FROM sys_user WHERE username = 'admin');
SET @lin_id = (SELECT id FROM sys_user WHERE username = 'xiaolin_demo2026');
SET @chen_id = (SELECT id FROM sys_user WHERE username = 'chenyu_demo2026');

INSERT INTO busi_item
    (user_id, category_id, title, description, exchange_wish, images, status, update_time)
VALUES
    (@admin_id, 1, '复古胶片相机',
     '九成新银色胶片相机，快门和测光功能正常，附相机带。适合刚开始接触胶片摄影的同学。',
     '想换一副成色不错的头戴式耳机',
     '/uploads/item/demo-camera.png', 2, NOW()),
    (@admin_id, 3, '帆布托特包',
     '米白色大容量帆布包，内有分层，能装下 14 寸电脑，清洗后闲置。',
     '想换几本小说或专业课教材',
     '/uploads/item/demo-books.png', 1, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
    (@lin_id, 2, '计算机专业教材套装',
     '包含数据结构、计算机网络和数据库三本教材，重点章节有少量整洁笔记。',
     '想换帆布包、摄影配件，也欢迎其他有趣提议',
     '/uploads/item/demo-books.png', 1, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
    (@lin_id, 3, '暖光蘑菇台灯',
     '宿舍床头小台灯，三档亮度，暖光不刺眼，USB 供电。',
     '想换桌面收纳或一盆好养的小绿植',
     '/uploads/item/demo-lamp.png', 1, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
    (@chen_id, 1, '奶油色头戴式耳机',
     '佩戴舒适，续航正常，耳罩无破损，已清洁消毒并附充电线。',
     '想换胶片相机或蓝牙音箱',
     '/uploads/item/demo-headphones.png', 2, DATE_SUB(NOW(), INTERVAL 4 HOUR)),
    (@chen_id, 3, '相机收纳包',
     '防泼水相机包，内部隔层可调，适合微单或小型胶片机。',
     '想换台灯、桌面摆件或校园文创',
     '/uploads/item/demo-camera.png', 1, DATE_SUB(NOW(), INTERVAL 5 HOUR));

INSERT INTO user_favorite (user_id, item_id, create_time)
VALUES
    (@admin_id, 3, NOW()),
    (@admin_id, 4, DATE_SUB(NOW(), INTERVAL 5 MINUTE));

INSERT INTO busi_exchange_order
    (initiator_id, target_id, offer_item_id, target_item_id, remark, status,
     initiator_confirmed, target_confirmed, create_time, finish_time)
VALUES
    (@lin_id, @admin_id, 3, 2,
     '你好！教材保存得很好，想和你的帆布包交换，可以在图书馆门口面交。',
     0, 0, 0, DATE_SUB(NOW(), INTERVAL 30 MINUTE), NULL),
    (@admin_id, @chen_id, 1, 5,
     '想用这台胶片相机换你的耳机，配件都齐全，可以当面试机。',
     1, 0, 0, DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL);

SET FOREIGN_KEY_CHECKS = 1;
