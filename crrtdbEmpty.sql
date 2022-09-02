-- --------------------------------------------------------
-- Хост:                         127.0.0.1
-- Версия сервера:               5.6.43 - MySQL Community Server (GPL)
-- Операционная система:         Win32
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


-- Дамп структуры базы данных crrt
CREATE DATABASE IF NOT EXISTS `crrt` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `crrt`;

-- Дамп структуры для функция crrt.getActiveRepairsByInvoiceId
DELIMITER //
CREATE DEFINER=`root`@`%` FUNCTION `getActiveRepairsByInvoiceId`(
	`inv_id` INT
) RETURNS int(11)
BEGIN
	DECLARE res INT;
	SELECT COUNT(id) INTO res 
	FROM tbl_repair_invoices WHERE expiration_date >= CURDATE() AND is_paid = 0 AND invoice_id = inv_id;
	RETURN res;
END//
DELIMITER ;

-- Дамп структуры для процедура crrt.GetBasicInvoiceByCarID
DELIMITER //
CREATE DEFINER=`root`@`%` PROCEDURE `GetBasicInvoiceByCarID`(
	IN `c_id` INT

)
BEGIN
	SELECT tbl_invoices.id AS invoice_id, `code`, date_start, date_end, is_rejected, is_canceled, is_paid, tbl_cars.*, tbl_users.email FROM tbl_invoices 
	JOIN tbl_cars ON tbl_invoices.car_id = tbl_cars.id
	JOIN tbl_users ON tbl_users.id = tbl_invoices.client_id
	WHERE car_id=c_id;
END//
DELIMITER ;

-- Дамп структуры для функция crrt.getExpiredRepairsByInvoiceId
DELIMITER //
CREATE DEFINER=`root`@`%` FUNCTION `getExpiredRepairsByInvoiceId`(
	`inv_id` INT

) RETURNS int(11)
BEGIN
	DECLARE res INT;
	SELECT COUNT(id) INTO res 
	FROM tbl_repair_invoices WHERE expiration_date < CURDATE() AND is_paid = 0 AND invoice_id = inv_id;
	RETURN res;
END//
DELIMITER ;

-- Дамп структуры для функция crrt.getLastInvoiceCity
DELIMITER //
CREATE DEFINER=`root`@`%` FUNCTION `getLastInvoiceCity`(
	`clientId` INT
) RETURNS tinyint(4)
BEGIN
	DECLARE cityId TINYINT;
	SELECT tbl_cars.city_id INTO cityId FROM tbl_invoices
	JOIN tbl_cars ON tbl_invoices.car_id = tbl_cars.id
	WHERE tbl_invoices.client_id = clientId
	ORDER BY tbl_invoices.ts_created
	LIMIT 1;
	RETURN cityId;
END//
DELIMITER ;

-- Дамп структуры для процедура crrt.GetShortRepairsInfoByInvoiceId
DELIMITER //
CREATE DEFINER=`root`@`%` PROCEDURE `GetShortRepairsInfoByInvoiceId`(
	IN `inv_id` INT
)
BEGIN
	SELECT 
		(SELECT COUNT(id) FROM tbl_repair_invoices WHERE expiration_date <= CURDATE() AND is_paid = 0 AND invoice_id = inv_id) AS expiredRepairs,
		(SELECT COUNT(id) FROM tbl_repair_invoices WHERE expiration_date >= CURDATE() AND is_paid = 0 AND invoice_id = inv_id) AS activeRepairs;
END//
DELIMITER ;

-- Дамп структуры для функция crrt.glueCarPhotos
DELIMITER //
CREATE DEFINER=`root`@`%` FUNCTION `glueCarPhotos`(c_id INT) RETURNS text CHARSET utf8
BEGIN 
  DECLARE gluedData TEXT;
  SELECT GROUP_CONCAT(CONCAT(id, '#', photo) SEPARATOR '?') INTO gluedData
  FROM tbl_cars_photos
  WHERE car_id = c_id GROUP BY car_id;
  RETURN gluedData;
END//
DELIMITER ;

-- Дамп структуры для таблица crrt.tbl_cars
CREATE TABLE IF NOT EXISTS `tbl_cars` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `brand` varchar(100) NOT NULL,
  `model` varchar(100) NOT NULL,
  `segment_id` tinyint(4) NOT NULL,
  `price` double NOT NULL,
  `city_id` tinyint(4) NOT NULL,
  `ts_edited` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8;

-- Экспортируемые данные не выделены.
-- Дамп структуры для таблица crrt.tbl_cars_photos
CREATE TABLE IF NOT EXISTS `tbl_cars_photos` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `car_id` int(11) NOT NULL,
  `photo` char(255) NOT NULL,
  `ts_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_tbl_cars_photos_tbl_cars` (`car_id`),
  CONSTRAINT `FK_tbl_cars_photos_tbl_cars` FOREIGN KEY (`car_id`) REFERENCES `tbl_cars` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8;

-- Экспортируемые данные не выделены.
-- Дамп структуры для таблица crrt.tbl_drivers
CREATE TABLE IF NOT EXISTS `tbl_drivers` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `city_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `FK_tbl_drivers_tbl_users` FOREIGN KEY (`user_id`) REFERENCES `tbl_users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;

-- Экспортируемые данные не выделены.
-- Дамп структуры для таблица crrt.tbl_invoices
CREATE TABLE IF NOT EXISTS `tbl_invoices` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` char(12) NOT NULL,
  `car_id` int(11) NOT NULL,
  `driver_id` int(11) DEFAULT NULL,
  `client_id` int(11) NOT NULL,
  `exp_price` int(11) NOT NULL,
  `date_start` date NOT NULL,
  `date_end` date NOT NULL,
  `is_canceled` tinyint(4) NOT NULL DEFAULT '0',
  `is_rejected` tinyint(4) NOT NULL DEFAULT '0',
  `reject_reason` varchar(50) DEFAULT NULL,
  `passport_id` int(11) NOT NULL,
  `ts_created` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`),
  KEY `FK_tbl_invoices_tbl_users` (`client_id`),
  KEY `FK_tbl_invoices_tbl_cars` (`car_id`),
  KEY `FK_tbl_invoices_tbl_drivers` (`driver_id`),
  KEY `FK_tbl_invoices_tbl_passport` (`passport_id`),
  FULLTEXT KEY `CODE_FT` (`code`),
  CONSTRAINT `FK_tbl_invoices_tbl_cars` FOREIGN KEY (`car_id`) REFERENCES `tbl_cars` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `FK_tbl_invoices_tbl_drivers` FOREIGN KEY (`driver_id`) REFERENCES `tbl_drivers` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `FK_tbl_invoices_tbl_passport` FOREIGN KEY (`passport_id`) REFERENCES `tbl_passport` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `FK_tbl_invoices_tbl_users` FOREIGN KEY (`client_id`) REFERENCES `tbl_users` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8;

-- Экспортируемые данные не выделены.
-- Дамп структуры для таблица crrt.tbl_logs
CREATE TABLE IF NOT EXISTS `tbl_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `class` varchar(100) DEFAULT NULL,
  `method` varchar(100) DEFAULT NULL,
  `level` varchar(100) DEFAULT NULL,
  `message` text,
  `ts_created` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8;

-- Экспортируемые данные не выделены.
-- Дамп структуры для таблица crrt.tbl_passport
CREATE TABLE IF NOT EXISTS `tbl_passport` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `firstname` varchar(100) DEFAULT NULL,
  `surname` varchar(100) DEFAULT NULL,
  `patronymic` varchar(100) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `date_of_issue` date DEFAULT NULL,
  `doc_number` bigint(20) DEFAULT NULL,
  `rntrc` bigint(20) DEFAULT NULL,
  `authority` int(11) DEFAULT NULL,
  `ts_created` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8;

-- Экспортируемые данные не выделены.
-- Дамп структуры для таблица crrt.tbl_repair_invoices
CREATE TABLE IF NOT EXISTS `tbl_repair_invoices` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `invoice_id` int(11) NOT NULL,
  `price` double NOT NULL,
  `expiration_date` date NOT NULL,
  `comment` varchar(1000) DEFAULT NULL,
  `is_paid` tinyint(1) DEFAULT '0',
  `ts_created` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `ts_edited` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_tbl_repair_invoices_tbl_invoices` (`invoice_id`),
  CONSTRAINT `FK_tbl_repair_invoices_tbl_invoices` FOREIGN KEY (`invoice_id`) REFERENCES `tbl_invoices` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8;

-- Экспортируемые данные не выделены.
-- Дамп структуры для таблица crrt.tbl_users
CREATE TABLE IF NOT EXISTS `tbl_users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `firstname` varchar(255) DEFAULT NULL,
  `surname` varchar(255) DEFAULT NULL,
  `patronymic` varchar(255) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `balance` float NOT NULL DEFAULT '0',
  `role_id` tinyint(4) NOT NULL DEFAULT '1',
  `is_blocked` tinyint(4) DEFAULT '2',
  `conf_code` tinytext,
  `ts_created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8;

-- Экспортируемые данные не выделены.
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
