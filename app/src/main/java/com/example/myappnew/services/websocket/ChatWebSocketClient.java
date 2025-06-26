package com.example.myappnew.services.websocket;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * WebSocket client for chat functionality using OkHttp.
 *
 * ---
 * <h4>Testing Strategy (Unit Tests):</h4>
 * <ul>
 *     <li>Mock <code>OkHttpClient</code> and <code>okhttp3.WebSocket</code> to isolate the client's logic.</li>
 *     <li>Verify <code>connect()</code> attempts to create a new WebSocket with the correct URL.</li>
 *     <li>Verify <code>sendMessage()</code> attempts to send a message via the WebSocket if connected, and handles not-connected state.</li>
 *     <li>Verify <code>disconnect()</code> attempts to close the WebSocket.</li>
 *     <li>Simulate <code>WebSocketListener</code> events (onOpen, onMessage, onClosing, onFailure) on the mocked WebSocket and verify that the corresponding <code>WebSocketListenerCallback</code> methods are invoked.</li>
 * </ul>
 * ---
 */
public class ChatWebSocketClient {

    private OkHttpClient client;
    private WebSocket webSocket;
    private WebSocketListenerCallback listenerCallback;
    private String serverUrl;

    public ChatWebSocketClient(String serverUrl, WebSocketListenerCallback listenerCallback) {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
        this.serverUrl = serverUrl;
        this.listenerCallback = listenerCallback;
    }

    public void connect() {
        if (webSocket != null) {
            System.out.println("ChatWebSocketClient: Already connected or connection attempt in progress.");
            return;
        }
        Request request = new Request.Builder().url(serverUrl).build();
        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(@NonNull WebSocket ws, @NonNull Response response) {
                System.out.println("ChatWebSocketClient: Connection opened.");
                if (listenerCallback != null) {
                    listenerCallback.onOpen();
                }
            }

            @Override
            public void onMessage(@NonNull WebSocket ws, @NonNull String text) {
                System.out.println("ChatWebSocketClient: Received message: " + text);
                if (listenerCallback != null) {
                    listenerCallback.onMessage(text);
                }
            }

            @Override
            public void onMessage(@NonNull WebSocket ws, @NonNull ByteString bytes) {
                System.out.println("ChatWebSocketClient: Received binary message: " + bytes.hex());
            }

            @Override
            public void onClosing(@NonNull WebSocket ws, int code, @NonNull String reason) {
                System.out.println("ChatWebSocketClient: Connection closing: " + code + " / " + reason);
                if (listenerCallback != null) {
                    listenerCallback.onClosing(code, reason);
                }
                webSocket = null;
            }

            @Override
            public void onFailure(@NonNull WebSocket ws, @NonNull Throwable t, @Nullable Response response) {
                System.err.println("ChatWebSocketClient: Connection failure: " + t.getMessage());
                t.printStackTrace();
                if (listenerCallback != null) {
                    listenerCallback.onFailure(t, response);
                }
                webSocket = null;
            }
        });
    }

    public boolean sendMessage(String message) {
        if (webSocket != null) {
            System.out.println("ChatWebSocketClient: Sending message: " + message);
            return webSocket.send(message);
        }
        System.err.println("ChatWebSocketClient: Cannot send message, WebSocket is not connected.");
        return false;
    }

    public void disconnect() {
        if (webSocket != null) {
            System.out.println("ChatWebSocketClient: Closing WebSocket connection.");
            webSocket.close(1000, "Client disconnected gracefully.");
        }
    }

    public void shutdown() {
        client.dispatcher().executorService().shutdown();
    }
}
