package com.lightsensi.aura;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
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
import java.util.ArrayList;

import rikka.shizuku.Shizuku;

public class MainActivity extends Activity {

    final int RED = 0xFFFF1E3C;
    final int GREEN = 0xFF20E080;
    final int ORANGE = 0xFFFFA229;

    float d;
    TextView status, log, title;
    View dot;
    ScrollView logBox;
    ProgressBar progress;
    LinearLayout content;
    Handler h = new Handler(Looper.getMainLooper());
    ArrayList<View> animations = new ArrayList<>();
    volatile boolean busy = false;

    int dp(float n) {
        return (int) (n * d);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        d = getResources().getDisplayMetrics().density;
        getWindow().setStatusBarColor(0xFF100002);
        getWindow().setNavigationBarColor(Color.BLACK);

        FrameLayout base = new FrameLayout(this);
        base.setBackgroundColor(0xFF070001);

        int aura = getResources().getIdentifier(
                "aura", "drawable", getPackageName()
        );

        if (aura != 0) {
            ImageView background = new ImageView(this);
            background.setImageResource(aura);
            background.setScaleType(ImageView.ScaleType.CENTER_CROP);
            background.setAlpha(0.30f);
            base.addView(background, new FrameLayout.LayoutParams(-1, -1));
        }

        View darkLayer = new View(this);
        darkLayer.setBackground(new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{
                        0x66130004,
                        0xA8000000,
                        0xF2000000
                }
        ));
        base.addView(darkLayer, new FrameLayout.LayoutParams(-1, -1));

        ScrollView page = new ScrollView(this);
        page.setVerticalScrollBarEnabled(false);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), dp(26), dp(16), dp(32));

        page.addView(content);
        base.addView(page, new FrameLayout.LayoutParams(-1, -1));

        buildHeader(aura);
        buildShizukuCard();
        buildMaxButton();
        addSections(Cmd.S, 1);
        addSections(Cmd.X, 1 + Cmd.S.length / 3);
        buildTerminal();

        setContentView(base);

        page.post(() -> {
            for (int i = 0; i < animations.size(); i++) {
                View v = animations.get(i);
                v.setTranslationY(dp(30));
                v.animate()
                        .alpha(1f)
                        .translationY(0)
                        .setDuration(360)
                        .setStartDelay(i * 40L)
                        .start();
            }
        });

        Shizuku.addRequestPermissionResultListener(
                (requestCode, grantResult) -> h.post(this::updateStatus)
        );

        Shizuku.addBinderReceivedListenerSticky(
                () -> h.post(this::updateStatus)
        );

        Shizuku.addBinderDeadListener(
                () -> h.post(this::updateStatus)
        );

        updateStatus();
    }

    void buildHeader(int aura) {
        LinearLayout header = row();

        ImageView logo = new ImageView(this);

        if (aura != 0) {
            logo.setImageResource(aura);
            logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        } else {
            logo.setImageResource(android.R.drawable.sym_def_app_icon);
        }

        GradientDrawable logoRing = new GradientDrawable();
        logoRing.setShape(GradientDrawable.OVAL);
        logoRing.setColors(new int[]{0x88FF1E3C, 0x22000000});
        logoRing.setStroke(dp(2), 0xBBFFFFFF);

        logo.setBackground(logoRing);
        logo.setPadding(dp(3), dp(3), dp(3), dp(3));

        logo.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });

        logo.setClipToOutline(true);

        LinearLayout.LayoutParams logoSize =
                new LinearLayout.LayoutParams(dp(66), dp(66));

        logoSize.rightMargin = dp(13);
        logo.setLayoutParams(logoSize);
        header.addView(logo);

        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);

        title = tv("LIGHT SENSI", 27, 0xFFFFF5F5, true);
        title.setLetterSpacing(0.05f);

        TextView sub = tv("KIRA  AURA  ENGINE", 10, 0xFFFF7B88, true);
        sub.setLetterSpacing(0.24f);

        textBox.addView(title);
        textBox.addView(sub);
        header.addView(textBox);

        addAnimated(header);

        ValueAnimator glow = ValueAnimator.ofFloat(6f, 28f);
        glow.setDuration(1300);
        glow.setRepeatCount(ValueAnimator.INFINITE);
        glow.setRepeatMode(ValueAnimator.REVERSE);

        glow.addUpdateListener(value ->
                title.setShadowLayer(
                        (Float) value.getAnimatedValue(),
                        0,
                        0,
                        RED
                )
        );

        glow.start();

        LinearLayout chips = row();
        chips.setPadding(0, dp(16), 0, 0);

        chips.addView(chip("ANDROID", Build.VERSION.RELEASE));
        chips.addView(chip("CORES",
                String.valueOf(Runtime.getRuntime().availableProcessors())));
        chips.addView(chip("DEVICE", Build.MODEL));

        addAnimated(chips);
    }

    void buildShizukuCard() {
        LinearLayout card = glassCard();

        LinearLayout statusRow = row();

        dot = new View(this);

        GradientDrawable dotBg = new GradientDrawable();
        dotBg.setShape(GradientDrawable.OVAL);
        dotBg.setColor(RED);

        dot.setBackground(dotBg);

        LinearLayout.LayoutParams dotSize =
                new LinearLayout.LayoutParams(dp(10), dp(10));

        dotSize.rightMargin = dp(10);
        dot.setLayoutParams(dotSize);

        statusRow.addView(dot);

        status = tv("SHIZUKU : CHECKING", 12, 0xFFFFB2BA, true);
        status.setLetterSpacing(0.08f);

        statusRow.addView(status);
        card.addView(statusRow);

        Button connect = button("CONNECT SHIZUKU", false, 48);

        connect.setOnClickListener(v -> {
            vibe(v);
            connectShizuku();
        });

        card.addView(connect);
        addAnimated(card);
    }

    void buildMaxButton() {
        Button all = button("ACTIVATE ALL  •  MAX AURA", true, 64);
        all.setTextSize(15);

        all.setOnClickListener(v -> {
            vibe(v);

            StringBuilder script = new StringBuilder();

            for (int i = 2; i < Cmd.S.length; i += 3) {
                script.append(Cmd.S[i]).append("\n");
            }

            runCommand("MAX AURA", script.toString(), (Button) v);
        });

        addAnimated(all);
    }

    void addSections(String[] list, int startNumber) {
        for (int i = 0; i < list.length; i += 3) {
            final String sectionTitle = list[i];
            final String sectionScript = list[i + 2];

            LinearLayout card = glassCard();
            LinearLayout top = row();

            top.addView(badge(startNumber + i / 3));

            LinearLayout tx = new LinearLayout(this);
            tx.setOrientation(LinearLayout.VERTICAL);

            TextView title = tv(sectionTitle, 15, 0xFFFFF4F4, true);
            TextView desc = tv(list[i + 1], 10, 0xFFB49DA1, false);

            tx.addView(title);
            tx.addView(desc);

            top.addView(tx);
            card.addView(top);

            boolean isRisky = sectionTitle.contains("EXTREME");
            boolean isInfo = sectionTitle.contains("INFO");
            boolean isReset = sectionTitle.contains("RESET");

            String buttonText = isInfo ? "CHECK" :
                    (isReset ? "RESET" : "APPLY");

            Button apply = button(
                    buttonText,
                    !isInfo && !isReset,
                    46
            );

            if (isRisky) {
                apply.setText("EXTREME");
            }

            apply.setOnClickListener(v -> {
                vibe(v);
                runCommand(sectionTitle, sectionScript, (Button) v);
            });

            card.addView(apply);
            addAnimated(card);
        }
    }

    void buildTerminal() {
        TextView label = tv("TERMINAL OUTPUT", 11, 0xFFFF7B88, true);
        label.setLetterSpacing(0.25f);
        label.setPadding(dp(6), dp(22), 0, dp(8));

        addAnimated(label);

        progress = new ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
        );

        progress.setIndeterminate(true);
        progress.setIndeterminateTintList(
                ColorStateList.valueOf(RED)
        );

        progress.setVisibility(View.INVISIBLE);
        progress.setLayoutParams(
                new LinearLayout.LayoutParams(-1, dp(3))
        );

        content.addView(progress);

        logBox = new ScrollView(this);
        logBox.setVerticalScrollBarEnabled(false);
        logBox.setBackground(glass(20,
                0x33000000,
                0x1A000000,
                0x44FFFFFF
        ));

        logBox.setPadding(dp(13), dp(13), dp(13), dp(13));

        logBox.setLayoutParams(
                new LinearLayout.LayoutParams(-1, dp(250))
        );

        log = tv("[ LIGHT SENSI ] Kira Aura ready.", 11,
                0xFFFFB0B8, false);

        log.setTypeface(Typeface.MONOSPACE);
        logBox.addView(log);

        content.addView(logBox);

        LinearLayout buttons = row();

        Button clear = button("CLEAR LOG", false, 44);
        clear.setTextSize(11);

        clear.setOnClickListener(v -> {
            vibe(v);
            log.setText("[ terminal cleared ]");
        });

        LinearLayout.LayoutParams half =
                new LinearLayout.LayoutParams(0, dp(44), 1f);

        half.topMargin = dp(10);
        half.leftMargin = dp(4);
        half.rightMargin = dp(4);

        clear.setLayoutParams(half);
        buttons.addView(clear);

        Button info = button("DEVICE INFO", false, 44);
        info.setTextSize(11);

        info.setOnClickListener(v -> {
            vibe(v);
            runCommand("DEVICE INFO", findInfoScript(), info);
        });

        info.setLayoutParams(
                new LinearLayout.LayoutParams(half)
        );

        buttons.addView(info);
        content.addView(buttons);

        TextView footer = tv(
                "LIGHT SENSI  •  KIRA GLASS EDITION  •  SHIZUKU",
                9,
                0xFF76545A,
                true
        );

        footer.setGravity(Gravity.CENTER);
        footer.setLetterSpacing(0.14f);
        footer.setPadding(0, dp(20), 0, 0);

        content.addView(footer);
    }

    String findInfoScript() {
        for (int i = 0; i < Cmd.X.length; i += 3) {
            if (Cmd.X[i].contains("INFO")) {
                return Cmd.X[i + 2];
            }
        }

        return "getprop ro.product.model";
    }

    void addAnimated(View view) {
        view.setAlpha(0f);
        animations.add(view);
        content.addView(view);
    }

    LinearLayout row() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutParams(
                new LinearLayout.LayoutParams(-1, -2)
        );
        return row;
    }

    LinearLayout glassCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);

        card.setPadding(dp(16), dp(15), dp(16), dp(15));

        card.setBackground(glass(
                25,
                0x33FFFFFF,
                0x12000000,
                0x45FFFFFF
        ));

        card.setElevation(dp(4));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, -2);

        p.topMargin = dp(13);
        card.setLayoutParams(p);

        return card;
    }

    GradientDrawable glass(int radius, int color1,
                           int color2, int border) {

        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{color1, color2}
        );

        bg.setCornerRadius(dp(radius));
        bg.setStroke(Math.max(1, dp(1)), border);

        return bg;
    }

    TextView tv(String text, int size,
                int color, boolean bold) {

        TextView t = new TextView(this);

        t.setText(text);
        t.setTextSize(size);
        t.setTextColor(color);

        if (bold) {
            t.setTypeface(Typeface.DEFAULT_BOLD);
        }

        return t;
    }

    LinearLayout chip(String key, String value) {
        LinearLayout card = new LinearLayout(this);

        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);

        card.setPadding(dp(4), dp(9), dp(4), dp(9));

        card.setBackground(glass(
                18,
                0x2AFFFFFF,
                0x11000000,
                0x36FFFFFF
        ));

        TextView a = tv(key, 8, 0xFFC0A6AA, true);
        a.setGravity(Gravity.CENTER);
        a.setLetterSpacing(0.13f);

        TextView b = tv(value, 11, 0xFFFF9AA5, true);
        b.setGravity(Gravity.CENTER);
        b.setSingleLine(true);
        b.setEllipsize(TextUtils.TruncateAt.END);

        card.addView(a);
        card.addView(b);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(0, -2, 1f);

        p.leftMargin = dp(4);
        p.rightMargin = dp(4);

        card.setLayoutParams(p);

        return card;
    }

    TextView badge(int number) {
        TextView n = tv(String.valueOf(number), 12,
                Color.WHITE, true);

        n.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{RED, 0xFF870015}
        );

        bg.setShape(GradientDrawable.OVAL);
        bg.setStroke(dp(1), 0x55FFFFFF);

        n.setBackground(bg);

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(dp(30), dp(30));

        p.rightMargin = dp(12);
        n.setLayoutParams(p);

        return n;
    }

    Button button(String text, boolean red, int height) {
        Button button = new Button(this);

        button.setText(text);
        button.setTextSize(13);
        button.setTextColor(Color.WHITE);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setLetterSpacing(0.07f);
        button.setAllCaps(true);
        button.setStateListAnimator(null);

        GradientDrawable face;

        if (red) {
            face = glass(18,
                    0xD9FF1E3C,
                    0xAA7A0015,
                    0x66FFFFFF
            );
        } else {
            face = glass(18,
                    0x38FFFFFF,
                    0x17000000,
                    0x45FFFFFF
            );
        }

        button.setBackground(new RippleDrawable(
                ColorStateList.valueOf(0x55FFFFFF),
                face,
                null
        ));

        LinearLayout.LayoutParams p =
                new LinearLayout.LayoutParams(-1, dp(height));

        p.topMargin = dp(11);
        button.setLayoutParams(p);

        return button;
    }

    void vibe(View view) {
        view.performHapticFeedback(
                HapticFeedbackConstants.VIRTUAL_KEY
        );
    }

    boolean isReady() {
        try {
            return Shizuku.pingBinder()
                    && !Shizuku.isPreV11()
                    && Shizuku.checkSelfPermission()
                    == PackageManager.PERMISSION_GRANTED;

        } catch (Throwable error) {
            return false;
        }
    }

    void updateStatus() {
        try {
            GradientDrawable dotColor =
                    (GradientDrawable) dot.getBackground();

            if (!Shizuku.pingBinder()) {
                status.setText("SHIZUKU : NOT RUNNING");
                status.setTextColor(0xFFFF9AA5);
                dotColor.setColor(RED);

            } else if (isReady()) {
                status.setText("SHIZUKU : CONNECTED");
                status.setTextColor(GREEN);
                dotColor.setColor(GREEN);

            } else {
                status.setText("SHIZUKU : PERMISSION NEEDED");
                status.setTextColor(ORANGE);
                dotColor.setColor(ORANGE);
            }

        } catch (Throwable error) {
            status.setText("SHIZUKU : ERROR");
        }
    }

    void connectShizuku() {
        try {
            if (!Shizuku.pingBinder()) {
                addLog("! Shizuku app open kore START koro.");
                return;
            }

            if (Shizuku.checkSelfPermission()
                    != PackageManager.PERMISSION_GRANTED) {

                Shizuku.requestPermission(7);

            } else {
                addLog("+ Shizuku already connected");
                updateStatus();
            }

        } catch (Throwable error) {
            addLog("! " + error);
        }
    }

    void addLog(String text) {
        h.post(() -> {
            log.append("\n" + text);
            logBox.post(() ->
                    logBox.fullScroll(ScrollView.FOCUS_DOWN)
            );
        });
    }

    void runCommand(String section, String script,
                    Button button) {

        if (!isReady()) {
            addLog("! Age SHIZUKU CONNECT koro");
            Toast.makeText(
                    this,
                    "Shizuku connect koro",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        if (busy) {
            Toast.makeText(
                    this,
                    "Wait koro, command cholche...",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        busy = true;

        progress.setVisibility(View.VISIBLE);
        button.setText("RUNNING...");
        addLog("\n>> " + section);

        new Thread(() -> {
            boolean success = true;

            try {
                Method processMethod =
                        Shizuku.class.getDeclaredMethod(
                                "newProcess",
                                String[].class,
                                String[].class,
                                String.class
                        );

                processMethod.setAccessible(true);

                Process process = (Process) processMethod.invoke(
                        null,
                        new String[]{
                                "sh",
                                "-c",
             
