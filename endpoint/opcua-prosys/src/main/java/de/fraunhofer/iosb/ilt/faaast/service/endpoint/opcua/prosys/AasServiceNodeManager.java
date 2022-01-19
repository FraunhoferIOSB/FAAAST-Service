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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.prosys;

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
import com.prosysopc.ua.stack.builtintypes.DateTime;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.Argument;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.types.opcua.DictionaryEntryType;
import com.prosysopc.ua.types.opcua.server.FileTypeNode;
import com.prosysopc.ua.types.opcua.server.FolderTypeNode;
import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBus;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionId;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.SubscriptionInfo;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementCreateEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ElementDeleteEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.event.change.ValueChangeEventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.AnnotatedRelationshipElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.BlobValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.DataElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.EntityValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.FileValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.MultiLanguagePropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.RangeValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.ReferenceElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.v3.valuedata.RelationshipElementValue;
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
import io.adminshell.aas.v3.model.EntityType;
import io.adminshell.aas.v3.model.Event;
import io.adminshell.aas.v3.model.File;
import io.adminshell.aas.v3.model.Identifier;
import io.adminshell.aas.v3.model.IdentifierKeyValuePair;
import io.adminshell.aas.v3.model.IdentifierType;
import io.adminshell.aas.v3.model.KeyType;
import io.adminshell.aas.v3.model.LangString;
import io.adminshell.aas.v3.model.ModelingKind;
import io.adminshell.aas.v3.model.MultiLanguageProperty;
import io.adminshell.aas.v3.model.Operation;
import io.adminshell.aas.v3.model.OperationVariable;
import io.adminshell.aas.v3.model.Property;
import io.adminshell.aas.v3.model.Qualifier;
import io.adminshell.aas.v3.model.Range;
import io.adminshell.aas.v3.model.Reference;
import io.adminshell.aas.v3.model.ReferenceElement;
import io.adminshell.aas.v3.model.RelationshipElement;
import io.adminshell.aas.v3.model.Submodel;
import io.adminshell.aas.v3.model.SubmodelElement;
import io.adminshell.aas.v3.model.SubmodelElementCollection;
import io.adminshell.aas.v3.model.impl.DefaultReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import opc.i4aas.AASAdministrativeInformationType;
import opc.i4aas.AASAnnotatedRelationshipElementType;
import opc.i4aas.AASAssetAdministrationShellType;
import opc.i4aas.AASAssetInformationType;
import opc.i4aas.AASAssetKindDataType;
import opc.i4aas.AASAssetType;
import opc.i4aas.AASBlobType;
import opc.i4aas.AASCapabilityType;
import opc.i4aas.AASCustomConceptDescriptionType;
import opc.i4aas.AASEntityType;
import opc.i4aas.AASEntityTypeDataType;
import opc.i4aas.AASEnvironmentType;
import opc.i4aas.AASEventType;
import opc.i4aas.AASFileType;
import opc.i4aas.AASIdentifiableType;
import opc.i4aas.AASIdentifierKeyValuePairList;
import opc.i4aas.AASIdentifierKeyValuePairType;
import opc.i4aas.AASIdentifierTypeDataType;
import opc.i4aas.AASIrdiConceptDescriptionType;
import opc.i4aas.AASIriConceptDescriptionType;
import opc.i4aas.AASKeyDataType;
import opc.i4aas.AASModelingKindDataType;
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
import opc.i4aas.AASSubmodelElementList;
import opc.i4aas.AASSubmodelElementType;
import opc.i4aas.AASSubmodelType;
import opc.i4aas.AASValueTypeDataType;
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
    private static final Logger logger = LoggerFactory.getLogger(AasServiceNodeManager.class);

    /**
     * Maps String to KeyType values
     */
    private static final Map<String, KeyType> keyTypeStringMap = new HashMap<>();

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
     * Maps AAS Submodel Element references to the corresponding submodel
     * reference
     */
    private final Map<Reference, Reference> submodelElementRefMap;

    /**
     * Lock for the submodelElementRefMap
     */
    private final ReentrantLock submodelElementRefMapLock;

    /**
     * Maps NodeIds to AAS Data (e.g. Properties and operations)
     */
    private final Map<NodeId, SubmodelElementData> submodelElementAasMap;

    /**
     * Lock for the submodelElementAasMap
     */
    private final ReentrantLock submodelElementAasMapLock;

    /**
     * Maps AAS SubmodelElements to OPC UA SubmodelElements
     */
    private final Map<Reference, AASSubmodelElementType> submodelElementOpcUAMap;

    /**
     * Lock for the submodelElementOpcUAMap
     */
    private final ReentrantLock submodelElementOpcUAMapLock;

    /**
     * Maps Submodel references to the OPC UA Submodel
     */
    private final Map<Reference, UaNode> submodelOpcUAMap;

    /**
     * Lock for the submodelOpcUAMap
     */
    private final ReentrantLock submodelOpcUAMapLock;

    //    /**
    //     * Maps NodeIds to the corresponding Submodels
    //     */
    //    private final Map<NodeId, Submodel> submodelMap;
    //
    //    /**
    //     * Lock for the submodelMap
    //     */
    //    private final ReentrantLock submodelMapLock;

    /**
     * The MessageBus for signalling changes, e.g. changed values
     */
    private final MessageBus messageBus;

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
        submodelElementRefMap = new HashMap<>();
        submodelElementRefMapLock = new ReentrantLock();
        submodelElementAasMapLock = new ReentrantLock();
        submodelElementAasMap = new HashMap<>();
        submodelElementOpcUAMap = new HashMap<>();
        submodelElementOpcUAMapLock = new ReentrantLock();
        submodelOpcUAMap = new HashMap<>();
        submodelOpcUAMapLock = new ReentrantLock();
        //submodelMap = new HashMap<>();
        //submodelMapLock = new ReentrantLock();

        messageBus = ep.getMessageBus();
        subscriptions = new ArrayList<>();
    }

    /**
     * Initialize static maps
     */
    static {
        for (KeyType keyType: KeyType.values()) {
            keyTypeStringMap.put(keyType.name().toUpperCase(), keyType);
        }
    }

    /**
     * Initializes the Node Manager
     *
     * @throws StatusException If the operation fails
     * @throws UaNodeFactoryException Error creating nodes
     */
    @Override
    protected void init() throws StatusException, UaNodeFactoryException {
        super.init();

        createAddressSpace();
    }


    /**
     * Closes the NodeManager
     */
    @Override
    protected void close() {
        try {
            unsubscribeMessageBus();
        }
        catch (Throwable ex) {
            logger.error("close Exception", ex);
        }

        super.close();
    }


    /**
     * Gets the AAS Data from the given NodeId.
     *
     * @param node The desired NodeId
     * @return The associated AAS Data, null if it was not found
     */
    public SubmodelElementData getAasData(NodeId node) {
        SubmodelElementData retval = null;

        try {
            submodelElementAasMapLock.lock();
            if (submodelElementAasMap.containsKey(node)) {
                retval = submodelElementAasMap.get(node);
                logger.debug("getAasSubmodelElement: NodeId: " + node + "; Property " + retval);
            }
            else {
                logger.info("Node " + node.toString() + " not found in submodelElementMap");
            }
        }
        catch (Throwable ex) {
            logger.error("getAasSubmodelElement Exception", ex);
            throw ex;
        }
        finally {
            submodelElementAasMapLock.unlock();
        }

        return retval;
    }

    //    /**
    //     * Gets the Submodel from the given SubmodelElement
    //     * 
    //     * @param node The desired Nodeid
    //     * @return The corresponding Submodel, null if it was not found
    //     */
    //    public Submodel getAasSubmodel(NodeId node) {
    //        Submodel retval = null;
    //        try {
    //            submodelMapLock.lock();
    //            if (submodelMap.containsKey(node)) {
    //                retval = submodelMap.get(node);
    //                logger.debug("getAasSubmodel: Node " + node.toString() + "; Submodel " + retval.getIdentification().getIdentifier());
    //            }
    //            else {
    //                logger.info("getAasSubmodel: Node " + node.toString() + " not found in submodelMap");
    //            }
    //        }
    //        catch (Throwable ex) {
    //            logger.error("getAasSubmodel Exception", ex);
    //            throw ex;
    //        }
    //        finally {
    //            submodelMapLock.unlock();
    //        }
    //
    //        return retval;
    //    }


    //    /**
    //     * The Value of the given Node was written.
    //     * 
    //     * @param nodeId The desired Node
    //     * @param newValue The new value
    //     */
    //    public void writeValue(NodeId nodeId, String newValue) {
    //        try {
    //            // TODO Service not yet implemented
    //
    //            //            if (messageBus != null) {
    //            //                Reference ref = getReferenceFromNodeId(nodeId);
    //            //
    //            //                logger.info("writeValue: Ref " + ref.toString() + "; old Value: " + oldValue + "; new Value: " + newValue);
    //            //                ValueChangeEventMessage valueChangeMessage = new ValueChangeEventMessage();
    //            //                valueChangeMessage.setElement(ref);
    //            //                PropertyValue propertyValue = new PropertyValue();
    //            //                propertyValue.setValue(oldValue);
    //            //                valueChangeMessage.setOldValue(propertyValue);
    //            //                propertyValue.setValue(newValue);
    //            //                valueChangeMessage.setNewValue(propertyValue);
    //            //                messageBus.publish(valueChangeMessage);
    //            //            }
    //            //            else {
    //            //                logger.warn("writeValue: MessageBus not available!");
    //            //            }
    //        }
    //        catch (Throwable ex) {
    //            logger.error("writeValue Exception", ex);
    //        }
    //    }
    /**
     * Creates the address space of the OPC UA Server
     */
    private void createAddressSpace() {
        try {
            logger.info("createAddressSpace");

            aasMethodManagerListener = new AasServiceMethodManagerListener(endpoint, this);

            createAasNodes();
            subscribeMessageBus();
        }
        catch (Throwable ex) {
            logger.error("createAddressSpace Exception", ex);
        }
    }


    /**
     * Creates the AAS nodes in the address space
     */
    private void createAasNodes() {
        try {
            if (aasEnvironment != null) {
                // add AASEnvironmentType
                addAasEnvironmentNode();

                // ConceptDescriptions. Necessary?
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

                TypeDefinitionBasedNodeBuilderConfiguration.Builder conf = TypeDefinitionBasedNodeBuilderConfiguration.builder();
                for (AssetAdministrationShell aas: aasEnvironment.getAssetAdministrationShells()) {
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
                }
            }
        }
        catch (Throwable ex) {
            logger.error("createAasNodes Exception", ex);
        }
    }


    /**
     * Adds a list of AAS Concept Descriptions
     *
     * @param descriptions The desired list of AAS Concept Descriptions
     * @throws StatusException If the operation fails
     * @throws ServiceResultException Generic service exception
     */
    private void addConceptDescriptions(List<ConceptDescription> descriptions) throws StatusException, ServiceResultException {
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
                switch (c.getIdentification().getIdType()) {
                    case IRDI:
                        AASIrdiConceptDescriptionType irdiNode = createInstance(AASIrdiConceptDescriptionType.class, name, nid);
                        addIdentifiable(irdiNode, c.getIdentification(), c.getAdministration(), name);
                        addConceptDescriptionReference(irdiNode, getReference(c));
                        dictEntriesFolder.addComponent(irdiNode);
                        dictionaryMap.put(getReference(c), irdiNode);
                        break;

                    case IRI:
                        AASIriConceptDescriptionType iriNode = createInstance(AASIriConceptDescriptionType.class, name, nid);
                        addIdentifiable(iriNode, c.getIdentification(), c.getAdministration(), name);
                        addConceptDescriptionReference(iriNode, getReference(c));
                        dictEntriesFolder.addComponent(iriNode);
                        dictionaryMap.put(getReference(c), iriNode);
                        break;

                    default:
                        AASCustomConceptDescriptionType customNode = createInstance(AASCustomConceptDescriptionType.class, name, nid);
                        addIdentifiable(customNode, c.getIdentification(), c.getAdministration(), name);
                        addConceptDescriptionReference(customNode, getReference(c));
                        dictEntriesFolder.addComponent(customNode);
                        dictionaryMap.put(getReference(c), customNode);
                        break;
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addConceptDescriptions Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds AAS Identifiable information to the given node
     *
     * @param identifiableNode The desired node where the Identifiable
     *            information should be added
     * @param identifier The corresponding AAS Identifier
     * @param adminInfo The corresponding AAS Administrative Information
     * @param category The desired category
     */
    private void addIdentifiable(AASIdentifiableType identifiableNode, Identifier identifier, AdministrativeInformation adminInfo, String category) {
        try {
            //            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.IAASIdentifiableType.getNamespaceUri(), IDENTIFICATION_NAME)
            //                    .toQualifiedName(getNamespaceTable());
            //            NodeId nid = createNodeId(node, browseName);
            //            IAASIdentifiableType identifiable = createInstance(IAASIdentifiableTypeNode.class, nid, browseName, LocalizedText.english(IDENTIFICATION_NAME));

            if (identifier != null) {
                identifiableNode.getIdentificationNode().setId(identifier.getIdentifier());
                identifiableNode.getIdentificationNode().setIdType(convertIdentifierType(identifier.getIdType()));
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

            //            node.addReference(identifiable, Identifiers.Organizes, false);
            //            node.addReference(identifiable, UaNodeId.fromLocal(ReferenceTypeIds.HasInterface).asNodeId(getNamespaceTable()), false);
        }
        catch (Throwable ex) {
            logger.error("addIdentifiable Exception", ex);
        }
    }


    /**
     * Adds AAS Identifiable information to the given node
     *
     * @param conceptDescriptionNode The desired node where the Identifiable
     *            information should be added
     * @param identifier The corresponding AAS Identifier
     * @param adminInfo The corresponding AAS Administrative Information
     * @param category The desired category
     */
    private void addIdentifiable(AASIrdiConceptDescriptionType conceptDescriptionNode, Identifier identifier, AdministrativeInformation adminInfo, String category) {
        try {
            //            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.IAASIdentifiableType.getNamespaceUri(), IDENTIFICATION_NAME)
            //                    .toQualifiedName(getNamespaceTable());
            //            NodeId nid = createNodeId(node, browseName);
            //            IAASIdentifiableType identifiable = createInstance(IAASIdentifiableTypeNode.class, nid, browseName, LocalizedText.english(IDENTIFICATION_NAME));

            if (identifier != null) {
                conceptDescriptionNode.getIdentificationNode().setId(identifier.getIdentifier());
                conceptDescriptionNode.getIdentificationNode().setIdType(convertIdentifierType(identifier.getIdType()));
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

            //            node.addReference(identifiable, Identifiers.Organizes, false);
            //            node.addReference(identifiable, UaNodeId.fromLocal(ReferenceTypeIds.HasInterface).asNodeId(getNamespaceTable()), false);
        }
        catch (Throwable ex) {
            logger.error("addIdentifiable Exception", ex);
        }
    }


    /**
     * Adds AAS Identifiable information to the given node
     *
     * @param conceptDescriptionNode The desired node where the Identifiable
     *            information should be added
     * @param identifier The corresponding AAS Identifier
     * @param adminInfo The corresponding AAS Administrative Information
     * @param category The desired category
     */
    private void addIdentifiable(AASIriConceptDescriptionType conceptDescriptionNode, Identifier identifier, AdministrativeInformation adminInfo, String category) {
        try {
            //            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.IAASIdentifiableType.getNamespaceUri(), IDENTIFICATION_NAME)
            //                    .toQualifiedName(getNamespaceTable());
            //            NodeId nid = createNodeId(node, browseName);
            //            IAASIdentifiableType identifiable = createInstance(IAASIdentifiableTypeNode.class, nid, browseName, LocalizedText.english(IDENTIFICATION_NAME));

            if (identifier != null) {
                conceptDescriptionNode.getIdentificationNode().setId(identifier.getIdentifier());
                conceptDescriptionNode.getIdentificationNode().setIdType(convertIdentifierType(identifier.getIdType()));
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

            //            node.addReference(identifiable, Identifiers.Organizes, false);
            //            node.addReference(identifiable, UaNodeId.fromLocal(ReferenceTypeIds.HasInterface).asNodeId(getNamespaceTable()), false);
        }
        catch (Throwable ex) {
            logger.error("addIdentifiable Exception", ex);
        }
    }


    /**
     * Adds AAS Identifiable information to the given node
     *
     * @param conceptDescriptionNode The desired node where the Identifiable
     *            information should be added
     * @param identifier The corresponding AAS Identifier
     * @param adminInfo The corresponding AAS Administrative Information
     * @param category The desired category
     */
    private void addIdentifiable(AASCustomConceptDescriptionType conceptDescriptionNode, Identifier identifier, AdministrativeInformation adminInfo, String category) {
        try {
            //            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.IAASIdentifiableType.getNamespaceUri(), IDENTIFICATION_NAME)
            //                    .toQualifiedName(getNamespaceTable());
            //            NodeId nid = createNodeId(node, browseName);
            //            IAASIdentifiableType identifiable = createInstance(IAASIdentifiableTypeNode.class, nid, browseName, LocalizedText.english(IDENTIFICATION_NAME));

            if (identifier != null) {
                conceptDescriptionNode.getIdentificationNode().setId(identifier.getIdentifier());
                conceptDescriptionNode.getIdentificationNode().setIdType(convertIdentifierType(identifier.getIdType()));
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

            //            node.addReference(identifiable, Identifiers.Organizes, false);
            //            node.addReference(identifiable, UaNodeId.fromLocal(ReferenceTypeIds.HasInterface).asNodeId(getNamespaceTable()), false);
        }
        catch (Throwable ex) {
            logger.error("addIdentifiable Exception", ex);
        }
    }


    /**
     * Adds the AdminInformation Properties th the given node (if they don't
     * exist)
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
        catch (Throwable ex) {
            logger.error("addAdminInfProperties Exception", ex);
        }
    }


    /**
     * Creates a reference for the given submodel
     *
     * @param submodel The desired submodel
     * @return The created reference
     */
    private Reference getReference(Submodel submodel) {
        //Reference retval = getReference(submodel, KeyElements.SUBMODEL);
        Reference retval = AasUtils.toReference(submodel);

        return retval;
    }


    /**
     * Creates a reference for the given submodel
     *
     * @param submodel The desired submodel
     * @return The created reference
     */
    private Reference getReference(ConceptDescription cd) {
        //Reference retval = getReference(cd, KeyElements.CONCEPT_DESCRIPTION);
        Reference retval = AasUtils.toReference(cd);

        return retval;
    }


    //    /**
    //     * Creates a reference for the given Identifiable object
    //     * 
    //     * @param object The desired object
    //     * @return The created reference
    //     */
    //    private Reference getReference(Identifiable object, KeyElements keyElement) {
    //        if (object == null) {
    //            throw new IllegalArgumentException("object is null");
    //        }
    //
    //        Identifier identifier = object.getIdentification();
    //        if (identifier == null) {
    //            throw new IllegalArgumentException("identifier is null");
    //        }
    //
    //        Reference retval = null;
    //
    //        try {
    //            retval = new DefaultReference.Builder()
    //                    .key(new DefaultKey.Builder().idType(getKeyTypeFromIdentifier(identifier.getIdType())).value(identifier.getIdentifier()).type(keyElement).build()).build();
    //        }
    //        catch (Throwable ex) {
    //            logger.error("getReference Exception", ex);
    //            throw ex;
    //        }
    //
    //        return retval;
    //    }
    /**
     * Creates a reference for the given SubmodelElement and corresponding
     * submodel (parent).
     *
     * @param submodelElement The desired SubmodelElement
     * @param submodel The corresponding Submodel as parent object of the data
     *            element
     * @return The created reference
     */
    private Reference getReference(SubmodelElement submodelElement, Submodel submodel) {
        if (submodelElement == null) {
            throw new IllegalArgumentException("submodelElement is null");
        }
        else if (submodel == null) {
            throw new IllegalArgumentException("submodel is null");
        }

        Reference retval = AasUtils.toReference(AasUtils.toReference(submodel), submodelElement);

        //        KeyElements keyElements = KeyElements.SUBMODEL_ELEMENT;
        //
        //        if (submodelElement instanceof Property) {
        //            keyElements = KeyElements.PROPERTY;
        //        }
        //        else if (submodelElement instanceof Operation) {
        //            keyElements = KeyElements.OPERATION;
        //        }
        //        else if (submodelElement instanceof Range) {
        //            keyElements = KeyElements.RANGE;
        //        }
        //        else if (submodelElement instanceof Blob) {
        //            keyElements = KeyElements.BLOB;
        //        }
        //        else if (submodelElement instanceof MultiLanguageProperty) {
        //            keyElements = KeyElements.MULTI_LANGUAGE_PROPERTY;
        //        }
        //        else if (submodelElement instanceof Event) {
        //            keyElements = KeyElements.EVENT;
        //        }
        //        else if (submodelElement instanceof Entity) {
        //            keyElements = KeyElements.ENTITY;
        //        }
        //        else if (submodelElement instanceof File) {
        //            keyElements = KeyElements.FILE;
        //        }
        //        else if (submodelElement instanceof ReferenceElement) {
        //            keyElements = KeyElements.REFERENCE_ELEMENT;
        //        }
        //        else if (submodelElement instanceof RelationshipElement) {
        //            keyElements = KeyElements.RELATIONSHIP_ELEMENT;
        //        }
        //        else if (submodelElement instanceof Capability) {
        //            keyElements = KeyElements.CAPABILITY;
        //        }
        //        else if (submodelElement instanceof SubmodelElementCollection) {
        //            keyElements = KeyElements.SUBMODEL_ELEMENT_COLLECTION;
        //        }
        //
        //        Reference retval = getReference(submodelElement, keyElements, submodel, KeyElements.SUBMODEL);
        return retval;
    }

    //    /**
    //     * Creates a reference for the given Referable object and corresponding
    //     * parent.
    //     *
    //     * @param object The desired object
    //     * @param objectKeyElement The KeyElements type of the desired object
    //     * @param parent The corresponding parent of the object
    //     * @param parentKeyElement The KeyElements type of the parent
    //     * @return The created reference
    //     */
    //    private Reference getReference(Referable object, KeyElements objectKeyElement, Identifiable parent, KeyElements parentKeyElement) {
    //        if (object == null) {
    //            throw new IllegalArgumentException("object is null");
    //        }
    //
    //        Identifier parentIdentifier = parent.getIdentification();
    //        if (parentIdentifier == null) {
    //            throw new IllegalArgumentException("parentIdentifier is null");
    //        }
    //
    //        Reference retval = null;
    //
    //        try {
    //            List<Key> keys = new ArrayList<>();
    //            keys.add(
    //                    new DefaultKey.Builder().idType(getKeyTypeFromIdentifier(parentIdentifier.getIdType())).value(parentIdentifier.getIdentifier()).type(parentKeyElement).build());
    //            keys.add(new DefaultKey.Builder().idType(KeyType.ID_SHORT).value(object.getIdShort()).type(objectKeyElement).build());
    //
    //            retval = new DefaultReference.Builder().keys(keys).build();
    //        }
    //        catch (Throwable ex) {
    //            logger.error("getReference Exception", ex);
    //            throw ex;
    //        }
    //
    //        return retval;
    //    }


    /**
     * Adds a reference to a ConceptDescription
     *
     * @param node The desired UA node
     * @param ref The reference to create
     * @throws StatusException If the operation fails
     * @throws ServiceResultException Generic service exception
     */
    private void addConceptDescriptionReference(UaNode node, Reference ref) throws StatusException, ServiceResultException {
        try {
            if (ref != null) {
                String name = "ConceptDescription";
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASMultiLanguagePropertyType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(node, browseName);
                AASReferenceType nodeRef = createInstance(AASReferenceTypeNode.class, nid, browseName, LocalizedText.english(name));

                setAasReferenceData(ref, nodeRef);
                node.addComponent(nodeRef);
                node.addReference(nodeRef, Identifiers.HasDictionaryEntry, false);
                //node.addReference(nodeRef, getNamespaceTable().toNodeId(ReferenceTypeIds.IsCaseOf), false);
            }
        }
        catch (Throwable ex) {
            logger.error("addConceptDescriptionReference Exception", ex);
            throw ex;
        }
    }


    /**
     * Sets the data in the given Reference node
     *
     * @param ref The desired UA reference object
     * @param refNode The AAS Reference object with the source data
     * @throws StatusException If the operation fails
     */
    private void setAasReferenceData(Reference ref, AASReferenceType refNode) throws StatusException {
        setAasReferenceData(ref, refNode, VALUES_READ_ONLY);
    }


    /**
     * Sets the data in the given Reference node
     *
     * @param ref The desired UA reference object
     * @param refNode The AAS Reference object with the source data
     * @param readOnly True if the value should be read-only
     * @throws StatusException If the operation fails
     */
    private void setAasReferenceData(Reference ref, AASReferenceType refNode, boolean readOnly) throws StatusException {
        if (refNode == null) {
            throw new IllegalArgumentException("refNode is null");
        }
        else if (ref == null) {
            throw new IllegalArgumentException("ref is null");
        }

        try {
            List<AASKeyDataType> keyList = new ArrayList<>();
            ref.getKeys().stream().map(k -> {
                AASKeyDataType keyValue = new AASKeyDataType();
                keyValue.setIdType(ValueConverter.getAasKeyType(k.getIdType()));
                keyValue.setType(ValueConverter.getAasKeyElementsDataType(k.getType()));
                keyValue.setValue(k.getValue());
                return keyValue;
            }).forEachOrdered(keyValue -> {
                keyList.add(keyValue);
            });

            refNode.getKeysNode().setArrayDimensions(new UnsignedInteger[] {
                    UnsignedInteger.valueOf(keyList.size())
            });
            if (readOnly) {
                refNode.getKeysNode().setAccessLevel(AccessLevelType.CurrentRead);
            }
            refNode.setKeys(keyList.toArray(AASKeyDataType[]::new));
        }
        catch (Throwable ex) {
            logger.error("setAasReferenceData Exception", ex);
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
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
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
                assetInfoNode.setAssetKind(convertAssetKind(assetKind));

                // BillOfMaterials
                List<Reference> assetBills = assetInformation.getBillOfMaterials();
                if ((assetBills != null) && (!assetBills.isEmpty())) {
                    AASReferenceList assetBillsNode = assetInfoNode.getBillOfMaterialNode();
                    addBillOfMaterials(assetBillsNode, assetBills);
                }

                // DefaultThumbnail
                File thumbnail = assetInformation.getDefaultThumbnail();
                if (thumbnail != null) {
                    addAasFile(assetInfoNode, thumbnail, false, AASAssetInformationType.DEFAULT_THUMBNAIL);
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

            logger.error("Method addAssetInformation not implemented");
        }
        catch (Throwable ex) {
            logger.error("addAssetInformation Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the BillOfMaterial objects to the given Node.
     *
     * @param node The desired node where the BillOfMaterials should be added
     * @param billOfMaterials The desired list of BillOfMaterials
     * @throws StatusException If the operation fails
     */
    private void addBillOfMaterials(UaNode node, List<Reference> billOfMaterials) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException("node = null");
        }
        else if (billOfMaterials == null) {
            throw new IllegalArgumentException("billOfMaterials = null");
        }

        try {
            addAasReferenceList(node, billOfMaterials, "BillOfMaterial");
        }
        catch (Throwable ex) {
            logger.error("addBillOfMaterials Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS Reference to the given node
     *
     * @param node The node in which the object is created
     * @param ref The desired AAS reference object to add
     * @param name The desired name
     * @return The created node
     * @throws StatusException If the operation fails
     */
    private UaNode addAasReferenceAasNS(UaNode node, Reference ref, String name) throws StatusException {
        UaNode retval = null;

        try {
            retval = addAasReference(node, ref, name, opc.i4aas.ObjectTypeIds.AASReferenceType.getNamespaceUri());
        }
        catch (Throwable ex) {
            logger.error("addAasReferenceAasNS Exception", ex);
            throw ex;
        }

        return retval;
    }


    /**
     * Adds an AAS Reference to the given node with the AAS namespace (e.g. for
     * DataSpecification)
     *
     * @param node The node in which the object is created
     * @param ref The desired AAS reference object to add
     * @param name The desired name
     * @param namespaceUri The desired namespace URI tu use
     * @return The created node
     * @throws StatusException If the operation fails
     */
    private UaNode addAasReference(UaNode node, Reference ref, String name, String namespaceUri) throws StatusException {
        UaNode retval = null;

        try {
            if (ref != null) {
                QualifiedName browseName = UaQualifiedName.from(namespaceUri, name).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(node, browseName);
                AASReferenceType nodeRef = createInstance(AASReferenceType.class, nid, browseName, LocalizedText.english(name));

                logger.debug("addAasReference: add Node " + nid + " to Node " + node.getNodeId());

                setAasReferenceData(ref, nodeRef);

                //nodeRef.addReference(nodeRef.getKeysNode().getNodeId(), getNamespaceTable().toNodeId(opc.i4aas.ReferenceTypeIds.AASReference), false);
                node.addComponent(nodeRef);

                retval = nodeRef;
            }
        }
        catch (Throwable ex) {
            logger.error("addAasReference Exception", ex);
            throw ex;
        }

        return retval;
    }


    /**
     * Adds an AAS file to the given UA node
     *
     * @param node The desired UA node
     * @param aasFile The AAS file object
     * @param ordered Specifies whether the file should be added ordered (true)
     *            or unordered (false)
     * @param nodeName The desired Name of the node. If this value is not set,
     *            the IdShort of the file is used.
     * @throws StatusException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    private void addAasFile(UaNode node, File aasFile, boolean ordered, String nodeName) throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        try {
            if ((node != null) && (aasFile != null)) {
                String name = aasFile.getIdShort();
                if ((nodeName != null) && (!nodeName.isEmpty())) {
                    name = nodeName;
                }

                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASFileType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(node, browseName);
                AASFileType fileNode = createInstance(AASFileType.class, nid, browseName, LocalizedText.english(name));
                addSubmodelElementBaseData(fileNode, aasFile, name);

                // MimeType
                if (!aasFile.getMimeType().isEmpty()) {
                    fileNode.setMimeType(aasFile.getMimeType());
                }

                // Value
                if (aasFile.getValue() != null) {
                    if (fileNode.getValueNode() == null) {
                        addFileValueNode(fileNode);
                    }

                    fileNode.setValue(aasFile.getValue());

                    if (!aasFile.getValue().isEmpty()) {
                        java.io.File f = new java.io.File(aasFile.getValue());
                        if (!f.exists()) {
                            logger.warn("addAasFile: File '" + f.getAbsolutePath() + "' does not exist!");
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

                            //fileNode.addComponent(fileType);
                            fileNode.addReference(fileType, Identifiers.HasAddIn, false);
                        }
                    }
                }

                if (ordered) {
                    node.addReference(fileNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(fileNode);
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addAasFile Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds a File Value Property to the fiven Node
     *
     * @param fileNode The desired File Node.
     */
    private void addFileValueNode(UaNode fileNode) {
        try {
            NodeId myPropertyId = new NodeId(getNamespaceIndex(), fileNode.getNodeId().getValue().toString() + "." + AASFileType.VALUE);
            PlainProperty<String> myProperty = new PlainProperty<>(this, myPropertyId,
                    UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASFileType.getNamespaceUri(), AASFileType.VALUE).toQualifiedName(getNamespaceTable()),
                    LocalizedText.english(AASFileType.VALUE));
            myProperty.setDataTypeId(Identifiers.String);
            if (VALUES_READ_ONLY) {
                myProperty.setAccessLevel(AccessLevelType.CurrentRead);
            }
            myProperty.setDescription(new LocalizedText("", ""));
            fileNode.addProperty(myProperty);
        }
        catch (Throwable ex) {
            logger.error("addFileFileNode Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds base data to the given submodel element
     *
     * @param node The desired submodel element UA node
     * @param element The corresponding AAS submodel element
     * @param name The desired name
     * @throws StatusException If the operation fails
     * @throws ServiceException If the operation fails
     * @throws AddressSpaceException If the operation fails
     * @throws ServiceResultException If the operation fails
     */
    private void addSubmodelElementBaseData(AASSubmodelElementType node, SubmodelElement element, String name)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        try {
            if ((node != null) && (element != null)) {
                // Category
                String category = element.getCategory();
                if (category == null) {
                    category = "";
                }
                node.setCategory(category);

                node.setModelingKind(convertModelingKind(element.getKind()));

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
                    //node.getIdShortNode().setAccessLevel(AccessLevelType.CurrentRead);
                    node.getCategoryNode().setAccessLevel(AccessLevelType.CurrentRead);
                    node.getModelingKindNode().setAccessLevel(AccessLevelType.CurrentRead);
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addSubmodelElementBaseData Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds a SemanticId
     *
     * @param node The UA node in which the SemanticId should be created
     * @param semanticId The reference of the desired SemanticId
     */
    private void addSemanticId(UaNode node, Reference semanticId) {
        try {
            // SemanticId belongs to the instance not the type
            // That's why we use the server namespace here
            // TODO: necessary?
            //addAasReferenceServerNS(node, semanticId, "SemanticId");

            if (dictionaryMap.containsKey(semanticId)) {
                node.addReference(dictionaryMap.get(semanticId), Identifiers.HasDictionaryEntry, false);
            }
            // if entry not found: perhaps create a new one?
        }
        catch (Throwable ex) {
            logger.error("addSemanticId Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the references to Embedded Data Specifications
     *
     * @param submodelNode The desired object where the DataSpecifications
     *            should be added
     * @param ds The list of the desired Data Specifications
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
                    addEmbeddedDataSpecifications(listNode, refList);
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addEmbeddedDataSpecifications Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the references to Embedded Data Specifications
     *
     * @param assetNode The desired object where the DataSpecifications should
     *            be added
     * @param ds The list of the desired Data Specifications
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
                    addEmbeddedDataSpecifications(listNode, refList);
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addEmbeddedDataSpecifications Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the references to Embedded Data Specifications
     *
     * @param submodelNode The desired object where the DataSpecifications
     *            should be added
     * @param ds The list of the desired Data Specifications
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
                    addEmbeddedDataSpecifications(listNode, refList);
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addEmbeddedDataSpecifications Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the references to Embedded Data Specifications
     *
     * @param submodelElementNode The desired object where the
     *            DataSpecifications should be added
     * @param ds The list of the desired Data Specifications
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
                    addEmbeddedDataSpecifications(listNode, refList);
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addEmbeddedDataSpecifications Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the references to Embedded Data Specifications
     *
     * @param refListNode The desired object where the DataSpecifications should
     *            be added
     * @param ds The list of the desired Data Specifications
     * @throws StatusException If the operation fails
     */
    private void addEmbeddedDataSpecifications(AASReferenceList refListNode, List<Reference> refList) throws StatusException {
        try {
            if ((refListNode != null) && (!refList.isEmpty())) {
                int count = 0;
                for (Reference ref: refList) {
                    count++;
                    //String name = AASAssetAdministrationShellType.DATA_SPECIFICATION.replace("<", "").replace(">", "");
                    String name = AASAssetAdministrationShellType.DATA_SPECIFICATION;
                    if (count > 1) {
                        name += count;
                    }

                    addAasReferenceAasNS(refListNode, ref, name);
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addEmbeddedDataSpecifications Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the list of Descriptions to the given node
     *
     * @param node The desired UA node in which the Descriptions should be
     *            created
     * @param descriptions The list of AAS descriptions
     */
    private void addDescriptions(UaNode node, List<LangString> descriptions) {
        try {
            if ((node != null) && (descriptions != null)) {
                if (!descriptions.isEmpty()) {
                    LangString desc = descriptions.get(0);
                    node.setDescription(new LocalizedText(desc.getValue(), desc.getLanguage()));
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addDescriptions Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the description to the given argument
     *
     * @param arg The desired UA argument
     * @param descriptions The list of AAS descriptions
     */
    private void addDescriptions(Argument arg, List<LangString> descriptions) {
        try {
            if ((arg != null) && (descriptions != null)) {
                if (!descriptions.isEmpty()) {
                    LangString desc = descriptions.get(0);
                    arg.setDescription(new LocalizedText(desc.getValue(), desc.getLanguage()));
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addDescriptions Exception", ex);
        }
    }


    /**
     * Adds a QualifierNode to the given Node
     *
     * @param node The desired base node
     */
    private void addQualifierNode(UaNode node) {
        try {
            String name = AASSubmodelElementType.QUALIFIER;
            logger.info("addQualifierNode " + name + "; to Node: " + node.toString());
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASQualifierList.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
            NodeId nid = createNodeId(node, browseName);
            AASQualifierList listNode = createInstance(AASQualifierList.class, nid, browseName, LocalizedText.english(name));

            node.addComponent(listNode);
        }
        catch (Throwable ex) {
            logger.error("addQualifierNode Exception", ex);
        }
    }


    /**
     * Adds a list of Qualifiers to the given Node
     *
     * @param listNode The UA node in which the Qualifiers should be created
     * @param qualifiers The desired list of Qualifiers
     */
    private void addQualifiers(AASQualifierList listNode, List<Constraint> qualifiers) throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
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
        catch (Throwable ex) {
            logger.error("addQualifiers Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds a Qualifier to the given Node
     *
     * @param node The UA node in which the Qualifier should be created
     * @param qualifier The desired Qualifier
     * @param name The name of the qualifier
     */
    private void addQualifier(UaNode node, Qualifier qualifier, String name) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException("node = null");
        }
        else if (qualifier == null) {
            throw new IllegalArgumentException("qualifier = null");
        }

        try {
            logger.info("addQualifier " + name + "; to Node: " + node.toString());
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

            node.addComponent(qualifierNode);
        }
        catch (Throwable ex) {
            logger.error("addQualifier Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds a list of IdentifierKeyValuePairs to the given Node
     *
     * @param node The AssetInformation node in which the
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
            logger.info("addSpecificAssetIds " + name + "; to Node: " + assetInfoNode.toString());
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
                    addIdentifierKeyValuePair(listNode, ikv);
                }
            }

            if (created) {
                assetInfoNode.addComponent(listNode);
            }
        }
        catch (Throwable ex) {
            logger.error("addSpecificAssetIds Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an IdentifierKeyValuePair to the given Node
     *
     * @param node The UA node in which the IdentifierKeyValuePair should be
     *            created
     * @param identifierPair The desired IdentifierKeyValuePair
     * @throws StatusException If the operation fails
     */
    private void addIdentifierKeyValuePair(UaNode node, IdentifierKeyValuePair identifierPair) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException("node = null");
        }
        else if (identifierPair == null) {
            throw new IllegalArgumentException("identifierPair = null");
        }

        try {
            String name = identifierPair.getKey();
            logger.info("addIdentifierKeyValuePair " + name + "; to Node: " + node.toString());
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASIdentifierKeyValuePairType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
            NodeId nid = createNodeId(node, browseName);
            AASIdentifierKeyValuePairType identifierPairNode = createInstance(AASIdentifierKeyValuePairType.class, nid, browseName, LocalizedText.english(name));

            // ExternalSubjectId
            Reference externalSubjectId = identifierPair.getExternalSubjectId();
            if (externalSubjectId != null) {
                AASReferenceType extSubjectNode = identifierPairNode.getExternalSubjectIdNode();
                if (extSubjectNode == null) {
                    addAasReferenceAasNS(identifierPairNode, externalSubjectId, "ExternalSubjectId");
                }
                else {
                    setAasReferenceData(externalSubjectId, extSubjectNode);
                }
            }

            // Key
            identifierPairNode.setKey(identifierPair.getKey());

            // Value
            identifierPairNode.setValue(identifierPair.getValue());

            node.addComponent(identifierPairNode);
        }
        catch (Throwable ex) {
            logger.error("addIdentifierKeyValuePair Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds a list of references to the given Node
     *
     * @param node The UA node in which the list of references should be created
     * @param list The desired list of references
     * @param name The desired name of the Node
     * @throws StatusException If the operation fails
     */
    private void addAasReferenceList(UaNode node, List<Reference> list, String name) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException("node = null");
        }
        else if (list == null) {
            throw new IllegalArgumentException("list = null");
        }

        try {
            logger.info("addAasReferenceList " + name + "; to Node: " + node.toString());
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASReferenceList.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
            NodeId nid = createNodeId(node, browseName);
            AASReferenceList referenceListNode = createInstance(AASReferenceList.class, nid, browseName, LocalizedText.english(name));

            int counter = 1;
            for (Reference ref: list) {
                addAasReferenceAasNS(referenceListNode, ref, name + counter++);
            }

            node.addComponent(referenceListNode);
        }
        catch (Throwable ex) {
            logger.error("addAasReferenceList Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the AASEnvironment Node
     */
    private void addAasEnvironmentNode() {
        try {
            final UaObject objectsFolder = getServer().getNodeManagerRoot().getObjectsFolder();
            if (aasEnvironment != null) {
                String name = "AASEnvironment";
                logger.info("addAasEnvironmentType " + name + "; to ObjectsFolder");
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASEnvironmentType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(objectsFolder, browseName);
                aasEnvironmentNode = createInstance(AASEnvironmentType.class, nid, browseName, LocalizedText.english(name));

                objectsFolder.addComponent(aasEnvironmentNode);
            }
        }
        catch (Throwable ex) {
            logger.error("addAasEnvironmentNode Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an Asset to the given Node
     *
     * @param node The UA node in which the Qualifier should be created
     * @param asset The desired Asset
     * @throws StatusException If the operation fails
     */
    private void addAsset(UaNode node, Asset asset) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException("node = null");
        }
        else if (asset == null) {
            throw new IllegalArgumentException("asset = null");
        }

        try {
            String name = asset.getIdShort();
            String displayName = "Asset:" + name;
            logger.info("addAsset " + name + "; to Node: " + node.toString());
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASAssetType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
            NodeId nid = createNodeId(node, browseName);
            AASAssetType assetNode = createInstance(AASAssetType.class, nid, browseName, LocalizedText.english(displayName));

            addIdentifiable(assetNode, asset.getIdentification(), asset.getAdministration(), asset.getCategory());

            // DataSpecifications
            addEmbeddedDataSpecifications(assetNode, asset.getEmbeddedDataSpecifications());

            node.addComponent(assetNode);
        }
        catch (Throwable ex) {
            logger.error("addAsset Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds a submodel to a given UANode
     *
     * @param node The desired UANode where the submodel should be added
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

                logger.trace("addSubmodel: create Submodel " + submodel.getIdShort() + "; NodeId: " + nid.toString());
                AASSubmodelType smNode = createInstance(AASSubmodelType.class, nid, browseName, LocalizedText.english(displayName));

                // ModelingKind
                smNode.setModelingKind(convertModelingKind(submodel.getKind()));
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

                // SubmodelElements
                addSubmodelElements(smNode, submodel.getSubmodelElements(), submodel);

                if (VALUES_READ_ONLY) {
                    smNode.getModelingKindNode().setAccessLevel(AccessLevelType.CurrentRead);
                }

                try {
                    submodelOpcUAMapLock.lock();
                    submodelOpcUAMap.put(getReference(submodel), smNode);
                }
                catch (Exception e2) {
                    submodelOpcUAMapLock.unlock();
                    logger.error("Error when adding to submodelRefMap", e2);
                }

                node.addComponent(smNode);
            }
            else {
                logger.warn("addSubmodel: IdShort is empty!");
            }
        }
        catch (Throwable ex) {
            logger.error("addSubmodel Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds submodel elements to the given node
     *
     * @param node The desired UA node in which the objects should be created
     * @param elements The desired list of submodel elements
     * @param submodel The corresponding submodel
     */
    private void addSubmodelElements(UaNode node, List<SubmodelElement> elements, Submodel submodel)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        addSubmodelElements(node, elements, submodel, false);
    }


    /**
     * Adds submodel elements to the given node (ordered if desired)
     *
     * @param node The desired UA node in which the objects should be created
     * @param elements The desired list of submodel elements
     * @param submodel The corresponding submodel
     * @param ordered Specifies where the elements should de added ordered
     *            (true) or unordered (false)
     */
    private void addSubmodelElements(UaNode node, Collection<SubmodelElement> elements, Submodel submodel, boolean ordered)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        try {
            if ((elements != null) && (!elements.isEmpty())) {
                for (SubmodelElement elem: elements) {
                    if ((submodel != null) && (elem != null)) {
                        try {
                            submodelElementRefMapLock.lock();
                            submodelElementRefMap.put(getReference(elem, submodel), getReference(submodel));
                        }
                        catch (Throwable ex) {
                            logger.error("addSubmodelElements RefMap Exception", ex);
                        }
                        finally {
                            submodelElementRefMapLock.unlock();
                        }
                    }

                    //                    try {
                    //                        submodelMapLock.lock();
                    //                        logger.debug("addSubmodelElements: " + elem.toString() + "; Submodel " + submodel);
                    //                        submodelMap.put(elem, submodel);
                    //                    }
                    //                    catch (Throwable ex) {
                    //                        logger.error("addSubmodelElements submodelMap Exception", ex);
                    //                    }
                    //                    finally {
                    //                        submodelMapLock.unlock();
                    //                    }

                    if (elem instanceof DataElement) {
                        addAasDataElement(node, (DataElement) elem, submodel, ordered);
                    }
                    else if (elem instanceof Capability) {
                        addAasCapability(node, (Capability) elem, ordered);
                    }
                    else if (elem instanceof Entity) {
                        addAasEntity(node, (Entity) elem, ordered);
                    }
                    else if (elem instanceof Operation) {
                        addAasOperation(node, (Operation) elem, submodel, ordered);
                    }
                    else if (elem instanceof Event) {
                        addAasEvent(node, (Event) elem, ordered);
                    }
                    else if (elem instanceof RelationshipElement) {
                        addAasRelationshipElement(node, (RelationshipElement) elem, submodel, ordered);
                    }
                    else if (elem instanceof SubmodelElementCollection) {
                        addAasSubmodelElementCollection(node, (SubmodelElementCollection) elem, submodel, ordered);
                    }
                    else if (elem != null) {
                        logger.warn("addSubmodelElements: unknown SubmodelElement: " + elem.getIdShort() + "; Class " + elem.getClass());
                    }
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addSubmodelElements Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS data element the given UA node
     *
     * @param node The desired UA node
     * @param aasDataElement The corresponding AAS data element to add
     * @param submodel The corresponding Submodel as parent object of the data
     *            element
     * @param ordered Specifies whether the element should be added ordered
     *            (true) or unordered (false)
     * @throws StatusException If the operation fails
     */
    private void addAasDataElement(UaNode node, DataElement aasDataElement, Submodel submodel, boolean ordered)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        try {
            if ((node != null) && (aasDataElement != null)) {
                if (aasDataElement instanceof Property) {
                    addAasProperty(node, (Property) aasDataElement, submodel, ordered);
                }
                else if (aasDataElement instanceof File) {
                    addAasFile(node, (File) aasDataElement, ordered, null);
                }
                else if (aasDataElement instanceof Blob) {
                    addAasBlob(node, (Blob) aasDataElement, submodel, ordered);
                }
                else if (aasDataElement instanceof ReferenceElement) {
                    addAasReferenceElement(node, (ReferenceElement) aasDataElement, submodel, ordered);
                }
                else if (aasDataElement instanceof Range) {
                    addAasRange(node, (Range) aasDataElement, submodel, ordered);
                }
                else if (aasDataElement instanceof MultiLanguageProperty) {
                    addAasMultiLanguageProperty(node, (MultiLanguageProperty) aasDataElement, submodel, ordered);
                }
                else {
                    logger.warn("addAasDataElement: unknown DataElement: " + aasDataElement.getIdShort() + "; Class " + aasDataElement.getClass());
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addAasDataElement Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS property the given UA node
     *
     * @param nodev The desired UA node
     * @param aasProperty The corresponding AAS property to add
     * @param submodel The corresponding Submodel as parent object of the data
     *            element
     * @param ordered Specifies whether the property should be added ordered
     *            (true) or unordered (false)
     */
    private void addAasProperty(UaNode node, Property aasProperty, Submodel submodel, boolean ordered) {
        try {
            String name = aasProperty.getIdShort();
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASPropertyType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
            NodeId nid = createNodeId(node, browseName);
            AASPropertyType prop = createInstance(AASPropertyType.class, nid, browseName, LocalizedText.english(name));
            addSubmodelElementBaseData(prop, aasProperty, name);

            //            try {
            //                submodelMapLock.lock();
            //                logger.debug("addSubmodelElements: " + aasProperty.toString() + "; Submodel " + submodel);
            //                submodelMap.put(prop.getValueNode(), submodel);
            //            }
            //            catch (Throwable ex) {
            //                logger.error("addSubmodelElements submodelMap Exception", ex);
            //            }
            //            finally {
            //                submodelMapLock.unlock();
            //            }

            // ValueId
            Reference ref = aasProperty.getValueId();
            if (ref != null) {
                addAasReferenceAasNS(prop, ref, AASPropertyType.VALUE_ID);
            }

            // here Value and ValueType are set
            setPropertyValueAndType(aasProperty, submodel, prop);

            if (VALUES_READ_ONLY) {
                prop.getValueTypeNode().setAccessLevel(AccessLevelType.CurrentRead);
            }

            if (ordered) {
                node.addReference(prop, Identifiers.HasOrderedComponent, false);
            }
            else {
                node.addComponent(prop);
            }
        }
        catch (Throwable ex) {
            logger.error("addAasProperty Exception", ex);
        }
    }


    /**
     * Adds the property itself to the given Property object and sets the value
     *
     * @param aasProperty The AAS property
     * @param submodel The corresponding Submodel as parent object of the data
     *            element
     * @param prop The UA Property object
     */
    private void setPropertyValueAndType(Property aasProperty, Submodel submodel, AASPropertyType prop) {
        try {
            NodeId myPropertyId = new NodeId(getNamespaceIndex(), prop.getNodeId().getValue().toString() + "." + AASPropertyType.VALUE);
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASPropertyType.getNamespaceUri(), AASPropertyType.VALUE).toQualifiedName(getNamespaceTable());
            LocalizedText displayName = LocalizedText.english(AASPropertyType.VALUE);
            String stringVal = aasProperty.getValue();

            try {
                submodelElementAasMapLock.lock();
                submodelElementAasMap.put(myPropertyId, new SubmodelElementData(aasProperty, submodel, SubmodelElementData.Type.PROPERTY_VALUE));
                logger.debug("setPropertyValueAndType: NodeId " + myPropertyId + "; Property: " + aasProperty);
            }
            catch (Exception ex2) {
                logger.warn("submodelElementAasMap problem", ex2);
            }
            finally {
                submodelElementAasMapLock.unlock();
            }

            if (submodel != null) {
                //                try {
                //                    submodelMapLock.lock();
                //                    logger.debug("addSubmodelElements: " + aasProperty.toString() + "; Submodel " + submodel);
                //                    submodelMap.put(myPropertyId, submodel);
                //                }
                //                catch (Throwable ex) {
                //                    logger.error("addSubmodelElements submodelMap Exception", ex);
                //                }
                //                finally {
                //                    submodelMapLock.unlock();
                //                }

                try {
                    Reference propRef = getReference(aasProperty, submodel);
                    submodelElementOpcUAMapLock.lock();
                    submodelElementOpcUAMap.put(propRef, prop);
                }
                catch (Exception ex3) {
                    logger.warn("submodelElementOpcUAMap problem", ex3);
                }
                finally {
                    submodelElementOpcUAMapLock.unlock();
                }
            }

            AASValueTypeDataType valueDataType = ValueConverter.stringToValueType(aasProperty.getValueType());
            prop.setValueType(valueDataType);

            // temporary solution for the "Value is String" problem!
            PlainProperty<String> myStringProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            myStringProperty.setDataTypeId(Identifiers.String);
            myStringProperty.setValue(stringVal);
            prop.addProperty(myStringProperty);

            //            switch (valueDataType) {
            //                //                case AnyURI:
            //                //                    PlainProperty<String> myUriProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            //                //                    myUriProperty.setDataTypeId(Identifiers.String);
            //                //                    if (val != null) {
            //                //                        myUriProperty.setValue(((AnyUri)val).getValue().toString());
            //                //                    }
            //                //                    prop.addProperty(myUriProperty);
            //                //                    break;
            //
            //                case ByteString:
            //                    PlainProperty<ByteString> myBSProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            //                    myBSProperty.setDataTypeId(Identifiers.ByteString);
            //                    // TODO integrate Property value
            //                    //if (val != null) {
            //                    //    myBSProperty.setValue(((Base64Binary)val).getValue());
            //                    //}
            //                    prop.addProperty(myBSProperty);
            //                    break;
            //
            //                case Boolean:
            //                    PlainProperty<Boolean> myBoolProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            //                    myBoolProperty.setDataTypeId(Identifiers.Boolean);
            //                    // TODO integrate Property value
            //                    //if (val != null) {
            //                    //    myBoolProperty.setValue(((BooleanValue)val).getValue());
            //                    //}
            //                    prop.addProperty(myBoolProperty);
            //                    break;
            //
            //                case DateTime:
            //                    PlainProperty<DateTime> myDateProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            //                    myDateProperty.setDataTypeId(Identifiers.DateTime);
            //                    // TODO integrate Property value
            //                    //if (val != null) {
            //                    //    myDateProperty.setValue(new DateTime(((DateValue)val).getValue().toGregorianCalendar()));
            //                    //}
            //                    prop.addProperty(myDateProperty);
            //                    break;
            //
            //                //                case Decimal:
            //                //                    PlainProperty<Long> myDecProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            //                //                    myDecProperty.setDataTypeId(Identifiers.Int64);
            //                //                    if (val != null) {
            //                //                        myDecProperty.setValue(((DecimalValue)val).getValue().longValue());
            //                //                    }
            //                //                    prop.addProperty(myDecProperty);
            //                //                    break;
            //
            //                case Int32:
            //                    PlainProperty<Integer> myIntProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            //                    myIntProperty.setDataTypeId(Identifiers.Int32);
            //                    // TODO integrate Property value
            //                    //if (val != null) {
            //                    //    myIntProperty.setValue(((IntValue)val).getValue());
            //                    //}
            //                    prop.addProperty(myIntProperty);
            //                    break;
            //
            //                case UInt32:
            //                    PlainProperty<UnsignedInteger> myUIntProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            //                    myUIntProperty.setDataTypeId(Identifiers.UInt32);
            //                    // TODO integrate Property value
            //                    //if (val != null) {
            //                    //    myIntProperty.setValue(((IntValue)val).getValue());
            //                    //}
            //                    prop.addProperty(myUIntProperty);
            //                    break;
            //
            //                case Int64:
            //                    PlainProperty<Long> myLongProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            //                    myLongProperty.setDataTypeId(Identifiers.Int64);
            //                    // TODO integrate Property value
            //                    //if (val != null) {
            //                    //    myLongProperty.setValue(((LongValue)val).getValue());
            //                    //}
            //                    prop.addProperty(myLongProperty);
            //                    break;
            //
            //                case UInt64:
            //                    PlainProperty<UnsignedLong> myULongProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            //                    myULongProperty.setDataTypeId(Identifiers.UInt64);
            //                    // TODO integrate Property value
            //                    //if (val != null) {
            //                    //    myLongProperty.setValue(((LongValue)val).getValue());
            //                    //}
            //                    prop.addProperty(myULongProperty);
            //                    break;
            //
            //                case Int16:
            //                    PlainProperty<Short> myInt16Property = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            //                    myInt16Property.setDataTypeId(Identifiers.Int16);
            //                    // TODO integrate Property value
            //                    //if (val != null) {
            //                    //    myInt16Property.setValue(((ShortValue)val).getValue());
            //                    //}
            //                    prop.addProperty(myInt16Property);
            //                    break;
            //
            //                case UInt16:
            //                    PlainProperty<UnsignedShort> myUInt16Property = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            //                    myUInt16Property.setDataTypeId(Identifiers.UInt16);
            //                    // TODO integrate Property value
            //                    //if (val != null) {
            //                    //    myInt16Property.setValue(((ShortValue)val).getValue());
            //                    //}
            //                    prop.addProperty(myUInt16Property);
            //                    break;
            //
            //                case Byte:
            //                    PlainProperty<UnsignedByte> myByteProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            //                    myByteProperty.setDataTypeId(Identifiers.Byte);
            //                    // TODO integrate Property value
            //                    //if (val != null) {
            //                    //    myByteProperty.setValue(((ByteValue)val).getValue());
            //                    //}
            //                    prop.addProperty(myByteProperty);
            //                    break;
            //
            //                case SByte:
            //                    PlainProperty<Byte> mySByteProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            //                    mySByteProperty.setDataTypeId(Identifiers.SByte);
            //                    // TODO integrate Property value
            //                    //if (val != null) {
            //                    //    myByteProperty.setValue(((ByteValue)val).getValue());
            //                    //}
            //                    prop.addProperty(mySByteProperty);
            //                    break;
            //
            //                case Double:
            //                    PlainProperty<Double> myDoubleProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            //                    myDoubleProperty.setDataTypeId(Identifiers.Double);
            //                    // TODO integrate Property value
            //                    //if (val != null) {
            //                    //    myDoubleProperty.setValue(((DoubleValue)val).getValue());
            //                    //}
            //                    prop.addProperty(myDoubleProperty);
            //                    break;
            //
            //                case Float:
            //                    PlainProperty<Float> myFloatProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            //                    myFloatProperty.setDataTypeId(Identifiers.Float);
            //                    // TODO integrate Property value
            //                    //if (val != null) {
            //                    //    myFloatProperty.setValue(((FloatValue)val).getValue());
            //                    //}
            //                    prop.addProperty(myFloatProperty);
            //                    break;
            //
            //                case LocalizedText:
            //                    PlainProperty<LocalizedText> myLTProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            //                    myLTProperty.setDataTypeId(Identifiers.LocalizedText);
            //                    // TODO integrate Property value
            //                    myLTProperty.setValue(LocalizedText.english(stringVal));
            //                    //if (val != null) {
            //                    //    myLTProperty.setValue(((QNameValue)val).getValue().toString());
            //                    //}
            //                    prop.addProperty(myLTProperty);
            //                    break;
            //
            //                case String:
            //                    PlainProperty<String> myStringProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            //                    myStringProperty.setDataTypeId(Identifiers.String);
            //                    // TODO integrate Property value
            //                    myStringProperty.setValue(stringVal);
            //                    //if (val != null) {
            //                    //    myStringProperty.setValue(((StringValue)val).getValue());
            //                    //}
            //                    prop.addProperty(myStringProperty);
            //                    break;
            //
            //                case UtcTime:
            //                    PlainProperty<DateTime> myTimeProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            //                    myTimeProperty.setDataTypeId(Identifiers.UtcTime);
            //                    // TODO integrate Property value
            //                    //if (val != null) {
            //                    //    myTimeProperty.setValue(new DateTime(((TimeValue)val).getValue().toGregorianCalendar()));
            //                    //}
            //                    prop.addProperty(myTimeProperty);
            //                    break;
            //
            //                //                case Duration:
            //                //                    PlainProperty<String> myDurProperty = new PlainProperty<>(this, myPropertyId, browseName, displayName);
            //                //                    myDurProperty.setDataTypeId(Identifiers.String);
            //                //                    if (val != null) {
            //                //                        myDurProperty.setValue(((DurationValue)val).getValue().toString());
            //                //                    }
            //                //                    prop.addProperty(myDurProperty);
            //                //                    break;
            //
            //                default:
            //                    logger.warn("setValueAndType: Property " + prop.getBrowseName().getName() + ": Unknown type: " + aasProperty.getValueType());
            //                    break;
            //            }
            if (prop.getValueNode() != null) {
                if (prop.getValueNode().getDescription() == null) {
                    prop.getValueNode().setDescription(new LocalizedText("", ""));
                }
            }
        }
        catch (Throwable ex) {
            logger.error("setPropertyValueAndType Exception", ex);
        }
    }


    /**
     * Sets the value of a property.
     *
     * @param property The desired Property
     * @param value The new value.
     * @throws StatusException If the operation fails.
     */
    private void setPropertyValue(AASPropertyType property, PropertyValue value) throws StatusException {
        if (property == null) {
            throw new IllegalArgumentException("property is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        logger.debug("setPropertyValue: " + property.getBrowseName().getName() + " to " + value.getValue());

        try {
            switch (property.getValueType()) {
                case ByteString:
                    // TODO integrate Property value
                    property.setValue(value.getValue());
                    break;

                case Boolean:
                    // TODO integrate Property value
                    property.setValue(value.getValue());
                    break;

                case DateTime:
                    // TODO integrate Property value
                    property.setValue(value.getValue());
                    break;

                case Int32:
                    // TODO integrate Property value
                    property.setValue(value.getValue());
                    break;

                case UInt32:
                    // TODO integrate Property value
                    property.setValue(value.getValue());
                    break;

                case Int64:
                    // TODO integrate Property value
                    property.setValue(value.getValue());
                    break;

                case UInt64:
                    // TODO integrate Property value
                    property.setValue(value.getValue());
                    break;

                case Int16:
                    // TODO integrate Property value
                    property.setValue(value.getValue());
                    break;

                case UInt16:
                    // TODO integrate Property value
                    property.setValue(value.getValue());
                    break;

                case Byte:
                    // TODO integrate Property value
                    property.setValue(value.getValue());
                    break;

                case SByte:
                    // TODO integrate Property value
                    property.setValue(value.getValue());
                    break;

                case Double:
                    // TODO integrate Property value
                    property.setValue(value.getValue());
                    break;

                case Float:
                    // TODO integrate Property value
                    property.setValue(value.getValue());
                    break;

                case LocalizedText:
                    // TODO integrate Property value
                    property.setValue(value.getValue());
                    break;

                case String:
                    // TODO integrate Property value
                    property.setValue(value.getValue());
                    break;

                case UtcTime:
                    // TODO integrate Property value
                    property.setValue(value.getValue());
                    break;

                default:
                    logger.warn("setPropertyValue: Property " + property.getBrowseName().getName() + ": Unknown type: " + property.getValueType());
                    break;
            }
        }
        catch (Throwable ex) {
            logger.error("setPropertyValue Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS Blob to the given UA node
     *
     * @param node The desired UA node
     * @param aasBlob The AAS blob to add
     * @param submodel The corresponding Submodel as parent object of the data
     *            element
     * @param ordered Specifies whether the blob should be added ordered (true)
     *            or unordered (false)
     * @throws StatusException If the operation fails
     */
    private void addAasBlob(UaNode node, Blob aasBlob, Submodel submodel, boolean ordered) throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        try {
            if ((node != null) && (aasBlob != null)) {
                String name = aasBlob.getIdShort();
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASBlobType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(node, browseName);
                AASBlobType blobNode = createInstance(AASBlobType.class, nid, browseName, LocalizedText.english(name));
                addSubmodelElementBaseData(blobNode, aasBlob, name);

                // MimeType
                blobNode.setMimeType(aasBlob.getMimeType());

                // Value
                if (aasBlob.getValue() != null) {
                    if (blobNode.getValueNode() == null) {
                        addBlobValueNode(blobNode);
                    }

                    try {
                        submodelElementAasMapLock.lock();
                        submodelElementAasMap.put(blobNode.getValueNode().getNodeId(), new SubmodelElementData(aasBlob, submodel, SubmodelElementData.Type.BLOB_VALUE));
                        logger.debug("addAasBlob: NodeId " + blobNode.getValueNode().getNodeId() + "; Blob: " + aasBlob);
                    }
                    catch (Exception ex2) {
                        logger.warn("submodelElementAasMap problem", ex2);
                    }
                    finally {
                        submodelElementAasMapLock.unlock();
                    }

                    try {
                        Reference blobRef = getReference(aasBlob, submodel);
                        submodelElementOpcUAMapLock.lock();
                        submodelElementOpcUAMap.put(blobRef, blobNode);
                    }
                    catch (Exception ex3) {
                        logger.warn("submodelElementOpcUAMap problem", ex3);
                    }
                    finally {
                        submodelElementOpcUAMapLock.unlock();
                    }

                    blobNode.setValue(ByteString.valueOf(aasBlob.getValue()));
                    //blobNode.getFileNode().setMimeType(aasBlob.getMimeType().getValue());
                    //blobNode.getFileNode().setSize(UnsignedLong.valueOf(aasBlob.getValue().getValue().length));
                }

                if (ordered) {
                    node.addReference(blobNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(blobNode);
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addAasBlob Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds a Value Property to the given Blob Node.
     *
     * @param node The desired Blob Node
     */
    private void addBlobValueNode(UaNode node) {
        try {
            NodeId myPropertyId = new NodeId(getNamespaceIndex(), node.getNodeId().getValue().toString() + "." + AASBlobType.VALUE);
            PlainProperty<ByteString> myProperty = new PlainProperty<>(this, myPropertyId,
                    UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASBlobType.getNamespaceUri(), AASBlobType.VALUE).toQualifiedName(getNamespaceTable()),
                    LocalizedText.english(AASBlobType.VALUE));
            myProperty.setDataTypeId(Identifiers.ByteString);
            //if (VALUES_READ_ONLY) {
            //    myProperty.setAccessLevel(AccessLevelType.CurrentRead);
            //}
            myProperty.setDescription(new LocalizedText("", ""));
            node.addProperty(myProperty);
        }
        catch (Throwable ex) {
            logger.error("addBlobValueNode Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS reference element to the given UA node
     *
     * @param node The desired UA node
     * @param aasRefElem The AAS reference element to add
     * @param submodel The corresponding Submodel as parent object of the data
     *            element
     * @param ordered Specifies whether the reference element should be added
     *            ordered (true) or unordered (false)
     * @throws StatusException If the operation fails
     */
    private void addAasReferenceElement(UaNode node, ReferenceElement aasRefElem, Submodel submodel, boolean ordered)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        try {
            if ((node != null) && (aasRefElem != null)) {
                String name = aasRefElem.getIdShort();
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASReferenceElementType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(node, browseName);
                AASReferenceElementType refElemNode = createInstance(AASReferenceElementType.class, nid, browseName, LocalizedText.english(name));
                addSubmodelElementBaseData(refElemNode, aasRefElem, name);

                if (aasRefElem.getValue() != null) {
                    setAasReferenceData(aasRefElem.getValue(), refElemNode.getValueNode(), false);
                }

                try {
                    submodelElementAasMapLock.lock();
                    submodelElementAasMap.put(refElemNode.getValueNode().getKeysNode().getNodeId(),
                            new SubmodelElementData(aasRefElem, submodel, SubmodelElementData.Type.REFERENCE_ELEMENT_VALUE));
                    //logger.debug("addAasMultiLanguageProperty: NodeId " + refElemNode.getValueNode().getNodeId() + "; ReferenceElement: " + aasRefElem);
                }
                catch (Exception ex2) {
                    logger.warn("submodelElementAasMap problem", ex2);
                }
                finally {
                    submodelElementAasMapLock.unlock();
                }

                try {
                    Reference blobRef = getReference(aasRefElem, submodel);
                    submodelElementOpcUAMapLock.lock();
                    submodelElementOpcUAMap.put(blobRef, refElemNode);
                }
                catch (Exception ex3) {
                    logger.warn("submodelElementOpcUAMap problem", ex3);
                }
                finally {
                    submodelElementOpcUAMapLock.unlock();
                }

                if (ordered) {
                    node.addReference(refElemNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(refElemNode);
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addAasReferenceElement Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS range object to the given UA node
     *
     * @param node The desired UA node
     * @param aasRange The corresponding AAS range object to add
     * @param submodel The corresponding Submodel as parent object of the data
     *            element
     * @param ordered Specifies whether the range should be added ordered (true)
     *            or unordered (false)
     * @throws StatusException If the operation fails
     */
    private void addAasRange(UaNode node, Range aasRange, Submodel submodel, boolean ordered)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        try {
            if ((node != null) && (aasRange != null)) {
                String name = aasRange.getIdShort();
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASRangeType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(node, browseName);
                AASRangeType rangeNode = createInstance(AASRangeType.class, nid, browseName, LocalizedText.english(name));
                addSubmodelElementBaseData(rangeNode, aasRange, name);

                setRangeValueAndType(aasRange, rangeNode, submodel);

                if (ordered) {
                    node.addReference(rangeNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(rangeNode);
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addAasRange Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the min and max properties to the UA range object and sets the
     * values
     *
     * @param aasRange The AAS range object
     * @param range The corresponding UA range object
     * @param submodel The corresponding submodel
     */
    private void setRangeValueAndType(Range aasRange, AASRangeType range, Submodel submodel) {
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

            try {
                submodelElementAasMapLock.lock();
                submodelElementAasMap.put(myPropertyIdMin, new SubmodelElementData(aasRange, submodel, SubmodelElementData.Type.RANGE_MIN));
                submodelElementAasMap.put(myPropertyIdMax, new SubmodelElementData(aasRange, submodel, SubmodelElementData.Type.RANGE_MAX));
            }
            catch (Exception ex2) {
                logger.warn("submodelElementAasMap problem", ex2);
            }
            finally {
                submodelElementAasMapLock.unlock();
            }

            try {
                Reference propRef = getReference(aasRange, submodel);
                submodelElementOpcUAMapLock.lock();
                submodelElementOpcUAMap.put(propRef, range);
            }
            catch (Exception ex3) {
                logger.warn("submodelElementOpcUAMap problem", ex3);
            }
            finally {
                submodelElementOpcUAMapLock.unlock();
            }

            AASValueTypeDataType valueDataType = ValueConverter.stringToValueType(valueType);
            range.setValueType(valueDataType);

            // temporary solution for the "Value is String" problem!
            if (minValue != null) {
                PlainProperty<String> myStringProperty = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
                myStringProperty.setDataTypeId(Identifiers.String);
                myStringProperty.setValue(minValue);
                myStringProperty.setDescription(new LocalizedText("", ""));
                range.addProperty(myStringProperty);
            }

            if (maxValue != null) {
                PlainProperty<String> myStringProperty = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
                myStringProperty.setDataTypeId(Identifiers.String);
                myStringProperty.setValue(maxValue);
                myStringProperty.setDescription(new LocalizedText("", ""));
                range.addProperty(myStringProperty);
            }

            //            switch (valueDataType) {
            //                //                case AnyURI:
            //                //                    if (minVal != null) {
            //                //                        PlainProperty<String> myUriProperty = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                //                        myUriProperty.setDataTypeId(Identifiers.String);
            //                //                        myUriProperty.setValue(((AnyUri)minVal).getValue().toString());
            //                //                        myUriProperty.setDescription(new LocalizedText("", ""));
            //                //                        range.addProperty(myUriProperty);
            //                //                    }
            //                //
            //                //                    if (maxVal != null) {
            //                //                        PlainProperty<String> myUriProperty = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                //                        myUriProperty.setDataTypeId(Identifiers.String);
            //                //                        myUriProperty.setValue(((AnyUri)maxVal).getValue().toString());
            //                //                        myUriProperty.setDescription(new LocalizedText("", ""));
            //                //                        range.addProperty(myUriProperty);
            //                //                    }
            //                //                    break;
            //
            //                case ByteString:
            //                    if (minValue != null) {
            //                        PlainProperty<ByteString> myBSProperty = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                        myBSProperty.setDataTypeId(Identifiers.ByteString);
            //                        // TODO integrate Range value
            //                        //myBSProperty.setValue(((Base64Binary)minVal).getValue());
            //                        myBSProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myBSProperty);
            //                    }
            //
            //                    if (maxValue != null) {
            //                        PlainProperty<ByteString> myBSProperty = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                        myBSProperty.setDataTypeId(Identifiers.ByteString);
            //                        // TODO integrate Range value
            //                        //myBSProperty.setValue(((Base64Binary)maxVal).getValue());
            //                        myBSProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myBSProperty);
            //                    }
            //                    break;
            //
            //                case Boolean:
            //                    if (minValue != null) {
            //                        PlainProperty<Boolean> myBoolProperty = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                        myBoolProperty.setDataTypeId(Identifiers.Boolean);
            //                        // TODO integrate Range value
            //                        //myBoolProperty.setValue(((BooleanValue)minVal).getValue());
            //                        myBoolProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myBoolProperty);
            //                    }
            //
            //                    if (maxValue != null) {
            //                        PlainProperty<Boolean> myBoolProperty = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                        myBoolProperty.setDataTypeId(Identifiers.Boolean);
            //                        // TODO integrate Range value
            //                        //myBoolProperty.setValue(((BooleanValue)maxVal).getValue());
            //                        myBoolProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myBoolProperty);
            //                    }
            //                    break;
            //
            //                case DateTime:
            //                    if (minValue != null) {
            //                        PlainProperty<DateTime> myDateProperty = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                        myDateProperty.setDataTypeId(Identifiers.DateTime);
            //                        // TODO integrate Range value
            //                        //myDateProperty.setValue(new DateTime(((DateValue)minVal).getValue().toGregorianCalendar()));
            //                        myDateProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myDateProperty);
            //                    }
            //
            //                    if (maxValue != null) {
            //                        PlainProperty<DateTime> myDateProperty = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                        myDateProperty.setDataTypeId(Identifiers.DateTime);
            //                        // TODO integrate Range value
            //                        //myDateProperty.setValue(new DateTime(((DateValue)maxVal).getValue().toGregorianCalendar()));
            //                        myDateProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myDateProperty);
            //                    }
            //                    break;
            //
            //                //                case Decimal:
            //                //                    if (minVal != null) {
            //                //                        PlainProperty<Long> myDecProperty = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                //                        myDecProperty.setDataTypeId(Identifiers.Int64);
            //                //                        myDecProperty.setValue(((DecimalValue)minVal).getValue().longValue());
            //                //                        myDecProperty.setDescription(new LocalizedText("", ""));
            //                //                        range.addProperty(myDecProperty);
            //                //                    }
            //                //                    
            //                //                    if (maxVal != null) {
            //                //                        PlainProperty<Long> myDecProperty = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                //                        myDecProperty.setDataTypeId(Identifiers.Int64);
            //                //                        myDecProperty.setValue(((DecimalValue)maxVal).getValue().longValue());
            //                //                        myDecProperty.setDescription(new LocalizedText("", ""));
            //                //                        range.addProperty(myDecProperty);
            //                //                    }
            //                //                    break;
            //                //                case Integer:
            //                //                    if (minVal != null) {
            //                //                        PlainProperty<Integer> myIntegerProperty = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                //                        myIntegerProperty.setDataTypeId(Identifiers.Int64);
            //                //                        myIntegerProperty.setValue(((IntegerValue)minVal).getValue().longValue());
            //                //                        myIntegerProperty.setDescription(new LocalizedText("", ""));
            //                //                        range.addProperty(myIntegerProperty);
            //                //                    }
            //                //                    
            //                //                    if (maxVal != null) {
            //                //                        PlainProperty<Integer> myIntegerProperty = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                //                        myIntegerProperty.setDataTypeId(Identifiers.Int64);
            //                //                        myIntegerProperty.setValue(((IntegerValue)maxVal).getValue().longValue());
            //                //                        myIntegerProperty.setDescription(new LocalizedText("", ""));
            //                //                        range.addProperty(myIntegerProperty);
            //                //                    }
            //                //                    break;
            //                case Int32:
            //                    if (minValue != null) {
            //                        PlainProperty<Integer> myIntProperty = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                        myIntProperty.setDataTypeId(Identifiers.Int32);
            //                        // TODO integrate Range value
            //                        //myIntProperty.setValue(((IntValue)minVal).getValue());
            //                        myIntProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myIntProperty);
            //                    }
            //
            //                    if (maxValue != null) {
            //                        PlainProperty<Integer> myIntProperty = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                        myIntProperty.setDataTypeId(Identifiers.Int32);
            //                        // TODO integrate Range value
            //                        //myIntProperty.setValue(((IntValue)maxVal).getValue());
            //                        myIntProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myIntProperty);
            //                    }
            //                    break;
            //
            //                case UInt32:
            //                    if (minValue != null) {
            //                        PlainProperty<UnsignedInteger> myUIntProperty = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                        myUIntProperty.setDataTypeId(Identifiers.UInt32);
            //                        // TODO integrate Range value
            //                        //myIntProperty.setValue(((IntValue)minVal).getValue());
            //                        myUIntProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myUIntProperty);
            //                    }
            //
            //                    if (maxValue != null) {
            //                        PlainProperty<UnsignedInteger> myUIntProperty = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                        myUIntProperty.setDataTypeId(Identifiers.UInt32);
            //                        // TODO integrate Range value
            //                        //myIntProperty.setValue(((IntValue)maxVal).getValue());
            //                        myUIntProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myUIntProperty);
            //                    }
            //                    break;
            //
            //                case Int64:
            //                    if (minValue != null) {
            //                        PlainProperty<Long> myLongProperty = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                        myLongProperty.setDataTypeId(Identifiers.Int64);
            //                        // TODO integrate Range value
            //                        //myLongProperty.setValue(((LongValue)minVal).getValue());
            //                        myLongProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myLongProperty);
            //                    }
            //
            //                    if (maxValue != null) {
            //                        PlainProperty<Long> myLongProperty = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                        myLongProperty.setDataTypeId(Identifiers.Int64);
            //                        // TODO integrate Range value
            //                        //myLongProperty.setValue(((LongValue)maxVal).getValue());
            //                        myLongProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myLongProperty);
            //                    }
            //                    break;
            //
            //                case UInt64:
            //                    if (minValue != null) {
            //                        PlainProperty<UnsignedLong> myULongProperty = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                        myULongProperty.setDataTypeId(Identifiers.UInt64);
            //                        // TODO integrate Range value
            //                        //myLongProperty.setValue(((LongValue)minVal).getValue());
            //                        myULongProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myULongProperty);
            //                    }
            //
            //                    if (maxValue != null) {
            //                        PlainProperty<UnsignedLong> myULongProperty = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                        myULongProperty.setDataTypeId(Identifiers.UInt64);
            //                        // TODO integrate Range value
            //                        //myLongProperty.setValue(((LongValue)maxVal).getValue());
            //                        myULongProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myULongProperty);
            //                    }
            //                    break;
            //
            //                case Int16:
            //                    if (minValue != null) {
            //                        PlainProperty<Short> myInt16Property = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                        myInt16Property.setDataTypeId(Identifiers.Int16);
            //                        // TODO integrate Range value
            //                        //myInt16Property.setValue(((ShortValue)minVal).getValue());
            //                        myInt16Property.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myInt16Property);
            //                    }
            //
            //                    if (maxValue != null) {
            //                        PlainProperty<Short> myInt16Property = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                        myInt16Property.setDataTypeId(Identifiers.Int16);
            //                        // TODO integrate Range value
            //                        //myInt16Property.setValue(((ShortValue)maxVal).getValue());
            //                        myInt16Property.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myInt16Property);
            //                    }
            //                    break;
            //
            //                case UInt16:
            //                    if (minValue != null) {
            //                        PlainProperty<UnsignedShort> myUInt16Property = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                        myUInt16Property.setDataTypeId(Identifiers.UInt16);
            //                        // TODO integrate Range value
            //                        //myInt16Property.setValue(((ShortValue)minVal).getValue());
            //                        myUInt16Property.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myUInt16Property);
            //                    }
            //
            //                    if (maxValue != null) {
            //                        PlainProperty<UnsignedShort> myUInt16Property = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                        myUInt16Property.setDataTypeId(Identifiers.UInt16);
            //                        // TODO integrate Range value
            //                        //myInt16Property.setValue(((ShortValue)maxVal).getValue());
            //                        myUInt16Property.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myUInt16Property);
            //                    }
            //                    break;
            //
            //                case SByte:
            //                    if (minValue != null) {
            //                        PlainProperty<Byte> mySByteProperty = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                        mySByteProperty.setDataTypeId(Identifiers.SByte);
            //                        // TODO integrate Range value
            //                        //myByteProperty.setValue(((ByteValue)minVal).getValue());
            //                        mySByteProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(mySByteProperty);
            //                    }
            //
            //                    if (maxValue != null) {
            //                        PlainProperty<Byte> mySByteProperty = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                        mySByteProperty.setDataTypeId(Identifiers.SByte);
            //                        // TODO integrate Range value
            //                        //myByteProperty.setValue(((ByteValue)maxVal).getValue());
            //                        mySByteProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(mySByteProperty);
            //                    }
            //                    break;
            //
            //                case Byte:
            //                    if (minValue != null) {
            //                        PlainProperty<UnsignedByte> myByteProperty = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                        myByteProperty.setDataTypeId(Identifiers.Byte);
            //                        // TODO integrate Range value
            //                        //myByteProperty.setValue(((ByteValue)minVal).getValue());
            //                        myByteProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myByteProperty);
            //                    }
            //
            //                    if (maxValue != null) {
            //                        PlainProperty<UnsignedByte> myByteProperty = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                        myByteProperty.setDataTypeId(Identifiers.Byte);
            //                        // TODO integrate Range value
            //                        //myByteProperty.setValue(((ByteValue)maxVal).getValue());
            //                        myByteProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myByteProperty);
            //                    }
            //                    break;
            //
            //                case Double:
            //                    if (minValue != null) {
            //                        PlainProperty<Double> myDoubleProperty = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                        myDoubleProperty.setDataTypeId(Identifiers.Double);
            //                        // TODO integrate Range value
            //                        //myDoubleProperty.setValue(((DoubleValue)minVal).getValue());
            //                        myDoubleProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myDoubleProperty);
            //                    }
            //
            //                    if (maxValue != null) {
            //                        PlainProperty<Double> myDoubleProperty = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                        myDoubleProperty.setDataTypeId(Identifiers.Double);
            //                        // TODO integrate Range value
            //                        //myDoubleProperty.setValue(((DoubleValue)maxVal).getValue());
            //                        myDoubleProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myDoubleProperty);
            //                    }
            //                    break;
            //
            //                case Float:
            //                    if (minValue != null) {
            //                        PlainProperty<Float> myFloatProperty = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                        myFloatProperty.setDataTypeId(Identifiers.Float);
            //                        // TODO integrate Range value
            //                        //myFloatProperty.setValue(((FloatValue)minVal).getValue());
            //                        myFloatProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myFloatProperty);
            //                    }
            //
            //                    if (maxValue != null) {
            //                        PlainProperty<Float> myFloatProperty = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                        myFloatProperty.setDataTypeId(Identifiers.Float);
            //                        // TODO integrate Range value
            //                        //myFloatProperty.setValue(((FloatValue)maxVal).getValue());
            //                        myFloatProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myFloatProperty);
            //                    }
            //                    break;
            //
            //                case LocalizedText:
            //                    if (minValue != null) {
            //                        PlainProperty<LocalizedText> myLTProperty = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                        myLTProperty.setDataTypeId(Identifiers.String);
            //                        // TODO integrate Range value
            //                        //myLTProperty.setValue(((QNameValue)minVal).getValue().toString());
            //                        myLTProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myLTProperty);
            //                    }
            //
            //                    if (maxValue != null) {
            //                        PlainProperty<LocalizedText> myLTProperty = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                        myLTProperty.setDataTypeId(Identifiers.LocalizedText);
            //                        // TODO integrate Range value
            //                        //myQNameProperty.setValue(((QNameValue)maxVal).getValue().toString());
            //                        myLTProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myLTProperty);
            //                    }
            //                    break;
            //
            //                case String:
            //                    if (minValue != null) {
            //                        PlainProperty<String> myStringProperty = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                        myStringProperty.setDataTypeId(Identifiers.String);
            //                        myStringProperty.setValue(minValue);
            //                        myStringProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myStringProperty);
            //                    }
            //
            //                    if (maxValue != null) {
            //                        PlainProperty<String> myStringProperty = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                        myStringProperty.setDataTypeId(Identifiers.String);
            //                        myStringProperty.setValue(maxValue);
            //                        myStringProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myStringProperty);
            //                    }
            //                    break;
            //
            //                case UtcTime:
            //                    if (minValue != null) {
            //                        PlainProperty<DateTime> myTimeProperty = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                        myTimeProperty.setDataTypeId(Identifiers.DateTime);
            //                        // TODO integrate Range value
            //                        //myTimeProperty.setValue(new DateTime(((TimeValue)minVal).getValue().toGregorianCalendar()));
            //                        myTimeProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myTimeProperty);
            //                    }
            //
            //                    if (maxValue != null) {
            //                        PlainProperty<DateTime> myTimeProperty = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                        myTimeProperty.setDataTypeId(Identifiers.DateTime);
            //                        // TODO integrate Range value
            //                        //myTimeProperty.setValue(new DateTime(((TimeValue)maxVal).getValue().toGregorianCalendar()));
            //                        myTimeProperty.setDescription(new LocalizedText("", ""));
            //                        range.addProperty(myTimeProperty);
            //                    }
            //                    break;
            //
            //                //                case Duration:
            //                //                    if (minVal != null) {
            //                //                        PlainProperty<String> myDurProperty = new PlainProperty<>(this, myPropertyIdMin, browseNameMin, displayNameMin);
            //                //                        myDurProperty.setDataTypeId(Identifiers.String);
            //                //                        myDurProperty.setValue(((DurationValue)minVal).getValue().toString());
            //                //                        myDurProperty.setDescription(new LocalizedText("", ""));
            //                //                        range.addProperty(myDurProperty);
            //                //                    }
            //                //                    
            //                //                    if (maxVal != null) {
            //                //                        PlainProperty<String> myDurProperty = new PlainProperty<>(this, myPropertyIdMax, browseNameMax, displayNameMax);
            //                //                        myDurProperty.setDataTypeId(Identifiers.String);
            //                //                        myDurProperty.setValue(((DurationValue)maxVal).getValue().toString());
            //                //                        myDurProperty.setDescription(new LocalizedText("", ""));
            //                //                        range.addProperty(myDurProperty);
            //                //                    }
            //                //                    break;
            //                default:
            //                    logger.warn("setRangeValueAndType: Range " + range.getBrowseName().getName() + ": Unknown type: " + valueType);
            //                    break;
            //            }
        }
        catch (Throwable ex) {
            logger.error("setRangeValueAndType Exception", ex);
        }
    }


    /**
     * Adds an AAS Multi Language Property to the given UA node
     *
     * @param node The desired UA node
     * @param aasMultiLang The AAS Multi Language Property to add
     * @param submodel The corresponding Submodel as parent object of the data
     *            element
     * @param ordered Specifies whether the multi language property should be
     *            added ordered (true) or unordered (false)
     * @throws StatusException If the operation fails
     */
    private void addAasMultiLanguageProperty(UaNode node, MultiLanguageProperty aasMultiLang, Submodel submodel, boolean ordered)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        try {
            if ((node != null) && (aasMultiLang != null)) {
                String name = aasMultiLang.getIdShort();
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASMultiLanguagePropertyType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(node, browseName);
                AASMultiLanguagePropertyType multiLangNode = createInstance(AASMultiLanguagePropertyType.class, nid, browseName, LocalizedText.english(name));
                addSubmodelElementBaseData(multiLangNode, aasMultiLang, name);

                List<LangString> values = aasMultiLang.getValues();
                if (values != null) {
                    if (multiLangNode.getValueNode() == null) {
                        addMultiLanguageValueNode(multiLangNode, values.size());
                    }

                    multiLangNode.getValueNode().setValue(ValueConverter.getLocalizedTextFromLangStringSet(values));
                }

                if (aasMultiLang.getValueId() != null) {
                    addAasReferenceAasNS(multiLangNode, aasMultiLang.getValueId(), AASMultiLanguagePropertyType.VALUE_ID);
                }

                try {
                    submodelElementAasMapLock.lock();
                    submodelElementAasMap.put(multiLangNode.getValueNode().getNodeId(),
                            new SubmodelElementData(aasMultiLang, submodel, SubmodelElementData.Type.MULTI_LANGUAGE_VALUE));
                    //logger.debug("addAasMultiLanguageProperty: NodeId " + multiLangNode.getValueNode().getNodeId() + "; Blob: " + aasMultiLang);
                }
                catch (Exception ex2) {
                    logger.warn("submodelElementAasMap problem", ex2);
                }
                finally {
                    submodelElementAasMapLock.unlock();
                }

                try {
                    Reference blobRef = getReference(aasMultiLang, submodel);
                    submodelElementOpcUAMapLock.lock();
                    submodelElementOpcUAMap.put(blobRef, multiLangNode);
                }
                catch (Exception ex3) {
                    logger.warn("submodelElementOpcUAMap problem", ex3);
                }
                finally {
                    submodelElementOpcUAMapLock.unlock();
                }

                if (ordered) {
                    node.addReference(multiLangNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(multiLangNode);
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addAasMultiLanguageProperty Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds the Value Node for the MultiLanguageProperty
     *
     * @param node The desired MultiLanguageProperty Node
     * @param arraySize The desired Array Size.
     */
    private void addMultiLanguageValueNode(UaNode node, int arraySize) {
        try {
            NodeId propId = new NodeId(getNamespaceIndex(), node.getNodeId().getValue().toString() + "." + AASMultiLanguagePropertyType.VALUE);
            PlainProperty<LocalizedText[]> myLTProperty = new PlainProperty<>(this, propId,
                    UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASMultiLanguagePropertyType.getNamespaceUri(), AASMultiLanguagePropertyType.VALUE)
                            .toQualifiedName(getNamespaceTable()),
                    LocalizedText.english(AASMultiLanguagePropertyType.VALUE));
            myLTProperty.setDataTypeId(Identifiers.LocalizedText);
            myLTProperty.setValueRank(ValueRanks.OneDimension);
            myLTProperty.setArrayDimensions(new UnsignedInteger[] {
                    UnsignedInteger.valueOf(arraySize)
            });
            node.addProperty(myLTProperty);
            //myLTProperty.setCurrentValue(getLocalizedTextFromLangStringSet(values));
            myLTProperty.setDescription(new LocalizedText("", ""));
        }
        catch (Throwable ex) {
            logger.error("addMultiLanguageValueNode Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS Capability to the given UA node
     *
     * @param node The desired UA node
     * @param aasCapability The corresponding AAS Capability to add
     * @param ordered Specifies whether the capability should be added ordered
     *            (true) or unordered (false)
     * @throws StatusException If the operation fails
     */
    private void addAasCapability(UaNode node, Capability aasCapability, boolean ordered) throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        try {
            if ((node != null) && (aasCapability != null)) {
                String name = aasCapability.getIdShort();
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASCapabilityType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(node, browseName);
                AASCapabilityType capabilityNode = createInstance(AASCapabilityType.class, nid, browseName, LocalizedText.english(name));
                addSubmodelElementBaseData(capabilityNode, aasCapability, name);

                if (ordered) {
                    node.addReference(capabilityNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(capabilityNode);
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addAasCapability Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS entity to the given UA node
     *
     * @param node The desired UA node
     * @param aasEntity The AAS entity to add
     * @param ordered Specifies whether the entity should be added ordered
     *            (true) or unordered (false)
     * @throws StatusException If the operation fails
     */
    private void addAasEntity(UaNode node, Entity aasEntity, boolean ordered) throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        try {
            if ((node != null) && (aasEntity != null)) {
                String name = aasEntity.getIdShort();
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASEntityType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(node, browseName);
                AASEntityType entityNode = createInstance(AASEntityType.class, nid, browseName, LocalizedText.english(name));
                addSubmodelElementBaseData(entityNode, aasEntity, name);

                // EntityType
                entityNode.setEntityType(convertEntityType(aasEntity.getEntityType()));

                // GlobalAssetId
                if (aasEntity.getGlobalAssetId() != null) {
                    if (entityNode.getGlobalAssetIdNode() == null) {
                        addAasReferenceAasNS(entityNode, aasEntity.getGlobalAssetId(), AASEntityType.GLOBAL_ASSET_ID);
                    }
                    else {
                        setAasReferenceData(aasEntity.getGlobalAssetId(), entityNode.getGlobalAssetIdNode());
                    }
                }

                // SpecificAssetIds
                IdentifierKeyValuePair specificAssetId = aasEntity.getSpecificAssetId();
                if (specificAssetId != null) {
                    addIdentifierKeyValuePair(entityNode, specificAssetId);
                }

                // Statements
                addSubmodelElements(entityNode, aasEntity.getStatements(), null);

                if (VALUES_READ_ONLY) {
                    entityNode.getEntityTypeNode().setAccessLevel(AccessLevelType.CurrentRead);
                }

                if (ordered) {
                    node.addReference(entityNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(entityNode);
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addAasEntity Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AASOperation to the given node
     *
     * @param node The desired UA node
     * @param aasOperation The corresponding AAS operation to add
     * @param submodel The corresponding Submodel as parent object of the data
     *            element
     * @param ordered Specifies whether the operation should be added ordered
     *            (true) or unordered (false)
     */
    private void addAasOperation(UaNode node, Operation aasOperation, Submodel submodel, boolean ordered) {
        try {
            String name = aasOperation.getIdShort();
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASOperationType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
            NodeId nid = createNodeId(node, browseName);
            AASOperationType oper = createInstance(AASOperationType.class, nid, browseName, LocalizedText.english(name));
            addSubmodelElementBaseData(oper, aasOperation, name);

            try {
                // for operations we put the corresponding operation object into the map
                submodelElementAasMapLock.lock();
                submodelElementAasMap.put(nid, new SubmodelElementData(aasOperation, submodel, SubmodelElementData.Type.OPERATION));
                logger.debug("addAasOperation: NodeId " + nid + "; Property: " + aasOperation);
            }
            catch (Exception ex2) {
                logger.warn("submodelElementAasMap problem", ex2);
            }
            finally {
                submodelElementAasMapLock.unlock();
            }

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
        }
        catch (Throwable ex) {
            logger.error("addAasOperation Exception", ex);
        }
    }


    /**
     * Sets the arguments for the given Operation Variable
     *
     * @param arg The UA argument
     * @param var The corresponding Operation Variable
     */
    private void setOperationArgument(Argument arg, OperationVariable var) {
        try {
            if (var.getValue() instanceof Property) {
                Property prop = (Property) var.getValue();
                arg.setName(prop.getIdShort());
                arg.setValueRank(ValueRanks.Scalar);
                arg.setArrayDimensions(null);

                // Description
                addDescriptions(arg, prop.getDescriptions());

                NodeId type = ValueConverter.convertValueTypeStringToNodeId(prop.getValueType());
                if (type.isNullNodeId()) {
                    logger.warn("setOperationArgument: Property " + prop.getIdShort() + ": Unknown type: " + prop.getValueType());

                    // Default type is String. That's what we receive from the AAS Service
                    arg.setDataType(Identifiers.String);
                }
                else {
                    arg.setDataType(type);
                }
            }
            else {
                logger.warn("setOperationArgument: unknown Argument type");
            }
        }
        catch (Throwable ex) {
            logger.error("setOperationArgument Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS Event to the given UA node
     *
     * @param node The desired UA node
     * @param aasEvent The AAS Event to add
     * @param ordered Specifies whether the entity should be added ordered
     *            (true) or unordered (false)
     * @throws StatusException If the operation fails
     */
    private void addAasEvent(UaNode node, Event aasEvent, boolean ordered) throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        try {
            if ((node != null) && (aasEvent != null)) {
                String name = aasEvent.getIdShort();
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASEventType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(node, browseName);
                AASEventType eventNode = createInstance(AASEventType.class, nid, browseName, LocalizedText.english(name));
                addSubmodelElementBaseData(eventNode, aasEvent, name);

                if (aasEvent instanceof BasicEvent) {
                    setBasicEventData(eventNode, (BasicEvent) aasEvent);
                }

                if (ordered) {
                    node.addReference(eventNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(eventNode);
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addAasEvent Exception", ex);
            throw ex;
        }
    }


    /**
     * Sets the Basic event data
     *
     * @param eventNode The desired UA event node
     * @param aasEvent The corresponding AAS BasicEvent
     */
    private void setBasicEventData(AASEventType eventNode, BasicEvent aasEvent) {
        try {
            if (aasEvent.getObserved() != null) {
                // TODO?
                logger.warn("setBasicEventData: not implemented! Event: " + eventNode.getBrowseName().getName());
            }
        }
        catch (Throwable ex) {
            logger.error("setBasicEventData Exception", ex);
            throw ex;
        }
    }


    /**
     * Adds an AAS Relationship Element to the given UA node
     *
     * @param node The desired UA node
     * @param aasRelElem The corresponding AAS Relationship Element
     * @param submodel The corresponding Submodel as parent object of the data
     *            element
     * @param ordered Specifies whether the entity should be added ordered
     *            (true) or unordered (false)
     * @throws StatusException If the operation fails
     */
    private void addAasRelationshipElement(UaNode node, RelationshipElement aasRelElem, Submodel submodel, boolean ordered)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        try {
            if ((node != null) && (aasRelElem != null)) {
                String name = aasRelElem.getIdShort();
                AASRelationshipElementType relElemNode;
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASRelationshipElementType.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(node, browseName);
                if (aasRelElem instanceof AnnotatedRelationshipElement) {
                    relElemNode = createAnnotatedRelationshipElement((AnnotatedRelationshipElement) aasRelElem, submodel, nid);
                }
                else {
                    relElemNode = createInstance(AASRelationshipElementType.class, nid, browseName, LocalizedText.english(name));
                }

                if (relElemNode != null) {
                    addSubmodelElementBaseData(relElemNode, aasRelElem, name);

                    setAasReferenceData(aasRelElem.getFirst(), relElemNode.getFirstNode());
                    setAasReferenceData(aasRelElem.getSecond(), relElemNode.getSecondNode());

                    if (ordered) {
                        node.addReference(relElemNode, Identifiers.HasOrderedComponent, false);
                    }
                    else {
                        node.addComponent(relElemNode);
                    }
                }
            }
        }
        catch (Throwable ex) {
            logger.error("addAasRelationshipElement Exception", ex);
            throw ex;
        }
    }


    /**
     * Creates an Annotated Relationship Element
     *
     * @param aasRelElem The AAS Annotated Relationship Element
     * @param submodel The corresponding Submodel as parent object of the data
     *            element
     * @param nodeId The desired NodeId for the node to be created
     * @return The create UA Annotated Relationship Element
     * @throws StatusException If the operation fails
     */
    private AASRelationshipElementType createAnnotatedRelationshipElement(AnnotatedRelationshipElement aasRelElem, Submodel submodel, NodeId nodeId)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        AASRelationshipElementType retval = null;

        try {
            AASAnnotatedRelationshipElementType relElemNode = createInstance(
                    AASAnnotatedRelationshipElementType.class, nodeId, UaQualifiedName
                            .from(opc.i4aas.ObjectTypeIds.AASAnnotatedRelationshipElementType.getNamespaceUri(), aasRelElem.getIdShort()).toQualifiedName(getNamespaceTable()),
                    LocalizedText.english(aasRelElem.getIdShort()));

            // Annotations 
            for (DataElement de: aasRelElem.getAnnotations()) {
                addAasDataElement(relElemNode.getAnnotationNode(), de, submodel, false);
            }

            retval = relElemNode;
        }
        catch (Throwable ex) {
            logger.error("createAnnotatedRelationshipElement Exception", ex);
            throw ex;
        }

        return retval;
    }


    /**
     * Adds a SubmodelElementCollection to the given UA node
     *
     * @param node The desired UA node
     * @param aasColl The corresponding SubmodelElementCollection to add
     * @param submodel The corresponding Submodel as parent object of the data
     *            element
     * @param ordered Specifies whether the entity should be added ordered
     *            (true) or unordered (false)
     * @throws StatusException If the operation fails
     */
    private void addAasSubmodelElementCollection(UaNode node, SubmodelElementCollection aasColl, Submodel submodel, boolean ordered)
            throws StatusException, ServiceException, AddressSpaceException, ServiceResultException {
        try {
            if ((node != null) && (aasColl != null)) {
                String name = aasColl.getIdShort();
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASSubmodelElementCollectionType.getNamespaceUri(), name)
                        .toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(node, browseName);
                AASSubmodelElementCollectionType collNode;
                if (aasColl.getOrdered()) {
                    collNode = createAasOrderedSubmodelElementCollection(name, nid);
                }
                else {
                    collNode = createInstance(AASSubmodelElementCollectionType.class, nid, browseName, LocalizedText.english(name));
                }

                addSubmodelElementBaseData(collNode, aasColl, name);

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

                // SubmodelElements 
                addSubmodelElements(collNode, aasColl.getValues(), submodel, aasColl.getOrdered());

                if (ordered) {
                    node.addReference(collNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(collNode);
                }
            }
        }
        catch (Throwable ex) {
            logger.error("createAasSubmodelElementCollection Exception", ex);
            throw ex;
        }
    }


    /**
     * Creates an AAS Ordered Submodel Element Collection
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
        catch (Throwable ex) {
            logger.error("createAasSubmodelElementCollection Exception", ex);
            throw ex;
        }

        return retval;
    }


    /**
     * Adds the given submodel references to the given node
     *
     * @param node The desired UA node in which the objects should be created
     * @param sumodelRefs The desired submodel references
     */
    private void addSubmodelReferences(AASAssetAdministrationShellType node, List<Reference> submodelRefs) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException("node = null");
        }
        else if (submodelRefs == null) {
            throw new IllegalArgumentException("sumodelRefs = null");
        }

        try {
            String name = "Submodel";
            AASReferenceList referenceListNode = node.getSubmodelNode();
            logger.info("addSubmodelReferences: add " + submodelRefs.size() + " Submodels to Node: " + node.toString());
            boolean added = false;
            if (referenceListNode == null) {
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASReferenceList.getNamespaceUri(), name).toQualifiedName(getNamespaceTable());
                NodeId nid = createNodeId(node, browseName);
                referenceListNode = createInstance(AASReferenceList.class, nid, browseName, LocalizedText.english(name));
                logger.info("addSubmodelReferences: add Node " + referenceListNode.getNodeId() + "to Node" + node.getNodeId());
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
                        logger.warn("addSubmodelReferences: Submodel " + ref + " not found in submodelRefMap");
                    }
                }
            }

            if (added) {
                node.addComponent(referenceListNode);
            }
        }
        catch (Throwable ex) {
            logger.error("addSubmodelReferences Exception", ex);
            throw ex;
        }
    }

    //    /**
    //     * Converts the given String value to KeyType
    //     *
    //     * @param stringValue The desired String
    //     * @return The KeyType
    //     */
    //    private static KeyType stringToKeyType(String stringValue) {
    //        KeyType retval;
    //        if (keyTypeStringMap.containsKey(stringValue.toUpperCase())) {
    //            retval = keyTypeStringMap.get(stringValue.toUpperCase());
    //        }
    //        else {
    //            logger.warn("stringToKeyType: unknown value: " + stringValue);
    //            retval = null;
    //        }
    //
    //        return retval;
    //    }

    //    /**
    //     * Returns a KeyType from an IdentifierType
    //     *
    //     * @param identifier The desired IdentifierType
    //     * @return The corresponding KeyType
    //     */
    //    private static KeyType getKeyTypeFromIdentifier(IdentifierType identifier) {
    //        KeyType retval = null;
    //
    //        try {
    //            retval = stringToKeyType(identifier.name());
    //            if (retval == null) {
    //                throw new IllegalArgumentException(identifier.name() + " not found in KeyType");
    //            }
    //        }
    //        catch (Throwable ex) {
    //            logger.error("getKeyTypeFromIdentifier Exception", ex);
    //            throw ex;
    //        }
    //
    //        return retval;
    //    }


    /**
     * Subscribes to Events on the MessageBus (e.g. ValueChangeEvents).
     */
    private void subscribeMessageBus() {
        try {
            if (messageBus != null) {
                logger.debug("subscribeMessageBus: subscribe ValueChangeEvents");
                SubscriptionInfo info = SubscriptionInfo.create(ValueChangeEventMessage.class, (t) -> {
                    try {
                        valueChanged(t.getElement(), t.getNewValue(), t.getOldValue());
                    }
                    catch (StatusException ex2) {
                        logger.error("valueChanged Exception", ex2);
                    }
                });
                SubscriptionId rv = messageBus.subscribe(info);
                subscriptions.add(rv);

                info = SubscriptionInfo.create(ElementCreateEventMessage.class, (x) -> {
                    try {
                        elementCreated(x.getElement());
                    }
                    catch (Exception ex3) {
                        logger.error("elementCreated Exception", ex3);
                    }
                });
                rv = messageBus.subscribe(info);
                subscriptions.add(rv);

                info = SubscriptionInfo.create(ElementDeleteEventMessage.class, (x) -> {
                    try {
                        elementDeleted(x.getElement());
                    }
                    catch (Exception ex3) {
                        logger.error("elementDeleted Exception", ex3);
                    }
                });
                rv = messageBus.subscribe(info);
                subscriptions.add(rv);
            }
            else {
                logger.warn("MessageBus not available!");
            }
        }
        catch (Throwable ex) {
            logger.error("subscribeMessageBus Exception", ex);
            throw ex;
        }
    }


    /**
     * Handles a ValueChanged event.
     *
     * @param element The Reference to the changed element
     * @param newValue The new value
     * @param oldValue The old value
     * @throws If the operation fails
     */
    private void valueChanged(Reference element, ElementValue newValue, ElementValue oldValue) throws StatusException {
        try {
            updateSubmodelElementValue(element, newValue, oldValue);
        }
        catch (Throwable ex) {
            logger.error("valueChanged Exception", ex);
            throw ex;
        }
    }


    /**
     * Handles an elementCreated event.
     *
     * @param element Reference to the created element.
     */
    private void elementCreated(Reference element) {
        if (element == null) {
            throw new IllegalArgumentException("element is null");
        }

        logger.info("elementCreated not implemented!");
        // TODO: implement
    }


    /**
     * Handles an elementDeleted event.
     *
     * @param element Reference to the created element.
     */
    private void elementDeleted(Reference element) {
        if (element == null) {
            throw new IllegalArgumentException("element is null");
        }

        logger.info("elementDeleted not implemented!");
        // TODO: implement
    }


    /**
     * Unsubscribes from the MessageBus.
     */
    private void unsubscribeMessageBus() {
        try {
            if (messageBus != null) {
                logger.info("unsubscribe from the MessageBus");
                for (int i = 0; i < subscriptions.size(); i++) {
                    messageBus.unsubscribe(subscriptions.get(i));
                }
            }
        }
        catch (Throwable ex) {
            logger.error("unsubscribeMessageBus Exception", ex);
            throw ex;
        }
        finally {
            subscriptions.clear();
        }
    }


    /**
     * Update the value of a SubmodelElement
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

        try {
            logger.debug("updateSubmodelElementValue");
            submodelElementOpcUAMapLock.lock();
            if (submodelElementOpcUAMap.containsKey(reference)) {
                AASSubmodelElementType subElem = submodelElementOpcUAMap.get(reference);
                setSubmodelElementValue(subElem, newValue);
            }
            else {
                logger.warn("SubmodelElement " + reference.toString() + " not found in submodelElementOpcUAMap");
            }
        }
        catch (Throwable ex) {
            logger.error("updateSubmodelElementValue Exception", ex);
            throw ex;
        }
        finally {
            submodelElementOpcUAMapLock.unlock();
        }
    }


    /**
     * Sets the value of the given SubmodelElement.
     *
     * @param subElem The desired SubmodelElement.
     * @param value The new value
     * @throws StatusException If the operation fails
     */
    private void setSubmodelElementValue(AASSubmodelElementType subElem, ElementValue value) throws StatusException {
        try {
            logger.debug("setSubmodelElementValue: " + subElem.getBrowseName().getName());
            if (value instanceof DataElementValue) {
                setDataElementValue(subElem, (DataElementValue) value);
            }
            else if ((value instanceof RelationshipElementValue) && (subElem instanceof AASRelationshipElementType)) {
                setRelationshipValue((AASRelationshipElementType) subElem, (RelationshipElementValue) value);
            }
            else if ((value instanceof EntityValue) && (subElem instanceof AASEntityType)) {
                setEntityValue((AASEntityType) subElem, (EntityValue) value);
            }
            else {
                logger.warn("SubmodelElement " + subElem.getBrowseName().getName() + " type not supported");
            }
        }
        catch (Throwable ex) {
            logger.error("setSubmodelElementValue Exception", ex);
            throw ex;
        }
    }


    /**
     * Sets the values for the given RelationshipElement.
     *
     * @param aasElement The desired RelationshipElement.
     * @param value The new value.
     * @throws StatusException If the operation fails
     */
    private void setRelationshipValue(AASRelationshipElementType aasElement, RelationshipElementValue value) throws StatusException {
        if (aasElement == null) {
            throw new IllegalArgumentException("aasElement is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        try {
            if ((aasElement instanceof AASAnnotatedRelationshipElementType) && (value instanceof AnnotatedRelationshipElementValue)) {
                AASAnnotatedRelationshipElementType annotatedElement = (AASAnnotatedRelationshipElementType) aasElement;
                AnnotatedRelationshipElementValue annotatedValue = (AnnotatedRelationshipElementValue) value;
                UaNode[] annotationNodes = annotatedElement.getAnnotationNode().getComponents();
                List<DataElementValue> valueList = annotatedValue.getAnnotation();
                if (annotationNodes.length != valueList.size()) {
                    logger.warn("Size of Value (" + valueList.size() + ") doesn't match the number of AnnotationNodes (" + annotationNodes.length + ")");
                    throw new IllegalArgumentException("Size of Value doesn't match the number of AnnotationNodes");
                }

                for (int i = 0; i < annotationNodes.length; i++) {
                    setDataElementValue(annotationNodes[i], valueList.get(i));
                }

                DefaultReference ref = new DefaultReference.Builder().keys(value.getFirst()).build();
                setAasReferenceData(ref, aasElement.getFirstNode());

                ref = new DefaultReference.Builder().keys(value.getSecond()).build();
                setAasReferenceData(ref, aasElement.getSecondNode());
            }
            else {
                logger.info("setRelationshipValue: No AnnotatedRelationshipElement " + aasElement.getBrowseName().getName());
            }

        }
        catch (Throwable ex) {
            logger.error("setAnnotatedRelationshipValue Exception", ex);
            throw ex;
        }
    }


    /**
     * Sets the values for the given DataElement.
     *
     * @param node The desired DataElement.
     * @param value The new value.
     * @throws StatusException If the operation fails
     */
    private void setDataElementValue(UaNode node, DataElementValue value) throws StatusException {
        if (node == null) {
            throw new IllegalArgumentException("node is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        try {
            if ((node instanceof AASPropertyType) && (value instanceof PropertyValue)) {
                setPropertyValue((AASPropertyType) node, (PropertyValue) value);
            }
            else if ((node instanceof AASFileType) && (value instanceof FileValue)) {
                setFileValue((AASFileType) node, (FileValue) value);
            }
            else if ((node instanceof AASBlobType) && (value instanceof BlobValue)) {
                setBlobValue((AASBlobType) node, (BlobValue) value);
            }
            else if ((node instanceof AASReferenceElementType) && (value instanceof ReferenceElementValue)) {
                setReferenceElementValue((AASReferenceElementType) node, (ReferenceElementValue) value);
            }
            else if ((node instanceof AASRangeType) && (value instanceof RangeValue)) {
                setRangeValue((AASRangeType) node, (RangeValue) value);
            }
            else if ((node instanceof AASMultiLanguagePropertyType) && (value instanceof MultiLanguagePropertyValue)) {
                setMultiLanguagePropertyValue((AASMultiLanguagePropertyType) node, (MultiLanguagePropertyValue) value);
            }
            else {
                logger.warn("addAasDataElement: unknown or invalid DataElement or value: " + node.getBrowseName().getName() + "; Class: " + node.getClass() + "; Value Class: "
                        + value.getClass());
            }
        }
        catch (Throwable ex) {
            logger.error("setDataElementValue Exception", ex);
            throw ex;
        }
    }


    /**
     * Sets the values for the given File.
     *
     * @param file The desired file.
     * @param value The new value
     * @throws StatusException If the operation fails
     */
    private void setFileValue(AASFileType file, FileValue value) throws StatusException {
        if (file == null) {
            throw new IllegalArgumentException("file is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        try {
            file.setMimeType(value.getMimeType());
            if (value.getValue() != null) {
                if (file.getValueNode() == null) {
                    addFileValueNode(file);
                }

                file.setValue(value.getValue());
            }
        }
        catch (Throwable ex) {
            logger.error("setFileValue Exception", ex);
            throw ex;
        }
    }


    /**
     * Sets the values for the given Blob.
     *
     * @param blob The desired blob.
     * @param value The new value
     * @throws StatusException If the operation fails
     */
    private void setBlobValue(AASBlobType blob, BlobValue value) throws StatusException {
        if (blob == null) {
            throw new IllegalArgumentException("blob is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        try {
            // MimeType
            blob.setMimeType(value.getMimeType());

            // Value
            if (value.getValue() != null) {
                if (blob.getValueNode() == null) {
                    addBlobValueNode(blob);
                }

                blob.setValue(ByteString.valueOf(value.getValue()));
            }
        }
        catch (Throwable ex) {
            logger.error("setBlobValue Exception", ex);
            throw ex;
        }
    }


    /**
     * Sets the value for the given ReferenceElement.
     *
     * @param refElement The desired ReferenceElement.
     * @param value The new value.
     * @throws StatusException If the operation fails
     */
    private void setReferenceElementValue(AASReferenceElementType refElement, ReferenceElementValue value) throws StatusException {
        if (refElement == null) {
            throw new IllegalArgumentException("refElement is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        try {
            DefaultReference ref = new DefaultReference.Builder().keys(value.getKeys()).build();
            setAasReferenceData(ref, refElement.getValueNode());
        }
        catch (Throwable ex) {
            logger.error("setReferenceElementValue Exception", ex);
            throw ex;
        }
    }


    /**
     * Sets the value for the given Range.
     *
     * @param range The desired Range.
     * @param value The new value
     * @throws StatusException If the operation fails
     */
    private void setRangeValue(AASRangeType range, RangeValue value) throws StatusException {
        if (range == null) {
            throw new IllegalArgumentException("range is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        try {
            range.setMin(((Double) value.getMin()).toString());
            range.setMax(((Double) value.getMax()).toString());
        }
        catch (Throwable ex) {
            logger.error("setRangeValue Exception", ex);
            throw ex;
        }
    }


    /**
     * Sets the values for the given MultiLanguageProperty.
     *
     * @param multiLangProp The desired MultiLanguageProperty.
     * @param value The new value
     * @throws StatusException If the operation fails
     */
    private void setMultiLanguagePropertyValue(AASMultiLanguagePropertyType multiLangProp, MultiLanguagePropertyValue value) throws StatusException {
        if (multiLangProp == null) {
            throw new IllegalArgumentException("multiLangProp is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        try {
            List<LangString> values = new ArrayList<>(value.getLangStringSet());
            if (multiLangProp.getValueNode() == null) {
                addMultiLanguageValueNode(multiLangProp, values.size());
            }

            multiLangProp.getValueNode().setValue(ValueConverter.getLocalizedTextFromLangStringSet(values));
        }
        catch (Throwable ex) {
            logger.error("setMultiLanguagePropertyValue Exception", ex);
            throw ex;
        }
    }


    /**
     * Sets the values for the given Entity.
     *
     * @param entity The desired Entity.
     * @param value The new value.
     * @throws StatusException If the operation fails
     */
    private void setEntityValue(AASEntityType entity, EntityValue value) throws StatusException {
        if (entity == null) {
            throw new IllegalArgumentException("entity is null");
        }
        else if (value == null) {
            throw new IllegalArgumentException("value is null");
        }

        try {
            // EntityType
            entity.setEntityType(convertEntityType(value.getEntityType()));

            // GlobalAssetId
            if (value.getGlobalAssetId() != null) {
                DefaultReference ref = new DefaultReference.Builder().keys(value.getGlobalAssetId()).build();
                setAasReferenceData(ref, entity.getGlobalAssetIdNode());
            }

            // Statements
            List<ElementValue> valueList = value.getStatements();
            AASSubmodelElementList statementNode = entity.getStatementNode();
            if (statementNode != null) {
                UaNode[] statementNodes = statementNode.getComponents();
                if (statementNodes.length != valueList.size()) {
                    logger.warn("Size of Value (" + valueList.size() + ") doesn't match the number of StatementNodes (" + statementNodes.length + ")");
                    throw new IllegalArgumentException("Size of Value doesn't match the number of StatementNodes");
                }

                for (int i = 0; i < valueList.size(); i++) {
                    if (statementNodes[i] instanceof AASSubmodelElementType) {
                        setSubmodelElementValue((AASSubmodelElementType) statementNodes[i], value);
                    }
                }
            }
        }
        catch (Throwable ex) {
            logger.error("setEntityValue Exception", ex);
            throw ex;
        }
    }


    /**
     * Converts the given EntityType to the corresponding AASEntityTypeDataType.
     *
     * @param value The desired EntityType
     * @return The corresponding AASEntityTypeDataType
     */
    private static AASEntityTypeDataType convertEntityType(EntityType value) {
        AASEntityTypeDataType retval = AASEntityTypeDataType.valueOf(value.ordinal());
        return retval;
    }


    /**
     * Converts the given ModelingKind to the corresponding
     * AASModelingKindDataType.
     *
     * @param value the desired ModelingKind
     * @return The corresponding AASModelingKindDataType
     */
    private static AASModelingKindDataType convertModelingKind(ModelingKind value) {
        AASModelingKindDataType retval;
        if (value == null) {
            logger.warn("convertModelingKind: value == null");
            retval = AASModelingKindDataType.Instance;
        }
        else {
            switch (value) {
                case INSTANCE:
                    retval = AASModelingKindDataType.Instance;
                    break;
                case TEMPLATE:
                    retval = AASModelingKindDataType.Template;
                    break;
                default:
                    logger.warn("convertModelingKind: unknown value " + value);
                    throw new IllegalArgumentException("unknown ModelingKind: " + value);
            }
        }

        return retval;
    }


    /**
     * Converts the given IdentifierType to the corresponding
     * AASIdentifierTypeDataType.
     *
     * @param value The desired IdentifierType
     * @return The corresponding AASIdentifierTypeDataType.
     */
    private static AASIdentifierTypeDataType convertIdentifierType(IdentifierType value) {
        AASIdentifierTypeDataType retval;
        switch (value) {
            case CUSTOM:
                retval = AASIdentifierTypeDataType.Custom;
                break;
            case IRI:
                retval = AASIdentifierTypeDataType.IRI;
                break;
            case IRDI:
                retval = AASIdentifierTypeDataType.IRDI;
                break;
            default:
                logger.warn("convertIdentifierType: unknown value " + value);
                throw new IllegalArgumentException("unknown IdentifierType: " + value);
        }
        return retval;
    }


    /**
     * Converts the given AssetKind to the corresponding AASAssetKindDataType.
     *
     * @param value The desired AssetKind
     * @return The corresponding AASAssetKindDataType
     */
    private static AASAssetKindDataType convertAssetKind(AssetKind value) {
        AASAssetKindDataType retval;
        switch (value) {
            case INSTANCE:
                retval = AASAssetKindDataType.Instance;
                break;
            case TYPE:
                retval = AASAssetKindDataType.Type;
                break;
            default:
                logger.warn("convertAssetKind: unknown value " + value);
                throw new IllegalArgumentException("unknown KeyType: " + value);
        }
        return retval;
    }


    /**
     * Extracts the name from the given Submodel Reference
     *
     * @param submodelRef The submodel reference
     * @return The Name of the Submodel
     */
    private static String getSubmodelName(Reference submodelRef) {
        String retval = "";
        if (submodelRef != null) {
            if (!submodelRef.getKeys().isEmpty()) {
                retval = submodelRef.getKeys().get(0).getValue();
            }
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
        catch (Throwable ex) {
            logger.error("addQualifierValueNode Exception", ex);
            throw ex;
        }
    }


    private NodeId getDefaultNodeId() {
        int nr = ++nodeIdCounter;
        return new NodeId(getNamespaceIndex(), nr);
    }

    //    /**
    //     * Checks if the given Node is of the desired type.
    //     * 
    //     * @param client The OPC UA Client
    //     * @param node The desired Node
    //     * @param typeNode The expected type.
    //     * @throws ServiceException If the operation fails
    //     * @throws AddressSpaceException If the operation fails
    //     * @throws ServiceResultException If the operation fails
    //     */
    //    private boolean checkType(NodeId node, NodeId typeNode) throws ServiceException, AddressSpaceException, ServiceResultException {
    //        boolean retval = false;
    //        UaNode uanode = findNode(node);
    //        UaReference ref = uanode.getReference(Identifiers.HasTypeDefinition, false);
    //        retval = typeNode.equals(getNamespaceTable().toNodeId(ref.getTargetId()));
    //        return retval;
    //    }
}
