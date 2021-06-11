package org.dice.porque.translator;
public interface LanguageTranslator {

    public String tranlate(String query, String source, String target);
    public Object listSupportLang();

}
