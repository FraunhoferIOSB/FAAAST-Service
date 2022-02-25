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
package de.fraunhofer.iosb.ilt.faaast.service.util;

import io.adminshell.aas.v3.dataformat.DeserializationException;
import io.adminshell.aas.v3.dataformat.SerializationException;
import io.adminshell.aas.v3.dataformat.json.JsonDeserializer;
import io.adminshell.aas.v3.dataformat.json.JsonSerializer;
import io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment;
import io.adminshell.aas.v3.model.Referable;
import java.util.ArrayList;
import java.util.List;


/**
 * Helper class with methods to create deep copies of
 * <p>
 * <ul>
 * <li>{@link io.adminshell.aas.v3.model.Identifiable}
 * <li>{@link io.adminshell.aas.v3.model.Referable}
 * <li>{@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment}
 * </ul>
 * <p>
 */
public class DeepCopyHelper {

    private DeepCopyHelper() {}


    /**
     * Create a deep copy of a
     * {@link io.adminshell.aas.v3.model.AssetAdministrationShellEnvironment} object
     *
     * @param env the asset administration shell environment which should be deep copied
     * @return a deep copied instance of the asset administration shell environment
     * @throws SerializationException
     * @throws DeserializationException
     */
    public static AssetAdministrationShellEnvironment deepCopy(AssetAdministrationShellEnvironment env) throws SerializationException, DeserializationException {
        return new JsonDeserializer().read(new JsonSerializer().write(env));
    }


    /**
     * Create a deep copy of a
     * {@link io.adminshell.aas.v3.model.Referable} object
     *
     * @param referable which should be deep copied
     * @param outputClass of the referable
     * @param <T> type of the referable
     * @return the deep copied referable
     */
    public static <T extends Referable> T deepCopy(Referable referable, Class<T> outputClass) {
        try {
            Referable deepCopy = new JsonDeserializer().readReferable(new JsonSerializer().write(referable), outputClass);
            if (deepCopy.getClass().isAssignableFrom(outputClass)) {
                return (T) deepCopy;
            }
        }
        catch (SerializationException | DeserializationException e) {
            return null;
        }
        return null;
    }


    /**
     * Create a deep copy of a list of
     * {@link io.adminshell.aas.v3.model.Referable} objects
     *
     * @param referableList list with referables which should be deep copied
     * @param outputClass of the referables
     * @param <T> type of the referables
     * @return a list with deep copied referables
     */
    public static <T extends Referable> List<T> deepCopy(List<T> referableList,
                                                         Class<T> outputClass) {
        List<T> deepCopyList = new ArrayList<>();
        for (Referable referable: referableList) {
            deepCopyList.add(deepCopy(referable, outputClass));
        }
        return deepCopyList;
    }
}
