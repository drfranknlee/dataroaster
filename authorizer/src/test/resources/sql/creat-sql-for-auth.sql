create table oauth_client_details (
  client_id VARCHAR(50) PRIMARY KEY,
  resource_ids VARCHAR(256),
  client_secret VARCHAR(256),
  scope VARCHAR(256),
  authorized_grant_types VARCHAR(256),
  web_server_redirect_uri VARCHAR(256),
  authorities VARCHAR(256),
  access_token_validity INTEGER,
  refresh_token_validity INTEGER,
  additional_information VARCHAR(4096),
  autoapprove VARCHAR(256)
) ROW_FORMAT=DYNAMIC;

create table if not exists oauth_client_token (
  token_id VARCHAR(255),
  token BLOB,
  authentication_id VARCHAR(50) PRIMARY KEY,
  user_name VARCHAR(255),
  client_id VARCHAR(255)
);

create table if not exists oauth_access_token (
  token_id VARCHAR(255),
  token BLOB,
  authentication_id VARCHAR(50) PRIMARY KEY,
  user_name VARCHAR(255),
  client_id VARCHAR(255),
  authentication BLOB,
  refresh_token VARCHAR(255)
);

create table if not exists oauth_refresh_token (
  token_id VARCHAR(255),
  token BLOB,
  authentication BLOB
);

create table if not exists oauth_code (
  code VARCHAR(255), authentication BLOB
);


INSERT INTO oauth_client_details
(
	client_id, 
	client_secret,
	resource_ids, 
	scope, 
	authorized_grant_types, 
	web_server_redirect_uri, 
	authorities, 
	access_token_validity, 
	refresh_token_validity, 
	additional_information, 
	autoapprove
)
VALUES
(
	'api',
	'$2a$08$xIucoQrRBLfgKPJi/irgMelLJaiviKhyyNd6L/fPGcMiOcUErMDsi',
	null, 
	'read,write', 
	'authorization_code,password,implicit,refresh_token',
	null,
	'API_ADMIN',
	3600,
	36000,
	null,
	null
);