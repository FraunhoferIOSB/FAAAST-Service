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
package de.fraunhofer.iosb.ilt.faaast.service.submodeltemplate.timeseries.provider;

import org.eclipse.digitaltwin.aas4j.v3.model.builder.ExtendableBuilder;


/**
 * Abstract base configuration for any implementation of {@link InternalSegmentProvider}.
 *
 * @param <T> type of the matching provider
 */
public abstract class InternalSegmentProviderConfig<T extends InternalSegmentProvider> extends SegmentProviderConfig<T> {

    protected abstract static class AbstractBuilder<T extends InternalSegmentProviderConfig, B extends AbstractBuilder<T, B>>
            extends ExtendableBuilder<T, B> {

    }
}
