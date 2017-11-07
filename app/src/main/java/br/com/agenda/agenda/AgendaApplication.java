package br.com.agenda.agenda;

import android.content.Context;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

/**
 * Created by italo.teixeira on 07/11/2017.
 */

public class AgendaApplication extends MultiDexApplication {

    private static AgendaApplication agendaApplication;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        agendaApplication = this;
    }

    public static synchronized AgendaApplication getInstance() {
        return agendaApplication;
    }

}
