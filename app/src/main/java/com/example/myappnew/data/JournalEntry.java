package com.example.myappnew.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.Ignore; // Added missing import for @Ignore
import java.util.Date;

@Entity(tableName = "journal_entries")
public class JournalEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String content;
    public long timestamp;

    // 多模态内容字段
    public String imageUri;    // 照片
    public String audioUri;    // 语音
    public String videoUri;    // 视频
    // 用户画像字段（可扩展为JSON或单独表）
    public String userProfileJson;

    @Ignore // Mark this constructor to be ignored by Room
    public JournalEntry(String content) {
        this.content = content;
        this.timestamp = new Date().getTime();
    }

    public JournalEntry(int id, String content, long timestamp) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
    }

    @Ignore // Mark this constructor to be ignored by Room
    public JournalEntry() {}


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }
    public String getAudioUri() { return audioUri; }
    public void setAudioUri(String audioUri) { this.audioUri = audioUri; }
    public String getVideoUri() { return videoUri; }
    public void setVideoUri(String videoUri) { this.videoUri = videoUri; }
    public String getUserProfileJson() { return userProfileJson; }
    public void setUserProfileJson(String userProfileJson) { this.userProfileJson = userProfileJson; }

    public Date getDate() { return new Date(timestamp); }
}
