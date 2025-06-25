package com.example.myappnew.ui.chat;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
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
import com.example.myappnew.services.websocket.ChatWebSocketClient;
import com.example.myappnew.services.websocket.WebSocketListenerCallback;

/**
 * Fragment for displaying the chat interface and handling WebSocket communication.
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
 *     <li>Mock <code>ChatWebSocketClient</code> or its callback to simulate connection states (open, message received, closed, failure) and verify UI updates accordingly (e.g., send button enabled/disabled, status messages shown).</li>
 *     <li>Test fragment lifecycle integration: WebSocket connection in <code>onViewCreated</code>, disconnection in <code>onDestroyView</code>.</li>
 *     <li>Consider using Espresso's IdlingResource for WebSocket asynchronous operations if not using mock callbacks directly.</li>
 * </ul>
 * ---
 */
public class ChatFragment extends Fragment implements WebSocketListenerCallback {

    private EditText editChatMessage;
    private Button buttonSendMessage;
    private TextView textChatMessages;

    private ChatWebSocketClient webSocketClient;
    // Using a public test WebSocket server
    private static final String WEB_SOCKET_URL = "wss://echo.websocket.org";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_chat, container, false);

        editChatMessage = root.findViewById(R.id.edit_chat_message);
        buttonSendMessage = root.findViewById(R.id.button_send_message);
        textChatMessages = root.findViewById(R.id.text_chat_messages);
        textChatMessages.setMovementMethod(new ScrollingMovementMethod()); // Enable scrolling
        buttonSendMessage.setEnabled(false); // Initially disable until connection is open


        buttonSendMessage.setOnClickListener(v -> {
            String message = editChatMessage.getText().toString().trim();
            if (!message.isEmpty() && webSocketClient != null) {
                if (webSocketClient.sendMessage(message)) {
                    // Optionally clear input after sending, or wait for echo
                    // editChatMessage.setText("");
                } else {
                    showToast("Failed to send. WebSocket not connected.");
                }
            } else if (message.isEmpty()) {
                showToast("Message cannot be empty.");
            } else {
                 showToast("WebSocket client not initialized.");
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize and connect WebSocket client
        // Ensure this URL is accessible from the environment.
        webSocketClient = new ChatWebSocketClient(WEB_SOCKET_URL, this);
        appendMessageToView("Connecting to " + WEB_SOCKET_URL + "...");
        webSocketClient.connect();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webSocketClient != null) {
            appendMessageToView("Disconnecting...");
            webSocketClient.disconnect();
        }
        // Consider calling webSocketClient.shutdown() if this is the absolute end of the app,
        // but usually not needed for fragment lifecycle.
    }

    // WebSocketListenerCallback methods
    @Override
    public void onOpen() {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                appendMessageToView("Status: Connected");
                showToast("WebSocket Connected!");
                buttonSendMessage.setEnabled(true); // Enable send button
            });
        }
    }

    @Override
    public void onMessage(String text) {
         if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                appendMessageToView("Received: " + text);
                // If the sent message was not cleared, and this is an echo server,
                // we might want to clear it now if it matches the sent message.
                if (editChatMessage.getText().toString().equals(text)) {
                    editChatMessage.setText("");
                }
            });
        }
    }

    @Override
    public void onClosing(int code, String reason) {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                appendMessageToView("Status: Closing - " + code + " / " + reason);
                buttonSendMessage.setEnabled(false);
            });
        }
    }

    @Override
    public void onFailure(Throwable t, okhttp3.Response response) {
        if (isAdded() && getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                String errorMsg = "Status: Connection Failed - " + t.getMessage();
                appendMessageToView(errorMsg);
                showToast(errorMsg);
                buttonSendMessage.setEnabled(false);
                if (response != null) {
                     appendMessageToView("Failure Response: Code=" + response.code() + ", Message=" + response.message());
                }
            });
        }
    }

    private void appendMessageToView(String message) {
        // Ensure UI updates are on the main thread (already handled by runOnUiThread in callbacks)
        textChatMessages.append(message + "\n");
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
