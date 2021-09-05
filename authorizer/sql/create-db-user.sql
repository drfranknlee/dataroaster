# create db and user.
CREATE DATABASE dataroaster;
 
CREATE USER 'dataroaster'@'localhost' IDENTIFIED BY 'icarus0337';
GRANT ALL PRIVILEGES ON *.* TO 'dataroaster'@'localhost' WITH GRANT OPTION;
flush privileges;
