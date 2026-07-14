USE guo_dept;

DELETE FROM department;

INSERT INTO department (id, name, description, create_time) VALUES
(1, '技术部', '负责产品研发与技术支持', '2026-07-08 09:00:00'),
(2, '市场部', '负责市场推广与销售', '2026-07-08 09:00:00'),
(3, '人事部', '负责招聘与员工管理', '2026-07-08 09:00:00');

ALTER TABLE department AUTO_INCREMENT = 4;

USE guo_user;

DELETE FROM user;

INSERT INTO user (id, name, age, email, create_time, department_id) VALUES
(1, '张三', 28, 'zhangsan@test.com', '2026-07-08 10:00:00', 1),
(2, '李四', 25, 'lisi@test.com',     '2026-07-08 10:05:00', 2),
(3, '王五', 30, 'wangwu@test.com',   '2026-07-08 10:10:00', 3),
(4, '赵六', 22, 'zhaoliu@test.com',  '2026-07-08 10:15:00', 1);

ALTER TABLE user AUTO_INCREMENT = 5;

SELECT 'department' AS table_name;
SELECT * FROM guo_dept.department ORDER BY id;

SELECT 'user' AS table_name;
SELECT * FROM guo_user.user ORDER BY id;
