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
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpConstants;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.Content;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.AbstractSubmodelInterfaceRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.util.EncodingHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.RegExHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.StringHelper;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Base class for requests that are part of the Submodel Interface API. This class exposes the URL both as the
 * stand-alone URL (e.g. submodels/{submodelIdentifier}/...) as well as the AAS-contextualied version (e.g.
 * /shells/{aasIdentifier}/submodels/{submodelIdentifier}/...).
 *
 * @param <T> actual type of the request
 * @param <R> actual type of the response
 */
public abstract class AbstractSubmodelInterfaceRequestMapper<T extends AbstractSubmodelInterfaceRequest<R>, R extends Response>
        extends AbstractRequestMapperWithOutputModifier<T, R> {

    protected static final String AAS_ID = RegExHelper.uniqueGroupName();
    protected static final String SUBMODEL_ID = RegExHelper.uniqueGroupName();
    protected static final String AAS_PATH_PATTERN = String.format("shells/%s/", pathElement(AAS_ID));
    protected static final String SUBMODEL_PATH_PATTERN = String.format("submodels/%s", pathElement(SUBMODEL_ID));
    protected String contextualizedUrlPattern;

    /**
     * urlPattern must not contain initial part of URL identifying the submodel.
     *
     * @param serviceContext the service context
     * @param method the HTTP method for this request
     * @param urlPattern the URL pattern
     * @param excludedContentModifiers content modifiers that are not allowed for this request as they are handled
     *            explicitely by another request. This is requred so that the generated URL patterns do not overlap.
     */
    protected AbstractSubmodelInterfaceRequestMapper(ServiceContext serviceContext, HttpMethod method, String urlPattern, Content... excludedContentModifiers) {
        super(serviceContext, method, addSubmodelPath(urlPattern), excludedContentModifiers);
        this.contextualizedUrlPattern = ensureUrlPatternAllowsContentModifier(
                RegExHelper.ensureLineMatch(addAasPath(addSubmodelPath(urlPattern))),
                excludedContentModifiers);
    }


    private static String addSubmodelPath(String urlPattern) {
        return String.format("%s%s%s",
                SUBMODEL_PATH_PATTERN,
                !StringHelper.isBlank(urlPattern) && !urlPattern.startsWith(HttpConstants.PATH_SEPERATOR)
                        ? "/"
                        : "",
                urlPattern);
    }


    private static String removeSubmodelPath(String url) {
        String result = url.replaceFirst(SUBMODEL_PATH_PATTERN, "");
        if (result.endsWith("/")) {
            return result.substring(0, result.length() - 1);
        }
        return result;
    }


    private static String addAasPath(String urlPattern) {
        return String.format("%s%s", AAS_PATH_PATTERN, urlPattern);
    }


    private static String removeAasPath(String url) {
        return url.replaceFirst(AAS_PATH_PATTERN, "");
    }


    private static boolean hasAasPath(String url) {
        return url.matches(String.format("^%s.*", AAS_PATH_PATTERN));
    }


    @Override
    public boolean matchesUrl(HttpRequest httpRequest) {
        return super.matchesUrl(httpRequest) || httpRequest.getPath().matches(contextualizedUrlPattern);
    }


    /**
     * Converts the HTTP request to protocol-agnostic request.
     *
     * @param httpRequest the HTTP request to convert
     * @return the protocol-agnostic request
     * @throws InvalidRequestException if conversion fails
     * @throws IllegalArgumentException if httpRequest is null
     */
    @Override
    public AbstractSubmodelInterfaceRequest parse(HttpRequest httpRequest) throws InvalidRequestException {
        Ensure.requireNonNull(httpRequest, "httpRequest must be non-null");
        boolean withAasContext = hasAasPath(httpRequest.getPath());
        String pattern = withAasContext
                ? contextualizedUrlPattern
                : urlPattern;
        Matcher matcher = Pattern.compile(pattern).matcher(httpRequest.getPath());
        if (matcher.matches()) {
            Map<String, String> urlParameters = RegExHelper.getGroupValues(pattern, httpRequest.getPath());
            httpRequest.setPath(hasAasPath(httpRequest.getPath())
                    ? removeAasPath(removeSubmodelPath(httpRequest.getPath()))
                    : removeSubmodelPath(httpRequest.getPath()));
            AbstractSubmodelInterfaceRequest<R> result = doParse(httpRequest, urlParameters);
            if (withAasContext) {
                result.setAasId(EncodingHelper.base64UrlDecode(urlParameters.get(AAS_ID)));
            }
            result.setSubmodelId(EncodingHelper.base64UrlDecode(urlParameters.get(SUBMODEL_ID)));
            return result;
        }
        throw new InvalidRequestException(String.format("request does neither satisfy URL pattern '%s' nor contextualized URL pattern '%s'", urlPattern, contextualizedUrlPattern));
    }


    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), contextualizedUrlPattern);
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AbstractSubmodelInterfaceRequestMapper<T, R> other = (AbstractSubmodelInterfaceRequestMapper<T, R>) obj;
        return super.equals(other)
                && Objects.equals(this.contextualizedUrlPattern, other.contextualizedUrlPattern);
    }

}
