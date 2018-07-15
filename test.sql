
-- 创建mysql 测试表
create DATABASE  datax;

use datax;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `telephone` varchar(30) DEFAULT NULL,
  `mail` varchar(50) DEFAULT NULL,
  `password` varchar(32) DEFAULT NULL,
  `remark` varchar(1000) DEFAULT NULL,
  `status` int(11) NOT NULL,
  `operator` varchar(50) NOT NULL,
  `operate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `operate_ip` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_username` (`username`) USING BTREE,
  UNIQUE KEY `idx_mail` (`mail`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8;



-- hive 创建外部表
CREATE EXTERNAL TABLE mysql01(
col1 STRING,
col2 STRING,
col3 STRING,
col4 STRING,
col5 STRING,
col6 STRING,
col7 STRING,
col8 STRING,
col9 STRING,
col10 STRING
) row format delimited fields terminated by '\t' stored AS orc
location '/user/hive/warehouse/mysql';
























