package io.spongebob.apiserver.domain.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "k8s_kubeconfig_admin")
public class K8sKubeconfigAdmin implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "secret_path")
    private String secretPath;


    @ManyToOne
    @JoinColumn(name ="cluster_id")
    private K8sCluster k8sCluster;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSecretPath() {
        return secretPath;
    }

    public void setSecretPath(String secretPath) {
        this.secretPath = secretPath;
    }

    public K8sCluster getK8sCluster() {
        return k8sCluster;
    }

    public void setK8sCluster(K8sCluster k8sCluster) {
        this.k8sCluster = k8sCluster;
    }
}
