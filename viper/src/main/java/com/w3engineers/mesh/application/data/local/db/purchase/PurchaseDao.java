package com.w3engineers.mesh.application.data.local.db.purchase;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;


import com.w3engineers.mesh.application.data.local.purchase.PurchaseConstants;

import java.util.List;

@Dao
public interface PurchaseDao {
    @Query("SELECT * FROM purchase")
    List<Purchase> getAll();

    @Query("SELECT * FROM purchase WHERE open_block_number = :open_block_number AND buyer_address = :buyer_address AND seller_address = :seller_address LIMIT 1")
    Purchase getPurchaseByBlock(long open_block_number, String buyer_address, String seller_address);

    @Query("SELECT * FROM purchase WHERE open_block_number = :open_block_number AND state =:state AND buyer_address = :buyer_address AND seller_address = :seller_address LIMIT 1")
    Purchase getPurchaseByBlockAndState(long open_block_number, int state, String buyer_address, String seller_address);

    @Query("SELECT * FROM purchase WHERE state = :state AND buyer_address = :buyer_address AND seller_address = :seller_address LIMIT 1")
    Purchase getPurchaseByState(int state, String buyer_address, String seller_address);

    @Query("SELECT * FROM purchase WHERE state = :state AND buyer_address = :buyer_address")
    List<Purchase> getMyPurchasesWithState(int state, String buyer_address);

    @Query("SELECT SUM(balance) FROM purchase WHERE seller_address = :myID AND block_chain_endpoint = :endPointType")
    LiveData<Double> getTotalEarnByUser(String myID, int endPointType);

    @Query("SELECT SUM(balance) FROM purchase WHERE buyer_address = :myID AND block_chain_endpoint = :endPointType")
    LiveData<Double> getTotalSpentByUser(String myID, int endPointType);

    @Query("SELECT SUM(balance)-SUM(withdrawn_balance) FROM purchase WHERE seller_address = :myID AND state = :channelStatus AND balance > withdrawn_balance AND block_chain_endpoint = :endPointType")
    LiveData<Double> getTotalPendingEarning(String myID, int channelStatus, int endPointType);

    @Query("SELECT COUNT(state) FROM purchase WHERE seller_address = :myID AND state = :channelStatus AND balance > withdrawn_balance")
    LiveData<Integer> getTotalNumberOfOpenChannel(String myID, int channelStatus);

    @Query("SELECT * FROM purchase WHERE seller_address = :myID AND state = :channelStatus AND balance > withdrawn_balance AND block_chain_endpoint = :endPointType")
    List<Purchase> getAllOpenDrawableBlock(String myID, int channelStatus, int endPointType);

    @Query ("SELECT * FROM purchase WHERE buyer_address = :buyer_address AND (state = "
            + PurchaseConstants.CHANNEL_STATE.OPEN + " OR state = " + PurchaseConstants.CHANNEL_STATE.CLOSING + ")")
    List<Purchase> getMyActivePurchases(String buyer_address);

    @Query("SELECT COUNT(state) FROM purchase WHERE seller_address = :myID AND state = :channelStatus")
    int getTotalNumberOfActiveBuyer(String myID, int channelStatus);

    @Query("SELECT * FROM purchase WHERE seller_address = :myID AND state = :channelStatus AND block_chain_endpoint = :endPointType")
    List<Purchase> getAllActiveChannel(String myID, int channelStatus, int endPointType);

    @Query("SELECT * FROM purchase WHERE seller_address = :myID AND state = :channelStatus")
    List<Purchase> getAllActiveChannel(String myID, int channelStatus);

    @Update
    void updatePurchase(Purchase purchase);

    @Insert
    long[] insertAll(Purchase... purchases);

    @Delete
    void delete(Purchase purchase);

    @Query("SELECT * FROM purchase WHERE state = :state AND buyer_address = :buyer_address AND seller_address = :seller_address AND block_chain_endpoint = :endPointType LIMIT 1")
    Purchase getPurchaseByState(int state, String buyer_address, String seller_address, int endPointType);

    @Query("SELECT * FROM purchase WHERE trx_hash = :trxHash LIMIT 1")
    Purchase getPurchaseByTrxHash(String trxHash);

    @Query("SELECT COUNT(*) FROM purchase WHERE seller_address = :myID AND block_chain_endpoint != :endPointType")
    LiveData<Integer> getDifferentNetworkData(String myID, int endPointType);

    @Query("SELECT COUNT(*) FROM purchase WHERE buyer_address = :myID AND block_chain_endpoint != :endPointType")
    LiveData<Integer> getDifferentNetworkPurchase(String myID, int endPointType);

}