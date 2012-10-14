package com.example.attendeasyserver;

import com.example.attendeasyserver.R;

import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.content.Intent;
import android.text.format.Time;
import android.view.Menu;
import android.widget.TextView;

public class ServerMain extends Activity {
    TextView infoText;
    Database db;        
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        
        infoText = (TextView) findViewById(R.id.infotext);
       // db = new Database(this);
        
        // TODO: Get cache of student list from server
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_layout, menu);
        return true;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Check to see that the Activity started due to (msg.getRecords()[0].getPaylopayad()an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];
        // record 0 contains the MIME type, record 1 is the AAR, if present
        
        infoText.setText(new String(msg.getRecords()[0].getPayload()));
        
        assert(isMessageValid(msg));
        String imei = getImei(msg);
        Time time = getTime(msg);
        
        processAttendance(imei, time);        
    }

	private boolean isMessageValid(NdefMessage msg) {
		// TODO Auto-generated method stub
		return true;
	}

	private void processAttendance(String imei, Time time) {
		Time now = new Time();
        now.setToNow();
        
        if (db.exists(imei)) {
            verifyAttendance(imei, time);		        	
        } else {
        	processNewStudent(imei, time);
        }
         
	}

	private void processNewStudent(String imei, Time time) {
		// TODO:Message/popup to get gtid and name
		String gtid = "";
		db.insertNew(imei, gtid);
	}

	private void verifyAttendance(String imei, Time time) {
		// TODO check time difference and valid IMEI
		boolean valid = false;
		
		if (valid) {
			// TODO: popup message saying "Logged".
			
			// TODO: send this attendance to server.
		} else {
			// TODO: popup message saying invalid
		}
	}

	private String getImei(NdefMessage msg) {
		// TODO Auto-generated method stub
		return null;
	}

	private Time getTime(NdefMessage msg) {
		// TODO Auto-generated method stub
		return null;
	}

}
