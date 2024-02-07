package de.fraunhofer.iosb.ilt.faaast.service.eventlistener.mqtt;

import de.fraunhofer.iosb.ilt.faaast.service.exception.EventListenerException;
import de.fraunhofer.iosb.ilt.faaast.service.model.value.ElementValue;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class RuleHandler {
    private ScriptEngineManager manager;
    private ScriptEngine engine;

    public RuleHandler() {
        manager = new ScriptEngineManager();
        engine = manager.getEngineByName("JavaScript");
    }

    public void handle(Rule rule, ElementValue value) throws EventListenerException {
        try {
            Object result = engine.eval("console.log('Hello from JavaScript');");
        } catch (ScriptException e) {
            throw new EventListenerException(e);
        }
    }

    public boolean match(Rule rule, Reference reference) {
        return rule.elements.contains(reference);
    }
}
