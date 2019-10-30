package com.w3engineers.mesh.ui.meshlog;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.w3engineers.ext.viper.R;
import com.w3engineers.ext.viper.databinding.FragmentMessageBinding;
import com.w3engineers.mesh.model.MeshLogModel;
import com.w3engineers.mesh.ui.base.BaseFragment;
import com.w3engineers.mesh.ui.createuser.CreateUserActivity;
import com.w3engineers.mesh.util.Constant;
import com.w3engineers.mesh.util.HandlerUtil;
import com.w3engineers.mesh.util.MeshLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MeshLogFragment extends BaseFragment implements View.OnClickListener {
    private FragmentMessageBinding binding;
    private IntentFilter intentFilter;
    private String[] logArray = {"All", "Info", "Warning", "Error", "Special"};
    private final int ALL = 0, INFO = 1, WARNING = 2, ERROR = 3, SPECIAL = 4;


    private int type = ALL;

    private MeshLogAdapter mAdapter;

    private List<MeshLogModel> logList;

    private boolean isScrollOff;

    private int searchPosition;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_message, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        logList = new ArrayList<>();
        initAdapter();
        initSearch();
        initAdvanceSearch();
        initCheckBoxListener();
        binding.textViewRestart.setOnClickListener(this);

        readFile(ALL);

        intentFilter = new IntentFilter();
        intentFilter.addAction("com.w3engineers.meshrnd.DEBUG_MESSAGE");

       /* binding.scrollView.post(new Runnable() {
            @Override
            public void run() {
                binding.scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });*/


        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String logName = logArray[position];
                binding.editTextAdvanceSearch.setText("");
                binding.editTextSearch.setText("");
                showItem(logName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        binding.textViewClear.setOnClickListener(v -> {
            mAdapter.clear();
            logList.clear();
            // We can remove text file also
            // MeshLog.clearLog();
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            getContext().unregisterReceiver(callBroadcast);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        getContext().registerReceiver(callBroadcast, intentFilter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_left:
                leftSectionSearch();
                break;
            case R.id.image_view_right:
                rightSectionSearch();
                break;
            case R.id.text_view_restart:
                restartApp();
                break;
        }
    }

    private void restartApp() {
        Intent mStartActivity = new Intent(getActivity(), CreateUserActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getActivity(), mPendingIntentId, mStartActivity,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    private void showItem(String logItem) {
        if (logItem.equalsIgnoreCase(logArray[2])) {
            type = WARNING;
        } else if (logItem.equalsIgnoreCase(logArray[1])) {
            type = INFO;

        } else if (logItem.equalsIgnoreCase(logArray[3])) {
            type = ERROR;

        } else if (logItem.equalsIgnoreCase(logArray[4])) {
            type = SPECIAL;
        } else {
            type = ALL;
        }

        mAdapter.clear();
        HandlerUtil.postForeground(() -> filterByLogTag(type), 300);

    }

    private void readFile(int type) {
/*        File sdCard = Environment.getExternalStorageDirectory();
        File directory = new File(sdCard.getAbsolutePath() +
                "/MeshRnD");*/
        String sdCard = Constant.Directory.PARENT_DIRECTORY + Constant.Directory.MESH_LOG;
        File directory = new File(sdCard);

        File file = new File(directory, Constant.CURRENT_LOG_FILE_NAME);
        StringBuilder text = new StringBuilder();

        try {

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                int logType = 0;
                if (line.startsWith(MeshLog.INFO)) {
                    logType = INFO;
                } else if (line.startsWith(MeshLog.WARNING)) {
                    logType = WARNING;
                } else if (line.startsWith(MeshLog.ERROR)) {
                    logType = ERROR;
                } else if (line.startsWith(MeshLog.SPECIAL)) {
                    logType = SPECIAL;
                }

                MeshLogModel model = new MeshLogModel(logType, line);
                mAdapter.addItemToPosition(model, 0);
                logList.add(0, model);
            }


            br.close();

            scrollSmoothly();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //binding.textView.setText(text.toString());
        //mainText = binding.textView.getText().toString();
    }


    public void newMessageFound(final String msg) {

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int logType = 0;
                if (msg.startsWith(MeshLog.INFO)) {
                    logType = INFO;
                } else if (msg.startsWith(MeshLog.WARNING)) {
                    logType = WARNING;
                } else if (msg.startsWith(MeshLog.ERROR)) {
                    logType = ERROR;
                } else if (msg.startsWith(MeshLog.SPECIAL)) {
                    logType = SPECIAL;
                }

                MeshLogModel model = new MeshLogModel(logType, msg);
                if (type == ALL || type == logType) {
                    mAdapter.addItemToPosition(model, 0);

                    if (!isScrollOff) {
                        scrollSmoothly();
                    }
                }
                logList.add(0, model);

                // binding.textView.setText(text.toString());
                // mainText = binding.textView.getText().toString();
            }
        });
    }

    BroadcastReceiver callBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.w3engineers.meshrnd.DEBUG_MESSAGE")) {
                String text = intent.getStringExtra("value");
                newMessageFound(text);
            }
        }
    };

    private void initSearch() {
        binding.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (TextUtils.isEmpty(s.toString())) {
                    binding.editTextAdvanceSearch.setEnabled(true);
                } else {
                    binding.editTextAdvanceSearch.setEnabled(false);
                }

                HandlerUtil.postForeground(() -> {
                    mAdapter.getFilter().filter(s);
                    mAdapter.notifyDataSetChanged();
                }, 500);

            }
        });
    }

    private void initAdvanceSearch() {
        binding.editTextAdvanceSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s.toString())) {
                    binding.imageViewLeft.setEnabled(false);
                    binding.imageViewRight.setEnabled(false);
                    searchPosition = 0;

                    binding.editTextSearch.setEnabled(true);

                } else {
                    binding.imageViewLeft.setEnabled(true);
                    binding.imageViewRight.setEnabled(true);

                    binding.editTextSearch.setEnabled(false);
                }


                HandlerUtil.postForeground(() -> {
                    mAdapter.advanceSearch(s.toString());
                    mAdapter.notifyDataSetChanged();
                }, 500);
            }
        });

        binding.imageViewLeft.setOnClickListener(this);
        binding.imageViewRight.setOnClickListener(this);

        binding.imageViewLeft.setEnabled(false);
        binding.imageViewRight.setEnabled(false);
    }

    private void initCheckBoxListener() {
        binding.checkBoxScroll.setOnCheckedChangeListener((buttonView, isChecked) -> isScrollOff = isChecked);
    }

    private void initAdapter() {
        binding.recyclerViewLog.setHasFixedSize(true);
        binding.recyclerViewLog.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new MeshLogAdapter(getActivity());
        binding.recyclerViewLog.setAdapter(mAdapter);
        mAdapter.clear();
    }

    private void filterByLogTag(int tag) {
        try {
            if (tag == ALL) {
                mAdapter.addItem(logList);
            } else {
                for (MeshLogModel meshLogModel : logList) {
                    if (meshLogModel.getType() == tag) {
                        mAdapter.addItem(meshLogModel);
                    }
                }
            }

            scrollSmoothly();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void scrollSmoothly() {
        binding.recyclerViewLog.smoothScrollToPosition(0);

       /* int index = mAdapter.getItemCount() - 1;
        if (index > 0) {
            binding.recyclerViewLog.smoothScrollToPosition(index);
        }*/
    }

    private void scrollSmoothlyByPosition(int position) {
        if (position >= 0 && position < mAdapter.getMatchedPosition().size()) {
            binding.recyclerViewLog.smoothScrollToPosition(mAdapter.getMatchedPosition().get(position));
        }
    }


    private void leftSectionSearch() {
        if (searchPosition > 0) {
            searchPosition--;
            scrollSmoothlyByPosition(searchPosition);
        }
    }

    private void rightSectionSearch() {
        // Log.d("AdvanceSearchTest", "totalItem: " + mAdapter.getMatchedPosition().size()+" current position: "+searchPosition);
        if (searchPosition < mAdapter.getMatchedPosition().size() - 1) {
            searchPosition++;
            scrollSmoothlyByPosition(searchPosition);
        }
    }
}
