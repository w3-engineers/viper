package com.w3engineers.mesh.application.data.local;

public interface DataPlanConstants {

    long SELLER_MINIMUM_WARNING_DATA = 1024;

    interface INTERRUPTION_EVENT {
        int USER_DISABLED_BT = 1;
        int USER_DISABLED_WIFI = 2;
        int LOCATION_PROVIDER_OFF = 3;
    }

    interface USER_ROLE {
        int MESH_USER = 0;
        int DATA_SELLER = 1;
        int DATA_BUYER = 2;
        int INTERNET_USER = 3;
        int MESH_STOP = 4;
    }

    interface DATA_MODE{
        int UNLIMITED = 0;
        int LIMITED = 1;
    }

    interface END_POINT_TYPE {
        int ETH_ROPSTEN = 1;
        int ETC_KOTTI = 2;
    }

    interface SELLERS_BTN_TEXT {
        String PURCHASE = "Purchase";
        String PURCHASING = "Purchasing";
        String CLOSE = "Close";
        String CLOSING = "Closing";
        String TOP_UP = "TopUp";
    }

    interface IntentKeys {
        String NUMBER_OF_ACTIVE_BUYER = "num_of_active_buyer";
    }

    interface SELLER_LABEL {
        int ONLINE_NOT_PURCHASED = 1;
        int ONLINE_PURCHASED = 2;
        int OFFLINE_PURCHASED = 3;
    }
}
