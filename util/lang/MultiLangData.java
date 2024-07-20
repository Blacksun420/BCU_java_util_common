package common.util.lang;

import common.CommonStatic;
import common.CommonStatic.Lang;
import common.io.json.JsonClass;
import common.io.json.JsonField;
import common.util.Data;
import org.jetbrains.annotations.NotNull;

import java.util.TreeMap;

@JsonClass(read = JsonClass.RType.FILL)
public class MultiLangData extends Data {
    @JsonField(generic = {Lang.Locale.class, String.class})
    private final TreeMap<Lang.Locale, String> dat = new TreeMap<>();

    @JsonClass.JCConstructor
    public MultiLangData() {
    }

    public boolean put(String data) {
        return put(lang(), data);
    }

    /**
     * Handles putting a name and removing duplicate/identical names in other languages.
     * @param lang The language index
     * @param data The string
     * @return True if the value in the given lang changed
     */
    public boolean put(Lang.Locale lang, String data) {
        if (data != null && data.length() > 0 && (lang == lang() || !toString().equals(data))) {
            String old = dat.put(lang, data);
            return old == null || !old.equals(data);
        }
        return dat.remove(lang) != null;
    }

    public void remove(int lang) {
        dat.remove(lang);
    }

    public String get(int lang) {
        String temp = dat.get(lang);
        if (temp != null)
            return temp;
        return toString();
    }

    public void replace(String olds, String news) {
        dat.replaceAll((k, v) -> v.replace(olds, news));
    }

    @NotNull
    @Override
    public String toString() {
        Lang.Locale lang = lang();
        Lang.Locale[] locales = Lang.Locale.values();

        String temp = dat.get(lang);
        if(temp != null)
            return temp;

        for (int i = 1; i < Lang.pref.length; i++) {
            if (i < Lang.pref[lang.ordinal()].length) {
                if (dat.containsKey(Lang.pref[lang.ordinal()][i])) {
                    temp = dat.get(Lang.pref[lang.ordinal()][i]);
                    if (temp != null)
                        return temp;
                }
            } else if (dat.containsKey(locales[i])) {
                temp = dat.get(locales[i]);
                if(temp != null)
                    return temp;
            }
        }
        return "";
    }

    public Lang.Locale getGrabbedLocale() {
        Lang.Locale lang = lang();
        Lang.Locale[] locales = Lang.Locale.values();

        for (int i = 1; i < Lang.pref.length; i++) {
            if (i < Lang.pref[lang.ordinal()].length) {
                if (dat.containsKey(Lang.pref[lang.ordinal()][i])) {
                    String temp = dat.get(Lang.pref[lang.ordinal()][i]);

                    if (temp != null)
                        return Lang.pref[lang.ordinal()][i];
                }
            } else if (dat.containsKey(locales[i])) {
                String temp = dat.get(locales[i]);

                if(temp != null)
                    return locales[i];
            }
        }

        return Lang.Locale.EN;
    }

    private static Lang.Locale lang() {
        return CommonStatic.getConfig().lang;
    }

    public void overwrite(MultiLangData ans) { //Replaces all values with the given MLD's values
        for (Lang.Locale lang : ans.dat.keySet())
            dat.put(lang, ans.dat.get(lang));
    }
}