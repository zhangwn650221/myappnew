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
public class ChatFragment extends Fragment {
    private EditText editChatMessage;
    private Button buttonSendMessage;
    private TextView textChatMessages;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);
        editChatMessage = root.findViewById(R.id.edit_chat_message);
        buttonSendMessage = root.findViewById(R.id.button_send_message);
        textChatMessages = root.findViewById(R.id.text_chat_messages);
        textChatMessages.setMovementMethod(new ScrollingMovementMethod());
        buttonSendMessage.setEnabled(true);

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        apiKey = prefs.getString(KEY_API, "");
        int geminiVersionIdx = prefs.getInt(KEY_GEMINI_VERSION, 0);
        geminiVersion = GEMINI_VERSION_ARRAY[Math.max(0, Math.min(geminiVersionIdx, GEMINI_VERSION_ARRAY.length-1))];
        geminiProvider = new GeminiProvider();
        geminiProvider.setModelVersion(geminiVersion);

        buttonSendMessage.setOnClickListener(v -> {
            String message = editChatMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                appendMessageToView(message, true);
                sendGeminiRequest(message);
            } else {
                showToast("Message cannot be empty.");
            }
        });
        return root;
    }

    private void sendGeminiRequest(String userInput) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + geminiVersion + ":generateContent?key=" + apiKey;
        String jsonBody = geminiProvider.buildRequestMessage(userInput);
        RequestBody body = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (getActivity() != null) getActivity().runOnUiThread(() -> showToast("请求失败: " + e.getMessage()));
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resp = response.body() != null ? response.body().string() : "";
                if (getActivity() != null) getActivity().runOnUiThread(() -> {
                    String modelReply = extractGeminiReply(resp);
                    appendMessageToView(modelReply, false);
                    editChatMessage.setText("");
                });
            }
        });
    }

    private void appendMessageToView(String message, boolean isUser) {
        SpannableString span;
        if (isUser) {
            span = new SpannableString("我: " + message + "\n");
            span.setSpan(new ForegroundColorSpan(0xFF1976D2), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {
            span = new SpannableString("Gemini: " + message + "\n");
            span.setSpan(new ForegroundColorSpan(0xFF388E3C), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, 7, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        textChatMessages.append(span);
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
}
