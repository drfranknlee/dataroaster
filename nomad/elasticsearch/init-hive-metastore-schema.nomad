job "init-hive-metastore-schema" {
  namespace = "hive-metastore"
  datacenters = ["dc1"]
  type        = "batch"

  group "mysql-server" {
    task "init-schema" {
      driver = "docker"
      config {
        image = "mykidong/hivemetastore:v3.0.0"
        command = "/opt/hive-metastore/bin/schematool"
        args  = [
          "--verbose",
          "-initSchema",
          "-dbType",
          "mysql" ,
          "-userName",
          "root",
          "-passWord", "password",
          "-url",
          "jdbc:mysql://hive-metastore-mysql-server.service.consul:3306/metastore_db?createDatabaseIfNotExist=true&connectTimeout=1000"
        ]
      }
      resources {
        cpu    = 100
        memory = 256
      }
    }
  }
}