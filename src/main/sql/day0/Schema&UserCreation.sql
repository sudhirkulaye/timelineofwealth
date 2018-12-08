GRANT ALL ON equityanalysis.* TO 'devuser'@'localhost' IDENTIFIED BY 'Dev703';

-- CREATE USER 'analyst'@'localhost' IDENTIFIED BY 'Analyst703';
GRANT ALL ON micromacronumbers.* TO 'devuser'@'localhost' IDENTIFIED BY 'Dev703';

create schema twealthbookprod;
create user 'apiuser'@'%' identified by 'ApiU$er108';
GRANT ALL ON twealthbookprod.* TO 'apiuser'@'localhost' IDENTIFIED BY 'ApiU$er108';

/**  timelineofwealth.com  **/

create schema timelineofwealth;
create user 'towdevuser'@'%' identified by 'Dev703';
GRANT ALL ON timelineofwealth.* TO 'towdevuser'@'localhost' IDENTIFIED BY 'Dev703';
