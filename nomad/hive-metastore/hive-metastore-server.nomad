job "hive-metastore-server" {
  namespace = "hive-metastore"
  datacenters = ["dc1"]
  type = "service"
  group "hive-metastore" {
    count = 1
    network {
      port "hms" {
        static = 9083
      }
    }
    restart {
      attempts = 10
      interval = "5m"
      delay    = "25s"
      mode     = "delay"
    }
    task "hive-metastore-server" {
      template {
        data        = <<EOF
<configuration>
    <property>
        <name>fs.s3a.connection.ssl.enabled</name>
	    <value>true</value>
    </property>
    <property>
        <name>fs.defaultFS</name>
        <value>s3a://mykidong</value>
    </property>
    <property>
        <name>fs.s3a.path.style.access</name>
        <value>true</value>
    </property>
    <property>
        <name>fs.s3a.access.key</name>
        <value>cclminio</value>
    </property>
    <property>
        <name>fs.s3a.secret.key</name>
        <value>rhksflja!@#</value>
    </property>
    <property>
        <name>fs.s3a.impl</name>
        <value>org.apache.hadoop.fs.s3a.S3AFileSystem</value>
    </property>
    <property>
        <name>fs.s3a.endpoint</name>
        <value>https://nginx-test.cloudchef-labs.com</value>
    </property>
    <property>
        <name>fs.s3a.fast.upload</name>
        <value>true</value>
    </property>
</configuration>
EOF
        destination = "local/core-site.xml"
        change_mode = "restart"
      }
      template {
        data        = <<EOF
<configuration>
	<property>
		<name>metastore.task.threads.always</name>
		<value>org.apache.hadoop.hive.metastore.events.EventCleanerTask</value>
	</property>
	<property>
		<name>metastore.expression.proxy</name>
		<value>org.apache.hadoop.hive.metastore.DefaultPartitionExpressionProxy</value>
	</property>
	<property>
		<name>javax.jdo.option.ConnectionDriverName</name>
		<value>com.mysql.jdbc.Driver</value>
	</property>
	<property>
		<name>javax.jdo.option.ConnectionURL</name>
		<value>jdbc:mysql://hive-metastore-mysql-server.service.consul:3306/metastore_db?useSSL=false</value>
	</property>
	<property>
		<name>javax.jdo.option.ConnectionUserName</name>
		<value>root</value>
	</property>
	<property>
		<name>javax.jdo.option.ConnectionPassword</name>
		<value>password</value>
	</property>
	<property>
		<name>metastore.warehouse.dir</name>
		<value>s3a://mykidong/warehouse/</value>
	</property>
	<property>
		<name>metastore.thrift.port</name>
		<value>9083</value>
	</property>
</configuration>
EOF
        destination = "local/metastore-site.xml"
        change_mode = "restart"
      }
      env {
        AWS_ACCESS_KEY_ID = "cclminio"
        AWS_SECRET_ACCESS_KEY = "rhksflja!@#"
      }
      driver = "docker"
      config {
        image = "mykidong/hivemetastore:v3.0.0"
        volumes = [
          "./local/core-site.xml:/opt/hadoop/etc/hadoop/core-site.xml",
          "./local/metastore-site.xml:/opt/hive-metastore/conf/metastore-site.xml"
        ]
        command = "/opt/hive-metastore/bin/start-metastore"
        args = ["-p", "9083"]
        ports = [
          "hms",
        ]
      }
      resources {
        cpu    = 500
        memory = 256
      }
      service {
        name = "hive-metastore"
        port = "hms"
        check {
          type     = "tcp"
          interval = "10s"
          timeout  = "2s"
        }
      }
    }
  }
}