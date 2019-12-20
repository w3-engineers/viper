package com.w3engineers.eth.data.remote.parse;

/*
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 *
 * Purpose: This class contains all Parse related Constant value.
 * Like table, Column information
 * ============================================================================
 */

import com.parse.ParseObject;

import java.math.BigInteger;

public class ParseConstant {
    interface Transaction {
        String TABLE = "Transaction";
        String TX_HASH = "tx_hash";
        String PURPOSE = "purpose";
        String LOG = "log";
    }

    interface BALANCE_APPROVE {
        String OWNER = "owner";
        String SPENDER = "spender";
        String VALUE = "value";
    }

    interface CHANNEL_CREATE {
        String SENDER_ADDRESS = "sender_address";
        String RECEIVER_ADDRESS = "receiver_address";
        String DEPOSIT = "deposit";
    }

    interface CHANNEL_TOPUP {
        String SENDER_ADDRESS = "sender_address";
        String RECEIVER_ADDRESS = "receiver_address";
        String OPEN_BLOCH_NUMBER = "open_block_number";
        String ADDED_DEPOSIT = "added_deposit";
    }

    interface CHANNEL_CLOSE {
        String SENDER_ADDRESS = "sender_address";
        String RECEIVER_ADDRESS = "receiver_address";
        String OPEN_BLOCH_NUMBER = "open_block_number";
        String BALANCE = "balance";
        String RECEIVER_TOKENS = "receiver_tokens";
    }

    interface CHANNEL_WITHDRAW {
        String SENDER_ADDRESS = "sender_address";
        String RECEIVER_ADDRESS = "receiver_address";
        String OPEN_BLOCH_NUMBER = "open_block_number";
        String WITHDRAWN_BALANCE = "withdrawn_balance";
    }

    interface ETH_GIFT {
        String SENDER_ADDRESS = "from";
        String RECEIVER_ADDRESS = "to";
        String VALUE = "value";
    }

    interface TOKEN_MINTED {
        String TO = "to";
        String NUM = "num";
    }

    interface REQUEST_TYPES {
        String CREATE_CHANNEL = "create";
        String TOPUP_CHANNEL = "topup";
        String CLOSE_CHANNEL = "close";
        String WITHDRAW_CHANNEL = "withdraw";
        String ETHER_GIFTED = "eth_gifted";
        String TOKEN_GIFTED = "tkn_gifted";

    }
}
