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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http;

import static de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.auth.SharedAttributes.ACL;
import static de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod.GET;

import de.fraunhofer.iosb.ilt.faaast.service.ServiceContext;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.SerializationException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.MethodNotAllowedException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.exception.UnauthorizedException;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.model.HttpRequest;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.request.RequestMappingManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.response.ResponseMappingManager;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.FormulaEvaluator;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.security.filter.ApiGateway;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization.HttpJsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aas.GetAssetAdministrationShellRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.aasrepository.GetAllAssetAdministrationShellsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetAllConceptDescriptionsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetAllConceptDescriptionsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.conceptdescription.GetConceptDescriptionByIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.GetSubmodelRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.GetAllSubmodelsByIdShortRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.GetAllSubmodelsBySemanticIdRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodelrepository.GetAllSubmodelsRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.UnsupportedModifierException;
import de.fraunhofer.iosb.ilt.faaast.service.model.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.AccessPermissionRule;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.LogicalExpression;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.Value;
import de.fraunhofer.iosb.ilt.faaast.service.util.Ensure;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.digitaltwin.aas4j.v3.model.Key;
import org.eclipse.digitaltwin.aas4j.v3.model.Message;
import org.eclipse.digitaltwin.aas4j.v3.model.MessageTypeEnum;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.jetty.server.Response;


/**
 * HTTP handler that actually handles all requests to the endpoint by finding the matching request class, deserializing
 * the request, executing it using the serviceContext and
 * serializing the result.
 */
public class RequestHandlerServlet extends HttpServlet {

    private final HttpEndpoint endpoint;
    private final HttpEndpointConfig config;
    private final ServiceContext serviceContext;
    private final RequestMappingManager requestMappingManager;
    private final ResponseMappingManager responseMappingManager;
    private final HttpJsonApiSerializer serializer;
    private final ApiGateway apiGateway;

    public RequestHandlerServlet(HttpEndpoint endpoint, HttpEndpointConfig config, ServiceContext serviceContext) {
        Ensure.requireNonNull(endpoint, "endpoint must be non-null");
        Ensure.requireNonNull(config, "config must be non-null");
        Ensure.requireNonNull(serviceContext, "serviceContext must be non-null");
        this.endpoint = endpoint;
        this.config = config;
        this.serviceContext = serviceContext;
        this.requestMappingManager = new RequestMappingManager(serviceContext);
        this.responseMappingManager = new ResponseMappingManager(serviceContext);
        this.serializer = new HttpJsonApiSerializer();
        this.apiGateway = Objects.nonNull(config.getJwkProvider()) ? new ApiGateway() : null;
    }


    private void doThrow(Exception e) throws ServletException {
        throw new ServletException(e);
    }


    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!request.getRequestURI().startsWith(endpoint.getPathPrefix())) {
            doThrow(new ResourceNotFoundException(String.format("Resource not found '%s'", request.getRequestURI())));
        }
        String url = request.getRequestURI().replaceFirst(endpoint.getPathPrefix(), "");
        HttpMethod method = null;
        try {
            method = HttpMethod.valueOf(request.getMethod());
        }
        catch (IllegalArgumentException e) {
            doThrow(new MethodNotAllowedException(
                    String.format("Unknown method '%s'", request.getMethod()),
                    e));
        }
        HttpRequest httpRequest = HttpRequest.builder()
                .path(url.replaceAll("/$", ""))
                .query(request.getQueryString())
                .body(request.getInputStream().readAllBytes())
                .method(method)
                .charset(request.getCharacterEncoding())
                .headers(Collections.list(request.getHeaderNames()).stream()
                        .collect(Collectors.toMap(
                                x -> x,
                                request::getHeader)))
                .build();

        try {
            if (Objects.nonNull(apiGateway) && GET == httpRequest.getMethod()) {
                modifyRequest(request, httpRequest);
            }
            executeAndSend(request, response, requestMappingManager.map(httpRequest));
        }
        catch (Exception e) {
            doThrow(e);
        }
    }


    private void modifyRequest(HttpServletRequest servletRequest, HttpRequest request)
            throws SerializationException, InvalidRequestException {
        var wouldHaveBeen = requestMappingManager.map(request);
        List<AccessPermissionRule> acl = (List<AccessPermissionRule>) servletRequest.getAttribute(ACL.getName());
        if (acl.isEmpty()) {
            throw new UnauthorizedException("unauthorized. TODO decide if 404 or 403");
        }
        List<LogicalExpression> formulas = acl.stream().map(AccessPermissionRule::getFormula).toList();
        LogicalExpression formulasOr = new LogicalExpression();
        formulasOr.set$or(formulas);

        if (wouldHaveBeen instanceof GetAllAssetAdministrationShellsRequest) {
            request.setPath("query/shells");
            request.setBody(serializer.write(formulasOr));
        }
        else if (wouldHaveBeen instanceof GetAllSubmodelsRequest) {
            request.setPath("query/submodels");
            request.setBody(serializer.write(formulasOr));
        }
        else if (wouldHaveBeen instanceof GetAllConceptDescriptionsRequest) {
            request.setPath("query/concept-descriptions");
            request.setBody(serializer.write(formulasOr));
        }

        else if (wouldHaveBeen instanceof GetAssetAdministrationShellRequest getAssetAdministrationShellRequest) {
            setQuery(request, "aas", "shells", "id", getAssetAdministrationShellRequest.getId(), formulasOr);
        }
        else if (wouldHaveBeen instanceof GetAllAssetAdministrationShellsByIdShortRequest getAllAssetAdministrationShellsByIdShortRequest) {
            setQuery(request, "aas", "shells", "idShort", getAllAssetAdministrationShellsByIdShortRequest.getIdShort(), formulasOr);
        }

        else if (wouldHaveBeen instanceof GetSubmodelRequest getSubmodelRequest) {
            setQuery(request, "sm", "submodels", "id", getSubmodelRequest.getSubmodelId(), formulasOr);
        }
        else if (wouldHaveBeen instanceof GetAllSubmodelsByIdShortRequest getAllSubmodelsByIdShortRequest) {
            setQuery(request, "sm", "submodels", "idShort", getAllSubmodelsByIdShortRequest.getIdShort(), formulasOr);
        }
        else if (wouldHaveBeen instanceof GetAllSubmodelsBySemanticIdRequest getAllSubmodelsBySemanticIdRequest) {
            setQuery(request, "sm", "submodels", "semanticId", getSemanticIdString(getAllSubmodelsBySemanticIdRequest.getSemanticId()), formulasOr);
        }

        else if (wouldHaveBeen instanceof GetConceptDescriptionByIdRequest getConceptDescriptionByIdRequest) {
            setQuery(request, "cd", "concept-descriptions", "id", getConceptDescriptionByIdRequest.getId(), formulasOr);
        }
        else if (wouldHaveBeen instanceof GetAllConceptDescriptionsByIdShortRequest getAllConceptDescriptionsByIdShortRequest) {
            setQuery(request, "cd", "concept-descriptions", "idShort", getAllConceptDescriptionsByIdShortRequest.getIdShort(), formulasOr);
        }
    }


    private void setQuery(HttpRequest request, String res, String resource, String pattern, String id, LogicalExpression formula)
            throws SerializationException, UnsupportedModifierException {
        request.setPath(String.format("query/%s", resource));
        LogicalExpression parentFormula = new LogicalExpression();
        parentFormula.set$and(List.of(formEq(String.format("$%s#%s", res, pattern), id), formula));
        request.setBody(serializer.write(parentFormula));
    }


    private void checkRequestSupportedByProfiles(de.fraunhofer.iosb.ilt.faaast.service.model.api.Request<? extends Response> apiRequest) throws InvalidRequestException {
        if (Objects.isNull(config.getProfiles()) || config.getProfiles().isEmpty()) {
            return;
        }
        config.getProfiles().stream()
                .flatMap(x -> x.getSupportedRequests().stream())
                .filter(x -> Objects.equals(x, apiRequest.getClass()))
                .findAny()
                .orElseThrow(() -> new InvalidRequestException(String.format(
                        "'%s' not supported on this server",
                        apiRequest.getClass().getSimpleName())));
    }


    private void executeAndSend(HttpServletRequest request, HttpServletResponse response,
                                de.fraunhofer.iosb.ilt.faaast.service.model.api.Request<? extends Response> apiRequest)
            throws Exception {
        if (Objects.isNull(apiRequest)) {
            throw new InvalidRequestException("empty API request");
        }
        checkRequestSupportedByProfiles(apiRequest);
        de.fraunhofer.iosb.ilt.faaast.service.model.api.Response apiResponse = null;
        if (Objects.nonNull(apiGateway)) {
            apiResponse = handleResponseWithAcl(request, apiRequest);
        }
        else {
            apiResponse = serviceContext.execute(endpoint, apiRequest);
        }
        if (Objects.isNull(apiResponse)) {
            throw new ServletException("empty API response");
        }
        if (isSuccessful(apiResponse)) {
            responseMappingManager.map(apiRequest, apiResponse, response);
        }
        else {
            HttpHelper.sendJson(response, apiResponse.getStatusCode(), serializer.write(apiResponse.getResult()));
        }
    }


    private static boolean isSuccessful(de.fraunhofer.iosb.ilt.faaast.service.model.api.Response response) {
        return Objects.nonNull(response)
                && response.getStatusCode().isSuccess()
                && Objects.nonNull(response.getResult())
                && Optional.ofNullable(response.getResult().getMessages())
                        .orElse(List.of())
                        .stream()
                        .map(Message::getMessageType)
                        .noneMatch(x -> Objects.equals(x, MessageTypeEnum.ERROR) || Objects.equals(x, MessageTypeEnum.EXCEPTION));
    }


    private de.fraunhofer.iosb.ilt.faaast.service.model.api.Response handleResponseWithAcl(HttpServletRequest request,
                                                                                           de.fraunhofer.iosb.ilt.faaast.service.model.api.Request<? extends Response> apiRequest)
            throws ServletException {
        List<AccessPermissionRule> rules = ((List<AccessPermissionRule>) request.getAttribute(ACL.getName()));
        if (rules.stream().noneMatch(rule -> FormulaEvaluator.evaluate(rule.getFormula(), new HashMap<>()))) {
            doThrow(new UnauthorizedException(
                    String.format("User not authorized '%s'", request.getRequestURI())));
        }

        return serviceContext.execute(endpoint, apiRequest);
    }


    private String getSemanticIdString(Reference semanticId) {
        return Optional.ofNullable(ReferenceHelper.getRoot(semanticId))
                .map(Key::getValue)
                .orElse(null);
    }


    private LogicalExpression formEq(String left, String right) {
        LogicalExpression eqFormula = new LogicalExpression();
        Value smId = new Value();
        smId.set$strVal(left);
        Value identifier = new Value();
        identifier.set$strVal(right);

        eqFormula.set$eq(List.of(smId, identifier));
        return eqFormula;
    }
}
