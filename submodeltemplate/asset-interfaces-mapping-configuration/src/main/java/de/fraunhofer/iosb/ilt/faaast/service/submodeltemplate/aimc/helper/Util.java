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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.helper;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionConfig;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionManager;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
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

        Optional<SubmodelElement> element;
        if (semanticIdEquals(current, Constants.AID_PROPERTY_ROOT_SEMANTIC_ID)) {
            element = current.getValue().stream().filter(e -> Constants.AID_PROPERTY_FORMS.equals(e.getIdShort())).findFirst();
            if (element.isEmpty()) {
                throw new IllegalArgumentException("Submodel AID invalid: Property forms not found.");
            }
        }
        else {
            throw new IllegalArgumentException("Submodel AID invalid: Root Property not found.");
        }
        return (SubmodelElementCollection) element.get();
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
        Optional<SubmodelElement> element = forms.getValue().stream().filter(e -> Constants.AID_FORMS_HREF.equals(e.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel AID invalid: Property href not found in forms.");
        }
        return (((Property) element.get()).getValue());
    }


    /**
     * Extracts the Base URL from the metadata.
     *
     * @param metadata The Metadata.
     * @return The base URL.
     */
    public static String getBaseUrl(SubmodelElementCollection metadata) {
        Optional<SubmodelElement> element = metadata.getValue().stream().filter(e -> Constants.AID_METADATA_BASE.equals(e.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel AID invalid: EndpointMetadata base not found.");
        }
        return ((Property) element.get()).getValue();
    }


    /**
     * Extracts the ContentType from the metadata.
     *
     * @param metadata The Metadata.
     * @return The ContentType.
     */
    public static String getContentType(SubmodelElementCollection metadata) {
        String contentType = null;
        Optional<SubmodelElement> element = metadata.getValue().stream().filter(e -> Constants.AID_METADATA_CONTENT_TYPE.equals(e.getIdShort())).findFirst();
        if (element.isPresent() && (element.get() instanceof Property prop)) {
            contentType = prop.getValue();
        }
        return contentType;
    }


    /**
     * Get the interface title from the interface.
     *
     * @param assetInterface The desired interface.
     * @return The title.
     */
    public static String getInterfaceTitle(SubmodelElementCollection assetInterface) {
        Optional<SubmodelElement> element = assetInterface.getValue().stream().filter(p -> Constants.AID_INTERFACE_TITLE.equals(p.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel AID invalid: Interface Title not found.");
        }
        return (((Property) element.get()).getValue());
    }


    /**
     * Gets the Endpoint Metadata from the interface.
     *
     * @param assetInterface The desired interface.
     * @return The Endpoint Metadata.
     */
    public static SubmodelElementCollection getEndpointMetadata(SubmodelElementCollection assetInterface) {
        Optional<SubmodelElement> element = assetInterface.getValue().stream().filter(e -> Constants.AID_ENDPOINT_METADATA.equals(e.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel AID invalid: EndpointMetadata not found.");
        }
        return (SubmodelElementCollection) element.get();
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
                Referable securityElement = EnvironmentHelper.resolve(refElem.getValue(), serviceContext.getAASEnvironment());
                if (Constants.AID_SECURITY_NOSEC.equals(securityElement.getIdShort()) || Constants.AID_SECURITY_BASIC.equals(securityElement.getIdShort())) {
                    supportedSecurity.add(securityElement.getIdShort());
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
        Optional<SubmodelElement> element = forms.getValue().stream().filter(e -> Constants.AID_FORMS_CONTENT_TYPE.equals(e.getIdShort())).findFirst();
        if (element.isPresent() && (element.get() instanceof Property prop)) {
            contentType = prop.getValue();
        }
        return contentType;
    }


    /**
     * Checks if a ValueProvider exists for the given Reference.
     *
     * @param reference The desired Reference.
     * @param assetConnectionManager The AssetConnection manager.
     * @return True, if a ValueProvider exists, false if not.
     */
    public static boolean hasValueProvider(Reference reference, AssetConnectionManager assetConnectionManager) {
        return assetConnectionManager.getConnections().stream()
                .anyMatch(x -> ReferenceHelper.containsSameReference(((AssetConnectionConfig) x.asConfig()).getValueProviders(), reference));
    }


    /**
     * Checks if a SubscriptionProvider exists for the given Reference.
     *
     * @param reference The desired Reference.
     * @param assetConnectionManager The AssetConnection manager.
     * @return True, if a SubscriptionProvider exists, false if not.
     */
    public static boolean hasSubscriptionProvider(Reference reference, AssetConnectionManager assetConnectionManager) {
        return assetConnectionManager.getConnections().stream()
                .anyMatch(x -> ReferenceHelper.containsSameReference(((AssetConnectionConfig) x.asConfig()).getSubscriptionProviders(), reference));
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
        //if (object.getSemanticId() != null) {
        return Objects.equals(ReferenceBuilder.global(Constants.AID_INTERACTION_METADATA_SEMANTIC_ID), object.getSemanticId());
        //}
    }


    /**
     * Gets the Key for the given object.
     *
     * @param object The desired object.
     * @return The key for the object.
     */
    public static String getKey(SubmodelElementCollection object) {
        String retval = object.getIdShort();
        Optional<SubmodelElement> element = object.getValue().stream().filter(e -> Constants.AID_PROPERTY_KEY.equals(e.getIdShort())).findFirst();
        if (element.isPresent() && (element.get() instanceof Property prop)) {
            retval = prop.getValue();
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
     *
     * @param object The object whose reference is to be compared.
     * @param semanticId The SemanticId to compare.
     * @return True if it's equal, false if not.
     */
    public static boolean semanticIdEquals(HasSemantics object, Reference semanticId) {
        if (object != null) {
            return Objects.equals(semanticId, object.getSemanticId());
        }
        else {
            return false;
        }
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
}
