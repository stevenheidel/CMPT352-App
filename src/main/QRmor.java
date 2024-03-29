package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class QRmor extends Activity {
	final int REGISTER = 1;
	Boolean firstRun = true;
	
	private CharSequence csAuthString = "";
	private CharSequence csPhoneNo = "";
	private CharSequence csIMEI = "";
	private CharSequence csUUID = "";
	private CharSequence csAuthCode = "";
	private CharSequence csRegCode = "";
	private CharSequence csUsername = "";
	private CharSequence csPassword = "";
	
	SharedPreferences prefs;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(com.qrmor.R.layout.main);

		prefs = this.getSharedPreferences("prefs", MODE_PRIVATE);

		if(!prefs.getBoolean("Registered", true) 
				&& firstRun == true){
			Intent intReg = new Intent(getApplicationContext(), Register.class);
			startActivityForResult(intReg,REGISTER);
			firstRun = false;
		} else {
			// Start the QR Scanner right at the beginning.
			firstRun = false;
			IntentIntegrator.initiateScan(QRmor.this);			
		} 	
	}
	

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
	  setContentView(com.qrmor.R.layout.main);
	  firstRun = false;
	}

	// Function for retrieving the results of the ZXing scanning library.
	// The results of the QR Code are put into the text label in the app
	// as well as a "Toast notification" which appears on the screen for
	// a few seconds.
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode, intent);
		if ((result != null && resultCode == RESULT_OK) && requestCode == IntentIntegrator.REQUEST_CODE) {
			CharSequence csContent = result.getContents();
			TextView text = (TextView) findViewById(com.qrmor.R.id.txtqrResults);
			text.setInputType(InputType.TYPE_CLASS_TEXT);
			if (csContent.toString() != null) {
				Toast.makeText(QRmor.this,csContent.toString(), Toast.LENGTH_LONG).show();
				text.setText(csContent);
				try {
					GenerateAuthString();
					
					// Send the login authentication to the server
					LoginAuth login = new LoginAuth(csUUID.toString(),csIMEI.toString(),csPhoneNo.toString(),csAuthCode.toString());
					Toast.makeText(QRmor.this,"Login Sent!",Toast.LENGTH_LONG).show();
			        SendAuth("login", csContent.toString(), login);
				} catch (Exception e) {
					e.printStackTrace();
				}	
			} else {
				Toast.makeText(QRmor.this,"Failed to read QR Code or no code scanned!",Toast.LENGTH_LONG).show();
			}
		}  else if ((intent != null && resultCode == RESULT_OK) && requestCode == REGISTER){
			// Code for retrieving data from the Register screen
			Bundle extras = intent.getExtras();
	        if (extras != null) {
	            csUsername = extras.getString("Register.username");
	            csPassword = extras.getString("Register.password");
	            csRegCode = extras.getString("Register.regcode");
	            
	            if (extras.getBoolean("Register.registered")){
	            	SharedPreferences.Editor prefsEditor = prefs.edit();
	            	 prefsEditor.putBoolean("Registered", true);
	                 prefsEditor.commit();
	            }
	        }
	        
	        // Send the register information to the server
	        GenerateAuthString();
	        LoginAuth login = new LoginAuth(csUUID.toString(),csIMEI.toString(),csPhoneNo.toString(),csAuthCode.toString());
	        RegAuth reg = new RegAuth(csUsername.toString(),csPassword.toString(),csRegCode.toString());
	        SendAuth("register", reg, login);
	        
		}
	}
	
	// Creates the menu which pops up when the user presses
	// the Menu key on their phone. This is for navigating
	// to the Register and About screens.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(com.qrmor.R.menu.menu, menu);
		return true;
	}

	// Method for navigating to the appropriate screen
	// based on which option was selected in the menu.
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case com.qrmor.R.id.register:
			Intent intReg = new Intent(getApplicationContext(), Register.class);
			startActivityForResult(intReg,REGISTER);
			return true;
		case com.qrmor.R.id.about:
			Intent intAbout = new Intent(getApplicationContext(), About.class);
			startActivity(intAbout);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	// LOGIN
	static void SendAuth(String type, String url, LoginAuth login){
		HttpClient client = new DefaultHttpClient();
		// Used for testing POST requests very handy site @ http://www.posttestserver.com/
		//HttpPost hPost = new HttpPost("http://205.196.210.187/post.php?dir=kevin");
		HttpPost hPost = new HttpPost("https://qrmorserver.appspot.com/api/qrauth/" + url);
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("type", type));
        nameValuePairs.add(new BasicNameValuePair("UUID", login.getUUID()));
        nameValuePairs.add(new BasicNameValuePair("IMEI", login.getIMEI()));
        nameValuePairs.add(new BasicNameValuePair("PN", login.getPhoneNo()));
        nameValuePairs.add(new BasicNameValuePair("AC", login.getAuthCode()));       

		try {
			hPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			hPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(hPost);
			StatusLine status = response.getStatusLine();
			System.out.println("RESPONSE: " + status.getReasonPhrase());
			System.out.println("RESPONSE: "
					+ "https://qrmorserver.appspot.com/api/qrauth/" + url);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// REGISTRATION
	static void SendAuth(String type, RegAuth reg, LoginAuth login) {
		HttpClient client = new DefaultHttpClient();
		// Used for testing POST requests very handy site
		// http://www.posttestserver.com/
		// HttpPost hPost = new
		// HttpPost("http://205.196.210.187/post.php?dir=kevin");
		//HttpPost hPost = new HttpPost("http://205.196.210.187/post.php?dir=kevin");
		HttpPost hPost = new HttpPost("https://qrmorserver.appspot.com/api/register");

		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("type", type));
		nameValuePairs.add(new BasicNameValuePair("UUID", login.getUUID()));
		nameValuePairs.add(new BasicNameValuePair("IMEI", login.getIMEI()));
		nameValuePairs.add(new BasicNameValuePair("PN", login.getPhoneNo()));
		nameValuePairs.add(new BasicNameValuePair("AC", login.getAuthCode()));
		nameValuePairs.add(new BasicNameValuePair("UN", reg.getUsername()));
		nameValuePairs.add(new BasicNameValuePair("PS", reg.getPassword()));
		nameValuePairs.add(new BasicNameValuePair("RC", reg.getRegCode()));

		try {
			hPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
			hPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(hPost);
			StatusLine status = response.getStatusLine();
			System.out.println("RESPONSE: " + status.getReasonPhrase());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String GenerateAuthString(){
		// Retrieving the Android UUID
		TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		Date newDate = new Date();
		// A six digit time code which is a 100 second
		// interval
		long time = ((newDate.getTime() / 100000) % 100000);

		// Setting the phone number, IMEI, and UUID into variables
		csPhoneNo = tManager.getLine1Number();
		csIMEI = tManager.getDeviceId();
		csUUID = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
		csAuthCode = Long.toString(time);
		
		// This csAuthString will eventually be an encrypted string
		// of some length. For prototype purposes it's in plaintext.
		csAuthString = "UUID=" + csUUID.toString() + "&" +
						"IMEI=" + csIMEI.toString() + "&" +
						"PN=" + csPhoneNo.toString() + "&" +
						"AC=" + csAuthCode.toString();

		// To display the UUID
		TextView txtUUID = (TextView) findViewById(com.qrmor.R.id.txtUUIDTitle);
		txtUUID.setId(InputType.TYPE_CLASS_TEXT);
		txtUUID.setText("UUID: " + csUUID);

		// To display the IMEI
		TextView txtIMEI = (TextView) findViewById(com.qrmor.R.id.txtIMEI);
		txtIMEI.setId(InputType.TYPE_CLASS_TEXT);
		txtIMEI.setText("IMEI: " + csIMEI);

		// To display the phone number
		TextView txtPhoneNo = (TextView) findViewById(com.qrmor.R.id.txtPhoneNo);
		txtPhoneNo.setId(InputType.TYPE_CLASS_TEXT);
		txtPhoneNo.setText("Phone Number: " + csPhoneNo);
		
		// To display the Auth String
		TextView txtAuthString = (TextView) findViewById(com.qrmor.R.id.txtAuthString);
		txtAuthString.setId(InputType.TYPE_CLASS_TEXT);
		txtAuthString.setText("Auth String: " + csAuthString);
		
		return csAuthString.toString();
	}
}