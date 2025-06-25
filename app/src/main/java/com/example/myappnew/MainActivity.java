package com.example.myappnew;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main activity of the application. Sets up the main UI and navigation.
 *
 * ---
 *
 * <h3>Async Operations and Performance Optimization Notes:</h3>
 *
 * This section provides a brief overview of existing asynchronous patterns
 * and key considerations for future performance optimization.
 *
 * <h4>Implemented Asynchronous Operations:</h4>
 * <ul>
 *     <li><strong>Database Access (Room):</strong>
 *         <ul>
 *             <li>Write operations (inserts, updates, deletes) in <code>JournalFragment</code> are performed on a background thread using an <code>ExecutorService</code>.</li>
 *             <li>Read operations (queries returning <code>LiveData</code>) are inherently asynchronous and update UI observers on the main thread.</li>
 *         </ul>
 *     </li>
 *     <li><strong>Simulated LLM Calls:</strong>
 *         <ul>
 *             <li><code>ImageAnalysisService</code> and <code>VoiceAnalysisService</code> use callbacks to handle results from the (dummy) <code>LlmService</code>, simulating asynchronous network calls.</li>
 *         </ul>
 *     </li>
 *     <li><strong>WebSocket Communication (OkHttp):</strong>
 *         <ul>
 *             <li><code>ChatWebSocketClient</code> utilizes OkHttp's <code>WebSocketListener</code>, whose callbacks (<code>onOpen</code>, <code>onMessage</code>, etc.) are executed on background threads. UI updates in <code>ChatFragment</code> are then posted to the main thread.</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * <h4>Key Future Performance Optimization Considerations:</h4>
 * <ul>
 *     <li><strong>Thread Management:</strong>
 *         <ul>
 *             <li>Review and potentially centralize thread pool management for more complex scenarios (e.g., using Kotlin Coroutines, RxJava, or a shared Hilt/Dagger-provided Executor).</li>
 *             <li>Ensure proper lifecycle management of threading resources to prevent leaks.</li>
 *         </ul>
 *     </li>
 *     <li><strong>UI Performance:</strong>
 *         <ul>
 *             <li><strong>RecyclerViews:</strong> Continue using <code>ListAdapter</code>/<code>DiffUtil</code>. Optimize complex item layouts and image loading (lazy loading, proper sizing).</li>
 *             <li><strong>Layouts:</strong> Prefer flatter hierarchies (<code>ConstraintLayout</code>). Use tools like Layout Inspector and Profiler to find bottlenecks. Avoid overdraw.</li>
 *             <li><strong>Main Thread:</strong> Strictly avoid any blocking I/O, long computations, or synchronous network calls on the main thread.</li>
 *         </ul>
 *     </li>
 *     <li><strong>Network Performance:</strong>
 *         <ul>
 *             <li>Implement request cancellation when UI components are destroyed or data is no longer needed.</li>
 *             <li>Use data compression (e.g., Gzip) and caching strategies (HTTP caching via OkHttp, or custom DB caching).</li>
 *         </ul>
 *     </li>
 *     <li><strong>Media & Image Handling:</strong>
 *         <ul>
 *             <li>Efficiently decode and scale images to display dimensions (libraries like Glide/Coil help).</li>
 *             <li>Perform all media processing (transcoding, analysis) on background threads.</li>
 *         </ul>
 *     </li>
 *     <li><strong>Memory Management:</strong>
 *         <ul>
 *             <li>Actively look for and prevent context leaks, especially in background tasks or long-lived objects.</li>
 *             <li>Use Android Studio's Profiler to monitor memory usage and detect leaks. Optimize data structures.</li>
 *         </ul>
 *     </li>
 *     <li><strong>Battery Life:</strong>
 *         <ul>
 *             <li>For background tasks, use <code>WorkManager</code> with appropriate constraints.</li>
 *             <li>Minimize unnecessary network activity and device wakeups. Batch operations where possible.</li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * ---
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNavView = findViewById(R.id.bottom_nav_view);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            NavigationUI.setupWithNavController(bottomNavView, navController);
        }
    }
}