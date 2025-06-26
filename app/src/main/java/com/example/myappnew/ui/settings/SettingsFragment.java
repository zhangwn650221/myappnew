package com.example.myappnew.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myappnew.R;

public class SettingsFragment extends Fragment {
    private EditText apiKeyEditText;
    private Button saveButton;
    private Button exportDataButton;
    private Button logoutButton;
    private Spinner modelTypeSpinner;
    private static final String PREFS_NAME = "user_settings";
    private static final String KEY_API = "user_api_key";
    private static final String KEY_MODEL_TYPE = "model_type";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        apiKeyEditText = view.findViewById(R.id.editTextApiKey);
        saveButton = view.findViewById(R.id.buttonSaveApiKey);
        exportDataButton = view.findViewById(R.id.buttonExportData);
        logoutButton = view.findViewById(R.id.buttonLogout);
        modelTypeSpinner = view.findViewById(R.id.spinnerModelType);

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // 加载已保存的API Key
        String savedKey = prefs.getString(KEY_API, "");
        apiKeyEditText.setText(savedKey);

        // 加载已保存的大模型类型
        int savedModelType = prefs.getInt(KEY_MODEL_TYPE, 0);
        modelTypeSpinner.setSelection(savedModelType);
        modelTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prefs.edit().putInt(KEY_MODEL_TYPE, position).apply();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        saveButton.setOnClickListener(v -> {
            String apiKey = apiKeyEditText.getText().toString().trim();
            if (TextUtils.isEmpty(apiKey)) {
                Toast.makeText(getContext(), "API Key不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            prefs.edit().putString(KEY_API, apiKey).apply();
            Toast.makeText(getContext(), "API Key已保存", Toast.LENGTH_SHORT).show();
        });

        exportDataButton.setOnClickListener(v -> {
            // TODO: 实现实际数据导出逻辑
            Toast.makeText(getContext(), "数据导出功能开发中...", Toast.LENGTH_SHORT).show();
        });

        logoutButton.setOnClickListener(v -> {
            new android.app.AlertDialog.Builder(getContext())
                .setTitle("账户注销")
                .setMessage("确定要注销账户并清除本地数据吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    prefs.edit().clear().apply();
                    Toast.makeText(getContext(), "账户已注销，本地数据已清除", Toast.LENGTH_SHORT).show();
                    // 跳转到登录页
                    requireActivity().startActivity(
                        new android.content.Intent(requireContext(), com.example.myappnew.ui.login.LoginActivity.class)
                    );
                    requireActivity().finish();
                })
                .setNegativeButton("取消", null)
                .show();
        });
        return view;
    }

    // 可添加导出数据、注销账号等功能按钮
}
