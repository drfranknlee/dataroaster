package com.cloudcheflabs.dataroaster.apiserver.domain.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "k8s_kubeconfig_user")
public class K8sKubeconfigUser implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "secret_path")
    private String secretPath;

    @ManyToOne
    @JoinColumn(name ="user_id")
    private Users users;

    @ManyToOne
    @JoinColumn(name ="group_id")
    private Groups groups;

    @ManyToOne
    @JoinColumn(name ="namespace_id")
    private K8sNamespace k8sNamespace;

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

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    public Groups getGroups() {
        return groups;
    }

    public void setGroups(Groups groups) {
        this.groups = groups;
    }

    public K8sNamespace getK8sNamespace() {
        return k8sNamespace;
    }

    public void setK8sNamespace(K8sNamespace k8sNamespace) {
        this.k8sNamespace = k8sNamespace;
    }
}
