package com.example.myappnew.di;

import android.content.Context;
import androidx.room.Room;
import com.example.myappnew.data.AppDatabase;
import com.example.myappnew.data.JournalDao;
import com.example.myappnew.services.llm.LlmService;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import javax.inject.Singleton;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "journal_database").build();
    }

    @Provides
    public JournalDao provideJournalDao(AppDatabase database) {
        return database.journalDao();
    }

    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder().build();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        // We need a base URL, even if it's a dummy one for now.
        // This will be replaced by actual service URLs in specific provider modules.
        return new Retrofit.Builder()
                .baseUrl("https://api.example.com/")
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    public LlmService provideLlmService(Retrofit retrofit) {
        return retrofit.create(LlmService.class);
    }
}
