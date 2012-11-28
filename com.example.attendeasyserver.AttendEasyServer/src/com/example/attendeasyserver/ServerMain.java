package com.example.attendeasyserver;

import java.util.concurrent.Callable;

import com.example.attendeasyserver.R;

import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ServerMain extends Activity {
  TextView infoText;
  Database db;
  private static final String CHECK_STRING = "Check this message";
  Activity me;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main_layout);    
    
    me = this;
    SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);

    infoText = (TextView) findViewById(R.id.infotext);

   //  this.deleteDatabase("attendeasy");
   // settings.edit().putInt(Constants.CURRENT_CLASS_SETTING, -1);
 // settings.edit().putString(Constants.SAVED_PIN, "");
   // settings.edit().commit();

    db = new Database(this);

    String className = db.getClassName(getCurrentClassId());
    if (className.isEmpty()) {
      infoText.setText(Constants.CLASS_STRING + ": " + Constants.NONE_STRING);
    }    else {
      infoText.setText(Constants.CLASS_STRING + ": " + className);
    }

    Button temp = (Button) findViewById(R.id.button1);
    temp.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        Time now = new Time();
        now.setToNow();

        processAttendance("12345", now);
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_layout, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
    case R.id.menu_settings:
      Protector.verifyPin(this, getSharedPreferences(Constants.PREFS_NAME, 0),
          new Callable<Object>() {
            public Object call() throws Exception {
              Intent intent = new Intent(me, MenuActivity.class);
              startActivity(intent);
              return null;
            }
          });
      break;
    default:
      // TODO throw error
      break;
    }

    return true;
  }

  @Override
  public void onResume() {
    super.onResume();

    String className = db.getClassName(getCurrentClassId());
    if (className.isEmpty()) {
      infoText.setText(Constants.CLASS_STRING + ": " + Constants.NONE_STRING);
    } else {
      infoText.setText(Constants.CLASS_STRING + ": " + className);
    }

    // Check to see that the Activity started due to
    // (msg.getRecords()[0].getPaylopayad()an Android Beam
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
    Parcelable[] rawMsgs = intent
        .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
    // only one message sent during the beam
    NdefMessage msg = (NdefMessage) rawMsgs[0];
    // record 0 contains the MIME type, record 1 is the AAR, if present

    String message;
    try {
      message = getMessageString(msg);
    } catch (Exception e) {
      Toast.makeText(getApplicationContext(), "Invalid message received!",
          Toast.LENGTH_LONG).show();
      return;
    }

    if (isMessageValid(message)) {
      String imei = getImei(message);
      Time time = getTime(message);
      processAttendance(imei, time);
    } else {
      Toast.makeText(getApplicationContext(), "Invalid message received!",
          Toast.LENGTH_LONG).show();
    }
  }

  private String getMessageString(NdefMessage msg) throws Exception {
    byte[] msgBytes = msg.getRecords()[0].getPayload();
    String actualBytes = Protector.decrypt(msgBytes);
    return actualBytes;
  }

  private boolean isMessageValid(String msg) {
    if (msg.substring(0, CHECK_STRING.length()).equals(CHECK_STRING)) {
      return true;
    }

    return false;
  }

  private void processAttendance(String imei, Time time) {
    if (getCurrentClassId() == -1) {
      Toast.makeText(getApplicationContext(), "No class selected!",
          Toast.LENGTH_LONG).show();
      return;
    }

    Time now = new Time();
    now.setToNow();

    boolean exists = db.exists(imei, getCurrentClassId());

    if (exists) {
      verifyAttendance(imei, time);
    } else {
      showNewStudentDialog(imei, time);
    }

  }

  private int getCurrentClassId() {
    SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
    int value = settings.getInt(Constants.CURRENT_CLASS_SETTING, -1);
    return value;
  }

  private void processNewStudent(String gtid, String imei, Time time) {
    // TODO:Message/popup to get gtid and name
    long success = db.insertNewStudent(imei, gtid, getCurrentClassId());

    if (success == -1) {
      Toast.makeText(getApplicationContext(),
          "Error in registration! Contact TA.", Toast.LENGTH_LONG).show();
    } else {
      Toast.makeText(getApplicationContext(), "Added and Attendance recorded",
          Toast.LENGTH_LONG).show();
    }
  }

  private void verifyAttendance(String imei, Time time) {
    // TODO check time difference and valid IMEI
    boolean valid = false;

    Time now = new Time();
    now.setToNow();

    Time mostRecentAttendance = db.getMostRecentAttendance(imei,
        getCurrentClassId());

    if (mostRecentAttendance == null) {
      // First class.
      valid = true;
    } else if (now.year == mostRecentAttendance.year
        && now.month == mostRecentAttendance.month
        && now.monthDay == mostRecentAttendance.monthDay
        && now.second == mostRecentAttendance.second) {
      Toast.makeText(getApplicationContext(),
          "You have already attended this day's class!", Toast.LENGTH_LONG)
          .show();
      valid = false;
    } else {
      valid = true;
    }

    if (valid) {
      db.recordAttendance(imei, getCurrentClassId());

      int numAttendance = db.getAttendance(imei, getCurrentClassId());
      Toast.makeText(
          getApplicationContext(),
          "Attendance recorded for " + now.format3339(true)
              + "\n You have attended " + numAttendance + " classes",
          Toast.LENGTH_LONG).show();

    }
  }

  private String getImei(String msg) {
    // TODO Auto-generated method stub
    return msg.substring(CHECK_STRING.length() + 14);
  }

  private Time getTime(String msg) {
    String millisString = msg.substring(CHECK_STRING.length(),
        CHECK_STRING.length() + 13);
    Long millis = Long.valueOf(millisString).longValue();
    Time time = new Time();
    time.set(millis);

    return time;
  }

  private void showNewStudentDialog(final String imei, final Time time) {
    AlertDialog.Builder alert = new AlertDialog.Builder(this);

    alert.setTitle("New Student Entry");
    alert.setMessage("Enter your GTID");

    // Set an EditText view to get user input
    final EditText input = new EditText(this);
    alert.setView(input);

    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        Editable value = input.getText();
        String gtid = value.toString();
        processNewStudent(gtid, imei, time);
      }
    });

    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        // Canceled.
      }
    });

    alert.show();
  }

}
