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
package de.fraunhofer.iosb.ilt.faaast.service.endpoint.http.serialization;

import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonApiSerializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.ServiceDescription;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.util.StdDateFormat;


/**
 * JSON serializer including HTTP specific functionality.
 */
public class HttpJsonApiSerializer extends JsonApiSerializer {

    @Override
    protected JsonMapper modifyMapper(JsonMapper mapper) {
        SimpleModule module = new SimpleModule();
        module.addSerializer(ServiceDescription.class, new ServiceDescriptionSerializer());

        return super.modifyMapper(mapper).rebuild()
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .defaultDateFormat(new StdDateFormat().withColonInTimeZone(true))
                .addModule(module)
                .build();
    }
}
