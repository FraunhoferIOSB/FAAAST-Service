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
package de.fraunhofer.iosb.ilt.faaast.service.assetconnection;

import de.fraunhofer.iosb.ilt.faaast.service.util.LambdaExceptionHelper;
import io.adminshell.aas.v3.model.OperationVariable;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.stream.Stream;


/**
 * An AssetOperationProvider provides methods to invoke AAS operations on an
 * asset; either synchronous or asynchronous
 */
public interface AssetOperationProvider extends AssetProvider {

    /**
     * Invokes as operation synchronously
     *
     * @param input input parameters
     * @param inoutput inoutput parameters, i.e. parameters that are passed as
     *            input to the operation but can be modified while execution
     * @return output variables of the operation
     * @throws
     * de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException
     *             when invoking operation on asset connection fails
     * @throws IllegalArgumentException if provided inoutput arguments do not
     *             match actual inoutput arguments
     */
    public default OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
        final String BASE_ERROR_MSG = "inoutput argument mismatch";
        final AtomicReference<OperationVariable[]> result = new AtomicReference<>();
        final AtomicReference<OperationVariable[]> modifiedInoutput = new AtomicReference<>();
        CountDownLatch condition = new CountDownLatch(1);
        invokeAsync(input, inoutput, (x, y) -> {
            result.set(x);
            modifiedInoutput.set(y);
            condition.countDown();
        });
        try {
            condition.await();
        }
        catch (InterruptedException x) {
            Thread.currentThread().interrupt();
            throw new AssetConnectionException("invoking operation failed because of timeout", x);
        }
        if (inoutput == null && result.get() != null && result.get().length > 0) {
            throw new IllegalArgumentException(String.format("%s - provided: none, actual: %d", BASE_ERROR_MSG, result.get().length));
        }
        if (result.get() == null && inoutput != null && inoutput.length > 0) {
            throw new IllegalArgumentException(String.format("%s - provided: %d, actual: none", BASE_ERROR_MSG, inoutput.length));
        }
        if (inoutput != null) {
            if (inoutput.length != modifiedInoutput.get().length) {
                throw new IllegalArgumentException(String.format("%s - provided: %d, actual: %d", BASE_ERROR_MSG, inoutput.length, result.get().length));
            }
            for (int i = 0; i < inoutput.length; i++) {
                final OperationVariable variable = inoutput[i];
                inoutput[i] = Stream.of(result.get())
                        .filter(x -> Objects.equals(x.getValue().getIdShort(), variable.getValue().getIdShort()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
                                String.format("%s - variable provided but not found in actual ouput (idShort: %s)",
                                        BASE_ERROR_MSG,
                                        variable.getValue().getIdShort())));
            }
        }
        return result.get();
    }


    /**
     * Invokes as operation asynchronously
     *
     * @param input input parameters
     * @param inoutput inoutput parameters, i.e. parameters that are passed as
     *            input to the operation but can be modified while execution
     * @param callback callback handler that is called when the operation is
     *            finished providing the result and inoutput variables
     * @throws
     * de.fraunhofer.iosb.ilt.faaast.service.assetconnection.AssetConnectionException
     *             when invoking operation on asset connection fails
     */
    public default void invokeAsync(OperationVariable[] input, OperationVariable[] inoutput, BiConsumer<OperationVariable[], OperationVariable[]> callback)
            throws AssetConnectionException {
        CompletableFuture.supplyAsync(LambdaExceptionHelper.rethrowSupplier(() -> invoke(input, inoutput))).thenAccept(x -> callback.accept(x, inoutput));

    }
}
