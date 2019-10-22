package com.w3engineers.mesh.application.ui.wallet;

import android.app.ProgressDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupMenu;

import com.w3engineers.eth.data.constant.PayLibConstant;
import com.w3engineers.eth.data.helper.PreferencesHelperPaylib;
import com.w3engineers.eth.data.remote.EthereumService;
import com.w3engineers.eth.util.helper.ToastUtil;
import com.w3engineers.ext.strom.application.ui.base.BaseActivity;
import com.w3engineers.ext.strom.util.helper.Toaster;
import com.w3engineers.mesh.R;
import com.w3engineers.mesh.application.data.local.db.SharedPref;
import com.w3engineers.mesh.application.data.local.helper.PreferencesHelperDataplan;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseManagerBuyer;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseManagerSeller;
import com.w3engineers.mesh.databinding.ActivityWalletBinding;
import com.w3engineers.mesh.databinding.PromptWalletWithdrowBinding;
import com.w3engineers.mesh.util.Constant;
import com.w3engineers.mesh.util.DialogUtil;
import com.w3engineers.mesh.util.EthereumServiceUtil;
import com.w3engineers.mesh.util.Util;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;


public class WalletActivity extends BaseActivity {

    private ActivityWalletBinding mBinding;
    private EthereumService ethService;
    private String walletAddress;
    //    double tokenBalance;
//    double ethBalance;
    private ProgressDialog dialog;
    private WalletViewModel walletViewModel;
    // private PurchaseManagerBuyer purchaseManagerBuyer;
    private PreferencesHelperDataplan preferencesHelperDataplan;
    private double payableDeposit;

    private LiveData<Double> totalEarningObserver, totalSpentObserver,
            totalPendingEarningObserver;
    private LiveData<Integer>  haveDifferentNetworkDataObserver;

    private interface REQUEST_TYPE {
        int ETHER = 1;
        int TOKEN = 2;
    }

    //private PurchaseManagerSeller purchaseManagerSeller;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_wallet;
    }

    @Override
    protected void startUI() {
        mBinding = (ActivityWalletBinding) getViewDataBinding();
        walletViewModel = getWalletViewModel();
        setClickListener(mBinding.opBack, mBinding.imgMyAddress, mBinding.btnWithdraw, mBinding.ethBlock, mBinding.tmeshBlock);
        // purchaseManagerSeller = PurchaseManagerSeller.getInstance(this);
        // purchaseManagerBuyer = PurchaseManagerBuyer.getInstance(this);
        preferencesHelperDataplan = PreferencesHelperDataplan.on();

        ethService = EthereumServiceUtil.getInstance(this).getEthereumService();
        walletAddress = ethService.getAddress();

        setCurrencyAndToken();

        dialog = new ProgressDialog(this);

        //For now we are refreshing balance every time we open the wallet --Major Arif 24/09/2019
//        if (tokenBalance == 0 || ethBalance == 0) {
        //refreshMyBalance();
//        }




        if (preferencesHelperDataplan.getDataShareMode() == preferencesHelperDataplan.DATA_BUYER) {
            mBinding.totalSpentBlock.setVisibility(View.GONE);

            if (!PurchaseManagerBuyer.getInstance().giftEtherForOtherNetwork()){
                refreshMyBalance();
            }
        } else if (preferencesHelperDataplan.getDataShareMode() == preferencesHelperDataplan.DATA_SELLER){
            if (!PurchaseManagerSeller.getInstance().requestForGiftForSeller()){
                refreshMyBalance();
            }
        }

        setTotalEarn();
        setTotalSpent();
        getTotalPendingEarningBySeller();
        setDifferentNetworkInfo();

        changeStatusBarColor();

        mBinding.pullToRefresh.setOnRefreshListener(() -> {
            runOnUiThread(() -> {
                refreshMyBalance();
                setLastUpdated();
                mBinding.pullToRefresh.setRefreshing(false);
            });
        });

        mBinding.currency.setOnClickListener(this);
    }

    private String convertTwoDigitString(double value) {
        String result = String.format("%.2f", value);
        return result;
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
            AddressLayout cdd = new AddressLayout(WalletActivity.this);
            cdd.show();
        } else if (v.getId() == R.id.btn_withdraw) {
            if (payableDeposit <= 0) {
                DialogUtil.showConfirmationDialog(this, "No payable deposit", "You don't have any payable deposit", null, "OK", null);
            } else {
                showAlertWithdraw();
            }
        } else if (v.getId() == R.id.eth_block) {
            showRequestAlert(Util.getCurrencyTypeMessage("%s Request"), Util.getCurrencyTypeMessage("Do you want to send a request for %s?"), REQUEST_TYPE.ETHER);
        } else if (v.getId() == R.id.tmesh_block) {
            showRequestAlert("Purchase Token", "Do you want to send a request for token?", REQUEST_TYPE.TOKEN);
        } else if (v.getId() == R.id.currency) {
            openCurrencyPopup(v);
        }
    }


    private void showRequestAlert(String title, String msg, int type) {

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
    }

    private void showAlertWithdraw() {
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

        promptBinding.tvEthCurrency.setText(Util.getCurrencyTypeMessage("%s"));
        promptBinding.tvEthCurrency2.setText(Util.getCurrencyTypeMessage("%s"));
        promptBinding.tvEthCurrency3.setText(Util.getCurrencyTypeMessage("%s"));


        deselectWithdrawOptions(promptBinding.layoutSlow,
                promptBinding.layoutAvg, promptBinding.layoutFast);

        promptBinding.btnCancel.setOnClickListener(v -> {
            mBinding.btnWithdraw.setEnabled(true);
            alertDialog.cancel();
        });

        promptBinding.btnWithdraw.setOnClickListener(v -> {
            mBinding.btnWithdraw.setEnabled(false);
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
    }

    private void selectWithdrawOption(ConstraintLayout view) {
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
    }

    private void refreshMyBalance() {
        PreferencesHelperDataplan preferencesHelperDataplan = PreferencesHelperDataplan.on();
        if (preferencesHelperDataplan.getDataShareMode() == PreferencesHelperDataplan.MESH_USER) {
            ToastUtil.showLong(this, "This feature is available only for data seller and data buyer.");
        } else if (preferencesHelperDataplan.getDataShareMode() == PreferencesHelperDataplan.DATA_SELLER) {
            dialog.setMessage("Refreshing balance, please wait.");
            dialog.setCancelable(false);
            dialog.show();
            PurchaseManagerSeller.getInstance().getMyBalanceInfo(new PurchaseManagerSeller.MyBalanceInfoListener() {
                @Override
                public void onBalanceInfoReceived(double ethBalance, double tknBalance) {
                    runOnUiThread(() -> {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        // Remove eth and token
                        mBinding.btnWithdraw.setEnabled(true);
                    });
                }

                @Override
                public void onBalanceErrorReceived(String msg) {
                    runOnUiThread(() -> {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        mBinding.btnWithdraw.setEnabled(true);
                        ToastUtil.showLong(WalletActivity.this, msg);
                    });
                }
            });
        } else {
            dialog.setMessage("Refreshing balance, please wait.");
            dialog.setCancelable(false);
            dialog.show();

            PurchaseManagerBuyer.getInstance().getMyBalanceInfo(new PurchaseManagerBuyer.MyBalanceInfoListener() {
                @Override
                public void onBalanceInfoReceived(double ethBalance, double tknBalance) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            // Remove eth and token
                        }
                    });
                }

                @Override
                public void onBalanceErrorReceived(String msg) {
                    runOnUiThread(() -> {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        ToastUtil.showLong(WalletActivity.this, msg);
                    });
                }
            });
        }
    }

    private void sendEtherRequest() {
        PreferencesHelperDataplan preferencesHelperDataplan = PreferencesHelperDataplan.on();

        if (preferencesHelperDataplan.getDataShareMode() == PreferencesHelperDataplan.MESH_USER) {
            ToastUtil.showLong(this, "This feature is available only for data seller and data buyer.");
        } else if (preferencesHelperDataplan.getDataShareMode() == PreferencesHelperDataplan.DATA_SELLER) {

            dialog.setMessage("Sending request, please wait.");
            dialog.setCancelable(false);
            dialog.show();

            PurchaseManagerSeller.getInstance().sendEtherRequest((success, msg) -> runOnUiThread(() -> {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                if (success) {
                    refreshMyBalance();
                }
                ToastUtil.showLong(WalletActivity.this, msg);

            }));
        } else {
            dialog.setMessage("Sending request, please wait.");
            dialog.show();
            PurchaseManagerBuyer.getInstance().sendEtherRequest(new PurchaseManagerBuyer.EtherRequestListener() {
                @Override
                public void onResponseReceived(boolean success, String from, String msg) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            if (success) {
                                refreshMyBalance();
                            }
                            ToastUtil.showLong(WalletActivity.this, msg);

                        }
                    });
                }
            });
        }
    }

    private void sendTokenRequest() {
        PreferencesHelperDataplan preferencesHelperDataplan = PreferencesHelperDataplan.on();

        if (preferencesHelperDataplan.getDataShareMode() == PreferencesHelperDataplan.MESH_USER) {
            ToastUtil.showLong(this, "This feature is available only for data seller and data buyer.");
        } else if (preferencesHelperDataplan.getDataShareMode() == PreferencesHelperDataplan.DATA_SELLER) {

            dialog.setMessage("Sending request, please wait.");
            dialog.show();

            PurchaseManagerSeller.getInstance().sendTokenRequest(new PurchaseManagerSeller.TokenRequestListener() {
                @Override
                public void onTokenRequestResponseReceived(boolean success, String msg, double tokenValue, double etherValue) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            // Remove eth and token
                            ToastUtil.showLong(WalletActivity.this, msg);
                        }
                    });
                }
            });
        } else {
            dialog.setMessage("Sending request, please wait.");
            dialog.show();
            PurchaseManagerBuyer.getInstance().sendTokenRequest(new PurchaseManagerBuyer.TokenRequestListener() {
                @Override
                public void onTokenRequestResponseReceived(boolean success, String from, String msg, double tokenValue, double ethValue) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            // Remove eth and token
                            ToastUtil.showLong(WalletActivity.this, msg);
                        }
                    });
                }

                ;
            });
        }
    }

    private void setTotalEarn() {

        String dateTime = SharedPref.read(Constant.LATEST_UPDATE);
        if (!TextUtils.isEmpty(dateTime)) {
            mBinding.tvLastUpdated.setText(getString(R.string.txt_last_updated) + dateTime);
        }

        if (totalEarningObserver != null) {
            totalEarningObserver.removeObservers(this);
        }

        totalEarningObserver = walletViewModel.getTotalEarn(ethService.getAddress());

        if (totalEarningObserver != null) {
            totalEarningObserver.observe(this, aDouble -> {
                mBinding.tvEarned.setText(aDouble == null ? "0" : aDouble.toString());
            });
        }
    }

    public void setTotalSpent() {

        if (totalSpentObserver != null) {
            totalSpentObserver.removeObservers(this);
        }

        totalSpentObserver = walletViewModel.getTotalSpent(ethService.getAddress());

        if (totalSpentObserver != null) {
            totalSpentObserver.observe(this, totalSpent -> {
                mBinding.tvSpent.setText(totalSpent == null ? "0" : totalSpent.toString());
            });
        }
    }

    public void getTotalOpenChannel() {
        try {
            PurchaseManagerSeller.getInstance().getTotalOpenChannel().observe(this, totalOpenChannel -> {
                //mBinding.textViewSpent.setText(String.format(getResources().getString(R.string.total_spent), totalOpenChannel == null ? "0" : totalOpenChannel.toString()));
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getTotalPendingEarningBySeller() {

        if (totalPendingEarningObserver != null) {
            totalPendingEarningObserver.removeObservers(this);
        }

        totalPendingEarningObserver = walletViewModel.getTotalPendingEarning(ethService.getAddress());

        if (totalPendingEarningObserver != null) {
            totalPendingEarningObserver.observe(this, totalPendingEarn -> {
                if (totalPendingEarn != null) {
                    payableDeposit = totalPendingEarn;
                } else {
                    payableDeposit = 0;
                }
                mBinding.tvPendingPayableDeposit.setText(totalPendingEarn == null ? "0" : totalPendingEarn.toString());
            });
        }
    }

    private void setDifferentNetworkInfo() {

        if (haveDifferentNetworkDataObserver != null) {
            haveDifferentNetworkDataObserver.removeObservers(this);
        }

        haveDifferentNetworkDataObserver = walletViewModel.getDifferentNetworkData(ethService.getAddress());

        if ( haveDifferentNetworkDataObserver != null) {
            haveDifferentNetworkDataObserver.observe(this, integer -> {

                if (integer != null && integer > 0) {
                    mBinding.anotherDeposit.setVisibility(View.VISIBLE);

                    if (PreferencesHelperDataplan.on().getDataShareMode() == PreferencesHelperDataplan.DATA_SELLER) {
                        mBinding.anotherDeposit.setText(getString(R.string.different_network_data_for_seller));
                    } else if (PreferencesHelperDataplan.on().getDataShareMode() == PreferencesHelperDataplan.DATA_BUYER) {
                        mBinding.anotherDeposit.setText(getString(R.string.different_network_data_for_buyer));
                    }

                } else {
                    mBinding.anotherDeposit.setVisibility(View.GONE);
                }
            });
        }
    }

    public void performWithdrawBalance() {
        try {
            PurchaseManagerSeller.getInstance().getAllOpenDrawableBlock(new PurchaseManagerSeller.BalanceWithdrawtListener() {
                @Override
                public void onRequestSubmitted(boolean success, String msg) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!success) {
                                mBinding.btnWithdraw.setEnabled(true);
                            }
                            Toaster.showLong(msg);
                        }
                    });
                }

                @Override
                public void onRequestCompleted(boolean success, String msg) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!success) {
                                mBinding.btnWithdraw.setEnabled(true);
                            }
                            Toaster.showLong(msg);
                            refreshMyBalance();
                        }
                    });
                }
            });
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setLastUpdated() {
        Date date = new Date();
        long timeMilli = date.getTime();
        date = new Date(timeMilli);
        Format format = new SimpleDateFormat("yyyy-MM-dd hh:mm aaa");
        String dateTime = format.format(date);
        mBinding.tvLastUpdated.setText(getString(R.string.txt_last_updated) + " " + dateTime);
        SharedPref.write(Constant.LATEST_UPDATE, dateTime);
    }

    private void setCurrencyAndToken() {
        walletViewModel.networkMutableLiveData.observe(this, networkInfo -> {
            if (networkInfo != null) {
                mBinding.tvEthBalance.setText(convertTwoDigitString(networkInfo.currencyAmount));
                mBinding.tvRmeshBalance.setText(convertTwoDigitString(networkInfo.tokenAmount));

                int dataShareMode = preferencesHelperDataplan.getDataShareMode();

                if (dataShareMode == PreferencesHelperDataplan.DATA_SELLER || dataShareMode == PreferencesHelperDataplan.DATA_BUYER) {

                    mBinding.currency.setText(networkInfo.currencySymbol);
                    mBinding.currency.setVisibility(View.VISIBLE);
                } else {
                    mBinding.currency.setVisibility(View.GONE);
                }

                mBinding.titleEthCurrency.setText(networkInfo.currencySymbol);
                mBinding.titleRmeshCurrency.setText(networkInfo.tokenSymbol);

                mBinding.currency.setText(networkInfo.currencySymbol);
            }
        });

        walletViewModel.getCurrencyAmount();
    }

    private void openCurrencyPopup(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.menu_currency);

        MenuItem itemETC = popup.getMenu().findItem(R.id.action_etc);
        MenuItem itemETH = popup.getMenu().findItem(R.id.action_eth);

        int endPointType = PreferencesHelperPaylib.onInstance(this).getEndpointMode();

        itemETC.setCheckable(false);
        itemETH.setCheckable(false);

        if (endPointType == PayLibConstant.END_POINT_TYPE.ETC_KOTTI) {
            itemETC.setCheckable(true);
            itemETC.setChecked(true);
        } else if (endPointType == PayLibConstant.END_POINT_TYPE.ETH_ROPSTEN) {
            itemETH.setCheckable(true);
            itemETH.setChecked(true);
        }

        popup.setOnMenuItemClickListener(item -> {

            int endPoint = 0;
            if (item.getItemId() == R.id.action_etc) {
                endPoint = PayLibConstant.END_POINT_TYPE.ETC_KOTTI;
            } else if (item.getItemId() == R.id.action_eth) {
                endPoint = PayLibConstant.END_POINT_TYPE.ETH_ROPSTEN;
            }

            PreferencesHelperPaylib.onInstance(this).setEndPointMode(endPoint);

            walletViewModel.getCurrencyAmount();
            setTotalEarn();

            setTotalSpent();
            getTotalPendingEarningBySeller();
            setDifferentNetworkInfo();

            if (preferencesHelperDataplan.getDataShareMode() == preferencesHelperDataplan.DATA_BUYER) {
                mBinding.totalSpentBlock.setVisibility(View.GONE);

                if (!PurchaseManagerBuyer.getInstance().giftEtherForOtherNetwork()){
                    refreshMyBalance();
                }
            } else if (preferencesHelperDataplan.getDataShareMode() == preferencesHelperDataplan.DATA_SELLER){
                if (!PurchaseManagerSeller.getInstance().requestForGiftForSeller()){
                    refreshMyBalance();
                }
            }
            return false;
        });
        popup.show();
    }

    private WalletViewModel getWalletViewModel() {
        return ViewModelProviders.of(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T) new WalletViewModel();
            }
        }).get(WalletViewModel.class);
    }
}


