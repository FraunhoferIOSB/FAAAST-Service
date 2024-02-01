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
import com.prosysopc.ua.stack.core.Identifiers;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.AasServiceNodeManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.ValueConverter;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.ObjectData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.data.SubmodelElementData;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.opcua.helper.AasSubmodelElementHelper;
import java.util.List;
import opc.i4aas.objecttypes.AASMultiLanguagePropertyType;
import org.eclipse.digitaltwin.aas4j.v3.model.LangStringTextType;
import org.eclipse.digitaltwin.aas4j.v3.model.MultiLanguageProperty;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to create MultiLanguageProperties and integrate them into the
 * OPC UA address space.
 */
public class MultiLanguagePropertyCreator extends SubmodelElementCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiLanguagePropertyCreator.class);

    /**
     * Adds an AAS Multi Language Property to the given node.
     *
     * @param node The desired UA node
     * @param aasMultiLang The AAS Multi Language Property to add
     * @param multiLangRef The AAS reference to the Multi Language Property
     * @param submodel The corresponding Submodel as parent object of the data element
     * @param ordered Specifies whether the multi language property should be
     *            added ordered (true) or unordered (false)
     * @param nodeManager The corresponding Node Manager
     * @throws StatusException If the operation fails
     */
    public static void addAasMultiLanguageProperty(UaNode node, MultiLanguageProperty aasMultiLang, Reference multiLangRef, Submodel submodel, boolean ordered,
                                                   AasServiceNodeManager nodeManager)
            throws StatusException {
        try {
            if ((node != null) && (aasMultiLang != null)) {
                String name = aasMultiLang.getIdShort();
                if ((name == null) || name.isEmpty()) {
                    name = getNameFromReference(multiLangRef);
                }
                QualifiedName browseName = UaQualifiedName.from(opc.i4aas.ObjectTypeIds.AASMultiLanguagePropertyType.getNamespaceUri(), name)
                        .toQualifiedName(nodeManager.getNamespaceTable());
                NodeId nid = nodeManager.getDefaultNodeId();
                AASMultiLanguagePropertyType multiLangNode = nodeManager.createInstance(AASMultiLanguagePropertyType.class, nid, browseName, LocalizedText.english(name));
                addSubmodelElementBaseData(multiLangNode, aasMultiLang, nodeManager);

                setMultiLanguagePropertyValues(aasMultiLang, multiLangNode, nodeManager);

                nodeManager.addSubmodelElementAasMap(multiLangNode.getValueNode().getNodeId(),
                        new SubmodelElementData(aasMultiLang, submodel, SubmodelElementData.Type.MULTI_LANGUAGE_VALUE, multiLangRef));

                nodeManager.addSubmodelElementOpcUA(multiLangRef, multiLangNode);

                if (ordered) {
                    node.addReference(multiLangNode, Identifiers.HasOrderedComponent, false);
                }
                else {
                    node.addComponent(multiLangNode);
                }

                nodeManager.addReferable(multiLangRef, new ObjectData(aasMultiLang, multiLangNode, submodel));
            }
        }
        catch (Exception ex) {
            LOGGER.error("addAasMultiLanguageProperty Exception", ex);
        }
    }


    private static void setMultiLanguagePropertyValues(MultiLanguageProperty aasMultiLang, AASMultiLanguagePropertyType multiLangNode, AasServiceNodeManager nodeManager)
            throws StatusException {
        List<LangStringTextType> values = aasMultiLang.getValue();
        if (values != null) {
            if (multiLangNode.getValueNode() == null) {
                AasSubmodelElementHelper.addMultiLanguageValueNode(multiLangNode, values.size(), nodeManager);
            }

            multiLangNode.getValueNode().setValue(ValueConverter.getLocalizedTextFromLangStringSet(values));
        }

        if (aasMultiLang.getValueId() != null) {
            AasReferenceCreator.addAasReferenceAasNS(multiLangNode, aasMultiLang.getValueId(), AASMultiLanguagePropertyType.VALUE_ID, nodeManager);
        }
    }

}
