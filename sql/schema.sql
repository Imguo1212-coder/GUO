
CREATE DATABASE IF NOT EXISTS guo_dept DEFAULT CHARACTER SET utf8mb4;
USE guo_dept;
CREATE TABLE IF NOT EXISTS department (
                                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                          name VARCHAR(100) NOT NULL,
    description VARCHAR(500) NULL,
    create_time DATETIME NULL
    );

CREATE DATABASE IF NOT EXISTS guo_user DEFAULT CHARACTER SET utf8mb4;
USE guo_user;
CREATE TABLE IF NOT EXISTS `user` (
                                      id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                      name VARCHAR(100) NOT NULL,
    age INT NULL,
    email VARCHAR(100) NULL,
    create_time DATETIME NULL,
    department_id BIGINT NULL
    );