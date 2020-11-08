package com.cloudcheflabs.dataroaster.apiserver.domain.model;

import com.google.common.collect.Sets;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "k8s_namespace")
public class K8sNamespace implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "namespace_name")
    private String namespaceName;

    @ManyToOne
    @JoinColumn(name ="cluster_id")
    private K8sCluster k8sCluster;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "k8s_namespace_groups",
            joinColumns = { @JoinColumn(name = "namespace_id") },
            inverseJoinColumns = { @JoinColumn(name = "group_id") }
    )
    private Set<Groups> groupsSet = new HashSet<>();

    @OneToMany(mappedBy = "k8sNamespace", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<K8sKubeconfigUser> k8sKubeconfigUserSet = Sets.newHashSet();

    @ManyToMany(mappedBy = "k8sNamespaceSet")
    private Set<K8sServices> k8sServicesSet = new HashSet<>();

    @ManyToMany(mappedBy = "hiveMetastoreK8sNamespaceSet")
    private Set<K8sServices> hiveMetastoreK8sServicesSet = new HashSet<>();

    @ManyToMany(mappedBy = "sparkThriftServerK8sNamespaceSet")
    private Set<K8sServices> sparkThriftServerK8sServicesSet = new HashSet<>();

    @ManyToMany(mappedBy = "prestoK8sNamespaceSet")
    private Set<K8sServices> prestoK8sServicesSet = new HashSet<>();

    @ManyToMany(mappedBy = "redashK8sNamespaceSet")
    private Set<K8sServices> redashK8sServicesSet = new HashSet<>();

    @ManyToMany(mappedBy = "jupyterHubK8sNamespaceSet")
    private Set<K8sServices> jupyterHubK8sServicesSet = new HashSet<>();


    @ManyToMany(mappedBy = "kafkaK8sNamespaceSet")
    private Set<K8sServices> kafkaK8sServicesSet = new HashSet<>();


    @ManyToMany(mappedBy = "elasticsearchK8sNamespaceSet")
    private Set<K8sServices> elasticsearchK8sServicesSet = new HashSet<>();


    public Set<K8sServices> getElasticsearchK8sServicesSet() {
        return elasticsearchK8sServicesSet;
    }

    public void setElasticsearchK8sServicesSet(Set<K8sServices> elasticsearchK8sServicesSet) {
        this.elasticsearchK8sServicesSet = elasticsearchK8sServicesSet;
    }

    public Set<K8sServices> getKafkaK8sServicesSet() {
        return kafkaK8sServicesSet;
    }

    public void setKafkaK8sServicesSet(Set<K8sServices> kafkaK8sServicesSet) {
        this.kafkaK8sServicesSet = kafkaK8sServicesSet;
    }

    public Set<K8sServices> getJupyterHubK8sServicesSet() {
        return jupyterHubK8sServicesSet;
    }

    public void setJupyterHubK8sServicesSet(Set<K8sServices> jupyterHubK8sServicesSet) {
        this.jupyterHubK8sServicesSet = jupyterHubK8sServicesSet;
    }

    public Set<K8sServices> getRedashK8sServicesSet() {
        return redashK8sServicesSet;
    }

    public void setRedashK8sServicesSet(Set<K8sServices> redashK8sServicesSet) {
        this.redashK8sServicesSet = redashK8sServicesSet;
    }

    public Set<K8sServices> getPrestoK8sServicesSet() {
        return prestoK8sServicesSet;
    }

    public void setPrestoK8sServicesSet(Set<K8sServices> prestoK8sServicesSet) {
        this.prestoK8sServicesSet = prestoK8sServicesSet;
    }

    public Set<K8sServices> getSparkThriftServerK8sServicesSet() {
        return sparkThriftServerK8sServicesSet;
    }

    public void setSparkThriftServerK8sServicesSet(Set<K8sServices> sparkThriftServerK8sServicesSet) {
        this.sparkThriftServerK8sServicesSet = sparkThriftServerK8sServicesSet;
    }

    public Set<K8sServices> getHiveMetastoreK8sServicesSet() {
        return hiveMetastoreK8sServicesSet;
    }

    public void setHiveMetastoreK8sServicesSet(Set<K8sServices> hiveMetastoreK8sServicesSet) {
        this.hiveMetastoreK8sServicesSet = hiveMetastoreK8sServicesSet;
    }

    public Set<K8sServices> getK8sServicesSet() {
        return k8sServicesSet;
    }

    public void setK8sServicesSet(Set<K8sServices> k8sServicesSet) {
        this.k8sServicesSet = k8sServicesSet;
    }

    public Set<K8sKubeconfigUser> getK8sKubeconfigUserSet() {
        return k8sKubeconfigUserSet;
    }

    public void setK8sKubeconfigUserSet(Set<K8sKubeconfigUser> k8sKubeconfigUserSet) {
        this.k8sKubeconfigUserSet = k8sKubeconfigUserSet;
    }

    public Set<Groups> getGroupsSet() {
        return groupsSet;
    }

    public void setGroupsSet(Set<Groups> groupsSet) {
        this.groupsSet = groupsSet;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNamespaceName() {
        return namespaceName;
    }

    public void setNamespaceName(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    public K8sCluster getK8sCluster() {
        return k8sCluster;
    }

    public void setK8sCluster(K8sCluster k8sCluster) {
        this.k8sCluster = k8sCluster;
    }
}
