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
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.aimc.Constants;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.EnvironmentHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.eclipse.digitaltwin.aas4j.v3.model.Property;
import org.eclipse.digitaltwin.aas4j.v3.model.Referable;
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
     * @return The Forms property.
     */
    public static SubmodelElementCollection getPropertyForms(SubmodelElementCollection property) {
        Optional<SubmodelElement> element = property.getValue().stream().filter(e -> Constants.AID_PROPERTY_FORMS.equals(e.getIdShort())).findFirst();
        if (element.isEmpty()) {
            throw new IllegalArgumentException("Submodel AID invalid: Property forms not found.");
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
}
