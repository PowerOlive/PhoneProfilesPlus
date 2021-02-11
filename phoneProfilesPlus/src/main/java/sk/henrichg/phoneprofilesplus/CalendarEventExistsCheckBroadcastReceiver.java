package sk.henrichg.phoneprofilesplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;

public class CalendarEventExistsCheckBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
//        PPApplication.logE("[IN_BROADCAST]  CalendarEventExistsCheckBroadcastReceiver.onReceive", "xxx");
        //CallsCounter.logCounter(context, "EventTimeBroadcastReceiver.onReceive", "EventTimeBroadcastReceiver_onReceive");

        String action = intent.getAction();
        if (action != null) {
            //PPApplication.logE("EventTimeBroadcastReceiver.onReceive", "action=" + action);
            doWork(/*true,*/ context);
        }
    }

    private void doWork(/*boolean useHandler,*/ Context context) {
        //PPApplication.logE("[HANDLER] EventTimeBroadcastReceiver.doWork", "useHandler="+useHandler);

        final Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(true))
            // application is not started
            return;

        if (Event.getGlobalEventsRunning()) {
            //if (useHandler) {
            PPApplication.startHandlerThreadBroadcast(/*"EventTimeBroadcastReceiver.doWork"*/);
            final Handler handler = new Handler(PPApplication.handlerThreadBroadcast.getLooper());
            handler.post(() -> {
//                    PPApplication.logE("[IN_THREAD_HANDLER] PPApplication.startHandlerThread", "START run - from=EventTimeBroadcastReceiver.doWork");

                PowerManager powerManager = (PowerManager) appContext.getSystemService(Context.POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                try {
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, PPApplication.PACKAGE_NAME + ":CalendarEventExistsCheckBroadcastReceiver_doWork");
                        wakeLock.acquire(10 * 60 * 1000);
                    }


//                    PPApplication.logE("[EVENTS_HANDLER_CALL] CalendarEventExistsCheckBroadcastReceiver.doWork", "sensorType=SENSOR_TYPE_CALENDAR_EVENT_EXISTS_CHECK");
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_CALENDAR_EVENT_EXISTS_CHECK);

                    DataWrapper dataWrapper = new DataWrapper(context.getApplicationContext(), false, 0, false);
                    dataWrapper.fillEventList();

                    for (Event _event : dataWrapper.eventList) {
                        if ((_event._eventPreferencesCalendar._enabled) && (_event.getStatus() != Event.ESTATUS_STOP)) {
//                            PPApplication.logE("[CALENDAR] CalendarEventExistsCheckBroadcastReceiver.doWork", "setAlarm() - event._id=" + _event._id);
                            _event._eventPreferencesCalendar.setAlarm(/*true,*/ 0, context, true);
                        }
                    }


//                    PPApplication.logE("[EVENTS_HANDLER_CALL] CalendarEventExistsCheckBroadcastReceiver.doWork", "END run");
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
            });
            /*}
            else {
                if (Event.getGlobalEventsRunning(appContext)) {
                    PPApplication.logE("EventTimeBroadcastReceiver.doWork", "handle events");
                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_TIME);
                }
            }*/
        }
    }

}