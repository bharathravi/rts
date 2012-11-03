package com.example.attendeasyserver;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.format.Time;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class PinActivity extends Activity {
	EditText pin;
	EditText repin;
  Activity me;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.pinview_layout);
		 
		 me = this;
		 Button savePin = (Button) findViewById(R.id.save_button);
		 pin = (EditText) findViewById(R.id.new_pin);
		 repin = (EditText) findViewById(R.id.reenter_pin);
		 
		 savePin.setOnClickListener(new OnClickListener() {
      
      public void onClick(View v) {
        String pin1 = pin.getEditableText().toString();
        String pin2 = repin.getEditableText().toString();
        
        if (Protector.validateNewPins(pin1, pin2, me)) {
          SharedPreferences settings = getSharedPreferences(
              Constants.PREFS_NAME, 0);
          if (Protector.savePin(settings, pin1)) {
            Toast.makeText(me, "Pin changed", Toast.LENGTH_LONG).show();
          } else {
            Toast.makeText(me, "Error while saving Pin!", Toast.LENGTH_LONG).show();
          }
        }
      }
    });
	}
}
