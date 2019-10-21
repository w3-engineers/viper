package com.w3engineers.ext.viper.application.data.local.db.datausage;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

@Dao
public interface DatausageDao {
    @Query("SELECT SUM(data_in_byte) FROM Datausage WHERE date >= :fromDate AND date <= :toDate")
    public abstract LiveData<Long> getDataUsage(long fromDate, long toDate);

    @Query("SELECT SUM(data_in_byte) FROM Datausage WHERE purchase_id = :pid")
    public long getDataUsage(int pid);

    @Query("SELECT SUM(data_in_byte) FROM Datausage WHERE date >= :fromDate AND date <= :toDate")
    public Long getUsedData(long fromDate, long toDate);


    @Update
    void updatePurchase(Datausage datausage);

    @Insert
    void insertAll(Datausage... datausages);

    @Delete
    void delete(Datausage datausage);
}
