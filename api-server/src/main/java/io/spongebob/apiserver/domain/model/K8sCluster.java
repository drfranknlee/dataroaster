package io.spongebob.apiserver.domain.model;

import com.google.common.collect.Sets;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "k8s_cluster")
public class K8sCluster implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "cluster_name")
    private String clusterName;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "k8sCluster", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<K8sNamespace> k8sNamespaceSet = Sets.newHashSet();


    @OneToMany(mappedBy = "k8sCluster", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<K8sKubeconfigAdmin> k8sKubeconfigAdminSet = Sets.newHashSet();

    @ManyToMany(mappedBy = "k8sClusterSet")
    private Set<K8sServices> k8sServicesSet = new HashSet<>();

    @ManyToMany(mappedBy = "nfsK8sClusterSet")
    private Set<K8sServices> nfsK8sServicesSet = new HashSet<>();


    @ManyToMany(mappedBy = "objectStorageOperatorK8sClusterSet")
    private Set<K8sServices> objectStorageOperatorK8sServicesSet = new HashSet<>();

    @ManyToMany(mappedBy = "ingressControllerK8sClusterSet")
    private Set<K8sServices> ingressControllerK8sServicesSet = new HashSet<>();

    @ManyToMany(mappedBy = "loadBalancerK8sClusterSet")
    private Set<K8sServices> loadBalancerK8sServicesSet = new HashSet<>();

    @ManyToMany(mappedBy = "certManagerK8sClusterSet")
    private Set<K8sServices> certManagerK8sServicesSet = new HashSet<>();

    public Set<K8sServices> getCertManagerK8sServicesSet() {
        return certManagerK8sServicesSet;
    }

    public void setCertManagerK8sServicesSet(Set<K8sServices> certManagerK8sServicesSet) {
        this.certManagerK8sServicesSet = certManagerK8sServicesSet;
    }

    public Set<K8sServices> getLoadBalancerK8sServicesSet() {
        return loadBalancerK8sServicesSet;
    }

    public void setLoadBalancerK8sServicesSet(Set<K8sServices> loadBalancerK8sServicesSet) {
        this.loadBalancerK8sServicesSet = loadBalancerK8sServicesSet;
    }

    public Set<K8sServices> getIngressControllerK8sServicesSet() {
        return ingressControllerK8sServicesSet;
    }

    public void setIngressControllerK8sServicesSet(Set<K8sServices> ingressControllerK8sServicesSet) {
        this.ingressControllerK8sServicesSet = ingressControllerK8sServicesSet;
    }

    public Set<K8sServices> getObjectStorageOperatorK8sServicesSet() {
        return objectStorageOperatorK8sServicesSet;
    }

    public void setObjectStorageOperatorK8sServicesSet(Set<K8sServices> objectStorageOperatorK8sServicesSet) {
        this.objectStorageOperatorK8sServicesSet = objectStorageOperatorK8sServicesSet;
    }

    public Set<K8sServices> getNfsK8sServicesSet() {
        return nfsK8sServicesSet;
    }

    public void setNfsK8sServicesSet(Set<K8sServices> nfsK8sServicesSet) {
        this.nfsK8sServicesSet = nfsK8sServicesSet;
    }

    public Set<K8sServices> getK8sServicesSet() {
        return k8sServicesSet;
    }

    public void setK8sServicesSet(Set<K8sServices> k8sServicesSet) {
        this.k8sServicesSet = k8sServicesSet;
    }

    public Set<K8sKubeconfigAdmin> getK8sKubeconfigAdminSet() {
        return k8sKubeconfigAdminSet;
    }

    public void setK8sKubeconfigAdminSet(Set<K8sKubeconfigAdmin> k8sKubeconfigAdminSet) {
        this.k8sKubeconfigAdminSet = k8sKubeconfigAdminSet;
    }

    public Set<K8sNamespace> getK8sNamespaceSet() {
        return k8sNamespaceSet;
    }

    public void setK8sNamespaceSet(Set<K8sNamespace> k8sNamespaceSet) {
        this.k8sNamespaceSet = k8sNamespaceSet;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
