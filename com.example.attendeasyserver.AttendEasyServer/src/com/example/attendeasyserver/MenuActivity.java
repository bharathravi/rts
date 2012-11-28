package com.example.attendeasyserver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MenuActivity extends Activity {

  Database db;
  ListView classSelectList;
  View currentSelected = null;
  TextView currentClassName;
  Activity me;

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
    SharedPreferences.Editor settings = getSharedPreferences(
        Constants.PREFS_NAME, 0).edit();
    settings.putInt(Constants.CURRENT_CLASS_SETTING, id);
    settings.commit();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.options_layout);
    me = this;
    db = new Database(this);
    classSelectList = (ListView) findViewById(R.id.class_select_list);
    currentClassName = (TextView) findViewById(R.id.current_class_name);

    populateClassList();
    addListClickListeners();
    addNewClassClickListener();
    addPinClickListener();    
    addEditStudentClickListener();
  }

  private boolean assertMedia() {
    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;
    String state = Environment.getExternalStorageState();

    if (Environment.MEDIA_MOUNTED.equals(state)) {
      // We can read and write the media
      mExternalStorageAvailable = mExternalStorageWriteable = true;
    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
      // We can only read the media
      mExternalStorageAvailable = true;
      mExternalStorageWriteable = false;
      System.out.println("External Media is not Writeable.");
      Toast.makeText(me, "External Media is not Writeable.", Toast.LENGTH_LONG).show();
    } else {
      // Something else is wrong. It may be one of many other states, but all we need
      //  to know is we can neither read nor write
      mExternalStorageAvailable = mExternalStorageWriteable = false;
      Toast.makeText(me, "External Media is not Available.", Toast.LENGTH_LONG).show();
    }
    return mExternalStorageWriteable;

  }


  private String fileWrite(String gotcsv, int classID) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm");
    //get current date time with Date()
    Date date = new Date(); 
    String file_name = classID + "_" +  dateFormat.format(date) + ".csv";
    File file = new File(getExternalFilesDir(null), file_name);
    try {

      OutputStream os = new FileOutputStream(file);
      byte[] data = gotcsv.getBytes();
      Toast.makeText(me, gotcsv, Toast.LENGTH_LONG).show();
      os.write(data);
      os.close();
      Toast.makeText(me, "File written to " + file.getCanonicalPath(), Toast.LENGTH_LONG).show();
    } catch (IOException e) {
      Toast.makeText(me, "Writing to file failed.", Toast.LENGTH_LONG).show();
    }

    return file_name;
  }

  private void shareMedia(String fileName) {

    File file = new File(getExternalFilesDir(null), fileName);

    Uri uriToFile = Uri.fromFile(file);
    Intent shareIntent = new Intent();
    shareIntent.setAction(Intent.ACTION_SEND);
    shareIntent.putExtra(Intent.EXTRA_STREAM, uriToFile);
    shareIntent.setType("*/*");
    startActivity(Intent.createChooser(shareIntent, "Share Options"));
  } 


  private void addListClickListeners() {
    // Short press listener
    classSelectList.setOnItemClickListener(new OnItemClickListener() {

      public void onItemClick(AdapterView<?> parent, View view, int position,
          long id) {
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
        final View itemView = view;
        AlertDialog.Builder classOptionsAlert = new AlertDialog.Builder(view.getContext());

        PopupMenu popup = new PopupMenu(me, view);
        popup.inflate(R.menu.popup_menu);
        popup.setOnMenuItemClickListener(new OnMenuItemClickListener() {
          public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
            case R.id.export_data:
              String gotcsv = db.getCsv(x.classId);

              boolean mediaWriteable = assertMedia();
              if(mediaWriteable) {
                String filename = fileWrite (gotcsv, x.classId);
                shareMedia(filename);
              }

              return true;

            case R.id.delete_class:
              showClassDeleteDialog(itemView.getContext(), x.classId);
              return true;
            }
            return false;
          }
        });

        popup.show();
        return true;
      }
    });
  }



  private void addNewClassClickListener() {
    Button newClassButton = (Button) findViewById(R.id.class_add_button);
    newClassButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        showNewClassDialog();
      }
    });
  }

  private void showClassDeleteDialog(Context context, final int classid) {
    AlertDialog.Builder deleteClassAlert = new AlertDialog.Builder(context);
    deleteClassAlert.setTitle("");
    deleteClassAlert.setMessage("Delete this class?");
    deleteClassAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        db.deleteClass(classid);

        if (getCurrentClassId() == classid) {
          setCurrentClassId(classid);
        }
        populateClassList();
      }
    });

    deleteClassAlert.setNegativeButton("Cancel",
        new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        // Canceled.
      }
    });

    deleteClassAlert.show();
  }

  private void addPinClickListener() {
    Button changePinButton = (Button) findViewById(R.id.change_pin_button);
    changePinButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        Protector.verifyPin(me, getSharedPreferences(Constants.PREFS_NAME, 0),
            new Callable<Object>() {
          public Object call() throws Exception {
            Intent intent = new Intent(me, PinActivity.class);
            startActivity(intent);
            return null;
          }
        });
      }
    });
  }
  
  private void addEditStudentClickListener() {
    Button editStudentButton = (Button) findViewById(R.id.edit_student_button);
    editStudentButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        Intent intent = new Intent(me, EditStudentActivity.class);
        startActivity(intent);                
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
