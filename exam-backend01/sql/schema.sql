-- hxf_exam 数据库导出
-- 源库: exam  ->  目标库: hxf_exam
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `hxf_exam`
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE `hxf_exam`;

-- ----------------------------
-- answer_record
-- ----------------------------
DROP TABLE IF EXISTS `answer_record`;
CREATE TABLE `answer_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `user_answer` text COMMENT '学生答案',
  `score` int DEFAULT NULL COMMENT '该题得分',
  `is_correct` tinyint DEFAULT NULL COMMENT '是否正确 1对 0错（客观题）',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_exam_student` (`exam_id`,`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='答卷记录表';

-- ----------------------------
-- exam
-- ----------------------------
DROP TABLE IF EXISTS `exam`;
CREATE TABLE `exam` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `paper_id` bigint NOT NULL COMMENT '试卷ID',
  `name` varchar(100) NOT NULL COMMENT '考试名称',
  `start_time` datetime NOT NULL COMMENT '开始时间',
  `end_time` datetime NOT NULL COMMENT '结束时间',
  `duration` int NOT NULL DEFAULT '60' COMMENT '时长(分钟)',
  `pass_score` int NOT NULL DEFAULT '60' COMMENT '及格分',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0未发布 1已发布',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_paper_id` (`paper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考试表';

-- ----------------------------
-- exam_result
-- ----------------------------
DROP TABLE IF EXISTS `exam_result`;
CREATE TABLE `exam_result` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `objective_score` int NOT NULL DEFAULT '0' COMMENT '客观题得分',
  `subjective_score` int NOT NULL DEFAULT '0' COMMENT '主观题得分',
  `total_score` int NOT NULL DEFAULT '0' COMMENT '总分',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0待阅卷 1已完成',
  `submit_time` datetime DEFAULT NULL COMMENT '交卷时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_exam_student` (`exam_id`,`student_id`),
  KEY `idx_exam_id` (`exam_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成绩表';

-- ----------------------------
-- exam_student
-- ----------------------------
DROP TABLE IF EXISTS `exam_student`;
CREATE TABLE `exam_student` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `exam_id` bigint NOT NULL COMMENT '考试ID',
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_exam_id` (`exam_id`),
  KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='考试考生表';

-- ----------------------------
-- paper
-- ----------------------------
DROP TABLE IF EXISTS `paper`;
CREATE TABLE `paper` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) NOT NULL COMMENT '试卷名称',
  `total_score` int NOT NULL DEFAULT '0' COMMENT '总分',
  `duration` int NOT NULL DEFAULT '60' COMMENT '考试时长(分钟)',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='试卷表';

-- ----------------------------
-- paper_question
-- ----------------------------
DROP TABLE IF EXISTS `paper_question`;
CREATE TABLE `paper_question` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `paper_id` bigint NOT NULL COMMENT '试卷ID',
  `question_id` bigint NOT NULL COMMENT '题目ID',
  `seq` int NOT NULL DEFAULT '0' COMMENT '题目顺序',
  `score` int NOT NULL DEFAULT '5' COMMENT '该题分值',
  PRIMARY KEY (`id`),
  KEY `idx_paper_id` (`paper_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='试卷题目关联表';

-- ----------------------------
-- question
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type` tinyint NOT NULL COMMENT '题型 1单选 2多选 3判断 4填空 5简答',
  `category_id` bigint NOT NULL DEFAULT '0' COMMENT '分类ID',
  `content` text NOT NULL COMMENT '题干',
  `options` text COMMENT '选项（JSON，选择题用）',
  `answer` text COMMENT '答案',
  `analysis` text COMMENT '解析',
  `difficulty` tinyint NOT NULL DEFAULT '1' COMMENT '难度 1简单 2中等 3困难',
  `score` int NOT NULL DEFAULT '5' COMMENT '默认分值',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='题目表';

-- ----------------------------
-- question_category
-- ----------------------------
DROP TABLE IF EXISTS `question_category`;
CREATE TABLE `question_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父分类ID，0为顶级',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='题目分类表';

-- ----------------------------
-- sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `role_name` varchar(50) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色表';

INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`) VALUES (1, '管理员', 'ADMIN', '系统管理员');
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`) VALUES (2, '教师', 'TEACHER', '教师');
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`) VALUES (3, '学生', 'STUDENT', '学生');

-- ----------------------------
-- sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) NOT NULL COMMENT '账号',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 1启用 0禁用',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0未删 1已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `status`, `created_at`, `updated_at`, `deleted`) VALUES (1, 'admin', '123456', '系统管理员', 1, '2026-09-11 17:41:11', '2026-09-11 17:41:11', 0);

-- ----------------------------
-- sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户角色关联表';

INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`) VALUES (1, 1, 1);

SET FOREIGN_KEY_CHECKS = 1;
