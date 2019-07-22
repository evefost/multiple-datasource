
drop  database if exists ds0;
create  database ds0;
drop  database if exists ds0_1;
create  database ds0_1;
drop  database if exists ds0_2;
create  database ds0_2;


drop  database if exists ds1;
create  database ds1;
drop  database if exists ds1_1;
create  database ds1_1;

drop  database if exists ds2;
create  database ds2;



DROP TABLE IF EXISTS ds0.`user`;
CREATE TABLE ds0.`user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
DROP TABLE IF EXISTS ds0_1.`user`;
CREATE TABLE ds0_1.`user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
DROP TABLE IF EXISTS ds0_2.`user`;
CREATE TABLE ds0_2.`user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
DROP TABLE IF EXISTS ds1.`user`;
CREATE TABLE ds1.`user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
DROP TABLE IF EXISTS ds1_1.`user`;
CREATE TABLE ds1_1.`user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;

-- ----------------------------
DROP TABLE IF EXISTS ds2.`user`;
CREATE TABLE ds2.`user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) DEFAULT NULL,
  `age` int(11) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;


-- ----------------------------
--  Records of `user`
-- ----------------------------
BEGIN;
INSERT INTO ds0.`user`
VALUES
('1', '老王ds0', '22', '2019-07-17 20:25:19'),
('2', '老王ds0', '22', '2019-07-17 20:32:26'),
('3', '老王ds0', '22', '2019-07-17 20:41:23'),
('4', '老王ds0', '22', '2019-07-17 22:26:42'),
('5', '老王ds0', '22', '2019-07-17 22:26:44'),
('6', '老王ds0', '22', '2019-07-17 22:26:45'),
('7', '老王ds0', '22', '2019-07-17 22:26:45'),
('8', '老王ds0', '22', '2019-07-17 22:26:45'),
('9', '老王ds0', '22', '2019-07-17 22:26:46');
COMMIT;
-- ----------------------------
--  Records of `user`
-- ----------------------------
BEGIN;
INSERT INTO ds0_1.`user`
VALUES
('1', '老王ds0', '22', '2019-07-17 20:25:19'),
('2', '老王ds0', '22', '2019-07-17 20:32:26'),
('3', '老王ds0', '22', '2019-07-17 20:41:23'),
('4', '老王ds0', '22', '2019-07-17 22:26:42'),
('5', '老王ds0', '22', '2019-07-17 22:26:44'),
('6', '老王ds0', '22', '2019-07-17 22:26:45'),
('7', '老王ds0', '22', '2019-07-17 22:26:45'),
('8', '老王ds0', '22', '2019-07-17 22:26:45'),
('9', '老王ds0', '22', '2019-07-17 22:26:46');
COMMIT;


-- ----------------------------
--  Records of `user`
-- ----------------------------
BEGIN;
INSERT INTO ds0_2.`user`
VALUES
('1', '老王ds0', '22', '2019-07-17 20:25:19'),
('2', '老王ds0', '22', '2019-07-17 20:32:26'),
('3', '老王ds0', '22', '2019-07-17 20:41:23'),
('4', '老王ds0', '22', '2019-07-17 22:26:42'),
('5', '老王ds0', '22', '2019-07-17 22:26:44'),
('6', '老王ds0', '22', '2019-07-17 22:26:45'),
('7', '老王ds0', '22', '2019-07-17 22:26:45'),
('8', '老王ds0', '22', '2019-07-17 22:26:45'),
('9', '老王ds0', '22', '2019-07-17 22:26:46');
COMMIT;

-- ----------------------------
--  Records of `user`
-- ----------------------------
BEGIN;
INSERT INTO ds1.`user`
VALUES
('1', '老王ds1', '22', '2019-07-17 20:25:19'),
('2', '老王ds1', '22', '2019-07-17 20:32:26'),
('3', '老王ds1', '22', '2019-07-17 20:41:23'),
('4', '老王ds1', '22', '2019-07-17 22:26:42'),
('5', '老王ds1', '22', '2019-07-17 22:26:44'),
('6', '老王ds1', '22', '2019-07-17 22:26:45'),
('7', '老王ds1', '22', '2019-07-17 22:26:45'),
('8', '老王ds1', '22', '2019-07-17 22:26:45'),
('9', '老王ds1', '22', '2019-07-17 22:26:46');
COMMIT;

-- ----------------------------
--  Records of `user`
-- ----------------------------
BEGIN;
INSERT INTO ds1_1.`user`
VALUES
('1', '老王ds1', '22', '2019-07-17 20:25:19'),
('2', '老王ds1', '22', '2019-07-17 20:32:26'),
('3', '老王ds1', '22', '2019-07-17 20:41:23'),
('4', '老王ds1', '22', '2019-07-17 22:26:42'),
('5', '老王ds1', '22', '2019-07-17 22:26:44'),
('6', '老王ds1', '22', '2019-07-17 22:26:45'),
('7', '老王ds1', '22', '2019-07-17 22:26:45'),
('8', '老王ds1', '22', '2019-07-17 22:26:45'),
('9', '老王ds1', '22', '2019-07-17 22:26:46');
COMMIT;

BEGIN;
INSERT INTO ds2.`user`
VALUES
('1', '老王ds2', '22', '2019-07-17 20:25:19'),
('2', '老王ds2', '22', '2019-07-17 20:32:26'),
('3', '老王ds2', '22', '2019-07-17 20:41:23'),
('4', '老王ds2', '22', '2019-07-17 22:26:42'),
('5', '老王ds2', '22', '2019-07-17 22:26:44'),
('6', '老王ds2', '22', '2019-07-17 22:26:45'),
('7', '老王ds2', '22', '2019-07-17 22:26:45'),
('8', '老王ds2', '22', '2019-07-17 22:26:45'),
('9', '老王ds2', '22', '2019-07-17 22:26:46');
COMMIT;

