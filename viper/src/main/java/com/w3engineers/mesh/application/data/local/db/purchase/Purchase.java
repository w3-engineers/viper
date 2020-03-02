package com.w3engineers.mesh.application.data.local.db.purchase;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.w3engineers.mesh.application.data.local.model.Seller;
import com.w3engineers.mesh.application.data.local.purchase.PurchaseConstants;

@Entity
public class Purchase {

    @NonNull
    @PrimaryKey(autoGenerate = true)
    public int pid;

    @ColumnInfo(name = "buyer_address")
    public String buyerAddress;

    @ColumnInfo(name = "seller_address")
    public String sellerAddress;

    @ColumnInfo(name = "open_block_number")
    public long openBlockNumber;

    @ColumnInfo(name = "deposit")
    public double deposit;

    @ColumnInfo(name = "balance")
    public double balance;

    @ColumnInfo(name = "total_data_amount")
    public double totalDataAmount;

    @ColumnInfo(name = "used_data_amount")
    public double usedDataAmount;

    @ColumnInfo(name = "balance_proof")
    public String balanceProof;

    @ColumnInfo(name = "closing_hash")
    public String closingHash;

    @ColumnInfo(name = "withdrawn_balance")
    public double withdrawnBalance;

    @ColumnInfo(name = "create_time")
    public long createTime;

    @ColumnInfo(name = "update_time")
    public long updateTime;

    @ColumnInfo(name = "state")
    public int state;

    @ColumnInfo(name = "block_chain_endpoint")
    public int blockChainEndpoint;

    @ColumnInfo(name = "trx_hash")
    public String trxHash;


    public Seller toSeller(int label, String name) {
        Seller seller =  new Seller()
                .setId(sellerAddress)
                .setName(name)
                .setPurchasedData(totalDataAmount)
                .setUsedData(usedDataAmount)
                .setLabel(label);

        if (state == PurchaseConstants.CHANNEL_STATE.CLOSING) {
            seller.setBtnEnabled(false).setBtnText(PurchaseConstants.SELLERS_BTN_TEXT.CLOSING);
        } else {
            /*seller.setBtnEnabled(true)
                    .setBtnText(balance < deposit ?
                            PurchaseConstants.SELLERS_BTN_TEXT.CLOSE : PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);*/

            seller.setBtnEnabled(true);

            if (balance < deposit) {
                if ((deposit - balance) < 0.5 && (deposit - balance) > 0.1) {
                    seller.setBtnText(PurchaseConstants.SELLERS_BTN_TEXT.TOP_UP);
                } else if (deposit - balance < 0.1) {
                    seller.setBtnText(PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);
                } else {
                    seller.setBtnText(PurchaseConstants.SELLERS_BTN_TEXT.CLOSE);
                }
            } else {
                seller.setBtnText(PurchaseConstants.SELLERS_BTN_TEXT.PURCHASE);
            }

        }
        return seller;
    }
}
