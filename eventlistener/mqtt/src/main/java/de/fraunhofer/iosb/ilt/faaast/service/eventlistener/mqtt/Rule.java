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
package de.fraunhofer.iosb.ilt.faaast.service.eventlistener.mqtt;

import de.fraunhofer.iosb.ilt.faaast.service.eventlistener.mqtt.actions.Action;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;


/**
 * A rule executed by the Script Engine.
 */
public class Rule {

    private List<Reference> elements;
    private List<Action> actions;

    public List<Reference> getElements() {
        return elements;
    }


    public List<Action> getActions() {
        return actions;
    }


    public String getExpression() {
        return expression;
    }


    public void setExpression(String expression) {
        this.expression = expression;
    }

    private String expression;

    /**
     * Initialize the rule.
     *
     * @param rule
     */
    public Rule(String rule) {
        this.elements = new ArrayList<>();
        this.actions = new ArrayList<>();
        this.expression = rule;
    }


    /**
     * Adds an SubmodelElement to the rule.
     *
     * @param reference
     */
    public void addElement(Reference reference) {
        elements.add(reference);
    }


    /**
     * Adds an action to the rule.
     *
     * @param action
     */
    public void addAction(Action action) {
        actions.add(action);
    }

}
