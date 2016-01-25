package com.github.vankain.crashcanary_library.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.vankain.crashcanary_library.R;
import com.github.vankain.crashcanary_library.log.Crash;
import com.github.vankain.crashcanary_library.log.CrashCanaryInternals;
import com.github.vankain.crashcanary_library.log.LogWriter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.text.format.DateUtils.FORMAT_SHOW_DATE;
import static android.text.format.DateUtils.FORMAT_SHOW_TIME;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by Administrator on 2016/1/22.
 */
public class DisplayCrashActivity extends Activity {

    private static final String TAG = "DisplayCrashActivity";
    private static final String SHOW_CRASH_EXTRA = "show_latest";
    public static final String SHOW_CRASH_EXTRA_KEY = "crashTime";

    public static PendingIntent createPendingIntent(Context context) {
        return createPendingIntent(context, null);
    }

    public static PendingIntent createPendingIntent(Context context, String blockStartTime) {
        Intent intent = new Intent(context, DisplayCrashActivity.class);
        intent.putExtra(SHOW_CRASH_EXTRA, blockStartTime);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(context, 1, intent, FLAG_UPDATE_CURRENT);
    }

    // null until it's been first loaded.
    private List<Crash> mCrashEntries = new ArrayList<>();
    private String mCrashTime;

    private ListView mListView;
    private TextView mFailureView;
    private Button mActionButton;
    private int mMaxStoredCrashCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCrashTime = savedInstanceState.getString(SHOW_CRASH_EXTRA_KEY);
        } else {
            Intent intent = getIntent();
            if (intent.hasExtra(SHOW_CRASH_EXTRA)) {
                mCrashTime = intent.getStringExtra(SHOW_CRASH_EXTRA);
            }
        }

        //noinspection unchecked
//        mBlockEntries = (List<Block>) getLastNonConfigurationInstance();

        setContentView(R.layout.crash_canary_display_leak);

        mListView = (ListView) findViewById(R.id.__leak_canary_display_leak_list);
        mFailureView = (TextView) findViewById(R.id.__leak_canary_display_leak_failure);
        mActionButton = (Button) findViewById(R.id.__leak_canary_action);

        mMaxStoredCrashCount = getResources().getInteger(R.integer.block_canary_max_stored_count);

        updateUi();
    }

    // No, it's not deprecated. Android lies.
    @Override
    public Object onRetainNonConfigurationInstance() {
        return mCrashEntries;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SHOW_CRASH_EXTRA_KEY, mCrashTime);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoadCrashs.load(this);
    }

    @Override
    public void setTheme(int resid) {
        // We don't want this to be called with an incompatible theme.
        // This could happen if you implement runtime switching of themes
        // using ActivityLifecycleCallbacks.
        if (resid != R.style.crash_canary_BlockCanary_Base) {
            return;
        }
        super.setTheme(resid);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LoadCrashs.forgetActivity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final Crash crash = getCrash(mCrashTime);
        if (crash != null) {
            menu.add(R.string.crash_canary_share_leak)
                    .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            shareCrash(crash);
                            return true;
                        }
                    });
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mCrashTime = null;
            updateUi();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mCrashTime != null) {
            mCrashTime = null;
            updateUi();
        } else {
            super.onBackPressed();
        }
    }

    private void shareCrash(Crash crash) {
        String leakInfo = crash.toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, leakInfo);
        startActivity(Intent.createChooser(intent, getString(R.string.crash_canary_share_with)));
    }


    private void updateUi() {
        final Crash crash = getCrash(mCrashTime);
        if (crash == null) {
            mCrashTime = null;
        }

        // Reset to defaults
        mListView.setVisibility(VISIBLE);
        mFailureView.setVisibility(GONE);

        if (crash != null) {
            renderBlockDetail(crash);
        } else {
            renderBlockList();
        }
    }

    private void renderBlockList() {
        ListAdapter listAdapter = mListView.getAdapter();
        if (listAdapter instanceof CrashListAdapter) {
            ((CrashListAdapter) listAdapter).notifyDataSetChanged();
        } else {
            CrashListAdapter adapter = new CrashListAdapter();
            mListView.setAdapter(adapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mCrashTime = mCrashEntries.get(position).time;
                    updateUi();
                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                invalidateOptionsMenu();
                ActionBar actionBar = getActionBar();
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(false);
                }
            }
            setTitle(getString(R.string.crash_canary_block_list_title, getPackageName()));
            mActionButton.setText(R.string.crash_canary_delete_all);
            mActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LogWriter.deleteLogFiles();
                    mCrashEntries = Collections.emptyList();
                    updateUi();
                }
            });
        }
        mActionButton.setVisibility(mCrashEntries.size() == 0 ? GONE : VISIBLE);
    }

    private void renderBlockDetail(final Crash crash) {
        ListAdapter listAdapter = mListView.getAdapter();
        final CrashDetailAdapter adapter;
        if (listAdapter instanceof CrashDetailAdapter) {
            adapter = (CrashDetailAdapter) listAdapter;
        } else {
            adapter = new CrashDetailAdapter();
            mListView.setAdapter(adapter);
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    adapter.toggleRow(position);
                }
            });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                invalidateOptionsMenu();
                ActionBar actionBar = getActionBar();
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }
            }
            mActionButton.setVisibility(VISIBLE);
            mActionButton.setText(R.string.crash_canary_delete);
            mActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (crash != null) {
                        crash.logFile.delete();
                        mCrashTime = null;
                        mCrashEntries.remove(crash);
                        updateUi();
                    }
                }
            });
        }
        adapter.update(crash);
        setTitle(getString(R.string.crash_canary_class_has_crashed, crash.time));

    }

    private Crash getCrash(String time) {
        if (mCrashEntries == null) {
            return null;
        }
        for (Crash crash : mCrashEntries) {
            if(crash.time == null || time == null) return null;
            if (crash.time.equals(time)) {
                return crash;
            }
        }
        return null;
    }

    class CrashListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mCrashEntries.size();
        }

        @Override
        public Crash getItem(int position) {
            return mCrashEntries.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(DisplayCrashActivity.this)
                        .inflate(R.layout.crash_canary_row, parent, false);
            }
            TextView titleView = (TextView) convertView.findViewById(R.id.__leak_canary_row_text);
            TextView timeView = (TextView) convertView.findViewById(R.id.__leak_canary_row_time);
            Crash crash = getItem(position);

            String index;
            if (position == 0 && mCrashEntries.size() == mMaxStoredCrashCount) {
                index = "MAX. ";
            } else {
                index = (mCrashEntries.size() - position) + ". ";
            }

            String title = index +
                    getString(R.string.crash_canary_class_has_crashed, crash.cause);
            titleView.setText(title);
            String time = DateUtils.formatDateTime(DisplayCrashActivity.this,
                    crash.logFile.lastModified(), FORMAT_SHOW_TIME | FORMAT_SHOW_DATE);
            timeView.setText(time);
            return convertView;
        }
    }

    static class LoadCrashs implements Runnable {

        static final List<LoadCrashs> inFlight = new ArrayList<>();

        static final Executor backgroundExecutor = Executors.newSingleThreadExecutor();

        static void load(DisplayCrashActivity activity) {
            LoadCrashs loadCrashs = new LoadCrashs(activity);
            inFlight.add(loadCrashs);
            backgroundExecutor.execute(loadCrashs);
        }

        static void forgetActivity() {
            for (LoadCrashs loadCrashs : inFlight) {
                loadCrashs.activityOrNull = null;
            }
            inFlight.clear();
        }

        private DisplayCrashActivity activityOrNull;
        private final Handler mainHandler;

        LoadCrashs(DisplayCrashActivity activity) {
            this.activityOrNull = activity;
            mainHandler = new Handler(Looper.getMainLooper());
        }

        @Override
        public void run() {
            final List<Crash> crashs = new ArrayList<Crash>();
            File[] files = CrashCanaryInternals.getLogFiles();
            if (files != null) {
                for (File crashFile : files) {
                    try {
                        crashs.add(Crash.newInstance(crashFile));
                    } catch (Exception e) {
                        crashFile.delete();
                        Log.e(TAG, "Could not read crash log file, deleted :" + crashFile, e);
                    }
                }
                Collections.sort(crashs, new Comparator<Crash>() {
                    @Override
                    public int compare(Crash lhs, Crash rhs) {
                        return Long.valueOf(rhs.logFile.lastModified())
                                .compareTo(lhs.logFile.lastModified());
                    }
                });
            }
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    inFlight.remove(LoadCrashs.this);
                    if (activityOrNull != null) {
                        activityOrNull.mCrashEntries = crashs;
                        activityOrNull.updateUi();
                    }
                }
            });
        }
    }
}
