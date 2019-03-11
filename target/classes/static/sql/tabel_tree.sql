/*
Navicat MySQL Data Transfer

Source Server         : b
Source Server Version : 50560
Source Host           : localhost:3306
Source Database       : jpa

Target Server Type    : MYSQL
Target Server Version : 50560
File Encoding         : 65001

Date: 2018-11-23 09:49:47
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for tabel_tree
-- ----------------------------
DROP TABLE IF EXISTS `tabel_tree`;
CREATE TABLE `tabel_tree` (
  `id` char(32) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `parent_id` int(11) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `icon` varchar(255) DEFAULT NULL,
  `order` int(11) DEFAULT NULL,
  `title` varchar(255) DEFAULT NULL,
  `type` varchar(255) DEFAULT NULL,
  `level` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of tabel_tree
-- ----------------------------
INSERT INTO `tabel_tree` VALUES ('1', '#', '0', '', '', '1', '类', 'folder', '1');
INSERT INTO `tabel_tree` VALUES ('2', '#', '1', '', '', '2', '系统类', 'folder', '2');
INSERT INTO `tabel_tree` VALUES ('3', 'sys_params', '2', null, null, null, '参数类', 'item', '3');
