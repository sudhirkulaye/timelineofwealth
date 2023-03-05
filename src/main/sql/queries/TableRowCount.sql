DROP TABLE dummy_user_table_row_count;

CREATE TABLE dummy_user_table_row_count (
  table_name VARCHAR(64) NOT NULL,
  row_count INT NOT NULL,
  PRIMARY KEY (table_name)
);

DROP PROCEDURE IF EXISTS populate_table_row_count;

DELIMITER $$

CREATE PROCEDURE `populate_table_row_count`()
BEGIN
  DECLARE done INT DEFAULT FALSE;
  
  DECLARE tablename VARCHAR(64);
  
  DECLARE cur1 CURSOR FOR
    SELECT table_name
    FROM information_schema.tables
    WHERE table_schema = 'timelineofwealth'
    ORDER BY table_name;
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  TRUNCATE TABLE dummy_user_table_row_count;

  OPEN cur1;

  read_loop: LOOP
    FETCH cur1 INTO tablename;
    IF done THEN
      LEAVE read_loop;
    END IF;

    SET @sql = CONCAT('INSERT INTO dummy_user_table_row_count (table_name, row_count) SELECT ''', tablename, ''', COUNT(1) FROM ', tablename);
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END LOOP;

  CLOSE cur1;
END$$
DELIMITER ;

CALL populate_table_row_count();

select * from dummy_user_table_row_count;