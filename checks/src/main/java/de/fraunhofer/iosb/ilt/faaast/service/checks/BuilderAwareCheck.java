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
package de.fraunhofer.iosb.ilt.faaast.service.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.Stack;


/**
 * Abstract base class for checkstyle checks that are aware if an element is inside a builder class or not.
 */
public abstract class BuilderAwareCheck extends AbstractCheck {

    private static final String CLASSNAME_PLACEHOLDER = "${classname}";

    private String builderNameRegex = "(Abstract)?(${classname})?Builder";
    protected boolean ignoreBuilder = true;

    protected final Stack<DetailAST> classHierarchy = new Stack<>();
    protected boolean currentlyInBuilder = false;

    public void setBuilderNameRegex(String builderNameRegex) {
        this.builderNameRegex = builderNameRegex;
    }


    public void setIgnoreBuilder(boolean value) {
        ignoreBuilder = value;
    }


    @Override
    public void leaveToken(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.CLASS_DEF: {
                classHierarchy.pop();
                updateCurrentlyInBuilder();
                break;
            }
            default:
        }
    }


    @Override
    public void visitToken(DetailAST ast) {
        switch (ast.getType()) {
            case TokenTypes.CLASS_DEF:
            case TokenTypes.INTERFACE_DEF:
            case TokenTypes.ENUM_DEF: {
                classHierarchy.push(ast);
                updateCurrentlyInBuilder();
                break;
            }
            default:
        }
    }


    /**
     * Checks if current AST element is defined inside a class
     *
     * @return true if current AST element is inside a class, false otherwise
     */
    protected boolean currentlyInClass() {
        return !classHierarchy.isEmpty() && classHierarchy.peek().getType() == TokenTypes.CLASS_DEF;
    }


    /**
     * Checks if current AST element is defined inside an enum
     *
     * @return true if current AST element is inside an enum, false otherwise
     */
    protected boolean currentlyInEnum() {
        return !classHierarchy.isEmpty() && classHierarchy.peek().getType() == TokenTypes.ENUM_DEF;
    }


    /**
     * Checks if current AST element is defined inside an interface
     *
     * @return true if current AST element is inside an interface, false otherwise
     */
    protected boolean currentlyInInterface() {
        return !classHierarchy.isEmpty() && classHierarchy.peek().getType() == TokenTypes.INTERFACE_DEF;
    }


    /**
     * Checks if a given class name matches the defined builder pattern
     *
     * @param className the classname to check
     * @return true if className matches the builder pattern, false otherwise
     */
    protected boolean matchesBuilderPattern(String className) {
        String parentClassName = classHierarchy.size() > 1
                ? classHierarchy.get(classHierarchy.size() - 2).findFirstToken(TokenTypes.IDENT).getText()
                : "";
        String regex = builderNameRegex.replace(CLASSNAME_PLACEHOLDER, parentClassName);
        return className.matches(regex);
    }


    private void updateCurrentlyInBuilder() {
        currentlyInBuilder = !classHierarchy.isEmpty() && matchesBuilderPattern(classHierarchy.peek().findFirstToken(TokenTypes.IDENT).getText());
    }
}
