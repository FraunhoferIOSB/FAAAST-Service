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
package de.fraunhofer.iosb.ilt.faaast.service.model.security.parser;

import de.fraunhofer.iosb.ilt.faaast.service.model.query.json.ObjectItem;
import de.fraunhofer.iosb.ilt.faaast.service.model.query.parser.Parser;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object.AccessObject;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object.FragmentObject;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object.RouteObject;
import de.fraunhofer.iosb.ilt.faaast.service.model.security.accessrule.object.referable.ReferableObjectFactory;


public class ObjectItemToAccessObjectParser implements Parser<ObjectItem, AccessObject> {
    @Override
    public AccessObject parse(ObjectItem objectItem) {
        if (notNullNorBlank(objectItem.getRoute())) {
            return new RouteObject(objectItem.getRoute());
        }
        else if (notNullNorBlank(objectItem.getIdentifiable())) {
            return ReferableObjectFactory.build(objectItem.getIdentifiable());
        }
        else if (notNullNorBlank(objectItem.getReferable())) {
            return ReferableObjectFactory.build(objectItem.getReferable());
        }
        else if (notNullNorBlank(objectItem.getFragment())) {
            return new FragmentObject(objectItem.getFragment());
        }

        throw new IllegalArgumentException(String.format("ObjectItem unknown: %s", objectItem));
    }


    private boolean notNullNorBlank(String s) {
        return s != null && !s.isBlank();
    }
}
