job "hive-metastore-mysql-server" {
  namespace = "hive-metastore"
  datacenters = ["dc1"]
  type        = "service"

  group "mysql-server" {
    count = 1

    volume "ceph-mysql" {
      type      = "csi"
      read_only = false
      source    = "hive-metastore-mysql"
    }

    network {
      port "db" {
        static = 3306
      }
    }

    restart {
      attempts = 10
      interval = "5m"
      delay    = "25s"
      mode     = "delay"
    }

    task "mysql-server" {
      driver = "docker"

      volume_mount {
        volume      = "ceph-mysql"
        destination = "/srv"
        read_only   = false
      }

      env {
        MYSQL_ROOT_PASSWORD = "password"
      }

      config {
        image = "mysql:5.7"
        args  = ["--datadir", "/srv/mysql"]
        ports = ["db"]
      }

      resources {
        cpu    = 500
        memory = 1024
      }

      service {
        name = "hive-metastore-mysql-server"
        port = "db"

        check {
          type     = "tcp"
          interval = "10s"
          timeout  = "2s"
        }
      }
    }
  }
}