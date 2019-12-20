package com.w3engineers.mesh.application.ui.wallet;

import android.app.ProgressDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.w3engineers.ext.strom.util.helper.Toaster;
import com.w3engineers.mesh.R;
import com.w3engineers.mesh.application.data.BaseServiceLocator;
import com.w3engineers.mesh.application.data.local.DataPlanConstants;
import com.w3engineers.mesh.application.data.local.dataplan.DataPlanManager;
import com.w3engineers.mesh.application.data.local.wallet.WalletManager;
import com.w3engineers.mesh.application.ui.base.TelemeshBaseActivity;
import com.w3engineers.mesh.application.ui.dataplan.TestDataPlanActivity;
import com.w3engineers.mesh.application.ui.tokenguide.PointGuidelineActivity;
import com.w3engineers.mesh.databinding.ActivityWalletBinding;
import com.w3engineers.mesh.util.DialogUtil;
import com.w3engineers.mesh.util.MeshLog;
import com.w3engineers.mesh.util.lib.mesh.HandlerUtil;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;


public class WalletActivity extends TelemeshBaseActivity implements WalletManager.WalletListener {

    private ActivityWalletBinding mBinding;
    private ProgressDialog dialog;
    private WalletViewModel walletViewModel;
    private double payableDeposit;

    private LiveData<Double> totalEarningObserver, totalSpentObserver, totalPendingEarningObserver;
    private LiveData<Integer> haveDifferentNetworkDataObserver;
    private Runnable dialogTimerRunnable;

    private WalletManager walletManager;
    private DataPlanManager dataPlanManager;

    private final String ropstenUrl = "https://ropsten.etherscan.io/address/";
    private final String kottyUrl = "https://explorer.eth.events/ethereum/ethereum/kotti/address/";
    private byte[] picture;
    private final double minimumWithdrawAmount = 10.0;

    /*private interface REQUEST_TYPE {
        int ETHER = 1;
        int TOKEN = 2;
    }*/


    @Override
    protected int getLayoutId() {
        return R.layout.activity_wallet;
    }

    @Override
    protected BaseServiceLocator getServiceLocator() {
        return null;
    }

    @Override
    protected int statusBarColor() {
        return R.color.colorPrimaryDark;
    }


    @Override
    protected void startUI() {

        mBinding = (ActivityWalletBinding) getViewDataBinding();
        Intent intent = getIntent();
        if (intent.hasExtra("picture")) {
            picture = intent.getByteArrayExtra("picture");
        }


        walletViewModel = getWalletViewModel();
        walletManager = WalletManager.getInstance();
        dataPlanManager = DataPlanManager.getInstance();

        walletManager.setWalletListener(this);
        mBinding.buttonViewTransaction.setOnClickListener(this);
        mBinding.tvBalanceLastUpdated.setOnClickListener(this);

        setClickListener(mBinding.opBack, mBinding.imgMyAddress, mBinding.tmeshBlock, mBinding.imgRefresh);

        setCurrencyAndTokenObserver();

        dialog = new ProgressDialog(WalletActivity.this);

        if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_BUYER) {
            //  mBinding.totalSpentBlock.setVisibility(View.GONE);
        }

        boolean giftEther = walletManager.giftEther();

        setDialogLoadingTimer("Refreshing balance, please wait.");

        if (!giftEther) {
            refreshMyBalance();
        }

        setTotalEarn();
        setTotalSpent();
        getTotalPendingEarningBySeller();
        setDifferentNetworkInfo();

        //changeStatusBarColor();

        mBinding.pullToRefresh.setOnRefreshListener(() -> {
            runOnUiThread(() -> {
                refreshBalance();
            });
        });

        //  mBinding.currency.setOnClickListener(this);
    }


    private void refreshBalance() {
        refreshMyBalance();
        setLastUpdated();
        mBinding.pullToRefresh.setRefreshing(false);
    }

    public static void openActivity(Context context) {
        Intent intent = new Intent(context, TestDataPlanActivity.class);
        context.startActivity(intent);
    }


    private String convertTwoDigitString(double value) {
        String result = String.format("%.2f", value);
        return result;
    }

    private String convertSixrDigitString(double value) {
        String result = String.format("%.6f", value);
        return result;
    }

    @Override
    public void onGiftResponse(boolean success, boolean isGifted, String message) {

        runOnUiThread(() -> {

            resetDialogLoadingTimer();

            if (success) {

                if (isGifted) {
                    DialogUtil.showConfirmationDialog(WalletActivity.this, "Gift Awarded!", message,
                            null, "OK", null);
                } else {
                    DialogUtil.showConfirmationDialog(WalletActivity.this, "Gift Awarded!", message,
                            null, "OK", null);
                }

            } else {
                if (isGifted) {

                } else {

                    String failedMessage = "Sorry, due to some reasons you are not eligible to be awarded.";
                    MeshLog.e(message);

                    DialogUtil.showConfirmationDialog(WalletActivity.this, "Gift Award Failed!", failedMessage,
                            null, "OK", null);
                }
            }
        });
    }

    @Override
    public void onBalanceInfo(boolean success, String msg) {
        runOnUiThread(() -> {
            resetDialogLoadingTimer();

            //    mBinding.btnWithdraw.setEnabled(true);
            Toaster.showLong(msg);
        });
    }

    @Override
    public void onEtherRequestResponse(boolean success, String msg) {
        runOnUiThread(() -> {
            resetDialogLoadingTimer();

            if (success) {
                WalletActivity.this.refreshMyBalance();
            }
            Toaster.showLong(msg);
        });
    }

    @Override
    public void onTokenRequestResponse(boolean success, String msg) {
        runOnUiThread(() -> {
            resetDialogLoadingTimer();

            Toaster.showLong(msg);
        });
    }

    @Override
    public void onRequestSubmitted(boolean success, String msg) {
        runOnUiThread(() -> {
            if (!success) {
                //   mBinding.btnWithdraw.setEnabled(true);
            }
            Toaster.showLong(msg);
        });
    }

    @Override
    public void onRequestCompleted(boolean success, String msg) {
        runOnUiThread(() -> {
            if (!success) {
                // mBinding.btnWithdraw.setEnabled(true);
            }
            Toaster.showLong(msg);
            refreshMyBalance();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        if (v.getId() == R.id.op_back) {
            finish();
        } else if (v.getId() == R.id.img_my_address) {
            BottomSheetFragment bottomSheetFragment = new BottomSheetFragment(walletManager.getMyAddress(), picture);
            bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());

        } else if (v.getId() == R.id.btn_withdraw) {
            /*if (payableDeposit <= 0) {
                DialogUtil.showConfirmationDialog(this, "No payable deposit",
                        "You don't have any payable deposit", null,
                        "OK", null);
            } else {
                showAlertWithdraw();
            }*/
        } else if (v.getId() == R.id.button_view_transaction) {
            openUrl();
        } else if (v.getId() == R.id.img_refresh) {
            refreshBalance();
        } else if (v.getId() == R.id.tv_balance_last_updated) {
            //startActivity(new Intent(WalletActivity.this, PointGuidelineActivity.class));
        }
    }


    private void openUrl() {
        int myRole = dataPlanManager.getDataPlanRole();

        if (myRole == DataPlanConstants.USER_ROLE.DATA_SELLER || myRole == DataPlanConstants.USER_ROLE.INTERNET_USER) {

            int networkType = walletManager.getMyEndpoint();
            String networkUrl = null;
            if (networkType == 1) { // ETH
                networkUrl = ropstenUrl + walletManager.getMyAddress();
            } else if (networkType == 2) { // ETC
                networkUrl = kottyUrl + walletManager.getMyAddress();
            }
            if (networkUrl != null) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(networkUrl));
                browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(browserIntent);
            } else {
                Toaster.showShort("No network selected");
            }
        } else {
            Toaster.showShort("This feature only available for Seller and Internet user");
        }
    }


    /*private void showRequestAlert(String title, String msg, int type) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(msg);

        alertDialogBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        if (type == REQUEST_TYPE.ETHER) {
                            sendEtherRequest();
                        } else {
                            sendTokenRequest();
                        }
                    }
                });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }*/

   /* private void showAlertWithdraw() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View promptView = inflater.inflate(R.layout.prompt_wallet_withdrow, null);
        PromptWalletWithdrowBinding promptBinding = PromptWalletWithdrowBinding.bind(promptView);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptView);
        AlertDialog alertDialog = alertDialogBuilder.create();
        String token = " " + getString(R.string.token);
        SpannableStringBuilder ssBuilder = new SpannableStringBuilder(token);

        ssBuilder.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorGradientPrimary)),
                token.indexOf(token), token.indexOf(token) + token.length(),
                Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        promptBinding.tvRmeshBalance.append(ssBuilder);
        promptBinding.tvRmeshBalance.setMovementMethod(LinkMovementMethod.getInstance());

        alertDialog.setCancelable(false);

        promptBinding.tvEthCurrency.setText(walletManager.getCurrencyTypeMessage("%s"));
        promptBinding.tvEthCurrency2.setText(walletManager.getCurrencyTypeMessage("%s"));
        promptBinding.tvEthCurrency3.setText(walletManager.getCurrencyTypeMessage("%s"));


        deselectWithdrawOptions(promptBinding.layoutSlow,
                promptBinding.layoutAvg, promptBinding.layoutFast);

        promptBinding.btnCancel.setOnClickListener(v -> {
            //     mBinding.btnWithdraw.setEnabled(true);
            alertDialog.cancel();
        });

        promptBinding.btnWithdraw.setOnClickListener(v -> {
            //   mBinding.btnWithdraw.setEnabled(false);
            performWithdrawBalance();
            alertDialog.cancel();
        });

        promptBinding.layoutSlow.setOnClickListener(v -> {
            deselectWithdrawOptions(promptBinding.layoutSlow, promptBinding.layoutAvg, promptBinding.layoutFast);
            selectWithdrawOption(promptBinding.layoutSlow);
        });

        promptBinding.layoutAvg.setOnClickListener(v -> {
            deselectWithdrawOptions(promptBinding.layoutSlow, promptBinding.layoutAvg, promptBinding.layoutFast);
            selectWithdrawOption(promptBinding.layoutAvg);
        });

        promptBinding.layoutFast.setOnClickListener(v -> {
            deselectWithdrawOptions(promptBinding.layoutSlow, promptBinding.layoutAvg, promptBinding.layoutFast);
            selectWithdrawOption(promptBinding.layoutFast);
        });

        alertDialog.show();
    }*/

   /* private void selectWithdrawOption(ConstraintLayout view) {
        view.setBackground(getResources().getDrawable(R.drawable.bg_withdraw_choice));
    }

    private void deselectWithdrawOptions(ConstraintLayout layoutSlow, ConstraintLayout layoutAvg,
                                         ConstraintLayout layoutFast) {
        layoutSlow.setBackground(getResources().getDrawable(R.drawable.bg_withdraw_choice_empty));
        layoutAvg.setBackground(getResources().getDrawable(R.drawable.bg_withdraw_choice_empty));
        layoutFast.setBackground(getResources().getDrawable(R.drawable.bg_withdraw_choice_empty));
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
    }*/

    private void refreshMyBalance() {

        setDialogLoadingTimer("Refreshing balance, please wait.");

        walletManager.refreshMyBalance();
    }

    private void sendEtherRequest() {
        setDialogLoadingTimer("Sending request, please wait.");

        walletManager.sendEtherRequest();
    }

    private void sendTokenRequest() {
        setDialogLoadingTimer("Sending request, please wait.");

        walletManager.sendTokenRequest();
    }

    private void setTotalEarn() {

        String dateTime = WalletPreference.on().read(WalletPreference.LATEST_UPDATE);
        if (!TextUtils.isEmpty(dateTime)) {
            //  mBinding.tvLastUpdated.setText(getString(R.string.txt_last_updated) + dateTime);
        }

        if (totalEarningObserver != null) {
            totalEarningObserver.removeObservers(this);
        }

        totalEarningObserver = walletViewModel.getTotalEarn();

        if (totalEarningObserver != null) {
            totalEarningObserver.observe(this, aDouble -> {
                mBinding.tvEarned.setText(aDouble == null ? "0.0" : convertSixrDigitString(aDouble));
            });
        }
    }

    public void setTotalSpent() {

        String dateTime = WalletPreference.on().read(WalletPreference.LATEST_UPDATE);
        if (!TextUtils.isEmpty(dateTime)) {
            //  mBinding.tvLastUpdated.setText(getString(R.string.txt_last_updated) + dateTime);
        }

        if (totalSpentObserver != null) {
            totalSpentObserver.removeObservers(this);
        }

        totalSpentObserver = walletViewModel.getTotalSpent();

        if (totalSpentObserver != null) {
            totalSpentObserver.observe(this, totalSpent -> {
                mBinding.tvSpent.setText(totalSpent == null ? "0.0" : convertSixrDigitString(totalSpent));
            });
        }
    }

    public void getTotalPendingEarningBySeller() {

        if (totalPendingEarningObserver != null) {
            totalPendingEarningObserver.removeObservers(this);
        }

        totalPendingEarningObserver = walletViewModel.getTotalPendingEarning();

        if (totalPendingEarningObserver != null) {
            totalPendingEarningObserver.observe(this, totalPendingEarn -> {
                if (totalPendingEarn != null) {
                    payableDeposit = totalPendingEarn;
                } else {
                    payableDeposit = 0;
                }

                mBinding.textViewPendingBalance.setText(totalPendingEarn == null ? "0" : convertSixrDigitString(totalPendingEarn));

                if (payableDeposit >= minimumWithdrawAmount) {
                    performWithdrawBalance();
                }
            });
        }
    }

    private void setDifferentNetworkInfo() {

        if (haveDifferentNetworkDataObserver != null) {
            haveDifferentNetworkDataObserver.removeObservers(this);
        }

        haveDifferentNetworkDataObserver = walletViewModel.getDifferentNetworkData(walletManager.getMyAddress());

        if (haveDifferentNetworkDataObserver != null) {
            haveDifferentNetworkDataObserver.observe(this, integer -> {

                if (integer != null && integer > 0) {
         /*           mBinding.anotherDeposit.setVisibility(View.VISIBLE);

                    if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_SELLER) {
                        mBinding.anotherDeposit.setText(getString(R.string.different_network_data_for_seller));
                    } else if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_BUYER) {
                        mBinding.anotherDeposit.setText(getString(R.string.different_network_data_for_buyer));
                    }*/

                } else {
                    //  mBinding.anotherDeposit.setVisibility(View.GONE);
                }
            });
        }
    }

    public void performWithdrawBalance() {
        walletManager.getAllOpenDrawableBlock();
    }

    public void setLastUpdated() {
        Date date = new Date();
        long timeMilli = date.getTime();
        date = new Date(timeMilli);
        Format format = new SimpleDateFormat("yyyy-MM-dd hh:mm aaa");
        String dateTime = format.format(date);
        //   mBinding.tvLastUpdated.setText(getString(R.string.txt_last_updated) + " " + dateTime);
        WalletPreference.on().write(WalletPreference.LATEST_UPDATE, dateTime);
    }

    private void setCurrencyAndTokenObserver() {
        walletViewModel.networkMutableLiveData.observe(this, walletInfo -> {
            Log.e("Wallet_info", "Wallet info received from ");
            if (walletInfo != null) {
                //mBinding.tvEthBalance.setText(convertTwoDigitString(walletInfo.currencyAmount));

                mBinding.textViewPointValue.setText(convertTwoDigitString(walletInfo.tokenAmount));

                int dataShareMode = dataPlanManager.getDataPlanRole();

                if (dataShareMode == DataPlanConstants.USER_ROLE.DATA_SELLER || dataShareMode == DataPlanConstants.USER_ROLE.DATA_BUYER) {

                    Intent intent = new Intent(WalletActivity.this, PointGuidelineActivity.class);
                    if (walletInfo.tokenAmount == 0) {
                        intent.putExtra(PointGuidelineActivity.class.getName(), true);
                        startActivity(intent);
                    } else if (walletInfo.currencyAmount == 0) {
                        intent.putExtra(PointGuidelineActivity.class.getName(), false);
                        startActivity(intent);
                    }
                }

                if (dataShareMode == DataPlanConstants.USER_ROLE.DATA_SELLER || dataShareMode == DataPlanConstants.USER_ROLE.DATA_BUYER) {

                    //    mBinding.currency.setText(walletInfo.currencySymbol);
                    //  mBinding.currency.setVisibility(View.VISIBLE);
                } else {
                    //  mBinding.currency.setVisibility(View.GONE);
                }

      /*          mBinding.titleEthCurrency.setText(walletInfo.currencySymbol);
                mBinding.titleRmeshCurrency.setText(walletInfo.tokenSymbol);*/

                //  mBinding.currency.setText(walletInfo.currencySymbol);
            } else {
                Log.e("Wallet_info", "Wallet info null received from ");
            }
        });

        walletViewModel.getCurrencyAmount();
    }


/*    private void openCurrencyPopup(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.menu_currency);

        MenuItem itemETC = popup.getMenu().findItem(R.id.action_etc);
        MenuItem itemETH = popup.getMenu().findItem(R.id.action_eth);

        int endPointType = walletManager.getMyEndpoint();

        itemETC.setCheckable(false);
        itemETH.setCheckable(false);

        if (endPointType == DataPlanConstants.END_POINT_TYPE.ETC_KOTTI) {
            itemETC.setCheckable(true);
            itemETC.setChecked(true);
        } else if (endPointType == DataPlanConstants.END_POINT_TYPE.ETH_ROPSTEN) {
            itemETH.setCheckable(true);
            itemETH.setChecked(true);
        }

        popup.setOnMenuItemClickListener(item -> {

            int endPoint = 0;
            if (item.getItemId() == R.id.action_etc) {
                endPoint = DataPlanConstants.END_POINT_TYPE.ETC_KOTTI;
            } else if (item.getItemId() == R.id.action_eth) {
                endPoint = DataPlanConstants.END_POINT_TYPE.ETH_ROPSTEN;
            }

            walletManager.setEndpoint(endPoint);

            walletViewModel.getCurrencyAmount();
            setTotalEarn();

            setTotalSpent();
            getTotalPendingEarningBySeller();
            setDifferentNetworkInfo();

            if (dataPlanManager.getDataPlanRole() == DataPlanConstants.USER_ROLE.DATA_BUYER) {
                mBinding.totalSpentBlock.setVisibility(View.GONE);
            }

            boolean giftEther = walletManager.giftEther();
            if (!giftEther) {
                refreshMyBalance();
            }
            return false;
        });
        popup.show();
    }*/

    private WalletViewModel getWalletViewModel() {
        return ViewModelProviders.of(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new WalletViewModel();
            }
        }).get(WalletViewModel.class);
    }

    private void setDialogLoadingTimer(String loaderMessage) {
        if (dialogTimerRunnable == null) {

            if (dialog != null && !dialog.isShowing()) {
                dialog.setMessage(loaderMessage);
                dialog.setCancelable(false);
                dialog.show();
            }

            dialogTimerRunnable = () -> runOnUiThread(() -> {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }

                Toaster.showLong("Request timeout.");
                dialogTimerRunnable = null;
            });

            HandlerUtil.postForeground(dialogTimerRunnable, 32000);
        }
    }

    private void resetDialogLoadingTimer() {

        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        if (dialogTimerRunnable != null) {
            HandlerUtil.removeForeGround(dialogTimerRunnable);
            dialogTimerRunnable = null;
        }
    }
}


