package com.example.myappnew.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myappnew.R;

public class LoginActivity extends AppCompatActivity {
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
    }

    private void loginWithChannel(String channel) {
        // TODO: 集成各渠道SDK
        Toast.makeText(this, channel + "登录功能开发中...", Toast.LENGTH_SHORT).show();
        // 登录成功后跳转主界面
        // startActivity(new Intent(this, MainActivity.class));
        // finish();
    }
}
