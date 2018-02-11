package com.gapp.common.animation;


import com.gapp.common.obj.IManager;

import java.util.List;

/**
 * Copyright (C) 2016 Tcl Corporation Limited
 * <p/>
 * Created on 16-8-16.
 * connecter is connect {@link IServant} and {@link IServant} , it decide if {@link IServant} is connected by connecter
 */
public interface IServantConnecter extends IManager {

    int SERVANT_STATE_ADD = 1;
    int SERVANT_STATE_REMOVE = 2;

    /**
     * this will be called by {@link IPainterView}, this called on {@link IPainterView#addServantConnnecter(IServantConnecter)} ()}
     *
     * @param servants
     */
    void connectServants(List<IServant> servants);

    /**
     * this will be called by {@link IPainterView}, when sprites is changed
     *
     * @param servant
     * @param state
     */
    void onServantStateChanged(IServant servant, int state);
    /**
     * this will be called by {@link IPainterView}
     */
    void pause();

    /**
     * this will be called by {@link IPainterView}
     */
    void resume();

    /**
     * this will be called by {@link IPainterView}
     */
    void running();


    /**
     * @param servant
     */
    void freeServant(IServant servant);


    /**
     * the servant controlled by {@link IServantConnecter}
     */
    interface IServant {

        void onControlByConnecter(IServantConnecter connecter, int state);
    }
}
