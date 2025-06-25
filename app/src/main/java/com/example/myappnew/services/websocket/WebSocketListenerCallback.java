package com.example.myappnew.services.websocket;

public interface WebSocketListenerCallback {
    void onOpen();
    void onMessage(String text);
    void onClosing(int code, String reason);
    void onFailure(Throwable t, okhttp3.Response response); // response can be null
}
