package com.cloudcheflabs.dataroaster.apiserver.domain.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "k8s_services")
public class K8sServices implements Serializable {

    public static enum ServiceTypeEnum {
        CSI,
        INGRESS_CONTROLLER,
        LOAD_BALANCER,
        OBJECT_STORAGE,
        OBJECT_STORAGE_OPERATOR,
        NFS,
        KAFKA,
        PRESTO,
        HIVE_METASTORE,
        CERT_MANAGER,
        REDASH,
        JUPYTERHUB,
        ELASTICSEARCH,
        SPARK_THRIFT_SERVER;
    }

    public static enum ObjectStorageNameEnum {
        MinIO,
        Ozone
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "type")
    private String type;

    @Column(name = "name")
    private String name;

    @Column(name = "version")
    private String version;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "k8s_csi_cluster",
            joinColumns = { @JoinColumn(name = "service_id") },
            inverseJoinColumns = { @JoinColumn(name = "cluster_id") }
    )
    private Set<K8sCluster> k8sClusterSet = new HashSet<>();


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "k8s_nfs_cluster",
            joinColumns = { @JoinColumn(name = "service_id") },
            inverseJoinColumns = { @JoinColumn(name = "cluster_id") }
    )
    private Set<K8sCluster> nfsK8sClusterSet = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "k8s_object_storage_namespace",
            joinColumns = { @JoinColumn(name = "service_id") },
            inverseJoinColumns = { @JoinColumn(name = "namespace_id") }
    )
    private Set<K8sNamespace> k8sNamespaceSet = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "k8s_object_storage_operator_cluster",
            joinColumns = { @JoinColumn(name = "service_id") },
            inverseJoinColumns = { @JoinColumn(name = "cluster_id") }
    )
    private Set<K8sCluster> objectStorageOperatorK8sClusterSet = new HashSet<>();


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "k8s_ingress_controller_cluster",
            joinColumns = { @JoinColumn(name = "service_id") },
            inverseJoinColumns = { @JoinColumn(name = "cluster_id") }
    )
    private Set<K8sCluster> ingressControllerK8sClusterSet = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "k8s_load_balancer_cluster",
            joinColumns = { @JoinColumn(name = "service_id") },
            inverseJoinColumns = { @JoinColumn(name = "cluster_id") }
    )
    private Set<K8sCluster> loadBalancerK8sClusterSet = new HashSet<>();


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "k8s_hive_metastore_namespace",
            joinColumns = { @JoinColumn(name = "service_id") },
            inverseJoinColumns = { @JoinColumn(name = "namespace_id") }
    )
    private Set<K8sNamespace> hiveMetastoreK8sNamespaceSet = new HashSet<>();


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "k8s_cert_manager_cluster",
            joinColumns = { @JoinColumn(name = "service_id") },
            inverseJoinColumns = { @JoinColumn(name = "cluster_id") }
    )
    private Set<K8sCluster> certManagerK8sClusterSet = new HashSet<>();


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "k8s_spark_thrift_server_namespace",
            joinColumns = { @JoinColumn(name = "service_id") },
            inverseJoinColumns = { @JoinColumn(name = "namespace_id") }
    )
    private Set<K8sNamespace> sparkThriftServerK8sNamespaceSet = new HashSet<>();


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "k8s_presto_namespace",
            joinColumns = { @JoinColumn(name = "service_id") },
            inverseJoinColumns = { @JoinColumn(name = "namespace_id") }
    )
    private Set<K8sNamespace> prestoK8sNamespaceSet = new HashSet<>();


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "k8s_redash_namespace",
            joinColumns = { @JoinColumn(name = "service_id") },
            inverseJoinColumns = { @JoinColumn(name = "namespace_id") }
    )
    private Set<K8sNamespace> redashK8sNamespaceSet = new HashSet<>();


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "k8s_jupyterhub_namespace",
            joinColumns = { @JoinColumn(name = "service_id") },
            inverseJoinColumns = { @JoinColumn(name = "namespace_id") }
    )
    private Set<K8sNamespace> jupyterHubK8sNamespaceSet = new HashSet<>();


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "k8s_kafka_namespace",
            joinColumns = { @JoinColumn(name = "service_id") },
            inverseJoinColumns = { @JoinColumn(name = "namespace_id") }
    )
    private Set<K8sNamespace> kafkaK8sNamespaceSet = new HashSet<>();


    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "k8s_elasticsearch_namespace",
            joinColumns = { @JoinColumn(name = "service_id") },
            inverseJoinColumns = { @JoinColumn(name = "namespace_id") }
    )
    private Set<K8sNamespace> elasticsearchK8sNamespaceSet = new HashSet<>();

    public Set<K8sNamespace> getElasticsearchK8sNamespaceSet() {
        return elasticsearchK8sNamespaceSet;
    }

    public void setElasticsearchK8sNamespaceSet(Set<K8sNamespace> elasticsearchK8sNamespaceSet) {
        this.elasticsearchK8sNamespaceSet = elasticsearchK8sNamespaceSet;
    }

    public Set<K8sNamespace> getKafkaK8sNamespaceSet() {
        return kafkaK8sNamespaceSet;
    }

    public void setKafkaK8sNamespaceSet(Set<K8sNamespace> kafkaK8sNamespaceSet) {
        this.kafkaK8sNamespaceSet = kafkaK8sNamespaceSet;
    }

    public Set<K8sNamespace> getJupyterHubK8sNamespaceSet() {
        return jupyterHubK8sNamespaceSet;
    }

    public void setJupyterHubK8sNamespaceSet(Set<K8sNamespace> jupyterHubK8sNamespaceSet) {
        this.jupyterHubK8sNamespaceSet = jupyterHubK8sNamespaceSet;
    }

    public Set<K8sNamespace> getRedashK8sNamespaceSet() {
        return redashK8sNamespaceSet;
    }

    public void setRedashK8sNamespaceSet(Set<K8sNamespace> redashK8sNamespaceSet) {
        this.redashK8sNamespaceSet = redashK8sNamespaceSet;
    }

    public Set<K8sNamespace> getPrestoK8sNamespaceSet() {
        return prestoK8sNamespaceSet;
    }

    public void setPrestoK8sNamespaceSet(Set<K8sNamespace> prestoK8sNamespaceSet) {
        this.prestoK8sNamespaceSet = prestoK8sNamespaceSet;
    }

    public Set<K8sNamespace> getSparkThriftServerK8sNamespaceSet() {
        return sparkThriftServerK8sNamespaceSet;
    }

    public void setSparkThriftServerK8sNamespaceSet(Set<K8sNamespace> sparkThriftServerK8sNamespaceSet) {
        this.sparkThriftServerK8sNamespaceSet = sparkThriftServerK8sNamespaceSet;
    }

    public Set<K8sCluster> getCertManagerK8sClusterSet() {
        return certManagerK8sClusterSet;
    }

    public void setCertManagerK8sClusterSet(Set<K8sCluster> certManagerK8sClusterSet) {
        this.certManagerK8sClusterSet = certManagerK8sClusterSet;
    }

    public Set<K8sNamespace> getHiveMetastoreK8sNamespaceSet() {
        return hiveMetastoreK8sNamespaceSet;
    }

    public void setHiveMetastoreK8sNamespaceSet(Set<K8sNamespace> hiveMetastoreK8sNamespaceSet) {
        this.hiveMetastoreK8sNamespaceSet = hiveMetastoreK8sNamespaceSet;
    }

    public Set<K8sCluster> getLoadBalancerK8sClusterSet() {
        return loadBalancerK8sClusterSet;
    }

    public void setLoadBalancerK8sClusterSet(Set<K8sCluster> loadBalancerK8sClusterSet) {
        this.loadBalancerK8sClusterSet = loadBalancerK8sClusterSet;
    }

    public Set<K8sCluster> getIngressControllerK8sClusterSet() {
        return ingressControllerK8sClusterSet;
    }

    public void setIngressControllerK8sClusterSet(Set<K8sCluster> ingressControllerK8sClusterSet) {
        this.ingressControllerK8sClusterSet = ingressControllerK8sClusterSet;
    }

    public Set<K8sCluster> getObjectStorageOperatorK8sClusterSet() {
        return objectStorageOperatorK8sClusterSet;
    }

    public void setObjectStorageOperatorK8sClusterSet(Set<K8sCluster> objectStorageOperatorK8sClusterSet) {
        this.objectStorageOperatorK8sClusterSet = objectStorageOperatorK8sClusterSet;
    }

    public Set<K8sNamespace> getK8sNamespaceSet() {
        return k8sNamespaceSet;
    }

    public void setK8sNamespaceSet(Set<K8sNamespace> k8sNamespaceSet) {
        this.k8sNamespaceSet = k8sNamespaceSet;
    }

    public Set<K8sCluster> getNfsK8sClusterSet() {
        return nfsK8sClusterSet;
    }

    public void setNfsK8sClusterSet(Set<K8sCluster> nfsK8sClusterSet) {
        this.nfsK8sClusterSet = nfsK8sClusterSet;
    }

    public Set<K8sCluster> getK8sClusterSet() {
        return k8sClusterSet;
    }

    public void setK8sClusterSet(Set<K8sCluster> k8sClusterSet) {
        this.k8sClusterSet = k8sClusterSet;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
