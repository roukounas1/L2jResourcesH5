CREATE TABLE IF NOT EXISTS `global_variables` (
  `var`  VARCHAR(50) NOT NULL DEFAULT '',
  `value` VARCHAR(255) ,
  PRIMARY KEY (`var`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;