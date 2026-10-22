package com.lakasa.n1launcher;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {

    private static final String PREFS = "n1launcher";
    private static final String KEY_VI = "vi"; // legacy toggle, migrated on first run
    private static final String KEY_LANG = "lang";
    private static final String KEY_ORDER = "order";

    // ---- Android TV 9 palette (dark plum gradient like the reference) ----
    private static final int COL_BG_TOP = Color.parseColor("#2E2138");
    private static final int COL_BG_BOTTOM = Color.parseColor("#120D1A");
    private static final int COL_CARD = Color.parseColor("#2C2733");
    private static final int COL_CARD_FOCUS = Color.parseColor("#3D3648");
    private static final int COL_TEXT = Color.parseColor("#E8EAED");
    private static final int COL_TEXT_DIM = Color.parseColor("#A49FB0");
    private static final int COL_MOVING = Color.parseColor("#E4572E");

    private SharedPreferences prefs;
    private Lang lang;
    private boolean editMode = false;
    private int movingPos = -1;

    private TextView clockView;
    private TextView dateView;
    private TextView hintView;
    private LinearLayout editBar;
    private GridView grid;
    private AppAdapter adapter;
    private final Handler handler = new Handler();
    private final Runnable clockTick = new Runnable() {
        @Override public void run() {
            updateClock();
            handler.postDelayed(this, 20000);
        }
    };

    static class AppItem {
        String label;
        Drawable icon;
        ComponentName component;
    }

    // ---------- i18n: file-based language packs (see Lang.java) ----------
    private String t(String k) {
        return lang != null ? lang.t(k) : k;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        lang = Lang.load(this);
        String langCode = prefs.getString(KEY_LANG, null);
        if (langCode == null) {
            // Migrate the v2.4-and-earlier English/Vietnamese toggle.
            langCode = prefs.getBoolean(KEY_VI, false) ? "vi" : "en";
        }
        lang.setCurrent(langCode);
        buildUi();
        loadApps();
        handler.post(clockTick);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(clockTick);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (movingPos != -1) {
                movingPos = -1;
                if (adapter != null) adapter.notifyDataSetChanged();
                return true;
            }
            if (editMode) {
                setEditMode(false);
                return true;
            }
            // A launcher should not be exited with Back.
            return true;
        }
        // NOTE: KEYCODE_MENU is intentionally NOT intercepted. On JMGO remotes
        // the Menu/Settings key (long-press) opens the projector's own settings
        // panel at system level - the launcher must not swallow it.
        return super.onKeyDown(keyCode, event);
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    // ---------- style helpers ----------
    private GradientDrawable pillBg(boolean focused) {
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(40));
        g.setColor(focused ? Color.WHITE : Color.parseColor("#353040"));
        return g;
    }

    private GradientDrawable cardBg(boolean focused) {
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(8));
        g.setColor(focused ? COL_CARD_FOCUS : COL_CARD);
        if (focused) g.setStroke(dp(3), Color.WHITE);
        return g;
    }

    private GradientDrawable movingBg() {
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(dp(8));
        g.setColor(COL_MOVING);
        g.setStroke(dp(3), Color.WHITE);
        return g;
    }

    private GradientDrawable circleBg(int color, boolean focused) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(color);
        if (focused) g.setStroke(dp(3), Color.WHITE);
        return g;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{COL_BG_TOP, COL_BG_BOTTOM});
        root.setBackground(bg);

        // ============ LEFT RAIL (Android TV 9 style) ============
        ScrollView railScroll = new ScrollView(this);
        railScroll.setVerticalScrollBarEnabled(false);
        railScroll.setFocusable(false);
        LinearLayout.LayoutParams railScrollLp = new LinearLayout.LayoutParams(dp(172), ViewGroup.LayoutParams.MATCH_PARENT);
        root.addView(railScroll, railScrollLp);

        LinearLayout rail = new LinearLayout(this);
        rail.setOrientation(LinearLayout.VERTICAL);
        rail.setGravity(Gravity.CENTER_HORIZONTAL);
        rail.setPadding(dp(8), dp(64), dp(8), dp(24));
        railScroll.addView(rail);

        addRailItem(rail, "⊞", Color.parseColor("#E53935"), t("apps"), new View.OnClickListener() {
            @Override public void onClick(View v) { focusApps(); }
        });
        addRailItem(rail, "▶", Color.parseColor("#FF0000"), "YouTube", new View.OnClickListener() {
            @Override public void onClick(View v) { launchYouTubeApp(); }
        });
        addRailItem(rail, "V", Color.parseColor("#EE0033"), "VTVgo TV", new View.OnClickListener() {
            @Override public void onClick(View v) { launchVtvgoApp(); }
        });
        addRailItem(rail, "◐", Color.parseColor("#5F6368"), t("projSettings"), new View.OnClickListener() {
            @Override public void onClick(View v) { openProjectorSettings(); }
        });
        addRailItem(rail, "⚙", Color.parseColor("#5F6368"), t("sysSettings"), new View.OnClickListener() {
            @Override public void onClick(View v) { openSystemSettings(); }
        });
        addRailItem(rail, "Aa", Color.parseColor("#1E88E5"), t("language"), new View.OnClickListener() {
            @Override public void onClick(View v) { toggleLanguage(); }
        });
        addRailItem(rail, "↩", Color.parseColor("#5F6368"), t("exit"), new View.OnClickListener() {
            @Override public void onClick(View v) { exitToStockHome(); }
        });

        // ============ RIGHT CONTENT ============
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(6), dp(24), dp(36), dp(14));
        root.addView(content, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        // Top bar: search pill + clock
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView search = new TextView(this);
        search.setText("  🔍   " + t("search"));
        search.setTextColor(COL_TEXT_DIM);
        search.setTextSize(15);
        search.setGravity(Gravity.CENTER_VERTICAL);
        search.setFocusable(true);
        search.setFocusableInTouchMode(false);
        search.setClickable(true);
        search.setBackground(pillBg(false));
        search.setPadding(dp(14), dp(11), dp(20), dp(11));
        search.setLayoutParams(new LinearLayout.LayoutParams(dp(430), ViewGroup.LayoutParams.WRAP_CONTENT));
        search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View v, boolean f) {
                v.setBackground(pillBg(f));
                ((TextView) v).setTextColor(f ? Color.parseColor("#202124") : COL_TEXT_DIM);
            }
        });
        search.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openGlobalSearch(); }
        });
        header.addView(search);
        header.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
        LinearLayout clockBox = new LinearLayout(this);
        clockBox.setOrientation(LinearLayout.VERTICAL);
        clockBox.setGravity(Gravity.RIGHT);
        clockView = new TextView(this);
        clockView.setTextColor(Color.WHITE);
        clockView.setTextSize(28);
        clockView.setGravity(Gravity.RIGHT);
        dateView = new TextView(this);
        dateView.setTextColor(COL_TEXT_DIM);
        dateView.setTextSize(12);
        dateView.setGravity(Gravity.RIGHT);
        clockBox.addView(clockView);
        clockBox.addView(dateView);
        header.addView(clockBox);
        content.addView(header);

        // Rearrange bar above the apps grid (visible in the Apps section)
        LinearLayout rearrangeRow = new LinearLayout(this);
        rearrangeRow.setOrientation(LinearLayout.HORIZONTAL);
        rearrangeRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rrLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rrLp.topMargin = dp(14);
        content.addView(rearrangeRow, rrLp);
        TextView rearrangeBtn = makeChip(t("rearrange"), new View.OnClickListener() {
            @Override public void onClick(View v) { setEditMode(!editMode); }
        });
        LinearLayout.LayoutParams chipLp = new LinearLayout.LayoutParams(dp(220), ViewGroup.LayoutParams.WRAP_CONTENT);
        rearrangeBtn.setLayoutParams(chipLp);
        rearrangeRow.addView(rearrangeBtn);
        hintView = new TextView(this);
        hintView.setText(t("editHint"));
        hintView.setTextColor(Color.parseColor("#FFC857"));
        hintView.setTextSize(13);
        hintView.setVisibility(View.GONE);
        LinearLayout.LayoutParams hintLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        hintLp.leftMargin = dp(14);
        hintView.setLayoutParams(hintLp);
        rearrangeRow.addView(hintView);

        // Edit-mode bar (Done / Reset order), hidden unless rearranging
        editBar = new LinearLayout(this);
        editBar.setOrientation(LinearLayout.HORIZONTAL);
        editBar.setVisibility(View.GONE);
        LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        barLp.topMargin = dp(8);
        content.addView(editBar, barLp);
        TextView doneBtn = makeChip(t("done"), new View.OnClickListener() {
            @Override public void onClick(View v) { setEditMode(false); }
        });
        doneBtn.setLayoutParams(new LinearLayout.LayoutParams(dp(160), ViewGroup.LayoutParams.WRAP_CONTENT));
        editBar.addView(doneBtn);
        TextView resetBtn = makeChip(t("resetOrder"), new View.OnClickListener() {
            @Override public void onClick(View v) {
                prefs.edit().remove(KEY_ORDER).apply();
                movingPos = -1;
                loadApps();
            }
        });
        LinearLayout.LayoutParams resetLp = new LinearLayout.LayoutParams(dp(220), ViewGroup.LayoutParams.WRAP_CONTENT);
        resetLp.leftMargin = dp(10);
        resetBtn.setLayoutParams(resetLp);
        editBar.addView(resetBtn);

        // Scrollable lower area: apps grid + YouTube rows
        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        scroll.setFocusable(false);
        LinearLayout.LayoutParams scrollLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        scrollLp.topMargin = dp(6);
        content.addView(scroll, scrollLp);

        LinearLayout lower = new LinearLayout(this);
        lower.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(lower);

        // Apps grid: 8 columns, medium icons, internal scrolling for extra rows
        grid = new GridView(this);
        grid.setNumColumns(8);
        grid.setHorizontalSpacing(dp(12));
        grid.setVerticalSpacing(dp(12));
        grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        grid.setSelector(android.R.color.transparent);
        grid.setFocusable(true);
        grid.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        LinearLayout.LayoutParams gridLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(118));
        lower.addView(grid, gridLp);
        grid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (adapter != null) adapter.setSelected(position);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (editMode) {
                    handleRearrangeTap(position);
                } else {
                    launch(adapter.getItem(position));
                }
            }
        });
        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Long-press OK on an app: enter rearrange mode and pick it up.
                if (!editMode) setEditMode(true);
                movingPos = position;
                if (adapter != null) adapter.notifyDataSetChanged();
                return true;
            }
        });

        // YouTube rows: big cards, 4 per row
        lower.addView(rowTitle(t("ytRow1")));
        HorizontalScrollView row1 = makeCardRow(lower);
        addBigCard(row1, "↺", t("ytHistory"), t("ytHistorySub"), 0xFF8E2A2A, 0xFF38090E,
                "HistoryLauncherActivity");
        addBigCard(row1, "▤", t("ytLibrary"), t("ytLibrarySub"), 0xFF2A4D8E, 0xFF0E1E38,
                "PlaylistsLauncherActivity");
        addBigCard(row1, "♥", t("ytLiked"), t("ytLikedSub"), 0xFF8E2A5E, 0xFF380E24,
                "PlaylistsLauncherActivity");
        addBigCard(row1, "✓", t("ytSubs"), t("ytSubsSub"), 0xFF2A8E6B, 0xFF0E382A,
                "SubscriptionsLauncherActivity");

        lower.addView(rowTitle(t("ytRow2")));
        HorizontalScrollView row2 = makeCardRow(lower);
        // No Trending card: SmartTube has no Trending launcher activity
        // (verified in the 32.68 APK); Trending lives in the home sidebar.
        addBigCard(row2, "🔍", t("ytSearch"), t("ytSearchSub"), 0xFF2A5E8E, 0xFF0E2438,
                "SearchLauncherActivity");
        addBigCard(row2, "◷", t("ytWatchLater"), t("ytWatchLaterSub"), 0xFF4A5160, 0xFF171A21,
                "PlaylistsLauncherActivity");
        addBigCard(row2, "▶", t("ytOpen"), t("ytOpenSub"), 0xFF3A3A3E, 0xFF0A0A0C,
                "HomeLauncherActivity");

        setContentView(root);

        // Start focused on the apps grid, like Android TV home.
        root.post(new Runnable() {
            @Override public void run() { focusApps(); }
        });
    }

    private TextView rowTitle(String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(COL_TEXT);
        tv.setTextSize(19);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(22);
        lp.bottomMargin = dp(10);
        tv.setLayoutParams(lp);
        return tv;
    }

    private HorizontalScrollView makeCardRow(LinearLayout parent) {
        HorizontalScrollView hsv = new HorizontalScrollView(this);
        hsv.setHorizontalScrollBarEnabled(false);
        hsv.setFocusable(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(4), dp(8), dp(16), dp(10));
        row.setTag("cardRowInner");
        hsv.addView(row);
        parent.addView(hsv);
        return hsv;
    }

    private void addBigCard(HorizontalScrollView hsv, String glyph, String title, String sub,
                            int gradTop, int gradBottom, final String sectionActivity) {
        LinearLayout row = (LinearLayout) hsv.findViewWithTag("cardRowInner");
        // Card width: fit exactly 4 cards across the right content area.
        int screenDp = (int) (getResources().getDisplayMetrics().widthPixels
                / getResources().getDisplayMetrics().density);
        int contentDp = screenDp - 172 - 42 - 20; // rail + right padding + row paddings
        int cardW = Math.max(150, (contentDp - 3 * 14) / 4);
        int cardH = cardW * 9 / 16;

        FrameLayout card = new FrameLayout(this);
        card.setFocusable(true);
        card.setFocusableInTouchMode(false);
        card.setClickable(true);
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{gradTop, gradBottom});
        g.setCornerRadius(dp(8));
        card.setBackground(g);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(cardW), dp(cardH));
        lp.setMargins(0, 0, dp(14), 0);
        card.setLayoutParams(lp);

        TextView glyphTv = new TextView(this);
        glyphTv.setText(glyph);
        glyphTv.setTextColor(0xCCFFFFFF);
        glyphTv.setTextSize(34);
        glyphTv.setGravity(Gravity.CENTER);
        card.addView(glyphTv, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        FrameLayout.LayoutParams textsLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textsLp.gravity = Gravity.BOTTOM;
        texts.setPadding(dp(12), 0, dp(12), dp(10));
        TextView titleTv = new TextView(this);
        titleTv.setText(title);
        titleTv.setTextColor(Color.WHITE);
        titleTv.setTextSize(16);
        titleTv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        titleTv.setShadowLayer(4, 0, 1, 0xAA000000);
        texts.addView(titleTv);
        TextView subTv = new TextView(this);
        subTv.setText(sub);
        subTv.setTextColor(0xCCFFFFFF);
        subTv.setTextSize(12);
        subTv.setShadowLayer(4, 0, 1, 0xAA000000);
        texts.addView(subTv);
        card.addView(texts, textsLp);

        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openYouTubeSection(sectionActivity); }
        });
        final HorizontalScrollView scroller = hsv;
        card.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View v, boolean f) {
                GradientDrawable cur = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                        new int[]{gradTop, gradBottom});
                cur.setCornerRadius(dp(8));
                if (f) cur.setStroke(dp(3), Color.WHITE);
                v.setBackground(cur);
                v.setScaleX(f ? 1.05f : 1f);
                v.setScaleY(f ? 1.05f : 1f);
                v.setElevation(f ? dp(10) : 0);
                if (f) {
                    final View cardView = v;
                    scroller.post(new Runnable() {
                        @Override public void run() {
                            scroller.smoothScrollTo(Math.max(0, cardView.getLeft() - dp(30)), 0);
                        }
                    });
                }
            }
        });
        row.addView(card);
    }

    private TextView makeChip(String text, View.OnClickListener click) {
        TextView b = new TextView(this);
        b.setText(text);
        b.setTextColor(COL_TEXT);
        b.setTextSize(14);
        b.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        b.setGravity(Gravity.CENTER);
        b.setFocusable(true);
        b.setFocusableInTouchMode(false);
        b.setClickable(true);
        b.setBackground(pillBg(false));
        b.setPadding(dp(8), dp(10), dp(8), dp(10));
        if (click != null) b.setOnClickListener(click);
        b.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View view, boolean hasFocus) {
                view.setBackground(pillBg(hasFocus));
                ((TextView) view).setTextColor(hasFocus ? Color.parseColor("#202124") : COL_TEXT);
            }
        });
        return b;
    }

    private void addRailItem(LinearLayout rail, String glyph, int circleColor,
                             String labelText, View.OnClickListener click) {
        final int railCircleColor = circleColor;
        final boolean whiteCircle = circleColor == Color.WHITE;
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER_HORIZONTAL);
        item.setFocusable(true);
        item.setFocusableInTouchMode(false);
        item.setClickable(true);
        item.setPadding(dp(4), dp(7), dp(4), dp(7));
        LinearLayout.LayoutParams itemLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemLp.bottomMargin = dp(6);
        item.setLayoutParams(itemLp);

        FrameLayout circleWrap = new FrameLayout(this);
        circleWrap.setLayoutParams(new LinearLayout.LayoutParams(dp(52), dp(52)));
        final View circle = new View(this);
        circle.setBackground(circleBg(railCircleColor, false));
        circleWrap.addView(circle, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        TextView glyphTv = new TextView(this);
        glyphTv.setText(glyph);
        glyphTv.setTextColor(whiteCircle ? Color.parseColor("#FF0000") : Color.WHITE);
        glyphTv.setTextSize(whiteCircle ? 24 : 22);
        glyphTv.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        glyphTv.setGravity(Gravity.CENTER);
        circleWrap.addView(glyphTv, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        item.addView(circleWrap);

        final TextView label = new TextView(this);
        label.setText(labelText);
        label.setTextColor(COL_TEXT_DIM);
        label.setTextSize(12);
        label.setGravity(Gravity.CENTER);
        label.setMaxLines(2);
        LinearLayout.LayoutParams labelLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        labelLp.topMargin = dp(4);
        label.setLayoutParams(labelLp);
        item.addView(label);

        item.setOnClickListener(click);
        item.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View v, boolean f) {
                circle.setBackground(circleBg(railCircleColor, f));
                circle.setScaleX(f ? 1.14f : 1f);
                circle.setScaleY(f ? 1.14f : 1f);
                glyphTv.setScaleX(f ? 1.14f : 1f);
                glyphTv.setScaleY(f ? 1.14f : 1f);
                label.setTextColor(f ? Color.WHITE : COL_TEXT_DIM);
                label.setTypeface(f ? android.graphics.Typeface.DEFAULT_BOLD
                        : android.graphics.Typeface.DEFAULT);
            }
        });
        rail.addView(item);
    }

    private void focusApps() {
        if (grid != null) {
            grid.requestFocus();
            if (adapter != null && adapter.getCount() > 0) grid.setSelection(0);
        }
    }

    private void openGlobalSearch() {
        try {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory("android.intent.category.APP_SEARCH");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (getPackageManager().resolveActivity(i, 0) != null) {
                startActivity(i);
                return;
            }
        } catch (Exception ignored) { }
        Toast.makeText(this, t("notAvailable"), Toast.LENGTH_SHORT).show();
    }

    private void toggleLanguage() {
        if (lang == null || lang.available().size() <= 1) {
            Toast.makeText(this, t("onlyEnglish"), Toast.LENGTH_LONG).show();
            return;
        }
        String next = lang.nextCode();
        prefs.edit().putString(KEY_LANG, next).apply();
        lang.setCurrent(next);
        recreate();
    }

    // ---------- rearrange ----------
    private void setEditMode(boolean on) {
        editMode = on;
        if (!on) movingPos = -1;
        if (hintView != null) hintView.setVisibility(on ? View.VISIBLE : View.GONE);
        if (editBar != null) editBar.setVisibility(on ? View.VISIBLE : View.GONE);
        if (adapter != null) adapter.notifyDataSetChanged();
        if (on) focusApps();
    }

    private void handleRearrangeTap(int position) {
        if (adapter == null) return;
        if (movingPos == -1) {
            movingPos = position;
        } else if (movingPos == position) {
            movingPos = -1;
        } else {
            adapter.moveItem(movingPos, position);
            movingPos = -1;
            saveOrder();
        }
        adapter.notifyDataSetChanged();
    }

    private void saveOrder() {
        if (adapter == null) return;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(adapter.getItem(i).component.flattenToString());
        }
        prefs.edit().putString(KEY_ORDER, sb.toString()).apply();
    }

    // ---------- apps ----------
    private void updateClock() {
        if (clockView == null) return;
        Date now = new Date();
        Locale loc = lang != null ? lang.locale() : Locale.US;
        clockView.setText(new SimpleDateFormat("HH:mm", Locale.US).format(now));
        dateView.setText(new SimpleDateFormat("EEEE, d MMMM yyyy", loc).format(now));
    }

    private void loadApps() {
        PackageManager pm = getPackageManager();
        List<AppItem> items = new ArrayList<AppItem>();
        Set<String> seen = new HashSet<String>();

        String[] categories = { Intent.CATEGORY_LAUNCHER, Intent.CATEGORY_LEANBACK_LAUNCHER };
        for (String cat : categories) {
            Intent q = new Intent(Intent.ACTION_MAIN);
            q.addCategory(cat);
            List<ResolveInfo> list = pm.queryIntentActivities(q, 0);
            for (ResolveInfo ri : list) {
                if (ri.activityInfo == null) continue;
                String pkg = ri.activityInfo.packageName;
                if (pkg.equals(getPackageName())) continue;
                ComponentName cn = new ComponentName(pkg, ri.activityInfo.name);
                String key = cn.flattenToString();
                if (!seen.add(key)) continue;
                CharSequence label = ri.loadLabel(pm);
                if (label == null || label.length() == 0) continue;
                AppItem it = new AppItem();
                it.label = label.toString();
                try { it.icon = ri.loadIcon(pm); } catch (Exception e) { it.icon = null; }
                it.component = cn;
                items.add(it);
            }
        }

        // Apply the user's saved order first; new apps fall back to alphabetical.
        final Map<String, Integer> orderIdx = new HashMap<String, Integer>();
        String saved = prefs.getString(KEY_ORDER, null);
        if (saved != null) {
            String[] parts = saved.split("\n");
            for (int i = 0; i < parts.length; i++) orderIdx.put(parts[i], i);
        }
        Collections.sort(items, new Comparator<AppItem>() {
            @Override public int compare(AppItem a, AppItem b) {
                Integer ia = orderIdx.get(a.component.flattenToString());
                Integer ib = orderIdx.get(b.component.flattenToString());
                if (ia != null && ib != null) return ia - ib;
                if (ia != null) return -1;
                if (ib != null) return 1;
                return a.label.compareToIgnoreCase(b.label);
            }
        });

        adapter = new AppAdapter(items);
        grid.setAdapter(adapter);
    }

    private void launch(AppItem item) {
        try {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_LAUNCHER);
            i.setComponent(item.component);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(i);
            return;
        } catch (Exception ignored) { }
        try {
            Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
            i.setComponent(item.component);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            startActivity(i);
            return;
        } catch (Exception ignored) { }
        try {
            Intent i = getPackageManager().getLaunchIntentForPackage(item.component.getPackageName());
            if (i != null) {
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return;
            }
        } catch (Exception ignored) { }
        Toast.makeText(this, t("couldNotOpen") + " " + item.label, Toast.LENGTH_SHORT).show();
    }

    private void safeStart(Intent i) {
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, t("notAvailable"), Toast.LENGTH_SHORT).show();
        }
    }

    // ---------- YouTube / Netflix ----------
    private boolean launchByPackageList(String[] pkgs) {
        PackageManager pm = getPackageManager();
        for (String pkg : pkgs) {
            try {
                Intent i = pm.getLaunchIntentForPackage(pkg);
                if (i != null) {
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    return true;
                }
            } catch (Exception ignored) { }
        }
        return false;
    }

    private boolean launchByLabelKeyword(String[] keywords) {
        PackageManager pm = getPackageManager();
        String[] categories = { Intent.CATEGORY_LAUNCHER, Intent.CATEGORY_LEANBACK_LAUNCHER };
        for (String cat : categories) {
            Intent q = new Intent(Intent.ACTION_MAIN);
            q.addCategory(cat);
            List<ResolveInfo> list = pm.queryIntentActivities(q, 0);
            for (ResolveInfo ri : list) {
                if (ri.activityInfo == null) continue;
                CharSequence lab = ri.loadLabel(pm);
                String hay = ((lab == null ? "" : lab.toString()) + " "
                        + ri.activityInfo.packageName).toLowerCase(Locale.US);
                for (String k : keywords) {
                    if (hay.contains(k)) {
                        AppItem it = new AppItem();
                        it.label = lab == null ? k : lab.toString();
                        it.component = new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
                        launch(it);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isPkgInstalled(String pkg) {
        try {
            getPackageManager().getPackageInfo(pkg, 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // SmartTube ships one exported launcher activity per section (verified
    // in the 32.68 APK manifest/dex). Opening the section activity directly
    // is the only reliable route in: every VIEW intent to youtube.com lands
    // on SmartTube's SplashActivity — the only activity with a youtube.com
    // intent-filter — which forwards into MainActivity and resumes whatever
    // section the existing task was left on (Settings, from sign-in),
    // ignoring the URL. That is why the v2.2/v2.3 URL approaches always
    // opened Settings regardless of the card tapped; CLEAR_TASK cannot fix
    // routing that happens inside SmartTube.
    private static final String[] SMARTTUBE_PKGS = {
            "org.smarttube.stable", "org.smarttube.beta",
            "com.teamsmarttube.tv", "com.teamsmarttube.tv.beta"};
    private static final String SMARTTUBE_LAUNCHER_PREFIX =
            "com.liskovsoft.smartyoutubetv2.tv.launchers.";

    private boolean openSmartTubeSection(String sectionActivity) {
        for (String pkg : SMARTTUBE_PKGS) {
            if (!isPkgInstalled(pkg)) continue;
            try {
                Intent i = new Intent();
                i.setComponent(new ComponentName(pkg,
                        SMARTTUBE_LAUNCHER_PREFIX + sectionActivity));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return true;
            } catch (Exception ignored) { }
        }
        return false;
    }

    private void openYouTubeSection(String sectionActivity) {
        if (openSmartTubeSection(sectionActivity)) return;
        // SmartTube missing, or its section entry points were disabled in
        // SmartTube's own settings: fall back to just opening the app.
        launchYouTubeApp();
    }

    private void launchYouTubeApp() {
        // SmartTube first: on this projector the official YouTube app is
        // blocked by Google ("not supported on this device"), SmartTube is
        // the working YouTube client. Official packages stay as fallback.
        if (openSmartTubeSection("HomeLauncherActivity")) return;
        if (launchByPackageList(new String[]{
                "org.smarttube", "com.teamsmart.videomanager.tv",
                "com.google.android.youtube.tv", "com.google.android.youtube"})) return;
        if (launchByLabelKeyword(new String[]{"smarttube", "smart tube"})) return;
        if (launchByLabelKeyword(new String[]{"youtube"})) return;
        Toast.makeText(this, t("notInstalled") + " SmartTube", Toast.LENGTH_SHORT).show();
    }

    private void launchVtvgoApp() {
        if (launchByPackageList(new String[]{
                "vn.vtv.vtvgotv", "vn.vtv.vtvgo"})) return;
        if (launchByLabelKeyword(new String[]{"vtvgo", "vtv go"})) return;
        Toast.makeText(this, t("notInstalled") + " VTVgo TV", Toast.LENGTH_SHORT).show();
    }

    // ---------- exit ----------
    private void exitToStockHome() {
        PackageManager pm = getPackageManager();
        Intent homeQ = new Intent(Intent.ACTION_MAIN);
        homeQ.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> homes = pm.queryIntentActivities(homeQ, 0);
        ResolveInfo best = null;
        for (ResolveInfo ri : homes) {
            if (ri.activityInfo == null) continue;
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(getPackageName())) continue;
            if (best == null) best = ri;
            String hay = pkg.toLowerCase(Locale.US);
            if (hay.contains("jmgo") || hay.contains("bonfire") || hay.contains("desktop")) {
                best = ri;
                break;
            }
        }
        if (best != null) {
            try {
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.addCategory(Intent.CATEGORY_HOME);
                i.setComponent(new ComponentName(best.activityInfo.packageName, best.activityInfo.name));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return;
            } catch (Exception ignored) { }
        }
        Toast.makeText(this, t("noStockHome"), Toast.LENGTH_LONG).show();
        finish();
    }

    // ---------- settings ----------
    private void openProjectorSettings() {
        PackageManager pm = getPackageManager();
        // Prefer JMGO/Bonfire's own settings (picture, focus, keystone...).
        try {
            Intent q = new Intent(Intent.ACTION_MAIN);
            q.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> all = pm.queryIntentActivities(q, 0);
            ResolveInfo jmgoBest = null;
            for (ResolveInfo ri : all) {
                if (ri.activityInfo == null) continue;
                String hay = (ri.activityInfo.packageName + "/" + ri.activityInfo.name)
                        .toLowerCase(Locale.US);
                if (hay.contains("setting") && (hay.contains("jmgo") || hay.contains("bonfire"))) {
                    jmgoBest = ri;
                    break;
                }
            }
            if (jmgoBest != null) {
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.addCategory(Intent.CATEGORY_LAUNCHER);
                i.setComponent(new ComponentName(jmgoBest.activityInfo.packageName, jmgoBest.activityInfo.name));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return;
            }
        } catch (Exception ignored) { }
        String[][] guesses = {
            {"com.jmgo.settings", "com.jmgo.settings.Settings"},
            {"com.jmgo.systemsettings", "com.jmgo.systemsettings.Settings"},
        };
        for (String[] g : guesses) {
            try {
                Intent i = new Intent();
                i.setComponent(new ComponentName(g[0], g[1]));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                return;
            } catch (Exception ignored) { }
        }
        // Last resort: whatever settings the ROM exposes, else the remote hint.
        openSystemSettings();
    }

    private void openSystemSettings() {
        PackageManager pm = getPackageManager();
        try {
            Intent i = new Intent(Settings.ACTION_SETTINGS);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (pm.resolveActivity(i, 0) != null) {
                startActivity(i);
                return;
            }
        } catch (Exception ignored) { }
        try {
            Intent i = new Intent();
            i.setComponent(new ComponentName("com.android.settings", "com.android.settings.Settings"));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            return;
        } catch (Exception ignored) { }
        // Scan for any settings-like launchable app.
        try {
            Intent q = new Intent(Intent.ACTION_MAIN);
            q.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> all = pm.queryIntentActivities(q, 0);
            for (ResolveInfo ri : all) {
                if (ri.activityInfo == null) continue;
                String hay = (ri.activityInfo.packageName + "/" + ri.activityInfo.name)
                        .toLowerCase(Locale.US);
                if (hay.contains("setting")) {
                    Intent i = new Intent(Intent.ACTION_MAIN);
                    i.addCategory(Intent.CATEGORY_LAUNCHER);
                    i.setComponent(new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name));
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    return;
                }
            }
        } catch (Exception ignored) { }
        Toast.makeText(this, t("settingsHint"), Toast.LENGTH_LONG).show();
    }

    class AppAdapter extends BaseAdapter {
        private final List<AppItem> items;
        private int selected = 0;
        AppAdapter(List<AppItem> items) { this.items = items; }
        void setSelected(int p) {
            if (p == selected) return;
            selected = p;
            notifyDataSetChanged();
        }
        void moveItem(int from, int to) {
            AppItem it = items.remove(from);
            items.add(to, it);
            selected = to;
        }
        @Override public int getCount() { return items.size(); }
        @Override public AppItem getItem(int p) { return items.get(p); }
        @Override public long getItemId(int p) { return p; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout cell;
            ImageView icon;
            TextView label;
            if (convertView == null) {
                cell = new LinearLayout(MainActivity.this);
                cell.setOrientation(LinearLayout.VERTICAL);
                cell.setGravity(Gravity.CENTER);
                cell.setPadding(dp(6), dp(10), dp(6), dp(8));
                cell.setFocusable(false);
                cell.setFocusableInTouchMode(false);
                cell.setClickable(false);
                icon = new ImageView(MainActivity.this);
                icon.setLayoutParams(new LinearLayout.LayoutParams(dp(52), dp(52)));
                icon.setTag("icon");
                label = new TextView(MainActivity.this);
                label.setTextSize(12);
                label.setGravity(Gravity.CENTER);
                label.setMaxLines(1);
                label.setEllipsize(android.text.TextUtils.TruncateAt.END);
                label.setTag("label");
                LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                llp.topMargin = dp(6);
                label.setLayoutParams(llp);
                cell.addView(icon);
                cell.addView(label);
            } else {
                cell = (LinearLayout) convertView;
                icon = (ImageView) cell.findViewWithTag("icon");
                label = (TextView) cell.findViewWithTag("label");
            }
            AppItem it = items.get(position);
            if (it.icon != null) icon.setImageDrawable(it.icon);
            label.setText(it.label);
            if (position == movingPos) {
                cell.setBackground(movingBg());
                label.setTextColor(Color.WHITE);
                cell.setScaleX(1.08f);
                cell.setScaleY(1.08f);
            } else if (position == selected) {
                cell.setBackground(cardBg(true));
                label.setTextColor(Color.WHITE);
                cell.setScaleX(1.05f);
                cell.setScaleY(1.05f);
            } else {
                cell.setBackground(cardBg(false));
                label.setTextColor(COL_TEXT_DIM);
                cell.setScaleX(1f);
                cell.setScaleY(1f);
            }
            return cell;
        }
    }
}
