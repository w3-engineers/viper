package com.w3engineers.mesh.application.data.local.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.w3engineers.mesh.application.data.local.db.buyerpendingmessage.BuyerPendingMessage;
import com.w3engineers.mesh.application.data.local.db.buyerpendingmessage.BuyerPendingMessageDao;
import com.w3engineers.mesh.application.data.local.db.datausage.Datausage;
import com.w3engineers.mesh.application.data.local.db.datausage.DatausageDao;
import com.w3engineers.mesh.application.data.local.db.message.Message;
import com.w3engineers.mesh.application.data.local.db.message.MessageDao;
import com.w3engineers.mesh.application.data.local.db.networkinfo.NetworkInfo;
import com.w3engineers.mesh.application.data.local.db.networkinfo.NetworkInfoDao;
import com.w3engineers.mesh.application.data.local.db.purchase.Purchase;
import com.w3engineers.mesh.application.data.local.db.purchase.PurchaseDao;
import com.w3engineers.mesh.application.data.local.db.purchaserequests.PurchaseRequests;
import com.w3engineers.mesh.application.data.local.db.purchaserequests.PurchaseRequestsDao;


@Database(entities = {Purchase.class, PurchaseRequests.class, Datausage.class, Message.class,
        BuyerPendingMessage.class, NetworkInfo.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PurchaseDao purchaseDao();
    public abstract PurchaseRequestsDao purchaseRequestsDao();
    public abstract DatausageDao datausageDao();
    public abstract MessageDao messageDao();
    public abstract BuyerPendingMessageDao buyerPendingMessageDao();
    public abstract NetworkInfoDao getNetworkInfoDao();
}

