create table users
(
    `id`       bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `username` varchar(100) not null,
    `password` varchar(400) not null,
    `enabled`  boolean     not null,
    PRIMARY KEY (`id`),
    UNIQUE KEY `username_unique` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

create table user_authorities
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `user_id`   bigint(11) unsigned NOT NULL,
    `authority` varchar(50) not null,
    PRIMARY KEY (`id`),
    UNIQUE KEY `username_authorities_unique` (`user_id`, `authority`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `user_authorities`
    ADD CONSTRAINT `fk_authorities_users`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

create table groups
(
    `id`       bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `group`    varchar(100) not null,
    PRIMARY KEY (`id`),
    UNIQUE KEY `group_unique` (`group`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;


create table user_groups
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `user_id`   bigint(11) unsigned NOT NULL,
    `group_id`  bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `username_group_unique` (`user_id`, `group_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `user_groups`
    ADD CONSTRAINT `fk_groups`
        FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`);

ALTER TABLE `user_groups`
    ADD CONSTRAINT `fk_users`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);




-- add users.
INSERT INTO users
(
	`id`,
	`username`,
	`password`,
	`enabled`
)
VALUES
(
	1,
	'mykidong',
	'$2a$08$jLRJiKPSyuitd1P.QMOYmOc7dcdIGI4GwGlnQzXZTdgs/.Crhmm7m',
	1
);

-- add user authorities.
INSERT INTO user_authorities
(
	`id`,
	`user_id`,
	`authority`
)
VALUES
(
	1,
	1,
	'ROLE_PLATFORM_ADMIN'
);



-- add groups.
INSERT INTO groups
(
	`id`,
	`group`
)
VALUES
(
	1,
	'developer'
);

INSERT INTO groups
(
	`id`,
	`group`
)
VALUES
(
	2,
	'data-engineer'
);

INSERT INTO groups
(
	`id`,
	`group`
)
VALUES
(
	3,
	'data-scientist'
);


-- add user groups.
INSERT INTO user_groups
(
	`id`,
	`user_id`,
	`group_id`
)
VALUES
(
	1,
	1,
	1
);

INSERT INTO user_groups
(
	`id`,
	`user_id`,
	`group_id`
)
VALUES
(
	2,
	1,
	2
);

INSERT INTO user_groups
(
	`id`,
	`user_id`,
	`group_id`
)
VALUES
(
	3,
	1,
	3
);

