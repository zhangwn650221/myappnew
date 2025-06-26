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

import com.example.myappnew.R;

public class LoginActivity extends AppCompatActivity {
    private Spinner modelTypeSpinner;
    private EditText apiKeyEditText;
    private static final String PREFS_NAME = "user_settings";
    private static final String KEY_MODEL_TYPE = "model_type";
    private static final String KEY_API = "user_api_key";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnWeChat = findViewById(R.id.buttonWeChatLogin);
        Button btnApple = findViewById(R.id.buttonAppleLogin);
        Button btnGoogle = findViewById(R.id.buttonGoogleLogin);
        Button btnPhone = findViewById(R.id.buttonPhoneLogin);

        btnWeChat.setOnClickListener(v -> loginWithChannel("微信"));
        btnApple.setOnClickListener(v -> loginWithChannel("Apple ID"));
        btnGoogle.setOnClickListener(v -> loginWithChannel("Google"));
        btnPhone.setOnClickListener(v -> loginWithChannel("手机号"));

        modelTypeSpinner = findViewById(R.id.spinnerModelType);
        apiKeyEditText = findViewById(R.id.editTextApiKey);
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        // 加载已保存的模型类型和API Key
        int savedModelType = prefs.getInt(KEY_MODEL_TYPE, 0);
        String savedApiKey = prefs.getString(KEY_API, "");
        modelTypeSpinner.setSelection(savedModelType);
        apiKeyEditText.setText(savedApiKey);
        modelTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putInt(KEY_MODEL_TYPE, position).apply();
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
    }

    private void loginWithChannel(String channel) {
        // TODO: 集成各渠道SDK
        Toast.makeText(this, channel + "登录功能开发中...", Toast.LENGTH_SHORT).show();
        // 登录成功后跳转主界面
        // startActivity(new Intent(this, MainActivity.class));
        // finish();
    }
}
