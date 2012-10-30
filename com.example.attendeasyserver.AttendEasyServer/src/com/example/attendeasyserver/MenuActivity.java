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

public class MenuActivity extends Activity {
	
	Database db;
	ListView classSelectList;
	View currentSelected = null;
	TextView currentClassName;
	
	private class MenuObject {
		String className;
		int classId;
		
		public MenuObject(Pair<Integer, String> thing) {
			className = thing.second;
			classId = thing.first;
		}
		
		@Override
		public String toString() {
			return className;
		}
	}
	
	private int getCurrentClassId() {
		SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
		return settings.getInt(Constants.CURRENT_CLASS_SETTING, -1);
	}
	
	private void setCurrentClassId(int id) {
		SharedPreferences.Editor settings = getSharedPreferences(Constants.PREFS_NAME, 0).edit();
	    settings.putInt(Constants.CURRENT_CLASS_SETTING, id);	    	    
	    settings.commit();
	}

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.options_layout);
	     db = new Database(this);	
	     classSelectList = (ListView) findViewById(R.id.class_select_list);
	     
	     currentClassName = (TextView) findViewById(R.id.current_class_name);
	     
	     
	     populateClassList();  		     		    
		    
	     // Short press listener
	     classSelectList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {				
				MenuObject x = (MenuObject) parent.getItemAtPosition(position);				
				view.setBackgroundColor(Color.CYAN);
				setCurrentClassId(x.classId);
			    
			    if (currentSelected != null) {
			    	currentSelected.setBackgroundColor(Color.TRANSPARENT);
			    }
			    
			    currentSelected = view;
			    populateClassList();
			}
			
			public void onNothingSelected(AdapterView<?> arg0) {
				// Do nothing												
			}
		});
	     
	    // Long press listener 
	     classSelectList.setOnItemLongClickListener(new OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				final MenuObject x = (MenuObject) parent.getItemAtPosition(position);
				AlertDialog.Builder alert = new AlertDialog.Builder(view.getContext());
				alert.setTitle("");
				alert.setMessage("Delete this class?");
				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					  public void onClick(DialogInterface dialog, int whichButton) {					    
					    db.deleteClass(x.classId);
					    
					    if (getCurrentClassId() == x.classId) {
					    	setCurrentClassId(x.classId);
					    }
					    populateClassList();
					  }
					});

					alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					  public void onClick(DialogInterface dialog, int whichButton) {
					    // Canceled.
					  }
					});
					
					alert.show();

				return true;
			}
		});
	     
	    Button button = (Button) findViewById(R.id.class_add_button);
	    button.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {				
				showNewClassDialog();
			}
		});
	}
	
	@Override 
	protected void onResume() {
		super.onResume();
		populateClassList();
	}

	private void populateClassList() {
		String className = db.getClassName(getCurrentClassId());
		if (className.isEmpty()) {
			currentClassName.setText(Constants.NONE_STRING);			
		} else {
			currentClassName.setText(className);
		}
		
		List<Pair<Integer, String>> classList = db.getClassList();
	     	     
	     List<MenuObject> nameList = new ArrayList<MenuObject>();
	     for (Pair<Integer, String> aClass : classList) {
	    	 nameList.add(new MenuObject(aClass));	    	 
	     }
	     
	     ArrayAdapter<MenuObject> dataAdapter = new ArrayAdapter<MenuObject>(this,
	    			android.R.layout.simple_list_item_1, nameList);	     
	     	     
	     classSelectList.setAdapter(dataAdapter);
	}
	
	private void showNewClassDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("New Class");
		alert.setMessage("Enter Class Name");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);
		
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    Editable value = input.getText();
		    String classname = value.toString();
		    processNewClass(classname);		  		 
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});
		
		alert.show();
	}

	private void processNewClass(String classname) {
		db.addNewClass(classname);
		populateClassList();
	}
}
