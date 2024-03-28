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

import de.fraunhofer.iosb.ilt.faaast.service.eventlistener.mqtt.actions.CallOperation;
import de.fraunhofer.iosb.ilt.faaast.service.exception.EventListenerException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.PropertyValue;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Handler executing the rule.
 */
public class RuleHandler {
    private ScriptEngineManager manager;
    private ScriptEngine engine;
    private String PATTERN_ELEMENTS = "\\$(.*?)\\$";
    private String PATTERN_ACTIONS = "\\!(.*?)\\!";
    private HttpProvider httpProvider;
    private static final Logger LOGGER = LoggerFactory.getLogger(RuleHandler.class);

    public RuleHandler(String baseUrl) {
        this.manager = new ScriptEngineManager();
        this.engine = this.manager.getEngineByName("JavaScript");
        this.httpProvider = new HttpProvider(baseUrl);
    }


    /**
     * Evaluates the rule and executes it.
     *
     * @param rule
     * @param value
     * @param reference
     * @throws EventListenerException
     */
    public void handle(Rule rule, ElementValue value, Reference reference) throws EventListenerException {
        try {
            String ruleForEngine = initializeRule(rule, value);
            if (!rule.getElements().contains(reference)) {
                return;
            }
            engine.eval(ruleForEngine);
            if (Objects.isNull(engine.get("result"))) {
                LOGGER.debug("Rule was not activated");
                return;
            }
            else {
                LOGGER.debug("Rule was activated");
                rule.getActions().get(0).execute(httpProvider);
            }
            //reset engine
            this.engine = this.manager.getEngineByName("JavaScript");
        }
        catch (ScriptException e) {
            throw new EventListenerException(e);
        }
    }


    private String initializeRule(Rule rule, ElementValue value) throws EventListenerException {
        String input = rule.getExpression();
        Pattern pattern = Pattern.compile(PATTERN_ELEMENTS);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String referenceString = matcher.group(1);
            Reference reference = ReferenceHelper.parse(referenceString);
            if (!rule.getElements().contains(reference)) {
                rule.addElement(reference);
            }
            if (value.getClass().isAssignableFrom(PropertyValue.class)) {
                PropertyValue propertyValue = (PropertyValue) value;
                input = input.replace("$" + referenceString + "$", propertyValue.getValue().getValue().toString());
            }
            else {
                throw new EventListenerException("Currently only property values are supported");
            }
        }
        pattern = Pattern.compile(PATTERN_ACTIONS);
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            String referenceString = matcher.group(1);
            Reference reference = ReferenceHelper.parse(referenceString);
            CallOperation action = new CallOperation(reference);
            if (!rule.getActions().contains(action)) {
                rule.addAction(action);
            }
            input = input.replace("!" + referenceString + "!", "var result=true");
        }
        return (input);
    }
}
