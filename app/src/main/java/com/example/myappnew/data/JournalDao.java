package com.example.myappnew.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

/**
 * Data Access Object for JournalEntry entities.
 *
 * ---
 * <h4>Testing Strategy:</h4>
 * <ul>
 *     <li>Use an in-memory Room database for hermetic testing (<code>Room.inMemoryDatabaseBuilder()</code>).</li>
 *     <li>Test basic CRUD operations: insert, update, delete, deleteAll.</li>
 *     <li>Verify that queries (e.g., <code>getAllEntries</code>) return expected data after operations.</li>
 *     <li>Test LiveData observation by ensuring observers are notified of data changes.</li>
 *     <li>Requires AndroidJUnit4 runner and Room testing artifacts.</li>
 * </ul>
 * ---
 */
@Dao
public interface JournalDao {
    @Insert
    void insert(JournalEntry entry);

    @Update
    void update(JournalEntry entry);

    @Delete
    void delete(JournalEntry entry);

    @Query("DELETE FROM journal_entries")
    void deleteAllEntries();

    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    LiveData<List<JournalEntry>> getAllEntries();
}
