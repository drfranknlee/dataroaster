job "elasticsearch" {
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
  group "master-0" {
    count = 1
    restart {
      attempts = 3
      delay = "30s"
      interval = "5m"
      mode = "fail"
    }
    network {
      port "request" {
        to = 9200
      }
      port "communication" {
        to = 9300
      }
    }
    volume "ceph-volume" {
      type = "csi"
      read_only = false
      source = "es-master-0"
    }
    task "elasticsearch" {
      driver = "docker"
      kill_timeout = "300s"
      kill_signal = "SIGTERM"
      volume_mount {
        volume      = "ceph-volume"
        destination = "/srv"
        read_only   = false
      }
      env {
        ES_TMPDIR = "/opt/elasticsearch/temp"
      }
      template {
        data = <<EOF
cluster.name: my-cluster
node:
  name: es-master-0
  master: true
  data: true
  ingest: true
network:
  host: 0.0.0.0
discovery.seed_hosts:
  - {{ env "NOMAD_IP_communication" }}:{{ env "NOMAD_HOST_PORT_communication" }}
path:
  data:
    - /srv/data
  logs: /srv/log
bootstrap.memory_lock: true
indices.query.bool.max_clause_count: 10000
EOF
        destination = "local/elasticsearch.yml"
      }
      template {
        data = <<EOF
-Xms512m
-Xmx512m
8-13:-XX:+UseConcMarkSweepGC
8-13:-XX:CMSInitiatingOccupancyFraction=75
8-13:-XX:+UseCMSInitiatingOccupancyOnly
14-:-XX:+UseG1GC
-Djava.io.tmpdir=${ES_TMPDIR}
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=data
-XX:ErrorFile=logs/hs_err_pid%p.log
8:-XX:+PrintGCDetails
8:-XX:+PrintGCDateStamps
8:-XX:+PrintTenuringDistribution
8:-XX:+PrintGCApplicationStoppedTime
8:-Xloggc:logs/gc.log
8:-XX:+UseGCLogFileRotation
8:-XX:NumberOfGCLogFiles=32
8:-XX:GCLogFileSize=64m
9-:-Xlog:gc*,gc+age=trace,safepoint:file=logs/gc.log:utctime,pid,tags:filecount=32,filesize=64m
EOF
        destination = "local/jvm.options"
      }
      config {
        image = "mykidong/elasticsearch:7.12.1"
        force_pull = true
        volumes = [
          "./local/elasticsearch.yml:/opt/elasticsearch/config/elasticsearch.yml",
          "./local/jvm.options:/opt/elasticsearch/config/jvm.options"
        ]
        command = "bin/elasticsearch"
        args = [
          "-Enetwork.publish_host=${NOMAD_IP_request}",
          "-Ehttp.publish_port=${NOMAD_HOST_PORT_request}",
          "-Etransport.publish_port=${NOMAD_HOST_PORT_communication}"
        ]
        ports = [
          "request",
          "communication"
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
        name = "es-master-0-req"
        port = "request"
        check {
          name = "rest-tcp"
          type = "tcp"
          interval = "10s"
          timeout = "2s"
        }
        check {
          name     = "rest-http"
          type     = "http"
          path     = "/"
          interval = "5s"
          timeout  = "4s"
        }
      }
      service {
        name = "es-master-0-comm"
        port = "communication"
        check {
          type = "tcp"
          interval = "10s"
          timeout = "2s"
        }
      }
    }
  }
  group "master-1" {
    count = 1
    restart {
      attempts = 3
      delay = "30s"
      interval = "5m"
      mode = "fail"
    }
    network {
      port "request" {
        to = 9200
      }
      port "communication" {
        to = 9300
      }
    }
    volume "ceph-volume" {
      type = "csi"
      read_only = false
      source = "es-master-1"
    }
    task "await-es-master-0-comm" {
      driver = "docker"
      config {
        image        = "busybox:1.28"
        command      = "sh"
        args         = ["-c", "echo -n 'Waiting for service'; until nslookup es-master-0-comm.service.consul 2>&1 >/dev/null; do echo '.'; sleep 2; done"]
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
    task "elasticsearch" {
      driver = "docker"
      kill_timeout = "300s"
      kill_signal = "SIGTERM"
      volume_mount {
        volume      = "ceph-volume"
        destination = "/srv"
        read_only   = false
      }
      env {
        ES_TMPDIR = "/opt/elasticsearch/temp"
      }
      template {
        data = <<EOF
cluster.name: my-cluster
node:
  name: es-master-1
  master: true
  data: true
  ingest: true
network:
  host: 0.0.0.0
discovery.seed_hosts:
  - {{ range service "es-master-0-comm" }}{{ .Address }}:{{ .Port }}{{ end }}
path:
  data:
    - /srv/data
  logs: /srv/log
bootstrap.memory_lock: true
indices.query.bool.max_clause_count: 10000
EOF
        destination = "local/elasticsearch.yml"
      }
      template {
        data = <<EOF
-Xms512m
-Xmx512m
8-13:-XX:+UseConcMarkSweepGC
8-13:-XX:CMSInitiatingOccupancyFraction=75
8-13:-XX:+UseCMSInitiatingOccupancyOnly
14-:-XX:+UseG1GC
-Djava.io.tmpdir=${ES_TMPDIR}
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=data
-XX:ErrorFile=logs/hs_err_pid%p.log
8:-XX:+PrintGCDetails
8:-XX:+PrintGCDateStamps
8:-XX:+PrintTenuringDistribution
8:-XX:+PrintGCApplicationStoppedTime
8:-Xloggc:logs/gc.log
8:-XX:+UseGCLogFileRotation
8:-XX:NumberOfGCLogFiles=32
8:-XX:GCLogFileSize=64m
9-:-Xlog:gc*,gc+age=trace,safepoint:file=logs/gc.log:utctime,pid,tags:filecount=32,filesize=64m
EOF
        destination = "local/jvm.options"
      }
      config {
        image = "mykidong/elasticsearch:7.12.1"
        force_pull = true
        volumes = [
          "./local/elasticsearch.yml:/opt/elasticsearch/config/elasticsearch.yml",
          "./local/jvm.options:/opt/elasticsearch/config/jvm.options"
        ]
        command = "bin/elasticsearch"
        args = [
          "-Enetwork.publish_host=${NOMAD_IP_request}",
          "-Ehttp.publish_port=${NOMAD_HOST_PORT_request}",
          "-Etransport.publish_port=${NOMAD_HOST_PORT_communication}"
        ]
        ports = [
          "request",
          "communication"
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
        name = "es-master-1-req"
        port = "request"
        check {
          name = "rest-tcp"
          type = "tcp"
          interval = "10s"
          timeout = "2s"
        }
        check {
          name     = "rest-http"
          type     = "http"
          path     = "/"
          interval = "5s"
          timeout  = "4s"
        }
      }
      service {
        name = "es-master-1-comm"
        port = "communication"
        check {
          type = "tcp"
          interval = "10s"
          timeout = "2s"
        }
      }
    }
  }
  group "master-2" {
    count = 1
    restart {
      attempts = 3
      delay = "30s"
      interval = "5m"
      mode = "fail"
    }
    network {
      port "request" {
        to = 9200
      }
      port "communication" {
        to = 9300
      }
    }
    volume "ceph-volume" {
      type = "csi"
      read_only = false
      source = "es-master-2"
    }
    task "await-es-master-0-comm" {
      driver = "docker"
      config {
        image        = "busybox:1.28"
        command      = "sh"
        args         = ["-c", "echo -n 'Waiting for service'; until nslookup es-master-0-comm.service.consul 2>&1 >/dev/null; do echo '.'; sleep 2; done"]
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
    task "await-es-master-1-comm" {
      driver = "docker"
      config {
        image        = "busybox:1.28"
        command      = "sh"
        args         = ["-c", "echo -n 'Waiting for service'; until nslookup es-master-1-comm.service.consul 2>&1 >/dev/null; do echo '.'; sleep 2; done"]
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
    task "elasticsearch" {
      driver = "docker"
      kill_timeout = "300s"
      kill_signal = "SIGTERM"
      volume_mount {
        volume      = "ceph-volume"
        destination = "/srv"
        read_only   = false
      }
      env {
        ES_TMPDIR = "/opt/elasticsearch/temp"
      }
      template {
        data = <<EOF
cluster.name: my-cluster
node:
  name: es-master-2
  master: true
  data: true
  ingest: true
network:
  host: 0.0.0.0
discovery.seed_hosts:
  - {{ range service "es-master-0-comm" }}{{ .Address }}:{{ .Port }}{{ end }}
  - {{ range service "es-master-1-comm" }}{{ .Address }}:{{ .Port }}{{ end }}
path:
  data:
    - /srv/data
  logs: /srv/log
bootstrap.memory_lock: true
indices.query.bool.max_clause_count: 10000
EOF
        destination = "local/elasticsearch.yml"
      }
      template {
        data = <<EOF
-Xms512m
-Xmx512m
8-13:-XX:+UseConcMarkSweepGC
8-13:-XX:CMSInitiatingOccupancyFraction=75
8-13:-XX:+UseCMSInitiatingOccupancyOnly
14-:-XX:+UseG1GC
-Djava.io.tmpdir=${ES_TMPDIR}
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=data
-XX:ErrorFile=logs/hs_err_pid%p.log
8:-XX:+PrintGCDetails
8:-XX:+PrintGCDateStamps
8:-XX:+PrintTenuringDistribution
8:-XX:+PrintGCApplicationStoppedTime
8:-Xloggc:logs/gc.log
8:-XX:+UseGCLogFileRotation
8:-XX:NumberOfGCLogFiles=32
8:-XX:GCLogFileSize=64m
9-:-Xlog:gc*,gc+age=trace,safepoint:file=logs/gc.log:utctime,pid,tags:filecount=32,filesize=64m
EOF
        destination = "local/jvm.options"
      }
      config {
        image = "mykidong/elasticsearch:7.12.1"
        force_pull = true
        volumes = [
          "./local/elasticsearch.yml:/opt/elasticsearch/config/elasticsearch.yml",
          "./local/jvm.options:/opt/elasticsearch/config/jvm.options"
        ]
        command = "bin/elasticsearch"
        args = [
          "-Enetwork.publish_host=${NOMAD_IP_request}",
          "-Ehttp.publish_port=${NOMAD_HOST_PORT_request}",
          "-Etransport.publish_port=${NOMAD_HOST_PORT_communication}"
        ]
        ports = [
          "request",
          "communication"
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
        name = "es-master-2-req"
        port = "request"
        check {
          name = "rest-tcp"
          type = "tcp"
          interval = "10s"
          timeout = "2s"
        }
        check {
          name     = "rest-http"
          type     = "http"
          path     = "/"
          interval = "5s"
          timeout  = "4s"
        }
      }
      service {
        name = "es-master-2-comm"
        port = "communication"
        check {
          type = "tcp"
          interval = "10s"
          timeout = "2s"
        }
      }
    }
  }
}