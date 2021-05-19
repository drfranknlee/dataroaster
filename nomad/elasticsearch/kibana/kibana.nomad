job "kibana" {
  namespace = "elasticsearch"
  datacenters = ["dc1"]
  type        = "service"
  update {
    max_parallel     = 1
    health_check     = "checks"
    min_healthy_time = "30s"
    healthy_deadline = "5m"
    auto_revert      = true
    canary           = 0
    stagger          = "30s"
  }
  group "kibana-server" {
    count = 1
    restart {
      attempts = 3
      delay = "30s"
      interval = "5m"
      mode = "fail"
    }
    network {
      port "http" {
        static = 5601
      }
    }
    volume "ceph-volume" {
      type = "csi"
      read_only = false
      source = "es-kibana"
    }
    task "await-es-req" {
      driver = "docker"
      config {
        image        = "busybox:1.28"
        command      = "sh"
        args         = ["-c", "echo -n 'Waiting for service'; until nslookup es-req.service.consul 2>&1 >/dev/null; do echo '.'; sleep 2; done"]
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
    task "kibana" {
      driver = "docker"
      kill_timeout = "300s"
      kill_signal = "SIGTERM"
      volume_mount {
        volume      = "ceph-volume"
        destination = "/srv"
        read_only   = false
      }
      template {
        data = <<EOF
elasticsearch:
  hosts:
    - http://{{with index (service "es-req") 0}}{{ .Address }}:{{ .Port }}{{ end }}
path:
  data: /srv/data
EOF
        destination = "local/kibana.yml"
      }
      config {
        image = "mykidong/kibana:7.12.1"
        force_pull = false
        volumes = [
          "./local/kibana.yml:/opt/kibana/config/kibana.yml",
        ]
        command = "bin/kibana"
        args = [
          "--host",
          "0.0.0.0",
          "--port",
          "${NOMAD_PORT_http}"
        ]
        ports = [
          "http"
        ]
        ulimit {
          memlock = "-1"
          nofile = "65536"
          nproc = "65536"
        }
      }
      resources {
        cpu = 100
        memory = 1024
      }
      service {
        name = "es-kibana-http"
        port = "http"
        check {
          name = "http-tcp"
          type = "tcp"
          interval = "10s"
          timeout = "2s"
        }
        check {
          name     = "http-http"
          type     = "http"
          path     = "/"
          interval = "5s"
          timeout  = "4s"
        }
      }
    }
  }
}