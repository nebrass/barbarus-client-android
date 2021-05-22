package com.targa.labs.dev.barbarus;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    private CodeScannerView scannerView;
    private Button btnSubmit;
    private FloatingActionButton btnResume;
    private FloatingActionButton btnLogout;

    private OkHttpClient client;

    private String barbarusUrl;
    private EditText urlEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private TextView textViewId;

    private ImageView owaspLogoImageView;

    private AccessTokenDto accessTokenDto;

    private Long startTime;
    private Timer timer;
    private Boolean timerRun;

    private String connected;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        urlEditText = findViewById(R.id.url);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        textViewId = findViewById(R.id.view_id);
        owaspLogoImageView = findViewById(R.id.logo_owasp);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 50);
        }

        connected = getString(R.string.connected);

        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        scannerView = findViewById(R.id.scanner_view);

        mCodeScanner = new CodeScanner(this, scannerView);
        mCodeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            if (isJson(result.getText())) {
                Gson gson = new Gson();
                BarbarusLoginDto barbarusLoginDto = gson.fromJson(result.getText(), BarbarusLoginDto.class);

                Toast.makeText(MainActivity.this, R.string.done, Toast.LENGTH_LONG).show();

                textViewId.setText(barbarusLoginDto.getViewId());
                urlEditText.setText(barbarusLoginDto.getLoginUrl());
            }
        }));
        scannerView.setOnClickListener(view -> mCodeScanner.startPreview());

        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE && !isEmpty(urlEditText) && !isEmpty(usernameEditText)) {
                btnSubmit.performClick();
                return true;
            }
            return false;
        });

        btnSubmit = findViewById(R.id.btn_submit);

        client = new OkHttpClient();
        btnSubmit.setOnClickListener(v -> {
            barbarusUrl = urlEditText.getText().toString();
            doLogin();
        });

        btnResume = (FloatingActionButton) findViewById(R.id.btn_resume);
        btnResume.setOnClickListener(view -> resume());

        btnLogout = (FloatingActionButton) findViewById(R.id.btn_logout);
        btnLogout.setOnClickListener(view -> {
            doLogout();
            resume();
        });
    }

    private void doLogout() {
        new Thread(() -> {
            String url = barbarusUrl.replace("login", "logout");

            Gson gson = new Gson();
            String json = gson.toJson(accessTokenDto);

            MediaType jsonType = MediaType.get("application/json");

            RequestBody body = RequestBody.create(json, jsonType);

            runOnUiThread(() -> {
                Request request =
                        new Request.Builder()
                                .url(url)
                                .post(body)
                                .build();

                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        textViewId.setText(R.string.logout_success);
                        textViewId.setTextColor(0xFF0F4C81);
                    } else {
                        textViewId.setText(response.message());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }

    private void doLogin() {
        new Thread(() -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();
            String viewId = textViewId.getText().toString();

            BarbarusLoginDto barbarusLoginDto = new BarbarusLoginDto(
                    username,
                    password,
                    viewId,
                    barbarusUrl
            );

            Gson gson = new Gson();
            String json = gson.toJson(barbarusLoginDto);

            MediaType jsonType = MediaType.get("application/json");

            RequestBody body = RequestBody.create(json, jsonType);

            runOnUiThread(() -> {
                Request request =
                        new Request.Builder().url(barbarusUrl)
                                .post(body)
                                .build();

                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseBodyJson = Objects.requireNonNull(response.body()).string();

                        accessTokenDto = gson.fromJson(responseBodyJson, AccessTokenDto.class);

                        textViewId.setTextColor(0xFF0F4C81);

                        btnSubmit.setVisibility(Button.GONE);
                        scannerView.setVisibility(View.INVISIBLE);
                        urlEditText.setEnabled(false);
                        usernameEditText.setEnabled(false);
                        passwordEditText.setEnabled(false);

                        onPause();

                        btnResume.setVisibility(View.VISIBLE);
                        btnLogout.setVisibility(View.VISIBLE);
                        owaspLogoImageView.setVisibility(View.VISIBLE);

                        playTimer();

                    } else {
                        textViewId.setText(response.message());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }).start();
    }

    private void resume() {
        textViewId.setText(R.string.scan);
        textViewId.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.black));
        textViewId.setTextAlignment(View.TEXT_ALIGNMENT_GRAVITY);

        timerRun = false;

        onResume();

        btnSubmit.setVisibility(Button.VISIBLE);
        scannerView.setVisibility(View.VISIBLE);
        urlEditText.setEnabled(true);
        urlEditText.setText("");
        usernameEditText.setEnabled(true);
        usernameEditText.setText("");
        passwordEditText.setEnabled(true);
        passwordEditText.setText("");

        btnResume.setVisibility(View.GONE);
        btnLogout.setVisibility(View.GONE);
        owaspLogoImageView.setVisibility(View.INVISIBLE);
    }

    private void playTimer() {
        timerRun = true;
        timer = new Timer();
        startTime = System.currentTimeMillis();
        timer.schedule(new UpdateTimeTask(), 100, 200);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.about) {
            Intent favIntent = new Intent(this, About.class);
            startActivity(favIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isEmpty(EditText editText) {
        return editText.getText().toString().trim().length() == 0;
    }

    public static boolean isJson(String Json) {
        try {
            new JSONObject(Json);
        } catch (JSONException ex) {
            try {
                new JSONArray(Json);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }

    class UpdateTimeTask extends TimerTask {
        @Override
        public void run() {
            if (timerRun) {
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                textViewId.setText(String.format("%s: %d:%02d", connected, minutes, seconds));
            } else {
                timer.cancel();
                timer.purge();
            }
        }
    }
}