package com.w3engineers.mesh.application.ui.meshlog.data.model;

/*
 *  ****************************************************************************
 *  * Created by : Md Tariqul Islam on 8/8/2019 at 11:04 AM.
 *  * Email : tariqul@w3engineers.com
 *  *
 *  * Purpose:
 *  *
 *  * Last edited by : Md Tariqul Islam on 8/8/2019.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */

public class MeshLogModel {
    private int type;
    private String log;

    public MeshLogModel(int type, String log) {
        this.type = type;
        this.log = log;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
