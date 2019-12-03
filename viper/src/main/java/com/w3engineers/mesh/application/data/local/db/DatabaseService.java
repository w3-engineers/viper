package com.w3engineers.mesh.application.data.local.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.util.Log;

import com.w3engineers.mesh.application.data.local.db.buyerpendingmessage.BuyerPendingMessage;
import com.w3engineers.mesh.application.data.local.db.datausage.Datausage;
import com.w3engineers.mesh.application.data.local.db.datausage.DatausageDao;
import com.w3engineers.mesh.application.data.local.db.message.Message;
import com.w3engineers.mesh.application.data.local.db.meta.TableMeta;
import com.w3engineers.mesh.application.data.local.db.networkinfo.NetworkInfo;
import com.w3engineers.mesh.application.data.local.db.purchase.Purchase;
import com.w3engineers.mesh.application.data.local.db.purchase.PurchaseDao;
import com.w3engineers.mesh.application.data.local.db.purchaserequests.PurchaseRequests;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseConstants;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import io.reactivex.Flowable;

public class DatabaseService {
    private static DatabaseService databaseService;
    private ExecutorService executor;
    private AppDatabase db;
    private Context context;


    DatabaseService(Context context) {
        this.context = context;
        executor = Executors.newFixedThreadPool(1);
        db = Room.databaseBuilder(this.context, AppDatabase.class, TableMeta.DB_NAME).fallbackToDestructiveMigration().build();
    }

    public static DatabaseService getInstance(Context context) {
        if (databaseService == null) {
            databaseService = new DatabaseService(context);
        }
        return databaseService;
    }

    public int insertPurchase(String buyerAddress, String sellerAddress, double totalData, double usedData, long blockNumber,
                              double deposit, String BPS, double balance, String closingHash, double withdrawnBalance,
                              int channelState, int endPointType)
            throws ExecutionException, InterruptedException {
        Future<Integer> future = executor.submit(new Callable() {
            @Override
            public Integer call() {
                long[] pid = null;
                try {

                    Purchase purchase = new Purchase();
                    long currentTime = System.currentTimeMillis();


                    purchase.buyerAddress = buyerAddress;
                    purchase.sellerAddress = sellerAddress;
                    purchase.totalDataAmount = totalData;
                    purchase.usedDataAmount = usedData;

                    purchase.openBlockNumber = blockNumber;
                    purchase.deposit = deposit;

                    purchase.balanceProof = BPS;
                    purchase.balance = balance;
                    purchase.closingHash = closingHash;


                    purchase.createTime = currentTime;
                    purchase.updateTime = currentTime;
                    purchase.blockChainEndpoint = endPointType;

                    purchase.withdrawnBalance = withdrawnBalance;
                    purchase.state = channelState;

                    pid = db.purchaseDao().insertAll(purchase);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return (int) pid[0];
            }
        });
        return future.get().intValue();
    }

    public List<Purchase> getMyPurchasesWithState(String muAddress, int state) throws ExecutionException, InterruptedException {
        Future<List<Purchase>> future = executor.submit(new Callable<List<Purchase>>() {
            @Override
            public List<Purchase> call() throws Exception {
                List<Purchase> purchaseList = null;
                try {
                    purchaseList = db.purchaseDao().getMyPurchasesWithState(state, muAddress);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return purchaseList;
            }
        });
        return future.get();
    }

    public List<Purchase> getMyActivePurchases(String myAddress) throws ExecutionException, InterruptedException {
        Future<List<Purchase>> future = executor.submit(() -> {
            List<Purchase> purchaseList = null;
            try {
                purchaseList = db.purchaseDao().getMyActivePurchases(myAddress);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return purchaseList;
        });
        return future.get();
    }


    public int getTotalNumberOfActiveBuyer(String myAddress, int channelStatus) throws ExecutionException, InterruptedException{
        Future<Integer> future = executor.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int totalOpenChannel = 0;
                try {
                    totalOpenChannel = db.purchaseDao().getTotalNumberOfActiveBuyer(myAddress, channelStatus);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return totalOpenChannel;
            }
        });

        return future.get();
    }

//    public List<Purchase> getMyOpenPurchases(String address, int state) throws ExecutionException, InterruptedException {
//
//        Future<List<Purchase>> future = executor.submit(new Callable() {
//            @Override
//            public List<Purchase> call() {
//                List<Purchase> purchaselist = null;
//                try {
//                    purchaselist = db.purchaseDao().gettMyOpenPurchases(address, state);
//                } catch (Exception e) {
//                    Log.e("error", e.toString());
//                }
//                return purchaselist;
//            }
//        });
//        return future.get();
//    }

    public void updatePurchase(Purchase purchase) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                long currentTime = System.currentTimeMillis();
                purchase.updateTime = currentTime;

                PurchaseDao purchaseDao = db.purchaseDao();
                purchaseDao.updatePurchase(purchase);
            }
        }).start();
    }

    public Purchase getPurchaseByBlockNumber(long blockNumber, String buyerAddress, String sellerAddress) throws ExecutionException, InterruptedException {
        Future<Purchase> future = executor.submit(new Callable() {
            @Override
            public Purchase call() {
                Purchase purchase = null;
                try {
                    purchase = db.purchaseDao().getPurchaseByBlock(blockNumber, buyerAddress, sellerAddress);


                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return purchase;
            }
        });
        return future.get();
    }

    public Purchase getPurchaseByState(int state, String buyerAddress, String sellerAddress, int endPointType) throws ExecutionException, InterruptedException {

        Future<Purchase> future = executor.submit(new Callable() {
            @Override
            public Purchase call() {
                Purchase purchase = null;
                try {
                    purchase = db.purchaseDao().getPurchaseByState(state, buyerAddress, sellerAddress, endPointType);

                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return purchase;
            }
        });
        return future.get();
    }

    public Purchase getPurchaseByState(int state, String buyerAddress, String sellerAddress) throws ExecutionException, InterruptedException {

        Future<Purchase> future = executor.submit(new Callable() {
            @Override
            public Purchase call() {
                Purchase purchase = null;
                try {
                    purchase = db.purchaseDao().getPurchaseByState(state, buyerAddress, sellerAddress);

                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return purchase;
            }
        });
        return future.get();
    }

    public List<Purchase> getAllPurchase() throws ExecutionException, InterruptedException {

        Future<List<Purchase>> future = executor.submit((Callable) () -> {
            List<Purchase> purchaselist = null;
            try {
                purchaselist = db.purchaseDao().getAll();
            } catch (Exception e) {
                Log.e("error", e.toString());
            }
            return purchaselist;
        });
        return future.get();
    }

    public List<Purchase> getAllActiveChannel(String address, int channelStatus, int endPointType) throws ExecutionException, InterruptedException {

        Future<List<Purchase>> future = executor.submit((Callable) () -> {
            List<Purchase> purchaselist = null;
            try {
                purchaselist = db.purchaseDao().getAllActiveChannel(address, channelStatus, endPointType);
            } catch (Exception e) {
                Log.e("error", e.toString());
            }
            return purchaselist;
        });
        return future.get();
    }


    public List<Purchase> getAllOpenDrawableBlock(String address, int channelStatus, int endPointType) throws ExecutionException, InterruptedException {

        Future<List<Purchase>> future = executor.submit(new Callable() {
            @Override
            public List<Purchase> call() {
                List<Purchase> allOpenDrawableList = null;
                try {
                    allOpenDrawableList = db.purchaseDao().getAllOpenDrawableBlock(address, channelStatus, endPointType);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return allOpenDrawableList;
            }
        });
        return future.get();
    }

    public LiveData<Double> getTotalPendingEarningBySeller(String myAddress, int channelStatus, int endPointType) throws ExecutionException, InterruptedException {
        Future<LiveData<Double>> future = executor.submit(new Callable<LiveData<Double>>() {
            @Override
            public LiveData<Double> call() throws Exception {
                LiveData<Double> totalPendingTokenEarn = null;
                try {
                    totalPendingTokenEarn = db.purchaseDao().getTotalPendingEarning(myAddress, channelStatus, endPointType);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return totalPendingTokenEarn;
            }
        });
        return future.get();
    }

    public LiveData<Integer> getDifferentNetworkData(String myAddress, int endPointType) throws ExecutionException, InterruptedException {

        Future<LiveData<Integer>> future = executor.submit(() -> {
            LiveData<Integer> totalTokenEarn = null;
            try {
                totalTokenEarn = db.purchaseDao().getDifferentNetworkData(myAddress, endPointType);
            } catch (Exception e) {
                Log.e("error", e.toString());
            }
            return totalTokenEarn;
        });

        return future.get();
    }

    public LiveData<Integer> getDifferentNetworkPurchase(String myAddress, int endPointType) throws ExecutionException, InterruptedException {

        Future<LiveData<Integer>> future = executor.submit(() -> {
            LiveData<Integer> totalTokenEarn = null;
            try {
                totalTokenEarn = db.purchaseDao().getDifferentNetworkPurchase(myAddress, endPointType);
            } catch (Exception e) {
                Log.e("error", e.toString());
            }
            return totalTokenEarn;
        });

        return future.get();
    }

    public LiveData<Integer> getTotalOpenChannel(String myAddress, int channelStatus) throws ExecutionException, InterruptedException {
        Future<LiveData<Integer>> future = executor.submit(new Callable<LiveData<Integer>>() {
            @Override
            public LiveData<Integer> call() throws Exception {
                LiveData<Integer> totalOpenChannel = null;
                try {
                    totalOpenChannel = db.purchaseDao().getTotalNumberOfOpenChannel(myAddress, channelStatus);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return totalOpenChannel;
            }
        });

        return future.get();
    }

    public LiveData<Double> getTotalEarnByUser(String myAddress, int endPointType) throws ExecutionException, InterruptedException {

        Future<LiveData<Double>> future = executor.submit(new Callable<LiveData<Double>>() {
            @Override
            public LiveData<Double> call() throws Exception {
                LiveData<Double> totalTokenEarn = null;
                try {
                    totalTokenEarn = db.purchaseDao().getTotalEarnByUser(myAddress, endPointType);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return totalTokenEarn;
            }
        });

        return future.get();
    }

    public LiveData<Double> getTotalSpentByUser(String myAddress, int endPointType) throws ExecutionException, InterruptedException {

        Future<LiveData<Double>> future = executor.submit(new Callable<LiveData<Double>>() {
            @Override
            public LiveData<Double> call() throws Exception {
                LiveData<Double> totalTokenEarn = null;
                try {
                    totalTokenEarn = db.purchaseDao().getTotalSpentByUser(myAddress, endPointType);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return totalTokenEarn;
            }
        });

        return future.get();
    }

    public int insertPurchaseRequest(PurchaseRequests purchaseRequests) throws ExecutionException, InterruptedException {

        Future<Integer> future = executor.submit(new Callable() {
            @Override
            public Integer call() {
                long[] rid = null;
                try {
                    long currentTime = System.currentTimeMillis();
                    purchaseRequests.createTime = currentTime;
                    purchaseRequests.updateTime = currentTime;


                    rid = db.purchaseRequestsDao().insertAll(purchaseRequests);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return (int) rid[0];
            }
        });
        return future.get().intValue();
    }

    public PurchaseRequests pickNextRequest(String address, int state) throws ExecutionException, InterruptedException {
        Future<PurchaseRequests> future = executor.submit(new Callable() {
            @Override
            public PurchaseRequests call() {
                PurchaseRequests purchaseRequests = null;
                try {
                    purchaseRequests = db.purchaseRequestsDao().getNext(address, state);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return purchaseRequests;
            }
        });
        return future.get();
    }

    public PurchaseRequests getPurchaseRequestById(int id) throws ExecutionException, InterruptedException {
        Future<PurchaseRequests> future = executor.submit(new Callable() {
            @Override
            public PurchaseRequests call() {
                PurchaseRequests purchaseRequests = null;
                try {
                    purchaseRequests = db.purchaseRequestsDao().getById(id);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return purchaseRequests;
            }
        });
        return future.get();
    }

    public void updatePurchaseRequest(PurchaseRequests purchaseRequests) {
        executor.submit(new Callable() {
            @Override
            public Integer call() {
                try {
                    long currentTime = System.currentTimeMillis();
                    purchaseRequests.updateTime = currentTime;
                    db.purchaseRequestsDao().update(purchaseRequests);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return 0;
            }
        });
    }

    public PurchaseRequests getPendingRequest(String buyerAddress, double value, int type, int state) throws ExecutionException, InterruptedException {
        Future<PurchaseRequests> future = executor.submit(new Callable() {
            @Override
            public PurchaseRequests call() {
                PurchaseRequests purchaseRequests = null;
                try {
                    purchaseRequests = db.purchaseRequestsDao().getPendingRequest(buyerAddress, value, type, state);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return purchaseRequests;
            }
        });
        return future.get();
    }

    public List<PurchaseRequests> getPendingRequestByUser(String buyerAddress, int state) throws ExecutionException, InterruptedException {
        Future<List<PurchaseRequests>> future = executor.submit(new Callable() {
            @Override
            public List<PurchaseRequests> call() {
                List<PurchaseRequests> purchaseRequests = null;
                try {
                    purchaseRequests = db.purchaseRequestsDao().getPendingRequest(buyerAddress, state);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return purchaseRequests;
            }
        });
        return future.get();

    }

    public List<String> getFailedRequestByUser(int state) throws ExecutionException, InterruptedException {
        Future<List<String>> future = executor.submit(new Callable() {
            @Override
            public List<String> call() {
                List<String> purchaseRequests = null;
                try {
                    purchaseRequests = db.purchaseRequestsDao().getFailedRequestByUser(state);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return purchaseRequests;
            }
        });
        return future.get();

    }

    public List<PurchaseRequests> getBuyerPendingRequest(int state) throws ExecutionException, InterruptedException {
        Future<List<PurchaseRequests>> future = executor.submit(new Callable() {
            @Override
            public List<PurchaseRequests> call() {
                List<PurchaseRequests> purchaseRequests = null;
                try {
                    purchaseRequests = db.purchaseRequestsDao().getBuyerPendingRequest(state);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return purchaseRequests;
            }
        });
        return future.get();

    }


    public PurchaseRequests getRequestByTrxHash(String hash) throws ExecutionException, InterruptedException {
        Future<PurchaseRequests> future = executor.submit(new Callable() {
            @Override
            public PurchaseRequests call() {
                PurchaseRequests purchaseRequests = null;
                try {
                    purchaseRequests = db.purchaseRequestsDao().getRequestByHash(hash);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return purchaseRequests;
            }
        });
        return future.get();
    }

    public List<PurchaseRequests> getUserCompletedRequested(String buyerAddress, int state) throws ExecutionException, InterruptedException {
        Future<List<PurchaseRequests>> future = executor.submit(new Callable() {
            @Override
            public List<PurchaseRequests> call() {
                List<PurchaseRequests> purchaseRequests = null;
                try {
                    purchaseRequests = db.purchaseRequestsDao().getCompletedRequest(buyerAddress, state);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return purchaseRequests;
            }
        });
        return future.get();
    }

    public PurchaseRequests getPurchaseRequestByMessageId(String messageId) throws ExecutionException, InterruptedException {
        Future<PurchaseRequests> future = executor.submit(new Callable() {
            @Override
            public PurchaseRequests call() {
                PurchaseRequests purchaseRequests = null;
                try {
                    purchaseRequests = db.purchaseRequestsDao().getRequestByMessageId(messageId);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return purchaseRequests;
            }
        });
        return future.get();
    }

    public void deletePurchaseRequest(PurchaseRequests purchaseRequests) throws ExecutionException, InterruptedException {
        executor.submit(new Callable() {
            @Override
            public Integer call() {
                try {
                    db.purchaseRequestsDao().delete(purchaseRequests);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return null;
            }
        });
    }


    public List<PurchaseRequests> getUserIncompleteRequests(String requesterAddress, int state, int endPoint) throws ExecutionException, InterruptedException {
        Future<List<PurchaseRequests>> future = executor.submit(new Callable() {
            @Override
            public List<PurchaseRequests> call() {
                List<PurchaseRequests> purchaseRequests = null;
                try {
                    purchaseRequests = db.purchaseRequestsDao().getIncompleteRequestsByRequesterAddress(requesterAddress, state, endPoint);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return purchaseRequests;
            }
        });
        return future.get();
    }

    public DatausageDao getDatausageDao() {
        return db.datausageDao();
    }

/*    public List<Message> getAll() throws ExecutionException, InterruptedException {
        Future<List<Message>> future = executor.submit(new Callable() {
            @Override
            public List<Message> call() {
                List<Message> messageList = null;
                try {
                    messageList = db.messageDao().getAll();
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return messageList;
            }
        });
        return future.get();

    }*/

    public void insertDataUsage(Datausage datausage) {
        executor.submit(new Callable() {
            @Override
            public Integer call() {
                try {
                    long currentTime = System.currentTimeMillis();
                    datausage.date = currentTime;
                    db.datausageDao().insertAll(datausage);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return 0;
            }
        });
    }

    public long getDataUsageByDate(long fromDate) throws ExecutionException, InterruptedException {

        Future<Long> future = executor.submit(new Callable() {
            @Override
            public Long call() {
                long dataSize = 0;
                try {
                    dataSize = db.datausageDao().getUsedData(fromDate);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return dataSize;
            }
        });
        return future.get();
    }


    public int insertBuyerPendingMessage(BuyerPendingMessage buyerPendingMessage) throws ExecutionException, InterruptedException {
        Future<Integer> future = executor.submit(new Callable() {
            @Override
            public Integer call() {
                long[] list = null;
                try {
                    long currentTime = System.currentTimeMillis();

                    buyerPendingMessage.createTime = currentTime;
                    buyerPendingMessage.updateTime = currentTime;

                    list = db.buyerPendingMessageDao().insertAll(buyerPendingMessage);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return (int) list[0];
            }
        });
        return future.get();
    }

    public void updateBuyerPendingMessage(BuyerPendingMessage buyerPendingMessage) {
        executor.submit(new Callable() {
            @Override
            public Integer call() {
                try {
                    long currentTime = System.currentTimeMillis();
                    buyerPendingMessage.updateTime = currentTime;

                    db.buyerPendingMessageDao().update(buyerPendingMessage);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return 0;
            }
        });
    }

    public BuyerPendingMessage getBuyerPendingMessageById(String msgId) throws ExecutionException, InterruptedException {
        Future<BuyerPendingMessage> future = executor.submit(new Callable() {
            @Override
            public BuyerPendingMessage call() {
                BuyerPendingMessage buyerPendingMessage = null;
                try {
                    buyerPendingMessage = db.buyerPendingMessageDao().getMsgById(msgId);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return buyerPendingMessage;
            }
        });
        return future.get();
    }

    /*public BuyerPendingMessage getBuyerPendingMessageByStatus(int status) throws ExecutionException, InterruptedException {
        Future<BuyerPendingMessage> future = executor.submit(new Callable() {
            @Override
            public BuyerPendingMessage call() {
                BuyerPendingMessage buyerPendingMessage = null;
                try {
                    buyerPendingMessage = db.buyerPendingMessageDao().getMsgByStatus(status);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return buyerPendingMessage;
            }
        });
        return future.get();
    }*/
    public BuyerPendingMessage getBuyerPendingMessageByUser(int status, String userAddress) throws ExecutionException, InterruptedException {
        Future<BuyerPendingMessage> future = executor.submit(new Callable() {
            @Override
            public BuyerPendingMessage call() {
                BuyerPendingMessage buyerPendingMessage = null;
                try {
                    buyerPendingMessage = db.buyerPendingMessageDao().getBuyerPendingMessageByUser(status, userAddress);
                } catch (Exception e) {
                    Log.e("error", e.toString());
                }
                return buyerPendingMessage;
            }
        });
        return future.get();
    }

    public void insertNetworkInfo(NetworkInfo... networkInfos) {
        executor.submit((Callable) () -> {
            try {
                db.getNetworkInfoDao().insertAll(networkInfos);
            } catch (Exception e) {
                Log.e("error", e.toString());
            }
            return 0;
        });
    }

    public Flowable<List<NetworkInfo>> getNetworkInfoByNetworkType() throws ExecutionException, InterruptedException {
        Future<Flowable<List<NetworkInfo>>> networkInfoFuture = executor.submit((Callable) () -> {
            Flowable<List<NetworkInfo>> networkInfo = null;
            try {
                networkInfo = db.getNetworkInfoDao().getAllNetworkInfo();
            } catch (Exception e) {
                Log.e("error", e.toString());
            }
            return networkInfo;
        });
        return networkInfoFuture.get();
    }

    public List<NetworkInfo> getAllNetworkInfo() throws ExecutionException, InterruptedException {
        Future<List<NetworkInfo>> networkInfoFuture = executor.submit((Callable) () -> {
            List<NetworkInfo> networkInfos = null;
            try {
                networkInfos = db.getNetworkInfoDao().getAll();
            } catch (Exception e) {
                Log.e("error", e.toString());
            }
            return networkInfos;
        });
        return networkInfoFuture.get();
    }

    public void updateCurrencyAndToken(int networkType, double currency, double token) {
        executor.submit((Callable) () -> {
            try {
                db.getNetworkInfoDao().updateCurrencyAndToken(networkType, currency, token);
            } catch (Exception e) {
                Log.e("error", e.toString());
            }
            return 0;
        });
    }

    public void updateCurrency(int networkType, double currency) {
        executor.submit((Callable) () -> {
            try {
                db.getNetworkInfoDao().updateCurrency(networkType, currency);
            } catch (Exception e) {
                Log.e("error", e.toString());
            }
            return 0;
        });
    }

    public void updateToken(int networkType, double token) {
        executor.submit((Callable) () -> {
            try {
                db.getNetworkInfoDao().updateToken(networkType, token);
            } catch (Exception e) {
                Log.e("error", e.toString());
            }
            return 0;
        });
    }

    public double getCurrencyByType(int networkType) throws ExecutionException, InterruptedException {
        Future<Double> future = executor.submit((Callable) () -> {
            try {
                return db.getNetworkInfoDao().getCurrencyByType(networkType);
            } catch (Exception e) {
                Log.e("error", e.toString());
            }
            return 0.0D;
        });
        return future.get();
    }

    public double getTokenByType(int networkType) throws ExecutionException, InterruptedException {
        Future<Double> future = executor.submit((Callable) () -> {
            try {
                return db.getNetworkInfoDao().getTokenByType(networkType);
            } catch (Exception e) {
                Log.e("error", e.toString());
            }
            return 0.0D;
        });
        return future.get();
    }
}
