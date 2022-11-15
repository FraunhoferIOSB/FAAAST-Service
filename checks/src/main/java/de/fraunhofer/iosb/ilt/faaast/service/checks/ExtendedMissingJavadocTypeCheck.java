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
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;
import com.puppycrawl.tools.checkstyle.utils.ScopeUtil;
import java.util.Set;


/**
 * Extension of {@link com.puppycrawl.tools.checkstyle.checks.javadoc.MissingJavadocTypeCheck} providing additional
 * functionality to ignore builder classes. By default, this check uses the regex
 * <i>(Abstract)?(${classname})?Builder</i> to decide whether a class is a builder class or not. The string
 * <i>${classname}</i> is replaced with the name of the enclosing class if present, otherwise with an empty string. By
 * default, all builder classes are ignored.
 */
@FileStatefulCheck
public class ExtendedMissingJavadocTypeCheck extends BuilderAwareCheck {

    private static final String MSG_JAVADOC_MISSING = "javadoc.missing";

    private Scope scope = Scope.PUBLIC;
    private Scope excludeScope;
    private Set<String> skipAnnotations = Set.of("Generated");

    public void setScope(Scope scope) {
        this.scope = scope;
    }


    public void setExcludeScope(Scope excludeScope) {
        this.excludeScope = excludeScope;
    }


    public void setSkipAnnotations(String... userAnnotations) {
        skipAnnotations = Set.of(userAnnotations);
    }


    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }


    @Override
    public int[] getAcceptableTokens() {
        return new int[] {
                TokenTypes.INTERFACE_DEF,
                TokenTypes.CLASS_DEF,
                TokenTypes.ENUM_DEF,
                TokenTypes.ANNOTATION_DEF,
                TokenTypes.RECORD_DEF,
        };
    }


    @Override
    public int[] getRequiredTokens() {
        return CommonUtil.EMPTY_INT_ARRAY;
    }


    @Override
    public final void visitToken(DetailAST ast) {
        super.visitToken(ast);
        if (shouldCheck(ast)) {
            final FileContents contents = getFileContents();
            final int lineNo = ast.getLineNo();
            final TextBlock textBlock = contents.getJavadocBefore(lineNo);
            if (textBlock == null) {
                log(ast, MSG_JAVADOC_MISSING);
            }
        }
    }


    private boolean shouldCheck(final DetailAST ast) {
        final Scope customScope = ScopeUtil.getScope(ast);
        final Scope surroundingScope = ScopeUtil.getSurroundingScope(ast);
        return customScope.isIn(scope)
                && !(ignoreBuilder && currentlyInBuilder)
                && (surroundingScope == null || surroundingScope.isIn(scope))
                && (excludeScope == null
                        || !customScope.isIn(excludeScope)
                        || surroundingScope != null
                                && !surroundingScope.isIn(excludeScope))
                && !AnnotationUtil.containsAnnotation(ast, skipAnnotations);
    }
}
