package com.example.attendeasy;

import java.nio.charset.Charset;

import javax.crypto.EncryptedPrivateKeyInfo;

import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.telephony.TelephonyManager;
import android.text.format.Time;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements CreateNdefMessageCallback, 
    OnNdefPushCompleteCallback {

	NfcAdapter nfcAdapter;
	TextView infoText;
	 private static final int MESSAGE_SENT = 1;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            infoText = (TextView) findViewById(R.id.infotext);
            infoText.setText("NFC is not available on this device.");
        }
        
        // Register callback to set NDEF message
        nfcAdapter.setNdefPushMessageCallback(this, this);
        // Register callback to listen for message-sent success
        nfcAdapter.setOnNdefPushCompleteCallback(this, this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	public NdefMessage createNdefMessage(NfcEvent event) {
		Time time = new Time();
        time.setToNow();
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String text = ("Beam me up!\n\n" +
                "Beam Time: " + time.format("%H:%M:%S") +
                "IMEI Number:" + tm.getDeviceId());
        
        /**
         * The Android Application Record (AAR) is commented out. When a device
         * receives a push with an AAR in it, the application specified in the AAR
         * is guaranteed to run. The AAR overrides the tag dispatch system.
         * You can add it back in to guarantee that this
         * activity starts when receiving a beamed message. For now, this code
         * uses the tag dispatch system.
         */
        
        try {
			byte[] encryptedText = Protector.encrypt(text);
		
        NdefMessage msg = new NdefMessage(
        		new NdefRecord[] {
        				createMimeRecord("application/com.example.android.beam", encryptedText)
        				,NdefRecord.createApplicationRecord("com.example.attendeasyserver")
        		});
        return msg;
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
                
	}

	/**
     * Creates a custom MIME type encapsulated in an NDEF record
     *
     * @param mimeType
     */
    public NdefRecord createMimeRecord(String mimeType, byte[] payload) {
        byte[] mimeBytes = mimeType.getBytes(Charset.forName("US-ASCII"));
        NdefRecord mimeRecord = new NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, new byte[0], payload);
        return mimeRecord;
    }

	
	public void onNdefPushComplete(NfcEvent event) {
		// TODO Auto-generated method stub
		mHandler.obtainMessage(MESSAGE_SENT).sendToTarget();
	}
	
	/** This handler receives a message from onNdefPushComplete */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_SENT:
                Toast.makeText(getApplicationContext(), "Message sent!", Toast.LENGTH_LONG).show();
                break;
            }
        }
    };

}
