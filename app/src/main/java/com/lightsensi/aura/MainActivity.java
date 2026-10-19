package com.lightsensi.aura;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import rikka.shizuku.Shizuku;

public class MainActivity extends Activity {

    TextView logT, stT;
    ScrollView logBox;
    final Handler h = new Handler(Looper.getMainLooper());
    volatile boolean busy = false;
    int d;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.BLACK);
        d = (int) getResources().getDisplayMetrics().density;

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(14 * d, 18 * d, 14 * d, 26 * d);
        ScrollView page = new ScrollView(this);
        page.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF2A0000, 0xFF000000}));
        page.addView(root);

        TextView t = tv("LIGHT SENSI", 32, 0xFFFF0022, true);
        t.setGravity(Gravity.CENTER);
        t.setShadowLayer(26, 0, 0, 0xFFFF0000);
        root.addView(t);
        TextView s = tv("AURA FARMING | RED EDITION | SHIZUKU", 11, 0xFF999999, true);
        s.setGravity(Gravity.CENTER);
        root.addView(s);

        LinearLayout c0 = card();
        stT = tv("Shizuku: checking...", 14, 0xFFFF3344, true);
        c0.addView(stT);
        c0.addView(btn("CONNECT SHIZUKU", v -> connect()));
        root.addView(c0);

        Button all = btn("ACTIVATE ALL - MAX AURA", v -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < Cmd.S.length; i += 3) sb.append(Cmd.S[i]).append("\n");
            run("MAX AURA", sb.toString());
        });
        all.setTextSize(16);
        root.addView(all);

        add(root, Cmd.S);
        add(root, Cmd.X);

        TextView lt = tv("TERMINAL LOG", 14, 0xFFFF0022, true);
        lt.setPadding(0, 16 * d, 0, 4 * d);
        root.addView(lt);
        logBox = new ScrollView(this);
        logBox.setBackground(box(0xFF000000, 0xFF0A0000));
        logBox.setPadding(8 * d, 8 * d, 8 * d, 8 * d);
        logBox.setLayoutParams(new LinearLayout.LayoutParams(-1, 260 * d));
        logT = tv("LIGHT SENSI ready...", 11, 0xFFFF5555, false);
        logT.setTypeface(Typeface.MONOSPACE);
        logBox.addView(logT);
        root.addView(logBox);
        root.addView(btn("CLEAR LOG", v -> logT.setText("")));

        setContentView(page);

        Shizuku.addRequestPermissionResultListener((a, b2) -> h.post(this::refresh));
        Shizuku.addBinderReceivedListenerSticky(() -> h.post(this::refresh));
        Shizuku.addBinderDeadListener(() -> h.post(this::refresh));
        refresh();
    }

    void add(LinearLayout root, String[] arr) {
        for (int i = 0; i < arr.length; i += 3) {
            final String title = arr[i];
            final String script = arr[i + 2];
            LinearLayout c = card();
            TextView h1 = tv(title, 17, 0xFFFF0022, true);
            h1.setShadowLayer(12, 0, 0, 0xFFFF0000);
            c.addView(h1);
            c.addView(tv(arr[i + 1], 11, 0xFF999999, false));
            c.addView(btn("APPLY", v -> run(title, script)));
            root.addView(c);
        }
    }

    GradientDrawable box(int a, int b) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{a, b});
        g.setCornerRadius(14 * d);
        g.setStroke(2 * d, 0xFFFF0022);
        return g;
    }

    LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(13 * d, 11 * d, 13 * d, 11 * d);
        c.setBackground(box(0xFF160000, 0xFF050505));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = 11 * d;
        c.setLayoutParams(p);
        return c;
    }

    TextView tv(String s, int size, int color, boolean bold) {
        TextView x = new TextView(this);
        x.setText(s);
        x.setTextSize(size);
        x.setTextColor(color);
        if (bold) x.setTypeface(Typeface.DEFAULT_BOLD);
        return x;
    }

    Button btn(String s, View.OnClickListener l) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(Color.WHITE);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setBackground(box(0xFFFF0022, 0xFF3A0000));
        b.setOnClickListener(l);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, 50 * d);
        p.topMargin = 8 * d;
        b.setLayoutParams(p);
        return b;
    }

    boolean ready() {
        try {
            return Shizuku.pingBinder() && !Shizuku.isPreV11()
                    && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Throwable e) {
            return false;
        }
    }

    void refresh() {
        try {
            if (!Shizuku.pingBinder()) {
                stT.setText("Shizuku: NOT RUNNING");
                stT.setTextColor(0xFFFF3344);
            } else if (ready()) {
                stT.setText("Shizuku: CONNECTED - READY");
                stT.setTextColor(0xFF00FF66);
            } else {
                stT.setText("Shizuku: permission lagbe");
                stT.setTextColor(0xFFFFAA00);
            }
        } catch (Throwable e) {
            stT.setText("Shizuku error");
        }
    }

    void connect() {
        try {
            if (!Shizuku.pingBinder()) {
                log("Shizuku app open kore Start koro.");
                return;
            }
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED)
                Shizuku.requestPermission(7);
            else {
                log("Already connected");
                refresh();
            }
        } catch (Throwable e) {
            log("ERR " + e);
        }
    }

    void log(String s) {
        h.post(() -> {
            logT.append("\n" + s);
            logBox.post(() -> logBox.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }

    void run(String title, String script) {
        if (!ready()) {
            log("Age Shizuku CONNECT koro!");
            Toast.makeText(this, "Shizuku connect koro", Toast.LENGTH_SHORT).show();
            return;
        }
        if (busy) {
            Toast.makeText(this, "Wait koro...", Toast.LENGTH_SHORT).show();
            return;
        }
        busy = true;
        log("\n==== " + title + " ====");
        new Thread(() -> {
            try {
                Method m = Shizuku.class.getDeclaredMethod("newProcess", String[].class, String[].class, String.class);
                m.setAccessible(true);
                Process p = (Process) m.invoke(null,
                        new String[]{"sh", "-c", "{ " + script + " } 2>&1"}, null, null);
                BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = r.readLine()) != null) if (!line.trim().isEmpty()) log("> " + line);
                p.waitFor();
                log("DONE: " + title);
            } catch (Throwable e) {
                log("Error: " + e);
            } finally {
                busy = false;
            }
        }).start();
    }
          }
