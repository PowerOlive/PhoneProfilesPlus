package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

public class AlarmClockEventEndBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST] AlarmClockEventEndBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "AlarmClockEventEndBroadcastReceiver.onReceive", "AlarmClockEventEndBroadcastReceiver_onReceive");

        String action = intent.getAction();
        if (action != null) {
            //PPApplication.logE("AlarmClockEventEndBroadcastReceiver.onReceive", "action=" + action);
            doWork(/*true,*/ context);
        }
    }

    private void doWork(/*boolean useHandler,*/ Context context) {
        //PPApplication.logE("[HANDLER] AlarmClockEventEndBroadcastReceiver.doWork", "useHandler="+useHandler);

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning()) {
            //if (useHandler) {
            PPApplication.startHandlerThreadBroadcast(/*"AlarmClockEventEndBroadcastReceiver.doWork"*/);
            final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=AlarmClockEventEndBroadcastReceiver.doWork");

                    PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    try {
                        if (powerManager != null) {
                            wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":AlarmClockEventEndBroadcastReceiver_doWork");
                            wakeLock.acquire(10 * 60 * 1000);
                        }

//                        PPApplication.logE("[EVENTS_HANDLER_CALL] AlarmClockEventEndBroadcastReceiver.doWork", "sensorType=SENSOR_TYPE_ALARM_CLOCK_EVENT_END");
                        EventsHandler eventsHandler = new EventsHandler(appContext);
                        eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_ALARM_CLOCK_EVENT_END);

                        //PPApplication.logE("****** EventsHandler.handleEvents", "END run - from=AlarmClockEventEndBroadcastReceiver.doWork");
                    } catch (Exception e) {
//                        PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", Log.getStackTraceString(e));
                        PPApplication.recordException(e);
                    } finally {
                        if ((wakeLock != null) && wakeLock.isHeld()) {
                            try {
                                wakeLock.release();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                }
            });
            /*}
            else {
                if (Event.getGlobalEventsRunning(appContext)) {
                    PPApplication.logE("AlarmClockEventEndBroadcastReceiver.doWork", "handle events");
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_ALARM_CLOCK_EVENT_END);
                }
            }*/
        }
    }

}
