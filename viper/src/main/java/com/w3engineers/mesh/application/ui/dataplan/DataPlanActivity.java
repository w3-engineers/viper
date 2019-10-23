package com.w3engineers.mesh.application.ui.dataplan;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


import com.w3engineers.mesh.application.ui.util.ToastUtil;
import com.w3engineers.ext.strom.application.ui.base.BaseActivity;
import com.w3engineers.mesh.R;
import com.w3engineers.mesh.application.data.local.DataPlanConstants;
import com.w3engineers.mesh.application.data.local.db.DatabaseService;
import com.w3engineers.mesh.application.data.local.helper.PreferencesHelperDataplan;
import com.w3engineers.mesh.application.data.local.helper.TimeHelper;
import com.w3engineers.mesh.application.data.local.model.Seller;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseConstants;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseManagerBuyer;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseManagerSeller;
import com.w3engineers.mesh.application.ui.wallet.WalletActivity;
import com.w3engineers.mesh.databinding.ActivityDataPlanBinding;
import com.w3engineers.mesh.util.DialogUtil;
import com.w3engineers.mesh.util.NotificationUtil;
import com.w3engineers.mesh.util.Util;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;


public class DataPlanActivity extends BaseActivity implements PurchaseManagerBuyer.PurchaseManagerBuyerListener {
    private ActivityDataPlanBinding mBinding;
    private SellerListAdapter sellerListAdapter;
    private DataPlanViewModel dataPlanViewModel;
    private volatile Map<String, String> msgMap;
    private List<RadioButton> dataShareRadioButtons = new ArrayList<>();
    private List<ConstraintLayout> dataSharingViews = new ArrayList<>();
    private List<RadioButton> dataLimitRadioButtons = new ArrayList<>();
    // private TextView dataUsage;
    private int mEarlierRole, mCurrentRole;
    private PreferencesHelperDataplan preferencesHelperDataplan;
    private Calendar myCalendar;
    private String myFormat = "dd/MM/yy";
    private SimpleDateFormat sdf;
    private ProgressDialog dialog;
    private EditText currentDateBox;
    private DataLimitModel dataLimitModel;
    private TimeHelper timeHelper;
    //  private PurchaseManagerSeller purchaseManagerSeller;
    //  private PurchaseManagerBuyer purchaseManagerBuyer;
    private ManageSellerList manageSellerList;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_data_plan;
    }

    @Override
    protected void startUI() {
        mBinding = (ActivityDataPlanBinding) getViewDataBinding();
        dataPlanViewModel = getViewModel();
        preferencesHelperDataplan = PreferencesHelperDataplan.on();
        myCalendar = Calendar.getInstance();
        sdf = new SimpleDateFormat(myFormat);

        msgMap = new ConcurrentHashMap<>();


        if (PreferencesHelperDataplan.on().getDataShareMode() == DataPlanConstants.USER_TYPES.DATA_BUYER) {
            PurchaseManagerBuyer.getInstance().setPurchaseManagerBuyerListener(this);
        }


        manageSellerList = ManageSellerList.getInstance(this);

        msgMap.put(getKey(DataPlanConstants.USER_TYPES.MESH_USER, DataPlanConstants.USER_TYPES.DATA_BUYER), getResources().getString(R.string.mesh_user_to_buyer));
        msgMap.put(getKey(DataPlanConstants.USER_TYPES.MESH_USER, DataPlanConstants.USER_TYPES.DATA_SELLER), getResources().getString(R.string.mesh_user_to_seller));
        msgMap.put(getKey(DataPlanConstants.USER_TYPES.MESH_USER, DataPlanConstants.USER_TYPES.INTERNET_USER), getResources().getString(R.string.mesh_user_to_internet_user));

        msgMap.put(getKey(DataPlanConstants.USER_TYPES.DATA_BUYER, DataPlanConstants.USER_TYPES.DATA_SELLER), getResources().getString(R.string.data_buyer_to_seller));
        msgMap.put(getKey(DataPlanConstants.USER_TYPES.DATA_BUYER, DataPlanConstants.USER_TYPES.MESH_USER), getResources().getString(R.string.data_buyer_to_mesh_user));
        msgMap.put(getKey(DataPlanConstants.USER_TYPES.DATA_BUYER, DataPlanConstants.USER_TYPES.INTERNET_USER), getResources().getString(R.string.data_buyer_to_internet_user));

        msgMap.put(getKey(DataPlanConstants.USER_TYPES.DATA_SELLER, DataPlanConstants.USER_TYPES.DATA_BUYER), getResources().getString(R.string.data_seller_to_buyer));
        msgMap.put(getKey(DataPlanConstants.USER_TYPES.DATA_SELLER, DataPlanConstants.USER_TYPES.MESH_USER), getResources().getString(R.string.data_seller_to_mesh_user));
        msgMap.put(getKey(DataPlanConstants.USER_TYPES.DATA_SELLER, DataPlanConstants.USER_TYPES.INTERNET_USER), getResources().getString(R.string.data_seller_to_internet_user));

        msgMap.put(getKey(DataPlanConstants.USER_TYPES.INTERNET_USER, DataPlanConstants.USER_TYPES.DATA_BUYER), getResources().getString(R.string.internet_user_to_buyer));
        msgMap.put(getKey(DataPlanConstants.USER_TYPES.INTERNET_USER, DataPlanConstants.USER_TYPES.DATA_SELLER), getResources().getString(R.string.internet_user_to_seller));
        msgMap.put(getKey(DataPlanConstants.USER_TYPES.INTERNET_USER, DataPlanConstants.USER_TYPES.MESH_USER), getResources().getString(R.string.internet_user_to_internet_user));


        mCurrentRole = preferencesHelperDataplan.getDataShareMode();

        dataLimitModel = DataLimitModel.getInstance(getApplicationContext());
        dataLimitModel.setInitialRole(mCurrentRole);
        mBinding.setDataLimitModel(dataLimitModel);
        mBinding.setLifecycleOwner(this);


        setClickListener(mBinding.imageViewBack, mBinding.icWallet,
                mBinding.layoutDataplan, mBinding.saveButton);

        init();

        dialog = new ProgressDialog(this);

        changeStatusBarColor();
//        FloatingView.on().setOnHotspotQuitListener(this);
        mBinding.currency.setOnClickListener(this);

/*        receiver = new MyBroadcastReceiverToHandleLocalBroadcast();
        IntentFilter i = new IntentFilter();
        i.addAction("data.usage.intent");
        LocalBroadcastManager.getInstance(this).
        registerReceiver(receiver, i);*/
    }

    @Override
    public void onClick(View view) {
        super.onClick(view);
        if (view.getId() == R.id.imageView_back) {
            // Do something
//            FloatingView.on().hideFloatingView();
//            finish();
        } else if (view.getId() == R.id.ic_wallet) {

            Intent intent = new Intent(this, WalletActivity.class);
            startActivity(intent);


        } else if (view.getId() == R.id.save_button) {
            checkSharingLimit();
        } else if (view.getId() == R.id.status) {
            Seller seller = (Seller) view.getTag();
            onButtonClickListener(seller);
        }
    }


    private void init() {
        mBinding.sellPriceInfo.setText(getResources().getString(R.string.txt_selling_prize, String.valueOf(1))
                + getResources().getString(R.string.txt_tmesh) + "/"
                + getResources().getString(R.string.txt_mb));

        mBinding.textViewSellPriceInfo.setText(getResources().getString(R.string.txt_buying_prize, String.valueOf(1))
                + getResources().getString(R.string.txt_tmesh) + "/"
                + getResources().getString(R.string.txt_mb));
        setDataPlanRadio();
        initUIComponents();
        dataLimitControl();
        initRecyclerView();
        //  setBuyerDataInfo();

        loadUI();
        setEventListener();

        parseIntent();
    }

    private void parseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(DataPlanActivity.class.getName())) {
            if (intent.getBooleanExtra(DataPlanActivity.class.getName(), false)) {
                NotificationUtil.removeSellerNotification(this);
                showSellerWarningDialog(intent.getIntExtra(PurchaseConstants.IntentKeys.NUMBER_OF_ACTIVE_BUYER, 0));
            }
        }
    }

    private void initUIComponents() {

        //dataSharingViews
        ConstraintLayout meshLayout = mBinding.meshUserLayout;
        ConstraintLayout sellerLayout = mBinding.dataSellLayout;
        ConstraintLayout buyerLayout = mBinding.dataBuyLayout;
        ConstraintLayout internetUserLayout = mBinding.internetUserLayout;

        dataSharingViews.add(meshLayout);
        dataSharingViews.add(sellerLayout);
        dataSharingViews.add(buyerLayout);
        dataSharingViews.add(internetUserLayout);

    }

    private void initRecyclerView() {
        mBinding.dataSellerList.setItemAnimator(null);
        mBinding.dataSellerList.setHasFixedSize(true);
        mBinding.dataSellerList.setLayoutManager(new LinearLayoutManager(this));

        sellerListAdapter = new SellerListAdapter(this);
        mBinding.dataSellerList.setAdapter(sellerListAdapter);
    }

    private void setDataPlanRadio() {

        RadioButton meshUserBtn = mBinding.meshUser;
        RadioButton dataSellerBtn = mBinding.dataSeller;
        RadioButton dataBuyerBtn = mBinding.dataBuyer;
        RadioButton internetUserBtn = mBinding.internetUser;

        dataShareRadioButtons.add(meshUserBtn);
        dataShareRadioButtons.add(dataSellerBtn);
        dataShareRadioButtons.add(dataBuyerBtn);
        dataShareRadioButtons.add(internetUserBtn);

        //dataLimitRadioButtons
        RadioButton radioButtonUnlimited = mBinding.unlimited;
        RadioButton radioButtonLimited = mBinding.limitTo;

        dataLimitRadioButtons.add(radioButtonUnlimited);
        dataLimitRadioButtons.add(radioButtonLimited);

        mBinding.dataPlanType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.mesh_user) {//  setMeshUserInfo();
                onRadioMeshButtonClicked();
            } else if (checkedId == R.id.data_seller) {// setDataSellInfo();
                onRadioSellerButtonClicked();
            } else if (checkedId == R.id.data_buyer) {//  setDataBuyInfo();
                onRadioBuyerButtonClicked();
            }else if (checkedId == R.id.internet_user) {//  setInternetUserInfo();
                onRadioInternetButtonClicked();
            }
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

    public void onRadioMeshButtonClicked() {
        if (mCurrentRole == DataPlanConstants.USER_TYPES.MESH_USER)
            return;

        showRoleSwitchConfirmation(mCurrentRole, DataPlanConstants.USER_TYPES.MESH_USER);
    }

    public void onRadioSellerButtonClicked() {

        if (mBinding.unlimited.isChecked()) {
            mBinding.textViewDataLimitWarning.setVisibility(View.GONE);
        }

        if (mCurrentRole == DataPlanConstants.USER_TYPES.DATA_SELLER)
            return;


        showRoleSwitchConfirmation(mCurrentRole, DataPlanConstants.USER_TYPES.DATA_SELLER);
    }

    public void onRadioBuyerButtonClicked() {
        if (mCurrentRole == DataPlanConstants.USER_TYPES.DATA_BUYER)
            return;

        showRoleSwitchConfirmation(mCurrentRole, DataPlanConstants.USER_TYPES.DATA_BUYER);
    }

    public void onRadioInternetButtonClicked() {
        if (mCurrentRole == DataPlanConstants.USER_TYPES.INTERNET_USER)
            return;

        showRoleSwitchConfirmation(mCurrentRole, DataPlanConstants.USER_TYPES.INTERNET_USER);
    }

    private void showRoleSwitchConfirmation(int prevRole, int currentRole) {
        DialogUtil.showConfirmationDialog(DataPlanActivity.this, getResources().getString(R.string.switch_role), msgMap.get(getKey(prevRole, currentRole)), getResources().getString(R.string.cancel), getResources().getString(R.string.yes), new DialogUtil.DialogButtonListener() {
            @Override
            public void onClickPositive() {
 /*               if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
                    if (currentRole == PreferencesHelperPaylib.MESH_USER) {
                        mCurrentRole = PreferencesHelperPaylib.MESH_USER;
                        //  setMeshUserInfo();
                        mBinding.tvHint.setVisibility(View.GONE);
                        FloatingView.on().hideFloatingView();
                        setRoleTasks(prevRole, currentRole);
                    } else if (currentRole == PreferencesHelperPaylib.DATA_SELLER) {
                        mCurrentRole = PreferencesHelperPaylib.DATA_SELLER;
                        //   setDataSellInfo();
                        manageSpannableAboveV24(prevRole, currentRole);
                    } else if (currentRole == PreferencesHelperPaylib.DATA_BUYER) {
                        mCurrentRole = PreferencesHelperPaylib.DATA_BUYER;
                        mBinding.tvHint.setVisibility(View.GONE);
                        // setDataBuyInfo();
                        FloatingView.on().hideFloatingView();
                        setRoleTasks(prevRole, currentRole);
                    }
                } else {
                    mBinding.tvHint.setVisibility(View.GONE);
                    setRoleTasks(prevRole, currentRole);
                }
*/
                mBinding.tvHint.setVisibility(View.GONE);
                setRoleTasks(prevRole, currentRole);
//                currencyControl();
            }

            @Override
            public void onCancel() {
                // Toast.makeText(DataPlanActivity.this, "Cancel button", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClickNegative() {
                dataShareRadioButtons.get(prevRole).setChecked(true);
            }
        });
    }

    private void setRoleTasks(int prev, int current) {

        dialog.setMessage(getResources().getString(R.string.switching_role));
        dialog.setCancelable(false);
        dialog.show();


        dataSharingViews.get(prev).setVisibility(View.GONE);
        dataSharingViews.get(current).setVisibility(View.VISIBLE);

        mEarlierRole = mCurrentRole;
        mCurrentRole = current;
        preferencesHelperDataplan.setDataShareMode(mCurrentRole);

        //TODO arif
//        TransportManager.getInstance().setNetworkModeListener(this::onTransportInit);
//        TransportManager.getInstance().restart();
    }

    private void manageSpannableAboveV24(int prev, int current) {
        String osName = getOsName();
        mBinding.tvHint.setText(getString(R.string.open_hotspot_hint_first_part)
                + " " + osName + " " + getString(R.string.open_hotspot_hint_second_part));
        mBinding.tvHint.setVisibility(View.VISIBLE);

        String text = " " + getString(R.string.open_hotspot_hint_third_part);
        SpannableStringBuilder ssBuilder = new SpannableStringBuilder(text);

        ClickableSpan hotspotSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                mBinding.tvHint.setVisibility(View.GONE);
                setRoleTasks(prev, current);
            }
        };

        String hotspot = getString(R.string.open_hotspot_click_here);
        spannableTextCustomization(ssBuilder, hotspotSpan, text, hotspot);

        mBinding.tvHint.append(ssBuilder);
        mBinding.tvHint.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void spannableTextCustomization(SpannableStringBuilder ssBuilder, ClickableSpan clickableSpan,
                                            String wholeText, String spannableText) {
        ssBuilder.setSpan(clickableSpan,
                wholeText.indexOf(spannableText),
                wholeText.indexOf(spannableText) + String.valueOf(spannableText).length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ssBuilder.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                wholeText.indexOf(spannableText),
                wholeText.indexOf(spannableText) + String.valueOf(spannableText).length(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        ssBuilder.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorClickHere)),
                wholeText.indexOf(spannableText), wholeText.indexOf(spannableText) + String.valueOf(spannableText).length(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);

    }

    private void loadUI() {

        dataSharingViews.get(preferencesHelperDataplan.getDataShareMode()).setVisibility(View.VISIBLE);

        // currentLayout = dataSharingViews.get(preferencesHelperDataplan.getDataShareMode());

        dataLimitRadioButtons.get(dataLimitModel.getDataLimited() ? 1 : 0).setChecked(true);

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

        long sharedData = PreferencesHelperDataplan.on().getSellDataAmount();

        if (sharedData <= 0) {
            dataLimitModel.setSharedData(Util.convertMegabytesToBytes(10));
            mBinding.range.setText("10");
        } else {
            int amount = (int) Util.convertBytesToMegabytes(sharedData);
            mBinding.range.setText(amount + "");
        }

        mBinding.fromDate.setEnabled(false);

        mBinding.dataUsageLimited.setVisibility(preferencesHelperDataplan.getDataAmountMode() == DataPlanConstants.DATA_MODE.LIMITED ? View.VISIBLE : View.INVISIBLE);
        mBinding.dataUsageUnlimited.setVisibility(preferencesHelperDataplan.getDataAmountMode() == DataPlanConstants.DATA_MODE.UNLIMITED ? View.VISIBLE : View.INVISIBLE);

        disableSaveButton();

    }


    private void prepareSellerData() {

        manageSellerList.getAllSellers().observe(this, sellers -> {
            getAdapter().clear();
            getAdapter().addItem(sellers);
        });

        manageSellerList.processAllUsers();
    }

    private void setDataLimitEnabled(boolean value) {
        mBinding.range.setEnabled(value);
        // mBinding.fromDate.setEnabled(value);
        mBinding.toDate.setEnabled(value);
    }


    private String getKey(int prev, int cur) {
        return prev + "" + cur;
    }

    // Mesh user part ++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void setMeshUserInfo() {
        resetAllView();
        mBinding.meshUserLayout.setVisibility(View.VISIBLE);
    }

    /**
     * To set visibility gone for all views
     */
    private void resetAllView() {
        mBinding.meshUserLayout.setVisibility(View.GONE);
        mBinding.dataSellLayout.setVisibility(View.GONE);
        mBinding.dataBuyLayout.setVisibility(View.GONE);
        mBinding.internetUserLayout.setVisibility(View.GONE);
    }

    // Data sell part ++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void setDataSellInfo() {
        resetAllView();
        mBinding.dataSellLayout.setVisibility(View.VISIBLE);
    }

    // Data buy part ++++++++++++++++++++++++++++++++++++++++++++++++++++
    private void setDataBuyInfo() {
        resetAllView();
        mBinding.dataBuyLayout.setVisibility(View.VISIBLE);
    }


    private void dataLimitControl() {
        mBinding.limitControl.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
 /*               case R.id.unlimited:
                  //  unlimitedDataPlan();
                    break;

                case R.id.limit_to:
                  //  limitDataPlan();
                    break;*/
            }
        });
    }

    private void setBuyerDataInfo() {
        dataPlanViewModel.getBuyerUsers.observe(this, buyerUsers -> {
            if (getAdapter() != null) {
                getAdapter().addItem(buyerUsers);
            }
        });

        dataPlanViewModel.getBuyerList();
    }


    private DataPlanViewModel getViewModel() {
        return ViewModelProviders.of(this).get(DataPlanViewModel.class);
    }

    private SellerListAdapter getAdapter() {
        return (SellerListAdapter) mBinding.dataSellerList.getAdapter();
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


        DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };


        mBinding.toDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDateBox = mBinding.toDate;
                if (dataLimitModel.getToDate() > myCalendar.getTimeInMillis() - 1000) {
                    myCalendar.setTimeInMillis(dataLimitModel.getToDate());
                }

                DatePickerDialog toDatePickerDialog = new DatePickerDialog(DataPlanActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH));
                toDatePickerDialog.getDatePicker().setMinDate(dataLimitModel.getFromDate() > System.currentTimeMillis() ? dataLimitModel.getFromDate() : System.currentTimeMillis());
                toDatePickerDialog.show();
            }
        });
    }


    private void checkSharingLimit() {

        if (mBinding.limitTo.isChecked()) {

            if (timeHelper == null) {
                timeHelper = new TimeHelper();
            }

            long from = 0l, to = 0l;
            long tempSharedData = Util.convertMegabytesToBytes(Integer.valueOf(mBinding.range.getText().toString()));
            try {
                from = timeHelper.getDayWiseTimeStamp(sdf.parse(mBinding.fromDate.getText().toString()).getTime());
                to = timeHelper.getDayWiseTimeStamp(sdf.parse(mBinding.toDate.getText().toString()).getTime()) + (24 * 60 * 60 * 1000 - 1000);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (to <= System.currentTimeMillis()) {
                mBinding.dateError.setVisibility(View.VISIBLE);
                mBinding.dateError.setText(getString(R.string.date_expired));

       /*         if (dataSharingManager.isProxyOn()) {
                    dataSharingManager.offRole(PreferencesHelperPaylib.DATA_SELLER, true);
                }*/
            } else {
                mBinding.dateError.setVisibility(View.INVISIBLE);
                long usedData = 0;
                try {
                    usedData = DatabaseService.getInstance(this).getDataUsageByDate(from, to);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                //long sharedData = preferencesHelperDataplan.getSellDataAmount();

                if (tempSharedData <= usedData) {
                    mBinding.dataLimitError.setVisibility(View.VISIBLE);
                    mBinding.dataLimitError.setText(this.getString(R.string.data_lomit_larger_needed));
           /*         if (dataSharingManager.isProxyOn()) {
                        dataSharingManager.offRole(PreferencesHelperPaylib.DATA_SELLER, true);
                    }*/

                } else {
                    mBinding.dataLimitError.setVisibility(View.INVISIBLE);
                    dataLimitModel.setFromDate(from);
                    dataLimitModel.setToDate(to);
                    dataLimitModel.setSharedData(tempSharedData);
                    //  dataSharingManager.onRole(PreferencesHelperPaylib.DATA_SELLER, true);

                    dataLimitModel.setDataLimited(true);

                    if (dataLimitModel != null
                            && dataLimitModel.getUsedData().getValue() != null
                            && dataLimitModel.getSharedData().getValue() != null) {

                        if (dataLimitModel.getUsedData().getValue() >= (dataLimitModel.getSharedData().getValue() - PurchaseConstants.SELLER_MINIMUM_WARNING_DATA)) {
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


    private void updateLabel() {
        currentDateBox.setText(sdf.format(myCalendar.getTime()));
        mBinding.fromDate.setText(sdf.format(System.currentTimeMillis()));

        enableSaveButton();
    }

    private String getOsName() {
        Field[] fields = Build.VERSION_CODES.class.getFields();
        String osName = fields[Build.VERSION.SDK_INT + 1].getName();
        if (osName.equals("O")) osName = "Oreo";
        if (osName.equals("N")) osName = "Nougat";
        if (osName.equals("M")) osName = "Marshmallow";

        if (osName.startsWith("O_")) osName = "Oreo";
        if (osName.startsWith("N_")) osName = "Nougat";

        return osName;
    }

    @Override
    protected void onResume() {
        super.onResume();

//        boolean floatingViewVisible = FloatingView.on().isDownheadVisible();
//        if (!floatingViewVisible) {
//            openFloatingViewConfirmation();
//        }

        prepareSellerData();
//        currencyControl();
    }

    //TODO arif
    /*@Override
    public void onTransportInit(String nodeId, String publicKey, TransportState transportState, String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (PreferencesHelperPaylib.on().getDataShareMode() == PreferencesHelperPaylib.DATA_BUYER) {
                    PurchaseManagerBuyer.getInstance().setPurchaseManagerBuyerListener(DataPlanActivity.this);
                    PurchaseManagerSeller.getInstance().destroyObject();
                } else if (PreferencesHelperPaylib.on().getDataShareMode() == PreferencesHelperPaylib.DATA_SELLER) {
                    PurchaseManagerBuyer.getInstance().setPurchaseManagerBuyerListener(null);
                    PurchaseManagerBuyer.getInstance().destroyObject();
                }
            }
        });
    }*/

    public void onButtonClickListener(Seller item) {
        String btnText = item.getBtnText();
        if (btnText.toLowerCase().equals(PurchaseConstants.SELLERS_BTN_TEXT.CLOSE.toLowerCase())) {
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
        bodyText = (TextView) promptsView.findViewById(R.id.bodyText);
        stopAndRefund = (TextView) promptsView.findViewById(R.id.tv_stop_and_refund);

        cancel = (TextView) promptsView.findViewById(R.id.tv_cancel);

        bodyText.setText("You have already used up " + seller.getUsedData() + "MB of " + seller.getPurchasedData() + "MB. Are you sure to stop data usage now?");

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        stopAndRefund.setOnClickListener(v -> {
            PurchaseManagerBuyer.getInstance().closePurchase(seller.getId());
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
                        PurchaseManagerBuyer.getInstance().buyData(amount, seller.getId());
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

//    @Override
//    public void onHotspotQuit() {
////        FloatingView.on().setOnHotspotQuitListener(this);
//        //openFloatingViewConfirmation();
//    }
    @Override
    public void onConnectingWithSeller(String sellerAddress) {
        runOnUiThread(() -> {
            for (Seller item : getAdapter().getItems()) {
                if (item.getId().equalsIgnoreCase(sellerAddress)) {
                    item.setBtnText(PurchaseConstants.SELLERS_BTN_TEXT.PURCHASING);
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
                    item.setBtnText(PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);
                    item.setBtnEnabled(true);
                    getAdapter().addItem(item);
                    ToastUtil.showLong(DataPlanActivity.this, msg);
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
                    item.setBtnText(PurchaseConstants.SELLERS_BTN_TEXT.CLOSE);
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
                    item.setBtnText(PurchaseConstants.SELLERS_BTN_TEXT.CLOSING);
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
                    item.setBtnText(PurchaseConstants.SELLERS_BTN_TEXT.CLOSE);
                    item.setBtnEnabled(true);
                    getAdapter().addItem(item);
                    ToastUtil.showLong(DataPlanActivity.this, msg);
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
                    item.setBtnText(PurchaseConstants.SELLERS_BTN_TEXT.TOP_UP);
                    item.setBtnEnabled(true);
                    getAdapter().addItem(item);
                    ToastUtil.showLong(DataPlanActivity.this, msg);
                    return;
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
                    item.setBtnText(PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);
                    item.setBtnEnabled(true);
                    getAdapter().addItem(item);
                    break;
                }
            }
        });
    }

    @Override
    public void showToastMessage(String msg) {
        runOnUiThread(() -> ToastUtil.showLong(DataPlanActivity.this, msg));
    }

    @Override
    public void onBalancedFinished(String sellerAddress, int remain) {
        runOnUiThread(() -> {
            if (remain == 1) {
                for (Seller item : getAdapter().getItems()) {
                    if (item.getId().equalsIgnoreCase(sellerAddress)) {
                        item.setBtnText(PurchaseConstants.SELLERS_BTN_TEXT.TOP_UP);
                        item.setBtnEnabled(true);
                        getAdapter().addItem(item);
                        break;
                    }
                }
            } else {
                for (Seller item : getAdapter().getItems()) {
                    if (item.getId().equalsIgnoreCase(sellerAddress)) {
                        item.setBtnText(PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);
                        item.setBtnEnabled(true);
                        getAdapter().addItem(item);
                        break;
                    }
                }
            }
        });
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

    private void showSellerWarningDialog(int activeBuyer) {
        long sharedData = PreferencesHelperDataplan.on().getSellDataAmount();
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
                        // All active channel will be closed
                        PurchaseManagerSeller.getInstance().closeAllActiveBuyerChannel();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        // Unregister since the activity is about to be closed.

        super.onDestroy();
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
}