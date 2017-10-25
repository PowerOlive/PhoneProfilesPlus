package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;

public class WifiConnectionBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PPApplication.logE("##### WifiConnectionBroadcastReceiver.onReceive", "xxx");
        CallsCounter.logCounter(context, "WifiConnectionBroadcastReceiver.onReceive", "WifiConnectionBroadcastReceiver_onReceive");

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        if (intent == null)
            return;

        //WifiJob.startForConnectionBroadcast(appContext, intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO));

        if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            final NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);

            if (info != null) {

                final Handler handler = new Handler(appContext.getMainLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive", "state=" + info.getState());

                        if (PhoneProfilesService.connectToSSIDStarted) {
                            // connect to SSID is started

                            if (info.getState() == NetworkInfo.State.CONNECTED) {
                                //WifiManager wifiManager = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
                                //WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                //PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive", "wifiInfo.getSSID()=" + wifiInfo.getSSID());
                                //PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive", "PhoneProfilesService.connectToSSID=" + PhoneProfilesService.connectToSSID);
                                //if ((PhoneProfilesService.connectToSSID.equals(Profile.CONNECTTOSSID_JUSTANY)) ||
                                //    (wifiInfo.getSSID().equals(PhoneProfilesService.connectToSSID)))
                                PhoneProfilesService.connectToSSIDStarted = false;
                            }
                        }

                        if (Event.getGlobalEventsRunning(appContext)) {
                            if ((info.getState() == NetworkInfo.State.CONNECTED) ||
                                    (info.getState() == NetworkInfo.State.DISCONNECTED)) {
                                if (!((WifiScanJob.getScanRequest(appContext)) ||
                                        (WifiScanJob.getWaitForResults(appContext)) ||
                                        (WifiScanJob.getWifiEnabledForScan(appContext)))) {
                                    // wifi is not scanned

                                    PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive", "wifi is not scanned");

                                    if (!PhoneProfilesService.connectToSSIDStarted) {
                                        // connect to SSID is not started

                                        // start events handler
                                        EventsHandler eventsHandler = new EventsHandler(appContext);
                                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_WIFI_CONNECTION, false);

                                    }
                                } else
                                    PPApplication.logE("$$$ WifiConnectionBroadcastReceiver.onReceive", "wifi is scanned");
                            }
                        }
                    }
                });
            }
        }
    }
}
