-- cluster.
create table k8s_cluster
(
    `id`       bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `cluster_name` varchar(100) not null,
    `description` varchar(1000) not null,
    PRIMARY KEY (`id`),
    UNIQUE KEY `cluster_name_unique` (`cluster_name`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

-- namespace.
create table k8s_namespace
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `namespace_name`   varchar(100) not null,
    `cluster_id`   bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `namespace_cluster_unique` (`namespace_name`, `cluster_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_namespace`
    ADD CONSTRAINT `fk_k8s_namespace_k8s_cluster`
        FOREIGN KEY (`cluster_id`) REFERENCES `k8s_cluster` (`id`);

-- mapping table of namespace and group.
create table k8s_namespace_groups
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `namespace_id`   bigint(11) unsigned NOT NULL,
    `group_id`  bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `namespace_group_unique` (`namespace_id`, `group_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_namespace_groups`
    ADD CONSTRAINT `fk_k8s_namespace`
        FOREIGN KEY (`namespace_id`) REFERENCES `k8s_namespace` (`id`);

ALTER TABLE `k8s_namespace_groups`
    ADD CONSTRAINT `fk_k8s_namespace_groups`
        FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`);

-- kubeconfig for cluster admin.
create table k8s_kubeconfig_admin
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `cluster_id`   bigint(11) unsigned NOT NULL,
    `secret_path`   varchar(400) not null,
    PRIMARY KEY (`id`),
    UNIQUE KEY `cluster_unique` (`cluster_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_kubeconfig_admin`
    ADD CONSTRAINT `fk_k8s_cluster`
        FOREIGN KEY (`cluster_id`) REFERENCES `k8s_cluster` (`id`);

-- kubeconfig for users.
create table k8s_kubeconfig_user
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `user_id`   bigint(11) unsigned NOT NULL,
    `group_id`  bigint(11) unsigned NOT NULL,
    `namespace_id`   bigint(11) unsigned NOT NULL,
    `secret_path`   varchar(400) not null,
    PRIMARY KEY (`id`),
    UNIQUE KEY `user_group_namespace_unique` (`user_id`, `group_id`, `namespace_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_kubeconfig_user`
    ADD CONSTRAINT `fk_k8s_kubeconfig_user_users`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

ALTER TABLE `k8s_kubeconfig_user`
    ADD CONSTRAINT `fk_k8s_kubeconfig_user_groups`
        FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`);

ALTER TABLE `k8s_kubeconfig_user`
    ADD CONSTRAINT `fk_k8s_kubeconfig_user_k8s_namespace`
        FOREIGN KEY (`namespace_id`) REFERENCES `k8s_namespace` (`id`);


-- as a service things, for instance, csi, object storage, kafka, presto, etc...
create table k8s_services
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `type`   varchar(100) not null,
    `name`   varchar(100) not null,
    `version`   varchar(100) not null,
    PRIMARY KEY (`id`),
    UNIQUE KEY `k8s_services_unique` (`type`, `name`, `version`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	1,
	'CSI',
	'MinIO Direct CSI',
	'0.2.1'
);

INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	2,
	'KAFKA',
	'Strimzi Kafka',
	'2.5.0'
);

INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	3,
	'INGRESS_CONTROLLER',
	'Ingress NGINX',
	'0.35.0'
);

INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	4,
	'LOAD_BALANCER',
	'MetalLB',
	'0.9.3'
);

INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	5,
	'NFS',
	'Direct CSI based NFS Server',
	'1.1.1'
);


INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	6,
	'OBJECT_STORAGE',
	'MinIO',
	'RELEASE.2020-09-05T07-14-49Z'
);

INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	7,
	'OBJECT_STORAGE_OPERATOR',
	'MinIO Operator',
	'3.0.16'
);

INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	8,
	'OBJECT_STORAGE',
	'Ozone',
	'1.0.0'
);


INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	9,
	'HIVE_METASTORE',
	'Hive Metastore',
	'3.0.0'
);

INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	10,
	'CERT_MANAGER',
	'Cert Manager',
	'1.0.1'
);

INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	11,
	'SPARK_THRIFT_SERVER',
	'Spark Thrift Server',
	'3.0.1'
);

INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	12,
	'PRESTO',
	'Presto',
	'337'
);

INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	13,
	'REDASH',
	'Redash',
	'8.0.0.b32245'
);

INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	14,
	'JUPYTERHUB',
	'JupyterHub',
	'0.9.0'
);

INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	15,
	'ELASTICSEARCH',
	'Elasticsearch',
	'7.9.1'
);


INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	16,
	'WORKFLOW',
	'Argo Workflow',
	'2.11.7'
);

INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	17,
	'RDB',
	'CockroachDB',
	'20.2.0'
);

INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	18,
	'CSI',
	'OpenEBS Mayastor',
	'0.5.0'
);


INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	19,
	'MONITORING',
	'Kubernetes Prometheus Stack',
	'12.2.4'
);

INSERT INTO k8s_services
(
	`id`,
	`type`,
	`name`,
	`version`
)
VALUES
(
	20,
	'MONITORING',
	'Metrics Server',
	'0.4.1'
);

-- mapping table of monitoring and cluster.
create table k8s_monitoring_cluster
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `cluster_id`   bigint(11) unsigned NOT NULL,
    `service_id`        bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `k8s_monitoring_cluster_unique` (`cluster_id`, `service_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_monitoring_cluster`
    ADD CONSTRAINT `fk_k8s_monitoring_cluster`
        FOREIGN KEY (`cluster_id`) REFERENCES `k8s_cluster` (`id`);

ALTER TABLE `k8s_monitoring_cluster`
    ADD CONSTRAINT `fk_k8s_monitoring_services`
        FOREIGN KEY (`service_id`) REFERENCES `k8s_services` (`id`);


-- mapping table of csi and cluster.
create table k8s_csi_cluster
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `cluster_id`   bigint(11) unsigned NOT NULL,
    `service_id`        bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `k8s_csi_cluster_unique` (`cluster_id`, `service_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_csi_cluster`
    ADD CONSTRAINT `fk_k8s_csi_cluster`
        FOREIGN KEY (`cluster_id`) REFERENCES `k8s_cluster` (`id`);

ALTER TABLE `k8s_csi_cluster`
    ADD CONSTRAINT `fk_k8s_csi_services`
        FOREIGN KEY (`service_id`) REFERENCES `k8s_services` (`id`);

-- mapping table of nfs and cluster.
create table k8s_nfs_cluster
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `cluster_id`   bigint(11) unsigned NOT NULL,
    `service_id`        bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `k8s_nfs_cluster_unique` (`cluster_id`, `service_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_nfs_cluster`
    ADD CONSTRAINT `fk_k8s_nfs_cluster`
        FOREIGN KEY (`cluster_id`) REFERENCES `k8s_cluster` (`id`);

ALTER TABLE `k8s_nfs_cluster`
    ADD CONSTRAINT `fk_k8s_nfs_services`
        FOREIGN KEY (`service_id`) REFERENCES `k8s_services` (`id`);


-- mapping table of object storage and namespace..
create table k8s_object_storage_namespace
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `namespace_id`   bigint(11) unsigned NOT NULL,
    `service_id`        bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `k8s_object_storage_namespace_unique` (`namespace_id`, `service_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_object_storage_namespace`
    ADD CONSTRAINT `fk_k8s_object_storage_namespace`
        FOREIGN KEY (`namespace_id`) REFERENCES `k8s_namespace` (`id`);

ALTER TABLE `k8s_object_storage_namespace`
    ADD CONSTRAINT `fk_k8s_object_storage_namespace_service`
        FOREIGN KEY (`service_id`) REFERENCES `k8s_services` (`id`);

-- mapping table of object storage operator and cluster.
create table k8s_object_storage_operator_cluster
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `cluster_id`   bigint(11) unsigned NOT NULL,
    `service_id`        bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `k8s_object_storage_operator_cluster_unique` (`cluster_id`, `service_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_object_storage_operator_cluster`
    ADD CONSTRAINT `fk_k8s_object_storage_operator_cluster`
        FOREIGN KEY (`cluster_id`) REFERENCES `k8s_cluster` (`id`);

ALTER TABLE `k8s_object_storage_operator_cluster`
    ADD CONSTRAINT `fk_k8s_object_storage_operator_cluster_svc`
        FOREIGN KEY (`service_id`) REFERENCES `k8s_services` (`id`);


-- mapping table of ingress_controller and cluster.
create table k8s_ingress_controller_cluster
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `cluster_id`   bigint(11) unsigned NOT NULL,
    `service_id`        bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `k8s_ingress_controller_cluster_unique` (`cluster_id`, `service_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_ingress_controller_cluster`
    ADD CONSTRAINT `fk_k8s_ingress_controller_cluster`
        FOREIGN KEY (`cluster_id`) REFERENCES `k8s_cluster` (`id`);

ALTER TABLE `k8s_ingress_controller_cluster`
    ADD CONSTRAINT `fk_k8s_ingress_controller_cluster_svc`
        FOREIGN KEY (`service_id`) REFERENCES `k8s_services` (`id`);

-- mapping table of load_balancer and cluster.
create table k8s_load_balancer_cluster
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `cluster_id`   bigint(11) unsigned NOT NULL,
    `service_id`        bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `k8s_load_balancer_cluster_unique` (`cluster_id`, `service_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_load_balancer_cluster`
    ADD CONSTRAINT `fk_k8s_load_balancer_cluster`
        FOREIGN KEY (`cluster_id`) REFERENCES `k8s_cluster` (`id`);

ALTER TABLE `k8s_load_balancer_cluster`
    ADD CONSTRAINT `fk_k8s_load_balancer_cluster_svc`
        FOREIGN KEY (`service_id`) REFERENCES `k8s_services` (`id`);


-- mapping table of hive metastore and namespace..
create table k8s_hive_metastore_namespace
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `namespace_id`   bigint(11) unsigned NOT NULL,
    `service_id`        bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `k8s_hive_metastore_namespace_unique` (`namespace_id`, `service_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_hive_metastore_namespace`
    ADD CONSTRAINT `fk_k8s_hive_metastore_namespace`
        FOREIGN KEY (`namespace_id`) REFERENCES `k8s_namespace` (`id`);

ALTER TABLE `k8s_hive_metastore_namespace`
    ADD CONSTRAINT `fk_k8s_hive_metastore_namespace_service`
        FOREIGN KEY (`service_id`) REFERENCES `k8s_services` (`id`);

-- mapping table of cert manager and cluster.
create table k8s_cert_manager_cluster
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `cluster_id`   bigint(11) unsigned NOT NULL,
    `service_id`        bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `k8s_cert_manager_cluster_unique` (`cluster_id`, `service_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_cert_manager_cluster`
    ADD CONSTRAINT `fk_k8s_cert_manager_cluster`
        FOREIGN KEY (`cluster_id`) REFERENCES `k8s_cluster` (`id`);

ALTER TABLE `k8s_cert_manager_cluster`
    ADD CONSTRAINT `fk_k8s_cert_manager_cluster_services`
        FOREIGN KEY (`service_id`) REFERENCES `k8s_services` (`id`);

-- mapping table of spark thrift server and namespace..
create table k8s_spark_thrift_server_namespace
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `namespace_id`   bigint(11) unsigned NOT NULL,
    `service_id`        bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `k8s_spark_thrift_server_namespace_unique` (`namespace_id`, `service_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_spark_thrift_server_namespace`
    ADD CONSTRAINT `fk_k8s_spark_thrift_server_namespace`
        FOREIGN KEY (`namespace_id`) REFERENCES `k8s_namespace` (`id`);

ALTER TABLE `k8s_spark_thrift_server_namespace`
    ADD CONSTRAINT `fk_k8s_spark_thrift_server_namespace_service`
        FOREIGN KEY (`service_id`) REFERENCES `k8s_services` (`id`);

-- mapping table of presto and namespace.
create table k8s_presto_namespace
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `namespace_id`   bigint(11) unsigned NOT NULL,
    `service_id`        bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `k8s_k8s_presto_namespace_unique` (`namespace_id`, `service_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_presto_namespace`
    ADD CONSTRAINT `fk_k8s_presto_namespace`
        FOREIGN KEY (`namespace_id`) REFERENCES `k8s_namespace` (`id`);

ALTER TABLE `k8s_presto_namespace`
    ADD CONSTRAINT `fk_k8s_presto_namespace_service`
        FOREIGN KEY (`service_id`) REFERENCES `k8s_services` (`id`);

-- mapping table of redash and namespace.
create table k8s_redash_namespace
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `namespace_id`   bigint(11) unsigned NOT NULL,
    `service_id`        bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `k8s_k8s_redash_namespace_unique` (`namespace_id`, `service_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_redash_namespace`
    ADD CONSTRAINT `fk_k8s_redash_namespace`
        FOREIGN KEY (`namespace_id`) REFERENCES `k8s_namespace` (`id`);

ALTER TABLE `k8s_redash_namespace`
    ADD CONSTRAINT `fk_k8s_redash_namespace_service`
        FOREIGN KEY (`service_id`) REFERENCES `k8s_services` (`id`);

-- mapping table of jupyterhub and namespace.
create table k8s_jupyterhub_namespace
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `namespace_id`   bigint(11) unsigned NOT NULL,
    `service_id`        bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `k8s_k8s_jupyterhub_namespace_unique` (`namespace_id`, `service_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_jupyterhub_namespace`
    ADD CONSTRAINT `fk_k8s_jupyterhub_namespace`
        FOREIGN KEY (`namespace_id`) REFERENCES `k8s_namespace` (`id`);

ALTER TABLE `k8s_jupyterhub_namespace`
    ADD CONSTRAINT `fk_k8s_jupyterhub_namespace_service`
        FOREIGN KEY (`service_id`) REFERENCES `k8s_services` (`id`);


-- mapping table of kafka and namespace.
create table k8s_kafka_namespace
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `namespace_id`   bigint(11) unsigned NOT NULL,
    `service_id`        bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `k8s_k8s_kafka_namespace_unique` (`namespace_id`, `service_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_kafka_namespace`
    ADD CONSTRAINT `fk_k8s_kafka_namespace`
        FOREIGN KEY (`namespace_id`) REFERENCES `k8s_namespace` (`id`);

ALTER TABLE `k8s_kafka_namespace`
    ADD CONSTRAINT `fk_k8s_kafka_namespace_service`
        FOREIGN KEY (`service_id`) REFERENCES `k8s_services` (`id`);

-- mapping table of elasticsearch and namespace.
create table k8s_elasticsearch_namespace
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `namespace_id`   bigint(11) unsigned NOT NULL,
    `service_id`        bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `k8s_k8s_elasticsearch_namespace_unique` (`namespace_id`, `service_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_elasticsearch_namespace`
    ADD CONSTRAINT `fk_k8s_elasticsearch_namespace`
        FOREIGN KEY (`namespace_id`) REFERENCES `k8s_namespace` (`id`);

ALTER TABLE `k8s_elasticsearch_namespace`
    ADD CONSTRAINT `fk_k8s_elasticsearch_namespace_service`
        FOREIGN KEY (`service_id`) REFERENCES `k8s_services` (`id`);

-- mapping table of workflow and namespace..
create table k8s_workflow_namespace
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `namespace_id`   bigint(11) unsigned NOT NULL,
    `service_id`        bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `k8s_workflow_namespace_unique` (`namespace_id`, `service_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_workflow_namespace`
    ADD CONSTRAINT `fk_k8s_workflow_namespace`
        FOREIGN KEY (`namespace_id`) REFERENCES `k8s_namespace` (`id`);

ALTER TABLE `k8s_workflow_namespace`
    ADD CONSTRAINT `fk_k8s_workflow_namespace_service`
        FOREIGN KEY (`service_id`) REFERENCES `k8s_services` (`id`);


-- mapping table of rdb and namespace..
create table k8s_rdb_namespace
(
    `id`        bigint(11) unsigned NOT NULL AUTO_INCREMENT,
    `namespace_id`   bigint(11) unsigned NOT NULL,
    `service_id`        bigint(11) unsigned NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `k8s_rdb_namespace_unique` (`namespace_id`, `service_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

ALTER TABLE `k8s_rdb_namespace`
    ADD CONSTRAINT `fk_k8s_rdb_namespace`
        FOREIGN KEY (`namespace_id`) REFERENCES `k8s_namespace` (`id`);

ALTER TABLE `k8s_rdb_namespace`
    ADD CONSTRAINT `fk_k8s_rdb_namespace_service`
        FOREIGN KEY (`service_id`) REFERENCES `k8s_services` (`id`);