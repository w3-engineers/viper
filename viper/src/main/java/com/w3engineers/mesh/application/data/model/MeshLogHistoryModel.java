package com.w3engineers.mesh.application.data.model;

/*
 *  ****************************************************************************
 *  * Created by : Md Tariqul Islam on 9/3/2019 at 11:03 AM.
 *  * Email : tariqul@w3engineers.com
 *  *
 *  * Purpose:
 *  *
 *  * Last edited by : Md Tariqul Islam on 9/3/2019.
 *  *
 *  * Last Reviewed by : <Reviewer Name> on <mm/dd/yy>
 *  ****************************************************************************
 */

public class MeshLogHistoryModel {
    private String name;
    private String path;

    public MeshLogHistoryModel(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
