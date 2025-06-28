package com.example.myappnew.ui.login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myappnew.MainActivity;
import com.example.myappnew.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {
    private Spinner modelTypeSpinner;
    private EditText apiKeyEditText;
    private Spinner geminiVersionSpinner;
    private static final String PREFS_NAME = "user_settings";
    private static final String KEY_MODEL_TYPE = "model_type";
    private static final String KEY_API = "user_api_key";
    private static final String KEY_GEMINI_VERSION = "gemini_version";
    private static final int RC_SIGN_IN = 9001;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnWeChat = findViewById(R.id.buttonWeChatLogin);
        Button btnApple = findViewById(R.id.buttonAppleLogin);
        Button btnGoogle = findViewById(R.id.buttonGoogleLogin);
        Button btnPhone = findViewById(R.id.buttonPhoneLogin);

        btnWeChat.setOnClickListener(v -> {
            if (validateModelSelectionAndApiKey()) loginWithChannel("微信");
        });
        btnApple.setOnClickListener(v -> {
            if (validateModelSelectionAndApiKey()) loginWithChannel("Apple ID");
        });
        btnGoogle.setOnClickListener(v -> {
            // 直接发起 Google 登录
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
        btnPhone.setOnClickListener(v -> {
            if (validateModelSelectionAndApiKey()) loginWithChannel("手机号");
        });

        modelTypeSpinner = findViewById(R.id.spinnerModelType);
        apiKeyEditText = findViewById(R.id.editTextApiKey);
        geminiVersionSpinner = findViewById(R.id.spinnerGeminiVersion);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // 加载已保存的模型类型和API Key
        int savedModelType = prefs.getInt(KEY_MODEL_TYPE, 0);
        String savedApiKey = prefs.getString(KEY_API, "");
        modelTypeSpinner.setSelection(savedModelType);
        apiKeyEditText.setText(savedApiKey);
        // 加载已保存的 Gemini 版本
        int savedGeminiVersion = prefs.getInt(KEY_GEMINI_VERSION, 0);
        geminiVersionSpinner.setSelection(savedGeminiVersion);
        // Gemini 版本选择监听
        geminiVersionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putInt(KEY_GEMINI_VERSION, position).apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        // 联动显示 Gemini 版本下拉框
        modelTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putInt(KEY_MODEL_TYPE, position).apply();
                String[] modelTypes = getResources().getStringArray(R.array.model_type_array);
                if (modelTypes[position].equals("Gemini")) {
                    geminiVersionSpinner.setVisibility(View.VISIBLE);
                } else {
                    geminiVersionSpinner.setVisibility(View.GONE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        apiKeyEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String apiKey = apiKeyEditText.getText().toString().trim();
                prefs.edit().putString(KEY_API, apiKey).apply();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private boolean validateModelSelectionAndApiKey() {
        int modelType = modelTypeSpinner.getSelectedItemPosition();
        String apiKey = apiKeyEditText.getText().toString().trim();
        if (modelType < 0) {
            Toast.makeText(this, "请选择大模型类型", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (apiKey.isEmpty()) {
            Toast.makeText(this, "请输入API Key", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void loginWithChannel(String channel) {
        // TODO: 集成各渠道SDK
        // 登录成功后跳转主界面
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String email = account.getEmail();
            String name = account.getDisplayName();
            Toast.makeText(this, "Google 登录成功：" + email, Toast.LENGTH_SHORT).show();
            // 可在此处保存邮箱到 SharedPreferences 或跳转主界面
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } catch (ApiException e) {
            Toast.makeText(this, "Google 登录失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
