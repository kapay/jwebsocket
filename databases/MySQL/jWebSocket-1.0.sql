-- MySQL Administrator dump 1.4
--
-- ------------------------------------------------------
-- Server version	5.1.44


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


--
-- Create schema jwebsocket
--

CREATE DATABASE IF NOT EXISTS jwebsocket;
USE jwebsocket;

--
-- Definition of table `jwebsocket`.`demo_child`
--

DROP TABLE IF EXISTS `jwebsocket`.`demo_child`;
CREATE TABLE  `jwebsocket`.`demo_child` (
  `child_id` int(10) unsigned NOT NULL DEFAULT '0',
  `master_id` int(10) unsigned NOT NULL,
  `child_string` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`child_id`),
  KEY `idx_demo_master_child` (`master_id`),
  CONSTRAINT `fk_demo_master_child` FOREIGN KEY (`master_id`) REFERENCES `demo_master` (`master_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `jwebsocket`.`demo_child`
--

/*!40000 ALTER TABLE `demo_child` DISABLE KEYS */;
LOCK TABLES `demo_child` WRITE;
INSERT INTO `jwebsocket`.`demo_child` VALUES  (1,1,'Child #1 for Master #1'),
 (2,1,'Child #2 for Master #1'),
 (3,1,'Child #3 for Master #1'),
 (4,2,'Child #1 for Master #2'),
 (5,2,'Child #2 for Master #2'),
 (6,2,'Child #3 for Master #2');
UNLOCK TABLES;
/*!40000 ALTER TABLE `demo_child` ENABLE KEYS */;


--
-- Definition of trigger `jwebsocket`.`tr_demo_child_bi`
--

DROP TRIGGER /*!50030 IF EXISTS */ `jwebsocket`.`tr_demo_child_bi`;

DELIMITER $$

CREATE DEFINER = `root`@`localhost` TRIGGER  `jwebsocket`.`tr_demo_child_bi` BEFORE INSERT ON `demo_child` FOR EACH ROW BEGIN
  if( ( new.child_id = 0 ) || ( new.child_id is null ) ) then
    set new.child_id = getSequence( 'sq_pk_demo_child' );
  end if;
END $$

DELIMITER ;

--
-- Definition of table `jwebsocket`.`demo_lookup`
--

DROP TABLE IF EXISTS `jwebsocket`.`demo_lookup`;
CREATE TABLE  `jwebsocket`.`demo_lookup` (
  `lookup_id` int(10) unsigned NOT NULL DEFAULT '0',
  `lookup_string` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`lookup_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `jwebsocket`.`demo_lookup`
--

/*!40000 ALTER TABLE `demo_lookup` DISABLE KEYS */;
LOCK TABLES `demo_lookup` WRITE;
INSERT INTO `jwebsocket`.`demo_lookup` VALUES  (1,'Lookup Value #1'),
 (2,'Lookup Value #2');
UNLOCK TABLES;
/*!40000 ALTER TABLE `demo_lookup` ENABLE KEYS */;


--
-- Definition of trigger `jwebsocket`.`tr_demo_lookup_bi`
--

DROP TRIGGER /*!50030 IF EXISTS */ `jwebsocket`.`tr_demo_lookup_bi`;

DELIMITER $$

CREATE DEFINER = `root`@`localhost` TRIGGER  `jwebsocket`.`tr_demo_lookup_bi` BEFORE INSERT ON `demo_lookup` FOR EACH ROW BEGIN
  if( ( new.lookup_id = 0 ) || ( new.lookup_id is null ) ) then
    set new.lookup_id = getSequence( 'sq_pk_demo_lookup' );
  end if;
END $$

DELIMITER ;

--
-- Definition of table `jwebsocket`.`demo_master`
--

DROP TABLE IF EXISTS `jwebsocket`.`demo_master`;
CREATE TABLE  `jwebsocket`.`demo_master` (
  `master_id` int(10) unsigned NOT NULL DEFAULT '0',
  `lookup_id` int(10) unsigned DEFAULT NULL,
  `master_int` int(10) unsigned DEFAULT NULL,
  `master_float` float DEFAULT NULL,
  `master_string` varchar(80) DEFAULT NULL,
  `master_clob` text,
  PRIMARY KEY (`master_id`),
  KEY `idx_demo_master_lookup` (`lookup_id`),
  CONSTRAINT `fk_demo_master_lookup` FOREIGN KEY (`lookup_id`) REFERENCES `demo_lookup` (`lookup_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `jwebsocket`.`demo_master`
--

/*!40000 ALTER TABLE `demo_master` DISABLE KEYS */;
LOCK TABLES `demo_master` WRITE;
INSERT INTO `jwebsocket`.`demo_master` VALUES  (1,1,1,1,'Master Row #1','Arbitrary Text Row #1'),
 (2,2,2,2,'Master Row #2','Arbitrary Text Row #2'),
 (3,1,3,3,'Master Row #3','Arbitrary Text Row #3'),
 (4,2,4,4,'Master Row #4','Arbitrary Text Row #4'),
 (5,1,5,5,'Master Row #5','Arbitrary Text Row #5');
UNLOCK TABLES;
/*!40000 ALTER TABLE `demo_master` ENABLE KEYS */;


--
-- Definition of trigger `jwebsocket`.`tr_demo_master_bi`
--

DROP TRIGGER /*!50030 IF EXISTS */ `jwebsocket`.`tr_demo_master_bi`;

DELIMITER $$

CREATE DEFINER = `root`@`localhost` TRIGGER  `jwebsocket`.`tr_demo_master_bi` BEFORE INSERT ON `demo_master` FOR EACH ROW BEGIN
  if( ( new.master_id = 0 ) || ( new.master_id is null ) ) then
    set new.master_id = getSequence( 'sq_pk_demo_master' );
  end if;
END $$

DELIMITER ;

--
-- Definition of table `jwebsocket`.`sequences`
--

DROP TABLE IF EXISTS `jwebsocket`.`sequences`;
CREATE TABLE  `jwebsocket`.`sequences` (
  `seq_id` varchar(40) NOT NULL,
  `next_val` int(10) unsigned DEFAULT '1',
  PRIMARY KEY (`seq_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `jwebsocket`.`sequences`
--

/*!40000 ALTER TABLE `sequences` DISABLE KEYS */;
LOCK TABLES `sequences` WRITE;
INSERT INTO `jwebsocket`.`sequences` VALUES  ('sq_pk_demo_child',7),
 ('sq_pk_demo_lookup',3),
 ('sq_pk_demo_master',6),
 ('sq_pk_system_log',29);
UNLOCK TABLES;
/*!40000 ALTER TABLE `sequences` ENABLE KEYS */;


--
-- Definition of table `jwebsocket`.`system_log`
--

DROP TABLE IF EXISTS `jwebsocket`.`system_log`;
CREATE TABLE  `jwebsocket`.`system_log` (
  `id` int(11) NOT NULL DEFAULT '0',
  `time_stamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `event_type` varchar(20) DEFAULT NULL,
  `customer` varchar(25) DEFAULT NULL,
  `app_name` varchar(25) DEFAULT NULL,
  `app_version` varchar(20) DEFAULT NULL,
  `app_module` varchar(25) DEFAULT NULL,
  `app_dialog` varchar(25) DEFAULT NULL,
  `ip` varchar(45) DEFAULT NULL,
  `user_name` varchar(40) DEFAULT NULL,
  `browser` varchar(20) DEFAULT NULL,
  `browser_version` varchar(15) DEFAULT NULL,
  `data_size` int(11) DEFAULT NULL,
  `process_time` int(11) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `message` varchar(132) DEFAULT NULL,
  `json` text,
  `ws_version` varchar(25) DEFAULT NULL,
  `session_id` varchar(32) DEFAULT NULL,
  `name_space` varchar(50) DEFAULT NULL,
  `token_type` varchar(30) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `jwebsocket`.`system_log`
--

/*!40000 ALTER TABLE `system_log` DISABLE KEYS */;
LOCK TABLES `system_log` WRITE;
INSERT INTO `jwebsocket`.`system_log` VALUES  (1,'2011-05-21 13:18:26',NULL,NULL,NULL,NULL,NULL,NULL,'0:0:0:0:0:0:0:1%0',NULL,'Mein Browser',NULL,NULL,NULL,NULL,'This is an message from the automated test suite.',NULL,NULL,NULL,NULL,NULL),
 (5,'2011-05-21 13:37:28','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','n/a','n/a',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,NULL,NULL,NULL,NULL),
 (6,'2011-05-21 14:04:49','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Chrome','11.0.696.68',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,NULL,NULL,NULL,NULL),
 (7,'2011-05-21 14:12:33','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Chrome','11.0.696.68',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,NULL,NULL,NULL,NULL),
 (8,'2011-05-21 14:13:32','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Opera','9.80',49,NULL,NULL,'This is an message from the automated test suite.',NULL,NULL,NULL,NULL,NULL),
 (9,'2011-05-21 14:14:34','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Firefox','4.0.1',49,NULL,NULL,'This is an message from the automated test suite.',NULL,NULL,NULL,NULL,NULL),
 (10,'2011-05-21 14:15:01','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Firefox','4.0.1',49,NULL,NULL,'This is an message from the automated test suite.',NULL,NULL,NULL,NULL,NULL),
 (11,'2011-05-21 14:16:04','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Safari','5.0.5.533.21.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,NULL,NULL,NULL,NULL),
 (12,'2011-05-21 14:16:20','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Safari','5.0.5.533.21.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,NULL,NULL,NULL,NULL),
 (15,'2011-05-21 14:30:18','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Safari','5.0.5.533.21.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,'native',NULL,NULL,NULL),
 (17,'2011-05-21 14:34:46','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Safari','5.0.5.533.21.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,'native',NULL,NULL,NULL),
 (18,'2011-05-21 14:35:14','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Firefox','4.0.1',49,NULL,NULL,'This is an message from the automated test suite.',NULL,'flash 10.1.102',NULL,NULL,NULL),
 (19,'2011-05-21 14:41:06','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Chrome','11.0.696.68',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,'native',NULL,NULL,NULL),
 (20,'2011-05-21 14:41:33','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Firefox','4.0.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,'flash 10.1.102',NULL,NULL,NULL),
 (21,'2011-05-21 14:46:56','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Safari','5.0.5.533.21.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,'native',NULL,NULL,NULL),
 (22,'2011-05-21 14:47:19','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Opera','9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.',NULL,'native',NULL,NULL,NULL),
 (23,'2011-05-21 14:49:51','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','0:0:0:0:0:0:0:1%0','root','Safari','5.0.5.533.21.1',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_7; en-us) AppleWebKit/533.21.1 (KHTML, like Gecko) Version/5.0.5 Safari/533.21.1\"}','native',NULL,NULL,NULL),
 (24,'2011-05-21 14:50:25','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Opera','9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Macintosh; Intel Mac OS X 10.6.7; U; en) Presto/2.8.131 Version/11.10\"}','native',NULL,NULL,NULL),
 (25,'2011-05-21 14:58:35','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Opera','n/11.10/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Macintosh; Intel Mac OS X 10.6.7; U; en) Presto/2.8.131 Version/11.10\"}','native',NULL,NULL,NULL),
 (26,'2011-05-21 14:59:35','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Opera','11.10/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Macintosh; Intel Mac OS X 10.6.7; U; en) Presto/2.8.131 Version/11.10\"}','native',NULL,NULL,NULL),
 (27,'2011-05-21 15:01:05','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Opera','11.10/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Macintosh; Intel Mac OS X 10.6.7; U; en) Presto/2.8.131 Version/11.10\"}','native',NULL,NULL,NULL),
 (28,'2011-05-21 15:01:08','loggingTest','jWebSocket.org','jWebSocket','1.0a10 (10519)','test automation','full tests','127.0.0.1','root','Opera','11.10/9.80',49,NULL,'ws://localhost:8787/jWebSocket/jWebSocket','This is an message from the automated test suite.','{\"userAgent\":\"Opera/9.80 (Macintosh; Intel Mac OS X 10.6.7; U; en) Presto/2.8.131 Version/11.10\"}','native',NULL,NULL,NULL);
UNLOCK TABLES;
/*!40000 ALTER TABLE `system_log` ENABLE KEYS */;


--
-- Definition of trigger `jwebsocket`.`tr_system_log_bi`
--

DROP TRIGGER /*!50030 IF EXISTS */ `jwebsocket`.`tr_system_log_bi`;

DELIMITER $$

CREATE DEFINER = `root`@`localhost` TRIGGER  `jwebsocket`.`tr_system_log_bi` BEFORE INSERT ON `system_log` FOR EACH ROW BEGIN
  if( ( new.id = 0 ) || ( new.id is null ) ) then
    set new.id = getSequence( 'sq_pk_system_log' );
  end if;
END $$

DELIMITER ;



/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
