CREATE TABLE IF NOT EXISTS `character_secondary_password` (
  `account_name` VARCHAR(45) NOT NULL DEFAULT '',
  `var`  VARCHAR(255) NOT NULL DEFAULT '',
  `value` text NOT NULL,
  PRIMARY KEY (`account_name`,`var`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;