package com.example.myappnew.ui.mainmenu;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.myappnew.R;

public class MainMenuFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);
        // 文字聊天
        View textChat = view.findViewById(R.id.menu_text_chat);
        textChat.setOnClickListener(v -> navigateTo(R.id.action_mainMenu_to_chatFragment));
        // 语音聊天
        View voiceChat = view.findViewById(R.id.menu_voice_chat);
        voiceChat.setOnClickListener(v -> navigateTo(R.id.action_mainMenu_to_voiceChatFragment));
        // 视频聊天
        View videoChat = view.findViewById(R.id.menu_video_chat);
        videoChat.setOnClickListener(v -> navigateTo(R.id.action_mainMenu_to_videoChatFragment));
        return view;
    }
    private void navigateTo(int actionId) {
        if (getView() != null && getActivity() != null) {
            androidx.navigation.Navigation.findNavController(getView()).navigate(actionId);
        }
    }
}
