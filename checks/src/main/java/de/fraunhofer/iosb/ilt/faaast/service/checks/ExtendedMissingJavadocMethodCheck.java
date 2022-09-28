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
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;
import com.puppycrawl.tools.checkstyle.utils.ScopeUtil;
import de.fraunhofer.iosb.ilt.faaast.service.checks.util.InterfaceHelper;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@FileStatefulCheck
public class ExtendedMissingJavadocMethodCheck extends BuilderAwareCheck {

    public static final String MSG_JAVADOC_MISSING = "javadoc.missing";
    private static final int STATIC_BUILDER_MAX_CHILDREN = 7;
    private static final int DEFAULT_MIN_LINE_COUNT = -1;
    private static final int STATIC_BUILDER_BODY_SIZE = 2;

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
                TokenTypes.METHOD_DEF,
                TokenTypes.CTOR_DEF,
                TokenTypes.ANNOTATION_FIELD_DEF,
                TokenTypes.COMPACT_CTOR_DEF,
        };
    }


    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }


    @Override
    public final int[] getRequiredTokens() {
        return CommonUtil.EMPTY_INT_ARRAY;
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
            }
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
                || (currentlyInClass() && (CheckUtil.isSetterMethod(ast) || CheckUtil.isGetterMethod(ast)))
                || (currentlyInInterface() && InterfaceHelper.isGetterOrGetterMethod(ast))
                || matchesSkipRegex(ast)
                || isContentsAllowMissingJavadoc(ast);
    }


    private boolean isStaticBuilderMethod(final DetailAST ast) {
        if (ast.getType() == TokenTypes.METHOD_DEF
                && ast.getChildCount() == STATIC_BUILDER_MAX_CHILDREN) {
            final DetailAST type = ast.findFirstToken(TokenTypes.TYPE);
            final String name = type.getNextSibling().getText();
            final String returnType = type.getFirstChild().getText();
            boolean isStatic = ast.findFirstToken(TokenTypes.MODIFIERS).findFirstToken(TokenTypes.LITERAL_STATIC) != null;
            boolean matchesMethodName = name.matches(staticBuilderMethodNameRegex);
            boolean returnTypeIsBuilder = matchesBuilderPattern(returnType);
            if (isStatic && matchesMethodName && returnTypeIsBuilder) {
                // ensure body contains only return statement
                final DetailAST slist = ast.findFirstToken(TokenTypes.SLIST);
                if (slist != null && slist.getChildCount() == STATIC_BUILDER_BODY_SIZE) {
                    final DetailAST expr = slist.getFirstChild();
                    return expr.getType() == TokenTypes.LITERAL_RETURN;
                }
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
