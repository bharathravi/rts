package com.example.attendeasyserver;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.Callable;

import javax.crypto.Cipher; 
import javax.crypto.spec.SecretKeySpec;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.Editable;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

public class Protector {
    private static final String ALGORITHM = "AES";
    private static final byte[] keyValue = 
        new byte[] { 'T', 'h', 'i', 's', 'I', 's', 'A', 'S', 'e', 'c', 'r', 'e', 't', 'K', 'e', 'y' };

     public static byte[] encrypt(String valueToEnc) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encValue = c.doFinal(valueToEnc.getBytes());       
        return encValue;
    }

    public static String decrypt(byte[] encryptedValue) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, key);       
        byte[] decValue = c.doFinal(encryptedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    }

    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGORITHM);
        // SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
        // key = keyFactory.generateSecret(new DESKeySpec(keyValue));
        return key;
    }
    
    public static void verifyPin(Context context, final SharedPreferences preferences, 
        Callable<Object> positiveCallable) {      
      String pin = preferences.getString(Constants.SAVED_PIN, "");
      
      if (pin.isEmpty()) {
        showNewPinDialog(context, preferences, positiveCallable);                
      } else {
        showEnterPinDialog(context, pin, positiveCallable);
      }
    }

    private static void showNewPinDialog(final Context context, final SharedPreferences preferences, 
        final Callable<Object> positiveCallable) {
      AlertDialog.Builder alert = new AlertDialog.Builder(context);
      alert.setTitle("New Pin");
      alert.setMessage("You have not set a Pin! Set a 6-digit Pin below:");
      alert.setCancelable(false);
      
      final TextView pinText = new TextView(context);
      pinText.setText("New Pin:");
      
      // Set an EditText view to get user input
      final EditText pin = new EditText(context);
      pin.setTransformationMethod(PasswordTransformationMethod.getInstance());
      pin.setGravity(Gravity.CENTER | Gravity.BOTTOM);
      pin.setEms(6);
      
      final TextView repinText = new TextView(context);
      repinText.setText("Re-enter Pin:");
      
      final EditText reenter_pin = new EditText(context);
      reenter_pin.setTransformationMethod(PasswordTransformationMethod.getInstance());
      reenter_pin.setGravity(Gravity.CENTER | Gravity.BOTTOM);
      reenter_pin.setEms(6);
            
      LinearLayout layout1 = new LinearLayout(context);
      layout1.setGravity(Gravity.TOP);
      layout1.addView(pinText);
      layout1.addView(pin);
      
      LinearLayout layout2 = new LinearLayout(context);
      layout1.setGravity(Gravity.BOTTOM);
      layout2.addView(repinText);
      layout2.addView(reenter_pin);
            
      LinearLayout layout = new LinearLayout(context);
      layout.addView(layout1);
      layout.addView(layout2);
      alert.setView(layout);
      alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          String value1 = pin.getText().toString();
          String value2 = reenter_pin.getText().toString();
          
          if (validateNewPins(value1, value2, context)) {            
            savePin(preferences, value1);
            try {
              positiveCallable.call();
            } catch (Exception e) {
              // TODO: Log
              Toast.makeText(context, "Unknown Exception!", Toast.LENGTH_LONG).show();
            }           
          } else{             
            showNewPinDialog(context, preferences, positiveCallable); 
          }          
        }                
      });

      alert.setNegativeButton("Cancel",
          new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          // TODO          
        }
      });

      alert.show();          
    }
    
    public static boolean validateNewPins(String value1, String value2, Context context) {
      if (value1.length() != 6) {
        Toast.makeText(context, "Pin must be 6 digits long!", Toast.LENGTH_LONG).show();
        return false;
      }
      
      try {
        Integer.parseInt(value1);
      } catch (NumberFormatException e) {
        Toast.makeText(context, "Pin must be a 6 digit number!", Toast.LENGTH_LONG).show();
        return false;
      }
      
      if (value1.equals(value2)) {
        return true;
      }
      
      return false;          
    }
    
    public static boolean savePin(final SharedPreferences preferences, String pin) {
      Editor editor = preferences.edit();
      
      try {
        MessageDigest x = MessageDigest.getInstance("MD5");
        String bytes = new String(x.digest(pin.getBytes()));
        editor.putString(Constants.SAVED_PIN, bytes);
        editor.commit();                
      } catch (NoSuchAlgorithmException e) {
        // TODO Log
        return false;
      }
      return true;
    }


    private static void showEnterPinDialog(final Context context, final String pin, 
        final Callable<Object> positiveCallable) {
      AlertDialog.Builder alert = new AlertDialog.Builder(context);
      alert.setTitle("Enter Pin");
      alert.setMessage("Enter your 6-digit Pin to continue");

      // Set an EditText view to get user input
      final EditText input = new EditText(context);
      
      alert.setView(input);
      alert.setCancelable(false);

      alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          String enteredPin = input.getText().toString();          
          
          try {
            MessageDigest x = MessageDigest.getInstance("MD5");
            String bytes = new String(x.digest(enteredPin.getBytes()));
            if (bytes.toString().equals(pin)) {
              positiveCallable.call();
            } else{ 
              Toast.makeText(context, "Incorrect Pin Entered", Toast.LENGTH_LONG).show(); 
            }
          } catch (NoSuchAlgorithmException e1) {
            Toast.makeText(context, "Error in Pin comparison", Toast.LENGTH_LONG).show();
            e1.printStackTrace();
          } catch (Exception e) {
            // TODO : Log
            Toast.makeText(context, "Unknown exception", Toast.LENGTH_LONG).show();
            e.printStackTrace();        
          }                             
        }
      });

      alert.setNegativeButton("Cancel",
          new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          // TODO
        }
      });

      alert.show();          
    }
}

