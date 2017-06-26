-- --------------------------------------------------------
-- Хост:                         127.0.0.1
-- Версия сервера:               5.7.13 - MySQL Community Server (GPL)
-- Операционная система:         Win32
-- HeidiSQL Версия:              9.4.0.5156
-- --------------------------------------------------------


-- Дамп структуры для таблица whipcake.api_apps
DROP TABLE IF EXISTS `api_apps`;
CREATE TABLE IF NOT EXISTS `api_apps` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `key` varchar(15) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `is_banned` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `key` (`key`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- Дамп данных таблицы whipcake.api_apps: ~2 rows (приблизительно)
/*!40000 ALTER TABLE `api_apps` DISABLE KEYS */;
INSERT INTO `api_apps` (`id`, `key`, `description`, `is_banned`) VALUES
	(1, '1', NULL, 0),
	(2, '2', NULL, 0),
	(3, 'banned', NULL, 1);
/*!40000 ALTER TABLE `api_apps` ENABLE KEYS */;

-- Дамп структуры для таблица whipcake.countries
DROP TABLE IF EXISTS `countries`;
CREATE TABLE IF NOT EXISTS `countries` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- Дамп данных таблицы whipcake.countries: ~1 rows (приблизительно)
/*!40000 ALTER TABLE `countries` DISABLE KEYS */;
INSERT INTO `countries` (`id`, `name`) VALUES
	(1, 'testCountry');
/*!40000 ALTER TABLE `countries` ENABLE KEYS */;

-- Дамп структуры для таблица whipcake.cities
DROP TABLE IF EXISTS `cities`;
CREATE TABLE IF NOT EXISTS `cities` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `country_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name1_UNIQUE` (`name`),
  KEY `fk_cities_countries_idx` (`country_id`),
  CONSTRAINT `fk_cities_countries` FOREIGN KEY (`country_id`) REFERENCES `countries` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- Дамп данных таблицы whipcake.cities: ~1 rows (приблизительно)
/*!40000 ALTER TABLE `cities` DISABLE KEYS */;
INSERT INTO `cities` (`id`, `name`, `country_id`) VALUES
	(1, 'testCity', 1);
/*!40000 ALTER TABLE `cities` ENABLE KEYS */;



-- Дамп структуры для таблица whipcake.projects
DROP TABLE IF EXISTS `projects`;
CREATE TABLE IF NOT EXISTS `projects` (
  `id` bigint(19) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` text,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ,
  `is_archived` tinyint(1) NOT NULL DEFAULT '0',
  `image` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Дамп данных таблицы whipcake.projects: ~0 rows (приблизительно)
/*!40000 ALTER TABLE `projects` DISABLE KEYS */;
/*!40000 ALTER TABLE `projects` ENABLE KEYS */;

-- Дамп структуры для таблица whipcake.tasks
DROP TABLE IF EXISTS `tasks`;
CREATE TABLE IF NOT EXISTS `tasks` (
  `id` bigint(19) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` text,
  `deadline` datetime DEFAULT NULL,
  `data` text DEFAULT NULL,
  `importance` tinyint(3) unsigned DEFAULT NULL,
  `complexity` tinyint(1) unsigned DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `is_archived` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- Дамп данных таблицы whipcake.tasks: ~1 rows (приблизительно)
/*!40000 ALTER TABLE `tasks` DISABLE KEYS */;
INSERT INTO `tasks` (`id`, `title`, `description`, `deadline`, `data`, `importance`, `complexity`, `created_at`, `updated_at`, `is_archived`) VALUES
	(1, 'First test task', 'descr1', NULL, '{"key": "val"}', 1, 3, '2017-05-29 17:17:37', '2017-05-29 21:37:14', 0),
	(2, 'Second test task', 'descr2', NULL, '{"key": "val"}', 3, 1, '2017-05-29 17:17:37', '2017-05-29 21:37:14', 0);
/*!40000 ALTER TABLE `tasks` ENABLE KEYS */;

-- Дамп структуры для таблица whipcake.project_has_task
DROP TABLE IF EXISTS `project_has_task`;
CREATE TABLE IF NOT EXISTS `project_has_task` (
  `projects_id` bigint(19) unsigned NOT NULL,
  `tasks_id` bigint(19) unsigned NOT NULL,
  PRIMARY KEY (`projects_id`,`tasks_id`),
  KEY `fk_projects_has_tasks_tasks1_idx` (`tasks_id`),
  KEY `fk_projects_has_tasks_projects1_idx` (`projects_id`),
  CONSTRAINT `fk_projects_has_tasks_projects1` FOREIGN KEY (`projects_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_projects_has_tasks_tasks1` FOREIGN KEY (`tasks_id`) REFERENCES `tasks` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Дамп данных таблицы whipcake.project_has_task: ~0 rows (приблизительно)
/*!40000 ALTER TABLE `project_has_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_has_task` ENABLE KEYS */;

-- Дамп структуры для таблица whipcake.user_ranks
DROP TABLE IF EXISTS `user_ranks`;
CREATE TABLE IF NOT EXISTS `user_ranks` (
  `id` tinyint(3) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- Дамп данных таблицы whipcake.user_ranks: ~1 rows (приблизительно)
/*!40000 ALTER TABLE `user_ranks` DISABLE KEYS */;
INSERT INTO `user_ranks` (`id`, `name`) VALUES
	(1, 'testRank');
/*!40000 ALTER TABLE `user_ranks` ENABLE KEYS */;

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
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `city_id` int(10) unsigned DEFAULT NULL,
  `statuses` text DEFAULT NULL,
  `user_rank_id` tinyint(3) unsigned NOT NULL,
  `premium_until` datetime DEFAULT NULL,
  `is_banned` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `login_UNIQUE` (`login`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  KEY `fk_users_cities1_idx` (`city_id`),
  KEY `fk_users_user_ranks1_idx` (`user_rank_id`),
  CONSTRAINT `fk_users_cities1` FOREIGN KEY (`city_id`) REFERENCES `cities` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_users_user_ranks1` FOREIGN KEY (`user_rank_id`) REFERENCES `user_ranks` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- Дамп данных таблицы whipcake.users: ~2 rows (приблизительно)
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` (`id`, `login`, `email`, `pass_hash`, `name`, `avatar`, `about_myself`, `date_of_birth`, `sex`, `created_at`, `updated_at`, `city_id`, `statuses`, `user_rank_id`, `premium_until`, `is_banned`) VALUES
	(1, 'testLogin', 'test', '$2a$10$bM4MbiT5.SIZV/HWyQFuDeu1dLpF/9Sun2f9H6HRruCCKLTAVfFxq', 'testName', 'testAvatar', NULL, NULL, NULL, '2017-05-01 21:10:14', '2017-05-20 10:33:09', 1, NULL, 1, NULL, 0),
	(2, 'id2', 'testemail@testdomain.test', '$2a$10$ExnMSd2gA.oeMebTAcq9iujqSbKfLVoWwJEH/RJWP6LahRULm1/Hi', NULL, NULL, NULL, NULL, NULL, '2017-05-20 21:28:57', '2017-05-20 21:28:58', NULL, NULL, 1, NULL, 0);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;

-- Дамп структуры для таблица whipcake.habits
DROP TABLE IF EXISTS `habits`;
CREATE TABLE IF NOT EXISTS `habits` (
  `id` bigint(19) unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `description` text,
  `habit_type` int NOT NULL,
  `complexity` int DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ,
  `user_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_habits_users1_idx` (`user_id`),
  CONSTRAINT `fk_habits_users1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Дамп структуры для таблица whipcake.users_has_api_tokens
DROP TABLE IF EXISTS `users_has_api_tokens`;
CREATE TABLE IF NOT EXISTS `users_has_api_tokens` (
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

-- Дамп данных таблицы whipcake.users_has_api_tokens: ~2 rows (приблизительно)
/*!40000 ALTER TABLE `users_has_api_tokens` DISABLE KEYS */;
INSERT INTO `users_has_api_tokens` (`token`, `user_id`, `app_id`, `expires_at`) VALUES
	('ft1IjFotneQESvMktZqVrQ4Xas0weJ', 1, 2, DATEADD('DAY',17, NOW())),
	('PjdSyBX62WSq8b1IEOEFMfsjBYZcpP', 1, 1, DATEADD('DAY',17, NOW()));
/*!40000 ALTER TABLE `users_has_api_tokens` ENABLE KEYS */;

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

-- Дамп данных таблицы whipcake.user_has_task: ~1 rows (приблизительно)
/*!40000 ALTER TABLE `user_has_task` DISABLE KEYS */;
INSERT INTO `user_has_task` (`user_id`, `task_id`) VALUES
	(1, 1),
	(2, 2);

