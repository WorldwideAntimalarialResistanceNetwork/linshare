-- Default script for database creation

DROP DATABASE IF EXISTS linshare;
DROP DATABASE IF EXISTS linshare_data;

CREATE USER linshare WITH PASSWORD 'linshare';
-- ALTER USER linshare WITH PASSWORD 'linshare';

CREATE DATABASE linshare_next
  WITH OWNER = linshare
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'fr_US.UTF-8'
       LC_CTYPE = 'fr_US.UTF-8'
       CONNECTION LIMIT = -1;

CREATE DATABASE linshare_next_data
  WITH OWNER = linshare
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'fr_US.UTF-8'
       LC_CTYPE = 'fr_US.UTF-8'
       CONNECTION LIMIT = -1;

GRANT ALL ON DATABASE linshare TO linshare;
GRANT ALL ON DATABASE linshare_data TO linshare;


