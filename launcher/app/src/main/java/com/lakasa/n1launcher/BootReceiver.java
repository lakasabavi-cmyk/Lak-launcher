package com.lakasa.n1launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Opens the launcher automatically after the projector finishes booting,
 * even on firmware where the stock home cannot be replaced as default.
 * Works once the app has been opened at least once (Android rule for
 * boot receivers) and the app is not force-stopped.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            Intent i = new Intent(context, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            context.startActivity(i);
        } catch (Exception ignored) {
            // Some firmware blocks background activity starts; in that case
            // setting this app as the default HOME (button in the UI) is the way.
        }
    }
}
