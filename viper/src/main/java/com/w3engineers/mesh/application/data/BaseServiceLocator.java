package com.w3engineers.mesh.application.data;


import com.w3engineers.mesh.application.data.local.BaseMeshDataSource;

/*
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * ============================================================================
 */
public abstract class BaseServiceLocator {

    public abstract BaseMeshDataSource getTmDataSource();

}
