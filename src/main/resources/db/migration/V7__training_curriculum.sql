-- 培训课程结构化：素材类型、最小时长、学习进度、考试会话

SET @db := DATABASE();

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'training_materials'
       AND COLUMN_NAME = 'sort_order') = 0,
    'ALTER TABLE training_materials ADD COLUMN sort_order INT NOT NULL DEFAULT 0 COMMENT ''课程排序'' AFTER content',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'training_materials'
       AND COLUMN_NAME = 'material_type') = 0,
    'ALTER TABLE training_materials ADD COLUMN material_type VARCHAR(20) NOT NULL DEFAULT ''DOC'' COMMENT ''DOC/VIDEO'' AFTER sort_order',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'training_materials'
       AND COLUMN_NAME = 'module_code') = 0,
    'ALTER TABLE training_materials ADD COLUMN module_code VARCHAR(50) NULL COMMENT ''课程模块编码'' AFTER material_type',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'training_materials'
       AND COLUMN_NAME = 'min_duration_seconds') = 0,
    'ALTER TABLE training_materials ADD COLUMN min_duration_seconds INT NOT NULL DEFAULT 60 COMMENT ''最短学习秒数'' AFTER module_code',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'training_materials'
       AND COLUMN_NAME = 'media_url') = 0,
    'ALTER TABLE training_materials ADD COLUMN media_url VARCHAR(500) NULL COMMENT ''视频/附件地址'' AFTER min_duration_seconds',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'training_materials'
       AND COLUMN_NAME = 'is_required') = 0,
    'ALTER TABLE training_materials ADD COLUMN is_required TINYINT NOT NULL DEFAULT 1 COMMENT ''是否必修'' AFTER media_url',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sitter_training_records'
       AND COLUMN_NAME = 'learning_started_at') = 0,
    'ALTER TABLE sitter_training_records ADD COLUMN learning_started_at DATETIME NULL COMMENT ''开始学习时间'' AFTER learning_completed_at',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sitter_training_records'
       AND COLUMN_NAME = 'learning_progress_json') = 0,
    'ALTER TABLE sitter_training_records ADD COLUMN learning_progress_json TEXT NULL COMMENT ''各素材学习进度JSON'' AFTER learning_started_at',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sitter_training_records'
       AND COLUMN_NAME = 'exam_question_ids_json') = 0,
    'ALTER TABLE sitter_training_records ADD COLUMN exam_question_ids_json TEXT NULL COMMENT ''本次考试题目ID列表'' AFTER last_exam_at',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'sitter_training_records'
       AND COLUMN_NAME = 'exam_started_at') = 0,
    'ALTER TABLE sitter_training_records ADD COLUMN exam_started_at DATETIME NULL COMMENT ''本次考试开始时间'' AFTER exam_question_ids_json',
    'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 默认全部设为选修，再标记 5 门必修
UPDATE training_materials SET is_required = 0 WHERE deleted = 0;
UPDATE training_materials SET
    sort_order = 1,
    material_type = 'DOC',
    module_code = 'INTRO',
    min_duration_seconds = 60,
    is_required = 1,
    title = '平台服务总则',
    content = '## 平台服务总则\n\n欢迎加入宠托平台。本课程将帮助你建立标准化服务认知。\n\n- 尊重宠主隐私与家庭边界\n- 按订单要求完成服务并上传凭证\n- 遇到异常第一时间上报平台与宠主\n\n> 完成全部必修课后，方可参加准入考试。'
WHERE material_id = 900001;

UPDATE training_materials SET
    sort_order = 2,
    material_type = 'DOC',
    module_code = 'PET_BEHAVIOR',
    min_duration_seconds = 90,
    is_required = 1,
    title = '宠物行为学基础',
    content = '## 宠物行为学基础\n\n### 常见行为识别\n- 摇尾、蹭腿：通常表示亲近\n- 飞机耳、炸毛、低吼：可能处于应激\n- 躲藏、拒食：需降低刺激并观察\n\n### 服务原则\n1. 先观察再接触\n2. 不强迫互动\n3. 异常行为及时记录并反馈宠主'
WHERE material_id = 900002;

UPDATE training_materials SET
    sort_order = 3,
    material_type = 'VIDEO',
    module_code = 'LEASH_SAFETY',
    min_duration_seconds = 90,
    is_required = 1,
    media_url = 'https://pets-backend.oss-cn-beijing.aliyuncs.com/seed/training/leash-safety.mp4',
    title = '牵引具使用规范',
    content = '## 牵引具使用规范\n\n本视频课程讲解：\n- 胸背/项圈选择与佩戴\n- 遛狗路线与避让原则\n- 突发挣脱时的处置步骤\n\n请完整观看视频，系统会记录学习时长。'
WHERE material_id = 900003;

UPDATE training_materials SET
    sort_order = 4,
    material_type = 'DOC',
    module_code = 'HOME_ENTRY',
    min_duration_seconds = 120,
    is_required = 1,
    title = '入户闭环流程',
    content = '## 入户闭环流程\n\n### 到达\n1. 提前联系宠主\n2. 按约定方式进入（门禁/钥匙/密码）\n\n### 服务中\n- 换鞋、消毒、按 SOP 执行\n- 关键节点拍照/视频打卡\n\n### 离场\n- 确认门窗、水电、宠物状态\n- 垃圾整理、凭证上传\n- 向宠主发送完成通知'
WHERE material_id = 900004;

UPDATE training_materials SET
    sort_order = 5,
    material_type = 'DOC',
    module_code = 'EMERGENCY',
    min_duration_seconds = 120,
    is_required = 1,
    title = '突发状况应急预案',
    content = '## 突发状况应急预案\n\n### 应激反应\n- 停止靠近，给宠物安全空间\n- 关闭门窗防止逃逸\n- 联系宠主并上报平台\n\n### 伤人风险\n- 立即停止服务，确保人身安全\n- 必要时联系120/110\n- 保留现场信息并上报\n\n### 误食可疑物\n- 禁止催吐（除非宠主/兽医明确指示）\n- 拍照留存并联系宠主\n- 按宠主指示送医或观察'
WHERE material_id = 900010;
