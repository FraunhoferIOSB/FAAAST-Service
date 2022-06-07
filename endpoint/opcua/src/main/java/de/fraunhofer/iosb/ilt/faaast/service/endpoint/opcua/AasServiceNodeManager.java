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
import com.prosysopc.ua.UaBrowsePath;
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
import com.prosysopc.ua.server.instantiation.TypeDefinitionBasedNodeBuilderConfiguration;
import com.prosysopc.ua.server.nodes.PlainMethod;
import com.prosysopc.ua.server.nodes.PlainProperty;
import com.prosysopc.ua.stack.builtintypes.ByteString;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.Argument;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.types.opcua.BaseObjectType;
import com.prosysopc.ua.types.opcua.DictionaryEntryType;
import com.prosysopc.ua.types.opcua.FolderType;
import com.prosysopc.ua.types.opcua.server.FileTypeNode;
import com.prosysopc.ua.types.opcua.server.FolderTypeNode;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.SubmodelElementData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.AasSubmodelElementHelper;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.ValueData;
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
import io.adminshell.aas.v3.model.AdministrativeInformation;
import io.adminshell.aas.v3.model.AnnotatedRelationshipElement;
import io.adminshell.aas.v3.model.Asset;
import io.adminshell.aas.v3.model.AssetAdministrationShell;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.AssetInformation;
import io.adminshell.aas.v3.model.AssetKind;
import io.adminshell.aas.v3.model.BasicEvent;
import io.adminshell.aas.v3.model.Blob;
import io.adminshell.aas.v3.model.Capability;
import io.adminshell.aas.v3.model.ConceptDescription;
import io.adminshell.aas.v3.model.Constraint;
import io.adminshell.aas.v3.model.DataElement;
import io.adminshell.aas.v3.model.EmbeddedDataSpecification;
import io.adminshell.aas.v3.model.Entity;
import io.adminshell.aas.v3.model.Event;
import io.adminshell.aas.v3.model.File;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.Key;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.MultiLanguageProperty;
import io.adminshell.aas.v3.model.Operation;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Qualifier;
import io.adminshell.aas.v3.model.Range;
import io.adminshell.aas.v3.model.Referable;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.ReferenceElement;
import io.adminshell.aas.v3.model.RelationshipElement;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import opc.i4aas.AASAdministrativeInformationType;
import opc.i4aas.AASAnnotatedRelationshipElementType;
import opc.i4aas.AASAssetAdministrationShellType;
import opc.i4aas.AASAssetInformationType;
import opc.i4aas.AASAssetType;
import opc.i4aas.AASBlobType;
import opc.i4aas.AASCapabilityType;
import opc.i4aas.AASCustomConceptDescriptionType;
import opc.i4aas.AASEntityType;
import opc.i4aas.AASEnvironmentType;
import opc.i4aas.AASEventType;
import opc.i4aas.AASFileType;
import opc.i4aas.AASIdentifiableType;
import opc.i4aas.AASIdentifierKeyValuePairList;
import opc.i4aas.AASIdentifierKeyValuePairType;
import opc.i4aas.AASIrdiConceptDescriptionType;
import opc.i4aas.AASIriConceptDescriptionType;
import opc.i4aas.AASMultiLanguagePropertyType;
import opc.i4aas.AASOperationType;
import opc.i4aas.AASOrderedSubmodelElementCollectionType;
import opc.i4aas.AASPropertyType;
import opc.i4aas.AASQualifierList;
import opc.i4aas.AASQualifierType;
import opc.i4aas.AASRangeType;
import opc.i4aas.AASReferenceElementType;
import opc.i4aas.AASReferenceList;
import opc.i4aas.AASReferenceType;
import opc.i4aas.AASRelationshipElementType;
import opc.i4aas.AASSubmodelElementCollectionType;
import opc.i4aas.AASSubmodelElementType;
import opc.i4aas.AASSubmodelType;
import opc.i4aas.server.AASAssetAdministrationShellTypeNode;
import opc.i4aas.server.AASReferenceTypeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A Node Manager for the AAS information model
 *
 * @author Tino Bischoff
 */
public class AasServiceNodeManager extends NodeManagerUaNode {

    /**
     * Text if value is null
     */
    private static final String VALUE_NULL = "value is null";

    /**
     * Text if node is null
     */
    private static final String NODE_NULL = "node is null";

    /**
     * Text if element is null
     */
    private static final String ELEMENT_NULL = "element is null";

    /**
     * Text for addIdentifiable Exception
     */
    private static final String ADD_IDENT_EXC = "addIdentifiable Exception";

    /**
     * Text for addEmbeddedDataSpecifications Exception
     */
    private static final String ADD_EMBED_DS_EXC = "addEmbeddedDataSpecifications Exception";

    /**
     * The namespace URI of this node manager
     */
    public static final String NAMESPACE_URI = "http://www.iosb.fraunhofer.de/ILT/AAS/OPCUA";

    /**
     * Make certain variable values read-only, because writing would not make
     * sense
     */
    private static final boolean VALUES_READ_ONLY = true;

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
     * Maps AAS references to dictionary entry types
     */
    private final Map<Reference, DictionaryEntryType> dictionaryMap;

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
        dictionaryMap = new HashMap<>();
        submodelElementAasMap = new ConcurrentHashMap<>();
        submodelElementOpcUAMap = new ConcurrentHashMap<>();
        submodelOpcUAMap = new ConcurrentHashMap<>();
        referableMap = new ConcurrentHashMap<>();

        messageBus = ep.getMessageBus();
        subscriptions = new ArrayList<>();
    }


    /**
     * Initializes the Node Manager.
     *
     * @throws StatusException If the operation fails
     * @throws UaNodeFactoryException Error creating nodes
     */
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


    /**
     * Closes the NodeManager
     */
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
            LOG.info("createAddressSpace");

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
                addConceptDescriptions(aasEnvironment.getConceptDescriptions());

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
                addAssetAdministrationShell(aas);
            }
        }
        catch (Exception ex) {
            LOG.error("addAssetAdministrationShells Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the given AssetAdministrationShell.
     * 
     * @throws StatusException If the operation fails
     */
    private void addAssetAdministrationShell(AssetAdministrationShell aas) throws StatusException {
        try {
            TypeDefinitionBasedNodeBuilderConfiguration.Builder conf = TypeDefinitionBasedNodeBuilderConfiguration.builder();
            Reference derivedFrom = aas.getDerivedFrom();
            if (derivedFrom != null) {
                UaBrowsePath bp = UaBrowsePath.from(opc.i4aas.ObjectTypeIds.AASAssetAdministrationShellType,
                        UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASAssetAdministrationShellType.getNamespaceUri(), AASAssetAdministrationShellType.DERIVED_FROM));
                conf.addOptional(bp);
            }

            this.setNodeBuilderConfiguration(conf.build());

            QualifiedName browseName = UaQualifiedName.from(NAMESPACE_URI, aas.getIdShort()).toQualifiedName(getNamespaceTable());
            String displayName = "AAS:" + aas.getIdShort();
            NodeId nid = new NodeId(getNamespaceIndex(), aas.getIdShort());
            if (findNode(nid) != null) {
                // The NodeId already exists
                nid = getDefaultNodeId();
            }

            AASAssetAdministrationShellType aasShell = createInstance(AASAssetAdministrationShellTypeNode.class, nid, browseName, LocalizedText.english(displayName));
            addIdentifiable(aasShell, aas.getIdentification(), aas.getAdministration(), aas.getCategory());

            // DataSpecifications
            addEmbeddedDataSpecifications(aasShell, aas.getEmbeddedDataSpecifications());

            // AssetInformation
            AssetInformation assetInformation = aas.getAssetInformation();
            if (assetInformation != null) {
                addAssetInformation(aasShell, assetInformation);
            }

            // submodel references
            List<Reference> submodelRefs = aas.getSubmodels();
            if ((submodelRefs != null) && (!submodelRefs.isEmpty())) {
                addSubmodelReferences(aasShell, submodelRefs);
            }

            // add AAS to Environment
            addNodeAndReference(aasEnvironmentNode, aasShell, Identifiers.Organizes);

            referableMap.put(AasUtils.toReference(aas), new ObjectData(aas, aasShell));
        }
        catch (Exception ex) {
            LOG.error("addAssetAdministrationShell Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the given list of AAS Concept Descriptions.
     *
     * @param descriptions The desired list of AAS Concept Descriptions
     * @throws StatusException If the operation fails
     */
    private void addConceptDescriptions(List<ConceptDescription> descriptions) throws StatusException {
        try {
            // create folder DictionaryEntries
            final UaNode dictionariesFolder = getServer().getNodeManagerRoot().getNodeOrExternal(Identifiers.Dictionaries);

            // Folder for my objects
            final NodeId dictionarieEntriesFolderId = new NodeId(getNamespaceIndex(), "Dictionaries.DictionaryEntries");
            FolderTypeNode dictEntriesFolder = createInstance(FolderTypeNode.class, "DictionaryEntries", dictionarieEntriesFolderId);

            this.addNodeAndReference(dictionariesFolder, dictEntriesFolder, Identifiers.Organizes);

            for (ConceptDescription c: descriptions) {
                String name = c.getIdShort();
                NodeId nid = createNodeId(dictionariesFolder, name);
                DictionaryEntryType dictNode;
                switch (c.getIdentification().getIdType()) {
                    case IRDI:
                        AASIrdiConceptDescriptionType irdiNode = createInstance(AASIrdiConceptDescriptionType.class, name, nid);
                        addIdentifiable(irdiNode, c.getIdentification(), c.getAdministration(), name);
                        addConceptDescriptionReference(irdiNode, getReference(c));
                        dictEntriesFolder.addComponent(irdiNode);
                        dictionaryMap.put(getReference(c), irdiNode);
                        dictNode = irdiNode;
                        break;

                    case IRI:
                        AASIriConceptDescriptionType iriNode = createInstance(AASIriConceptDescriptionType.class, name, nid);
                        addIdentifiable(iriNode, c.getIdentification(), c.getAdministration(), name);
                        addConceptDescriptionReference(iriNode, getReference(c));
                        dictEntriesFolder.addComponent(iriNode);
                        dictionaryMap.put(getReference(c), iriNode);
                        dictNode = iriNode;
                        break;

                    default:
                        AASCustomConceptDescriptionType customNode = createInstance(AASCustomConceptDescriptionType.class, name, nid);
                        addIdentifiable(customNode, c.getIdentification(), c.getAdministration(), name);
                        addConceptDescriptionReference(customNode, getReference(c));
                        dictEntriesFolder.addComponent(customNode);
                        dictionaryMap.put(getReference(c), customNode);
                        dictNode = customNode;
                        break;
                }

                referableMap.put(AasUtils.toReference(c), new ObjectData(c, dictNode));
            }
        }
        catch (Exception ex) {
            LOG.error("addConceptDescriptions Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds AAS Identifiable information to the given node.
     *
     * @param identifiableNode The desired node where the Identifiable
     *            information should be added
     * @param identifier The corresponding AAS Identifier
     * @param adminInfo The corresponding AAS Administrative Information
     * @param category The desired category
     */
    private void addIdentifiable(AASIdentifiableType identifiableNode, Identifier identifier, AdministrativeInformation adminInfo, String category) {
        try {
            if (identifier != null) {
                identifiableNode.getIdentificationNode().setId(identifier.getIdentifier());
                identifiableNode.getIdentificationNode().setIdType(ValueConverter.convertIdentifierType(identifier.getIdType()));
            }

            addAdminInformationProperties(identifiableNode.getAdministrationNode(), adminInfo);

            if (category == null) {
                category = "";
            }
            identifiableNode.setCategory(category);

            if (VALUES_READ_ONLY) {
                identifiableNode.getIdentificationNode().getIdNode().setAccessLevel(AccessLevelType.CurrentRead);
                identifiableNode.getIdentificationNode().getIdTypeNode().setAccessLevel(AccessLevelType.CurrentRead);
                identifiableNode.getCategoryNode().setAccessLevel(AccessLevelType.CurrentRead);
            }
        }
        catch (Exception ex) {
            LOG.error(ADD_IDENT_EXC, ex);
        }
    }


    /**
     * Adds AAS Identifiable information to the given node.
     *
     * @param conceptDescriptionNode The desired node where the Identifiable
     *            information should be added
     * @param identifier The corresponding AAS Identifier
     * @param adminInfo The corresponding AAS Administrative Information
     * @param category The desired category
     */
    private void addIdentifiable(AASIrdiConceptDescriptionType conceptDescriptionNode, Identifier identifier, AdministrativeInformation adminInfo, String category) {
        try {
            if (identifier != null) {
                conceptDescriptionNode.getIdentificationNode().setId(identifier.getIdentifier());
                conceptDescriptionNode.getIdentificationNode().setIdType(ValueConverter.convertIdentifierType(identifier.getIdType()));
            }

            addAdminInformationProperties(conceptDescriptionNode.getAdministrationNode(), adminInfo);

            if (category == null) {
                category = "";
            }
            conceptDescriptionNode.setCategory(category);

            if (VALUES_READ_ONLY) {
                conceptDescriptionNode.getIdentificationNode().getIdNode().setAccessLevel(AccessLevelType.CurrentRead);
                conceptDescriptionNode.getIdentificationNode().getIdTypeNode().setAccessLevel(AccessLevelType.CurrentRead);
                conceptDescriptionNode.getCategoryNode().setAccessLevel(AccessLevelType.CurrentRead);
            }
        }
        catch (Exception ex) {
            LOG.error(ADD_IDENT_EXC, ex);
        }
    }


    /**
     * Adds AAS Identifiable information to the given node.
     *
     * @param conceptDescriptionNode The desired node where the Identifiable
     *            information should be added
     * @param identifier The corresponding AAS Identifier
     * @param adminInfo The corresponding AAS Administrative Information
     * @param category The desired category
     */
    private void addIdentifiable(AASIriConceptDescriptionType conceptDescriptionNode, Identifier identifier, AdministrativeInformation adminInfo, String category) {
        try {
            if (identifier != null) {
                conceptDescriptionNode.getIdentificationNode().setId(identifier.getIdentifier());
                conceptDescriptionNode.getIdentificationNode().setIdType(ValueConverter.convertIdentifierType(identifier.getIdType()));
            }

            addAdminInformationProperties(conceptDescriptionNode.getAdministrationNode(), adminInfo);

            if (category == null) {
                category = "";
            }
            conceptDescriptionNode.setCategory(category);

            if (VALUES_READ_ONLY) {
                conceptDescriptionNode.getIdentificationNode().getIdNode().setAccessLevel(AccessLevelType.CurrentRead);
                conceptDescriptionNode.getIdentificationNode().getIdTypeNode().setAccessLevel(AccessLevelType.CurrentRead);
                conceptDescriptionNode.getCategoryNode().setAccessLevel(AccessLevelType.CurrentRead);
            }
        }
        catch (Exception ex) {
            LOG.error(ADD_IDENT_EXC, ex);
        }
    }


    /**
     * Adds AAS Identifiable information to the given node.
     *
     * @param conceptDescriptionNode The desired node where the Identifiable
     *            information should be added
     * @param identifier The corresponding AAS Identifier
     * @param adminInfo The corresponding AAS Administrative Information
     * @param category The desired category
     */
    private void addIdentifiable(AASCustomConceptDescriptionType conceptDescriptionNode, Identifier identifier, AdministrativeInformation adminInfo, String category) {
        try {
            if (identifier != null) {
                conceptDescriptionNode.getIdentificationNode().setId(identifier.getIdentifier());
                conceptDescriptionNode.getIdentificationNode().setIdType(ValueConverter.convertIdentifierType(identifier.getIdType()));
            }

            addAdminInformationProperties(conceptDescriptionNode.getAdministrationNode(), adminInfo);

            if (category == null) {
                category = "";
            }
            conceptDescriptionNode.setCategory(category);

            if (VALUES_READ_ONLY) {
                conceptDescriptionNode.getIdentificationNode().getIdNode().setAccessLevel(AccessLevelType.CurrentRead);
                conceptDescriptionNode.getIdentificationNode().getIdTypeNode().setAccessLevel(AccessLevelType.CurrentRead);
                conceptDescriptionNode.getCategoryNode().setAccessLevel(AccessLevelType.CurrentRead);
            }
        }
        catch (Exception ex) {
            LOG.error(ADD_IDENT_EXC, ex);
        }
    }


    /**
     * Adds the AdminInformation Properties to the given node (if they don't
     * exist).
     *
     * @param adminInfNode The desired AdminInformation node
     * @param info The corresponding AAS AdministrativeInformation object
     */
    private void addAdminInformationProperties(AASAdministrativeInformationType adminInfNode, AdministrativeInformation info) {
        try {
            if ((adminInfNode != null) && (info != null)) {
                if (info.getVersion() != null) {
                    if (adminInfNode.getVersionNode() == null) {
                        NodeId myPropertyId = new NodeId(getNamespaceIndex(), adminInfNode.getNodeId().getValue().toString() + "." + AASAdministrativeInformationType.VERSION);
                        PlainProperty<String> myProperty = new PlainProperty<>(this, myPropertyId,
                                UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASAssetAdministrationShellType.getNamespaceUri(), AASAdministrativeInformationType.VERSION)
                                        .toQualifiedName(getNamespaceTable()),
                                LocalizedText.english(AASAdministrativeInformationType.VERSION));
                        myProperty.setDataTypeId(Identifiers.String);
                        if (VALUES_READ_ONLY) {
                            myProperty.setAccessLevel(AccessLevelType.CurrentRead);
                        }
                        myProperty.setDescription(new LocalizedText("", ""));
                        adminInfNode.addProperty(myProperty);
                    }

                    adminInfNode.setVersion(info.getVersion());
                }

                if (info.getRevision() != null) {
                    if (adminInfNode.getRevisionNode() == null) {
                        NodeId myPropertyId = new NodeId(getNamespaceIndex(), adminInfNode.getNodeId().getValue().toString() + "." + AASAdministrativeInformationType.REVISION);
                        PlainProperty<String> myProperty = new PlainProperty<>(this, myPropertyId,
                                UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASAssetAdministrationShellType.getNamespaceUri(), AASAdministrativeInformationType.REVISION)
                                        .toQualifiedName(getNamespaceTable()),
                                LocalizedText.english(AASAdministrativeInformationType.REVISION));
                        myProperty.setDataTypeId(Identifiers.String);
                        if (VALUES_READ_ONLY) {
                            myProperty.setAccessLevel(AccessLevelType.CurrentRead);
                        }
                        myProperty.setDescription(new LocalizedText("", ""));
                        adminInfNode.addProperty(myProperty);
                    }

                    adminInfNode.setRevision(info.getRevision());
                }
            }
        }
        catch (Exception ex) {
            LOG.error("addAdminInfProperties Exception", ex);
        }
    }


    /**
     * Creates a reference for the given ConceptDescription.
     *
     * @param cd The desired ConceptDescription
     * @return The created reference
     */
    private Reference getReference(ConceptDescription cd) {
        return AasUtils.toReference(cd);
    }


    /**
     * Adds a reference to a ConceptDescription.
     *
     * @param node The desired UA node
     * @param ref The reference to create
     * @throws StatusException If the operation fails
     */
    private void addConceptDescriptionReference(UaNode node, Reference ref) throws StatusException {
        try {
            if (ref != null) {
                String name = "ConceptDescription";
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASMultiLanguagePropertyType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(node, browseName);
                AASReferenceType nodeRef = createInstance(AASReferenceTypeNode.class, nid, browseName, LocalizedText.english(name));

                AasSubmodelElementHelper.setAasReferenceData(ref, nodeRef);
                node.addComponent(nodeRef);
                node.addReference(nodeRef, Identifiers.HasDictionaryEntry, false);
            }
        }
        catch (Exception ex) {
            LOG.error("addConceptDescriptionReference Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AssetInformation object to the given Node.
     *
     * @param aasNode The AAS node where the AssetInformation should be added
     * @param assetInformation The desired AssetInformation object
     * @throws StatusException If the operation fails
     */
    private void addAssetInformation(AASAssetAdministrationShellType aasNode, AssetInformation assetInformation)
            throws StatusException {
        if (aasNode == null) {
            throw new IllegalArgumentException("aasNode = null");
        }
        else if (assetInformation == null) {
            throw new IllegalArgumentException("assetInformation = null");
        }

        try {
            boolean created = false;
            AASAssetInformationType assetInfoNode;
            assetInfoNode = aasNode.getAssetInformationNode();
            if (assetInfoNode == null) {
                String displayName = "AssetInformation";
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASSubmodelType.getNamespaceUri(), displayName).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(aasNode, browseName);
                assetInfoNode = createInstance(AASAssetInformationType.class, nid, browseName, LocalizedText.english(displayName));
                created = true;
            }

            if (assetInfoNode != null) {
                // AssetKind
                AssetKind assetKind = assetInformation.getAssetKind();
                assetInfoNode.setAssetKind(ValueConverter.convertAssetKind(assetKind));

                // BillOfMaterials
                List<Reference> assetBills = assetInformation.getBillOfMaterials();
                if ((assetBills != null) && (!assetBills.isEmpty())) {
                    AASReferenceList assetBillsNode = assetInfoNode.getBillOfMaterialNode();
                    addBillOfMaterials(assetBillsNode, assetBills);
                }

                // DefaultThumbnail
                File thumbnail = assetInformation.getDefaultThumbnail();
                if (thumbnail != null) {
                    addAasFile(assetInfoNode, thumbnail, null, null, false, AASAssetInformationType.DEFAULT_THUMBNAIL);
                }

                // GlobalAssetId
                Reference globalAssetId = assetInformation.getGlobalAssetId();
                if (globalAssetId != null) {
                    addAasReferenceAasNS(assetInfoNode, globalAssetId, AASAssetInformationType.GLOBAL_ASSET_ID);
                }

                // SpecificAssetIds
                List<IdentifierKeyValuePair> specificAssetIds = assetInformation.getSpecificAssetIds();
                if ((specificAssetIds != null) && (!specificAssetIds.isEmpty())) {
                    addSpecificAssetIds(assetInfoNode, specificAssetIds, "SpecificAssetIds");
                }

                if (created) {
                    aasNode.addComponent(assetInfoNode);
                }
            }
        }
        catch (Exception ex) {
            LOG.error("addAssetInformation Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the list of BillOfMaterial objects to the given Node.
     *
     * @param node The desired node where the BillOfMaterials should be added
     * @param billOfMaterials The desired list of BillOfMaterials
     * @throws StatusException If the operation fails
     */
    private void addBillOfMaterials(UaNode node, List<Reference> billOfMaterials) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException(NODE_NULL);
        }
        else if (billOfMaterials == null) {
            throw new IllegalArgumentException("billOfMaterials = null");
        }

        try {
            addAasReferenceList(node, billOfMaterials, "BillOfMaterial");
        }
        catch (Exception ex) {
            LOG.error("addBillOfMaterials Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS Reference to the given node with the AAS namespace (read-only).
     *
     * @param node The node in which the object is created
     * @param ref The desired AAS reference object to add
     * @param name The desired name
     * @return The created node
     * @throws StatusException If the operation fails
     */
    private UaNode addAasReferenceAasNS(UaNode node, Reference ref, String name) throws StatusException {
        return addAasReferenceAasNS(node, ref, name, true);
    }


    /**
     * Adds an AAS Reference to the given node with the AAS namespace.
     *
     * @param node The node in which the object is created
     * @param ref The desired AAS reference object to add
     * @param name The desired name
     * @param readOnly True if the value should be read-only
     * @return The created node
     * @throws StatusException If the operation fails
     */
    private UaNode addAasReferenceAasNS(UaNode node, Reference ref, String name, boolean readOnly) throws StatusException {
        UaNode retval = null;

        try {
            retval = addAasReference(node, ref, name, opc.i4aas.ObjectTypeIds.AASReferenceType.getNamespaceUri(), readOnly);
        }
        catch (Exception ex) {
            LOG.error("addAasReferenceAasNS Exception", ex);
            throw ex;
        }

        return retval;
    }


    /**
     * Adds an AAS Reference to the given node with the given namespace.
     *
     * @param node The node in which the object is created
     * @param ref The desired AAS reference object to add
     * @param name The desired name
     * @param namespaceUri The desired namespace URI tu use
     * @param readOnly True if the value should be read-only
     * @return The created node
     * @throws StatusException If the operation fails
     */
    private UaNode addAasReference(UaNode node, Reference ref, String name, String namespaceUri, boolean readOnly) throws StatusException {
        UaNode retval = null;

        try {
            if (ref != null) {
                QualifiedName browseName = UaQualifiedName.from(namespaceUri, name).toQualifiedName(getNamespaceTable());
                NodeId nid = getDefaultNodeId();
                AASReferenceType nodeRef = createInstance(AASReferenceType.class, nid, browseName, LocalizedText.english(name));

                LOG.debug("addAasReference: add Node {} to Node {}", nid, node.getNodeId());

                AasSubmodelElementHelper.setAasReferenceData(ref, nodeRef, readOnly);

                node.addComponent(nodeRef);

                retval = nodeRef;
            }
        }
        catch (Exception ex) {
            LOG.error("addAasReference Exception", ex);
            throw ex;
        }

        return retval;
    }


    /**
     * Adds an AAS file to the given node.
     *
     * @param node The desired UA node
     * @param aasFile The AAS file object
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param parentRef The AAS reference to the parent node
     * @param ordered Specifies whether the file should be added ordered (true) or unordered (false)
     * @param nodeName The desired Name of the node. If this value is not set,
     *            the IdShort of the file is used.
     * @throws StatusException If the operation fails
     */
    private void addAasFile(UaNode node, File aasFile, Submodel submodel, Reference parentRef, boolean ordered, String nodeName)
            throws StatusException {
        try {
            if ((node != null) && (aasFile != null)) {
                String name = aasFile.getIdShort();
                if ((nodeName != null) && (!nodeName.isEmpty())) {
                    name = nodeName;
                }

                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASFileType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = getDefaultNodeId();
                AASFileType fileNode = createInstance(AASFileType.class, nid, browseName, LocalizedText.english(name));
                addSubmodelElementBaseData(fileNode, aasFile);

                // MimeType
                if (!aasFile.getMimeType().isEmpty()) {
                    fileNode.setMimeType(aasFile.getMimeType());
                }

                // Value
                if (aasFile.getValue() != null) {
                    if (fileNode.getValueNode() == null) {
                        AasSubmodelElementHelper.addFileValueNode(fileNode);
                    }

                    fileNode.setValue(aasFile.getValue());

                    if (!aasFile.getValue().isEmpty()) {
                        java.io.File f = new java.io.File(aasFile.getValue());
                        if (!f.exists()) {
                            LOG.warn("addAasFile: File '{}' does not exist!", f.getAbsolutePath());
                        }
                        else {
                            // File Object: include only when the file exists
                            QualifiedName fileBrowseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASFileType.getNamespaceUri(), AASFileType.FILE)
                                    .toQualifiedName(getNamespaceTable());
                            NodeId fileId = new NodeId(getNamespaceIndex(), fileNode.getNodeId().getValue().toString() + "." + AASFileType.FILE);
                            FileTypeNode fileType = createInstance(FileTypeNode.class, fileId, fileBrowseName, LocalizedText.english(AASFileType.FILE));
                            fileType.setFile(new java.io.File(aasFile.getValue()));
                            fileType.setWritable(false);
                            fileType.setUserWritable(false);
                            if (fileType.getNodeVersion() != null) {
                                fileType.getNodeVersion().setDescription(new LocalizedText("", ""));
                            }

                            fileNode.addReference(fileType, Identifiers.HasAddIn, false);
                        }
                    }
                }

                if (VALUES_READ_ONLY) {
                    fileNode.getMimeTypeNode().setAccessLevel(AccessLevelType.CurrentRead);
                }

                if (ordered) {
                    node.addReference(fileNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(fileNode);
                }

                if (parentRef != null) {
                    Reference fileRef = AasUtils.toReference(parentRef, aasFile);

                    referableMap.put(fileRef, new ObjectData(aasFile, fileNode, submodel));
                }
            }
        }
        catch (Exception ex) {
            LOG.error("addAasFile Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds base data to the given submodel element.
     *
     * @param node The desired submodel element UA node
     * @param element The corresponding AAS submodel element
     * @throws StatusException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    private void addSubmodelElementBaseData(AASSubmodelElementType node, SubmodelElement element)
            throws StatusException {
        try {
            if ((node != null) && (element != null)) {
                // Category
                String category = element.getCategory();
                if (category == null) {
                    category = "";
                }
                node.setCategory(category);

                node.setModelingKind(ValueConverter.convertModelingKind(element.getKind()));

                // DataSpecifications
                addEmbeddedDataSpecifications(node, element.getEmbeddedDataSpecifications());

                // SemanticId
                if (element.getSemanticId() != null) {
                    addSemanticId(node, element.getSemanticId());
                }

                // Qualifiers
                List<Constraint> qualifiers = element.getQualifiers();
                if ((qualifiers != null) && (!qualifiers.isEmpty())) {
                    if (node.getQualifierNode() == null) {
                        addQualifierNode(node);
                    }

                    addQualifiers(node.getQualifierNode(), qualifiers);
                }

                // Description
                addDescriptions(node, element.getDescriptions());

                if (VALUES_READ_ONLY) {
                    node.getCategoryNode().setAccessLevel(AccessLevelType.CurrentRead);
                    node.getModelingKindNode().setAccessLevel(AccessLevelType.CurrentRead);
                }
            }
        }
        catch (Exception ex) {
            LOG.error("addSubmodelElementBaseData Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds a SemanticId to the given node.
     *
     * @param node The UA node in which the SemanticId should be created
     * @param semanticId The reference of the desired SemanticId
     */
    private void addSemanticId(UaNode node, Reference semanticId) {
        try {
            if (dictionaryMap.containsKey(semanticId)) {
                node.addReference(dictionaryMap.get(semanticId), Identifiers.HasDictionaryEntry, false);
            }
            // if entry not found: perhaps create a new one?
        }
        catch (Exception ex) {
            LOG.error("addSemanticId Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the references to the given Embedded Data Specifications.
     *
     * @param aasNode The desired object where the DataSpecifications should be added
     * @param list The list of the desired Data Specifications
     * @throws StatusException If the operation fails
     */
    private void addEmbeddedDataSpecifications(AASAssetAdministrationShellType aasNode, List<EmbeddedDataSpecification> list) throws StatusException {
        try {
            if ((list != null) && (!list.isEmpty())) {
                List<Reference> refList = new ArrayList<>();
                for (EmbeddedDataSpecification eds: list) {
                    refList.add(eds.getDataSpecification());
                }

                AASReferenceList listNode = aasNode.getDataSpecificationNode();

                if (listNode == null) {
                    addAasReferenceList(aasNode, refList, AASAssetAdministrationShellType.DATA_SPECIFICATION);
                }
                else {
                    addEmbeddedDataSpecificationsReferences(listNode, refList);
                }
            }
        }
        catch (Exception ex) {
            LOG.error(ADD_EMBED_DS_EXC, ex);
            throw ex;
        }
    }


    /**
     * Adds the references to the given Embedded Data Specifications.
     *
     * @param assetNode The desired node where the DataSpecifications should be added
     * @param list The list of the desired Data Specifications
     * @throws StatusException If the operation fails
     */
    private void addEmbeddedDataSpecifications(AASAssetType assetNode, List<EmbeddedDataSpecification> list) throws StatusException {
        try {
            if ((list != null) && (!list.isEmpty())) {
                List<Reference> refList = new ArrayList<>();
                for (EmbeddedDataSpecification eds: list) {
                    refList.add(eds.getDataSpecification());
                }

                AASReferenceList listNode = assetNode.getDataSpecificationNode();

                if (listNode == null) {
                    addAasReferenceList(assetNode, refList, AASAssetType.DATA_SPECIFICATION);
                }
                else {
                    addEmbeddedDataSpecificationsReferences(listNode, refList);
                }
            }
        }
        catch (Exception ex) {
            LOG.error(ADD_EMBED_DS_EXC, ex);
            throw ex;
        }
    }


    /**
     * Adds the references to the given Embedded Data Specifications.
     *
     * @param submodelNode The desired object where the DataSpecifications should be added
     * @param list The list of the desired Data Specifications
     * @throws StatusException If the operation fails
     */
    private void addEmbeddedDataSpecifications(AASSubmodelType submodelNode, List<EmbeddedDataSpecification> list) throws StatusException {
        try {
            if ((list != null) && (!list.isEmpty())) {
                List<Reference> refList = new ArrayList<>();
                for (EmbeddedDataSpecification eds: list) {
                    refList.add(eds.getDataSpecification());
                }

                AASReferenceList listNode = submodelNode.getDataSpecificationNode();

                if (listNode == null) {
                    addAasReferenceList(submodelNode, refList, AASSubmodelType.DATA_SPECIFICATION);
                }
                else {
                    addEmbeddedDataSpecificationsReferences(listNode, refList);
                }
            }
        }
        catch (Exception ex) {
            LOG.error(ADD_EMBED_DS_EXC, ex);
            throw ex;
        }
    }


    /**
     * Adds the references to the given Embedded Data Specifications.
     *
     * @param submodelElementNode The desired object where the
     *            DataSpecifications should be added
     * @param list The list of the desired Data Specifications
     * @throws StatusException If the operation fails
     */
    private void addEmbeddedDataSpecifications(AASSubmodelElementType submodelElementNode, List<EmbeddedDataSpecification> list) throws StatusException {
        try {
            if ((list != null) && (!list.isEmpty())) {
                List<Reference> refList = new ArrayList<>();
                for (EmbeddedDataSpecification eds: list) {
                    refList.add(eds.getDataSpecification());
                }

                AASReferenceList listNode = submodelElementNode.getDataSpecificationNode();

                if (listNode == null) {
                    addAasReferenceList(submodelElementNode, refList, AASSubmodelElementType.DATA_SPECIFICATION);
                }
                else {
                    addEmbeddedDataSpecificationsReferences(listNode, refList);
                }
            }
        }
        catch (Exception ex) {
            LOG.error(ADD_EMBED_DS_EXC, ex);
            throw ex;
        }
    }


    /**
     * Adds the references to the given Embedded Data Specification references.
     *
     * @param refListNode The desired object where the DataSpecifications should be added
     * @param refList The list of the desired Data Specification references
     * @throws StatusException If the operation fails
     */
    private void addEmbeddedDataSpecificationsReferences(AASReferenceList refListNode, List<Reference> refList) throws StatusException {
        try {
            if ((refListNode != null) && (!refList.isEmpty())) {
                int count = 0;
                for (Reference ref: refList) {
                    count++;
                    String name = AASAssetAdministrationShellType.DATA_SPECIFICATION;
                    if (count > 1) {
                        name += count;
                    }

                    addAasReferenceAasNS(refListNode, ref, name);
                }
            }
        }
        catch (Exception ex) {
            LOG.error(ADD_EMBED_DS_EXC, ex);
            throw ex;
        }
    }


    /**
     * Adds the list of Descriptions to the given node.
     *
     * @param node The desired UA node in which the Descriptions should be created
     * @param descriptions The list of AAS descriptions
     */
    private void addDescriptions(UaNode node, List<LangString> descriptions) {
        try {
            if ((node != null) && (descriptions != null) && (!descriptions.isEmpty())) {
                LangString desc = descriptions.get(0);
                node.setDescription(new LocalizedText(desc.getValue(), desc.getLanguage()));
            }
        }
        catch (Exception ex) {
            LOG.error("addDescriptions Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the descriptions to the given argument.
     *
     * @param arg The desired UA argument
     * @param descriptions The list of AAS descriptions
     */
    private void addDescriptions(Argument arg, List<LangString> descriptions) {
        try {
            if ((arg != null) && (descriptions != null) && (!descriptions.isEmpty())) {
                LangString desc = descriptions.get(0);
                arg.setDescription(new LocalizedText(desc.getValue(), desc.getLanguage()));
            }
        }
        catch (Exception ex) {
            LOG.error("addDescriptions Exception", ex);
        }
    }


    /**
     * Adds a QualifierNode to the given Node.
     *
     * @param node The desired base node
     */
    private void addQualifierNode(UaNode node) {
        try {
            String name = AASSubmodelElementType.QUALIFIER;
            LOG.info("addQualifierNode {}; to Node: {}", name, node);
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASQualifierList.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
            NodeId nid = createNodeId(node, browseName);
            AASQualifierList listNode = createInstance(AASQualifierList.class, nid, browseName, LocalizedText.english(name));

            node.addComponent(listNode);
        }
        catch (Exception ex) {
            LOG.error("addQualifierNode Exception", ex);
        }
    }


    /**
     * Adds a list of Qualifiers to the given Node.
     *
     * @param listNode The UA node in which the Qualifiers should be created
     * @param qualifiers The desired list of Qualifiers
     */
    private void addQualifiers(AASQualifierList listNode, List<Constraint> qualifiers) throws StatusException {
        if (listNode == null) {
            throw new IllegalArgumentException("listNode = null");
        }
        else if (qualifiers == null) {
            throw new IllegalArgumentException("qualifiers = null");
        }

        try {

            int index = 1;
            for (Constraint constraint: qualifiers) {
                if ((constraint != null) && (Qualifier.class.isAssignableFrom(constraint.getClass()))) {
                    addQualifier(listNode, (Qualifier) constraint, "Qualifier " + index);
                }

                index++;
            }
        }
        catch (Exception ex) {
            LOG.error("addQualifiers Exception", ex);
            throw ex;
        }
    }


    /**
     * Creates and adds a Qualifier to the given Node.
     *
     * @param node The UA node in which the Qualifier should be created
     * @param qualifier The desired Qualifier
     * @param name The name of the qualifier
     */
    private void addQualifier(UaNode node, Qualifier qualifier, String name) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException(NODE_NULL);
        }
        else if (qualifier == null) {
            throw new IllegalArgumentException("qualifier = null");
        }

        try {
            LOG.info("addQualifier {}; to Node: {}", name, node);
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASQualifierType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
            NodeId nid = createNodeId(node, browseName);
            AASQualifierType qualifierNode = createInstance(AASQualifierType.class, nid, browseName, LocalizedText.english(name));

            // Type
            qualifierNode.setType(qualifier.getType());

            // ValueType
            qualifierNode.setValueType(ValueConverter.stringToValueType(qualifier.getValueType()));

            // Value
            if (qualifier.getValue() != null) {
                if (qualifierNode.getValueNode() == null) {
                    addQualifierValueNode(qualifierNode);
                }

                qualifierNode.setValue(qualifier.getValue());
            }

            // ValueId
            if (qualifier.getValueId() != null) {
                addAasReferenceAasNS(qualifierNode, qualifier.getValueId(), AASQualifierType.VALUE_ID);
            }

            if (VALUES_READ_ONLY) {
                if (qualifierNode.getValueNode() != null) {
                    qualifierNode.getValueNode().setAccessLevel(AccessLevelType.CurrentRead);
                }
                if (qualifierNode.getValueTypeNode() != null) {
                    qualifierNode.getValueTypeNode().setAccessLevel(AccessLevelType.CurrentRead);
                }
                if (qualifierNode.getTypeNode() != null) {
                    qualifierNode.getTypeNode().setAccessLevel(AccessLevelType.CurrentRead);
                }
            }

            node.addComponent(qualifierNode);
        }
        catch (Exception ex) {
            LOG.error("addQualifier Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds a list of IdentifierKeyValuePairs to the given Node.
     *
     * @param assetInfoNode The AssetInformation node in which the
     *            IdentifierKeyValuePairs should be created or added
     * @param list The desired list of IdentifierKeyValuePairs
     * @param name The desired name of the Node
     * @throws StatusException If the operation fails
     */
    private void addSpecificAssetIds(AASAssetInformationType assetInfoNode, List<IdentifierKeyValuePair> list, String name) throws StatusException {
        if (assetInfoNode == null) {
            throw new IllegalArgumentException("assetInfoNode = null");
        }
        else if (list == null) {
            throw new IllegalArgumentException("list = null");
        }

        try {
            LOG.info("addSpecificAssetIds {}; to Node: {}", name, assetInfoNode);
            AASIdentifierKeyValuePairList listNode = assetInfoNode.getSpecificAssetIdNode();
            boolean created = false;

            if (listNode == null) {
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASIdentifierKeyValuePairList.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(assetInfoNode, browseName);
                listNode = createInstance(AASIdentifierKeyValuePairList.class, nid, browseName, LocalizedText.english(name));
                created = true;
            }

            for (IdentifierKeyValuePair ikv: list) {
                if (ikv != null) {
                    addIdentifierKeyValuePair(listNode, ikv, ikv.getKey());
                }
            }

            if (created) {
                assetInfoNode.addComponent(listNode);
            }
        }
        catch (Exception ex) {
            LOG.error("addSpecificAssetIds Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an IdentifierKeyValuePair to the given Node.
     *
     * @param node The UA node in which the IdentifierKeyValuePair should be created
     * @param identifierPair The desired IdentifierKeyValuePair
     * @param name The desired name of the IdentifierKeyValuePair node
     * @throws StatusException If the operation fails
     */
    private void addIdentifierKeyValuePair(UaNode node, IdentifierKeyValuePair identifierPair, String name) throws StatusException {
        addIdentifierKeyValuePair(node, identifierPair, name, VALUES_READ_ONLY);
    }


    /**
     * Adds an IdentifierKeyValuePair to the given Node.
     *
     * @param node The UA node in which the IdentifierKeyValuePair should be created
     * @param identifierPair The desired IdentifierKeyValuePair
     * @param name The desired name of the IdentifierKeyValuePair node
     * @param readOnly True if the value should be read-only
     * @throws StatusException If the operation fails
     */
    private void addIdentifierKeyValuePair(UaNode node, IdentifierKeyValuePair identifierPair, String name, boolean readOnly) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException(NODE_NULL);
        }
        else if (identifierPair == null) {
            throw new IllegalArgumentException("identifierPair = null");
        }

        try {
            LOG.info("addIdentifierKeyValuePair {}; to Node: {}", name, node);
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASIdentifierKeyValuePairType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
            NodeId nid = createNodeId(node, browseName);
            AASIdentifierKeyValuePairType identifierPairNode = createInstance(AASIdentifierKeyValuePairType.class, nid, browseName, LocalizedText.english(name));

            setIdentifierKeyValuePairData(identifierPairNode, identifierPair, readOnly);

            node.addComponent(identifierPairNode);
        }
        catch (Exception ex) {
            LOG.error("addIdentifierKeyValuePair Exception", ex);
            throw ex;
        }
    }


    /**
     * Sets the data for the given IdentifierKeyValuePair Node from the corresponding AAS object.
     * 
     * @param identifierPairNode The desired IdentifierKeyValuePair Node
     * @param aasIdentifierPair The corresponding AAS IdentifierKeyValuePair
     * @throws StatusException If the operation fails
     */
    private void setIdentifierKeyValuePairData(AASIdentifierKeyValuePairType identifierPairNode, IdentifierKeyValuePair aasIdentifierPair) throws StatusException {
        setIdentifierKeyValuePairData(identifierPairNode, aasIdentifierPair, VALUES_READ_ONLY);
    }


    /**
     * Sets the data for the given IdentifierKeyValuePair Node from the corresponding AAS object.
     * 
     * @param identifierPairNode The desired IdentifierKeyValuePair Node
     * @param aasIdentifierPair The corresponding AAS IdentifierKeyValuePair
     * @param readOnly True if the value should be read-only
     * @throws StatusException If the operation fails
     */
    private void setIdentifierKeyValuePairData(AASIdentifierKeyValuePairType identifierPairNode, IdentifierKeyValuePair aasIdentifierPair, boolean readOnly)
            throws StatusException {
        try {
            // ExternalSubjectId
            Reference externalSubjectId = aasIdentifierPair.getExternalSubjectId();
            if (externalSubjectId != null) {
                AASReferenceType extSubjectNode = identifierPairNode.getExternalSubjectIdNode();
                if (extSubjectNode == null) {
                    addAasReferenceAasNS(identifierPairNode, externalSubjectId, AASIdentifierKeyValuePairType.EXTERNAL_SUBJECT_ID);
                }
                else {
                    AasSubmodelElementHelper.setAasReferenceData(externalSubjectId, extSubjectNode);
                }
            }

            // Key
            identifierPairNode.setKey(aasIdentifierPair.getKey());

            // Value
            identifierPairNode.setValue(aasIdentifierPair.getValue());

            if (readOnly) {
                identifierPairNode.getKeyNode().setAccessLevel(AccessLevelType.CurrentRead);
                identifierPairNode.getValueNode().setAccessLevel(AccessLevelType.CurrentRead);
            }
        }
        catch (Exception ex) {
            LOG.error("setIdentifierKeyValuePairData Exception", ex);
            throw ex;
        }
    }


    /**
     * Creates a node with the given name and adds the given list of references.
     *
     * @param node The UA node in which the list of references should be created
     * @param list The desired list of references
     * @param name The desired name of the Node
     * @throws StatusException If the operation fails
     */
    private void addAasReferenceList(UaNode node, List<Reference> list, String name) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException(NODE_NULL);
        }
        else if (list == null) {
            throw new IllegalArgumentException("list = null");
        }

        try {
            LOG.info("addAasReferenceList {}; to Node: {}", name, node);
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASReferenceList.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
            NodeId nid = getDefaultNodeId();
            AASReferenceList referenceListNode = createInstance(AASReferenceList.class, nid, browseName, LocalizedText.english(name));

            int counter = 1;
            for (Reference ref: list) {
                addAasReferenceAasNS(referenceListNode, ref, name + counter++);
            }

            node.addComponent(referenceListNode);
        }
        catch (Exception ex) {
            LOG.error("addAasReferenceList Exception", ex);
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
                LOG.info("addAasEnvironmentNode {}; to ObjectsFolder", name);
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASEnvironmentType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(objectsFolder, browseName);
                FolderType ft = createInstance(AASEnvironmentType.class, nid, browseName, LocalizedText.english(name));
                LOG.info("addAasEnvironmentNode: Created class: {}", ft.getClass().getName());
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
            LOG.info("addAsset {}; to Node: {}", name, node);
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASAssetType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
            NodeId nid = createNodeId(node, browseName);
            AASAssetType assetNode = createInstance(AASAssetType.class, nid, browseName, LocalizedText.english(displayName));

            addIdentifiable(assetNode, asset.getIdentification(), asset.getAdministration(), asset.getCategory());

            // DataSpecifications
            addEmbeddedDataSpecifications(assetNode, asset.getEmbeddedDataSpecifications());

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
                addIdentifiable(smNode, submodel.getIdentification(), submodel.getAdministration(), submodel.getCategory());

                // DataSpecifications
                addEmbeddedDataSpecifications(smNode, submodel.getEmbeddedDataSpecifications());

                // Qualifiers
                List<Constraint> qualifiers = submodel.getQualifiers();
                if ((qualifiers != null) && (!qualifiers.isEmpty())) {
                    if (smNode.getQualifierNode() == null) {
                        addQualifierNode(smNode);
                    }

                    addQualifiers(smNode.getQualifierNode(), qualifiers);
                }

                // SemanticId
                if (submodel.getSemanticId() != null) {
                    addSemanticId(smNode, submodel.getSemanticId());
                }

                // Description
                addDescriptions(smNode, submodel.getDescriptions());

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
                        addAasDataElement(node, (DataElement) elem, submodel, parentRef, ordered);
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
     * Adds an AAS data element the given node.
     *
     * @param node The desired node
     * @param aasDataElement The corresponding AAS data element to add
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param parentRef The AAS reference to the parent object
     * @param ordered Specifies whether the element should be added ordered
     *            (true) or unordered (false)
     * @throws StatusException If the operation fails
     */
    private void addAasDataElement(UaNode node, DataElement aasDataElement, Submodel submodel, Reference parentRef, boolean ordered)
            throws StatusException {
        try {
            if ((node != null) && (aasDataElement != null)) {
                if (aasDataElement instanceof Property) {
                    addAasProperty(node, (Property) aasDataElement, submodel, parentRef, ordered);
                }
                else if (aasDataElement instanceof File) {
                    addAasFile(node, (File) aasDataElement, submodel, parentRef, ordered, null);
                }
                else if (aasDataElement instanceof Blob) {
                    addAasBlob(node, (Blob) aasDataElement, submodel, parentRef, ordered);
                }
                else if (aasDataElement instanceof ReferenceElement) {
                    addAasReferenceElement(node, (ReferenceElement) aasDataElement, submodel, parentRef, ordered);
                }
                else if (aasDataElement instanceof Range) {
                    addAasRange(node, (Range) aasDataElement, submodel, parentRef, ordered);
                }
                else if (aasDataElement instanceof MultiLanguageProperty) {
                    addAasMultiLanguageProperty(node, (MultiLanguageProperty) aasDataElement, submodel, parentRef, ordered);
                }
                else {
                    LOG.warn("addAasDataElement: unknown DataElement: {}; Class {}", aasDataElement.getIdShort(), aasDataElement.getClass());
                }
            }
        }
        catch (Exception ex) {
            LOG.error("addAasDataElement Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS property the given node.
     *
     * @param node The desired node
     * @param aasProperty The corresponding AAS property to add
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param parentRef The AAS reference to the parent node
     * @param ordered Specifies whether the property should be added ordered
     *            (true) or unordered (false)
     */
    private void addAasProperty(UaNode node, Property aasProperty, Submodel submodel, Reference parentRef, boolean ordered) {
        try {
            String name = aasProperty.getIdShort();
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASPropertyType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
            NodeId nid = getDefaultNodeId();

            AASPropertyType prop = createInstance(AASPropertyType.class, nid, browseName, LocalizedText.english(name));
            addSubmodelElementBaseData(prop, aasProperty);

            Reference propRef = AasUtils.toReference(parentRef, aasProperty);

            // ValueId
            Reference ref = aasProperty.getValueId();
            if (ref != null) {
                addAasReferenceAasNS(prop, ref, AASPropertyType.VALUE_ID);
            }

            // here Value and ValueType are set
            addOpcUaProperty(aasProperty, submodel, prop, propRef);

            if (submodel != null) {
                submodelElementOpcUAMap.put(propRef, prop);
            }

            if (VALUES_READ_ONLY) {
                // ValueType read-only
                prop.getValueTypeNode().setAccessLevel(AccessLevelType.CurrentRead);

                // if the Submodel is null, we also make the value read-only
                if ((submodel == null) && (prop.getValueNode() != null)) {
                    prop.getValueNode().setAccessLevel(AccessLevelType.CurrentRead);
                }
            }

            LOG.info("addAasProperty: add Property {}", nid);

            if (ordered) {
                node.addReference(prop, Identifiers.HasOrderedComponent, false);
            }
            else {
                node.addComponent(prop);
            }

            referableMap.put(propRef, new ObjectData(aasProperty, prop, submodel));
        }
        catch (Exception ex) {
            LOG.error("addAasProperty Exception", ex);
        }
    }


    /**
     * Adds the OPC UA property itself to the given Property object and sets the value.
     *
     * @param aasProperty The AAS property
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param prop The UA Property object
     * @param propRef The AAS reference to the property
     */
    private void addOpcUaProperty(Property aasProperty, Submodel submodel, AASPropertyType prop, Reference propRef) {
        try {
            NodeId myPropertyId = new NodeId(getNamespaceIndex(), prop.getNodeId().getValue().toString() + "." + AASPropertyType.VALUE);
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASPropertyType.getNamespaceUri(), AASPropertyType.VALUE).toQualifiedName(getNamespaceTable());
            LocalizedText displayName = LocalizedText.english(AASPropertyType.VALUE);

            submodelElementAasMap.put(myPropertyId, new SubmodelElementData(aasProperty, submodel, SubmodelElementData.Type.PROPERTY_VALUE, propRef));
            LOG.debug("setPropertyValueAndType: NodeId {}; Property: {}", myPropertyId, aasProperty);

            AasSubmodelElementHelper.setPropertyValueAndType(aasProperty, prop, new ValueData(myPropertyId, browseName, displayName));
        }
        catch (Exception ex) {
            LOG.error("addOpcUaProperty Exception", ex);
        }
    }


    /**
     * Adds an AAS Blob to the given UA node.
     *
     * @param node The desired UA node
     * @param aasBlob The AAS blob to add
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param parentRef Tne reference to the parent object
     * @param ordered Specifies whether the blob should be added ordered (true)
     *            or unordered (false)
     * @throws StatusException If the operation fails
     */
    private void addAasBlob(UaNode node, Blob aasBlob, Submodel submodel, Reference parentRef, boolean ordered)
            throws StatusException {
        try {
            if ((node != null) && (aasBlob != null)) {
                String name = aasBlob.getIdShort();
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASBlobType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = getDefaultNodeId();
                AASBlobType blobNode = createInstance(AASBlobType.class, nid, browseName, LocalizedText.english(name));
                addSubmodelElementBaseData(blobNode, aasBlob);

                // MimeType
                blobNode.setMimeType(aasBlob.getMimeType());

                Reference blobRef = AasUtils.toReference(parentRef, aasBlob);

                // Value
                if (aasBlob.getValue() != null) {
                    if (blobNode.getValueNode() == null) {
                        AasSubmodelElementHelper.addBlobValueNode(blobNode);
                    }

                    submodelElementAasMap.put(blobNode.getValueNode().getNodeId(), new SubmodelElementData(aasBlob, submodel, SubmodelElementData.Type.BLOB_VALUE, blobRef));
                    LOG.debug("addAasBlob: NodeId {}; Blob: {}", blobNode.getValueNode().getNodeId(), aasBlob);

                    submodelElementOpcUAMap.put(blobRef, blobNode);

                    blobNode.setValue(ByteString.valueOf(aasBlob.getValue()));
                }

                if (VALUES_READ_ONLY) {
                    blobNode.getMimeTypeNode().setAccessLevel(AccessLevelType.CurrentRead);
                }

                if (ordered) {
                    node.addReference(blobNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(blobNode);
                }

                referableMap.put(blobRef, new ObjectData(aasBlob, blobNode, submodel));
            }
        }
        catch (Exception ex) {
            LOG.error("addAasBlob Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS reference element to the given node.
     *
     * @param node The desired UA node
     * @param aasRefElem The AAS reference element to add
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param parentRef The reference to the parent object
     * @param ordered Specifies whether the reference element should be added
     *            ordered (true) or unordered (false)
     * @throws StatusException If the operation fails
     */
    private void addAasReferenceElement(UaNode node, ReferenceElement aasRefElem, Submodel submodel, Reference parentRef, boolean ordered)
            throws StatusException {
        try {
            if ((node != null) && (aasRefElem != null)) {
                String name = aasRefElem.getIdShort();
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASReferenceElementType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = getDefaultNodeId();
                AASReferenceElementType refElemNode = createInstance(AASReferenceElementType.class, nid, browseName, LocalizedText.english(name));
                addSubmodelElementBaseData(refElemNode, aasRefElem);

                if (aasRefElem.getValue() != null) {
                    AasSubmodelElementHelper.setAasReferenceData(aasRefElem.getValue(), refElemNode.getValueNode(), false);
                }

                Reference refElemRef = AasUtils.toReference(parentRef, aasRefElem);

                submodelElementAasMap.put(refElemNode.getValueNode().getKeysNode().getNodeId(),
                        new SubmodelElementData(aasRefElem, submodel, SubmodelElementData.Type.REFERENCE_ELEMENT_VALUE, refElemRef));

                submodelElementOpcUAMap.put(refElemRef, refElemNode);

                if (ordered) {
                    node.addReference(refElemNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(refElemNode);
                }

                referableMap.put(refElemRef, new ObjectData(aasRefElem, refElemNode, submodel));
            }
        }
        catch (Exception ex) {
            LOG.error("addAasReferenceElement Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS range object to the given node.
     *
     * @param node The desired UA node
     * @param aasRange The corresponding AAS range object to add
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param parentRef The reference to the parent object
     * @param ordered Specifies whether the range should be added ordered (true)
     *            or unordered (false)
     * @throws StatusException If the operation fails
     */
    private void addAasRange(UaNode node, Range aasRange, Submodel submodel, Reference parentRef, boolean ordered)
            throws StatusException {
        try {
            if ((node != null) && (aasRange != null)) {
                String name = aasRange.getIdShort();
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASRangeType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = getDefaultNodeId();
                AASRangeType rangeNode = createInstance(AASRangeType.class, nid, browseName, LocalizedText.english(name));
                addSubmodelElementBaseData(rangeNode, aasRange);

                Reference rangeRef = AasUtils.toReference(parentRef, aasRange);
                addOpcUaRange(aasRange, rangeNode, submodel, rangeRef);

                if (VALUES_READ_ONLY) {
                    // ValueType read-only
                    rangeNode.getValueTypeNode().setAccessLevel(AccessLevelType.CurrentRead);
                }

                if (ordered) {
                    node.addReference(rangeNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(rangeNode);
                }

                referableMap.put(rangeRef, new ObjectData(aasRange, rangeNode, submodel));
            }
        }
        catch (Exception ex) {
            LOG.error("addAasRange Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the min and max properties to the UA range object and sets the values
     *
     * @param aasRange The AAS range object
     * @param range The corresponding UA range object
     * @param submodel The corresponding submodel
     * @param rangeRef The AAS reference to the Range
     */
    private void addOpcUaRange(Range aasRange, AASRangeType range, Submodel submodel, Reference rangeRef) {
        try {
            String minValue = aasRange.getMin();
            String maxValue = aasRange.getMax();
            NodeId myPropertyIdMin = new NodeId(getNamespaceIndex(), range.getNodeId().getValue().toString() + "." + AASRangeType.MIN);
            NodeId myPropertyIdMax = new NodeId(getNamespaceIndex(), range.getNodeId().getValue().toString() + "." + AASRangeType.MAX);
            String valueType = aasRange.getValueType();
            QualifiedName browseNameMin = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASRangeType.getNamespaceUri(), AASRangeType.MIN).toQualifiedName(getNamespaceTable());
            LocalizedText displayNameMin = LocalizedText.english(AASRangeType.MIN);
            QualifiedName browseNameMax = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASRangeType.getNamespaceUri(), AASRangeType.MAX).toQualifiedName(getNamespaceTable());
            LocalizedText displayNameMax = LocalizedText.english(AASRangeType.MAX);

            submodelElementAasMap.put(myPropertyIdMin, new SubmodelElementData(aasRange, submodel, SubmodelElementData.Type.RANGE_MIN, rangeRef));
            submodelElementAasMap.put(myPropertyIdMax, new SubmodelElementData(aasRange, submodel, SubmodelElementData.Type.RANGE_MAX, rangeRef));

            submodelElementOpcUAMap.put(rangeRef, range);

            AasSubmodelElementHelper.setRangeValueAndType(valueType, minValue, maxValue, range, new ValueData(myPropertyIdMin, browseNameMin, displayNameMin),
                    new ValueData(myPropertyIdMax, browseNameMax, displayNameMax));
        }
        catch (Exception ex) {
            LOG.error("setRangeValueAndType Exception", ex);
        }
    }


    /**
     * Adds an AAS Multi Language Property to the given node.
     *
     * @param node The desired UA node
     * @param aasMultiLang The AAS Multi Language Property to add
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param parentRef The AAS reference to the parent object
     * @param ordered Specifies whether the multi language property should be
     *            added ordered (true) or unordered (false)
     * @throws StatusException If the operation fails
     */
    private void addAasMultiLanguageProperty(UaNode node, MultiLanguageProperty aasMultiLang, Submodel submodel, Reference parentRef, boolean ordered)
            throws StatusException {
        try {
            if ((node != null) && (aasMultiLang != null)) {
                String name = aasMultiLang.getIdShort();
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASMultiLanguagePropertyType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = getDefaultNodeId();
                AASMultiLanguagePropertyType multiLangNode = createInstance(AASMultiLanguagePropertyType.class, nid, browseName, LocalizedText.english(name));
                addSubmodelElementBaseData(multiLangNode, aasMultiLang);

                List<LangString> values = aasMultiLang.getValues();
                if (values != null) {
                    if (multiLangNode.getValueNode() == null) {
                        AasSubmodelElementHelper.addMultiLanguageValueNode(multiLangNode, values.size());
                    }

                    multiLangNode.getValueNode().setValue(ValueConverter.getLocalizedTextFromLangStringSet(values));
                }

                if (aasMultiLang.getValueId() != null) {
                    addAasReferenceAasNS(multiLangNode, aasMultiLang.getValueId(), AASMultiLanguagePropertyType.VALUE_ID);
                }

                Reference multiLangRef = AasUtils.toReference(parentRef, aasMultiLang);
                submodelElementAasMap.put(multiLangNode.getValueNode().getNodeId(),
                        new SubmodelElementData(aasMultiLang, submodel, SubmodelElementData.Type.MULTI_LANGUAGE_VALUE, multiLangRef));

                submodelElementOpcUAMap.put(multiLangRef, multiLangNode);

                if (ordered) {
                    node.addReference(multiLangNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(multiLangNode);
                }

                referableMap.put(multiLangRef, new ObjectData(aasMultiLang, multiLangNode, submodel));
            }
        }
        catch (Exception ex) {
            LOG.error("addAasMultiLanguageProperty Exception", ex);
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
                addSubmodelElementBaseData(capabilityNode, aasCapability);

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
                addSubmodelElementBaseData(entityNode, aasEntity);

                Reference entityRef = AasUtils.toReference(parentRef, aasEntity);

                // EntityType
                entityNode.setEntityType(ValueConverter.getAasEntityType(aasEntity.getEntityType()));

                submodelElementAasMap.put(entityNode.getEntityTypeNode().getNodeId(),
                        new SubmodelElementData(aasEntity, submodel, SubmodelElementData.Type.ENTITY_TYPE, entityRef));

                // GlobalAssetId
                if (aasEntity.getGlobalAssetId() != null) {
                    if (entityNode.getGlobalAssetIdNode() == null) {
                        addAasReferenceAasNS(entityNode, aasEntity.getGlobalAssetId(), AASEntityType.GLOBAL_ASSET_ID, false);
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
                        addIdentifierKeyValuePair(entityNode, specificAssetId, AASEntityType.SPECIFIC_ASSET_ID);
                    }
                    else {
                        setIdentifierKeyValuePairData(entityNode.getSpecificAssetIdNode(), specificAssetId);
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
            addSubmodelElementBaseData(oper, aasOperation);

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

            Argument[] outputs = new Argument[1];
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
                addDescriptions(arg, prop.getDescriptions());

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
                addSubmodelElementBaseData(eventNode, aasEvent);

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
                    addSubmodelElementBaseData(relElemNode, aasRelElem);

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
                addAasDataElement(relElemNode.getAnnotationNode(), de, submodel, relElemRef, false);
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

                addSubmodelElementBaseData(collNode, aasColl);

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
     * Adds the given submodel references to the given node.
     *
     * @param node The desired UA node in which the objects should be created
     * @param submodelRefs The desired submodel references
     * @throws StatusException If the operation fails
     */
    private void addSubmodelReferences(AASAssetAdministrationShellType node, List<Reference> submodelRefs) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException(NODE_NULL);
        }
        else if (submodelRefs == null) {
            throw new IllegalArgumentException("sumodelRefs = null");
        }

        try {
            String name = "Submodel";
            AASReferenceList referenceListNode = node.getSubmodelNode();
            LOG.info("addSubmodelReferences: add {} Submodels to Node: {}", submodelRefs.size(), node);
            boolean added = false;
            if (referenceListNode == null) {
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASReferenceList.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(node, browseName);
                referenceListNode = createInstance(AASReferenceList.class, nid, browseName, LocalizedText.english(name));
                LOG.info("addSubmodelReferences: add Node {} to Node {}", referenceListNode.getNodeId(), node.getNodeId());
                added = true;
            }

            int counter = 1;
            for (Reference ref: submodelRefs) {
                UaNode submodelNode = null;
                String submodelName = getSubmodelName(ref);
                if (submodelName.isEmpty()) {
                    submodelName = name + counter++;
                }

                if (submodelOpcUAMap.containsKey(ref)) {
                    submodelNode = submodelOpcUAMap.get(ref);
                }

                UaNode refNode = addAasReferenceAasNS(referenceListNode, ref, submodelName);

                if (refNode != null) {
                    // add hasAddIn reference to the submodel
                    if (submodelNode != null) {
                        refNode.addReference(submodelNode, Identifiers.HasAddIn, false);
                    }
                    else {
                        LOG.warn("addSubmodelReferences: Submodel {} not found in submodelRefMap", ref);
                    }
                }
            }

            if (added) {
                node.addComponent(referenceListNode);
            }
        }
        catch (Exception ex) {
            LOG.error("addSubmodelReferences Exception", ex);
            throw ex;
        }
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
            LOG.debug("elementCreated called. Reference {}", AasUtils.asString(element));

            // The element is the parent object where the value is added
            ObjectData parent = null;
            if (referableMap.containsKey(element)) {
                parent = referableMap.get(element);
            }
            else {
                LOG.info("elementCreated: element not found in referableMap: {}", AasUtils.asString(element));
            }

            if (value instanceof ConceptDescription) {
                addConceptDescriptions(List.of((ConceptDescription) value));
            }
            else if (value instanceof Asset) {
                addAsset(aasEnvironmentNode, (Asset) value);
            }
            else if (value instanceof Submodel) {
                addSubmodel(aasEnvironmentNode, (Submodel) value);
            }
            else if (value instanceof AssetAdministrationShell) {
                addAssetAdministrationShell((AssetAdministrationShell) value);
            }
            else if (parent != null) {
                if (value instanceof EmbeddedDataSpecification) {
                    if (parent.getNode() instanceof AASAssetAdministrationShellType) {
                        addEmbeddedDataSpecifications((AASAssetAdministrationShellType) parent.getNode(), List.of((EmbeddedDataSpecification) value));
                    }
                    else if (parent.getNode() instanceof AASSubmodelType) {
                        addEmbeddedDataSpecifications((AASSubmodelType) parent.getNode(), List.of((EmbeddedDataSpecification) value));
                    }
                    else if (parent.getNode() instanceof AASSubmodelElementType) {
                        addEmbeddedDataSpecifications((AASSubmodelElementType) parent.getNode(), List.of((EmbeddedDataSpecification) value));
                    }
                    else if (parent.getNode() instanceof AASAssetType) {
                        addEmbeddedDataSpecifications((AASAssetType) parent.getNode(), List.of((EmbeddedDataSpecification) value));
                    }
                    else {
                        LOG.warn("elementCreated: EmbeddedDataSpecification parent class not found");
                    }
                }
                else if (value instanceof Constraint) {
                    if (parent.getNode() instanceof AASSubmodelType) {
                        addQualifiers(((AASSubmodelType) parent.getNode()).getQualifierNode(), List.of((Constraint) value));
                    }
                    else if (parent.getNode() instanceof AASSubmodelElementType) {
                        addQualifiers(((AASSubmodelElementType) parent.getNode()).getQualifierNode(), List.of((Constraint) value));
                    }
                    else {
                        LOG.warn("elementCreated: Constraint parent class not found");
                    }
                }
                else if (value instanceof SubmodelElement) {
                    if (parent.getNode() instanceof AASSubmodelType) {
                        LOG.info("elementCreated: call addSubmodelElements");
                        addSubmodelElements(parent.getNode(), List.of((SubmodelElement) value), (Submodel) parent.getReferable(), element);
                    }
                    else if (parent.getNode() instanceof AASSubmodelElementType) {
                        LOG.info("elementCreated: call addSubmodelElements");
                        addSubmodelElements(parent.getNode(), List.of((SubmodelElement) value), parent.getSubmodel(), element);
                    }
                    else {
                        LOG.warn("elementCreated: SubmodelElement parent class not found: {}; {}", parent.getNode().getNodeId(), parent.getNode());
                    }
                }
            }
            else {
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
            LOG.debug("elementDeleted called. Reference {}", AasUtils.asString(element));

            // The element is the object that should be deleted
            ObjectData data = null;
            if (referableMap.containsKey(element)) {
                data = referableMap.get(element);

                // remove element from the map
                referableMap.remove(element);
            }
            else {
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
            LOG.debug("elementUpdated called. Reference {}", AasUtils.asString(element));

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
                LOG.info("unsubscribe from the MessageBus");
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
        else {
            LOG.warn("SubmodelElement {} not found in submodelElementOpcUAMap", AasUtils.asString(reference));
        }
    }


    /**
     * Extracts the name from the given Submodel Reference.
     *
     * @param submodelRef The submodel reference
     * @return The Name of the Submodel
     */
    private static String getSubmodelName(Reference submodelRef) {
        String retval = "";
        if ((submodelRef != null) && (!submodelRef.getKeys().isEmpty())) {
            retval = submodelRef.getKeys().get(0).getValue();
        }

        return retval;
    }


    /**
     * Adds a Value Property to the given Qualifier Node.
     *
     * @param node The desired Blob Node
     */
    private void addQualifierValueNode(UaNode node) {
        try {
            NodeId myPropertyId = new NodeId(getNamespaceIndex(), node.getNodeId().getValue().toString() + "." + AASQualifierType.VALUE);
            PlainProperty<ByteString> myProperty = new PlainProperty<>(this, myPropertyId,
                    UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASQualifierType.getNamespaceUri(), AASQualifierType.VALUE).toQualifiedName(getNamespaceTable()),
                    LocalizedText.english(AASQualifierType.VALUE));
            myProperty.setDataTypeId(Identifiers.String);
            if (VALUES_READ_ONLY) {
                myProperty.setAccessLevel(AccessLevelType.CurrentRead);
            }
            myProperty.setDescription(new LocalizedText("", ""));
            node.addProperty(myProperty);
        }
        catch (Exception ex) {
            LOG.error("addQualifierValueNode Exception", ex);
            throw ex;
        }
    }


    /**
     * Gets the next availabe default NodeId.
     * 
     * @return The desired NodeId
     */
    private NodeId getDefaultNodeId() {
        int nr = ++nodeIdCounter;
        return new NodeId(getNamespaceIndex(), nr);
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
            LOG.debug("doRemoveFromMaps: remove SubmodelElement {}", AasUtils.asString(reference));

            if (submodelElementOpcUAMap.containsKey(reference)) {
                submodelElementOpcUAMap.remove(reference);
                LOG.debug("doRemoveFromMaps: remove SubmodelElement from submodelElementOpcUAMap: {}", AasUtils.asString(reference));
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
            else {
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
            LOG.debug("doRemoveFromMaps: remove submodel {}", AasUtils.asString(reference));

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
