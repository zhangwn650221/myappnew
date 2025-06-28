package com.example.myappnew.ui.chat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myappnew.R;
import com.example.myappnew.modelprovider.GeminiProvider;

import org.json.JSONObject;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Fragment for displaying the chat interface and handling communication with the Gemini API.
 *
 * ---
 * <h4>Testing Strategy (Integration/UI Tests - Espresso):</h4>
 * <ul>
 *     <li>Test UI element interactions:
 *         <ul>
 *             <li>Typing text into the message EditText.</li>
 *             <li>Clicking the send button.</li>
 *         </ul>
 *     </li>
 *     <li>Verify message display:
 *         <ul>
 *             <li>After sending a message, verify it (or an echo from a test server/mocked callback) appears in the messages TextView.</li>
 *         </ul>
 *     </li>
 *     <li>Mock Gemini API responses to test various scenarios (successful response, error response, etc.) and verify UI updates accordingly.</li>
 *     <li>Test fragment lifecycle integration: API request in <code>onViewCreated</code>, cleanup in <code>onDestroyView</code>.</li>
 *     <li>Consider using Espresso's IdlingResource for API asynchronous operations if not using mock responses directly.</li>
 * </ul>
 * ---
 */
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ChatFragment extends Fragment {
    private EditText editChatMessage;
    private Button buttonSendMessage;
    private TextView textChatOutput;
    private EditText editChatPrompt;
    private static final String KEY_CHAT_PROMPT = "chat_prompt";
    private GeminiProvider geminiProvider;
    private String apiKey;
    private String geminiVersion;
    private OkHttpClient httpClient = new OkHttpClient();
    private static final String PREFS_NAME = "user_settings";
    private static final String KEY_API = "user_api_key";
    private static final String KEY_GEMINI_VERSION = "gemini_version";
    private static final String[] GEMINI_VERSION_ARRAY = {
        "gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.0-pro", "gemini-2.5-pro"
    };
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private List<String> chatHistory = new LinkedList<>();
    private static final int HISTORY_ANALYZE_WINDOW = 5;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);
        editChatMessage = root.findViewById(R.id.edit_chat_message);
        buttonSendMessage = root.findViewById(R.id.button_send_message);
        textChatOutput = root.findViewById(R.id.text_chat_output);
        editChatPrompt = root.findViewById(R.id.edit_chat_prompt);
        // 读取已保存的 prompt 或用默认
        String defaultPrompt = "你是专业AI助手，善于理解和帮助用户。";
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedPrompt = prefs.getString(KEY_CHAT_PROMPT, defaultPrompt);
        editChatPrompt.setText(savedPrompt);
        editChatPrompt.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String prompt = editChatPrompt.getText().toString().trim();
                prefs.edit().putString(KEY_CHAT_PROMPT, prompt).apply();
            }
        });

        // 不要重复声明 prefs，后续直接复用
        apiKey = prefs.getString(KEY_API, "");
        int geminiVersionIdx = prefs.getInt(KEY_GEMINI_VERSION, 0);
        geminiVersion = GEMINI_VERSION_ARRAY[Math.max(0, Math.min(geminiVersionIdx, GEMINI_VERSION_ARRAY.length-1))];
        geminiProvider = new GeminiProvider();
        geminiProvider.setModelVersion(geminiVersion);

        buttonSendMessage.setOnClickListener(v -> {
            String message = editChatMessage.getText().toString().trim();
            String prompt = editChatPrompt.getText().toString().trim();
            if (!message.isEmpty()) {
                // 只显示用户输入在输入框，不再追加到输出区
                sendGeminiRequest(message, prompt);
            } else {
                showToast("Message cannot be empty.");
            }
        });
        return root;
    }

    private void sendGeminiRequest(String userInput, String systemPrompt) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiVersion + ":generateContent?key=" + apiKey;
        String jsonBody = geminiProvider.buildRequestMessage(userInput, systemPrompt);
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> textChatOutput.setText("网络请求失败: " + e.getMessage()));
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body() != null ? response.body().string() : "";
                android.util.Log.e("GeminiAPI", "Response code: " + response.code() + ", body: " + resp);
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    if (!response.isSuccessful()) {
                        textChatOutput.setText("API错误: " + response.code() + "\n" + resp);
                    } else {
                        String modelReply = extractGeminiReply(resp);
                        textChatOutput.setText(modelReply);
                    }
                    editChatMessage.setText("");
                });
            }
        });
    }

    private void appendMessageToView(String message, boolean isUser) {
        // 不再使用历史消息区，仅保留输出区
        if (!isUser && textChatOutput != null) {
            textChatOutput.setText(message);
        }
    }
    private void appendMessageToView(String message) {
        appendMessageToView(message, false);
    }
    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
    private String extractGeminiReply(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            if (obj.has("candidates")) {
                org.json.JSONArray candidates = obj.getJSONArray("candidates");
                if (candidates.length() > 0) {
                    JSONObject first = candidates.getJSONObject(0);
                    if (first.has("content")) {
                        JSONObject content = first.getJSONObject("content");
                        if (content.has("parts")) {
                            org.json.JSONArray parts = content.getJSONArray("parts");
                            if (parts.length() > 0) {
                                JSONObject part = parts.getJSONObject(0);
                                if (part.has("text")) {
                                    return part.getString("text");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            return json;
        }
        return json;
    }

    private void analyzeHistoryAndUpdatePrompt() {
        // 取最近N轮对话
        StringBuilder history = new StringBuilder();
        int start = Math.max(0, chatHistory.size() - HISTORY_ANALYZE_WINDOW);
        for (int i = start; i < chatHistory.size(); i++) {
            history.append(chatHistory.get(i)).append("\n");
        }
        String analysisPrompt = "请根据以下对话内容，判断用户的心理状态，并生成一句适合AI陪护的系统提示词（如‘你要温柔鼓励用户’或‘你要多倾听和安慰’），只返回这句提示词：\n" + history;
        // 直接用 Gemini API 分析
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiVersion + ":generateContent?key=" + apiKey;
        String jsonBody = geminiProvider.buildRequestMessage(analysisPrompt, "你是心理健康分析师，请只返回一句系统提示词");
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { /* 可忽略分析失败 */ }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body() != null ? response.body().string() : "";
                String newPrompt = extractGeminiReply(resp);
                if (getActivity() != null && newPrompt != null && !newPrompt.isEmpty()) {
                    getActivity().runOnUiThread(() -> editChatPrompt.setText(newPrompt));
                }
            }
        });
    }
}
