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
package de.fraunhofer.iosb.ilt.faaast.service.model.aasx;

import java.util.List;
import java.util.Objects;


/**
 * Model class for package description.
 */
public class PackageDescription {

    private String packageId;
    private List<String> aasId;

    public String getPackageId() {
        return packageId;
    }


    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }


    public List<String> getAasId() {
        return aasId;
    }


    public void setAasId(List<String> aasId) {
        this.aasId = aasId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PackageDescription that = (PackageDescription) o;
        return Objects.equals(packageId, that.packageId) && Objects.equals(aasId, that.aasId);
    }


    @Override
    public int hashCode() {
        return Objects.hash(packageId, aasId);
    }
}
