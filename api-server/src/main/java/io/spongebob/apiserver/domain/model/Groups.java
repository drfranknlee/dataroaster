package io.spongebob.apiserver.domain.model;

import com.google.common.collect.Sets;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "groups")
public class Groups implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "group")
    private String group;

    @ManyToMany(mappedBy = "groupsSet")
    private Set<Users> usersSet = new HashSet<>();

    @ManyToMany(mappedBy = "groupsSet")
    private Set<K8sNamespace> k8sNamespaceSet = new HashSet<>();

    @OneToMany(mappedBy = "groups", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<K8sKubeconfigUser> k8sKubeconfigUserSet = Sets.newHashSet();

    public Set<K8sKubeconfigUser> getK8sKubeconfigUserSet() {
        return k8sKubeconfigUserSet;
    }

    public void setK8sKubeconfigUserSet(Set<K8sKubeconfigUser> k8sKubeconfigUserSet) {
        this.k8sKubeconfigUserSet = k8sKubeconfigUserSet;
    }

    public Set<K8sNamespace> getK8sNamespaceSet() {
        return k8sNamespaceSet;
    }

    public void setK8sNamespaceSet(Set<K8sNamespace> k8sNamespaceSet) {
        this.k8sNamespaceSet = k8sNamespaceSet;
    }

    public Set<Users> getUsersSet() {
        return usersSet;
    }

    public void setUsersSet(Set<Users> usersSet) {
        this.usersSet = usersSet;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
