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

import com.puppycrawl.tools.checkstyle.FileStatefulCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.Scope;
import com.puppycrawl.tools.checkstyle.api.TextBlock;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.checks.javadoc.MissingJavadocMethodCheck;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;
import com.puppycrawl.tools.checkstyle.utils.ScopeUtil;
import de.fraunhofer.iosb.ilt.faaast.service.checks.util.InterfaceHelper;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Extension of {@link com.puppycrawl.tools.checkstyle.checks.javadoc.MissingJavadocMethodCheck} providing additional
 * functionality to ignore builder-related methods. By default, this check uses the regex
 * <i>(Abstract)?(${classname})?Builder</i> to decide whether a class is a builder class or not. The string
 * <i>${classname}</i> is replaced with the name of the enclosing class if present, otherwise with an empty string.
 * Static builder methods are identified using the regex <i>builder</i> and are required to take no parameters and
 * return a type that itself can be identified as builder class. By default, all methods within builder classes as well
 * as static builder methods are ignored.
 */
@FileStatefulCheck
public class ExtendedMissingJavadocMethodCheck extends BuilderAwareCheck {

    private static final String MSG_JAVADOC_MISSING = "javadoc.missing";
    private static final int DEFAULT_MIN_LINE_COUNT = -1;
    private static final String UNSUPPORTED_TYPE = "UNSUPPORTED_TYPE";

    private boolean allowMissingPropertyJavadoc;
    private Set<String> allowedAnnotations = Set.of("Override");
    private Scope excludeScope;
    private Pattern ignoreMethodNamesRegex;
    private boolean ignoreStaticBuilderMethods = true;
    private int minLineCount = DEFAULT_MIN_LINE_COUNT;
    private Scope scope = Scope.PUBLIC;
    private String staticBuilderMethodNameRegex = "builder";

    @Override
    public int[] getAcceptableTokens() {
        return new int[] {
                TokenTypes.CLASS_DEF,
                TokenTypes.INTERFACE_DEF,
                TokenTypes.ENUM_DEF,
                TokenTypes.METHOD_DEF,
                TokenTypes.CTOR_DEF,
                TokenTypes.ANNOTATION_FIELD_DEF,
                TokenTypes.COMPACT_CTOR_DEF
        };
    }


    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }


    @Override
    public final int[] getRequiredTokens() {
        return new int[] {
                TokenTypes.CLASS_DEF,
                TokenTypes.INTERFACE_DEF,
                TokenTypes.ENUM_DEF
        };
    }


    public void setAllowMissingPropertyJavadoc(final boolean flag) {
        allowMissingPropertyJavadoc = flag;
    }


    public void setAllowedAnnotations(String... userAnnotations) {
        allowedAnnotations = Set.of(userAnnotations);
    }


    public void setExcludeScope(Scope excludeScope) {
        this.excludeScope = excludeScope;
    }


    public void setIgnoreMethodNamesRegex(Pattern pattern) {
        ignoreMethodNamesRegex = pattern;
    }


    public void setIgnoreStaticBuilderMethods(boolean value) {
        ignoreStaticBuilderMethods = value;
    }


    public void setMinLineCount(int value) {
        minLineCount = value;
    }


    public void setScope(Scope scope) {
        this.scope = scope;
    }


    public void setStaticBuilderMethodNameRegex(String staticBuilderMethodNameRegex) {
        this.staticBuilderMethodNameRegex = staticBuilderMethodNameRegex;
    }


    @Override
    public final void visitToken(DetailAST ast) {
        super.visitToken(ast);
        switch (ast.getType()) {
            case TokenTypes.METHOD_DEF:
            case TokenTypes.CTOR_DEF:
            case TokenTypes.ANNOTATION_FIELD_DEF:
            case TokenTypes.COMPACT_CTOR_DEF: {
                if (!(ignoreBuilder && currentlyInBuilder)) {
                    final Scope theScope = ScopeUtil.getScope(ast);
                    if (shouldCheck(ast, theScope)) {
                        final FileContents contents = getFileContents();
                        final TextBlock textBlock = contents.getJavadocBefore(ast.getLineNo());
                        if (textBlock == null && !isMissingJavadocAllowed(ast)) {
                            log(ast, MSG_JAVADOC_MISSING);
                        }
                    }
                }
                break;
            }
            default:
        }
    }


    private boolean isContentsAllowMissingJavadoc(DetailAST ast) {
        return (ast.getType() == TokenTypes.METHOD_DEF
                || ast.getType() == TokenTypes.CTOR_DEF
                || ast.getType() == TokenTypes.COMPACT_CTOR_DEF)
                && (getMethodsNumberOfLine(ast) <= minLineCount
                        || AnnotationUtil.containsAnnotation(ast, allowedAnnotations));
    }


    private boolean isMissingJavadocAllowed(final DetailAST ast) {
        return allowMissingPropertyJavadoc
                && (ignoreStaticBuilderMethods && isStaticBuilderMethod(ast))
                || isSetterOrGetter(ast)
                || matchesSkipRegex(ast)
                || isContentsAllowMissingJavadoc(ast);
    }


    private boolean isSetterOrGetter(final DetailAST ast) {
        return ((currentlyInClass() || currentlyInEnum()) && (MissingJavadocMethodCheck.isSetterMethod(ast) || MissingJavadocMethodCheck.isGetterMethod(ast)))
                || (currentlyInInterface() && InterfaceHelper.isGetterOrGetterMethod(ast));
    }


    private static String getReturnType(final DetailAST methodDef) {
        DetailAST current = methodDef.findFirstToken(TokenTypes.TYPE);
        String result = null;
        while (result == null) {
            if (current.findFirstToken(TokenTypes.IDENT) != null) {
                result = current.findFirstToken(TokenTypes.IDENT).getText();
            }
            else if (current.findFirstToken(TokenTypes.DOT) != null) {
                current = current.findFirstToken(TokenTypes.DOT).getLastChild();
            }
            else if (current.findFirstToken(TokenTypes.ARRAY_DECLARATOR) != null) {
                result = current.getFirstChild().getText();
            }
            else {
                result = UNSUPPORTED_TYPE;
            }
        }
        return result;
    }


    private boolean isStaticBuilderMethod(final DetailAST ast) {
        if (ast.getType() == TokenTypes.METHOD_DEF) {
            final DetailAST type = ast.findFirstToken(TokenTypes.TYPE);
            final String name = type.getNextSibling().getText();
            final String returnType = getReturnType(ast);
            boolean isStatic = ast.findFirstToken(TokenTypes.MODIFIERS).findFirstToken(TokenTypes.LITERAL_STATIC) != null;
            boolean matchesMethodName = name.matches(staticBuilderMethodNameRegex);
            boolean returnTypeIsBuilder = matchesBuilderPattern(returnType);
            if (isStatic
                    && matchesMethodName
                    && (returnTypeIsBuilder || UNSUPPORTED_TYPE.equals(returnType))) {
                final DetailAST slist = ast.findFirstToken(TokenTypes.SLIST);
                return slist != null && slist.getFirstChild().getType() == TokenTypes.LITERAL_RETURN;
            }
        }
        return false;
    }


    private boolean matchesSkipRegex(DetailAST methodDef) {
        boolean result = false;
        if (ignoreMethodNamesRegex != null) {
            final DetailAST ident = methodDef.findFirstToken(TokenTypes.IDENT);
            final String methodName = ident.getText();
            final Matcher matcher = ignoreMethodNamesRegex.matcher(methodName);
            if (matcher.matches()) {
                result = true;
            }
        }
        return result;
    }


    private boolean shouldCheck(final DetailAST ast, final Scope nodeScope) {
        final Scope surroundingScope = ScopeUtil.getSurroundingScope(ast);
        return (excludeScope == null
                || nodeScope != excludeScope
                        && surroundingScope != excludeScope)
                && nodeScope.isIn(scope)
                && surroundingScope.isIn(scope);
    }


    private static int getMethodsNumberOfLine(DetailAST methodDef) {
        final int numberOfLines;
        final DetailAST lcurly = methodDef.getLastChild();
        final DetailAST rcurly = lcurly.getLastChild();
        if (lcurly.getFirstChild() == rcurly) {
            numberOfLines = 1;
        }
        else {
            numberOfLines = rcurly.getLineNo() - lcurly.getLineNo() - 1;
        }
        return numberOfLines;
    }

}
