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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;


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
     */
    public default OperationVariable[] invoke(OperationVariable[] input, OperationVariable[] inoutput) throws AssetConnectionException {
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
        catch (InterruptedException e) {
            throw new AssetConnectionException("invoking operation failed because of timeout", e);
        }
        // TODO check if 1:1 assignment of each value is needed
        inoutput = modifiedInoutput.get();
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
        CompletableFuture.supplyAsync(LambdaExceptionHelper.rethrowSupplier(() -> invoke(input, inoutput))).thenAccept(x -> {
            callback.accept(x, inoutput);
        });

    }
}
