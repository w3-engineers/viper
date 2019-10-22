package com.w3engineers.mesh.application.data;
 
/*
============================================================================
Copyright (C) 2019 W3 Engineers Ltd. - All Rights Reserved.
Unauthorized copying of this file, via any medium is strictly prohibited
Proprietary and confidential
============================================================================
*/

import com.w3engineers.mesh.application.data.model.DataAckEvent;
import com.w3engineers.mesh.application.data.model.DataEvent;
import com.w3engineers.mesh.application.data.model.Event;
import com.w3engineers.mesh.application.data.model.PeerAdd;
import com.w3engineers.mesh.application.data.model.PeerRemoved;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public interface ApiEvent {

    Class DATA = DataEvent.class;
    Class DATA_ACKNOWLEDGEMENT = DataAckEvent.class;

    Class PEER_ADD = PeerAdd.class;
    Class PEER_REMOVED = PeerRemoved.class;

    Disposable startObserver(Class event, Consumer<? extends Event> next);

    void sendObserverData(Event event);
}
