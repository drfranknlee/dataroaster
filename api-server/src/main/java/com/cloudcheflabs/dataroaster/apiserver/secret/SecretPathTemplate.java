package com.cloudcheflabs.dataroaster.apiserver.secret;

public class SecretPathTemplate {

    public static final String SECRET_KUBECONFIG_ADMIN = "secret/kubeconfig/{{ clusterId }}/admin/{{ user }}";

    public static final String SECRET_KUBECONFIG_USER = "secret/kubeconfig/{{ clusterId }}/user/{{ user }}/{{ namespace }}";

}
