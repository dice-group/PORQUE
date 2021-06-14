package org.dice.porque.translator;
public interface LanguageTranslator {

    String tranlate(String query, String source, String target);
    Object listSupportLang();

}
