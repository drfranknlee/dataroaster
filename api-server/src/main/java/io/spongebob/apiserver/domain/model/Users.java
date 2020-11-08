package io.spongebob.apiserver.domain.model;

import com.google.common.collect.Sets;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
public class Users implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "username")
    private String userName;

    @Column(name = "password")
    private String password;

    @Column(name = "enabled")
    private boolean enabled;

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<UserAuthorities> userAuthoritiesSet = Sets.newHashSet();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_groups",
            joinColumns = { @JoinColumn(name = "user_id") },
            inverseJoinColumns = { @JoinColumn(name = "group_id") }
    )
    private Set<Groups> groupsSet = new HashSet<>();

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<K8sKubeconfigUser> k8sKubeconfigUserSet = Sets.newHashSet();

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

    public Set<UserAuthorities> getUserAuthoritiesSet() {
        return userAuthoritiesSet;
    }

    public void setUserAuthoritiesSet(Set<UserAuthorities> userAuthoritiesSet) {
        this.userAuthoritiesSet = userAuthoritiesSet;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
