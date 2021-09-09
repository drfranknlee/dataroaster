package com.cloudcheflabs.dataroaster.apiserver.dao.kubernetes;

import com.cloudcheflabs.dataroaster.apiserver.api.dao.ResourceControlDao;
import com.cloudcheflabs.dataroaster.apiserver.domain.Kubeconfig;
import com.cloudcheflabs.dataroaster.apiserver.domain.StorageClass;
import com.cloudcheflabs.dataroaster.apiserver.kubernetes.client.KubernetesClientUtils;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class KubernetesResourceControlDao implements ResourceControlDao {
    @Override
    public List<StorageClass> listStorageClasses(Kubeconfig kubeconfig) {
        List<StorageClass> storageClasses = new ArrayList<>();

        KubernetesClient adminClient = KubernetesClientUtils.newClientWithKubeconfigYaml(kubeconfig);
        List<io.fabric8.kubernetes.api.model.storage.StorageClass> storageClassList = adminClient.storage().storageClasses().list().getItems();
        for(io.fabric8.kubernetes.api.model.storage.StorageClass storageClass : storageClassList) {
            String name = storageClass.getMetadata().getName();
            String provisioner = storageClass.getProvisioner();
            String reclaimPolicy = storageClass.getReclaimPolicy();
            String volumeBindingMode = storageClass.getVolumeBindingMode();

            storageClasses.add(new StorageClass(name, provisioner, reclaimPolicy, volumeBindingMode));
        }

        return storageClasses;
    }
}
