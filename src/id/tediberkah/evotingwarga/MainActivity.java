package id.tediberkah.evotingwarga;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.webkit.WebChromeClient;

public class MainActivity extends Activity {
    private LinearLayout root;
    private EditText serverInput;
    private WebView webView;
    private SharedPreferences prefs;

    private final int bg = Color.rgb(7, 17, 35);
    private final int panel = Color.rgb(17, 33, 57);
    private final int blue = Color.rgb(0, 150, 220);
    private final int cyan = Color.rgb(62, 215, 235);
    private final int white = Color.WHITE;
    private final int muted = Color.rgb(185, 202, 222);

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);

        prefs = getSharedPreferences("tedi_voting", MODE_PRIVATE);
        String base = prefs.getString(
            "server", "https://tediju.pythonanywhere.com"
        );
        showWebPage(base + "/pemilih/masuk");
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private TextView text(String value, int size, int color) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER);
        t.setPadding(8, 8, 8, 8);
        return t;
    }

    private TextView heading(String value, int size) {
        TextView t = text(value, size, white);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private LinearLayout card() {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(18, 16, 18, 16);
        c.setBackground(shape(panel, 24));
        return c;
    }

    private void gap(LinearLayout target, int height) {
        View v = new View(this);
        target.addView(v, new LinearLayout.LayoutParams(
            1, height
        ));
    }

    private void showStartPage(String saved) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(bg);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(22, 22, 22, 28);
        scroll.addView(root);

        TextView brand = heading("TEDI BERKAH", 15);
        brand.setTextColor(cyan);
        brand.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        root.addView(brand);

        gap(root, 24);

        TextView emblem = text("TB", 34, white);
        emblem.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        emblem.setBackground(shape(blue, 28));
        LinearLayout.LayoutParams emblemParams =
            new LinearLayout.LayoutParams(100, 100);
        emblemParams.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(emblem, emblemParams);

        gap(root, 18);

        root.addView(heading("E-VOTING WARGA", 27));
        root.addView(text(
            "SISTEM PEMILIHAN RT / RW",
            13, cyan
        ));

        gap(root, 26);

        LinearLayout info = card();
        info.addView(heading("SELAMAT DATANG", 19));
        info.addView(text(
            "Gunakan aplikasi ini untuk membuka halaman "
            + "pemilihan warga yang dikelola oleh server admin.",
            14, muted
        ));
        root.addView(info);

        gap(root, 18);

        LinearLayout connection = card();
        connection.addView(heading("KONEKSI SERVER", 17));
        gap(connection, 6);

        TextView label = text(
            "Alamat server admin", 13, muted
        );
        label.setGravity(Gravity.LEFT);
        connection.addView(label);

        serverInput = new EditText(this);
        serverInput.setSingleLine(true);
        serverInput.setText(saved);
        serverInput.setTextColor(white);
        serverInput.setHintTextColor(muted);
        serverInput.setHint("http://alamat-ip:5000");
        serverInput.setTextSize(15);
        serverInput.setPadding(14, 12, 14, 12);
        serverInput.setInputType(
            InputType.TYPE_CLASS_TEXT
            | InputType.TYPE_TEXT_VARIATION_URI
        );
        serverInput.setImeOptions(EditorInfo.IME_ACTION_GO);
        serverInput.setBackground(shape(bg, 14));
        connection.addView(serverInput);

        gap(connection, 14);

        Button open = new Button(this);
        open.setText("MASUK KE HALAMAN WARGA  →");
        open.setTextColor(white);
        open.setTextSize(14);
        open.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        open.setAllCaps(false);
        open.setBackgroundTintList(
            android.content.res.ColorStateList.valueOf(blue)
        );
        connection.addView(open);
        root.addView(connection);

        open.setOnClickListener(v -> connectServer());
        serverInput.setOnEditorActionListener((v, action, event) -> {
            if (action == EditorInfo.IME_ACTION_GO) {
                connectServer();
                return true;
            }
            return false;
        });

        gap(root, 18);

        LinearLayout note = card();
        note.addView(heading("PETUNJUK", 15));
        note.addView(text(
            "1. Pastikan server admin aktif.\n"
            + "2. Hubungkan HP ke jaringan yang sama.\n"
            + "3. Masukkan alamat server yang benar.",
            13, muted
        ));
        root.addView(note);

        gap(root, 20);
        root.addView(text(
            "TEDI BERKAH • E-VOTING WARGA",
            11, muted
        ));

        setContentView(scroll);
    }

    private void connectServer() {
        String base = serverInput.getText().toString().trim();

        if (base.isEmpty()) {
            Toast.makeText(this,
                "Masukkan alamat server admin",
                Toast.LENGTH_LONG).show();
            return;
        }

        if (!base.startsWith("http://")
                && !base.startsWith("https://")) {
            base = "http://" + base;
        }

        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        if (!base.matches(
                "(?i)^https?://[^/]+(?::[0-9]{1,5})?$")) {
            Toast.makeText(this,
                "Alamat server tidak valid",
                Toast.LENGTH_LONG).show();
            return;
        }

        prefs.edit().putString("server", base).apply();
        showWebPage(base + "/pemilih/masuk");
    }

    private void showWebPage(String url) {
        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
        webView.setBackgroundColor(bg);
        webView.loadUrl(url);
        setContentView(webView);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
