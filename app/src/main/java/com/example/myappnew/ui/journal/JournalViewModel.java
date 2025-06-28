package com.example.myappnew.ui.journal;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import com.example.myappnew.data.JournalDao;
import com.example.myappnew.data.JournalEntry;
import java.util.List;
import javax.inject.Inject;
import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class JournalViewModel extends ViewModel {

    private final JournalDao journalDao;
    private final LiveData<List<JournalEntry>> allEntries;

    @Inject
    public JournalViewModel(JournalDao journalDao) {
        this.journalDao = journalDao;
        this.allEntries = journalDao.getAllEntries();
    }

    public LiveData<List<JournalEntry>> getAllEntries() {
        return allEntries;
    }

    public void insert(JournalEntry journalEntry) {
        Completable.fromAction(() -> journalDao.insert(journalEntry))
                .subscribeOn(Schedulers.io())
                .subscribe();
    }
}
