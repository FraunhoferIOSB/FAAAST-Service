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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.mapper;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpConstants;
import de.fraunhofer.iosb.ilt.faaast.service.model.IdShortPath;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractSubmodelInterfaceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.RegExHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;

import java.util.Map;


/**
 * Base class for requests that are part of the Submodel Interface API. This class exposes the URL both as the
 * stand-alone URL (e.g. submodels/{submodelIdentifier}/...) as well as
 * the AAS-contextualized version (e.g. /shells/{aasIdentifier}/submodels/{submodelIdentifier}/...).
 *
 * @param <T> actual type of the request
 * @param <R> actual type of the response
 */
public abstract class AbstractSubmodelElementInterfaceRequestMapper<T extends AbstractSubmodelInterfaceRequest<R>, R extends Response>
        extends AbstractSubmodelInterfaceRequestMapper<T, R> {
    protected static final String SUBMODEL_ELEMENT_PATH = RegExHelper.uniqueGroupName();
    protected static final String SUBMODEL_ELEMENT_PATH_PATTERN = String.format("submodel-elements/%s", pathElement(SUBMODEL_ELEMENT_PATH));

    /**
     * urlPattern must not contain initial part of URL identifying the submodel.
     *
     * @param serviceContext the service context
     * @param method the HTTP method for this request
     * @param urlPattern the URL pattern
     * @param excludedContentModifiers content modifiers that are not allowed for this request as they are handled
     *            explicitly by another request. This is required so that the
     *            generated URL patterns do not overlap.
     */
    protected AbstractSubmodelElementInterfaceRequestMapper(ServiceContext serviceContext, HttpMethod method, String urlPattern, Content... excludedContentModifiers) {
        super(serviceContext, method, addSubmodelElementPath(urlPattern), excludedContentModifiers);
    }


    private static String addSubmodelElementPath(String urlPattern) {
        return String.format("%s%s%s",
                SUBMODEL_ELEMENT_PATH_PATTERN,
                !StringHelper.isBlank(urlPattern) && !urlPattern.startsWith(HttpConstants.PATH_SEPERATOR)
                        ? "/"
                        : "",
                urlPattern);
    }


    @Override
    protected boolean doFilter(AccessPermissionRule rule, Map<String, String> urlParameters) throws InvalidRequestException {
        return filterRule(rule, getSubmodelId(urlParameters), IdShortPath.parse(getIdShortPath(urlParameters)));
    }


    /**
     * Returns the idShort path of a referable request.
     * 
     * @param urlParameters The url params of the request.
     * @return The id short path
     */
    protected String getIdShortPath(Map<String, String> urlParameters) {
        return EncodingHelper.urlDecode(urlParameters.get(SUBMODEL_ELEMENT_PATH));
    }

}
