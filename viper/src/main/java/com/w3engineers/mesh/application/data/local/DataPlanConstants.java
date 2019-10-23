package com.w3engineers.mesh.application.data.local;

public interface DataPlanConstants {
    interface USER_TYPES {
        int  MESH_USER = 0;
        int DATA_SELLER = 1;
        int DATA_BUYER = 2;
        int INTERNET_USER = 3;
    }

    interface DATA_MODE{
        int UNLIMITED = 0;
        int LIMITED = 1;
    }

    interface END_POINT_TYPE {
        int ETH_ROPSTEN = 1;
        int ETC_KOTTI = 2;
    }
}
