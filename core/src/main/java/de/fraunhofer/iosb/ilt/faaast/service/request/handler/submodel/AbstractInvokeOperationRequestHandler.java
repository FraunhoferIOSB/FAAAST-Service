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
package de.fraunhofer.iosb.ilt.faaast.service.request.handler.submodel;

import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.ArgumentValidationMode;
import de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetOperationProviderConfig;
import de.fraunhofer.iosb.ilt.faaast.service.exception.MessageBusException;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Response;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.modifier.QueryModifier;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.request.submodel.InvokeOperationRequest;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.InvalidRequestException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.PersistenceException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ResourceNotFoundException;
import de.fraunhofer.iosb.ilt.faaast.service.model.exception.ValueMappingException;
import de.fraunhofer.iosb.ilt.faaast.service.model.messagebus.EventMessage;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.mapper.ElementValueMapper;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.AbstractSubmodelInterfaceRequestHandler;
import de.fraunhofer.iosb.ilt.faaast.service.request.handler.RequestExecutionContext;
import de.fraunhofer.iosb.ilt.faaast.service.util.DeepCopyHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.eclipse.digitaltwin.aas4j.v3.model.Operation;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;
import org.eclipse.digitaltwin.aas4j.v3.model.Qualifier;
import org.eclipse.digitaltwin.aas4j.v3.model.QualifierKind;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;
import org.eclipse.digitaltwin.aas4j.v3.model.impl.DefaultOperationVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Abstract class for handling execution of {@link org.eclipse.digitaltwin.aas4j.v3.model.Operation}s.
 *
 * @param <T> type if the request
 * @param <U> type of the response
 */
public abstract class AbstractInvokeOperationRequestHandler<T extends InvokeOperationRequest<U>, U extends Response> extends AbstractSubmodelInterfaceRequestHandler<T, U> {

    public static final Reference SEMANTIC_ID_QUALIFIER_VALUE_BY_REFERENCE = ReferenceBuilder.global("http://iosb.fraunhofer.de/faaaast/qualifier/operation-value-by-reference");
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInvokeOperationRequestHandler.class);

    @Override
    public U doProcess(T request, RequestExecutionContext context) throws ResourceNotFoundException, InvalidRequestException, PersistenceException {
        Reference reference = new ReferenceBuilder()
                .submodel(request.getSubmodelId())
                .idShortPath(request.getPath())
                .build();
        if (!context.getAssetConnectionManager().hasOperationProvider(reference)) {
            throw new IllegalArgumentException(String.format(
                    "error executing operation - no operation provider defined for reference '%s'",
                    ReferenceHelper.toString(reference)));
        }
        Operation operation = context.getPersistence().getSubmodelElement(reference, QueryModifier.MINIMAL, Operation.class);
        AssetOperationProviderConfig config = context.getAssetConnectionManager().getOperationProvider(reference).getConfig();
        request.setInputArguments(validateAndPrepare(
                operation.getInputVariables(),
                request.getInputArguments(),
                config.getInputValidationMode(),
                ArgumentType.INPUT,
                context));
        request.setInoutputArguments(validateAndPrepare(
                operation.getInoutputVariables(),
                request.getInoutputArguments(),
                config.getInoutputValidationMode(),
                ArgumentType.INOUTPUT,
                context));
        return executeOperation(reference, request, context);
    }


    /**
     * Executes the operation.
     *
     * @param reference reference to the operation element
     * @param request the request
     * @param context the execution context
     * @return the execution result
     */
    protected abstract U executeOperation(Reference reference, T request, RequestExecutionContext context);


    /**
     * Validates and prepares the arguments. Depending on the
     * {@link de.fraunhofer.iosb.ilt.faaast.service.assetconnection.ArgumentValidationMode}, this might include
     * modifying the arguments, e.g. by setting default values.
     *
     * @param definedArguments the arguments defined in the definition of the operatoin
     * @param providedArguments the actual arguments present
     * @param mode the validation mode
     * @param argumentType the type of argumen; needed for property error messages
     * @param context the execution context
     * @return the potentially updated arguments
     * @throws InvalidRequestException if validation of the arguments fails
     */
    protected List<OperationVariable> validateAndPrepare(
                                                         List<OperationVariable> definedArguments,
                                                         List<OperationVariable> providedArguments,
                                                         ArgumentValidationMode mode,
                                                         ArgumentType argumentType,
                                                         RequestExecutionContext context)
            throws InvalidRequestException {
        if (mode == ArgumentValidationMode.NONE) {
            return providedArguments;
        }
        validateArguments(definedArguments, providedArguments, argumentType);

        List<OperationVariable> result = new ArrayList<>();
        for (SubmodelElement definedArgument: definedArguments.stream().map(OperationVariable::getValue).toList()) {
            Optional<SubmodelElement> provided = providedArguments.stream()
                    .map(OperationVariable::getValue)
                    .filter(x -> Objects.equals(definedArgument.getIdShort(), x.getIdShort()))
                    .findFirst();
            SubmodelElement actual = null;
            if (provided.isPresent()) {
                actual = provided.get();
            }
            else {
                if (mode == ArgumentValidationMode.REQUIRE_PRESENT) {
                    throw new InvalidRequestException(String.format("missing required input argument '%s'", definedArgument.getIdShort()));
                }
                if (mode == ArgumentValidationMode.REQUIRE_PRESENT_OR_DEFAULT) {
                    if (argumentType == ArgumentType.INPUT) {
                        actual = handleNotProvidedInputArgument(definedArgument, context);
                    }
                    else if (argumentType == ArgumentType.OUTPUT) {
                        actual = handleNotProvidedOutputArgument(definedArgument, provided.get(), context);
                    }
                }
            }
            result.add(new DefaultOperationVariable.Builder()
                    .value(actual)
                    .build());
        }
        return result;
    }


    /**
     * Publishes an event message on the message bus. Instead of throwing an exception when this fails the error is logged.
     *
     * @param message the event message to publish
     * @param context the execution context
     */
    protected void publishSafe(EventMessage message, RequestExecutionContext context) {
        try {
            context.getMessageBus().publish(message);
        }
        catch (MessageBusException e) {
            LOGGER.warn("Publishing event on message bus failed (reason: {})", e.getMessage(), e);
        }
    }


    private static Optional<Qualifier> findValueProvidedViaReferenceQualifier(SubmodelElement argument) {
        return argument.getQualifiers().stream()
                .filter(x -> Objects.equals(QualifierKind.VALUE_QUALIFIER, x.getKind()))
                .filter(x -> ReferenceHelper.equals(SEMANTIC_ID_QUALIFIER_VALUE_BY_REFERENCE, x.getSemanticId()))
                .findFirst();
    }


    private static <T extends SubmodelElement> T loadValueFromReference(T argument, Reference reference, RequestExecutionContext context) throws InvalidRequestException {
        try {
            SubmodelElement referencedElement = context.getPersistence().getSubmodelElement(reference, QueryModifier.MAXIMAL);
            T result = DeepCopyHelper.deepCopy(argument);
            ElementValueMapper.setValue(result, ElementValueMapper.toValue(referencedElement));
            return result;
        }
        catch (ResourceNotFoundException | PersistenceException | ValueMappingException e) {
            throw new InvalidRequestException(String.format(
                    "Unable to resolve referenced value for argument (name: %s, reference: %s)",
                    argument.getIdShort(),
                    ReferenceHelper.asString(reference)),
                    e);
        }
    }


    private static void writeValueToReference(SubmodelElement argument, Reference reference, RequestExecutionContext context) throws InvalidRequestException {
        try {
            SubmodelElement referencedElement = context.getPersistence().getSubmodelElement(reference, QueryModifier.MAXIMAL);
            ElementValueMapper.setValue(referencedElement, ElementValueMapper.toValue(argument));
            context.getPersistence().update(reference, referencedElement);
        }
        catch (ResourceNotFoundException | PersistenceException | ValueMappingException e) {
            throw new InvalidRequestException(String.format(
                    "Unable to write operation output ot referenced element (name: %s, reference: %s)",
                    argument.getIdShort(),
                    ReferenceHelper.asString(reference)),
                    e);
        }
    }


    private void validateArguments(List<OperationVariable> definedArguments,
                                   List<OperationVariable> providedArguments,
                                   ArgumentType argumentType)
            throws InvalidRequestException {
        if (definedArguments.stream().anyMatch(Objects::isNull)
                || definedArguments.stream().map(OperationVariable::getValue).anyMatch(Objects::isNull)) {
            throw new InvalidRequestException(String.format(
                    "operation definition contains invalid %s argument definition(s)",
                    argumentType.getName()));
        }
        if (providedArguments.stream().anyMatch(Objects::isNull)
                || providedArguments.stream().map(OperationVariable::getValue).anyMatch(Objects::isNull)) {
            throw new InvalidRequestException(String.format(
                    "invalid operation %s argument(s) - must be non-null",
                    argumentType.getName()));
        }
        List<String> definedArgumentNames = definedArguments.stream().map(x -> x.getValue().getIdShort()).distinct().toList();
        List<String> unkownProvidedArguments = providedArguments.stream()
                .map(x -> x.getValue().getIdShort())
                .filter(Predicate.not(definedArgumentNames::contains))
                .distinct()
                .toList();
        if (!unkownProvidedArguments.isEmpty()) {
            throw new InvalidRequestException(String.format(
                    "unknown %s argument(s): %s",
                    argumentType.getName(),
                    String.join(", ", unkownProvidedArguments)));
        }
        for (SubmodelElement definedArgument: definedArguments.stream().map(OperationVariable::getValue).toList()) {
            List<SubmodelElement> provided = providedArguments.stream()
                    .map(OperationVariable::getValue)
                    .filter(x -> Objects.equals(definedArgument.getIdShort(), x.getIdShort())).toList();
            if (provided.size() > 1) {
                throw new InvalidRequestException(String.format(
                        "duplicate %s argument '%s'",
                        argumentType.getName(),
                        definedArgument.getIdShort()));
            }
        }

    }


    private SubmodelElement handleNotProvidedInputArgument(SubmodelElement definedArgument, RequestExecutionContext context) throws InvalidRequestException {
        Optional<Qualifier> qualifier = findValueProvidedViaReferenceQualifier(definedArgument);
        if (qualifier.isPresent()) {
            return loadValueFromReference(definedArgument, qualifier.get().getValueId(), context);
        }
        return DeepCopyHelper.deepCopy(definedArgument);
    }


    private SubmodelElement handleNotProvidedOutputArgument(SubmodelElement definedArgument, SubmodelElement providedArgument, RequestExecutionContext context)
            throws InvalidRequestException {
        Optional<Qualifier> qualifier = findValueProvidedViaReferenceQualifier(definedArgument);
        if (qualifier.isPresent()) {
            writeValueToReference(providedArgument, qualifier.get().getValueId(), context);
            return providedArgument;
        }
        return DeepCopyHelper.deepCopy(definedArgument);
    }

    /**
     * Describes type of argument.
     */
    protected enum ArgumentType {
        INPUT,
        INOUTPUT,
        OUTPUT;

        String getName() {
            return this.name().toLowerCase();
        }
    }

}
