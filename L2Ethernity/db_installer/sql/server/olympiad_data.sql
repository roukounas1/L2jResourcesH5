CREATE TABLE IF NOT EXISTS `olympiad_data` (
  `id` TINYINT UNSIGNED NOT NULL default 0,
  `current_cycle` MEDIUMINT UNSIGNED NOT NULL default 1,
  `period` MEDIUMINT UNSIGNED NOT NULL default 0,
  `comp_start` bigint(13) unsigned NOT NULL DEFAULT '0',
  `comp_end` bigint(13) unsigned NOT NULL DEFAULT '0',
  `olympiad_end` bigint(13) unsigned NOT NULL DEFAULT '0',
  `validation_end` bigint(13) unsigned NOT NULL DEFAULT '0',
  `next_weekly_change` bigint(13) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;