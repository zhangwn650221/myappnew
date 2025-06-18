package com.example.myappnew.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "journal_entries")
public class JournalEntry {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String content;
    public long timestamp;

    public JournalEntry(String content) {
        this.content = content;
        this.timestamp = new Date().getTime();
    }

    public JournalEntry(int id, String content, long timestamp) {
        this.id = id;
        this.content = content;
        this.timestamp = timestamp;
    }

    public JournalEntry() {}


    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public Date getDate() { return new Date(timestamp); }
}
