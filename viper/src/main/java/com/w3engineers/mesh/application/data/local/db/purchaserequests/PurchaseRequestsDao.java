package com.w3engineers.mesh.application.data.local.db.purchaserequests;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface PurchaseRequestsDao {

    @Query("SELECT * FROM purchaserequests")
    List<PurchaseRequests> getAll();


    @Query("SELECT * FROM purchaserequests WHERE requester_address= :address AND state=:state ORDER BY nonce ASC LIMIT 1")
    PurchaseRequests getNext(String address, int state);

    @Query("SELECT * FROM purchaserequests WHERE rid= :id")
    PurchaseRequests getById(int id);

    @Update
    void update(PurchaseRequests purchaseRequests);

    @Insert
    long[] insertAll(PurchaseRequests... purchaseRequests);

    @Delete
    void delete(PurchaseRequests purchaseRequests);

    @Query("SELECT * FROM purchaserequests WHERE buyer_address= :buyerAddress AND request_value = :value AND request_type = :type AND state<=:state ORDER BY nonce ASC LIMIT 1")
    PurchaseRequests getPendingRequest(String buyerAddress, double value, int type, int state);

    @Query("SELECT * FROM purchaserequests WHERE buyer_address= :buyerAddress AND state<=:state")
    List<PurchaseRequests> getPendingRequest(String buyerAddress, int state);

    @Query("SELECT DISTINCT buyer_address FROM purchaserequests WHERE state =:state")
    List<String> getFailedRequestByUser(int state);

    @Query("SELECT * FROM purchaserequests WHERE state =:state")
    List<PurchaseRequests> getBuyerPendingRequest(int state);

    @Query("SELECT * FROM purchaserequests WHERE trx_hash= :hash")
    PurchaseRequests getRequestByHash(String hash);

    @Query("SELECT * FROM purchaserequests WHERE buyer_address= :buyerAddress AND state=:state ORDER BY nonce ASC")
    List<PurchaseRequests> getCompletedRequest(String buyerAddress, int state);

    @Query("SELECT * FROM purchaserequests WHERE message_id= :messageId")
    PurchaseRequests getRequestByMessageId(String messageId);

}
