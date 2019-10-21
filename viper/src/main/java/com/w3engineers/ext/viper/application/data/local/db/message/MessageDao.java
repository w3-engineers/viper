package com.w3engineers.ext.viper.application.data.local.db.message;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface MessageDao {
    @Query("SELECT * FROM message")
    List<Message> getAll();

    @Query("SELECT * FROM message WHERE receiverId= :receiverId")
    List<Message> getPendingMessage(String receiverId);

    @Query("SELECT * FROM message WHERE messageId= :id")
    Message getMessageById(String id);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Message... messages);

    @Delete
    void delete(Message... delete);

    @Query("DELETE FROM message WHERE messageId = :messageId")
    void deleteByMessageById(String messageId);

}
