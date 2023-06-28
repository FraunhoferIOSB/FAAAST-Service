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
package de.fraunhofer.iosb.ilt.faaast.service.starter.fixtures;

import de.fraunhofer.iosb.ilt.faaast.service.messagebus.MessageBusConfig;


/**
 * Configuration class for {@link DummyMessageBus}.
 */
public class DummyMessageBusConfig extends MessageBusConfig<DummyMessageBus> {

    private DummyNestedClass nes_ted;
    private DummyNestedClass ambi;
    private int ab;
    private int c;
    private int c_d;
    private int ambi_guity;

    public DummyMessageBusConfig() {
        nes_ted = new DummyNestedClass();
        ambi = new DummyNestedClass();
    }


    public void setAb(int value) {
        ab = value;
    }


    public int getAb() {
        return ab;
    }


    public void setC(int value) {
        c = value;
    }


    public int getC() {
        return c;
    }


    public void setC_d(int value) {
        c_d = value;
    }


    public int getC_d() {
        return c_d;
    }


    public void setNes_ted(DummyNestedClass value) {
        nes_ted = value;
    }


    public DummyNestedClass getNes_ted() {
        return nes_ted;
    }


    public void setAmbi(DummyNestedClass value) {
        ambi = value;
    }


    public DummyNestedClass getAmbi() {
        return ambi;
    }


    public void setAmbi_guity(int value) {
        ambi_guity = value;
    }


    public int getAmbi_guity() {
        return ambi_guity;
    }


    public static Builder builder() {
        return new Builder();
    }

    private abstract static class AbstractBuilder<T extends DummyMessageBusConfig, B extends AbstractBuilder<T, B>>
            extends MessageBusConfig.AbstractBuilder<DummyMessageBus, T, B> {

        public B ab(int value) {
            getBuildingInstance().setAb(value);
            return getSelf();
        }


        public B c(int value) {
            getBuildingInstance().setC(value);
            return getSelf();
        }


        public B c_d(int value) {
            getBuildingInstance().setC_d(value);
            return getSelf();
        }


        public B ambi_guity(int value) {
            getBuildingInstance().setAmbi_guity(value);
            return getSelf();
        }


        public B nes_ted(DummyNestedClass value) {
            getBuildingInstance().setNes_ted(value);
            return getSelf();
        }


        public B ambi(DummyNestedClass value) {
            getBuildingInstance().setAmbi(value);
            return getSelf();
        }
    }

    public static class Builder extends AbstractBuilder<DummyMessageBusConfig, Builder> {
        @Override
        protected Builder getSelf() {
            return this;
        }


        @Override
        protected DummyMessageBusConfig newBuildingInstance() {
            return new DummyMessageBusConfig();
        }
    }
}
