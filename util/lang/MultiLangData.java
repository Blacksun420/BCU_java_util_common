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

    public void put(String data) {
        put(lang(), data);
    }

    public void put(int lang, String data) {
        if (data != null && data.length() > 0)
            dat.put(lang, data);
        else
            dat.remove(lang);
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

        if (dat.containsKey(lang)) {
            String temp = dat.get(lang);

            if(temp != null)
                return temp;
        }

        for (int i = 1; i < Lang.LOC_CODE.length; i++) {
            if (i < Lang.pref[lang].length) {
                if (dat.containsKey(Lang.pref[lang][i])) {
                    String temp = dat.get(Lang.pref[lang][i]);

                    if (temp != null)
                        return temp;
                }
            } else if (dat.containsKey(i)) {
                String temp = dat.get(i);

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
                    return Lang.pref[lang][i];
            }
        }

        return -1;
    }

    private static int lang() {
        return CommonStatic.getConfig().lang;
    }

    public void overwrite(MultiLangData ans) { //Replaces all values with the given MLD's values
        for (int lang : ans.dat.keySet())
            dat.put(lang, dat.get(lang));
    }
}
