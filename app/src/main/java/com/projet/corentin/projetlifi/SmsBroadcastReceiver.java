package com.projet.corentin.projetlifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by corentin on 21/11/17.
 */

public class SmsBroadcastReceiver extends BroadcastReceiver {

    public static final String SMS_BUNDLE = "pdus";

    public void onReceive(Context context, Intent intent) {
        Log.w("toto", "onReceive123");
        Bundle intentExtras = intent.getExtras();

        if (intentExtras != null) {
            Object[] sms = (Object[]) intentExtras.get(SMS_BUNDLE);
            String smsMessageStr = "";
            for (int i = 0; i < sms.length; ++i) {
                String format = intentExtras.getString("format");
                SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) sms[i], format);

                String smsBody = smsMessage.getMessageBody().toString();
                String address = smsMessage.getOriginatingAddress();

                Toast.makeText(context, smsBody, Toast.LENGTH_LONG).show();
//                smsMessageStr += "SMS From: " + address + "\n";
                smsMessageStr += smsBody;
            }

            MainActivity inst = MainActivity.instance();
            inst.createToast(smsMessageStr);
            Log.d("toto1", smsMessageStr);
            inst.handleMessage(smsMessageStr);
        }
    }
}
