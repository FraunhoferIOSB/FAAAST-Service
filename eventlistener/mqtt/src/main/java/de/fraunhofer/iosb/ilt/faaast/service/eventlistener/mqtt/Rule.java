package de.fraunhofer.iosb.ilt.faaast.service.eventlistener.mqtt;

import de.fraunhofer.iosb.ilt.faaast.service.eventlistener.mqtt.actions.Action;
import de.fraunhofer.iosb.ilt.faaast.service.eventlistener.mqtt.actions.CallOperation;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceBuilder;
import de.fraunhofer.iosb.ilt.faaast.service.util.ReferenceHelper;
import de.fraunhofer.iosb.ilt.faaast.service.util.RegExHelper;
import org.eclipse.digitaltwin.aas4j.v3.model.Reference;
import org.eclipse.digitaltwin.aas4j.v3.model.SubmodelElement;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule {

    List<Reference> elements;
    List<Action> actions;
    private String PATTERN_ELEMENTS = "\\$(.*?)\\$";
    private String PATTERN_ACTIONS = "\\!(.*?)\\!";

    public Rule(String rule) {
        elements = new ArrayList<>();
        actions = new ArrayList<>();
        fromString(rule);
    }

    private void addElement(Reference reference) {
        elements.add(reference);
    }

    private void addAction(Action action) {
        actions.add(action);
    }

    public void fromString(String input) {
        Pattern pattern = Pattern.compile(PATTERN_ELEMENTS);
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            addElement(ReferenceHelper.parse(matcher.group(1)));
        }
        pattern = Pattern.compile(PATTERN_ACTIONS);
        matcher = pattern.matcher(input);
        while (matcher.find()) {
            addAction(new CallOperation(ReferenceHelper.parse(matcher.group(1))));
        }
    }
}
