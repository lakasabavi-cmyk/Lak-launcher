package com.lakasa.n1launcher;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Tiny file-based i18n for MTH Launcher.
 *
 * Languages are plain UTF-8 text files, one per language, named by code:
 * "en.txt", "vi.txt", "zh.txt", ... Each line is key=value. Lines starting
 * with '#' are comments. Two meta keys describe the pack itself:
 *   __name   = display name shown in the language menu (e.g. "Tiếng Việt")
 *   __locale = locale for dates, e.g. "vi-VN" (defaults to the file code)
 *
 * Sources, in increasing priority (later sources override earlier ones):
 *   1. APK assets:  assets/lang/*.txt        (baked in at build time)
 *   2. App folder:  Android/data/com.lakasa.n1launcher/files/lang/*.txt
 *   3. Public folder: /sdcard/MTH-Launcher/lang/*.txt
 *
 * Sources 2 and 3 are what make languages user-installable: push any .txt
 * pack with ADB (or copy it with a file manager) and the launcher picks it
 * up on the next start - no rebuild, no update needed.
 *
 * English ("en") is the reference language and the fallback for every
 * missing key: current language -> English -> the key itself.
 */
public class Lang {

    private final Map<String, Map<String, String>> tables = new HashMap<String, Map<String, String>>();
    private String current = "en";

    public static Lang load(Context ctx) {
        Lang lang = new Lang();
        // 1) bundled packs in assets/lang/
        try {
            AssetManager am = ctx.getAssets();
            String[] files = am.list("lang");
            if (files != null) {
                for (String f : files) {
                    if (!f.endsWith(".txt")) continue;
                    String code = codeOf(f);
                    InputStream in = null;
                    try {
                        in = am.open("lang/" + f);
                        lang.putTable(code, parse(in));
                    } catch (Exception ignored) {
                    } finally {
                        if (in != null) try { in.close(); } catch (Exception ignored) { }
                    }
                }
            }
        } catch (Exception ignored) { }
        // 2) app-specific external folder (no permission needed)
        try {
            File ext = ctx.getExternalFilesDir(null);
            if (ext != null) lang.loadDir(new File(ext, "lang"));
        } catch (Exception ignored) { }
        // 3) public folder users can reach with any file manager / ADB
        try {
            File pub = new File(Environment.getExternalStorageDirectory(), "MTH-Launcher/lang");
            lang.loadDir(pub);
        } catch (Exception ignored) { }
        // English must always exist, even if every file went missing.
        if (!lang.tables.containsKey("en")) {
            Map<String, String> en = new HashMap<String, String>();
            en.put("__name", "English");
            en.put("__locale", "en-US");
            lang.tables.put("en", en);
        }
        return lang;
    }

    private void loadDir(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (!f.isFile() || !f.getName().endsWith(".txt")) continue;
            InputStream in = null;
            try {
                in = new FileInputStream(f);
                putTable(codeOf(f.getName()), parse(in));
            } catch (Exception ignored) {
            } finally {
                if (in != null) try { in.close(); } catch (Exception ignored) { }
            }
        }
    }

    private void putTable(String code, Map<String, String> table) {
        if (code == null || table == null || table.isEmpty()) return;
        Map<String, String> existing = tables.get(code);
        if (existing == null) {
            tables.put(code, table);
        } else {
            existing.putAll(table); // external packs may override single keys
        }
    }

    private static String codeOf(String fileName) {
        String n = fileName.substring(0, fileName.length() - 4); // strip ".txt"
        return n.trim().toLowerCase(Locale.US);
    }

    private static Map<String, String> parse(InputStream in) throws Exception {
        Map<String, String> map = new HashMap<String, String>();
        BufferedReader r = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        String line;
        while ((line = r.readLine()) != null) {
            String s = line.trim();
            if (s.isEmpty() || s.startsWith("#")) continue;
            int eq = s.indexOf('=');
            if (eq <= 0) continue;
            String key = s.substring(0, eq).trim();
            String val = s.substring(eq + 1).trim();
            if (!key.isEmpty()) map.put(key, val);
        }
        return map;
    }

    /** All language codes found, English first, the rest alphabetical. */
    public List<String> available() {
        List<String> codes = new ArrayList<String>(tables.keySet());
        Collections.sort(codes);
        if (codes.contains("en")) {
            codes.remove("en");
            codes.add(0, "en");
        }
        return codes;
    }

    public void setCurrent(String code) {
        if (code != null && tables.containsKey(code)) current = code;
    }

    public String currentCode() {
        return current;
    }

    /** Display name of a pack ("__name"), or the code itself. */
    public String nameOf(String code) {
        Map<String, String> t = tables.get(code);
        if (t != null) {
            String n = t.get("__name");
            if (n != null && !n.isEmpty()) return n;
        }
        return code;
    }

    /** Next language in the cycle (used by the Language menu entry). */
    public String nextCode() {
        List<String> codes = available();
        int i = codes.indexOf(current);
        if (i < 0) return "en";
        return codes.get((i + 1) % codes.size());
    }

    /** Translate a key: current language, then English, then the key. */
    public String t(String key) {
        Map<String, String> t = tables.get(current);
        if (t != null) {
            String v = t.get(key);
            if (v != null) return v;
        }
        Map<String, String> en = tables.get("en");
        if (en != null) {
            String v = en.get(key);
            if (v != null) return v;
        }
        return key;
    }

    /** Locale for date formatting, from "__locale" (e.g. "vi-VN"). */
    public Locale locale() {
        Map<String, String> t = tables.get(current);
        String loc = t != null ? t.get("__locale") : null;
        if (loc != null && !loc.isEmpty()) {
            String[] p = loc.replace('_', '-').split("-");
            if (p.length >= 2) return new Locale(p[0], p[1]);
            return new Locale(p[0]);
        }
        if ("en".equals(current)) return Locale.US;
        return new Locale(current);
    }
}
