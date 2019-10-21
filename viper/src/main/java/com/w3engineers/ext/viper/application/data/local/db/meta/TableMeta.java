package com.w3engineers.ext.viper.application.data.local.db.meta;

/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <br>----------------------------------------------------------------------------
 * <br>Created by: Ahmed Mohmmad Ullah (Azim) on [2019-07-18 at 11:10 AM].
 * <br>----------------------------------------------------------------------------
 * <br>Project: meshsdk.
 * <br>Code Responsibility: <Purpose of code>
 * <br>----------------------------------------------------------------------------
 * <br>Edited by :
 * <br>1. <First Editor> on [2019-07-18 at 11:10 AM].
 * <br>2. <Second Editor>
 * <br>----------------------------------------------------------------------------
 * <br>Reviewed by :
 * <br>1. <First Reviewer> on [2019-07-18 at 11:10 AM].
 * <br>2. <Second Reviewer>
 * <br>============================================================================
 **/
public class TableMeta {

    public static final String DB_NAME = "mesh.db";

    public interface TableNames {
        String ROUTING = "routing";
    }

    public interface ColumnNames {
        String ADDRESS = "address";
        String MAC = "mac";
        String TYPE = "type";
        String NEXT_HOP = "hop";
        String IP = "ip";
        String IS_ONLINE = "is_online";
        String TIME = "time";
        String NETWORK_NAME = "net_name";
    }

}
