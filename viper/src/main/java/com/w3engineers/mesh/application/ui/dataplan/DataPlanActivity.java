package com.w3engineers.mesh.application.ui.dataplan;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.w3engineers.ext.strom.util.helper.Toaster;
import com.w3engineers.mesh.R;
import com.w3engineers.mesh.application.data.BaseServiceLocator;
import com.w3engineers.mesh.application.data.local.DataPlanConstants;
import com.w3engineers.mesh.application.data.local.dataplan.DataPlanManager;
import com.w3engineers.mesh.application.data.local.model.Seller;
import com.w3engineers.mesh.application.data.local.wallet.WalletManager;
import com.w3engineers.mesh.application.ui.base.TelemeshBaseActivity;
import com.w3engineers.mesh.databinding.ActivityDataPlanBinding;
import com.w3engineers.mesh.util.DialogUtil;
import com.w3engineers.mesh.util.NotificationUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;


public class DataPlanActivity extends TelemeshBaseActivity implements DataPlanManager.DataPlanListener {
    private ActivityDataPlanBinding mBinding;
    private SellerListAdapter sellerListAdapter;
    private DataPlanViewModel viewModel;
    private DataLimitModel dataLimitModel;

    private volatile Map<String, String> roleSwitchMap;
    private RadioButton[] radioButtonsDataPlan, dataLimitRadioButtons;
    private ConstraintLayout[] dataPlanViews;
    private Calendar myCalendar;
    private SimpleDateFormat sdf;

    private ProgressDialog progressDialog;

    private int mCurrentRole;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_data_plan;
    }

    @Override
    protected BaseServiceLocator getServiceLocator() {
        return null;
    }

    @Override
    protected void startUI() {
        initAll();

        changeStatusBarColor();

        roleSwitchingMapMsg();

        prepareDataPlanRadio();

        initRecyclerView();

        loadUI();

        setEventListener();

        parseIntent();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DataPlanManager.getInstance().getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_BUYER) {
            prepareSellerData();
        }
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);

        if (view.getId() == R.id.imageView_back) {
            finish();
        } else if (view.getId() == R.id.ic_wallet) {
            WalletManager.openActivity(this);
        } else if (view.getId() == R.id.save_button) {
            checkSharingLimit();
        } else if (view.getId() == R.id.status) {
            Seller seller = (Seller) view.getTag();
            onButtonClickListener(seller);
        }
    }

    @Override
    public void onConnectingWithSeller(String sellerAddress) {
        runOnUiThread(() -> {
            for (Seller item : getAdapter().getItems()) {
                if (item.getId().equalsIgnoreCase(sellerAddress)) {
                    item.setBtnText(DataPlanConstants.SELLERS_BTN_TEXT.PURCHASING);
                    item.setBtnEnabled(false);
                    getAdapter().addItem(item);
                    return;
                }
            }
        });
    }

    @Override
    public void onPurchaseFailed(String sellerAddress, String msg) {
        runOnUiThread(() -> {
            for (Seller item : getAdapter().getItems()) {
                if (item.getId().equalsIgnoreCase(sellerAddress)) {
                    item.setBtnText(DataPlanConstants.SELLERS_BTN_TEXT.PURCHASE);
                    item.setBtnEnabled(true);
                    getAdapter().addItem(item);
                    Toaster.showLong(msg);
                    return;
                }
            }
        });
    }

    @Override
    public void onPurchaseSuccess(String sellerAddress, double purchasedData, long blockNumber) {
        runOnUiThread(() -> {
            for (Seller item : getAdapter().getItems()) {
                if (item.getId().equalsIgnoreCase(sellerAddress)) {
                    item.setBtnText(DataPlanConstants.SELLERS_BTN_TEXT.CLOSE);
                    item.setBtnEnabled(true);
                    item.setPurchasedData(purchasedData);
                    item.setBlockNumber(blockNumber);
                    getAdapter().addItem(item);
                    return;
                }
            }
        });
    }

    @Override
    public void onPurchaseClosing(String sellerAddress) {
        runOnUiThread(() -> {
            for (Seller item : getAdapter().getItems()) {
                if (item.getId().equalsIgnoreCase(sellerAddress)) {
                    item.setBtnText(DataPlanConstants.SELLERS_BTN_TEXT.CLOSING);
                    item.setBtnEnabled(false);
                    getAdapter().addItem(item);
                    return;
                }
            }
        });
    }

    @Override
    public void onPurchaseCloseFailed(String sellerAddress, String msg) {
        runOnUiThread(() -> {
            for (Seller item : getAdapter().getItems()) {
                if (item.getId().equalsIgnoreCase(sellerAddress)) {
                    item.setBtnText(DataPlanConstants.SELLERS_BTN_TEXT.CLOSE);
                    item.setBtnEnabled(true);
                    getAdapter().addItem(item);
                    Toaster.showLong(msg);
                    return;
                }
            }
        });
    }

    @Override
    public void onTopUpFailed(String sellerAddress, String msg) {
        runOnUiThread(() -> {
            for (Seller item : getAdapter().getItems()) {
                if (item.getId().equalsIgnoreCase(sellerAddress)) {
                    item.setBtnText(DataPlanConstants.SELLERS_BTN_TEXT.TOP_UP);
                    item.setBtnEnabled(true);
                    getAdapter().addItem(item);
                    Toaster.showLong(msg);
                    return;
                }
            }
        });
    }

    @Override
    public void onRoleSwitchCompleted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null){
                    progressDialog.dismiss();
                }
            }
        });

    }

    @Override
    public void onPurchaseCloseSuccess(String sellerAddress) {
        runOnUiThread(() -> {
            //TODO remove item if seller not connected

            for (Seller item : getAdapter().getItems()) {
                if (item.getId().equalsIgnoreCase(sellerAddress)) {
                    item.setBtnText(DataPlanConstants.SELLERS_BTN_TEXT.PURCHASE);
                    item.setBtnEnabled(true);
                    getAdapter().addItem(item);
                    break;
                }
            }
        });
    }

    @Override
    public void showToastMessage(String msg) {
        runOnUiThread(() -> Toaster.showLong(msg));
    }

    @Override
    public void onBalancedFinished(String sellerAddress, int remain) {
        runOnUiThread(() -> {
            if (remain == 1) {
                for (Seller item : getAdapter().getItems()) {
                    if (item.getId().equalsIgnoreCase(sellerAddress)) {
                        item.setBtnText(DataPlanConstants.SELLERS_BTN_TEXT.TOP_UP);
                        item.setBtnEnabled(true);
                        getAdapter().addItem(item);
                        break;
                    }
                }
            } else {
                for (Seller item : getAdapter().getItems()) {
                    if (item.getId().equalsIgnoreCase(sellerAddress)) {
                        item.setBtnText(DataPlanConstants.SELLERS_BTN_TEXT.PURCHASE);
                        item.setBtnEnabled(true);
                        getAdapter().addItem(item);
                        break;
                    }
                }
            }
        });
    }

    private void prepareSellerData() {

        viewModel.allSellers.observe(this, sellers -> {
            if (sellers != null) {
                getAdapter().clear();
                getAdapter().addItem(sellers);
            }
        });

        viewModel.getAllSellers();
    }

    private SellerListAdapter getAdapter() {
        return (SellerListAdapter) mBinding.dataSellerList.getAdapter();
    }


    private void initAll() {
        mBinding = (ActivityDataPlanBinding) getViewDataBinding();
        viewModel = getViewModel();

        roleSwitchMap = new ConcurrentHashMap<>();
        progressDialog = new ProgressDialog(this);
        dataLimitModel = DataLimitModel.getInstance(getApplicationContext());

        myCalendar = Calendar.getInstance();
        sdf = new SimpleDateFormat("dd/MM/yy");

        mCurrentRole = DataPlanManager.getInstance().getDataPlanRole();
        dataLimitModel.setInitialRole(mCurrentRole);
        mBinding.setDataLimitModel(dataLimitModel);

        setClickListener(mBinding.imageViewBack, mBinding.icWallet,
                mBinding.layoutDataplan, mBinding.saveButton);

        DataPlanManager.getInstance().setDataPlanListener(this);
    }


    private void changeStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }


    private void roleSwitchingMapMsg() {
        roleSwitchMap.put(getKey(DataPlanConstants.USER_ROLE.MESH_USER, DataPlanConstants.USER_ROLE.DATA_BUYER), getResources().getString(R.string.mesh_user_to_buyer));
        roleSwitchMap.put(getKey(DataPlanConstants.USER_ROLE.MESH_USER, DataPlanConstants.USER_ROLE.DATA_SELLER), getResources().getString(R.string.mesh_user_to_seller));
        roleSwitchMap.put(getKey(DataPlanConstants.USER_ROLE.MESH_USER, DataPlanConstants.USER_ROLE.INTERNET_USER), getResources().getString(R.string.mesh_user_to_internet_user));

        roleSwitchMap.put(getKey(DataPlanConstants.USER_ROLE.DATA_BUYER, DataPlanConstants.USER_ROLE.DATA_SELLER), getResources().getString(R.string.data_buyer_to_seller));
        roleSwitchMap.put(getKey(DataPlanConstants.USER_ROLE.DATA_BUYER, DataPlanConstants.USER_ROLE.MESH_USER), getResources().getString(R.string.data_buyer_to_mesh_user));
        roleSwitchMap.put(getKey(DataPlanConstants.USER_ROLE.DATA_BUYER, DataPlanConstants.USER_ROLE.INTERNET_USER), getResources().getString(R.string.data_buyer_to_internet_user));

        roleSwitchMap.put(getKey(DataPlanConstants.USER_ROLE.DATA_SELLER, DataPlanConstants.USER_ROLE.DATA_BUYER), getResources().getString(R.string.data_seller_to_buyer));
        roleSwitchMap.put(getKey(DataPlanConstants.USER_ROLE.DATA_SELLER, DataPlanConstants.USER_ROLE.MESH_USER), getResources().getString(R.string.data_seller_to_mesh_user));
        roleSwitchMap.put(getKey(DataPlanConstants.USER_ROLE.DATA_SELLER, DataPlanConstants.USER_ROLE.INTERNET_USER), getResources().getString(R.string.data_seller_to_internet_user));

        roleSwitchMap.put(getKey(DataPlanConstants.USER_ROLE.INTERNET_USER, DataPlanConstants.USER_ROLE.DATA_BUYER), getResources().getString(R.string.internet_user_to_buyer));
        roleSwitchMap.put(getKey(DataPlanConstants.USER_ROLE.INTERNET_USER, DataPlanConstants.USER_ROLE.DATA_SELLER), getResources().getString(R.string.internet_user_to_seller));
        roleSwitchMap.put(getKey(DataPlanConstants.USER_ROLE.INTERNET_USER, DataPlanConstants.USER_ROLE.MESH_USER), getResources().getString(R.string.internet_user_to_internet_user));
    }

    private String getKey(int prev, int cur) {
        return prev + "" + cur;
    }


    private void prepareDataPlanRadio() {

        radioButtonsDataPlan = new RadioButton[]{mBinding.meshUser, mBinding.dataSeller, mBinding.dataBuyer, mBinding.internetUser};
        dataLimitRadioButtons = new RadioButton[]{mBinding.unlimited, mBinding.limitTo};
        dataPlanViews = new ConstraintLayout[]{mBinding.meshUserLayout, mBinding.dataSellLayout, mBinding.dataBuyLayout, mBinding.internetUserLayout};

        mBinding.dataPlanType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.mesh_user) {
                dataPlanRadioClicked(DataPlanConstants.USER_ROLE.MESH_USER);
            } else if (checkedId == R.id.data_seller) {
                dataPlanRadioClicked(DataPlanConstants.USER_ROLE.DATA_SELLER);
            } else if (checkedId == R.id.data_buyer) {
                dataPlanRadioClicked(DataPlanConstants.USER_ROLE.DATA_BUYER);
            } else if (checkedId == R.id.internet_user) {
                dataPlanRadioClicked(DataPlanConstants.USER_ROLE.INTERNET_USER);
            }
        });
    }

    private void dataPlanRadioClicked(int type) {
        if (mCurrentRole == type)
            return;
        showRoleSwitchConfirmation(mCurrentRole, type);
    }

    private void showRoleSwitchConfirmation(int prevRole, int currentRole) {
        DialogUtil.showConfirmationDialog(DataPlanActivity.this,
                getResources().getString(R.string.switch_role),
                roleSwitchMap.get(getKey(prevRole, currentRole)),
                getResources().getString(R.string.cancel),
                getResources().getString(R.string.yes),
                new DialogUtil.DialogButtonListener() {
            @Override
            public void onClickPositive() {
                mBinding.tvHint.setVisibility(View.GONE);
                setRoleTasks(prevRole, currentRole);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onClickNegative() {
                radioButtonsDataPlan[prevRole].setChecked(true);
            }
        });
    }

    private void setRoleTasks(int prev, int current) {

        progressDialog.setMessage(getResources().getString(R.string.switching_role));
        progressDialog.setCancelable(false);
        progressDialog.show();

        dataPlanViews[prev].setVisibility(View.GONE);
        dataPlanViews[current].setVisibility(View.VISIBLE);

        mCurrentRole = current;
        viewModel.roleSwitch(mCurrentRole);
    }


    private void initRecyclerView() {
        mBinding.dataSellerList.setItemAnimator(null);
        mBinding.dataSellerList.setHasFixedSize(true);
        mBinding.dataSellerList.setLayoutManager(new LinearLayoutManager(this));

        sellerListAdapter = new SellerListAdapter(this);
        mBinding.dataSellerList.setAdapter(sellerListAdapter);
    }


    private void loadUI() {

        dataPlanViews[DataPlanManager.getInstance().getDataPlanRole()].setVisibility(View.VISIBLE);
        dataLimitRadioButtons[dataLimitModel.getDataLimited() ? 1 : 0].setChecked(true);

        setDataLimitEnabled(dataLimitModel.getDataLimited());

        if (dataLimitModel.getFromDate() > 0) {
            mBinding.fromDate.setText(sdf.format(dataLimitModel.getFromDate()));
        } else {
            mBinding.fromDate.setText(sdf.format(myCalendar.getTime()));
            dataLimitModel.setFromDate(myCalendar.getTimeInMillis());
        }
        if (dataLimitModel.getToDate() > 0) {
            mBinding.toDate.setText(sdf.format(dataLimitModel.getToDate()));
        } else {
            mBinding.toDate.setText(sdf.format(myCalendar.getTime()));
            dataLimitModel.setToDate(myCalendar.getTimeInMillis());
        }

        long sharedData = DataPlanManager.getInstance().getSellAmountData();

        if (sharedData <= 0) {
            dataLimitModel.setSharedData(convertMegabytesToBytes(10));
            mBinding.range.setText("10");
        } else {
            int amount = (int) convertBytesToMegabytes(sharedData);
            mBinding.range.setText(amount + "");
        }

        mBinding.fromDate.setEnabled(false);

        mBinding.dataUsageLimited.setVisibility(DataPlanManager.getInstance().getDataAmountMode() == DataPlanConstants.DATA_MODE.LIMITED
                ? View.VISIBLE : View.INVISIBLE);
        mBinding.dataUsageUnlimited.setVisibility(DataPlanManager.getInstance().getDataAmountMode() == DataPlanConstants.DATA_MODE.UNLIMITED
                ? View.VISIBLE : View.INVISIBLE);

        disableSaveButton();
    }

    public double convertBytesToMegabytes(long bytes) {
        return (double) bytes / (1024.0 * 1024.0);
    }

    private long convertMegabytesToBytes(double mb) {
        return (long) mb * 1024 * 1024;
    }

    private void setDataLimitEnabled(boolean value) {
        mBinding.range.setEnabled(value);
        mBinding.toDate.setEnabled(value);
    }


    private void setEventListener() {
        mBinding.range.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (s.length() > 0) {
                    enableSaveButton();
                } else {
                    disableSaveButton();
                }
            }
        });

        DatePickerDialog.OnDateSetListener date = (view, year, monthOfYear, dayOfMonth) -> {

            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, monthOfYear);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            mBinding.fromDate.setText(sdf.format(System.currentTimeMillis()));

            enableSaveButton();
        };


        mBinding.toDate.setOnClickListener(v -> {
            if (dataLimitModel.getToDate() > myCalendar.getTimeInMillis() - 1000) {
                myCalendar.setTimeInMillis(dataLimitModel.getToDate());
            }

            DatePickerDialog toDatePickerDialog = new DatePickerDialog(DataPlanActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
            toDatePickerDialog.getDatePicker().setMinDate(dataLimitModel.getFromDate() > System.currentTimeMillis() ? dataLimitModel.getFromDate() : System.currentTimeMillis());
            toDatePickerDialog.show();
        });
    }


    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(DataPlanActivity.class.getName())) {
            if (intent.getBooleanExtra(DataPlanActivity.class.getName(), false)) {
                NotificationUtil.removeSellerNotification(this);
                showSellerWarningDialog(intent.getIntExtra(DataPlanConstants.IntentKeys.NUMBER_OF_ACTIVE_BUYER, 0));
            }
        }
    }

    private void showSellerWarningDialog(int activeBuyer) {
        long sharedData = DataPlanManager.getInstance().getSellAmountData();
        DialogUtil.showConfirmationDialog(DataPlanActivity.this, "Data Limit exceed",
                "Your data shared limit" + " " + sharedData + " " + "exceeded, there are" + " " + activeBuyer + " " + "active buyer." + "Do you want to enhance shared data limit? If not then all the active channel would be closed",
                getResources().getString(R.string.no),
                getResources().getString(R.string.yes),
                new DialogUtil.DialogButtonListener() {
                    @Override
                    public void onClickPositive() {

                    }

                    @Override
                    public void onCancel() {
                    }

                    @Override
                    public void onClickNegative() {
                        DataPlanManager.getInstance().closeAllActiveChannel();
                    }
                });
    }


    public void onButtonClickListener(Seller item) {
        String btnText = item.getBtnText();
        if (btnText.toLowerCase().equals(DataPlanConstants.SELLERS_BTN_TEXT.CLOSE.toLowerCase())) {
            showDisconnectConfirmation(item);
        } else {
            showInputDialog(item);
        }
    }

    private void showDisconnectConfirmation(Seller seller) {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.disconnect_confirmation, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setView(promptsView);
        TextView bodyText, stopAndRefund, onlyStop, cancel;
        bodyText = promptsView.findViewById(R.id.bodyText);
        stopAndRefund = promptsView.findViewById(R.id.tv_stop_and_refund);

        cancel = (TextView) promptsView.findViewById(R.id.tv_cancel);

        bodyText.setText("You have already used up " + seller.getUsedData() + "MB of " + seller.getPurchasedData()
                + "MB. Are you sure to stop data usage now?");

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        stopAndRefund.setOnClickListener(v -> {
            DataPlanManager.getInstance().closePurchase(seller.getId());
            alertDialog.cancel();
        });
        cancel.setOnClickListener(v -> {
            alertDialog.cancel();
        });

        alertDialog.show();
    }

    private void showInputDialog(Seller seller) {
        runOnUiThread(() -> {
            LayoutInflater li = LayoutInflater.from(DataPlanActivity.this);
            View promptsView = li.inflate(R.layout.text_input_dataamount, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DataPlanActivity.this);
            alertDialogBuilder.setView(promptsView);

            TextView tvOk, tvCancel;
            final EditText userInput = (EditText) promptsView.findViewById(R.id.et_user_input);
            tvOk = (TextView) promptsView.findViewById(R.id.tv_ok);
            tvCancel = (TextView) promptsView.findViewById(R.id.tv_cancel);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.setCancelable(false);
            tvOk.setOnClickListener(v -> {
                String inputText = userInput.getText().toString();
                if (inputText.length() > 0) {
                    double amount = Double.valueOf(inputText);
                    if (amount > 0) {
                        DataPlanManager.getInstance().initPurchase(amount, seller.getId());
                    } else {
                        Toast.makeText(DataPlanActivity.this, "Data amount should be bigger than zero.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(DataPlanActivity.this, "Data amount required.", Toast.LENGTH_SHORT).show();
                }
                alertDialog.cancel();
            });

            tvCancel.setOnClickListener(v -> {
                alertDialog.cancel();
            });

            alertDialog.show();
        });
    }


    public void onRadioUnlimitedButtonClicked(View view) {
        setDataLimitEnabled(false);
        mBinding.dataUsageLimited.setVisibility(View.INVISIBLE);
        mBinding.dataUsageUnlimited.setVisibility(View.VISIBLE);

        mBinding.textViewDataLimitWarning.setVisibility(View.GONE);
        enableSaveButton();
    }

    public void onRadioLimitedButtonClicked(View view) {
        setDataLimitEnabled(true);
        mBinding.dataUsageLimited.setVisibility(View.VISIBLE);
        mBinding.dataUsageUnlimited.setVisibility(View.INVISIBLE);
        enableSaveButton();
    }

    private void disableSaveButton() {
        mBinding.saveButton.setEnabled(false);
        int paddingTopBottom = mBinding.saveButton.getPaddingTop();
        int paddingLeftRight = mBinding.saveButton.getTotalPaddingLeft();
        mBinding.saveButton.setBackground(ContextCompat.getDrawable(DataPlanActivity.this, R.drawable.rectangular_gray_small));
        mBinding.saveButton.setTextColor(this.getResources().getColor(R.color.colorEtBorder));
        mBinding.saveButton.setPadding(paddingLeftRight, paddingTopBottom, paddingLeftRight, paddingTopBottom);
    }

    private void enableSaveButton() {
        mBinding.saveButton.setEnabled(true);
        int paddingTopBottom = mBinding.saveButton.getPaddingTop();
        int paddingLeftRight = mBinding.saveButton.getTotalPaddingLeft();
        mBinding.saveButton.setBackground(ContextCompat.getDrawable(DataPlanActivity.this, R.drawable.ractangular_green_small));
        mBinding.saveButton.setTextColor(DataPlanActivity.this.getResources().getColor(R.color.colorGradientPrimary));
        mBinding.saveButton.setPadding(paddingLeftRight, paddingTopBottom, paddingLeftRight, paddingTopBottom);
    }

    private void checkSharingLimit() {

        if (mBinding.limitTo.isChecked()) {

            long from = 0l, to = 0l;
            long tempSharedData = convertMegabytesToBytes(Integer.valueOf(mBinding.range.getText().toString()));
            try {
                from = getDayWiseTimeStamp(sdf.parse(mBinding.fromDate.getText().toString()).getTime());
                to = getDayWiseTimeStamp(sdf.parse(mBinding.toDate.getText().toString()).getTime()) + (24 * 60 * 60 * 1000 - 1000);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (to <= System.currentTimeMillis()) {
                mBinding.dateError.setVisibility(View.VISIBLE);
                mBinding.dateError.setText(getString(R.string.date_expired));

            } else {
                mBinding.dateError.setVisibility(View.INVISIBLE);
                long usedData = 0;
                try {
                    usedData = DataPlanManager.getInstance().getUsedData(this, from, to);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (tempSharedData <= usedData) {
                    mBinding.dataLimitError.setVisibility(View.VISIBLE);
                    mBinding.dataLimitError.setText(this.getString(R.string.data_lomit_larger_needed));

                } else {
                    mBinding.dataLimitError.setVisibility(View.INVISIBLE);
                    dataLimitModel.setFromDate(from);
                    dataLimitModel.setToDate(to);
                    dataLimitModel.setSharedData(tempSharedData);

                    dataLimitModel.setDataLimited(true);

                    if (dataLimitModel != null
                            && dataLimitModel.getUsedData().getValue() != null
                            && dataLimitModel.getSharedData().getValue() != null) {

                        if (dataLimitModel.getUsedData().getValue() >= (dataLimitModel.getSharedData().getValue() - DataPlanConstants.SELLER_MINIMUM_WARNING_DATA)) {
                            mBinding.textViewDataLimitWarning.setVisibility(View.VISIBLE);
                        } else {
                            mBinding.textViewDataLimitWarning.setVisibility(View.GONE);
                        }
                    } else {
                        mBinding.textViewDataLimitWarning.setVisibility(View.GONE);
                    }

                    disableSaveButton();
                }
            }
        } else {
            mBinding.dataLimitError.setVisibility(View.INVISIBLE);
            mBinding.dateError.setVisibility(View.INVISIBLE);

            dataLimitModel.setDataLimited(false);

            disableSaveButton();
        }
    }

    public long getDayWiseTimeStamp(long timeStamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeStamp);
        cal.set(Calendar.HOUR_OF_DAY, 0); //set hours to zero
        cal.set(Calendar.MINUTE, 0); // set minutes to zero
        cal.set(Calendar.SECOND, 0); //set seconds to zero
        return (cal.getTimeInMillis() / 1000) * 1000;
    }

    private DataPlanViewModel getViewModel() {
        return ViewModelProviders.of(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new DataPlanViewModel(getApplication());
            }
        }).get(DataPlanViewModel.class);
    }
}