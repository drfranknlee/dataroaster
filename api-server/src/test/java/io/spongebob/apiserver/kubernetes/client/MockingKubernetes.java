package io.spongebob.apiserver.kubernetes.client;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import junit.framework.AssertionFailedError;
import org.junit.Assert;
import org.junit.Rule;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class MockingKubernetes {

    @Rule
    public KubernetesServer server = new KubernetesServer(true, true);

    @Test
    public void testInCrudMode() throws Exception{
        KubernetesClient client = server.getClient();

        final CountDownLatch deleteLatch = new CountDownLatch(1);
        final CountDownLatch closeLatch = new CountDownLatch(1);

        //CREATE
        client.pods().inNamespace("ns1").create(new PodBuilder().withNewMetadata().withName("pod1").endMetadata().build());

        //READ
        PodList podList = client.pods().inNamespace("ns1").list();
        assertNotNull(podList);
        assertEquals(1, podList.getItems().size());

        //WATCH
        Watch watch = client.pods().inNamespace("ns1").withName("pod1").watch(new Watcher<Pod>() {
            @Override
            public void eventReceived(Watcher.Action action, Pod resource) {
                switch (action) {
                    case DELETED:
                        deleteLatch.countDown();
                        break;
                    default:
                        throw new AssertionFailedError(action.toString().concat(" isn't recognised."));
                }
            }

            @Override
            public void onClose(KubernetesClientException cause) {
                closeLatch.countDown();
            }
        });

        //DELETE
        client.pods().inNamespace("ns1").withName("pod1").delete();

        //READ AGAIN
        podList = client.pods().inNamespace("ns1").list();
        assertNotNull(podList);
        assertEquals(0, podList.getItems().size());

        assertTrue(deleteLatch.await(1, TimeUnit.MINUTES));
        watch.close();
        assertTrue(closeLatch.await(1, TimeUnit.MINUTES));
    }

    @Test
    public void loadFromExternal() throws Exception {
        KubernetesClient client = server.getClient();

        Namespace myns = client.namespaces().createNew()
                .withNewMetadata()
                .withName("presto")
                .addToLabels("name", "presto")
                .endMetadata()
                .done();

        NamespaceList namespaceList = client.namespaces().list();
        namespaceList.getItems().forEach(ns -> {
            System.out.println("ns: " + ns.getMetadata().getName());
        });

        // create pod with manifest.
        InputStream is = new ClassPathResource("manifests/a-pod.yaml").getInputStream();
        client.load(is).createOrReplace().forEach(hasMetadata -> {
            String kind = hasMetadata.getKind();
            String metaName = hasMetadata.getMetadata().getName();
            System.out.println("kind: [" + kind + "], metaName: [" + metaName + "]");
        });


        //READ
        PodList podList = client.pods().inNamespace("presto").list();
        podList.getItems().forEach(pod -> {
            String metaName = pod.getMetadata().getName();
            System.out.println("metaName: [" + metaName + "]");
        });
        Assert.assertEquals(1, podList.getItems().size());

    }
}
