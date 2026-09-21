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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.creator;

import com.prosysopc.ua.StatusException;
import com.prosysopc.ua.ValueRanks;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.server.NodeManagerUaNode;
import com.prosysopc.ua.stack.builtintypes.DataValue;
import com.prosysopc.ua.stack.builtintypes.ExpandedNodeId;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.UnsignedInteger;
import com.prosysopc.ua.stack.builtintypes.Variant;
import com.prosysopc.ua.stack.common.ServiceResultException;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.types.opcua.BaseDataVariableType;
import com.prosysopc.ua.types.opcua.DictionaryEntryType;
import com.prosysopc.ua.types.opcua.IrdiDictionaryEntryType;
import com.prosysopc.ua.types.opcua.UriDictionaryEntryType;
import com.prosysopc.ua.types.opcua.server.DictionaryFolderTypeNode;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.UaHelper;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.nodemanager.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.nodemanager.IrdiDictionaryNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.nodemanager.UriDictionaryNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import opc.ua.aas.Ids;
import opc.ua.aas.datatypes.AASConceptDescription;
import opc.ua.aas.datatypes.AASConceptDescriptionCommonAttributes;
import opc.ua.aas.datatypes.AASDataSpecificationContent;
import opc.ua.aas.datatypes.AASDataSpecificationIec61360;
import opc.ua.aas.datatypes.AASEmbeddedConceptDescription;
import opc.ua.aas.datatypes.AASEmbeddedDataSpecification;
import opc.ua.aas.datatypes.AASLevelType;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.DataSpecificationContent;
import org.eclipse.digitaltwin.aas4j.v3.model.DataSpecificationIec61360;
import org.eclipse.digitaltwin.aas4j.v3.model.EmbeddedDataSpecification;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create ConceptDescriptions and integrate them into the OPC UA address space.
 */
public class ConceptDescriptionCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConceptDescriptionCreator.class);
    private static final String CONCEPT_DESCRIPTIONS = "ConceptDescriptions";

    private static DictionaryFolderTypeNode dictEntriesFolder = null;

    /**
     * Maps AAS references to dictionary entry types
     */
    private static final Map<Reference, DictionaryEntryType> dictionaryMap = new HashMap<>();

    private ConceptDescriptionCreator() {
        throw new IllegalStateException("Class not instantiable");
    }


    /**
     * Adds the given Concept Descriptions to the specified node.
     *
     * @param baseNode The node where
     * @param descriptions The desired Concept Descriptions.
     * @param nodeManager The NodeManager.
     * @throws ServiceResultException If an error occurs.
     * @throws StatusException If an error occurs.
     */
    public static void addConceptDescriptions(UaNode baseNode, List<ConceptDescription> descriptions, AasServiceNodeManager nodeManager)
            throws ServiceResultException, StatusException {

        NodeId nodeId = nodeManager.createNodeId(baseNode, CONCEPT_DESCRIPTIONS);
        BaseDataVariableType cdsNode = (BaseDataVariableType) nodeManager.findNode(nodeId);
        if (cdsNode == null) {
            cdsNode = nodeManager.createInstance(BaseDataVariableType.class, CONCEPT_DESCRIPTIONS, nodeId);
            baseNode.addComponent(cdsNode);
        }

        List<AASConceptDescription> list = getConceptDescriptions(cdsNode);
        for (ConceptDescription c: descriptions) {
            AASConceptDescription node = getConceptDescriptionData(c, null);
            list.add(node);
        }
        cdsNode.setValueRank(ValueRanks.OneDimension);
        cdsNode.setDataTypeId(nodeManager.getNamespaceTable().toNodeId(Ids.AASConceptDescription));
        cdsNode.setArrayDimensions(new UnsignedInteger[] {
                UnsignedInteger.valueOf(list.size())
        });
        cdsNode.setValue(list.toArray(AASConceptDescription[]::new));
    }


    /**
     * Adds a SemanticId to the given node.
     *
     * @param node The UA node in which the SemanticId should be created
     * @param semanticId The reference of the desired SemanticId
     * @param nodeManager The NodeManager
     */
    public static void addSemanticId(UaNode node, Reference semanticId, NodeManagerUaNode nodeManager) {
        Ensure.requireNonNull(node);
        Ensure.requireNonNull(semanticId);
        try {
            if (ReferenceHelper.containsSameReference(dictionaryMap, semanticId)) {
                node.addReference(ReferenceHelper.getValueBySameReference(dictionaryMap, semanticId), Identifiers.HasDictionaryEntry, false);
            }
            // if entry not found: perhaps create a new one?
            if ((semanticId.getKeys() != null) && (semanticId.getKeys().size() == 1)) {
                String id = semanticId.getKeys().get(0).getValue();
                DictionaryEntryType entry = createDictionaryEntry(id, nodeManager);

                if (entry != null) {
                    LOGGER.atDebug().log("addSemanticId: add HasDictionaryEntry reference for Node {} to {}", node.getNodeId().toString(), entry.getNodeId().toString());
                    node.addReference(entry, Identifiers.HasDictionaryEntry, false);
                    dictionaryMap.put(semanticId, entry);
                }
            }
        }
        catch (Exception ex) {
            LOGGER.error("addSemanticId error", ex);
        }
    }


    private static AASConceptDescription getConceptDescriptionData(ConceptDescription conceptDescription, List<EmbeddedDataSpecification> embeddedDataSpecifications) {
        AASConceptDescription descriptionNode;

        List<AASEmbeddedDataSpecification> list = new ArrayList<>();
        if (embeddedDataSpecifications != null) {
            for (var embedDataSpec: embeddedDataSpecifications) {
                list.add(getEmbeddedDataDescription(embedDataSpec));
            }
        }

        if ((conceptDescription != null) && (conceptDescription.getEmbeddedDataSpecifications() != null)) {
            for (var embedDataSpec: conceptDescription.getEmbeddedDataSpecifications()) {
                list.add(getEmbeddedDataDescription(embedDataSpec));
            }
        }

        if (!list.isEmpty()) {
            AASEmbeddedConceptDescription embeddedDescription = new AASEmbeddedConceptDescription();
            embeddedDescription.setEmbeddedDataSpecification(list.toArray(AASEmbeddedDataSpecification[]::new));
            descriptionNode = embeddedDescription;
        }
        else {
            descriptionNode = new AASConceptDescription();
        }

        if (conceptDescription != null) {
            setConceptDescriptionData(conceptDescription, descriptionNode);
        }

        // check CommonAttributes
        if (descriptionNode.getCommonAttributes() == null) {
            AASConceptDescriptionCommonAttributes common = new AASConceptDescriptionCommonAttributes();
            descriptionNode.setCommonAttributes(common);
        }

        descriptionNode.getCommonAttributes().setIdentifiable(BaseDataCreator.getIdentifiable(conceptDescription));
        HasDataSpecificationCreator.addHasDataSpecification(descriptionNode.getCommonAttributes(), conceptDescription);

        return descriptionNode;
    }


    private static void setConceptDescriptionData(ConceptDescription conceptDescription, AASConceptDescription descriptionNode) {
        if (conceptDescription.getIdShort() != null) {
            descriptionNode.setIdShort(conceptDescription.getIdShort());
        }

        if (conceptDescription.getDisplayName() != null) {
            descriptionNode.setDisplayName(ValueConverter.convertLangStringSet(conceptDescription.getDisplayName()));
        }

        if (conceptDescription.getDescription() != null) {
            descriptionNode.setDescription(ValueConverter.convertLangStringSet(conceptDescription.getDescription()));
        }
    }


    private static AASEmbeddedDataSpecification getEmbeddedDataDescription(EmbeddedDataSpecification dataSpecification) {
        AASEmbeddedDataSpecification retval = new AASEmbeddedDataSpecification();

        retval.setDataSpecification(ReferenceCreator.getAasReference(dataSpecification.getDataSpecification()));
        retval.setDataSpecificationContent(getDataSpecificationContent(dataSpecification.getDataSpecificationContent()));
        return retval;
    }


    private static AASDataSpecificationContent getDataSpecificationContent(DataSpecificationContent content) {
        AASDataSpecificationContent retval = null;
        if ((content != null) && (content instanceof DataSpecificationIec61360 content61360)) {
            var builder = AASDataSpecificationIec61360.builder();
            if (content61360.getDataType() != null) {
                builder.setDataType(ValueConverter.convertDataTypeIec61360(content61360.getDataType()));
            }

            if (content61360.getDefinition() != null) {
                builder.setDefinition(ValueConverter.convertLangStringSet(content61360.getDefinition()));
            }

            if (content61360.getLevelType() != null) {
                var level = content61360.getLevelType();
                builder.setLevelType(new AASLevelType(level.getMin(), level.getMax(), level.getNom(), level.getTyp()));
            }

            if (content61360.getPreferredName() != null) {
                builder.setPreferredName(ValueConverter.convertLangStringSet(content61360.getPreferredName()));
            }

            if (content61360.getShortName() != null) {
                builder.setShortName(ValueConverter.convertLangStringSet(content61360.getShortName()));
            }

            if (content61360.getSourceOfDefinition() != null) {
                builder.setSourceOfDefinition(content61360.getSourceOfDefinition());
            }

            if (content61360.getSymbol() != null) {
                builder.setSymbol(content61360.getSymbol());
            }

            if (content61360.getUnit() != null) {
                builder.setUnit(content61360.getUnit());
            }

            if (content61360.getUnitId() != null) {
                builder.setUnitId(ReferenceCreator.getAasReference(content61360.getUnitId()));
            }

            if (content61360.getValue() != null) {
                builder.setValue(content61360.getValue());
            }

            if (content61360.getValueFormat() != null) {
                builder.setValueFormat(content61360.getValueFormat());
            }

            if (content61360.getValueList() != null) {
                builder.setValueList(ValueConverter.convertValueList(content61360.getValueList()));
            }
            retval = builder.build();
        }
        return retval;
    }


    private static void createDictionaryEntriesFolder(NodeManagerUaNode nodeManager) throws StatusException {
        // create folder DictionaryEntries
        final UaNode dictionariesFolder = nodeManager.getServer().getNodeManagerRoot().getNodeOrExternal(Identifiers.Dictionaries);
        final NodeId dictionarieEntriesFolderId = new NodeId(nodeManager.getNamespaceIndex(), "Dictionaries.DictionaryEntries");
        dictEntriesFolder = nodeManager.createInstance(DictionaryFolderTypeNode.class, "DictionaryEntries", dictionarieEntriesFolderId);

        nodeManager.addNodeAndReference(dictionariesFolder, dictEntriesFolder, Identifiers.Organizes);
    }


    private static DictionaryEntryType createDictionaryEntry(String id, NodeManagerUaNode nodeManager) throws StatusException, ServiceResultException {
        DictionaryEntryType entry;
        if (dictEntriesFolder == null) {
            createDictionaryEntriesFolder(nodeManager);
        }
        if (UaHelper.isIrdi(id)) {
            entry = nodeManager.createInstance(IrdiDictionaryEntryType.class, id,
                    nodeManager.getNamespaceTable().toNodeId(new ExpandedNodeId(IrdiDictionaryNodeManager.NAMESPACE, id)));
            dictEntriesFolder.addComponent(entry);
        }
        else if (UaHelper.isUri(id)) {
            NodeId nodeId = nodeManager.getNamespaceTable().toNodeId(new ExpandedNodeId(UriDictionaryNodeManager.NAMESPACE, id));
            entry = nodeManager.createInstance(UriDictionaryEntryType.class, id, nodeId);
            dictEntriesFolder.addComponent(entry);
        }
        else {
            LOGGER.info("createDictionaryEntry: SemanticId ({}) is neither an IRDI nor an URI.", id);
            entry = null;
        }
        return entry;
    }


    private static List<AASConceptDescription> getConceptDescriptions(BaseDataVariableType cdsNode) {
        List<AASConceptDescription> retval = new ArrayList<>();
        DataValue dv = cdsNode.getValue();
        if (dv != null) {
            Variant variant = dv.getValue();
            if ((variant != null) && (variant.isArray())) {
                AASConceptDescription[] arr = variant.asClass(AASConceptDescription[].class, null);
                if (arr != null) {
                    retval = Arrays.asList(arr);
                }
            }
        }
        return retval;
    }
}
