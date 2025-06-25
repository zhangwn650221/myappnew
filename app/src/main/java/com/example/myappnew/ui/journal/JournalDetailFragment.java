package com.example.myappnew.ui.journal;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myappnew.R;
import com.example.myappnew.data.JournalEntry;
import com.example.myappnew.services.llm.LlmRequest;
import com.example.myappnew.services.llm.LlmResponse;
import com.example.myappnew.services.llm.LlmService;
import com.example.myappnew.services.llm.LlmServiceProvider;

public class JournalDetailFragment extends Fragment {
    private TextView textContent;
    private TextView textTimestamp;
    private TextView textMultimodal;
    private ImageView imageView;
    private Button buttonAiAnalyze;

    private JournalEntry entry;

    private String lastPrompt = null;

    private LlmServiceProvider llmServiceProvider;
    private LlmService llmService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_journal_detail, container, false);
        textContent = view.findViewById(R.id.text_journal_detail_content);
        textTimestamp = view.findViewById(R.id.text_journal_detail_timestamp);
        textMultimodal = view.findViewById(R.id.text_journal_detail_multimodal);
        imageView = view.findViewById(R.id.image_journal_detail);
        buttonAiAnalyze = view.findViewById(R.id.button_ai_analyze);

        // TODO: 获取并展示 JournalEntry 数据（可通过 Bundle/Args 传递）
        // entry = ...
        // textContent.setText(entry.getContent());
        // textTimestamp.setText(...);
        // textMultimodal.setText(...);
        // if (entry.getImageUri() != null) { imageView.setImageURI(Uri.parse(entry.getImageUri())); }

        buttonAiAnalyze.setOnClickListener(v -> sendPromptToAI(entry));
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        llmServiceProvider = new LlmServiceProvider();
        llmService = llmServiceProvider.getService();
    }

    private String generatePrompt(JournalEntry entry) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("请作为一名专业的心理陪伴AI，根据以下用户日记内容和多模态信息，结合用户画像，给予个性化的情绪分析和疗愈建议。\n");
        prompt.append("【日记内容】：").append(entry.getContent() == null ? "无" : entry.getContent()).append("\n");
        if (entry.getImageUri() != null && !entry.getImageUri().isEmpty()) {
            prompt.append("【照片】：用户上传了照片，请结合图片内容分析情绪。\n");
        }
        if (entry.getAudioUri() != null && !entry.getAudioUri().isEmpty()) {
            prompt.append("【语音】：用户上传了语音，请结合语音内容分析情绪。\n");
        }
        if (entry.getVideoUri() != null && !entry.getVideoUri().isEmpty()) {
            prompt.append("【视频】：用户上传了视频，请结合视频内容分析情绪。\n");
        }
        if (entry.getUserProfileJson() != null && !entry.getUserProfileJson().isEmpty()) {
            prompt.append("【用户画像】：").append(entry.getUserProfileJson()).append("\n");
        }
        prompt.append("请用温暖、鼓励的语言，输出分析和建议。");
        return prompt.toString();
    }

    private void sendPromptToAI(JournalEntry entry) {
        String prompt = generatePrompt(entry);
        lastPrompt = prompt;
        LlmRequest request = new LlmRequest(prompt);
        llmService.generateText(request).enqueue(new retrofit2.Callback<LlmResponse>() {
            @Override
            public void onResponse(retrofit2.Call<LlmResponse> call, retrofit2.Response<LlmResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getGeneratedText() != null) {
                    // 展示AI分析结果
                    Toast.makeText(getContext(), "AI分析结果：" + response.body().getGeneratedText(), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getContext(), "AI分析失败: " + (response.body() != null ? response.body().getError() : "未知错误"), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<LlmResponse> call, Throwable t) {
                Toast.makeText(getContext(), "AI服务调用失败: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
