package com.w3engineers.mesh.application.data;
 
/*
============================================================================
Copyright (C) 2019 W3 Engineers Ltd. - All Rights Reserved.
Unauthorized copying of this file, via any medium is strictly prohibited
Proprietary and confidential
============================================================================
*/

import com.w3engineers.mesh.application.data.model.Event;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;

public class AppDataObserver implements ApiEvent {

    private static AppDataObserver appDataObserver;
    private PublishSubject<Event> publishSubject;

    private AppDataObserver() {
        publishSubject = PublishSubject.create();
    }

    static {
        appDataObserver = new AppDataObserver();
    }

    public static AppDataObserver on() {
        return appDataObserver;
    }


    @Override
    public Disposable startObserver(Class event, Consumer<? extends Event> next) {
        return publishSubject.ofType(event).subscribe(next);
    }

    @Override
    public void sendObserverData(Event event) {
        new Thread(() -> publishSubject.onNext(event)).start();
    }
}
