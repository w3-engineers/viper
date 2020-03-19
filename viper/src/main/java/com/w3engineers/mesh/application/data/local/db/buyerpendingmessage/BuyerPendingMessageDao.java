package com.w3engineers.mesh.application.data.local.db.buyerpendingmessage;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

@Dao
public interface BuyerPendingMessageDao {

    @Query("SELECT * FROM buyerpendingmessage WHERE msg_id =:msgID")
    public BuyerPendingMessage getMsgById(String msgID);

    @Query("SELECT * FROM buyerpendingmessage WHERE status =:status ORDER BY create_time ASC LIMIT 1")
    public BuyerPendingMessage getMsgByStatus(int status);

    @Query("SELECT * FROM buyerpendingmessage WHERE status =:status AND ( (is_incomming=1 AND owner =:userAddress) OR ( is_incomming=0 AND sender=:userAddress ))  ORDER BY create_time ASC LIMIT 1")
    public BuyerPendingMessage getBuyerPendingMessageByUser(int status, String userAddress);

    @Update
    void update(BuyerPendingMessage buyerPendingMessage);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long[] insertAll(BuyerPendingMessage... buyerPendingMessages);

    @Delete
    void delete(BuyerPendingMessage buyerPendingMessage);
}