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
        MINIO_ROOT_USER = "${NOMAD_VAR_minio_root_user}"
        MINIO_ROOT_PASSWORD = "${NOMAD_VAR_minio_root_password}"
      }
      config {
        image = "minio/minio:RELEASE.2021-03-01T04-20-55Z"
        force_pull = false
        args = [
          "server",
          "--address",
          ":${NOMAD_HOST_PORT_communication}",
          "http://minio-svc-0.service.consul:9999/srv/data{1...6}",
          "http://minio-svc-1.service.consul:9991/srv/data{1...6}",
          "http://minio-svc-2.service.consul:9992/srv/data{1...6}"
        ]
        network_mode = "host"
        ports = [
          "communication"
        ]
      }
      resources {
        cpu = 200
        memory = 2048
      }
      service {
        name = "minio-server-0"
        port = "communication"
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
        MINIO_ROOT_USER = "${NOMAD_VAR_minio_root_user}"
        MINIO_ROOT_PASSWORD = "${NOMAD_VAR_minio_root_password}"
      }
      config {
        image = "minio/minio:RELEASE.2021-03-01T04-20-55Z"
        force_pull = false
        args = [
          "server",
          "--address",
          ":${NOMAD_HOST_PORT_communication}",
          "http://minio-svc-1.service.consul:9991/srv/data{1...6}",
          "http://minio-svc-0.service.consul:9999/srv/data{1...6}",
          "http://minio-svc-2.service.consul:9992/srv/data{1...6}"
        ]
        network_mode = "host"
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
        MINIO_ROOT_USER = "${NOMAD_VAR_minio_root_user}"
        MINIO_ROOT_PASSWORD = "${NOMAD_VAR_minio_root_password}"
      }
      config {
        image = "minio/minio:RELEASE.2021-03-01T04-20-55Z"
        force_pull = false
        args = [
          "server",
          "--address",
          ":${NOMAD_HOST_PORT_communication}",
          "http://minio-svc-2.service.consul:9992/srv/data{1...6}",
          "http://minio-svc-0.service.consul:9999/srv/data{1...6}",
          "http://minio-svc-1.service.consul:9991/srv/data{1...6}"
        ]
        network_mode = "host"
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
      }
    }
  }
  ##################################### minio-svc-0 ############################################
  group "nginx-0" {
    count = 1

    network {
      port "http" {
        static = 9999
      }
    }

    service {
      name = "minio-svc-0"
      port = "http"
    }

    task "nginx" {
      driver = "docker"

      config {
        image = "nginx"

        ports = ["http"]

        volumes = [
          "local:/etc/nginx/conf.d",
        ]
      }

      template {
        data = <<EOF
upstream backend {
{{ range service "minio-server-0" }}
  server {{ .Address }}:{{ .Port }};
{{ else }}server 127.0.0.1:65535; # force a 502
{{ end }}
}

server {
   listen 9999;
   ignore_invalid_headers off;
   client_max_body_size 0;
   proxy_buffering off;

   location / {
      proxy_set_header X-Real-IP $remote_addr;
      proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
      proxy_set_header X-Forwarded-Proto $scheme;
      proxy_set_header Host $http_host;
      proxy_connect_timeout 300;
      proxy_http_version 1.1;
      proxy_set_header Connection "";
      chunked_transfer_encoding off;
      proxy_pass http://backend;
   }
}
EOF

        destination   = "local/load-balancer.conf"
        change_mode   = "signal"
        change_signal = "SIGHUP"
      }
    }
  }
  ##################################### minio-svc-1 ############################################
  group "nginx-1" {
    count = 1

    network {
      port "http" {
        static = 9991
      }
    }

    service {
      name = "minio-svc-1"
      port = "http"
    }

    task "nginx" {
      driver = "docker"

      config {
        image = "nginx"

        ports = ["http"]

        volumes = [
          "local:/etc/nginx/conf.d",
        ]
      }

      template {
        data = <<EOF
upstream backend {
{{ range service "minio-server-1" }}
  server {{ .Address }}:{{ .Port }};
{{ else }}server 127.0.0.1:65535; # force a 502
{{ end }}
}

server {
   listen 9991;
   ignore_invalid_headers off;
   client_max_body_size 0;
   proxy_buffering off;

   location / {
      proxy_set_header X-Real-IP $remote_addr;
      proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
      proxy_set_header X-Forwarded-Proto $scheme;
      proxy_set_header Host $http_host;
      proxy_connect_timeout 300;
      proxy_http_version 1.1;
      proxy_set_header Connection "";
      chunked_transfer_encoding off;
      proxy_pass http://backend;
   }
}
EOF

        destination   = "local/load-balancer.conf"
        change_mode   = "signal"
        change_signal = "SIGHUP"
      }
    }
  }
  ##################################### minio-svc-2 ############################################
  group "nginx-2" {
    count = 1

    network {
      port "http" {
        static = 9992
      }
    }

    service {
      name = "minio-svc-2"
      port = "http"
    }

    task "nginx" {
      driver = "docker"

      config {
        image = "nginx"

        ports = ["http"]

        volumes = [
          "local:/etc/nginx/conf.d",
        ]
      }

      template {
        data = <<EOF
upstream backend {
{{ range service "minio-server-2" }}
  server {{ .Address }}:{{ .Port }};
{{ else }}server 127.0.0.1:65535; # force a 502
{{ end }}
}

server {
   listen 9992;
   ignore_invalid_headers off;
   client_max_body_size 0;
   proxy_buffering off;

   location / {
      proxy_set_header X-Real-IP $remote_addr;
      proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
      proxy_set_header X-Forwarded-Proto $scheme;
      proxy_set_header Host $http_host;
      proxy_connect_timeout 300;
      proxy_http_version 1.1;
      proxy_set_header Connection "";
      chunked_transfer_encoding off;
      proxy_pass http://backend;
   }
}
EOF

        destination   = "local/load-balancer.conf"
        change_mode   = "signal"
        change_signal = "SIGHUP"
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
      config {
        image = "minio/mc:RELEASE.2021-05-18T03-39-44Z"
        force_pull = false
        entrypoint = [
          "/bin/sh"
        ]
      }
      resources {
        cpu = 100
        memory = 1024
      }
    }
  }
}