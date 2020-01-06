package com.w3engineers.mesh.application.data.local.purchase;


public interface PurchaseConstants {
    public static long SELLER_MINIMUM_WARNING_DATA = 1024; // The format is byte

    interface MESSAGE_TYPES {
        int INIT_PURCHASE = 1;
        int INIT_PURCHASE_OK = 2;
        int INIT_PURCHASE_ERROR = 3;

        int CREATE_CHANNEL = 4;
        int CREATE_CHANNEL_OK = 5;
        int CREATE_CHANNEL_ERROR = 6;

        int INFO_QUERY = 7;
        int INFO_OK = 8;
        int INFO_ERROR = 9;

        int GOT_MESSAGE = 10;
        int PAY_FOR_MESSAGE_OK = 11;
        int PAY_FOR_MESSAGE_ERROR = 12;
        int PAY_FOR_MESSAGE_RESPONSE = 13;
        int BUYER_UPDATE_NOTIFYER = 14;

        int BUY_TOKEN = 17;
        int BUY_TOKEN_RESPONSE = 18;

        int SYNC_BUYER_TO_SELLER = 19;
        int SYNC_SELLER_TO_BUYER = 21;
        int SYNC_SELLER_TO_BUYER_OK = 22;

        int ETHER_REQUEST = 23;
        int ETHER_REQUEST_RESPONSE = 24;
        int RECEIVED_ETHER = 25;

        int BLOCKCHAIN_REQUEST = 26;
        int BLOCKCHAIN_RESPONSE = 27;

        int CHANNEL_CLOSED = 28;
        int CHANNEL_TOPUP = 29;

        int GIFT_ETHER_REQUEST = 31;
        int GIFT_ETHER_REQUEST_SUBMITTED = 32;
        int GIFT_ETHER_REQUEST_WITH_HASH = 33;
        int GIFT_RESPONSE = 34;

        int DISCONNECTED_BY_SELLER = 35;
    }

    interface INTERNAL_MESSAGE_TYPES {
        int INTERNET_MESSAGE_RESPONSE = 1;
    }

    double PRICE_PER_MB = 1.0;
    double RMESH_PER_POINT = 1.0;
    double BUY_TOKEN_ETHER_VALUE = 0.1;

    interface CHANNEL_STATE {
        int OPEN = 1;
        int CLOSED = 2;
        int CLOSING = 3;
    }

    interface JSON_KEYS {
        String REQUEST_TYPE = "rt";
        String SIGNED_MESSAGE = "sm";
        String REQUEST_VALUE = "rv";
        String NONCE = "n";

        //MESSAGE GENERAL KEYS
        String MESSAME_FROM = "fr";
        String REQUEST_LIST = "rl";
        String MESSAGE_TYPE = "t";
        String MESSAGE_TEXT = "msg";
        String OPEN_BLOCK = "ob";
        String DEPOSIT = "deposit";
        String INFO_KEYS = "info";
        String INFO_PURPOSE = "pur";
        String INFO_JSON = "ij";


        String ACK_MODE = "am";
        String MESSAGE_MODE = "mm";
        String MESSAGE_BPS = "bps";
        String MESSAGE_CHS = "chs";
        String MESSAGE_SENDER = "ms";
        String MESSAGE_RECEIVER = "mr";
        String MESSAGE_DATA = "md";
        String BPS_BALANCE = "bb";
        String DATA_SIZE = "ds";

        String MESSAGE_SENT_SUCCESS = "mss";
        String MESSAGE_BODY = "mb";
        String MESSAGE_ID = "mi";

        String BUYER_ADDRESS = "ba";
        String SELLER_ADDRESS = "sa";
        String USED_DATA = "ud";
        String TOTAL_DATA = "td";
        String ETHER = "eth";
        String RESPONSE_CODE = "rc";
        String REQUEST_SUCCESS = "rs";
        String END_POINT_TYPE = "ept";

        String GIFT_REQUEST_IS_SUBMITTED = "gris";
        String GIFT_REQUEST_SUBMIT_MESSAGE = "grsm";
        String GIFT_ETH_HASH_REQUEST_SUBMIT = "gers";
        String GIFT_TKN_HASH_REQUEST_SUBMIT = "gtrs";
        String GIFT_TKN_FAILED_BY = "failedby";

        String GIFT_ETH_BALANCE = "geb";
        String GIFT_TKN_BALANCE = "gtb";

        String IS_INCOMING = "ii";
    }

    interface REQUEST_TYPES {
        int APPROVE_ZERO = 0;
        int APPROVE_TOKEN = 1;
        int CREATE_CHANNEL = 2;
        int TOPUP_CHANNEL = 3;
        int CLOSE_CHANNEL = 4;
        int WITHDRAW_CHANNEL = 5;
        int BUY_TOKEN = 6;
        int CLAIM_RM = 7;
        int CONVERT_RM = 8;
    }

    interface REQUEST_STATE {
        int RECEIVED = 0;
        int PENDING = 1;
        int COMPLETED = 2;
        int NOTIFIED = 3;
    }

    interface MESSAGE_MODE {
        int INTERNET_SEND = 1;
        int INTERNET_RECEIVE = 2;
        int LOCAL = 3;

        int INTERNET_SEND_ACK = 4;
        int INTERNET_RECEIVE_ACK = 5;
        int LOCAL_ACK = 6;
    }

    interface DATA_USAGE_PURPOSE {
        int MESSAGE = 1;
        int OTHER = 2;
    }

    interface BUYER_PENDING_MESSAGE_STATUS {
        int RECEIVED = 1;
        int SENT_PAID = 2;
        int SENT_NOT_PAID = 3;
        int IN_PROGRESS = 4;
    }

    interface INFO_KEYS {
        String SHARED_DATA = "shared_data";
        String ETH_BALANCE = "eth_balance";
        String TKN_BALANCE = "tkn_balance";
        String ALOWANCE = "allowance";
        String NONCE = "nonce";
        String PENDING_REQUEST_NUMBER = "prn";
        String PURCHASE_INFO = "pur_info";
    }

    interface INFO_PURPOSES {
        int REFRESH_BALANCE = 1;
        int BUY_TOKEN = 2;
        int CLOSE_PURCHASE = 3;
        int CREATE_CHANNEL = 4;
        int TOPUP_CHANNEL = 5;
        int CONVERT_RM = 6;
    }
    interface TimeoutPurpose {
        int INIT_PURCHASE = 9;
        int INIT_CHANNEL = 10;
        int INIT_ETHER = 11;

        int BUYER_PENDING_MESSAGE = 12;
        int PAY_FOR_MESSAGE_RESPONSE = 13;
    }

    interface SELLERS_BTN_TEXT {
        String PURCHASE = "Purchase";
        String PURCHASING = "Purchasing";
        String CLOSE = "Close";
        String CLOSING = "Closing";
        String TOP_UP = "TopUp";
    }

    interface GIFT_REQUEST_STATE {
        int NOT_REQUESTED_YET = 1;
        int REQUESTED_TO_SELLER = 2;
        int GOT_TRANX_HASH = 3;
        int GOT_GIFT_ETHER = 4;
    }
    public interface IntentKeys {
        String NUMBER_OF_ACTIVE_BUYER = "num_of_active_buyer";
    }
}




