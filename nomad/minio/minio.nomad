variables {
  minio_root_user = "cclminio"
  minio_root_password = "rhksflja!@#"
}
job "minio" {
  namespace = "minio"
  datacenters = [
    "dc1"]
  type = "service"
  update {
    max_parallel      = 1
    health_check      = "checks"
    min_healthy_time  = "10s"
    healthy_deadline  = "12m"
    progress_deadline = "15m"
    canary = 0
    stagger = "30s"
  }
  ##################################### minio-0 ############################################
  group "minio-0" {
    count = 1
    restart {
      attempts = 3
      delay = "30s"
      interval = "5m"
      mode = "fail"
    }
    network {
      port "communication" {
      }
    }
    volume "ceph-volume" {
      type = "csi"
      read_only = false
      source = "minio-0"
    }
    task "minio" {
      driver = "docker"
      kill_timeout = "300s"
      kill_signal = "SIGTERM"
      volume_mount {
        volume = "ceph-volume"
        destination = "/srv"
        read_only = false
      }
      env {
        MINIO_ROOT_USER = var.minio_root_user
        MINIO_ROOT_PASSWORD = var.minio_root_password
      }
      config {
        image = "minio/minio:RELEASE.2021-03-01T04-20-55Z"
        force_pull = false
        args = [
          "server",
          "--address",
          ":${NOMAD_HOST_PORT_communication}",
          "http://localhost:${NOMAD_HOST_PORT_communication}/srv/data{1...6}"
        ]
        ports = [
          "communication"
        ]
      }
      resources {
        cpu = 100
        memory = 2048
      }
      service {
        name = "minio-server-0"
        port = "communication"
        check {
          name      = "minio-server-0-live"
          type      = "http"
          port      = "communication"
          path      = "/minio/health/live"
          interval  = "10s"
          timeout   = "2s"
        }
        check {
          name      = "minio-server-0-ready"
          type      = "http"
          port      = "communication"
          path      = "/minio/health/ready"
          interval  = "15s"
          timeout   = "4s"
        }
      }
    }
  }
  ##################################### minio-1 ############################################
  group "minio-1" {
    count = 1
    restart {
      attempts = 3
      delay = "30s"
      interval = "5m"
      mode = "fail"
    }
    network {
      port "communication" {
      }
    }
    volume "ceph-volume" {
      type = "csi"
      read_only = false
      source = "minio-1"
    }
    task "await-minio-server-0" {
      driver = "docker"
      config {
        image        = "busybox:1.28"
        command      = "sh"
        args         = ["-c", "echo -n 'Waiting for service'; until nslookup minio-server-0.service.consul 2>&1 >/dev/null; do echo '.'; sleep 2; done"]
        network_mode = "host"
      }
      resources {
        cpu    = 200
        memory = 128
      }
      lifecycle {
        hook    = "prestart"
        sidecar = false
      }
    }
    task "minio" {
      driver = "docker"
      kill_timeout = "300s"
      kill_signal = "SIGTERM"
      volume_mount {
        volume = "ceph-volume"
        destination = "/srv"
        read_only = false
      }
      env {
        MINIO_ROOT_USER = var.minio_root_user
        MINIO_ROOT_PASSWORD = var.minio_root_password
      }
      template {
        data = <<EOF
MINIO_SERVER_0 = {{range $index, $element := service "minio-server-0"}}{{if eq $index 0}}minio-server-0.service.consul:{{ .Port }}{{end}}{{end}}
EOF
        destination = "local/minio.env"
        env = true
        change_mode = "noop"
      }
      config {
        image = "minio/minio:RELEASE.2021-03-01T04-20-55Z"
        force_pull = false
        args = [
          "server",
          "--address",
          ":${NOMAD_HOST_PORT_communication}",
          "http://localhost:${NOMAD_HOST_PORT_communication}/srv/data{1...6}",
          "http://${MINIO_SERVER_0}/srv/data{1...6}"
        ]
        ports = [
          "communication"
        ]
      }
      resources {
        cpu = 200
        memory = 2048
      }
      service {
        name = "minio-server-1"
        port = "communication"
        check {
          name      = "minio-server-1-live"
          type      = "http"
          port      = "communication"
          path      = "/minio/health/live"
          interval  = "10s"
          timeout   = "2s"
        }
        check {
          name      = "minio-server-1-ready"
          type      = "http"
          port      = "communication"
          path      = "/minio/health/ready"
          interval  = "15s"
          timeout   = "4s"
        }
      }
    }
  }
  ##################################### minio-2 ############################################
  group "minio-2" {
    count = 1
    restart {
      attempts = 3
      delay = "30s"
      interval = "5m"
      mode = "fail"
    }
    network {
      port "communication" {
      }
    }
    volume "ceph-volume" {
      type = "csi"
      read_only = false
      source = "minio-2"
    }
    task "await-minio-server-0" {
      driver = "docker"
      config {
        image        = "busybox:1.28"
        command      = "sh"
        args         = ["-c", "echo -n 'Waiting for service'; until nslookup minio-server-0.service.consul 2>&1 >/dev/null; do echo '.'; sleep 2; done"]
        network_mode = "host"
      }
      resources {
        cpu    = 100
        memory = 128
      }
      lifecycle {
        hook    = "prestart"
        sidecar = false
      }
    }
    task "await-minio-server-1" {
      driver = "docker"
      config {
        image        = "busybox:1.28"
        command      = "sh"
        args         = ["-c", "echo -n 'Waiting for service'; until nslookup minio-server-1.service.consul 2>&1 >/dev/null; do echo '.'; sleep 2; done"]
        network_mode = "host"
      }
      resources {
        cpu    = 100
        memory = 128
      }
      lifecycle {
        hook    = "prestart"
        sidecar = false
      }
    }
    task "minio" {
      driver = "docker"
      kill_timeout = "300s"
      kill_signal = "SIGTERM"
      volume_mount {
        volume = "ceph-volume"
        destination = "/srv"
        read_only = false
      }
      env {
        MINIO_ROOT_USER = var.minio_root_user
        MINIO_ROOT_PASSWORD = var.minio_root_password
      }
      template {
        data = <<EOF
MINIO_SERVER_0 = {{range $index, $element := service "minio-server-0"}}{{if eq $index 0}}minio-server-0.service.consul:{{ .Port }}{{end}}{{end}}
MINIO_SERVER_1 = {{range $index, $element := service "minio-server-1"}}{{if eq $index 0}}minio-server-1.service.consul:{{ .Port }}{{end}}{{end}}
EOF
        destination = "local/minio.env"
        env = true
      }
      config {
        image = "minio/minio:RELEASE.2021-03-01T04-20-55Z"
        force_pull = false
        args = [
          "server",
          "--address",
          ":${NOMAD_HOST_PORT_communication}",
          "http://localhost:${NOMAD_HOST_PORT_communication}/srv/data{1...6}",
          "http://${MINIO_SERVER_0}/srv/data{1...6}",
          "http://${MINIO_SERVER_1}/srv/data{1...6}"
        ]
        ports = [
          "communication"
        ]
      }
      resources {
        cpu = 200
        memory = 2048
      }
      service {
        name = "minio-server-2"
        port = "communication"
        check {
          name      = "minio-server-2-live"
          type      = "http"
          port      = "communication"
          path      = "/minio/health/live"
          interval  = "10s"
          timeout   = "2s"
        }
        check {
          name      = "minio-server-2-ready"
          type      = "http"
          port      = "communication"
          path      = "/minio/health/ready"
          interval  = "15s"
          timeout   = "4s"
        }
      }
    }
  }
  ##################################### mc ############################################
  group "mc" {
    count = 1
    restart {
      attempts = 3
      delay = "30s"
      interval = "5m"
      mode = "fail"
    }
    task "minio-cli" {
      driver = "docker"
      kill_timeout = "300s"
      kill_signal = "SIGTERM"
      config {
        image = "minio/mc:RELEASE.2021-02-14T04-28-06Z"
        force_pull = false
        command = "tail"
        args = [
          "-f",
          "/dev/null"
        ]
      }
      resources {
        cpu = 100
        memory = 1024
      }
    }
  }
}