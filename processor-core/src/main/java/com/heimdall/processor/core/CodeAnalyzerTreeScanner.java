package com.heimdall.processor.core;

import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;

/**
 * @author crh
 * @since 2020-10-03
 */
public class CodeAnalyzerTreeScanner extends TreePathScanner<Object, Trees> {

    private String fieldName;

    private String fieldInitializer;

    void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    String getFieldInitializer() {
        return this.fieldInitializer;
    }

    @Override
    public Object visitVariable(VariableTree variableTree, Trees trees) {
        if (variableTree.getName().toString().equals(this.fieldName)) {
            this.fieldInitializer = variableTree.getInitializer().toString();
        }

        return super.visitVariable(variableTree, trees);
    }
}