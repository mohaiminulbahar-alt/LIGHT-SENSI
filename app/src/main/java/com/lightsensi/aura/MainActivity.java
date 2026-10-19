package com.lightsensi.aura;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import rikka.shizuku.Shizuku;

public class MainActivity extends Activity {

    final int RED = 0xFFFF1E3C;
    final int GREEN = 0xFF22D87B;
    final int ORANGE = 0xFFFFA12A;

    float d;
    LinearLayout root;
    TextView status;
    TextView logs;
    ScrollView logBox;
    ProgressBar bar;
    View dot;
    Handler ui = new Handler(Looper.getMainLooper());
    boolean busy = false;

    int dp(float n) {
        return (int) (n * d);
    }

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        d = getResources().getDisplayMetrics().density;
        getWindow().setStatusBarColor(0xFF100002);
        getWindow().setNavigationBarColor(Color.BLACK);

        FrameLayout base = new FrameLayout(this);
        base.setBackgroundColor(0xFF070001);

        int aura = getResources().getIdentifier(
                "aura", "drawable", getPackageName()
        );

        if (aura != 0) {
            ImageView bg = new ImageView(this);
            bg.setImageResource(aura);
            bg.setScaleType(ImageView.ScaleType.CENTER_CROP);
            bg.setAlpha(0.30f);
            base.addView(bg, new FrameLayout.LayoutParams(-1, -1));
        }

        View shade = new View(this);
        shade.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0x661A0005, 0xB8000000, 0xF5000000}
        ));
        base.addView(shade, new FrameLayout.LayoutParams(-1, -1));

        ScrollView page = new ScrollView(this);
        page.setVerticalScrollBarEnabled(false);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(25), dp(16), dp(32));

        page.addView(root);
        base.addView(page, new FrameLayout.LayoutParams(-1, -1));

        header(aura);
        shizukuCard();

        Button all = button("ACTIVATE ALL  •  MAX AURA", 1, 62);
        all.setTextSize(15);
        all.setOnClickListener(v -> {
            StringBuilder script = new StringBuilder();
            for (int i = 2; i < Cmd.S.length; i += 3) {
                script.append(Cmd.S[i]).append("\n");
            }
            run("MAX AURA", script.toString(), all);
        });
        root.addView(all);

        sections(Cmd.S, 1);
        sections(Cmd.X, 1 + Cmd.S.length / 3);
        terminal();

        setContentView(base);

        Shizuku.addRequestPermissionResultListener(
                (code, result) -> ui.post(this::state)
        );

        state();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (status != null) state();
    }

    void header(int aura) {
        LinearLayout head = row();

        ImageView logo = new ImageView(this);
        if (aura != 0) {
            logo.setImageResource(aura);
            logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            logo.setImageResource(android.R.drawable.sym_def_app_icon);
        }

        logo.setBackground(circle(0x66000000, 0xAAFF4058));
        logo.setPadding(dp(3), dp(3), dp(3), dp(3));

        logo.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View v, Outline o) {
                o.setOval(0, 0, v.getWidth(), v.getHeight());
            }
        });

        logo.setClipToOutline(true);

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(dp(66), dp(66));

        lp.rightMargin = dp(13);
        logo.setLayoutParams(lp);
        head.addView(logo);

        LinearLayout text = new LinearLayout(this);
        text.setOrientation(LinearLayout.VERTICAL);

        TextView name = tv("LIGHT SENSI", 27, 0xFFFFF3F4, true);
        name.setLetterSpacing(0.05f);
        name.setShadowLayer(dp(14), 0, 0, RED);

        TextView sub = tv("KIRA  AURA  ENGINE", 10, 0xFFFF8090, true);
        sub.setLetterSpacing(0.20f);

        text.addView(name);
        text.addView(sub);
        head.addView(text);

        root.addView(head);

        LinearLayout chips = row();
        chips.setPadding(0, dp(16), 0, 0);

        chips.addView(chip("ANDROID", android.os.Build.VERSION.RELEASE));
        chips.addView(chip(
                "CORES",
                String.valueOf(Runtime.getRuntime().availableProcessors())
        ));
        chips.addView(chip("DEVICE", android.os.Build.MODEL));

        root.addView(chips);
    }

    void shizukuCard() {
        LinearLayout card = glassCard();
        LinearLayout line = row();

        dot = new View(this);
        dot.setBackground(circle(RED, RED));

        LinearLayout.LayoutParams dpv =
                new LinearLayout.LayoutParams(dp(10), dp(10));

        dpv.rightMargin = dp(10);
        dot.setLayoutParams(dpv);

        line.addView(dot);

        status = tv("SHIZUKU : CHECKING", 12, 0xFFFFB7BE, true);
        status.setLetterSpacing(0.08f);

        line.addView(status);
        card.addView(line);

        Button connect = button("CONNECT SHIZUKU", 0, 48);
        connect.setOnClickListener(v -> connect());

        card.addView(connect);
        root.addView(card);
    }

    void sections(String[] list, int start) {
        for (int i = 0; i < list.length; i += 3) {
            String title = list[i];
            String desc = list[i + 1];
            String script = list[i + 2];

            LinearLayout card = glassCard();
            LinearLayout top = row();

            top.addView(badge(start + i / 3));

            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);

            info.addView(tv(title, 15, 0xFFFFF3F4, true));
            info.addView(tv(desc, 10, 0xFFB89FA5, false));

            top.addView(info);
            card.addView(top);

            int style = 1;
            String label = "APPLY";

            if (title.contains("EXTREME")) {
                label = "EXTREME";
                style = 2;
            }

            if (title.contains("RESET")) {
                label = "RESET";
                style = 0;
            }

            if (title.contains("INFO")) {
                label = "CHECK";
                style = 0;
            }

            Button apply = button(label, style, 46);

            final String t = title;
            final String s = script;

            apply.setOnClickListener(v -> run(t, s, apply));

            card.addView(apply);
            root.addView(card);
        }
    }

    void terminal() {
        TextView label = tv("TERMINAL OUTPUT", 11, 0xFFFF8090, true);
        label.setLetterSpacing(0.20f);
        label.setPadding(dp(6), dp(22), 0, dp(8));
        root.addView(label);

        bar = new ProgressBar(
                this, null, android.R.attr.progressBarStyleHorizontal
        );

        bar.setIndeterminate(true);
        bar.setIndeterminateTintList(ColorStateList.valueOf(RED));
        bar.setVisibility(View.INVISIBLE);
        bar.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(3)));
        root.addView(bar);

        logBox = new ScrollView(this);
        logBox.setVerticalScrollBarEnabled(false);
        logBox.setBackground(glass(20, 0x3A000000, 0x18000000, 0x44FFFFFF));
        logBox.setPadding(dp(13), dp(13), dp(13), dp(13));
        logBox.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(245)));

        logs = tv("[ LIGHT SENSI ] Kira Aura ready.", 11, 0xFFFFB0B8, false);
        logs.setTypeface(Typeface.MONOSPACE);

        logBox.addView(logs);
        root.addView(logBox);

        Button clear = button("CLEAR LOG", 0, 44);
        clear.setTextSize(11);
        clear.setOnClickListener(v -> logs.setText("[ terminal cleared ]"));
        root.addView(clear);

        TextView foot = tv(
                "LIGHT SENSI • KIRA GLASS EDITION • SHIZUKU",
                9,
                0xFF795258,
                true
        );

        foot.setGravity(Gravity.CENTER);
        foot.setLetterSpacing(0.12f);
        foot.setPadding(0, dp(20), 0, 0);

        root.addView(foot);
    }

    LinearLayout row() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.HORIZONTAL);
        v.setGravity(Gravity.CENTER_VERTICAL);
        v.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        return v;
    }

    LinearLayout glassCard() {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setPadding(dp(16), dp(15), dp(16), dp(15));
        v.setBackground(glass(25, 0x38FFFFFF, 0x16000000, 0x4DFFFFFF));
        v.setElevation(dp(4));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, -2);

        p.topMargin = dp(13);
        v.setLayoutParams(p);

        return v;
    }

    LinearLayout chip(String a, String b) {
        LinearLayout v = new LinearLayout(this);
        v.setOrientation(LinearLayout.VERTICAL);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(4), dp(9), dp(4), dp(9));
        v.setBackground(glass(18, 0x2CFFFFFF, 0x14000000, 0x3BFFFFFF));

        TextView x = tv(a, 8, 0xFFC6AEB2, true);
        x.setGravity(Gravity.CENTER);

        TextView y = tv(b, 11, 0xFFFF9DA8, true);
        y.setGravity(Gravity.CENTER);
        y.setSingleLine(true);

        v.addView(x);
        v.addView(y);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(0, -2, 1f);

        p.leftMargin = dp(4);
        p.rightMargin = dp(4);

        v.setLayoutParams(p);

        return v;
    }

    TextView badge(int n) {
        TextView v = tv(String.valueOf(n), 12, Color.WHITE, true);
        v.setGravity(Gravity.CENTER);
        v.setBackground(circle(RED, 0x66FFFFFF));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(dp(30), dp(30));

        p.rightMargin = dp(12);
        v.setLayoutParams(p);

        return v;
    }

    TextView tv(String s, int size, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(size);
        v.setTextColor(color);

        if (bold) v.setTypeface(Typeface.DEFAULT_BOLD);

        return v;
    }

    Button button(String s, int style, int height) {
        Button v = new Button(this);

        v.setText(s);
        v.setTextSize(13);
        v.setTextColor(Color.WHITE);
        v.setTypeface(Typeface.DEFAULT_BOLD);
        v.setLetterSpacing(0.07f);
        v.setAllCaps(true);
        v.setStateListAnimator(null);

        int a = 0x3CFFFFFF;
        int b = 0x16000000;

        if (style == 1) {
            a = 0xD9FF1E3C;
            b = 0xAA780011;
        }

        if (style == 2) {
            a = 0xDDFF801A;
            b = 0xAA703000;
        }

        v.setBackground(new RippleDrawable(
                ColorStateList.valueOf(0x55FFFFFF),
                glass(18, a, b, 0x66FFFFFF),
                null
        ));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, dp(height));

        p.topMargin = dp(11);
        v.setLayoutParams(p);

        return v;
    }

    GradientDrawable glass(int radius, int a, int b, int border) {
        GradientDrawable v = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{a, b}
        );

        v.setCornerRadius(dp(radius));
        v.setStroke(Math.max(1, dp(1)), border);

        return v;
    }

    GradientDrawable circle(int color, int border) {
        GradientDrawable v = new GradientDrawable();
        v.setShape(GradientDrawable.OVAL);
        v.setColor(color);
        v.setStroke(dp(1), border);
        return v;
    }

    boolean ready() {
        try {
            return Shizuku.pingBinder()
                    && Shizuku.checkSelfPermission()
                    == PackageManager.PERMISSION_GRANTED;

        } catch (Throwable e) {
            return false;
        }
    }

    void state() {
        try {
            GradientDrawable bg = (GradientDrawable) dot.getBackground();

            if (!Shizuku.pingBinder()) {
                status.setText("SHIZUKU : NOT RUNNING");
                status.setTextColor(0xFFFF9CA7);
                bg.setColor(RED);

            } else if (ready()) {
                status.setText("SHIZUKU : CONNECTED");
                status.setTextColor(GREEN);
                bg.setColor(GREEN);

            } else {
                status.setText("SHIZUKU : PERMISSION NEEDED");
                status.setTextColor(ORANGE);
                bg.setColor(ORANGE);
            }

        } catch (Throwable e) {
            status.setText("SHIZUKU : ERROR");
        }
    }

    void connect() {
        try {
            if (!Shizuku.pingBinder()) {
                add("! Shizuku app open kore START koro.");
                return;
            }

            if (Shizuku.checkSelfPermission()
                    != PackageManager.PERMISSION_GRANTED) {

                Shizuku.requestPermission(7);

            } else {
                add("+ Already connected");
                state();
            }

        } catch (Throwable e) {
            add("! " + e);
        }
    }

    void add(String s) {
        ui.post(() -> {
            logs.append("\n" + s);
            logBox.post(() -> logBox.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }

    void run(String title, String script, Button bt) {
        if (!ready()) {
            add("! Age SHIZUKU CONNECT koro");

            Toast.makeText(
                    this,
                    "Shizuku connect koro",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (busy) {
            Toast.makeText(this, "Wait koro...", Toast.LENGTH_SHORT).show();
            return;
        }

        busy = true;
        bar.setVisibility(View.VISIBLE);
        bt.setText("RUNNING...");
        add("\n>> " + title);

        new Thread(() -> {
            boolean success = true;

            try {
                Method m = Shizuku.class.getDeclaredMethod(
                        "newProcess",
                        String[].class,
                        String[].class,
                        String.class
                );

                m.setAccessible(true);

                Process p = (Process) m.invoke(
                        null,
                        new Object[]{
                                new String[]{
                                        "sh",
                                        "-c",
                                        "{\n" + script + "\n} 2>&1"
                                },
                                null,
                                null
                        }
                );

                BufferedReader r = new BufferedReader(
                        new InputStreamReader(p.getInputStream())
                );

                String line;

                while ((line = r.readLine()) != null) {
                    if (!line.trim().isEmpty()) add("   " + line);
                }

                p.waitFor();

            } catch (Throwable e) {
                success = false;
                add("! Error: " + e);
            }

            boolean ok = success;

            ui.post(() -> {
                busy = false;
                bar.setVisibility(View.INVISIBLE);

                if (ok) {
                    bt.setText("APPLIED ✓");
                    bt.setBackground(new RippleDrawable(
                            ColorStateList.valueOf(0x55FFFFFF),
                            glass(18, 0xCC22D87B, 0x99006735, 0x66FFFFFF),
                            null
                    ));
                } else {
                    bt.setText("RETRY");
                }
            });

            add("<< DONE : " + title);

        }).start();
    }
            }
