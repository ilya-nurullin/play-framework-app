-- --------------------------------------------------------
-- Хост:                         127.0.0.1
-- Версия сервера:               5.7.13 - MySQL Community Server (GPL)
-- Операционная система:         Win32
-- HeidiSQL Версия:              9.4.0.5156
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Дамп структуры для таблица whipcake.api_apps
DROP TABLE IF EXISTS `api_apps`;
CREATE TABLE IF NOT EXISTS `api_apps` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `key` varchar(15) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `is_banned` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `key` (`key`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- Дамп данных таблицы whipcake.api_apps: ~3 rows (приблизительно)
/*!40000 ALTER TABLE `api_apps` DISABLE KEYS */;
INSERT INTO `api_apps` (`id`, `key`, `description`, `is_banned`) VALUES
  (1, '1', NULL, 0),
  (2, '2', NULL, 0),
  (3, 'banned', NULL, 1);
/*!40000 ALTER TABLE `api_apps` ENABLE KEYS */;

-- Дамп структуры для таблица whipcake.cities
DROP TABLE IF EXISTS `cities`;
CREATE TABLE IF NOT EXISTS `cities` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `country_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`),
  KEY `fk_cities_countries_idx` (`country_id`),
  CONSTRAINT `fk_cities_countries` FOREIGN KEY (`country_id`) REFERENCES `countries` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- Дамп данных таблицы whipcake.cities: ~0 rows (приблизительно)
/*!40000 ALTER TABLE `cities` DISABLE KEYS */;
INSERT INTO `cities` (`id`, `name`, `country_id`) VALUES
  (1, 'testCity', 1);
/*!40000 ALTER TABLE `cities` ENABLE KEYS */;

-- Дамп структуры для таблица whipcake.countries
DROP TABLE IF EXISTS `countries`;
CREATE TABLE IF NOT EXISTS `countries` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- Дамп данных таблицы whipcake.countries: ~0 rows (приблизительно)
/*!40000 ALTER TABLE `countries` DISABLE KEYS */;
INSERT INTO `countries` (`id`, `name`) VALUES
  (1, 'testCountry');
/*!40000 ALTER TABLE `countries` ENABLE KEYS */;

-- Дамп структуры для таблица whipcake.habits
DROP TABLE IF EXISTS `habits`;
CREATE TABLE IF NOT EXISTS `habits` (
  `id` bigint(19) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` text,
  `habit_type` tinyint(3) unsigned NOT NULL,
  `complexity` tinyint(3) unsigned DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `user_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_habits_users1_idx` (`user_id`),
  CONSTRAINT `fk_habits_users1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Дамп данных таблицы whipcake.habits: ~0 rows (приблизительно)
/*!40000 ALTER TABLE `habits` DISABLE KEYS */;
/*!40000 ALTER TABLE `habits` ENABLE KEYS */;

-- Дамп структуры для таблица whipcake.projects
DROP TABLE IF EXISTS `projects`;
CREATE TABLE IF NOT EXISTS `projects` (
  `id` bigint(19) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` text,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_archived` tinyint(1) NOT NULL DEFAULT '0',
  `color` char(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- Дамп данных таблицы whipcake.projects: ~1 rows (приблизительно)
/*!40000 ALTER TABLE `projects` DISABLE KEYS */;
INSERT INTO `projects` (`id`, `title`, `description`, `created_at`, `updated_at`, `is_archived`, `color`) VALUES
  (1, 'Тестовый преокт', NULL, '2017-07-07 11:17:12', '2017-07-07 11:17:12', 0, NULL),
  (2, 'Второй тестовый преокт', NULL, '2017-07-07 11:17:12', '2017-07-07 11:17:12', 0, NULL),
  (3, 'Проект3', NULL, '2017-08-30 12:15:03', '2017-08-30 12:15:03', 0, NULL);

/*!40000 ALTER TABLE `projects` ENABLE KEYS */;

-- Дамп структуры для таблица whipcake.tasks
DROP TABLE IF EXISTS `tasks`;
CREATE TABLE IF NOT EXISTS `tasks` (
  `id` bigint(19) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `project_id` bigint(20) unsigned NOT NULL,
  `description` text,
  `deadline` datetime DEFAULT NULL,
  `data` json DEFAULT NULL,
  `importance` tinyint(3) unsigned DEFAULT NULL,
  `complexity` tinyint(1) unsigned DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_open_for_surety` TINYINT(1) NOT NULL DEFAULT '0',
  `is_archived` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `FK_tasks_projects` (`project_id`),
  CONSTRAINT `FK_tasks_projects` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=utf8;

-- Дамп данных таблицы whipcake.tasks: ~28 rows (приблизительно)
/*!40000 ALTER TABLE `tasks` DISABLE KEYS */;
INSERT INTO `tasks` (`id`, `title`, `project_id`, `description`, `deadline`, `data`, `importance`, `complexity`, `created_at`, `updated_at`,`is_open_for_surety`, `is_archived`) VALUES
  (1, 'First test task', 1, 'descr1', NULL, '{"key": "val"}', 1, 3, '2017-05-29 17:17:37', '2017-07-07 11:18:31', 0, 0),
  (2, 'Second test task', 1, 'descr2', NULL, '{"key": "val"}', 3, 1, '2017-05-29 17:17:37', '2017-07-07 11:18:31', 0, 0),
  (3, 'New Task', 1, NULL, NULL, NULL, NULL, NULL, '2017-06-23 09:05:26', '2017-07-07 11:18:31', 0, 0),
  (4, 'New Task', 1, NULL, NULL, NULL, NULL, NULL, '2017-06-23 09:05:59', '2017-07-07 11:18:31', 0, 0),
  (5, 'New Task', 1, 'desc', NULL, NULL, NULL, NULL, '2017-06-23 09:08:39', '2017-07-07 11:18:31', 0, 0),
  (6, 'New Task', 1, 'desc', NULL, NULL, NULL, 3, '2017-06-23 09:09:17', '2017-07-07 11:18:31', 0, 0),
  (7, 'New Task', 1, 'desc', NULL, NULL, 4, 3, '2017-06-23 09:09:28', '2017-07-07 11:18:31', 0, 0),
  (8, 'New Task', 1, 'desc', NULL, '{"amber": 3, "chick": "yes"}', 4, 3, '2017-06-23 10:13:21', '2017-07-07 11:18:31', 0, 0),
  (9, 'New Task', 1, NULL, NULL, '{"amber": 3, "chick": "yes"}', 4, 3, '2017-06-23 10:14:27', '2017-07-07 11:18:31', 0, 0),
  (10, 'New Task', 1, 'desc', NULL, NULL, 4, 3, '2017-06-23 11:20:51', '2017-07-07 11:18:31', 0, 0),
  (11, 'New Task', 1, 'desc', NULL, NULL, 4, 3, '2017-06-23 11:26:49', '2017-07-07 11:18:31', 0, 0),
  (12, 'New Task', 1, 'desc', NULL, NULL, 4, 3, '2017-06-23 11:41:15', '2017-07-07 11:18:31', 0, 0),
  (13, 'New 131 Task', 1, 'desc', NULL, '{"amber": 4, "chick": "yes"}', 4, 3, '2017-06-23 11:41:50', '2017-07-07 11:18:31', 0, 0),
  (14, 'New Task', 1, 'desc', NULL, '{"amber": 3, "chick": "yes"}', 4, 3, '2017-06-23 11:43:01', '2017-07-07 11:18:31', 0, 0),
  (15, 'New Task', 1, 'desc', NULL, '{"amber": 4, "chick": "yes"}', 4, 3, '2017-06-23 11:43:43', '2017-07-07 11:18:31', 0, 0),
  (16, 'New Task', 1, 'desc', NULL, '{"amber": 4, "chick": "yes"}', 4, 3, '2017-06-23 11:43:43', '2017-07-07 11:18:31', 0, 0),
  (17, 'New Task', 1, 'desc', NULL, '{"amber": 4, "chick": "yes"}', 4, 3, '2017-06-23 11:43:43', '2017-07-07 11:18:31', 0, 0),
  (18, 'New Task', 1, 'desc', NULL, '{"amber": 4, "chick": "yes"}', 4, 3, '2017-06-23 11:43:43', '2017-07-07 11:18:31', 0, 0),
  (19, 'New Task', 1, 'desc', NULL, '{"amber": 4, "chick": "yes"}', 4, 3, '2017-06-23 11:43:43', '2017-07-07 11:18:31', 0, 0),
  (20, 'New Task', 1, 'desc', NULL, '{"amber": 4, "chick": "yes"}', 4, 3, '2017-06-23 11:43:43', '2017-07-07 11:18:31', 0, 0),
  (21, 'New Task', 1, 'desc', NULL, '{"amber": 4, "chick": "yes"}', 4, 3, '2017-06-23 11:43:43', '2017-07-07 11:18:31', 0, 0),
  (22, 'New Task', 1, 'desc', NULL, '{"amber": 4, "chick": "yes"}', 4, 3, '2017-06-23 11:43:43', '2017-07-07 11:18:31', 0, 0),
  (23, 'New Task', 1, 'desc', NULL, '{"amber": 4, "chick": "yes"}', 4, 3, '2017-06-23 11:43:43', '2017-07-07 11:18:31', 0, 0),
  (24, 'New Task', 1, 'desc', NULL, '{"amber": 4, "chick": "yes"}', 4, 3, '2017-06-23 11:43:43', '2017-07-07 11:18:31', 0, 0),
  (25, 'New Task', 1, 'desc', NULL, '{"amber": 4, "chick": "yes"}', 4, 3, '2017-06-23 11:43:43', '2017-07-07 11:18:31', 0, 0),
  (26, 'New Task', 1, 'desc', NULL, '{"amber": 4, "chick": "yes"}', 4, 3, '2017-06-23 11:43:43', '2017-07-07 11:18:31', 0, 0),
  (27, 'New Task', 1, 'desc', NULL, '{"amber": 4, "chick": "yes"}', 4, 3, '2017-06-23 11:43:43', '2017-07-07 11:18:31', 0, 0),
  (28, 'New Task', 2, 'desc', NULL, '{"amber": 4, "chick": "yes"}', 4, 3, '2017-06-23 11:43:43', '2017-07-07 11:18:31', 0, 0);
/*!40000 ALTER TABLE `tasks` ENABLE KEYS */;

-- Дамп структуры для таблица whipcake.users
DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `login` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `pass_hash` varchar(100) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `avatar` varchar(100) DEFAULT NULL,
  `about_myself` varchar(255) DEFAULT NULL,
  `date_of_birth` date DEFAULT NULL,
  `sex` tinyint(1) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `city_id` int(10) unsigned DEFAULT NULL,
  `statuses` json DEFAULT NULL,
  `user_rank_id` tinyint(3) unsigned NOT NULL,
  `premium_until` datetime DEFAULT NULL,
  `is_banned` tinyint(1) NOT NULL DEFAULT '0',
  `soc_networks` json DEFAULT NULL,
  `default_project` BIGINT(20) UNSIGNED NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `login_UNIQUE` (`login`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  KEY `fk_users_cities1_idx` (`city_id`),
  KEY `fk_users_user_ranks1_idx` (`user_rank_id`),
  INDEX `FK_users_projects` (`default_project`),
  CONSTRAINT `FK_users_projects` FOREIGN KEY (`default_project`) REFERENCES `projects` (`id`) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT `fk_users_cities1` FOREIGN KEY (`city_id`) REFERENCES `cities` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_users_user_ranks1` FOREIGN KEY (`user_rank_id`) REFERENCES `user_ranks` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- Дамп данных таблицы whipcake.users: ~2 rows (приблизительно)
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` (`id`, `login`, `email`, `pass_hash`, `name`, `avatar`, `about_myself`, `date_of_birth`, `sex`, `created_at`, `updated_at`, `city_id`, `statuses`, `user_rank_id`, `premium_until`, `is_banned`, `soc_networks`, `default_project`) VALUES
  (1, 'testLogin', 'test', '$2a$10$gZtzxmt1KJICEFFLZdyMDefn8Cmc2hoE9c0oBhp5dlWEfJ9y5enyi' /*PASS: testPassword*/, 'testName', 'testAva', NULL, NULL, NULL, '2017-05-01 21:10:14', '2017-06-15 22:37:07', NULL, NULL, 1, NULL, 0, NULL, 1),
  (2, 'id2', 'testemail@testdomain.test', '$2a$10$ExnMSd2gA.oeMebTAcq9iujqSbKfLVoWwJEH/RJWP6LahRULm1/Hi', NULL, NULL, NULL, NULL, NULL, '2017-05-20 21:28:57', '2017-05-20 21:28:58', NULL, NULL, 1, NULL, 0, NULL, 2);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;

-- Дамп структуры для таблица whipcake.users_has_api_tokens
DROP TABLE IF EXISTS `user_has_api_token`;
CREATE TABLE IF NOT EXISTS `user_has_api_token` (
  `token` varchar(30) NOT NULL,
  `user_id` int(10) unsigned NOT NULL,
  `app_id` int(11) unsigned NOT NULL,
  `expires_at` datetime NOT NULL,
  PRIMARY KEY (`token`),
  UNIQUE KEY `user_id` (`user_id`,`app_id`),
  KEY `fk_users_has_api_apps_users1_idx` (`user_id`),
  KEY `FK_users_has_api_tokens_api_apps` (`app_id`),
  CONSTRAINT `FK_users_has_api_tokens_api_apps` FOREIGN KEY (`app_id`) REFERENCES `api_apps` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_users_has_api_apps_users1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Дамп данных таблицы whipcake.users_has_api_tokens: ~4 rows (приблизительно)
/*!40000 ALTER TABLE `user_has_api_token` DISABLE KEYS */;
INSERT INTO `user_has_api_token` (`token`, `user_id`, `app_id`, `expires_at`) VALUES
  ('2', 2, 1, '2017-07-24 11:16:11'),
  ('ft1IjFotneQESvMktZqVrQ4Xas0weJ', 1, 2, ADDDATE(NOW(), INTERVAL 17 DAY)),
  ('PjdSyBX62WSq8b1IEOEFMfsjBYZcpP', 1, 1, ADDDATE(NOW(), INTERVAL 17 DAY));
/*!40000 ALTER TABLE `user_has_api_token` ENABLE KEYS */;

-- Дамп структуры для таблица whipcake.user_has_project
DROP TABLE IF EXISTS `user_has_project`;
CREATE TABLE IF NOT EXISTS `user_has_project` (
  `user_id` int(10) unsigned NOT NULL,
  `project_id` bigint(19) unsigned NOT NULL,
  PRIMARY KEY (`user_id`,`project_id`),
  KEY `fk_users_has_projects_projects1_idx` (`project_id`),
  KEY `fk_users_has_projects_users1_idx` (`user_id`),
  CONSTRAINT `fk_users_has_projects_projects1` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_users_has_projects_users1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Дамп данных таблицы whipcake.user_has_project: ~0 rows (приблизительно)
/*!40000 ALTER TABLE `user_has_project` DISABLE KEYS */;
INSERT INTO `user_has_project` (`user_id`, `project_id`) VALUES
  (1, 1),
  (1, 3),
  (2, 2);
/*!40000 ALTER TABLE `user_has_project` ENABLE KEYS */;

-- Дамп структуры для таблица whipcake.user_has_task
DROP TABLE IF EXISTS `user_has_task`;
CREATE TABLE IF NOT EXISTS `user_has_task` (
  `user_id` int(10) unsigned NOT NULL,
  `task_id` bigint(19) unsigned NOT NULL,
  PRIMARY KEY (`user_id`,`task_id`),
  KEY `fk_users_has_tasks_tasks1_idx` (`task_id`),
  KEY `fk_users_has_tasks_users1_idx` (`user_id`),
  CONSTRAINT `fk_users_has_tasks_tasks1` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_users_has_tasks_users1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Дамп данных таблицы whipcake.user_has_task: ~28 rows (приблизительно)
/*!40000 ALTER TABLE `user_has_task` DISABLE KEYS */;
INSERT INTO `user_has_task` (`user_id`, `task_id`) VALUES
  (1, 1),
  (1, 3),
  (1, 4),
  (1, 6),
  (1, 7),
  (1, 8),
  (1, 9),
  (1, 10),
  (1, 11),
  (1, 12),
  (1, 13),
  (1, 14),
  (1, 15),
  (1, 16),
  (1, 17),
  (1, 18),
  (1, 19),
  (1, 20),
  (1, 21),
  (1, 22),
  (1, 23),
  (1, 24),
  (1, 25),
  (1, 26),
  (1, 27),
  (1, 28),
  (2, 2),
  (2, 5);
/*!40000 ALTER TABLE `user_has_task` ENABLE KEYS */;

-- Дамп структуры для таблица whipcake.user_ranks
DROP TABLE IF EXISTS `user_ranks`;
CREATE TABLE IF NOT EXISTS `user_ranks` (
  `id` tinyint(3) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- Дамп данных таблицы whipcake.user_ranks: ~0 rows (приблизительно)
/*!40000 ALTER TABLE `user_ranks` DISABLE KEYS */;
INSERT INTO `user_ranks` (`id`, `name`) VALUES
  (1, 'testRank');
/*!40000 ALTER TABLE `user_ranks` ENABLE KEYS */;

DROP TABLE IF EXISTS `user_has_social_network`;
CREATE TABLE `user_has_social_network` (
  `user_id` INT(10) UNSIGNED NOT NULL,
  `network_name` VARCHAR(45) NOT NULL,
  `user_network_id` VARCHAR(45) NOT NULL,
  `email` VARCHAR(100) NULL DEFAULT NULL,
  PRIMARY KEY (`user_id`, `network_name`),
  CONSTRAINT `fk_user_has_social_network_users1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON UPDATE CASCADE ON DELETE CASCADE
) COLLATE='utf8_general_ci' ENGINE=InnoDB;


DROP TABLE IF EXISTS `user_has_last_sync_id`;
CREATE TABLE `user_has_last_sync_id` (
  `user_id` INT(10) UNSIGNED NOT NULL,
  `app_id` INT(11) UNSIGNED NOT NULL,
  `last_sync_id` BIGINT(19) UNSIGNED NULL DEFAULT '0',
  PRIMARY KEY (`user_id`, `app_id`),
  INDEX `fk_users_has_api_apps_users2_idx` (`user_id`),
  INDEX `fk_users_has_api_apps_api_apps1_idx` (`app_id`),
  CONSTRAINT `fk_users_has_api_apps_api_apps1` FOREIGN KEY (`app_id`) REFERENCES `api_apps` (`id`) ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT `fk_users_has_api_apps_users2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON UPDATE CASCADE ON DELETE CASCADE
) COLLATE='utf8_general_ci' ENGINE=InnoDB;


/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
