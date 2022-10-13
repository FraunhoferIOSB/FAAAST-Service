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
package de.fraunhofer.iosb.ilt.faaast.service.checks.util;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import java.util.regex.Pattern;


/**
 * Utility class providing common operations for handling interface classes within checkstyle checks.
 */
public class InterfaceHelper {

    private static final Pattern GETTER_PATTERN = Pattern.compile("^(is|get)[A-Z].*");
    private static final Pattern SETTER_PATTERN = Pattern.compile("^set[A-Z].*");
    private static final int SETTER_GETTER_MAX_CHILDREN = 7;

    private InterfaceHelper() {}


    /**
     * Checks if given AST element represents a setter or getter method.
     *
     * @param ast AST element to check
     * @return true if ast represents a setter or getter method, false otherwise
     */
    public static boolean isGetterOrGetterMethod(final DetailAST ast) {
        return isSetterMethod(ast) || isGetterMethod(ast);
    }


    /**
     * Checks if given AST element represents a getter method. To be classified as getter method, a method must match
     * the naming conventions, have a non-void return type, take no parameters and not have a body/default
     * implementation.
     *
     * @param ast AST element to check
     * @return true if ast represents a getter method, false otherwise
     */
    public static boolean isGetterMethod(final DetailAST ast) {
        if (ast.getType() == TokenTypes.METHOD_DEF
                && ast.getChildCount() == SETTER_GETTER_MAX_CHILDREN) {
            final DetailAST type = ast.findFirstToken(TokenTypes.TYPE);
            final String name = type.getNextSibling().getText();
            final boolean matchesGetterFormat = GETTER_PATTERN.matcher(name).matches();
            final boolean noVoidReturnType = type.findFirstToken(TokenTypes.LITERAL_VOID) == null;
            final DetailAST params = ast.findFirstToken(TokenTypes.PARAMETERS);
            final boolean noParams = params.getChildCount(TokenTypes.PARAMETER_DEF) == 0;
            final boolean noBody = ast.findFirstToken(TokenTypes.SLIST) == null;
            return matchesGetterFormat
                    && noVoidReturnType
                    && noParams
                    && noBody;
        }
        return false;
    }


    /**
     * Checks if given AST element represents a setter method. To be classified as setter method, a method must match
     * the naming conventions, have void return type, take a single parameter and not have a body/default
     * implementation.
     *
     * @param ast AST element to check
     * @return true if ast represents a setter method, false otherwise
     */
    public static boolean isSetterMethod(final DetailAST ast) {
        if (ast.getType() == TokenTypes.METHOD_DEF
                && ast.getChildCount() == SETTER_GETTER_MAX_CHILDREN) {
            final DetailAST type = ast.findFirstToken(TokenTypes.TYPE);
            final String name = type.getNextSibling().getText();
            final boolean matchesSetterFormat = SETTER_PATTERN.matcher(name).matches();
            final boolean voidReturnType = type.findFirstToken(TokenTypes.LITERAL_VOID) != null;
            final DetailAST params = ast.findFirstToken(TokenTypes.PARAMETERS);
            final boolean singleParam = params.getChildCount(TokenTypes.PARAMETER_DEF) == 1;
            final boolean noBody = ast.findFirstToken(TokenTypes.SLIST) == null;
            return matchesSetterFormat && voidReturnType && singleParam && noBody;
        }
        return false;
    }
}
