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
package de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.mixins.value;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.serializer.OperationVariableCollectionValueSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.Message;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.operation.OperationResult;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.OperationVariable;


/**
 * Mixing for {@link OperationResult}.
 */
public interface OperationResultValueMixin {

    @JsonIgnore
    public List<Message> getMessages();


    @JsonSerialize(using = OperationVariableCollectionValueSerializer.class)
    public List<OperationVariable> getInoutputArguments();


    @JsonSerialize(using = OperationVariableCollectionValueSerializer.class)
    public List<OperationVariable> getOutputArguments();
}
