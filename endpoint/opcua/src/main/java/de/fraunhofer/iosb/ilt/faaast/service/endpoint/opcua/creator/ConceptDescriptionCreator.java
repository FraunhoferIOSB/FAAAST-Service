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
import com.prosysopc.ua.UaQualifiedName;
import com.prosysopc.ua.nodes.UaNode;
import com.prosysopc.ua.stack.builtintypes.LocalizedText;
import com.prosysopc.ua.stack.builtintypes.NodeId;
import com.prosysopc.ua.stack.builtintypes.QualifiedName;
import com.prosysopc.ua.stack.core.AccessLevelType;
import com.prosysopc.ua.stack.core.Identifiers;
import com.prosysopc.ua.types.opcua.DictionaryEntryType;
import com.prosysopc.ua.types.opcua.server.FolderTypeNode;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import opc.i4aas.objecttypes.AASConceptDescriptionType;
import opc.i4aas.objecttypes.AASReferenceList;
import opc.i4aas.objecttypes.AASReferenceType;
import opc.i4aas.objecttypes.server.AASReferenceTypeNode;
import org.eclipse.digitaltwin.aas4j.v3.dataformat.core.util.AasUtils;
import org.eclipse.digitaltwin.aas4j.v3.model.AdministrativeInformation;
import org.eclipse.digitaltwin.aas4j.v3.model.ConceptDescription;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * Helper class to create ConceptDescriptions and integrate them into the OPC UA address space.
 */
public class ConceptDescriptionCreator {

    private static final String DEFAULT_NAME = "ConceptDescription";

    /**
     * Maps AAS references to dictionary entry types
     */
    private static final Map<Reference, DictionaryEntryType> dictionaryMap = new HashMap<>();

    private ConceptDescriptionCreator() {
        throw new IllegalStateException("Class not instantiable");
    }


    /**
     * Adds the given list of AAS Concept Descriptions.
     *
     * @param descriptions The desired list of AAS Concept Descriptions
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addConceptDescriptions(List<ConceptDescription> descriptions, AasServiceNodeManager nodeManager) throws StatusException {
        // create folder DictionaryEntries
        final UaNode dictionariesFolder = nodeManager.getServer().getNodeManagerRoot().getNodeOrExternal(Identifiers.Dictionaries);

        // Folder for my objects
        final NodeId dictionarieEntriesFolderId = new NodeId(nodeManager.getNamespaceIndex(), "Dictionaries.DictionaryEntries");
        FolderTypeNode dictEntriesFolder = nodeManager.createInstance(FolderTypeNode.class, "DictionaryEntries", dictionarieEntriesFolderId);

        nodeManager.addNodeAndReference(dictionariesFolder, dictEntriesFolder, Identifiers.Organizes);

        int counter = 1;
        for (ConceptDescription c: descriptions) {
            String name = c.getIdShort();
            DictionaryEntryType dictNode;
            NodeId nid = null;
            if ((name != null) && (!name.isEmpty())) {
                nid = nodeManager.createNodeId(dictionariesFolder, name);
                if (nodeManager.hasNode(nid)) {
                    // The NodeId already exists
                    nid = nodeManager.getDefaultNodeId();
                }
            }
            else {
                nid = nodeManager.getDefaultNodeId();
                name = String.format("%s %s", DEFAULT_NAME, counter++);
            }

            AASConceptDescriptionType desriptionNode = nodeManager.createInstance(AASConceptDescriptionType.class, name, nid);
            AASReferenceList listNode = desriptionNode.getIsCaseOfNode();
            if (listNode == null) {
                AasReferenceCreator.addAasReferenceListNode(desriptionNode, c.getIsCaseOf(), AASConceptDescriptionType.IS_CASE_OF, nodeManager);
            }
            else {
                AasReferenceCreator.addAasReferencesToList(listNode, c.getIsCaseOf(), AASConceptDescriptionType.IS_CASE_OF, nodeManager);
            }

            addIdentifiableData(desriptionNode, c.getId(), c.getAdministration(), name, nodeManager);
            addConceptDescriptionReference(desriptionNode, AasUtils.toReference(c), nodeManager);
            dictEntriesFolder.addComponent(desriptionNode);
            dictionaryMap.put(AasUtils.toReference(c), desriptionNode);
            dictNode = desriptionNode;

            nodeManager.addReferable(AasUtils.toReference(c), new ObjectData(c, dictNode));
        }
    }


    /**
     * Adds a SemanticId to the given node.
     *
     * @param node The UA node in which the SemanticId should be created
     * @param semanticId The reference of the desired SemanticId
     */
    public static void addSemanticId(UaNode node, Reference semanticId) {
        if (ReferenceHelper.containsSameReference(dictionaryMap, semanticId)) {
            node.addReference(ReferenceHelper.getValueBySameReference(dictionaryMap, semanticId), Identifiers.HasDictionaryEntry, false);
        }
        // if entry not found: perhaps create a new one?
    }


    /**
     * Adds a reference to a ConceptDescription.
     *
     * @param node The desired UA node
     * @param ref The reference to create
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    private static void addConceptDescriptionReference(UaNode node, Reference ref, AasServiceNodeManager nodeManager) throws StatusException {
        if (ref != null) {
            String name = "ConceptDescription";
            QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASMultiLanguagePropertyType.getNamespaceUri(), name)
                    .toQualifiedName(nodeManager.getNamespaceTable());
            NodeId nid = nodeManager.createNodeId(node, browseName);
            AASReferenceType nodeRef = nodeManager.createInstance(AASReferenceTypeNode.class, nid, browseName, LocalizedText.english(name));

            AasReferenceCreator.setAasReferenceData(ref, nodeRef);
            node.addComponent(nodeRef);
            node.addReference(nodeRef, Identifiers.HasDictionaryEntry, false);
        }
    }


    /**
     * Adds AAS Identifiable information to the given node.
     *
     * @param conceptDescriptionNode The desired node where the Identifiable information should be added
     * @param identifier The corresponding AAS Identifier
     * @param adminInfo The corresponding AAS Administrative Information
     * @param category The desired category
     * @param nodeManager The corresponding Node Manager
     */
    private static void addIdentifiableData(AASConceptDescriptionType conceptDescriptionNode, String identifier, AdministrativeInformation adminInfo, String category,
                                            AasServiceNodeManager nodeManager)
            throws StatusException {
        if (identifier != null) {
            conceptDescriptionNode.setId(identifier);
        }

        AdministrativeInformationCreator.addAdminInformationProperties(conceptDescriptionNode.getAdministrationNode(), adminInfo, nodeManager);

        conceptDescriptionNode.setCategory(category != null ? category : "");

        if (AasServiceNodeManager.VALUES_READ_ONLY) {
            conceptDescriptionNode.getCategoryNode().setAccessLevel(AccessLevelType.of(AccessLevelType.Options.CurrentRead));
        }
    }
}
