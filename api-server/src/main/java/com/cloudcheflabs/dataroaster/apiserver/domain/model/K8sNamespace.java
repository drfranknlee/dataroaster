package com.cloudcheflabs.dataroaster.apiserver.domain.model;

import com.google.common.collect.Sets;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "k8s_namespace")
public class K8sNamespace implements Serializable {

    public static final String DEFAULT_NAMESPACE_INGRESS_CONTROLLER_NGINX = "ingress-nginx";
    public static final String DEFAULT_NAMESPACE_CERT_MANAGER = "cert-manager";

    public static final String DEFAULT_NAMESPACE_FILEBEAT = "dataroaster-filebeat";
    public static final String DEFAULT_NAMESPACE_LOGSTASH = "dataroaster-logstash";

    public static final String DEFAULT_NAMESPACE_PROM_STACK = "dataroaster-prom-stack";

    public static final String DEFAULT_NAMESPACE_JAEGER = "dataroaster-jaeger";

    public static final String DEFAULT_NAMESPACE_HARBOR = "dataroaster-harbor";

    public static final String DEFAULT_NAMESPACE_ARGOCD = "dataroaster-argocd";
    public static final String DEFAULT_NAMESPACE_JENKINS = "dataroaster-jenkins";

    public static final String DEFAULT_NAMESPACE_VELERO = "dataroaster-velero";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "namespace_name")
    private String namespaceName;

    @ManyToOne
    @JoinColumn(name ="cluster_id")
    private K8sCluster k8sCluster;


    @OneToMany(mappedBy = "k8sNamespace", fetch = FetchType.EAGER)
    private Set<Services> servicesSet = Sets.newHashSet();


    public Set<Services> getServicesSet() {
        return servicesSet;
    }

    public void setServicesSet(Set<Services> servicesSet) {
        this.servicesSet = servicesSet;
    }

    public K8sCluster getK8sCluster() {
        return k8sCluster;
    }

    public void setK8sCluster(K8sCluster k8sCluster) {
        this.k8sCluster = k8sCluster;
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
}
