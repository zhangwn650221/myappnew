package com.example.myappnew.ui.journal;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myappnew.R;
import com.example.myappnew.data.JournalEntry;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;


public class JournalEntryAdapter extends ListAdapter<JournalEntry, JournalEntryAdapter.JournalEntryViewHolder> {

    private static final SimpleDateFormat sdf;
    static {
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
    }

    public JournalEntryAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<JournalEntry> DIFF_CALLBACK = new DiffUtil.ItemCallback<JournalEntry>() {
        @Override
        public boolean areItemsTheSame(@NonNull JournalEntry oldItem, @NonNull JournalEntry newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull JournalEntry oldItem, @NonNull JournalEntry newItem) {
            return oldItem.getContent().equals(newItem.getContent()) &&
                   oldItem.getTimestamp() == newItem.getTimestamp();
        }
    };

    @NonNull
    @Override
    public JournalEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_journal_entry, parent, false);
        JournalEntryViewHolder holder = new JournalEntryViewHolder(itemView);
        itemView.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                JournalEntry entry = getItem(position);
                // 跳转到详情页
                android.os.Bundle args = new android.os.Bundle();
                args.putInt("entry_id", entry.getId());
                androidx.navigation.Navigation.findNavController(itemView).navigate(
                    R.id.action_navigation_journal_to_journalDetailFragment, args
                );
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull JournalEntryViewHolder holder, int position) {
        JournalEntry currentEntry = getItem(position);
        holder.textContent.setText(currentEntry.getContent());
        holder.textTimestamp.setText(sdf.format(currentEntry.getDate()));
        // 多模态内容标记
        StringBuilder multimodal = new StringBuilder();
        if (currentEntry.getImageUri() != null && !currentEntry.getImageUri().isEmpty()) multimodal.append("[图片] ");
        if (currentEntry.getAudioUri() != null && !currentEntry.getAudioUri().isEmpty()) multimodal.append("[语音] ");
        if (currentEntry.getVideoUri() != null && !currentEntry.getVideoUri().isEmpty()) multimodal.append("[视频] ");
        if (multimodal.length() > 0) {
            holder.textMultimodal.setText(multimodal.toString().trim());
            holder.textMultimodal.setVisibility(View.VISIBLE);
        } else {
            holder.textMultimodal.setVisibility(View.GONE);
        }
    }

    static class JournalEntryViewHolder extends RecyclerView.ViewHolder {
        private final TextView textContent;
        private final TextView textTimestamp;
        private final TextView textMultimodal;

        public JournalEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            textContent = itemView.findViewById(R.id.text_journal_content);
            textTimestamp = itemView.findViewById(R.id.text_journal_timestamp);
            textMultimodal = itemView.findViewById(R.id.text_journal_multimodal);
        }
    }
}
