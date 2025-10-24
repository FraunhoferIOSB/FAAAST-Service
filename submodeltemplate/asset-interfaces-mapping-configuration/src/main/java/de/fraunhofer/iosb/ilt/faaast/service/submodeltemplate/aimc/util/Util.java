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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.util;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.model.SemanticIdPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.model.RelationData;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.model.HasSemantics;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.ReferenceElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementCollection;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElementList;


/**
 * Class with general utilty methods.
 */
public class Util {

    private Util() {}


    /**
     * Extracts the Forms property from the given SubmodelElementCollection.
     *
     * @param property The list of properties.
     * @param propertyReference The refence to the property.
     * @param data The relation data.
     * @return The Forms property.
     * @throws PersistenceException if storage error occurs
     * @throws ResourceNotFoundException if the resource dcesn't exist.
     */
    public static SubmodelElementCollection getPropertyForms(SubmodelElementCollection property, Reference propertyReference, RelationData data)
            throws PersistenceException, ResourceNotFoundException {
        // search root object
        SubmodelElementCollection current = property;
        Reference currentReference = propertyReference;
        while (semanticIdEquals(current, Constants.AID_PROPERTY_NESTED_SEMANTIC_ID)) {
            Reference grandParent = getGrandParent(currentReference);
            if ((grandParent != null)
                    && (EnvironmentHelper.resolve(grandParent, data.getServiceContext().getAASEnvironment()) instanceof SubmodelElementCollection grandParentObject)) {
                current = grandParentObject;
                currentReference = grandParent;
            }
            else {
                throw new IllegalArgumentException("Submodel AID invalid: Root Property not found.");
            }
        }

        Optional<SubmodelElementCollection> element;
        if (semanticIdEquals(current, Constants.AID_PROPERTY_ROOT_SEMANTIC_ID)) {
            element = SemanticIdPath.builder()
                    .globalReference(Constants.AID_PROPERTY_FORMS_SEMANTIC_ID)
                    .build()
                    .resolveOptional(current, SubmodelElementCollection.class);

            if (element.isEmpty()) {
                throw new IllegalArgumentException("Submodel AID invalid: Property forms not found.");
            }
        }
        else {
            throw new IllegalArgumentException("Submodel AID invalid: Root Property not found.");
        }
        return element.get();
    }


    /**
     * Converts the content type in MIME format to the desired format string.
     *
     * @param contentType The content type in MIME format.
     * @return The desired format.
     */
    public static String getFormatFromContentType(String contentType) {
        Ensure.requireNonNull(contentType);
        switch (contentType) {
            case "application/xml", "text/xml":
                return "XML";

            case "application/json":
                return "JSON";

            default:
                throw new IllegalArgumentException("unsupported contentType: " + contentType);
        }
    }


    /**
     * Extracts the Href property from the Forms Property list.
     *
     * @param forms The Forms Property list.
     * @return The desired Href property.
     */
    public static String getFormsHref(SubmodelElementCollection forms) {
        return SemanticIdPath.builder()
                .globalReference(Constants.AID_FORMS_HREF_SEMANTIC_ID)
                .build()
                .resolveUnique(forms, Property.class)
                .getValue();
    }


    /**
     * Extracts the Base URL from the metadata.
     *
     * @param metadata The Metadata.
     * @return The base URL.
     */
    public static String getBaseUrl(SubmodelElementCollection metadata) {
        return SemanticIdPath.builder()
                .globalReference(Constants.AID_METADATA_BASE_SEMANTIC_ID)
                .build()
                .resolveUnique(metadata, Property.class)
                .getValue();
    }


    /**
     * Extracts the ContentType from the metadata.
     *
     * @param metadata The Metadata.
     * @return The ContentType.
     */
    public static String getContentType(SubmodelElementCollection metadata) {
        return SemanticIdPath.builder()
                .globalReference(Constants.AID_CONTENT_TYPE_SEMANTIC_ID)
                .build()
                .resolveUnique(metadata, Property.class)
                .getValue();
    }


    /**
     * Get the interface title from the interface.
     *
     * @param assetInterface The desired interface.
     * @return The title.
     */
    public static String getInterfaceTitle(SubmodelElementCollection assetInterface) {
        return SemanticIdPath.builder()
                .globalReference(Constants.AID_INTERFACE_TITLE_SEMANTIC_ID)
                .build()
                .resolveUnique(assetInterface, Property.class)
                .getValue();
    }


    /**
     * Gets the Endpoint Metadata from the interface.
     *
     * @param assetInterface The desired interface.
     * @return The Endpoint Metadata.
     */
    public static SubmodelElementCollection getEndpointMetadata(SubmodelElementCollection assetInterface) {
        return SemanticIdPath.builder()
                .globalReference(Constants.AID_ENDPOINT_METADATA_SEMANTIC_ID)
                .build()
                .resolveUnique(assetInterface, SubmodelElementCollection.class);
    }


    /**
     * Extracts the list of supported security definitions.
     *
     * @param serviceContext The serviceContext.
     * @param securityList The complete list of security definitions.
     * @return The list of supported security definitions
     * @throws PersistenceException if a storage error occurs.
     * @throws ResourceNotFoundException if the resource dcesn't exist.
     */
    public static List<String> getSupportedSecurityList(ServiceContext serviceContext, SubmodelElementList securityList) throws PersistenceException, ResourceNotFoundException {
        List<String> supportedSecurity = new ArrayList<>();
        for (SubmodelElement se: securityList.getValue()) {
            if (se instanceof ReferenceElement refElem) {
                Referable securityReferable = EnvironmentHelper.resolve(refElem.getValue(), serviceContext.getAASEnvironment());
                if (securityReferable instanceof SubmodelElement securityElement) {
                    if (semanticIdEquals(securityElement, Constants.AID_SECURITY_NOSEC_SEMANTIC_ID)) {
                        supportedSecurity.add(Constants.AID_SECURITY_NOSEC);
                    }
                    else if (semanticIdEquals(securityElement, Constants.AID_SECURITY_BASIC_SEMANTIC_ID)) {
                        supportedSecurity.add(Constants.AID_SECURITY_BASIC);
                    }
                }
                else {
                    throw new IllegalArgumentException("SecurityElement invalid: no SubmodelElement");
                }
            }
        }

        return supportedSecurity;
    }


    /**
     * Gets the content for the desired element.
     *
     * @param baseContentType The base content type.
     * @param forms The forms attribute of the desired element.
     * @return The contentr type of the element.
     */
    public static String getContentType(String baseContentType, SubmodelElementCollection forms) {
        String contentType = baseContentType;
        Optional<Property> prop = SemanticIdPath.builder()
                .globalReference(Constants.AID_CONTENT_TYPE_SEMANTIC_ID)
                .build()
                .resolveOptional(forms, Property.class);
        if (prop.isPresent()) {
            contentType = prop.get().getValue();
        }
        return contentType;
    }


    /**
     * Creates the JsonPath from the given list of keys.
     *
     * @param pathList The list of keys.
     * @return The Jsonpath.
     */
    public static String createJsonPath(List<String> pathList) {
        if ((pathList == null) || pathList.isEmpty()) {
            return "";
        }
        else {
            StringBuilder path = new StringBuilder("$");
            for (String s: pathList) {
                path.append(".");
                path.append(s);
            }
            return path.toString();
        }
    }


    /**
     * Checks if the given object is the InteractionMetadata or not.
     *
     * @param object The object to check.
     * @return True if it's the InteractionMetadata object, false if not.
     */
    public static boolean isInteractionMetadata(SubmodelElementCollection object) {
        return semanticIdEquals(object, Constants.AID_INTERACTION_METADATA_SEMANTIC_ID);
    }


    /**
     * Gets the Key for the given object.
     *
     * @param object The desired object.
     * @return The key for the object.
     */
    public static String getKey(SubmodelElementCollection object) {
        String retval = object.getIdShort();
        Optional<Property> prop = SemanticIdPath.builder()
                .globalReference(Constants.AID_PROPERTY_KEY_SEMANTIC_ID)
                .build()
                .resolveOptional(object, Property.class);
        if (prop.isPresent()) {
            retval = prop.get().getValue();
        }
        return retval;
    }


    /**
     * Gets the grandparent of the given object.
     *
     * @param reference The reference of the desired object.
     * @return The refence to the grandparent, null if not available.
     */
    public static Reference getGrandParent(Reference reference) {
        Reference parent = ReferenceHelper.getParent(reference);
        if (parent != null) {
            return ReferenceHelper.getParent(parent);
        }
        return null;
    }


    /**
     * Checks if the SemanticId of the given object is the same as the given SemanticId.
     * As GlobalReference and ConceptDescription is possible, we only compare the key value.
     *
     * @param object The object whose reference is to be compared.
     * @param semanticId The SemanticId String to compare. Must not be null.
     * @return True if it's equal, false if not.
     */
    public static boolean semanticIdEquals(HasSemantics object, String semanticId) {
        if ((object != null) && (object.getSemanticId() != null)) {
            Reference ref = object.getSemanticId();
            return semanticReferenceEquals(ref, semanticId);
        }
        else {
            return false;
        }
    }


    /**
     * Checks if the given SemanticId is contained in the SupplementalSemanticIds.
     * As GlobalReference and ConceptDescription is possible, we only compare the key value.
     *
     * @param object The object whose reference is to be compared.
     * @param semanticId The SemanticId String to compare. Must not be null.
     * @return True if it's cinatined, false if not.
     */
    public static boolean containsSupplementalSemanticId(HasSemantics object, String semanticId) {
        if ((object != null) && (object.getSupplementalSemanticIds() != null)) {
            for (Reference ref: object.getSupplementalSemanticIds()) {
                if (semanticReferenceEquals(ref, semanticId)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Gets the JSON query path for the specified property.
     *
     * @param property The desired property.
     * @param propertyReference The reference for the given property.
     * @param data The relation data.
     * @return The JSON query path.
     * @throws PersistenceException if storage error occurs
     * @throws ResourceNotFoundException if the resource dcesn't exist.
     */
    public static String getJsonPath(SubmodelElementCollection property, Reference propertyReference, RelationData data) throws PersistenceException, ResourceNotFoundException {
        List<String> pathList = new ArrayList<>();
        boolean ende = false;
        pathList.add(Util.getKey(property));
        Reference current = propertyReference;
        while (!ende) {
            ende = true;
            Reference parent = ReferenceHelper.getParent(current);
            if (parent != null) {
                Reference grandParent = ReferenceHelper.getParent(parent);
                if ((grandParent != null)
                        && (EnvironmentHelper.resolve(grandParent, data.getServiceContext().getAASEnvironment()) instanceof SubmodelElementCollection grandParentObject)
                        && (!Util.isInteractionMetadata(grandParentObject))) {
                    pathList.add(Util.getKey(grandParentObject));
                    current = grandParent;
                    ende = false;
                }
            }
        }
        // reverse the order and remove the top level object
        if (!pathList.isEmpty()) {
            Collections.reverse(pathList);
            pathList.remove(0);
        }
        return Util.createJsonPath(pathList);
    }


    private static boolean semanticReferenceEquals(Reference ref, String semanticId) {
        return (ref.getKeys().size() == 1) && semanticId.equals(ref.getKeys().get(0).getValue());
    }
}
