package com.w3engineers.mesh.application.data.local.db.datausage;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

@Dao
public interface DatausageDao {
    @Query("SELECT SUM(data_in_byte) FROM Datausage WHERE date >= :fromDate")
    public abstract LiveData<Long> getDataUsage(long fromDate);

    @Query("SELECT SUM(data_in_byte) FROM Datausage WHERE date >= :fromDate")
    public Long getUsedData(long fromDate);

    @Insert
    void insertAll(Datausage... datausages);

    @Delete
    void delete(Datausage datausage);
}
