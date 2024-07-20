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
    @JsonField(generic = {Integer.class, String.class})
    private final TreeMap<Integer, String> dat = new TreeMap<>();

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
    public boolean put(int lang, String data) {
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
        int lang = lang();

        String temp = dat.get(lang);
        if(temp != null)
            return temp;

        for (int i = 1; i < Lang.LOC_CODE.length; i++) {
            if (i < Lang.pref[lang].length) {
                if (dat.containsKey(Lang.pref[lang][i])) {
                    temp = dat.get(Lang.pref[lang][i]);
                    if (temp != null)
                        return temp;
                }
            } else if (dat.containsKey(i)) {
                temp = dat.get(i);
                if(temp != null)
                    return temp;
            }
        }
        return "";
    }

    public int getGrabbedLocale() {
        int lang = lang();

        for (int i = 1; i < Lang.LOC_CODE.length; i++) {
            if (i < Lang.pref[lang].length) {
                if (dat.containsKey(Lang.pref[lang][i])) {
                    String temp = dat.get(Lang.pref[lang][i]);

                    if (temp != null)
                        return Lang.pref[lang][i];
                }
            } else if (dat.containsKey(i)) {
                String temp = dat.get(i);

                if(temp != null)
                    return i;
            }
        }

        return -1;
    }

    private static int lang() {
        return CommonStatic.getConfig().lang;
    }

    public void overwrite(MultiLangData ans) { //Replaces all values with the given MLD's values
        for (int lang : ans.dat.keySet())
            dat.put(lang, ans.dat.get(lang));
    }
}