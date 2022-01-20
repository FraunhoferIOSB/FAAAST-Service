package de.fraunhofer.iosb.ilt.faaast.service.assetconnection.opcua.utils;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UShort;
import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaMonitoredItem;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemModifyRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import org.eclipse.milo.opcua.stack.core.AttributeId;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoredItemCreateRequest;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.eclipse.milo.opcua.stack.core.types.structured.ReadValueId;


/**
 *
 * @author Michael Jacoby
 */
public class OpcUaUtils {

    private static final Logger logger = LoggerFactory.getLogger(OpcUaUtils.class);
    private static final String NODE_ID_SEPARATOR = ";";
    private static final String NS_PREFIX = "ns=";

    private OpcUaUtils() {
    }

    public static NodeId parseNodeId(OpcUaClient client, String nodeId) {
        Optional<String> ns = Stream.of(nodeId.split(NODE_ID_SEPARATOR))
                .filter(x -> x.startsWith(NS_PREFIX))
                .findFirst();
        int namespaceIndex = 0;
        if (ns.isPresent()) {
            String namespace = ns.get().replace(NS_PREFIX, "");
            try {
                namespaceIndex = Integer.parseUnsignedInt(namespace);
            } catch (NumberFormatException ex) {
                UShort actualNamespaceIndex = client.getNamespaceTable().getIndex(namespace);
                if (actualNamespaceIndex == null) {
                    throw new RuntimeException(String.format("could not resolve namespace '%s'", namespace));
                }
                namespaceIndex = actualNamespaceIndex.intValue();
            }
        } else {
            logger.warn("no namespace provided for node '%s'. Using default (ns=0)", nodeId);
        }
        return NodeId.parse(nodeId.replace(ns.get(), NS_PREFIX + namespaceIndex));
    }

    public static OpcUaClient createClient(String opcUrl, IdentityProvider identityProvider) throws UaException {
        return createClient(URI.create(opcUrl), identityProvider);
    }

    public static OpcUaClient createClient(URI opcUrl, IdentityProvider identityProvider) throws UaException {

        return OpcUaClient.create(
                opcUrl.toString(),
                endpoints ->
                        endpoints.stream()
                                .filter(e -> e.getSecurityPolicyUri().equals(SecurityPolicy.None.getUri()))
                                .findFirst(),
                configBuilder
                -> configBuilder
                        .setApplicationName(LocalizedText.english("AAS-Service"))
                        .setApplicationUri("urn:de:fraunhofer:iosb:aas:service")
                        .setIdentityProvider(identityProvider)
                        .setRequestTimeout(uint(5000))
                        .build()
        );
    }

    public static UaSubscription subscribe(OpcUaClient client, NodeId node, double interval, Consumer<DataValue> consumer) throws InterruptedException, ExecutionException {
        UaSubscription result = null;
        List<UaMonitoredItem> items = null;
        ReadValueId readValueId = new ReadValueId(node, AttributeId.Value.uid(), null, null);
        UInteger clientHandle = uint(new Random().nextInt((int) Math.min(UInteger.MAX.longValue(), (long) Integer.MAX_VALUE)));
        MonitoringParameters monitorParameters = new MonitoringParameters(clientHandle, 1000.0, null, uint(10), true);
        MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, monitorParameters);
        result = client.getSubscriptionManager().createSubscription(interval).get();
        items = result.createMonitoredItems(
                TimestampsToReturn.Both,
                Arrays.asList(request),
                (monitoredItem, id) -> {
                    monitoredItem.setValueConsumer(consumer);
                }).get();
        return result;
    }

    public static UaSubscription unsubscribe(OpcUaClient client, NodeId node, double interval) throws InterruptedException, ExecutionException {
        UaSubscription result = null;
        List<UaMonitoredItem> items = null;

        ReadValueId readValueId = new ReadValueId(node, AttributeId.Value.uid(), null, null);
        UInteger clientHandle = uint(new Random().nextInt((int) Math.min(UInteger.MAX.longValue(), (long) Integer.MAX_VALUE)));

        for (UaSubscription u: client.getSubscriptionManager().getSubscriptions()
             ) {
            MonitoringParameters monitorParameters = new MonitoringParameters(clientHandle, interval, null, uint(10), true);
            MonitoredItemCreateRequest request = new MonitoredItemCreateRequest(readValueId, MonitoringMode.Reporting, monitorParameters);

            items = result.createMonitoredItems(
                    TimestampsToReturn.Both,
                    Arrays.asList(request)).get();

            if(u.getMonitoredItems().contains(items)) {
                u.deleteMonitoredItems(items).get();
            }
        }
        return result;
    }


}
