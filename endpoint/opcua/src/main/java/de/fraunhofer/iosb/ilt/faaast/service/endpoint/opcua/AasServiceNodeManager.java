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
import com.prosysopc.ua.ValueRanks;
import com.prosysopc.ua.client.AddressSpaceException;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.nodes.UaNodeFactoryException;
import com.prosysopc.ua.nodes.UaObject;
import com.prosysopc.ua.server.CallableListener;
import com.prosysopc.ua.server.MethodManagerUaNode;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.server.UaServer;
import com.prosysopc.ua.server.nodes.PlainMethod;
import com.prosysopc.ua.server.nodes.PlainProperty;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.Argument;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.types.opcua.BaseObjectType;
import com.prosysopc.ua.types.opcua.FolderType;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.AasReferenceCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.AssetAdministrationShellCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.ConceptDescriptionCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.DataElementCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.DescriptionCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.EmbeddedDataSpecificationCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.IdentifiableCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.IdentifierKeyValuePairCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.QualifierCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator.SubmodelElementCreator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.SubmodelElementData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.AasSubmodelElementHelper;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.listener.AasServiceMethodManagerListener;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionId;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementUpdateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import io.adminshell.aas.v3.dataformat.core.util.AasUtils;
import io.adminshell.aas.v3.model.AnnotatedRelationshipElement;
import io.adminshell.aas.v3.model.Asset;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.BasicEvent;
import io.adminshell.aas.v3.model.Capability;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.Constraint;
import io.adminshell.aas.v3.model.DataElement;
import io.adminshell.aas.v3.model.EmbeddedDataSpecification;
import io.adminshell.aas.v3.model.Entity;
import io.adminshell.aas.v3.model.Event;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.Operation;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.RelationshipElement;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import opc.i4aas.AASAnnotatedRelationshipElementType;
import opc.i4aas.AASAssetAdministrationShellType;
import opc.i4aas.AASAssetType;
import opc.i4aas.AASBlobType;
import opc.i4aas.AASCapabilityType;
import opc.i4aas.AASEntityType;
import opc.i4aas.AASEnvironmentType;
import opc.i4aas.AASEventType;
import opc.i4aas.AASMultiLanguagePropertyType;
import opc.i4aas.AASOperationType;
import opc.i4aas.AASOrderedSubmodelElementCollectionType;
import opc.i4aas.AASPropertyType;
import opc.i4aas.AASRangeType;
import opc.i4aas.AASReferenceElementType;
import opc.i4aas.AASRelationshipElementType;
import opc.i4aas.AASSubmodelElementCollectionType;
import opc.i4aas.AASSubmodelElementType;
import opc.i4aas.AASSubmodelType;
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
     * Text if value is null
     */
    private static final String VALUE_NULL = "value is null";

    /**
     * Text if element is null
     */
    private static final String ELEMENT_NULL = "element is null";

    /**
     * The namespace URI of this node manager
     */
    public static final String NAMESPACE_URI = "http://www.iosb.fraunhofer.de/ILT/AAS/OPCUA";

    /**
     * The logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(AasServiceNodeManager.class);

    /**
     * The AAS environment associated with this Node Manager
     */
    private final AssetAdministrationShellEnvironment aasEnvironment;

    /**
     * The associated Endpoint
     */
    private final OpcUaEndpoint endpoint;

    /**
     * The associated listener for method calls
     */
    private CallableListener aasMethodManagerListener;

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
    private final Map<Reference, AASSubmodelElementType> submodelElementOpcUAMap;

    /**
     * Maps Submodel references to the OPC UA Submodel
     */
    private final Map<Reference, UaNode> submodelOpcUAMap;

    /**
     * Maps NodeIds to the corresponding Referable elements
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
     * @param aas the AAS environment
     * @param ep the associated endpoint
     */
    public AasServiceNodeManager(UaServer server, String namespaceUri, AssetAdministrationShellEnvironment aas, OpcUaEndpoint ep) {
        super(server, namespaceUri);
        aasEnvironment = aas;

        endpoint = ep;
        submodelElementAasMap = new ConcurrentHashMap<>();
        submodelElementOpcUAMap = new ConcurrentHashMap<>();
        submodelOpcUAMap = new ConcurrentHashMap<>();
        referableMap = new ConcurrentHashMap<>();

        messageBus = ep.getMessageBus();
        subscriptions = new ArrayList<>();
    }


    @Override
    protected void init() throws StatusException, UaNodeFactoryException {
        try {
            super.init();

            AasSubmodelElementHelper.setNodeManager(this);

            createAddressSpace();
        }
        catch (ServiceResultException ex) {
            throw new StatusException(ex);
        }
        catch (ServiceException ex) {
            throw new StatusException(ex.getServiceResult(), ex);
        }
        catch (AddressSpaceException | MessageBusException ex) {
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
            LOG.info("Node {} not found in submodelElementMap", node);
        }

        return retval;
    }


    /**
     * Creates the address space of the OPC UA Server.
     */
    private void createAddressSpace() throws StatusException, ServiceResultException, ServiceException, AddressSpaceException, MessageBusException {
        try {
            LOG.trace("createAddressSpace");

            aasMethodManagerListener = new AasServiceMethodManagerListener(endpoint, this);

            createAasNodes();
            subscribeMessageBus();
        }
        catch (Exception ex) {
            LOG.error("createAddressSpace Exception", ex);
            throw ex;
        }
    }


    /**
     * Creates the AAS nodes in the address space.
     * 
     * @throws StatusException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    private void createAasNodes() throws StatusException, ServiceResultException, ServiceException, AddressSpaceException {
        try {
            if (aasEnvironment != null) {
                // add AASEnvironmentType
                addAasEnvironmentNode();

                // ConceptDescriptions.
                ConceptDescriptionCreator.addConceptDescriptions(aasEnvironment.getConceptDescriptions(), this);

                // Assets
                List<Asset> assets = aasEnvironment.getAssets();
                if ((assets != null) && (!assets.isEmpty())) {
                    for (Asset asset: assets) {
                        addAsset(aasEnvironmentNode, asset);
                    }
                }

                // Submodels
                List<Submodel> submodels = aasEnvironment.getSubmodels();
                if ((submodels != null) && (!submodels.isEmpty())) {
                    for (Submodel submodel: submodels) {
                        addSubmodel(aasEnvironmentNode, submodel);
                    }
                }

                addAssetAdministrationShells();
            }
        }
        catch (Exception ex) {
            LOG.error("createAasNodes Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the AssetAdministrationShells of the current environment.
     * 
     * @throws StatusException If the operation fails
     */
    private void addAssetAdministrationShells() throws StatusException {
        try {
            for (AssetAdministrationShell aas: aasEnvironment.getAssetAdministrationShells()) {
                AssetAdministrationShellCreator.addAssetAdministrationShell(aasEnvironmentNode, aas, this);
            }
        }
        catch (Exception ex) {
            LOG.error("addAssetAdministrationShells Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the AASEnvironment Node.
     */
    private void addAasEnvironmentNode() {
        try {
            final UaObject objectsFolder = getServer().getNodeManagerRoot().getObjectsFolder();
            if (aasEnvironment != null) {
                String name = "AASEnvironment";
                LOG.debug("addAasEnvironmentNode {}; to ObjectsFolder", name);
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASEnvironmentType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(objectsFolder, browseName);
                FolderType ft = createInstance(AASEnvironmentType.class, nid, browseName, LocalizedText.english(name));
                LOG.debug("addAasEnvironmentNode: Created class: {}", ft.getClass().getName());
                aasEnvironmentNode = (AASEnvironmentType) ft;

                objectsFolder.addComponent(aasEnvironmentNode);
            }
        }
        catch (Exception ex) {
            LOG.error("addAasEnvironmentNode Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an Asset to the given Node.
     *
     * @param node The UA node in which the Asset should be created
     * @param asset The desired Asset
     * @throws StatusException If the operation fails
     */
    private void addAsset(UaNode node, Asset asset) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException(NODE_NULL);
        }
        else if (asset == null) {
            throw new IllegalArgumentException("asset = null");
        }

        try {
            String name = asset.getIdShort();
            String displayName = "Asset:" + name;
            LOG.debug("addAsset {}; to Node: {}", name, node);
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASAssetType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
            NodeId nid = createNodeId(node, browseName);
            AASAssetType assetNode = createInstance(AASAssetType.class, nid, browseName, LocalizedText.english(displayName));

            IdentifiableCreator.addIdentifiable(assetNode, asset.getIdentification(), asset.getAdministration(), asset.getCategory(), this);

            // DataSpecifications
            EmbeddedDataSpecificationCreator.addEmbeddedDataSpecifications(assetNode, asset.getEmbeddedDataSpecifications(), this);

            node.addComponent(assetNode);

            referableMap.put(AasUtils.toReference(asset), new ObjectData(asset, assetNode));
        }
        catch (Exception ex) {
            LOG.error("addAsset Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds a submodel to a given Node
     *
     * @param node The desired Node where the submodel should be added
     * @param submodel The desired AAS submodel
     */
    private void addSubmodel(UaNode node, Submodel submodel) throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        if (submodel == null) {
            throw new IllegalArgumentException("submodel is null");
        }

        try {
            String shortId = submodel.getIdShort();
            if (!shortId.isEmpty()) {
                String displayName = "Submodel:" + shortId;
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASSubmodelType.getNamespaceUri(), shortId).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(node, browseName);
                if (findNode(nid) != null) {
                    // The NodeId already exists
                    nid = getDefaultNodeId();
                }

                LOG.trace("addSubmodel: create Submodel {}; NodeId: {}", submodel.getIdShort(), nid);
                AASSubmodelType smNode = createInstance(AASSubmodelType.class, nid, browseName, LocalizedText.english(displayName));

                // ModelingKind
                smNode.setModelingKind(ValueConverter.convertModelingKind(submodel.getKind()));
                IdentifiableCreator.addIdentifiable(smNode, submodel.getIdentification(), submodel.getAdministration(), submodel.getCategory(), this);

                // DataSpecifications
                EmbeddedDataSpecificationCreator.addEmbeddedDataSpecifications(smNode, submodel.getEmbeddedDataSpecifications(), this);

                // Qualifiers
                List<Constraint> qualifiers = submodel.getQualifiers();
                if ((qualifiers != null) && (!qualifiers.isEmpty())) {
                    if (smNode.getQualifierNode() == null) {
                        QualifierCreator.addQualifierNode(smNode, this);
                    }

                    QualifierCreator.addQualifiers(smNode.getQualifierNode(), qualifiers, this);
                }

                // SemanticId
                if (submodel.getSemanticId() != null) {
                    ConceptDescriptionCreator.addSemanticId(smNode, submodel.getSemanticId());
                }

                // Description
                DescriptionCreator.addDescriptions(smNode, submodel.getDescriptions());

                Reference refSubmodel = AasUtils.toReference(submodel);

                // SubmodelElements
                addSubmodelElements(smNode, submodel.getSubmodelElements(), submodel, refSubmodel);

                if (VALUES_READ_ONLY) {
                    smNode.getModelingKindNode().setAccessLevel(AccessLevelType.CurrentRead);
                }

                submodelOpcUAMap.put(AasUtils.toReference(submodel), smNode);

                node.addComponent(smNode);

                referableMap.put(AasUtils.toReference(submodel), new ObjectData(submodel, smNode));
            }
            else {
                LOG.warn("addSubmodel: IdShort is empty!");
            }
        }
        catch (Exception ex) {
            LOG.error("addSubmodel Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds a list of submodel elements to the given node.
     *
     * @param node The desired node in which the objects should be created
     * @param elements The desired list of submodel elements
     * @param submodel The corresponding submodel
     * @param parentRef The AAS reference to the parent object
     */
    private void addSubmodelElements(UaNode node, List<SubmodelElement> elements, Submodel submodel, Reference parentRef)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        addSubmodelElements(node, elements, submodel, parentRef, false);
    }


    /**
     * Adds a list of submodel elements to the given node (ordered, if requested).
     *
     * @param node The desired node in which the objects should be created
     * @param elements The desired list of submodel elements
     * @param submodel The corresponding submodel
     * @param parentRef The AAS reference to the parent object
     * @param ordered Specifies where the elements should de added ordered
     *            (true) or unordered (false)
     */
    private void addSubmodelElements(UaNode node, Collection<SubmodelElement> elements, Submodel submodel, Reference parentRef, boolean ordered)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        try {
            if ((elements != null) && (!elements.isEmpty())) {
                for (SubmodelElement elem: elements) {
                    if (elem instanceof DataElement) {
                        DataElementCreator.addAasDataElement(node, (DataElement) elem, submodel, parentRef, ordered, this);
                    }
                    else if (elem instanceof Capability) {
                        addAasCapability(node, (Capability) elem, submodel, parentRef, ordered);
                    }
                    else if (elem instanceof Entity) {
                        addAasEntity(node, (Entity) elem, submodel, parentRef, ordered);
                    }
                    else if (elem instanceof Operation) {
                        addAasOperation(node, (Operation) elem, submodel, parentRef, ordered);
                    }
                    else if (elem instanceof Event) {
                        addAasEvent(node, (Event) elem, submodel, parentRef, ordered);
                    }
                    else if (elem instanceof RelationshipElement) {
                        addAasRelationshipElement(node, (RelationshipElement) elem, submodel, parentRef, ordered);
                    }
                    else if (elem instanceof SubmodelElementCollection) {
                        addAasSubmodelElementCollection(node, (SubmodelElementCollection) elem, submodel, parentRef, ordered);
                    }
                    else if (elem != null) {
                        LOG.warn("addSubmodelElements: unknown SubmodelElement: {}; Class {}", elem.getIdShort(), elem.getClass());
                    }
                }
            }
        }
        catch (Exception ex) {
            LOG.error("addSubmodelElements Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS Capability to the given node.
     *
     * @param node The desired UA node
     * @param aasCapability The corresponding AAS Capability to add
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param parentRef The AAS reference to the parent object
     * @param ordered Specifies whether the capability should be added ordered
     *            (true) or unordered (false)
     * @throws StatusException If the operation fails
     */
    private void addAasCapability(UaNode node, Capability aasCapability, Submodel submodel, Reference parentRef, boolean ordered)
            throws StatusException {
        try {
            if ((node != null) && (aasCapability != null)) {
                String name = aasCapability.getIdShort();
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASCapabilityType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = getDefaultNodeId();
                AASCapabilityType capabilityNode = createInstance(AASCapabilityType.class, nid, browseName, LocalizedText.english(name));
                SubmodelElementCreator.addSubmodelElementBaseData(capabilityNode, aasCapability, this);

                if (ordered) {
                    node.addReference(capabilityNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(capabilityNode);
                }

                Reference capabilityRef = AasUtils.toReference(parentRef, aasCapability);

                referableMap.put(capabilityRef, new ObjectData(aasCapability, capabilityNode, submodel));
            }
        }
        catch (Exception ex) {
            LOG.error("addAasCapability Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS entity to the given node.
     *
     * @param node The desired UA node
     * @param aasEntity The AAS entity to add
     * @param submodel The corresponding Submodel
     * @param parentRef The AAS reference to the parent object
     * @param ordered Specifies whether the entity should be added ordered
     *            (true) or unordered (false)
     * @throws StatusException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    private void addAasEntity(UaNode node, Entity aasEntity, Submodel submodel, Reference parentRef, boolean ordered)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        try {
            if ((node != null) && (aasEntity != null)) {
                String name = aasEntity.getIdShort();
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASEntityType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = getDefaultNodeId();
                AASEntityType entityNode = createInstance(AASEntityType.class, nid, browseName, LocalizedText.english(name));
                SubmodelElementCreator.addSubmodelElementBaseData(entityNode, aasEntity, this);

                Reference entityRef = AasUtils.toReference(parentRef, aasEntity);

                // EntityType
                entityNode.setEntityType(ValueConverter.getAasEntityType(aasEntity.getEntityType()));

                submodelElementAasMap.put(entityNode.getEntityTypeNode().getNodeId(),
                        new SubmodelElementData(aasEntity, submodel, SubmodelElementData.Type.ENTITY_TYPE, entityRef));

                // GlobalAssetId
                if (aasEntity.getGlobalAssetId() != null) {
                    if (entityNode.getGlobalAssetIdNode() == null) {
                        AasReferenceCreator.addAasReferenceAasNS(entityNode, aasEntity.getGlobalAssetId(), AASEntityType.GLOBAL_ASSET_ID, false, this);
                    }
                    else {
                        AasSubmodelElementHelper.setAasReferenceData(aasEntity.getGlobalAssetId(), entityNode.getGlobalAssetIdNode(), false);
                    }

                    submodelElementAasMap.put(entityNode.getGlobalAssetIdNode().getKeysNode().getNodeId(),
                            new SubmodelElementData(aasEntity, submodel, SubmodelElementData.Type.ENTITY_GLOBAL_ASSET_ID, entityRef));
                }

                // SpecificAssetIds
                IdentifierKeyValuePair specificAssetId = aasEntity.getSpecificAssetId();
                if (specificAssetId != null) {
                    if (entityNode.getSpecificAssetIdNode() == null) {
                        IdentifierKeyValuePairCreator.addIdentifierKeyValuePair(entityNode, specificAssetId, AASEntityType.SPECIFIC_ASSET_ID, this);
                    }
                    else {
                        IdentifierKeyValuePairCreator.setIdentifierKeyValuePairData(entityNode.getSpecificAssetIdNode(), specificAssetId, this);
                    }
                }

                // Statements
                addSubmodelElements(entityNode.getStatementNode(), aasEntity.getStatements(), submodel, entityRef);

                submodelElementOpcUAMap.put(entityRef, entityNode);

                if (ordered) {
                    node.addReference(entityNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(entityNode);
                }

                referableMap.put(entityRef, new ObjectData(aasEntity, entityNode, submodel));
            }
        }
        catch (Exception ex) {
            LOG.error("addAasEntity Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS Operation to the given node.
     *
     * @param node The desired UA node
     * @param aasOperation The corresponding AAS operation to add
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param parentRef The reference to the parent object
     * @param ordered Specifies whether the operation should be added ordered
     *            (true) or unordered (false)
     */
    private void addAasOperation(UaNode node, Operation aasOperation, Submodel submodel, Reference parentRef, boolean ordered) {
        try {
            String name = aasOperation.getIdShort();
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASOperationType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
            NodeId nid = getDefaultNodeId();
            AASOperationType oper = createInstance(AASOperationType.class, nid, browseName, LocalizedText.english(name));
            SubmodelElementCreator.addSubmodelElementBaseData(oper, aasOperation, this);

            Reference operRef = AasUtils.toReference(parentRef, aasOperation);

            // for operations we put the corresponding operation object into the map
            submodelElementAasMap.put(nid, new SubmodelElementData(aasOperation, submodel, SubmodelElementData.Type.OPERATION, operRef));
            LOG.debug("addAasOperation: NodeId {}; Property: {}", nid, aasOperation);

            // add method
            NodeId myMethodId = new NodeId(getNamespaceIndex(), nid.getValue().toString() + "." + name);
            PlainMethod method = new PlainMethod(this, myMethodId, name, Locale.ENGLISH);
            Argument[] inputs = new Argument[aasOperation.getInputVariables().size()];
            for (int i = 0; i < aasOperation.getInputVariables().size(); i++) {
                OperationVariable v = aasOperation.getInputVariables().get(i);
                inputs[i] = new Argument();
                setOperationArgument(inputs[i], v);
            }

            method.setInputArguments(inputs);

            Argument[] outputs = new Argument[aasOperation.getOutputVariables().size()];
            for (int i = 0; i < aasOperation.getOutputVariables().size(); i++) {
                OperationVariable v = aasOperation.getOutputVariables().get(i);
                outputs[i] = new Argument();
                setOperationArgument(outputs[i], v);
            }

            method.setOutputArguments(outputs);

            MethodManagerUaNode m = (MethodManagerUaNode) this.getMethodManager();
            m.addCallListener(aasMethodManagerListener);

            method.setDescription(new LocalizedText("", ""));
            oper.addComponent(method);

            if (ordered) {
                node.addReference(oper, Identifiers.HasOrderedComponent, false);
            }
            else {
                node.addComponent(oper);
            }

            referableMap.put(operRef, new ObjectData(aasOperation, oper, submodel));
        }
        catch (Exception ex) {
            LOG.error("addAasOperation Exception", ex);
        }
    }


    /**
     * Sets the arguments for the given Operation Variable.
     *
     * @param arg The UA argument
     * @param var The corresponding Operation Variable
     */
    private void setOperationArgument(Argument arg, OperationVariable operVar) {
        try {
            if (operVar.getValue() instanceof Property) {
                Property prop = (Property) operVar.getValue();
                arg.setName(prop.getIdShort());
                arg.setValueRank(ValueRanks.Scalar);
                arg.setArrayDimensions(null);

                // Description
                DescriptionCreator.addDescriptions(arg, prop.getDescriptions());

                NodeId type = ValueConverter.convertValueTypeStringToNodeId(prop.getValueType());
                if (type.isNullNodeId()) {
                    LOG.warn("setOperationArgument: Property {}: Unknown type: {}", prop.getIdShort(), prop.getValueType());

                    // Default type is String. That's what we receive from the AAS Service
                    arg.setDataType(Identifiers.String);
                }
                else {
                    arg.setDataType(type);
                }
            }
            else {
                LOG.warn("setOperationArgument: unknown Argument type");
            }
        }
        catch (Exception ex) {
            LOG.error("setOperationArgument Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS Event to the given node.
     *
     * @param node The desired UA node
     * @param aasEvent The AAS Event to add
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param parentRef The AAS reference to the parent object
     * @param ordered Specifies whether the entity should be added ordered
     *            (true) or unordered (false)
     * @throws StatusException If the operation fails
     */
    private void addAasEvent(UaNode node, Event aasEvent, Submodel submodel, Reference parentRef, boolean ordered)
            throws StatusException {
        try {
            if ((node != null) && (aasEvent != null)) {
                String name = aasEvent.getIdShort();
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASEventType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = getDefaultNodeId();
                AASEventType eventNode = createInstance(AASEventType.class, nid, browseName, LocalizedText.english(name));
                SubmodelElementCreator.addSubmodelElementBaseData(eventNode, aasEvent, this);

                if (aasEvent instanceof BasicEvent) {
                    setBasicEventData(eventNode, (BasicEvent) aasEvent);
                }

                Reference eventRef = AasUtils.toReference(parentRef, aasEvent);

                if (ordered) {
                    node.addReference(eventNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(eventNode);
                }

                referableMap.put(eventRef, new ObjectData(aasEvent, eventNode, submodel));
            }
        }
        catch (Exception ex) {
            LOG.error("addAasEvent Exception", ex);
            throw ex;
        }
    }


    /**
     * Sets the Basic event data.
     *
     * @param eventNode The desired UA event node
     * @param aasEvent The corresponding AAS BasicEvent
     */
    private void setBasicEventData(AASEventType eventNode, BasicEvent aasEvent) {
        try {
            if (aasEvent.getObserved() != null) {
                LOG.warn("setBasicEventData: not implemented! Event: {}", eventNode.getBrowseName().getName());
            }
        }
        catch (Exception ex) {
            LOG.error("setBasicEventData Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS Relationship Element to the given node.
     *
     * @param node The desired UA node
     * @param aasRelElem The corresponding AAS Relationship Element
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param parentRef The AAS reference to the parent object
     * @param ordered Specifies whether the entity should be added ordered
     *            (true) or unordered (false)
     * @throws StatusException If the operation fails
     */
    private void addAasRelationshipElement(UaNode node, RelationshipElement aasRelElem, Submodel submodel, Reference parentRef, boolean ordered)
            throws StatusException {
        try {
            if ((node != null) && (aasRelElem != null)) {
                Reference relElemRef = AasUtils.toReference(parentRef, aasRelElem);

                String name = aasRelElem.getIdShort();
                AASRelationshipElementType relElemNode;
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASRelationshipElementType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = getDefaultNodeId();
                if (aasRelElem instanceof AnnotatedRelationshipElement) {
                    relElemNode = createAnnotatedRelationshipElement((AnnotatedRelationshipElement) aasRelElem, submodel, relElemRef, nid);
                }
                else {
                    relElemNode = createInstance(AASRelationshipElementType.class, nid, browseName, LocalizedText.english(name));
                }

                if (relElemNode != null) {
                    SubmodelElementCreator.addSubmodelElementBaseData(relElemNode, aasRelElem, this);

                    AasSubmodelElementHelper.setAasReferenceData(aasRelElem.getFirst(), relElemNode.getFirstNode(), false);
                    AasSubmodelElementHelper.setAasReferenceData(aasRelElem.getSecond(), relElemNode.getSecondNode(), false);

                    submodelElementAasMap.put(relElemNode.getFirstNode().getKeysNode().getNodeId(),
                            new SubmodelElementData(aasRelElem, submodel, SubmodelElementData.Type.RELATIONSHIP_ELEMENT_FIRST, relElemRef));
                    submodelElementAasMap.put(relElemNode.getSecondNode().getKeysNode().getNodeId(),
                            new SubmodelElementData(aasRelElem, submodel, SubmodelElementData.Type.RELATIONSHIP_ELEMENT_SECOND, relElemRef));

                    submodelElementOpcUAMap.put(relElemRef, relElemNode);

                    if (ordered) {
                        node.addReference(relElemNode, Identifiers.HasOrderedComponent, false);
                    }
                    else {
                        node.addComponent(relElemNode);
                    }

                    referableMap.put(relElemRef, new ObjectData(aasRelElem, relElemNode, submodel));
                }
            }
        }
        catch (Exception ex) {
            LOG.error("addAasRelationshipElement Exception", ex);
            throw ex;
        }
    }


    /**
     * Creates an Annotated Relationship Element.
     *
     * @param aasRelElem The AAS Annotated Relationship Element
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param relElemRef The AAS reference to the AnnotatedRelationshipElement
     * @param nodeId The desired NodeId for the node to be created
     * @return The create UA Annotated Relationship Element
     * @throws StatusException If the operation fails
     */
    private AASRelationshipElementType createAnnotatedRelationshipElement(AnnotatedRelationshipElement aasRelElem, Submodel submodel, Reference relElemRef, NodeId nodeId)
            throws StatusException {
        AASRelationshipElementType retval = null;

        try {
            AASAnnotatedRelationshipElementType relElemNode = createInstance(
                    AASAnnotatedRelationshipElementType.class, nodeId, UaQualifiedName
                            .from(opc.i4aas.ObjectTypeIds.AASAnnotatedRelationshipElementType.getNamespaceUri(), aasRelElem.getIdShort()).toQualifiedName(getNamespaceTable()),
                    LocalizedText.english(aasRelElem.getIdShort()));

            // Annotations 
            for (DataElement de: aasRelElem.getAnnotations()) {
                DataElementCreator.addAasDataElement(relElemNode.getAnnotationNode(), de, submodel, relElemRef, false, this);
            }

            retval = relElemNode;
        }
        catch (Exception ex) {
            LOG.error("createAnnotatedRelationshipElement Exception", ex);
            throw ex;
        }

        return retval;
    }


    /**
     * Adds a SubmodelElementCollection to the given node.
     *
     * @param node The desired UA node
     * @param aasColl The corresponding SubmodelElementCollection to add
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param parentRef The AAS reference to the parent object
     * @param ordered Specifies whether the entity should be added ordered
     *            (true) or unordered (false)
     * @throws StatusException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    private void addAasSubmodelElementCollection(UaNode node, SubmodelElementCollection aasColl, Submodel submodel, Reference parentRef, boolean ordered)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        try {
            if ((node != null) && (aasColl != null)) {
                String name = aasColl.getIdShort();
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASSubmodelElementCollectionType.getNamespaceUri(), name)
                        .toQualifiedName(getNamespaceTable());
                NodeId nid = getDefaultNodeId();
                AASSubmodelElementCollectionType collNode;
                if (aasColl.getOrdered()) {
                    collNode = createAasOrderedSubmodelElementCollection(name, nid);
                }
                else {
                    collNode = createInstance(AASSubmodelElementCollectionType.class, nid, browseName, LocalizedText.english(name));
                }

                SubmodelElementCreator.addSubmodelElementBaseData(collNode, aasColl, this);

                // AllowDuplicates
                if (collNode.getAllowDuplicatesNode() == null) {
                    NodeId myPropertyId = new NodeId(getNamespaceIndex(), collNode.getNodeId().getValue().toString() + "." + AASSubmodelElementCollectionType.ALLOW_DUPLICATES);
                    PlainProperty<Boolean> myProperty = new PlainProperty<>(this, myPropertyId,
                            UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASSubmodelElementCollectionType.getNamespaceUri(), AASSubmodelElementCollectionType.ALLOW_DUPLICATES)
                                    .toQualifiedName(getNamespaceTable()),
                            LocalizedText.english(AASSubmodelElementCollectionType.ALLOW_DUPLICATES));
                    myProperty.setDataTypeId(Identifiers.Boolean);
                    myProperty.setDescription(new LocalizedText("", ""));
                    if (VALUES_READ_ONLY) {
                        myProperty.setAccessLevel(AccessLevelType.CurrentRead);
                    }
                    collNode.addProperty(myProperty);
                }

                collNode.setAllowDuplicates(aasColl.getAllowDuplicates());

                Reference collRef = AasUtils.toReference(parentRef, aasColl);

                // SubmodelElements 
                addSubmodelElements(collNode, aasColl.getValues(), submodel, collRef, aasColl.getOrdered());

                if (ordered) {
                    node.addReference(collNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(collNode);
                }

                referableMap.put(collRef, new ObjectData(aasColl, collNode, submodel));
            }
        }
        catch (Exception ex) {
            LOG.error("createAasSubmodelElementCollection Exception", ex);
            throw ex;
        }
    }


    /**
     * Creates an AAS Ordered Submodel Element Collection.
     *
     * @param name The desired name
     * @param nid The desired NodeId
     * @return The created Ordered Submodel Element Collection object
     */
    private AASSubmodelElementCollectionType createAasOrderedSubmodelElementCollection(String name, NodeId nid) {
        AASSubmodelElementCollectionType retval = null;

        try {
            AASOrderedSubmodelElementCollectionType orderedNode = createInstance(AASOrderedSubmodelElementCollectionType.class, nid,
                    UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASOrderedSubmodelElementCollectionType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable()),
                    LocalizedText.english(name));

            retval = orderedNode;
        }
        catch (Exception ex) {
            LOG.error("createAasOrderedSubmodelElementCollection Exception", ex);
            throw ex;
        }

        return retval;
    }


    /**
     * Subscribes to Events on the MessageBus (e.g. ValueChangeEvents).
     * 
     * @throws MessageBusException if subscribing fails
     */
    private void subscribeMessageBus() throws MessageBusException {
        try {
            if (messageBus != null) {
                LOG.debug("subscribeMessageBus: subscribe ValueChangeEvents");
                SubscriptionInfo info = SubscriptionInfo.create(ValueChangeEventMessage.class, t -> {
                    try {
                        valueChanged(t.getElement(), t.getNewValue(), t.getOldValue());
                    }
                    catch (StatusException e) {
                        LOG.error("valueChanged Exception", e);
                    }
                });
                SubscriptionId rv = messageBus.subscribe(info);
                subscriptions.add(rv);

                info = SubscriptionInfo.create(ElementCreateEventMessage.class, x -> {
                    try {
                        elementCreated(x.getElement(), x.getValue());
                    }
                    catch (Exception e) {
                        LOG.error("elementCreated Exception", e);
                    }
                });
                rv = messageBus.subscribe(info);
                subscriptions.add(rv);

                info = SubscriptionInfo.create(ElementDeleteEventMessage.class, x -> {
                    try {
                        elementDeleted(x.getElement());
                    }
                    catch (Exception e) {
                        LOG.error("elementDeleted Exception", e);
                    }
                });
                rv = messageBus.subscribe(info);
                subscriptions.add(rv);

                info = SubscriptionInfo.create(ElementUpdateEventMessage.class, x -> {
                    try {
                        elementUpdated(x.getElement(), x.getValue());
                    }
                    catch (Exception e) {
                        LOG.error("elementUpdated Exception", e);
                    }
                });
                rv = messageBus.subscribe(info);
                subscriptions.add(rv);
            }
            else {
                LOG.warn("MessageBus not available!");
            }
        }
        catch (Exception ex) {
            LOG.error("subscribeMessageBus Exception", ex);
            throw ex;
        }
    }


    /**
     * Handles a ValueChanged event.
     *
     * @param element The Reference to the changed element
     * @param newValue The new value
     * @param oldValue The old value
     * @throws StatusException If the operation fails
     */
    private void valueChanged(Reference element, ElementValue newValue, ElementValue oldValue) throws StatusException {
        try {
            updateSubmodelElementValue(element, newValue, oldValue);
        }
        catch (Exception ex) {
            LOG.error("valueChanged Exception", ex);
            throw ex;
        }
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
     */
    private void elementCreated(Reference element, Referable value) throws StatusException, ServiceResultException, ServiceException, AddressSpaceException {
        if (element == null) {
            throw new IllegalArgumentException(ELEMENT_NULL);
        }
        else if (value == null) {
            throw new IllegalArgumentException(VALUE_NULL);
        }

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("elementCreated called. Reference {}", AasUtils.asString(element));
            }
            // The element is the parent object where the value is added
            ObjectData parent = null;
            if (referableMap.containsKey(element)) {
                parent = referableMap.get(element);
            }
            else if (LOG.isInfoEnabled()) {
                LOG.info("elementCreated: element not found in referableMap: {}", AasUtils.asString(element));
            }

            if (value instanceof ConceptDescription) {
                ConceptDescriptionCreator.addConceptDescriptions(List.of((ConceptDescription) value), this);
            }
            else if (value instanceof Asset) {
                addAsset(aasEnvironmentNode, (Asset) value);
            }
            else if (value instanceof Submodel) {
                addSubmodel(aasEnvironmentNode, (Submodel) value);
            }
            else if (value instanceof AssetAdministrationShell) {
                AssetAdministrationShellCreator.addAssetAdministrationShell(aasEnvironmentNode, (AssetAdministrationShell) value, this);
            }
            else if (parent != null) {
                if (value instanceof EmbeddedDataSpecification) {
                    if (parent.getNode() instanceof AASAssetAdministrationShellType) {
                        EmbeddedDataSpecificationCreator.addEmbeddedDataSpecifications((AASAssetAdministrationShellType) parent.getNode(),
                                List.of((EmbeddedDataSpecification) value), this);
                    }
                    else if (parent.getNode() instanceof AASSubmodelType) {
                        EmbeddedDataSpecificationCreator.addEmbeddedDataSpecifications((AASSubmodelType) parent.getNode(), List.of((EmbeddedDataSpecification) value), this);
                    }
                    else if (parent.getNode() instanceof AASSubmodelElementType) {
                        EmbeddedDataSpecificationCreator.addEmbeddedDataSpecifications((AASSubmodelElementType) parent.getNode(), List.of((EmbeddedDataSpecification) value), this);
                    }
                    else if (parent.getNode() instanceof AASAssetType) {
                        EmbeddedDataSpecificationCreator.addEmbeddedDataSpecifications((AASAssetType) parent.getNode(), List.of((EmbeddedDataSpecification) value), this);
                    }
                    else {
                        LOG.warn("elementCreated: EmbeddedDataSpecification parent class not found");
                    }
                }
                else if (value instanceof Constraint) {
                    if (parent.getNode() instanceof AASSubmodelType) {
                        QualifierCreator.addQualifiers(((AASSubmodelType) parent.getNode()).getQualifierNode(), List.of((Constraint) value), this);
                    }
                    else if (parent.getNode() instanceof AASSubmodelElementType) {
                        QualifierCreator.addQualifiers(((AASSubmodelElementType) parent.getNode()).getQualifierNode(), List.of((Constraint) value), this);
                    }
                    else {
                        LOG.warn("elementCreated: Constraint parent class not found");
                    }
                }
                else if (value instanceof SubmodelElement) {
                    if (parent.getNode() instanceof AASSubmodelType) {
                        LOG.trace("elementCreated: call addSubmodelElements");
                        addSubmodelElements(parent.getNode(), List.of((SubmodelElement) value), (Submodel) parent.getReferable(), element);
                    }
                    else if (parent.getNode() instanceof AASSubmodelElementType) {
                        LOG.trace("elementCreated: call addSubmodelElements");
                        addSubmodelElements(parent.getNode(), List.of((SubmodelElement) value), parent.getSubmodel(), element);
                    }
                    else {
                        LOG.warn("elementCreated: SubmodelElement parent class not found: {}; {}", parent.getNode().getNodeId(), parent.getNode());
                    }
                }
            }
            else if (LOG.isWarnEnabled()) {
                LOG.warn("elementCreated: element not found: {}", AasUtils.asString(element));
            }
        }
        catch (Exception ex) {
            LOG.error("elementCreated Exception", ex);
            throw ex;
        }
    }


    /**
     * Handles an elementDeleted event.
     *
     * @param element Reference to the deleted element.
     * @throws StatusException If the operation fails
     */
    private void elementDeleted(Reference element) throws StatusException {
        if (element == null) {
            throw new IllegalArgumentException(ELEMENT_NULL);
        }

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("elementDeleted called. Reference {}", AasUtils.asString(element));
            }
            // The element is the object that should be deleted
            ObjectData data = null;
            if (referableMap.containsKey(element)) {
                data = referableMap.get(element);

                // remove element from the map
                referableMap.remove(element);
            }
            else if (LOG.isInfoEnabled()) {
                LOG.info("elementDeleted: element not found in referableMap: {}", AasUtils.asString(element));
            }

            if (data != null) {
                removeFromMaps(data.getNode(), element, data.getReferable());
                deleteNode(data.getNode(), true, true);
            }
        }
        catch (Exception ex) {
            LOG.error("elementDeleted Exception", ex);
            throw ex;
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
     */
    private void elementUpdated(Reference element, Referable value) throws StatusException, ServiceResultException, ServiceException, AddressSpaceException {
        if (element == null) {
            throw new IllegalArgumentException(ELEMENT_NULL);
        }
        else if (value == null) {
            throw new IllegalArgumentException(VALUE_NULL);
        }

        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("elementUpdated called. Reference {}", AasUtils.asString(element));
            }
            // Currently we implement update as delete and create. 
            elementDeleted(element);

            // elementCreated needs the parent as element 
            List<Key> keys = element.getKeys();
            if (keys.size() > 1) {
                // remove the last element from the list
                keys.remove(keys.size() - 1);
            }
            element.setKeys(keys);
            elementCreated(element, value);
        }
        catch (Exception ex) {
            LOG.error("elementUpdated Exception", ex);
            throw ex;
        }
    }


    /**
     * Unsubscribes from the MessageBus.
     * 
     * @throws MessageBusException if subscribing fails
     */
    private void unsubscribeMessageBus() throws MessageBusException {
        try {
            if (messageBus != null) {
                LOG.debug("unsubscribe from the MessageBus");
                for (int i = 0; i < subscriptions.size(); i++) {
                    messageBus.unsubscribe(subscriptions.get(i));
                }
            }
        }
        catch (Exception ex) {
            LOG.error("unsubscribeMessageBus Exception", ex);
            throw ex;
        }
        finally {
            subscriptions.clear();
        }
    }


    /**
     * Update the value of a SubmodelElement.
     *
     * @param reference The reference of the desired SubmodelElement
     * @param newValue The new value of the SubmodelElement
     * @param oldValue The old value of the SubmodelElement
     * @throws StatusException If the operation fails
     */
    public void updateSubmodelElementValue(Reference reference, ElementValue newValue, ElementValue oldValue) throws StatusException {
        if (reference == null) {
            throw new IllegalArgumentException("reference is null");
        }
        else if (newValue == null) {
            throw new IllegalArgumentException("newValue is null");
        }

        LOG.debug("updateSubmodelElementValue");
        if (submodelElementOpcUAMap.containsKey(reference)) {
            AASSubmodelElementType subElem = submodelElementOpcUAMap.get(reference);
            AasSubmodelElementHelper.setSubmodelElementValue(subElem, newValue);
        }
        else if (LOG.isWarnEnabled()) {
            LOG.warn("SubmodelElement {} not found in submodelElementOpcUAMap", AasUtils.asString(reference));
        }
    }


    /**
     * Gets the next availabe default NodeId.
     * 
     * @return The desired NodeId
     */
    public NodeId getDefaultNodeId() {
        int nr = ++nodeIdCounter;
        return new NodeId(getNamespaceIndex(), nr);
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
        UaNode retval = null;
        if (submodelOpcUAMap.containsKey(reference)) {
            retval = submodelOpcUAMap.get(reference);
        }

        return retval;
    }


    /**
     * Adds a SubmodelElement to the submodelElementOpcUAMap.
     * 
     * @param reference The reference to the desired SubmodelElement.
     * @param submodelElement The corresponding SubmodelElement node.
     */
    public void addSubmodelElementOpcUA(Reference reference, AASSubmodelElementType submodelElement) {
        submodelElementOpcUAMap.put(reference, submodelElement);
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
     * Removes the given node (and all sub-nodes) from the maps.
     * 
     * @param node The desired node
     * @param reference The reference to the desired SubmodelElement
     * @param referable The corresponding referable
     */
    private void removeFromMaps(BaseObjectType node, Reference reference, Referable referable) {
        if (node == null) {
            throw new IllegalArgumentException(NODE_NULL);
        }

        try {
            if (node instanceof AASSubmodelElementType) {
                doRemoveFromMaps((AASSubmodelElementType) node, reference, referable);
            }
            else if (referable instanceof Submodel) {
                doRemoveFromMaps(reference, (Submodel) referable);
            }

            // no special treatment necessary for other types like AssetAdministrationShell, Asset or others
        }
        catch (Exception ex) {
            // This exception is not thrown here. We ignore the error.
            LOG.error("removeFromMaps Exception", ex);
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
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("doRemoveFromMaps: remove SubmodelElement {}", AasUtils.asString(reference));
            }
            if (submodelElementOpcUAMap.containsKey(reference)) {
                submodelElementOpcUAMap.remove(reference);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("doRemoveFromMaps: remove SubmodelElement from submodelElementOpcUAMap: {}", AasUtils.asString(reference));
                }
            }

            if (element instanceof AASPropertyType) {
                AASPropertyType prop = (AASPropertyType) element;
                if (submodelElementAasMap.containsKey(prop.getValueNode().getNodeId())) {
                    submodelElementAasMap.remove(prop.getValueNode().getNodeId());
                    LOG.debug("doRemoveFromMaps: remove Property NodeId {}", prop.getValueNode().getNodeId());
                }
            }
            else if (element instanceof AASRangeType) {
                AASRangeType range = (AASRangeType) element;
                if (submodelElementAasMap.containsKey(range.getMinNode().getNodeId())) {
                    submodelElementAasMap.remove(range.getMinNode().getNodeId());
                    LOG.debug("doRemoveFromMaps: remove Range Min NodeId {}", range.getMinNode().getNodeId());
                }

                if (submodelElementAasMap.containsKey(range.getMaxNode().getNodeId())) {
                    submodelElementAasMap.remove(range.getMaxNode().getNodeId());
                    LOG.debug("doRemoveFromMaps: remove Range Max NodeId {}", range.getMaxNode().getNodeId());
                }
            }
            else if (element instanceof AASOperationType) {
                AASOperationType oper = (AASOperationType) element;
                if (submodelElementAasMap.containsKey(oper.getOperationNode().getNodeId())) {
                    submodelElementAasMap.remove(oper.getOperationNode().getNodeId());
                    LOG.debug("doRemoveFromMaps: remove Operation NodeId {}", oper.getOperationNode().getNodeId());
                }
            }
            else if (element instanceof AASBlobType) {
                AASBlobType blob = (AASBlobType) element;
                if (submodelElementAasMap.containsKey(blob.getValueNode().getNodeId())) {
                    submodelElementAasMap.remove(blob.getValueNode().getNodeId());
                    LOG.debug("doRemoveFromMaps: remove Blob NodeId {}", blob.getValueNode().getNodeId());
                }
            }
            else if (element instanceof AASMultiLanguagePropertyType) {
                AASMultiLanguagePropertyType mlp = (AASMultiLanguagePropertyType) element;
                if (submodelElementAasMap.containsKey(mlp.getValueNode().getNodeId())) {
                    submodelElementAasMap.remove(mlp.getValueNode().getNodeId());
                    LOG.debug("doRemoveFromMaps: remove AASMultiLanguageProperty NodeId {}", mlp.getValueNode().getNodeId());
                }
            }
            else if (element instanceof AASReferenceElementType) {
                AASReferenceElementType refElem = (AASReferenceElementType) element;
                NodeId nid = refElem.getValueNode().getKeysNode().getNodeId();
                if (submodelElementAasMap.containsKey(nid)) {
                    submodelElementAasMap.remove(nid);
                    LOG.debug("doRemoveFromMaps: remove AASReferenceElement NodeId {}", nid);
                }
            }
            else if (element instanceof AASRelationshipElementType) {
                AASRelationshipElementType relElem = (AASRelationshipElementType) element;
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
            else if (element instanceof AASEntityType) {
                AASEntityType ent = (AASEntityType) element;
                if ((ent.getGlobalAssetIdNode() != null) && (ent.getGlobalAssetIdNode().getKeysNode() != null)) {
                    NodeId nid = ent.getGlobalAssetIdNode().getKeysNode().getNodeId();
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
            else if (referable instanceof SubmodelElementCollection) {
                SubmodelElementCollection sec = (SubmodelElementCollection) referable;
                for (SubmodelElement se: sec.getValues()) {
                    doRemoveFromMaps(reference, se);
                }
            }

            // Capability and File are currently not relevant here
        }
        catch (Exception ex) {
            LOG.error("doRemoveFromMaps Exception", ex);
            throw ex;
        }
    }


    /**
     * Removes the given SubmodelElement from the maps.
     * 
     * @param parent The reference to the parent element.
     * @param de The desired SubmodelElement
     */
    private void doRemoveFromMaps(Reference parent, SubmodelElement de) {
        try {
            Reference ref = AasUtils.toReference(parent, de);
            ObjectData element = null;
            if (referableMap.containsKey(ref)) {
                element = referableMap.get(ref);

                if (element.getNode() instanceof AASSubmodelElementType) {
                    doRemoveFromMaps((AASSubmodelElementType) element.getNode(), ref, de);
                }

                // remove element from the map
                referableMap.remove(ref);
            }
            else if (LOG.isInfoEnabled()) {
                LOG.info("doRemoveFromMaps: element not found in referableMap: {}", AasUtils.asString(ref));
            }
        }
        catch (Exception ex) {
            LOG.error("doRemoveFromMaps Exception", ex);
            throw ex;
        }
    }


    /**
     * Removes the given SubmodelElement from the maps.
     * 
     * @param reference The reference to the desired submodel.
     * @param submodel The desired submodel
     */
    private void doRemoveFromMaps(Reference reference, Submodel submodel) {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug("doRemoveFromMaps: remove submodel {}", AasUtils.asString(reference));
            }
            for (SubmodelElement element: submodel.getSubmodelElements()) {
                doRemoveFromMaps(reference, element);
            }

            if (submodelOpcUAMap.containsKey(reference)) {
                submodelOpcUAMap.remove(reference);
            }
        }
        catch (Exception ex) {
            LOG.error("doRemoveFromMaps (SM) Exception", ex);
            throw ex;
        }
    }
}
