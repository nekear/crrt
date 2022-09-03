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
READS SQL DATA
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
READS SQL DATA
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

)
RETURNS int(11)
READS SQL DATA
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
)
RETURNS tinyint(4)
READS SQL DATA
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
READS SQL DATA
BEGIN
	SELECT 
		(SELECT COUNT(id) FROM tbl_repair_invoices WHERE expiration_date <= CURDATE() AND is_paid = 0 AND invoice_id = inv_id) AS expiredRepairs,
		(SELECT COUNT(id) FROM tbl_repair_invoices WHERE expiration_date >= CURDATE() AND is_paid = 0 AND invoice_id = inv_id) AS activeRepairs;
END//
DELIMITER ;

-- Дамп структуры для функция crrt.glueCarPhotos
DELIMITER //
CREATE DEFINER=`root`@`%` FUNCTION `glueCarPhotos`(c_id INT) RETURNS text CHARSET utf8
READS SQL DATA
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

-- Дамп данных таблицы crrt.tbl_cars: ~7 rows (приблизительно)
/*!40000 ALTER TABLE `tbl_cars` DISABLE KEYS */;
INSERT INTO `tbl_cars` (`id`, `brand`, `model`, `segment_id`, `price`, `city_id`, `ts_edited`) VALUES
	(18, 'Ford', 'Mondeo', 1, 32, 1, '2022-08-31 13:19:05'),
	(19, 'Toyota', 'Camry', 1, 32, 2, '2022-08-31 15:00:40'),
	(20, 'Porsche', 'Panamera', 2, 80, 1, '2022-08-31 15:11:31'),
	(21, 'Bugatti', 'Chiron', 3, 320, 1, '2022-08-31 15:15:44'),
	(22, 'Pagani', 'Huayra', 3, 262, 1, '2022-08-31 15:29:19'),
	(23, 'Ferrari', 'LaFerrari', 3, 200, 2, '2022-08-31 15:33:33'),
	(24, 'Porsche', '918 Spyder', 3, 160, 1, '2022-08-31 15:38:10');
/*!40000 ALTER TABLE `tbl_cars` ENABLE KEYS */;

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

-- Дамп данных таблицы crrt.tbl_cars_photos: ~15 rows (приблизительно)
/*!40000 ALTER TABLE `tbl_cars_photos` DISABLE KEYS */;
INSERT INTO `tbl_cars_photos` (`id`, `car_id`, `photo`, `ts_created`) VALUES
	(16, 18, '166194524210848777qygdwo.jpg', '2022-08-31 14:27:22'),
	(17, 18, '166194524480521499gmhisf.jpg', '2022-08-31 14:27:24'),
	(18, 18, '166194524752187889cbaqwm.jpg', '2022-08-31 14:27:27'),
	(19, 19, '166194724006982693jakavf.jpg', '2022-08-31 15:00:40'),
	(20, 19, '166194724009370805ovlcpv.jpg', '2022-08-31 15:00:40'),
	(21, 19, '166194724010436494hlhlqz.jpg', '2022-08-31 15:00:40'),
	(22, 20, '166194792554918268spolec.png', '2022-08-31 15:12:05'),
	(23, 20, '166194792820958358lxcyfk.png', '2022-08-31 15:12:08'),
	(24, 21, '166194816876968990vwgbxf.jpg', '2022-08-31 15:16:08'),
	(25, 21, '166194817135333942asqaoe.jpg', '2022-08-31 15:16:11'),
	(26, 23, '166194923118279417vgkjhk.jpg', '2022-08-31 15:33:51'),
	(27, 23, '166194923389377823madtde.jpg', '2022-08-31 15:33:53'),
	(28, 24, '166194947321955064nzlhxa.jpg', '2022-08-31 15:37:53'),
	(29, 24, '166194947322244068dbqyyo.jpg', '2022-08-31 15:37:53'),
	(30, 24, '166194947375491046iuwpgu.jpg', '2022-08-31 15:37:53');
/*!40000 ALTER TABLE `tbl_cars_photos` ENABLE KEYS */;

-- Дамп структуры для таблица crrt.tbl_drivers
CREATE TABLE IF NOT EXISTS `tbl_drivers` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `city_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `FK_tbl_drivers_tbl_users` FOREIGN KEY (`user_id`) REFERENCES `tbl_users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8;

-- Дамп данных таблицы crrt.tbl_drivers: ~2 rows (приблизительно)
/*!40000 ALTER TABLE `tbl_drivers` DISABLE KEYS */;
INSERT INTO `tbl_drivers` (`id`, `user_id`, `city_id`) VALUES
	(12, 23, 1),
	(19, 28, 1);
/*!40000 ALTER TABLE `tbl_drivers` ENABLE KEYS */;

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

-- Дамп данных таблицы crrt.tbl_invoices: ~2 rows (приблизительно)
/*!40000 ALTER TABLE `tbl_invoices` DISABLE KEYS */;
INSERT INTO `tbl_invoices` (`id`, `code`, `car_id`, `driver_id`, `client_id`, `exp_price`, `date_start`, `date_end`, `is_canceled`, `is_rejected`, `reject_reason`, `passport_id`, `ts_created`) VALUES
	(28, 'prthukj', 24, NULL, 22, 160, '2022-09-06', '2022-09-10', 0, 0, NULL, 10, '2022-08-31 17:08:09'),
	(29, 'mqnvujp', 18, 12, 22, 128, '2022-08-30', '2022-09-04', 0, 0, NULL, 11, '2022-08-31 17:55:48');
/*!40000 ALTER TABLE `tbl_invoices` ENABLE KEYS */;

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

-- Дамп данных таблицы crrt.tbl_logs: ~8 rows (приблизительно)
/*!40000 ALTER TABLE `tbl_logs` DISABLE KEYS */;
INSERT INTO `tbl_logs` (`id`, `class`, `method`, `level`, `message`, `ts_created`) VALUES
	(54, 'IntroService', 'createRent', 'INFO', 'User [22] rented car [24] for date range [2022-09-01] to [2022-09-02]. Rent price was [160.0]. With driver -> [null]', '2022-08-31 17:08:09'),
	(55, 'ManagerService', 'deleteRepairmentInvoice', 'INFO', 'Repairment invoice [#14] deleted successfully from client@mail.ua.', '2022-08-31 17:18:58'),
	(56, 'IntroService', 'createRent', 'INFO', 'User [22] rented car [18] for date range [2022-09-01] to [2022-09-04]. Rent price was [128.0]. With driver -> [12]', '2022-08-31 17:55:50'),
	(57, 'ManagerService', 'deleteRepairmentInvoice', 'INFO', 'Repairment invoice [#15] deleted successfully from client@mail.ua.', '2022-08-31 20:09:31'),
	(58, 'ManagerService', 'deleteRepairmentInvoice', 'INFO', 'Repairment invoice [#16] deleted successfully from client@mail.ua.', '2022-08-31 20:12:15'),
	(59, 'ManagerService', 'deleteRepairmentInvoice', 'INFO', 'Repairment invoice [#17] deleted successfully from client@mail.ua.', '2022-08-31 20:21:40'),
	(60, 'ManagerService', 'deleteRepairmentInvoice', 'INFO', 'Money refunded from repairment invoice [#17] to client@mail.ua at amount of 100.0$.', '2022-08-31 20:21:43'),
	(61, 'ManagerService', 'deleteRepairmentInvoice', 'INFO', 'Repairment invoice [#19] deleted successfully from client@mail.ua.', '2022-08-31 20:35:29'),
	(62, 'ManagerService', 'deleteRepairmentInvoice', 'INFO', 'Money refunded from repairment invoice [#19] to client@mail.ua at amount of 200.0$.', '2022-08-31 20:35:31'),
	(63, 'DriverService', 'skipInvoice', 'INFO', 'Driver [12] successfully skipped rent [28]. In was delegated to driver [28].', '2022-08-31 20:39:46');
/*!40000 ALTER TABLE `tbl_logs` ENABLE KEYS */;

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

-- Дамп данных таблицы crrt.tbl_passport: ~2 rows (приблизительно)
/*!40000 ALTER TABLE `tbl_passport` DISABLE KEYS */;
INSERT INTO `tbl_passport` (`id`, `firstname`, `surname`, `patronymic`, `date_of_birth`, `date_of_issue`, `doc_number`, `rntrc`, `authority`, `ts_created`) VALUES
	(10, 'Taras', 'Ivanchenko', 'Borisovich', '2000-08-10', '2018-01-01', 999999999, 9999999999, 9999, '2022-08-31 17:08:09'),
	(11, 'Taras', 'Ivanchenko', 'Borisovich', '2003-10-22', '2018-01-01', 999999999, 9999999999, 9999, '2022-08-31 17:55:48');
/*!40000 ALTER TABLE `tbl_passport` ENABLE KEYS */;

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

-- Дамп данных таблицы crrt.tbl_repair_invoices: ~1 rows (приблизительно)
/*!40000 ALTER TABLE `tbl_repair_invoices` DISABLE KEYS */;
INSERT INTO `tbl_repair_invoices` (`id`, `invoice_id`, `price`, `expiration_date`, `comment`, `is_paid`, `ts_created`, `ts_edited`) VALUES
	(18, 29, 100, '2022-08-31', 'Test repairment invoice', 1, '2022-08-31 20:26:42', '2022-08-31 20:36:59');
/*!40000 ALTER TABLE `tbl_repair_invoices` ENABLE KEYS */;

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

-- Дамп данных таблицы crrt.tbl_users: ~5 rows (приблизительно)
/*!40000 ALTER TABLE `tbl_users` DISABLE KEYS */;
INSERT INTO `tbl_users` (`id`, `email`, `password`, `firstname`, `surname`, `patronymic`, `avatar`, `balance`, `role_id`, `is_blocked`, `conf_code`, `ts_created`) VALUES
	(21, 'admin@crrt.com', '$2a$06$YVPxV0z3V0LwalPda0Dqb.bDdXHNUwFCA/4XA5wTU6ZT8S32v4.Me', 'Михайло', 'Дяченко', 'Дмитрович', '166194109941071975upjupv.jpg', 10000, 4, 2, NULL, '2022-08-31 12:38:58'),
	(22, 'client@mail.ua', '$2a$06$YVPxV0z3V0LwalPda0Dqb.0BZt7lIgrUhe0btOuKJRNdC4c/gFIae', NULL, NULL, NULL, NULL, 172, 1, 2, NULL, '2022-08-31 13:20:11'),
	(23, 'driver@crrt.com', '$2a$06$YVPxV0z3V0LwalPda0Dqb.9Hhxys3W5WL6BW3mvomzlPgxc/0KpS2', 'Серхіо', 'Дзаглика', 'Лукіч', NULL, 0, 2, 2, NULL, '2022-08-31 17:48:50'),
	(27, 'manager@crrt.com', '$2a$06$YVPxV0z3V0LwalPda0Dqb.dgZWSFL3zk9a4R3vlIUiNGzUQ/8EEle', NULL, NULL, NULL, NULL, 0, 3, 2, NULL, '2022-08-31 19:21:25'),
	(28, 'driver2@crrt.com', '$2a$06$YVPxV0z3V0LwalPda0Dqb.8lxltH7WuLqK37nSxy5qH34jkGZfuny', NULL, NULL, NULL, NULL, 0, 2, 2, NULL, '2022-08-31 20:38:28');
/*!40000 ALTER TABLE `tbl_users` ENABLE KEYS */;

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
