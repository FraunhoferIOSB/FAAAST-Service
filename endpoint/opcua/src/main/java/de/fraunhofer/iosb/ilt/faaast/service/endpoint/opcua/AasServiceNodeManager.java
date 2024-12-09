/*
 * Copyright (c) 2021 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua;

import com.prosysopc.ua.ServiceException;
import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.UaQualifiedName;
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaNodeFactoryException;
import com.prosysopc.ua.nodes.UaObject;
import com.prosysopc.ua.server.MethodManagerUaNode;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.types.opcua.BaseObjectType;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.AssetAdministrationShellCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.ConceptDescriptionCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.EmbeddedDataSpecificationCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.QualifierCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.SubmodelCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.SubmodelElementCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.SubmodelElementData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.AasSubmodelElementHelper;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.listener.AasServiceMethodManagerListener;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.SubmodelElementIdentifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.AmbiguousElementException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueFormatException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionId;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import opc.i4aas.objecttypes.AASAnnotatedRelationshipElementType;
import opc.i4aas.objecttypes.AASAssetAdministrationShellType;
import opc.i4aas.objecttypes.AASBlobType;
import opc.i4aas.objecttypes.AASEntityType;
import opc.i4aas.objecttypes.AASEnvironmentType;
import opc.i4aas.objecttypes.AASMultiLanguagePropertyType;
import opc.i4aas.objecttypes.AASOperationType;
import opc.i4aas.objecttypes.AASPropertyType;
import opc.i4aas.objecttypes.AASRangeType;
import opc.i4aas.objecttypes.AASReferenceElementType;
import opc.i4aas.objecttypes.AASRelationshipElementType;
import opc.i4aas.objecttypes.AASSubmodelElementType;
import opc.i4aas.objecttypes.AASSubmodelType;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.AnnotatedRelationshipElement;
import org.eclipse.digitaltwin.aas4j.v3.model.AssetAdministrationShell;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.DataElement;
import org.eclipse.digitaltwin.aas4j.v3.model.EmbeddedDataSpecification;
import org.eclipse.digitaltwin.aas4j.v3.model.Environment;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Node Manager for the AAS information model
 */
public class AasServiceNodeManager extends NodeManagerUaNode {

    /**
     * Make certain variable values read-only, because writing would not make
     * sense
     */
    public static final boolean VALUES_READ_ONLY = true;

    /**
     * Text if node is null
     */
    public static final String NODE_NULL = "node is null";

    /**
     * Text for addIdentifiable Exception
     */
    public static final String ADD_IDENT_EXC = "addIdentifiable Exception";

    /**
     * Text for Address Space Exception
     */
    private static final String ERROR_ADDRESS_SPACE = "Error creating address space";

    /**
     * Text if value is null
     */
    private static final String VALUE_NULL = "value must not be null";

    /**
     * Text if element is null
     */
    private static final String ELEMENT_NULL = "element must not be null";

    /**
     * The namespace URI of this node manager
     */
    public static final String NAMESPACE_URI = "http://www.iosb.fraunhofer.de/ILT/AAS/OPCUA";

    /**
     * The name of the AAS Environment node
     */
    private static final String AAS_ENVIRONMENT_NAME = "AASEnvironment";

    /**
     * The logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(AasServiceNodeManager.class);

    /**
     * The associated Endpoint
     */
    private final OpcUaEndpoint endpoint;

    /**
     * The AAS environment associated with this Node Manager
     */
    private Environment aasEnvironment;

    /**
     * The OPC UA Node for the AAS Environment
     */
    private AASEnvironmentType aasEnvironmentNode;

    /**
     * Maps NodeIds to AAS Data (e.g. Properties and operations)
     */
    private final Map<NodeId, SubmodelElementData> submodelElementAasMap;

    /**
     * Maps AAS SubmodelElements to OPC UA SubmodelElements
     */
    private final Map<SubmodelElementIdentifier, AASSubmodelElementType> submodelElementOpcUAMap;

    /**
     * Maps Submodel references to the OPC UA Submodel
     */
    private final Map<SubmodelElementIdentifier, UaNode> submodelOpcUAMap;

    /**
     * Maps reference to the corresponding Referable elements
     */
    private final Map<Reference, ObjectData> referableMap;

    /**
     * The MessageBus for signalling changes, e.g. changed values
     */
    private final MessageBus<?> messageBus;

    /**
     * The list of subscriptions to the MessageBus.
     */
    private final List<SubscriptionId> subscriptions;

    /**
     * The counter for default NodeIds
     */
    private int nodeIdCounter;

    /**
     * Creates a new instance of AasServiceNodeManager
     *
     * @param server the server in which the node manager is created.
     * @param namespaceUri the namespace URI for the nodes
     * @param aasEnvironment the AAS environment
     * @param endpoint the associated endpoint
     */
    public AasServiceNodeManager(UaServer server, String namespaceUri, Environment aasEnvironment, OpcUaEndpoint endpoint) {
        super(server, namespaceUri);
        Ensure.requireNonNull(aasEnvironment, "aasEnvironment must not be null");
        Ensure.requireNonNull(endpoint, "endpoint must not be null");

        this.aasEnvironment = aasEnvironment;
        this.endpoint = endpoint;
        submodelElementAasMap = new ConcurrentHashMap<>();
        submodelElementOpcUAMap = new ConcurrentHashMap<>();
        submodelOpcUAMap = new ConcurrentHashMap<>();
        referableMap = new ConcurrentHashMap<>();

        messageBus = endpoint.getMessageBus();
        Ensure.requireNonNull(messageBus, "messageBus must not be null");
        subscriptions = new ArrayList<>();
    }


    @Override
    protected void init() throws StatusException, UaNodeFactoryException {
        super.init();
        try {
            createAddressSpace();
        }
        catch (ServiceResultException ex) {
            LOG.error(ERROR_ADDRESS_SPACE);
            throw new StatusException(ex);
        }
        catch (ServiceException ex) {
            LOG.error(ERROR_ADDRESS_SPACE);
            throw new StatusException(ex.getServiceResult(), ex);
        }
        catch (AddressSpaceException | MessageBusException | ValueFormatException | AmbiguousElementException ex) {
            LOG.error(ERROR_ADDRESS_SPACE);
            throw new StatusException(ex.getMessage(), ex);
        }
    }


    @Override
    protected void close() {
        try {
            unsubscribeMessageBus();
        }
        catch (Exception ex) {
            LOG.error("close Exception", ex);
        }

        super.close();
    }


    /**
     * Gets the AAS Data for the given NodeId.
     *
     * @param node The desired NodeId
     * @return The associated AAS Data, null if it was not found
     */
    public SubmodelElementData getAasData(NodeId node) {
        SubmodelElementData retval = null;

        if (submodelElementAasMap.containsKey(node)) {
            retval = submodelElementAasMap.get(node);
            LOG.debug("getAasSubmodelElement: NodeId: {}; Property {}", node, retval);
        }
        else {
            LOG.trace("Node {} not found in submodelElementMap", node);
        }

        return retval;
    }


    /**
     * Creates the address space of the OPC UA Server.
     */
    private void createAddressSpace()
            throws StatusException, ServiceResultException, ServiceException, AddressSpaceException, MessageBusException, ValueFormatException, AmbiguousElementException {
        LOG.trace("createAddressSpace");

        MethodManagerUaNode methodManager = (MethodManagerUaNode) getMethodManager();
        methodManager.addCallListener(new AasServiceMethodManagerListener(endpoint, this));

        createAasNodes();
        subscribeMessageBus();
    }


    /**
     * Creates the AAS nodes in the address space.
     *
     * @throws StatusException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ServiceResultException If the operation fails
     * @throws ValueFormatException The data format of the value is invalid
     * @throws AmbiguousElementException if there are multiple matching elements in the environment
     */
    private void createAasNodes() throws StatusException, ServiceResultException, ServiceException, AddressSpaceException, ValueFormatException, AmbiguousElementException {
        addAasEnvironmentNode();

        ConceptDescriptionCreator.addConceptDescriptions(aasEnvironment.getConceptDescriptions(), this);

        List<Submodel> submodels = aasEnvironment.getSubmodels();
        if (submodels != null) {
            for (Submodel submodel: submodels) {
                SubmodelCreator.addSubmodel(aasEnvironmentNode, submodel, this);
            }
        }

        if (aasEnvironment.getAssetAdministrationShells() != null) {
            for (AssetAdministrationShell aas: aasEnvironment.getAssetAdministrationShells()) {
                AssetAdministrationShellCreator.addAssetAdministrationShell(aasEnvironmentNode, aas, this);
            }
        }
    }


    /**
     * Adds the AASEnvironment Node.
     */
    private void addAasEnvironmentNode() {
        final UaObject objectsFolder = getServer().getNodeManagerRoot().getObjectsFolder();
        LOG.debug("addAasEnvironmentNode {}; to ObjectsFolder", AAS_ENVIRONMENT_NAME);
        QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASEnvironmentType.getNamespaceUri(), AAS_ENVIRONMENT_NAME).toQualifiedName(getNamespaceTable());
        aasEnvironmentNode = createInstance(AASEnvironmentType.class, createNodeId(objectsFolder, browseName), browseName, LocalizedText.english(AAS_ENVIRONMENT_NAME));
        LOG.debug("addAasEnvironmentNode: Created class: {}", aasEnvironmentNode.getClass().getName());

        objectsFolder.addComponent(aasEnvironmentNode);
    }


    /**
     * Subscribes to Events on the MessageBus (e.g. ValueChangeEvents).
     *
     * @throws MessageBusException if subscribing fails
     */
    private void subscribeMessageBus() throws MessageBusException {
        LOG.debug("subscribeMessageBus: subscribe ValueChangeEvents");
        SubscriptionInfo info = SubscriptionInfo.create(ValueChangeEventMessage.class, x -> {
            try {
                updateSubmodelElementValue(x.getElement(), x.getNewValue(), x.getOldValue());
            }
            catch (Exception e) {
                LOG.error("valueChanged Exception", e);
            }
        });
        subscriptions.add(messageBus.subscribe(info));

        info = SubscriptionInfo.create(ElementCreateEventMessage.class, x -> {
            try {
                elementCreated(x.getElement(), x.getValue());
            }
            catch (Exception e) {
                LOG.error("elementCreated Exception", e);
            }
        });
        subscriptions.add(messageBus.subscribe(info));

        info = SubscriptionInfo.create(ElementDeleteEventMessage.class, x -> {
            try {
                elementDeleted(x.getElement());
            }
            catch (Exception e) {
                LOG.error("elementDeleted Exception", e);
            }
        });
        subscriptions.add(messageBus.subscribe(info));

        info = SubscriptionInfo.create(ElementUpdateEventMessage.class, x -> {
            try {
                elementUpdated(x.getElement(), x.getValue());
            }
            catch (Exception e) {
                LOG.error("elementUpdated Exception", e);
            }
        });
        subscriptions.add(messageBus.subscribe(info));
    }


    /**
     * Handles an elementCreated event.
     *
     * @param element Reference to the created element.
     * @param value The element that was added.
     * @throws StatusException If the operation fails
     * @throws ServiceResultException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ValueFormatException The data format of the value is invalid
     * @throws AmbiguousElementException if there are multiple matching elements in the environment
     * @throws PersistenceException if accessing the environment fails
     */
    private void elementCreated(Reference element, Referable value)
            throws StatusException, ServiceResultException, ServiceException, AddressSpaceException, ValueFormatException, AmbiguousElementException, PersistenceException {
        Ensure.requireNonNull(element, ELEMENT_NULL);
        Ensure.requireNonNull(value, VALUE_NULL);

        Reference parentRef = ReferenceHelper.getParent(element);
        if (LOG.isDebugEnabled()) {
            LOG.debug("elementCreated called. Reference {}; Value: {}; ParentRef: {}; Class {}", ReferenceHelper.toString(element), value.getIdShort(),
                    ReferenceHelper.toString(parentRef), value.getClass());
        }
        // The element is the reference to the object which is added itself
        // formerly it was the parent
        ObjectData parent = null;
        if ((parentRef != null) && ReferenceHelper.containsSameReference(referableMap, parentRef)) {
            parent = ReferenceHelper.getValueBySameReference(referableMap, parentRef);
        }

        if (value instanceof ConceptDescription conceptDescription) {
            ConceptDescriptionCreator.addConceptDescriptions(List.of(conceptDescription), this);
        }
        else if (value instanceof Submodel submodel) {
            SubmodelCreator.addSubmodel(aasEnvironmentNode, submodel, this);
        }
        else if (value instanceof AssetAdministrationShell assetAdministrationShell) {
            // read new Environment
            aasEnvironment = endpoint.getAASEnvironment();
            AssetAdministrationShellCreator.addAssetAdministrationShell(aasEnvironmentNode, assetAdministrationShell, this);
        }
        else if (parent != null) {
            if (value instanceof EmbeddedDataSpecification) {
                addEmbeddedDataSpecification(parent, value);
            }
            else if (value instanceof Qualifier) {
                addQualifier(parent, value);
            }
            else if (value instanceof SubmodelElement) {
                addSubmodelElement(parent, value, parentRef);
            }
        }
        else if (LOG.isDebugEnabled()) {
            LOG.debug("elementCreated: parent not found: {}", ReferenceHelper.toString(parentRef));
        }
    }


    /**
     * Handles an elementDeleted event.
     *
     * @param element Reference to the deleted element.
     * @throws StatusException If the operation fails
     */
    private void elementDeleted(Reference element) throws StatusException {
        Ensure.requireNonNull(element, ELEMENT_NULL);

        if (LOG.isDebugEnabled()) {
            LOG.debug("elementDeleted called. Reference {}", ReferenceHelper.toString(element));
        }
        // The element is the object that should be deleted
        var entry = ReferenceHelper.getEntryBySameReference(referableMap, element);
        if (entry != null) {
            ObjectData data = entry.getValue();
            // remove element from the map
            referableMap.remove(entry.getKey());
            removeFromMaps(data.getNode(), element, data.getReferable());
            deleteNode(data.getNode(), true, true);
        }
        else {
            LOG.atTrace().log("elementDeleted: element not found in referableMap: {}", ReferenceHelper.toString(element));
        }
    }


    /**
     * Handles an elementUpdated event.
     *
     * @param element Reference to the updated element.
     * @param value The element that was updated.
     * @throws StatusException If the operation fails
     * @throws ServiceResultException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ValueFormatException The data format of the value is invalid
     * @throws AmbiguousElementException if there are multiple matching elements in the environment
     * @throws PersistenceException if accessing the environment fails
     */
    private void elementUpdated(Reference element, Referable value)
            throws StatusException, ServiceResultException, ServiceException, AddressSpaceException, ValueFormatException, AmbiguousElementException, PersistenceException {
        Ensure.requireNonNull(element, ELEMENT_NULL);
        Ensure.requireNonNull(value, VALUE_NULL);

        if (LOG.isDebugEnabled()) {
            LOG.debug("elementUpdated called. Reference {}", ReferenceHelper.toString(element));
        }
        // Currently we implement update as delete and create. 
        elementDeleted(element);

        elementCreated(element, value);
    }


    /**
     * Unsubscribes from the MessageBus.
     */
    private void unsubscribeMessageBus() {
        LOG.debug("unsubscribe from the MessageBus ({} Subscriptions)", subscriptions.size());
        for (var subscription: subscriptions) {
            try {
                messageBus.unsubscribe(subscription);
            }
            catch (Exception ex) {
                LOG.error("unsubscribeMessageBus Exception", ex);
            }
        }
        subscriptions.clear();
    }


    /**
     * Update the value of a SubmodelElement.
     *
     * @param reference The reference of the desired SubmodelElement
     * @param newValue The new value of the SubmodelElement
     * @param oldValue The old value of the SubmodelElement
     * @throws StatusException If the operation fails
     * @throws ValueFormatException The data format of the value is invalid
     */
    public void updateSubmodelElementValue(Reference reference, ElementValue newValue, ElementValue oldValue) throws StatusException, ValueFormatException {
        Ensure.requireNonNull(reference, "reference must not be null");
        Ensure.requireNonNull(newValue, "newValue must not be null");

        SubmodelElementIdentifier path = SubmodelElementIdentifier.fromReference(reference);
        if (LOG.isTraceEnabled()) {
            LOG.trace("updateSubmodelElementValue Reference {}; Path {}", ReferenceHelper.toString(reference), dumpSubmodelElementIdentifier(path));
        }
        if (submodelElementOpcUAMap.containsKey(path)) {
            AasSubmodelElementHelper.setSubmodelElementValue(submodelElementOpcUAMap.get(path), newValue, this);
        }
        else if (LOG.isWarnEnabled()) {
            LOG.warn("SubmodelElement {} not found in submodelElementOpcUAMap", ReferenceHelper.toString(reference));
        }
    }


    /**
     * Gets the next availabe default NodeId.
     * 
     * @return The desired NodeId
     */
    public NodeId getDefaultNodeId() {
        return new NodeId(getNamespaceIndex(), ++nodeIdCounter);
    }


    /**
     * Adds an entry to the Referable map.
     * 
     * @param reference The reference to the desired referable.
     * @param referableData The data of the desired referable.
     */
    public void addReferable(Reference reference, ObjectData referableData) {
        referableMap.put(reference, referableData);
    }


    /**
     * Retrieves the Submodel node for the given reference.
     * 
     * @param reference The desired submodel reference.
     * @return The coresponding Submodel node
     */
    public UaNode getSubmodelNode(Reference reference) {
        return submodelOpcUAMap.get(SubmodelElementIdentifier.fromReference(reference));
    }


    /**
     * Adds a SubmodelElement to the submodelElementOpcUAMap.
     * 
     * @param reference The reference to the desired SubmodelElement.
     * @param submodelElement The corresponding SubmodelElement node.
     */
    public void addSubmodelElementOpcUA(Reference reference, AASSubmodelElementType submodelElement) {
        SubmodelElementIdentifier smid = SubmodelElementIdentifier.fromReference(reference);
        submodelElementOpcUAMap.put(smid, submodelElement);
    }


    /**
     * Adds a Submodel to the submodelOpcUAMap.
     * 
     * @param reference The reference to the desired Submodel.
     * @param node The corresponding Submodel node.
     */
    public void addSubmodelOpcUA(Reference reference, UaNode node) {
        submodelOpcUAMap.put(SubmodelElementIdentifier.fromReference(reference), node);
    }


    /**
     * Adds a SubmodelElement to the submodelElementAasMap.
     *
     * @param nodeId The Nodeid of the desired SubmodelElement.
     * @param data The corresponding SubmodelElement data.
     */
    public void addSubmodelElementAasMap(NodeId nodeId, SubmodelElementData data) {
        submodelElementAasMap.put(nodeId, data);
    }


    /**
     * Get the AAS Environment.
     * 
     * @return the AAS Environment.
     */
    public Environment getEnvironment() {
        return aasEnvironment;
    }


    /**
     * Removes the given node (and all sub-nodes) from the maps.
     *
     * @param node The desired node
     * @param reference The reference to the desired SubmodelElement
     * @param referable The corresponding referable
     */
    private void removeFromMaps(BaseObjectType node, Reference reference, Referable referable) {
        Ensure.requireNonNull(node, "node must not be null");

        try {
            if (node instanceof AASSubmodelElementType aASSubmodelElementType) {
                doRemoveFromMaps(aASSubmodelElementType, reference, referable);
            }
            else if (referable instanceof Submodel submodel) {
                doRemoveFromMaps(reference, submodel);
            }

            // no special treatment necessary for other types like AssetAdministrationShell, Asset or others
        }
        catch (RuntimeException ex) {
            // This exception is not thrown here. We ignore the error.
            LOG.info("removeFromMaps Exception", ex);
        }
    }


    /**
     * Removes the given SubmodelElement from the maps.
     *
     * @param element The desired SubmodelElement
     * @param reference The reference to the desired SubmodelElement
     * @param referable The corresponding referable
     */
    private void doRemoveFromMaps(AASSubmodelElementType element, Reference reference, Referable referable) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("doRemoveFromMaps: remove SubmodelElement {}", ReferenceHelper.toString(reference));
        }
        SubmodelElementIdentifier smid = SubmodelElementIdentifier.fromReference(reference);
        AASSubmodelElementType removedElement = submodelElementOpcUAMap.remove(smid);
        if ((removedElement != null) && LOG.isDebugEnabled()) {
            LOG.debug("doRemoveFromMaps: remove SubmodelElement from submodelElementOpcUAMap: {}", ReferenceHelper.toString(reference));
        }

        if (element instanceof AASPropertyType prop) {
            if (submodelElementAasMap.containsKey(prop.getValueNode().getNodeId())) {
                submodelElementAasMap.remove(prop.getValueNode().getNodeId());
                LOG.debug("doRemoveFromMaps: remove Property NodeId {}", prop.getValueNode().getNodeId());
            }
        }
        else if (element instanceof AASRangeType range) {
            if (submodelElementAasMap.containsKey(range.getMinNode().getNodeId())) {
                submodelElementAasMap.remove(range.getMinNode().getNodeId());
                LOG.debug("doRemoveFromMaps: remove Range Min NodeId {}", range.getMinNode().getNodeId());
            }

            if (submodelElementAasMap.containsKey(range.getMaxNode().getNodeId())) {
                submodelElementAasMap.remove(range.getMaxNode().getNodeId());
                LOG.debug("doRemoveFromMaps: remove Range Max NodeId {}", range.getMaxNode().getNodeId());
            }
        }
        else if (element instanceof AASOperationType oper) {
            if (submodelElementAasMap.containsKey(oper.getOperationNode().getNodeId())) {
                submodelElementAasMap.remove(oper.getOperationNode().getNodeId());
                LOG.debug("doRemoveFromMaps: remove Operation NodeId {}", oper.getOperationNode().getNodeId());
            }
        }
        else if (element instanceof AASBlobType blob) {
            if ((blob.getValueNode() != null) && (submodelElementAasMap.containsKey(blob.getValueNode().getNodeId()))) {
                submodelElementAasMap.remove(blob.getValueNode().getNodeId());
                LOG.debug("doRemoveFromMaps: remove Blob NodeId {}", blob.getValueNode().getNodeId());
            }
        }
        else if (element instanceof AASMultiLanguagePropertyType mlp) {
            if (submodelElementAasMap.containsKey(mlp.getValueNode().getNodeId())) {
                submodelElementAasMap.remove(mlp.getValueNode().getNodeId());
                LOG.debug("doRemoveFromMaps: remove AASMultiLanguageProperty NodeId {}", mlp.getValueNode().getNodeId());
            }
        }
        else if (element instanceof AASReferenceElementType refElem) {
            NodeId nid = refElem.getValueNode().getKeysNode().getNodeId();
            if (submodelElementAasMap.containsKey(nid)) {
                submodelElementAasMap.remove(nid);
                LOG.debug("doRemoveFromMaps: remove AASReferenceElement NodeId {}", nid);
            }
        }
        else if (element instanceof AASRelationshipElementType relElem) {
            NodeId nid = relElem.getFirstNode().getKeysNode().getNodeId();
            if (submodelElementAasMap.containsKey(nid)) {
                submodelElementAasMap.remove(nid);
                LOG.debug("doRemoveFromMaps: remove AASRelationshipElement First NodeId {}", nid);
            }

            nid = relElem.getSecondNode().getKeysNode().getNodeId();
            if (submodelElementAasMap.containsKey(nid)) {
                submodelElementAasMap.remove(nid);
                LOG.debug("doRemoveFromMaps: remove AASRelationshipElement Second NodeId {}", nid);
            }

            if ((relElem instanceof AASAnnotatedRelationshipElementType) && (referable instanceof AnnotatedRelationshipElement)) {
                AnnotatedRelationshipElement annRelElem = (AnnotatedRelationshipElement) referable;
                for (DataElement de: annRelElem.getAnnotations()) {
                    doRemoveFromMaps(reference, de);
                }
            }
        }
        else if (element instanceof AASEntityType ent) {
            if ((ent.getGlobalAssetIdNode() != null)) {
                NodeId nid = ent.getGlobalAssetIdNode().getNodeId();
                if (submodelElementAasMap.containsKey(nid)) {
                    submodelElementAasMap.remove(nid);
                    LOG.debug("doRemoveFromMaps: remove Entity GlobalAssetId NodeId {}", nid);
                }
            }

            if (submodelElementAasMap.containsKey(ent.getEntityTypeNode().getNodeId())) {
                submodelElementAasMap.remove(ent.getEntityTypeNode().getNodeId());
                LOG.debug("doRemoveFromMaps: remove Entity EntityType NodeId {}", ent.getEntityTypeNode().getNodeId());
            }
        }
        else if (referable instanceof SubmodelElementCollection sec) {
            for (SubmodelElement se: sec.getValue()) {
                doRemoveFromMaps(reference, se);
            }
        }

        // Capability and File are currently not relevant here
    }


    /**
     * Removes the given SubmodelElement from the maps.
     *
     * @param parent The reference to the parent element.
     * @param de The desired SubmodelElement
     */
    private void doRemoveFromMaps(Reference parent, SubmodelElement de) {
        Reference ref = AasUtils.toReference(parent, de);
        var entry = ReferenceHelper.getEntryBySameReference(referableMap, ref);
        if (entry != null) {
            ObjectData element = referableMap.remove(entry.getKey());
            if (element.getNode() instanceof AASSubmodelElementType aASSubmodelElementType) {
                doRemoveFromMaps(aASSubmodelElementType, ref, de);
            }
        }
        else {
            LOG.atTrace().log("doRemoveFromMaps: element not found in referableMap: {}", ReferenceHelper.toString(ref));
        }
    }


    /**
     * Removes the given SubmodelElement from the maps.
     *
     * @param reference The reference to the desired submodel.
     * @param submodel The desired submodel
     */
    private void doRemoveFromMaps(Reference reference, Submodel submodel) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("doRemoveFromMaps: remove submodel {}", ReferenceHelper.toString(reference));
        }
        for (SubmodelElement element: submodel.getSubmodelElements()) {
            doRemoveFromMaps(reference, element);
        }

        submodelOpcUAMap.remove(SubmodelElementIdentifier.fromReference(reference));
    }


    private static String dumpSubmodelElementIdentifier(SubmodelElementIdentifier value) {
        return String.format("SubmodelElementIdentifier: Submodel %s; IdShortPath %s", value.getSubmodelId(), value.getIdShortPath().toString());
    }


    private void addQualifier(ObjectData parent, Referable value) throws StatusException {
        if (parent.getNode() instanceof AASSubmodelType aASSubmodelType) {
            QualifierCreator.addQualifiers(aASSubmodelType.getQualifierNode(), List.of((Qualifier) value), this);
        }
        else if (parent.getNode() instanceof AASSubmodelElementType aASSubmodelElementType) {
            QualifierCreator.addQualifiers(aASSubmodelElementType.getQualifierNode(), List.of((Qualifier) value), this);
        }
        else {
            LOG.debug("elementCreated: Constraint parent class not found");
        }
    }


    private void addSubmodelElement(ObjectData parent, Referable value, Reference element)
            throws StatusException, ValueFormatException, ServiceResultException, ServiceException, AddressSpaceException {
        if (parent.getNode() instanceof AASSubmodelType) {
            LOG.trace("elementCreated: call addSubmodelElements");
            SubmodelElementCreator.addSubmodelElements(parent.getNode(), List.of((SubmodelElement) value), (Submodel) parent.getReferable(), element, this);
        }
        else if (parent.getNode() instanceof AASSubmodelElementType) {
            LOG.debug("elementCreated: call addSubmodelElements");
            SubmodelElementCreator.addSubmodelElements(parent.getNode(), List.of((SubmodelElement) value), parent.getSubmodel(), element, this);
        }
        else {
            LOG.debug("elementCreated: SubmodelElement parent class not found: {}; {}", parent.getNode().getNodeId(), parent.getNode());
        }
    }


    private void addEmbeddedDataSpecification(ObjectData parent, Referable value) throws StatusException {
        if (parent.getNode() instanceof AASAssetAdministrationShellType aASAssetAdministrationShellType) {
            EmbeddedDataSpecificationCreator.addEmbeddedDataSpecifications(aASAssetAdministrationShellType,
                    List.of((EmbeddedDataSpecification) value), this);
        }
        else if (parent.getNode() instanceof AASSubmodelType aASSubmodelType) {
            EmbeddedDataSpecificationCreator.addEmbeddedDataSpecifications(aASSubmodelType, List.of((EmbeddedDataSpecification) value), this);
        }
        else if (parent.getNode() instanceof AASSubmodelElementType aASSubmodelElementType) {
            EmbeddedDataSpecificationCreator.addEmbeddedDataSpecifications(aASSubmodelElementType, List.of((EmbeddedDataSpecification) value), this);
        }
        else {
            LOG.debug("elementCreated: EmbeddedDataSpecification parent class not found");
        }
    }
}
