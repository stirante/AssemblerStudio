package com.stirante.asem.syntax.code;

/**
 * Created by stirante
 */
public interface CodeElement {

    int getDefinitionStart();

    int getDefinitionEnd();

    int getDefinitionLine();

    CodeElementType getType();

}
