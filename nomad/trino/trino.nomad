job "trino" {
  namespace = "trino"
  datacenters = [
    "dc1"]
  type = "service"
  update {
    max_parallel = 1
    health_check = "checks"
    min_healthy_time = "30s"
    healthy_deadline = "5m"
    auto_revert = true
    canary = 0
    stagger = "30s"
  }
  ##################################### coordinator ############################################
  group "coordinator" {
    count = 1
    restart {
      attempts = 3
      delay = "30s"
      interval = "5m"
      mode = "fail"
    }
    network {
      port "http" {
      }
    }
    task "trino-server" {
      driver = "docker"
      kill_timeout = "300s"
      kill_signal = "SIGTERM"
      template {
        data = <<EOF
coordinator=true
node-scheduler.include-coordinator=false
http-server.http.port={{ env "NOMAD_HOST_PORT_http" }}
query.max-memory=5GB
query.max-memory-per-node=1GB
query.max-total-memory-per-node=2GB
query.max-stage-count=200
task.writer-count=4
discovery-server.enabled=true
discovery.uri=http://{{ env "NOMAD_IP_http" }}:{{ env "NOMAD_HOST_PORT_http" }}
EOF
        destination = "local/config.properties"
      }
      template {
        data = <<EOF
node.id={{ env "NOMAD_NAMESPACE" }}-{{ env "NOMAD_JOB_NAME" }}-{{ env "NOMAD_GROUP_NAME" }}-{{ env "NOMAD_ALLOC_ID" }}
node.environment=production
node.data-dir=/opt/trino-server/data
spiller-spill-path=/tmp
max-spill-per-node=4TB
query-max-spill-per-node=1TB
EOF
        destination = "local/node.properties"
      }
      template {
        data = <<EOF
-server
-Xmx16G
-XX:-UseBiasedLocking
-XX:+UseG1GC
-XX:G1HeapRegionSize=32M
-XX:+ExplicitGCInvokesConcurrent
-XX:+ExitOnOutOfMemoryError
-XX:+HeapDumpOnOutOfMemoryError
-XX:ReservedCodeCacheSize=512M
-XX:PerMethodRecompilationCutoff=10000
-XX:PerBytecodeRecompilationCutoff=10000
-Djdk.attach.allowAttachSelf=true
-Djdk.nio.maxCachedBufferSize=2000000
EOF
        destination = "local/jvm.config"
      }
      template {
        data = <<EOF
connector.name=hive-hadoop2
hive.metastore.uri=thrift://{{range $index, $element := service "hive-metastore"}}{{if eq $index 0}}{{ .Address }}:{{ .Port }}{{end}}{{end}}
hive.allow-drop-table=true
hive.max-partitions-per-scan=1000000
hive.compression-codec=NONE
hive.s3.endpoint=https://nginx-test.cloudchef-labs.com
hive.s3.path-style-access=true
hive.s3.ssl.enabled=true
hive.s3.max-connections=100
hive.s3.aws-access-key=cclminio
hive.s3.aws-secret-key=rhksflja!@#
EOF
        destination = "local/catalog/hive.properties"
      }
      config {
        image = "mykidong/trino:356"
        force_pull = false
        volumes = [
          "./local/config.properties:/opt/trino-server/etc/config.properties",
          "./local/node.properties:/opt/trino-server/etc/node.properties",
          "./local/jvm.config:/opt/trino-server/etc/jvm.config",
          "./local/catalog/hive.properties:/opt/trino-server/etc/catalog/hive.properties"
        ]
        command = "bin/launcher"
        args = [
          "run"
        ]
        ports = [
          "http"
        ]
        ulimit {
          nofile = "131072"
          nproc = "65536"
        }
      }
      resources {
        cpu = 200
        memory = 4096
      }
      service {
        name = "trino-coordinator"
        port = "http"
        check {
          name = "rest-tcp"
          type = "tcp"
          interval = "10s"
          timeout = "2s"
        }
      }
    }
  }
  ##################################### worker ############################################
  group "worker" {
    count = 3
    restart {
      attempts = 3
      delay = "30s"
      interval = "5m"
      mode = "fail"
    }
    network {
      port "http" {
      }
    }
    task "await-coordinator" {
      driver = "docker"
      config {
        image        = "busybox:1.28"
        command      = "sh"
        args         = ["-c", "echo -n 'Waiting for service'; until nslookup trino-coordinator.service.consul 2>&1 >/dev/null; do echo '.'; sleep 2; done"]
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
    task "trino-server" {
      driver = "docker"
      kill_timeout = "300s"
      kill_signal = "SIGTERM"
      template {
        data = <<EOF
coordinator=false
http-server.http.port={{ env "NOMAD_HOST_PORT_http" }}
query.max-memory=5GB
query.max-memory-per-node=1GB
query.max-total-memory-per-node=2GB
query.max-stage-count=200
task.writer-count=4
discovery.uri=http://{{range $index, $element := service "trino-coordinator"}}{{if eq $index 0}}{{ .Address }}:{{ .Port }}{{end}}{{end}}
EOF
        destination = "local/config.properties"
      }
      template {
        data = <<EOF
node.id={{ env "NOMAD_NAMESPACE" }}-{{ env "NOMAD_JOB_NAME" }}-{{ env "NOMAD_GROUP_NAME" }}-{{ env "NOMAD_ALLOC_ID" }}
node.environment=production
node.data-dir=/opt/trino-server/data
spiller-spill-path=/tmp
max-spill-per-node=4TB
query-max-spill-per-node=1TB
EOF
        destination = "local/node.properties"
      }
      template {
        data = <<EOF
-server
-Xmx16G
-XX:-UseBiasedLocking
-XX:+UseG1GC
-XX:G1HeapRegionSize=32M
-XX:+ExplicitGCInvokesConcurrent
-XX:+ExitOnOutOfMemoryError
-XX:+HeapDumpOnOutOfMemoryError
-XX:ReservedCodeCacheSize=512M
-XX:PerMethodRecompilationCutoff=10000
-XX:PerBytecodeRecompilationCutoff=10000
-Djdk.attach.allowAttachSelf=true
-Djdk.nio.maxCachedBufferSize=2000000
EOF
        destination = "local/jvm.config"
      }
      template {
        data = <<EOF
connector.name=hive-hadoop2
hive.metastore.uri=thrift://{{range $index, $element := service "hive-metastore"}}{{if eq $index 0}}{{ .Address }}:{{ .Port }}{{end}}{{end}}
hive.allow-drop-table=true
hive.max-partitions-per-scan=1000000
hive.compression-codec=NONE
hive.s3.endpoint=https://nginx-test.cloudchef-labs.com
hive.s3.path-style-access=true
hive.s3.ssl.enabled=true
hive.s3.max-connections=100
hive.s3.aws-access-key=cclminio
hive.s3.aws-secret-key=rhksflja!@#
EOF
        destination = "local/catalog/hive.properties"
      }
      config {
        image = "mykidong/trino:356"
        force_pull = false
        volumes = [
          "./local/config.properties:/opt/trino-server/etc/config.properties",
          "./local/node.properties:/opt/trino-server/etc/node.properties",
          "./local/jvm.config:/opt/trino-server/etc/jvm.config",
          "./local/catalog/hive.properties:/opt/trino-server/etc/catalog/hive.properties"
        ]
        command = "bin/launcher"
        args = [
          "run"
        ]
        ports = [
          "http"
        ]
        ulimit {
          nofile = "131072"
          nproc = "65536"
        }
      }
      resources {
        cpu = 200
        memory = 4096
      }
      service {
        name = "trino-worker"
        port = "http"
        check {
          name = "rest-tcp"
          type = "tcp"
          interval = "10s"
          timeout = "2s"
        }
      }
    }
  }
  ##################################### cli ############################################
  group "cli" {
    count = 1
    restart {
      attempts = 3
      delay = "30s"
      interval = "5m"
      mode = "fail"
    }
    task "await-coordinator" {
      driver = "docker"
      config {
        image        = "busybox:1.28"
        command      = "sh"
        args         = ["-c", "echo -n 'Waiting for service'; until nslookup trino-coordinator.service.consul 2>&1 >/dev/null; do echo '.'; sleep 2; done"]
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
    task "trino-cli" {
      driver = "docker"
      kill_timeout = "300s"
      kill_signal = "SIGTERM"
      config {
        image = "mykidong/trino-cli:356"
        force_pull = false
        command = "tail"
        args = [
          "-f",
          "/dev/null"
        ]
      }
      resources {
        cpu = 100
        memory = 256
      }
    }
  }
}