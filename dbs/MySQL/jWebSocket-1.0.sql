-- MySQL Administrator dump 1.4
--
-- ------------------------------------------------------
-- Server version	5.1.45-community


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
-- Definition of table `demo_child`
--

DROP TABLE IF EXISTS `demo_child`;
CREATE TABLE `demo_child` (
  `child_id` int(10) unsigned NOT NULL DEFAULT '0',
  `master_id` int(10) unsigned NOT NULL,
  `child_string` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`child_id`),
  KEY `idx_demo_master_child` (`master_id`),
  CONSTRAINT `fk_demo_master_child` FOREIGN KEY (`master_id`) REFERENCES `demo_master` (`master_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `demo_child`
--

/*!40000 ALTER TABLE `demo_child` DISABLE KEYS */;
INSERT INTO `demo_child` (`child_id`,`master_id`,`child_string`) VALUES 
 (1,1,'Child #1 for Master #1'),
 (2,1,'Child #2 for Master #1'),
 (3,1,'Child #3 for Master #1'),
 (4,2,'Child #1 for Master #2'),
 (5,2,'Child #2 for Master #2'),
 (6,2,'Child #3 for Master #2');
/*!40000 ALTER TABLE `demo_child` ENABLE KEYS */;


--
-- Definition of trigger `tr_demo_child_bi`
--

DROP TRIGGER /*!50030 IF EXISTS */ `tr_demo_child_bi`;

DELIMITER $$

CREATE DEFINER = `root`@`localhost` TRIGGER `tr_demo_child_bi` BEFORE INSERT ON `demo_child` FOR EACH ROW BEGIN
  if( ( new.child_id = 0 ) || ( new.child_id is null ) ) then
    set new.child_id = getSequence( 'sq_pk_demo_child' );
  end if;
END $$

DELIMITER ;

--
-- Definition of table `demo_lookup`
--

DROP TABLE IF EXISTS `demo_lookup`;
CREATE TABLE `demo_lookup` (
  `lookup_id` int(10) unsigned NOT NULL DEFAULT '0',
  `lookup_string` varchar(80) DEFAULT NULL,
  PRIMARY KEY (`lookup_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `demo_lookup`
--

/*!40000 ALTER TABLE `demo_lookup` DISABLE KEYS */;
INSERT INTO `demo_lookup` (`lookup_id`,`lookup_string`) VALUES 
 (1,'Lookup Value #1'),
 (2,'Lookup Value #2');
/*!40000 ALTER TABLE `demo_lookup` ENABLE KEYS */;


--
-- Definition of trigger `tr_demo_lookup_bi`
--

DROP TRIGGER /*!50030 IF EXISTS */ `tr_demo_lookup_bi`;

DELIMITER $$

CREATE DEFINER = `root`@`localhost` TRIGGER `tr_demo_lookup_bi` BEFORE INSERT ON `demo_lookup` FOR EACH ROW BEGIN
  if( ( new.lookup_id = 0 ) || ( new.lookup_id is null ) ) then
    set new.lookup_id = getSequence( 'sq_pk_demo_lookup' );
  end if;
END $$

DELIMITER ;

--
-- Definition of table `demo_master`
--

DROP TABLE IF EXISTS `demo_master`;
CREATE TABLE `demo_master` (
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
-- Dumping data for table `demo_master`
--

/*!40000 ALTER TABLE `demo_master` DISABLE KEYS */;
INSERT INTO `demo_master` (`master_id`,`lookup_id`,`master_int`,`master_float`,`master_string`,`master_clob`) VALUES 
 (1,1,1,1,'Master Row #1','Arbitrary Text Row #1'),
 (2,2,2,2,'Master Row #2','Arbitrary Text Row #2'),
 (3,1,3,3,'Master Row #3','Arbitrary Text Row #3'),
 (4,2,4,4,'Master Row #4','Arbitrary Text Row #4'),
 (5,1,5,5,'Master Row #5','Arbitrary Text Row #5');
/*!40000 ALTER TABLE `demo_master` ENABLE KEYS */;


--
-- Definition of trigger `tr_demo_master_bi`
--

DROP TRIGGER /*!50030 IF EXISTS */ `tr_demo_master_bi`;

DELIMITER $$

CREATE DEFINER = `root`@`localhost` TRIGGER `tr_demo_master_bi` BEFORE INSERT ON `demo_master` FOR EACH ROW BEGIN
  if( ( new.master_id = 0 ) || ( new.master_id is null ) ) then
    set new.master_id = getSequence( 'sq_pk_demo_master' );
  end if;
END $$

DELIMITER ;

--
-- Definition of table `sequences`
--

DROP TABLE IF EXISTS `sequences`;
CREATE TABLE `sequences` (
  `seq_id` varchar(40) NOT NULL,
  `next_val` int(10) unsigned DEFAULT '1',
  PRIMARY KEY (`seq_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Dumping data for table `sequences`
--

/*!40000 ALTER TABLE `sequences` DISABLE KEYS */;
INSERT INTO `sequences` (`seq_id`,`next_val`) VALUES 
 ('sq_pk_demo_child',7),
 ('sq_pk_demo_lookup',3),
 ('sq_pk_demo_master',6);
/*!40000 ALTER TABLE `sequences` ENABLE KEYS */;


--
-- Definition of function `getSequence`
--

DROP FUNCTION IF EXISTS `getSequence`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */ $$
CREATE DEFINER=`root`@`localhost` FUNCTION `getSequence`( aId VARCHAR(40) ) RETURNS int(11)
BEGIN
  DECLARE lNextVal INT;
  select next_val into lNextVal from sequences where seq_id = aId;
  update sequences set next_val = next_val + 1 where seq_id = aId;
  return lNextVal;
END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;

--
-- Definition of procedure `initSequences`
--

DROP PROCEDURE IF EXISTS `initSequences`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ $$
CREATE DEFINER=`root`@`localhost` PROCEDURE `initSequences`()
BEGIN
  DECLARE lMaxId INT;

  select max( master_id ) into lMaxId from demo_master;
  call setSequence( 'sq_pk_demo_master', lMaxId + 1 );

  select max( child_id ) into lMaxId from demo_child;
  call setSequence( 'sq_pk_demo_child', lMaxId + 1 );

  select max( lookup_id ) into lMaxId from demo_lookup;
  call setSequence( 'sq_pk_demo_lookup', lMaxId + 1 );

END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;

--
-- Definition of procedure `setSequence`
--

DROP PROCEDURE IF EXISTS `setSequence`;

DELIMITER $$

/*!50003 SET @TEMP_SQL_MODE=@@SQL_MODE, SQL_MODE='STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER' */ $$
CREATE DEFINER=`root`@`localhost` PROCEDURE `setSequence`( aId VARCHAR(40), aNextVal INT )
BEGIN

  DECLARE lCnt INT;
  select count( * ) into lCnt from sequences where seq_id = aId;

  if( aNextVaL is null ) then
    set aNextVal = 1;
  end if;

  if( lCnt = 0 ) then
    insert into sequences ( seq_id, next_val ) values ( aId, aNextVal );
  else
    update sequences set next_val = aNextVal where seq_id = aId;
  end if;
END $$
/*!50003 SET SESSION SQL_MODE=@TEMP_SQL_MODE */  $$

DELIMITER ;



/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
