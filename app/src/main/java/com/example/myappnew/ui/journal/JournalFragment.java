package com.example.myappnew.ui.journal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myappnew.R;
import com.example.myappnew.data.JournalEntry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Date;

/**
 * Fragment for displaying and managing journal entries.
 *
 * ---
 * <h4>Testing Strategy:</h4>
 * <ul>
 *     <li><strong>Unit Tests (with a ViewModel if refactored):</strong>
 *         <ul>
 *             <li>If logic is moved to a ViewModel, test ViewModel's interaction with a mocked DAO or Repository.</li>
 *             <li>Test LiveData updates in the ViewModel.</li>
 *         </ul>
 *     </li>
 *     <li><strong>Integration/UI Tests (Espresso):</strong>
 *         <ul>
 *             <li>Mock or use an in-memory <code>AppDatabase</code> to provide controlled data for the DAO.</li>
 *             <li>Verify <code>RecyclerView</code> displays the correct data from the DAO.</li>
 *             <li>Test FAB click:
 *                 <ul>
 *                     <li>Verify a new (dummy) entry is added to the database (check DAO or LiveData observer).</li>
 *                     <li>Verify the <code>RecyclerView</code> updates to show the new entry.</li>
 *                 </ul>
 *             </li>
 *             <li>Test scrolling behavior if the list is long.</li>
 *             <li>Test item click/long-click interactions if implemented in the future.</li>
 *         </ul>
 *     </li>
 * </ul>
 * ---
 */
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class JournalFragment extends Fragment {

    private RecyclerView recyclerView;
    private JournalEntryAdapter adapter;
    private FloatingActionButton fabAddEntry;
    private JournalViewModel journalViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new JournalEntryAdapter();
        journalViewModel = new ViewModelProvider(this).get(JournalViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_journal, container, false);

        recyclerView = root.findViewById(R.id.recycler_view_journal);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        fabAddEntry = root.findViewById(R.id.fab_add_journal_entry);
        fabAddEntry.setOnClickListener(v -> {
            JournalEntry newEntry = new JournalEntry("Dummy journal entry: " + new Date().toString());
            journalViewModel.insert(newEntry);
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        journalViewModel.getAllEntries().observe(getViewLifecycleOwner(), entries -> {
            adapter.submitList(entries);
        });
    }
}
