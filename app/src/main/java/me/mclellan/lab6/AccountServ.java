package me.mclellan.lab6;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AccountServ extends Service {
    public AccountServ() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}