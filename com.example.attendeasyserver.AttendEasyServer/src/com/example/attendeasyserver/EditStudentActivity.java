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

public class EditStudentActivity extends Activity {
	EditText search_text;
	EditText imei;
	EditText gtid;
	
	String searchedString;
  Activity me;
	Button searchButton;
	Button saveButton;
  
  
  Database db;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.editstudent_layout);
		 
		 me = this;
		 search_text = (EditText) findViewById(R.id.search_text);
		 imei = (EditText) findViewById(R.id.imei_text);
		 gtid = (EditText) findViewById(R.id.gtid_text);
		 searchButton = (Button) findViewById(R.id.search_button);
		 saveButton = (Button) findViewById(R.id.save_student_button);
		 searchedString = ""; 
		 db = new Database(this);
				 
		 addSearchClickListener();
		 addSaveClickListener();
	}
	
	
  private void addSaveClickListener() {
    saveButton.setOnClickListener(new OnClickListener() {
      
      public void onClick(View v) {
        db.updateStudent(searchedString, imei.getEditableText().toString(),
            gtid.getEditableText().toString());    
        
        search_text.setText("");
        searchedString = "";
        gtid.setText("");
        imei.setText("");
        Toast.makeText(me, "Student Updated!", Toast.LENGTH_LONG).show();
      }
    });
    
  }


  private void addSearchClickListener() {
    
    searchButton.setOnClickListener(new OnClickListener() {
      
      public void onClick(View v) {
        searchedString = search_text.getEditableText().toString();
        String imeiFound = "";
        String gtidFound = "";
        
        try {
          Pair<String, String> pair = db.searchForStudent(searchedString);
          imeiFound = pair.first;
          gtidFound = pair.second;
          
        } catch (StudentNotFoundException e) {
          Toast.makeText(me, "No Student Found!", Toast.LENGTH_LONG).show();
        } catch (TooManyResultsException e) {
          Toast.makeText(me, "Too many results: No exact match!", Toast.LENGTH_LONG).show();
        } finally {
          imei.setText(imeiFound);
          gtid.setText(gtidFound);
        }
        
      }
    });               
  }
}