package main;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.*;
import android.text.InputType;
import android.provider.Settings.Secure;

public class AndroidAuthentication extends Activity {

	final int SETTINGS = 0;
	final int REGISTER = 1;
	
	private CharSequence csAuthString = "";
	private CharSequence csUsername = "";
	private CharSequence csPassword = "";
	private CharSequence csPhoneNo = "";
	private CharSequence csIMEI = "";
	private CharSequence csUUID = "";
	private CharSequence csAuthCode = "";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(main.namespace.R.layout.main);
		
		// Button to launch the ZXing scanner
		Button button = (Button) findViewById(main.namespace.R.id.launchQR);
		// Button for generating the Auth String (for testing purposes not for release)
		Button btnGenAuth = (Button) findViewById(main.namespace.R.id.btnGenAuth);

		// Starts the ZXing scanning library which sends the user into the
		// Barcode Scanner app.
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				IntentIntegrator.initiateScan(AndroidAuthentication.this);
			}
		});
		
		// Generates the Auth String
		btnGenAuth.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				csAuthCode = GenerateAuthString();
				
				try {
					String authString = new String(Encrypt(csAuthCode.toString()));
					// To display the Encrypted Auth String
					TextView txtEncString = (TextView) findViewById(main.namespace.R.id.txtEncString);
					txtEncString.setId(InputType.TYPE_CLASS_TEXT);
					txtEncString.append(authString);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
	}

	// Function for retrieving the results of the ZXing scanning library.
	// The results of the QR Code are put into the text label in the app
	// as well as a "Toast notification" which appears on the screen for
	// a few seconds.
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult result = IntentIntegrator.parseActivityResult(requestCode,
				resultCode, intent);
		if ((result != null && resultCode == RESULT_OK) && requestCode == IntentIntegrator.REQUEST_CODE) {
			CharSequence csContent = result.getContents();
			TextView text = (TextView) findViewById(main.namespace.R.id.txtqrResults);
			text.setInputType(InputType.TYPE_CLASS_TEXT);
			if (csContent.toString() != null) {
				Toast.makeText(AndroidAuthentication.this,
						csContent.toString(), Toast.LENGTH_LONG).show();
				text.setText(csContent);
				SendAuth("login", csAuthString.toString());

			} else {
				Toast.makeText(AndroidAuthentication.this,
						"Failed to read QR Code or no code scanned!",
						Toast.LENGTH_LONG).show();
			}
		} else if ((result != null && resultCode == RESULT_OK) && requestCode == SETTINGS){
			Bundle intExtras = intent.getExtras();
			if (intExtras != null) {
				csUsername = intExtras.getString("Settings.username");
				csPassword = intExtras.getString("Settings.password");
			} else {
				csUsername = "null";
				csPassword = "null";
			}
		} else if ((result != null && resultCode == RESULT_OK) && requestCode == REGISTER){
			// Code for retrieving data from the Register screen
		}
	}
	
	// Creates the menu which pops up when the user presses
	// the Menu key on their phone. This is for navigating
	// to the Register and About screens.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(main.namespace.R.menu.menu, menu);
		return true;
	}

	// Method for navigating to the appropriate screen
	// based on which option was selected in the menu.
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case main.namespace.R.id.register:
			Intent intReg = new Intent(getApplicationContext(), Register.class);
			startActivity(intReg);
			return true;
		case main.namespace.R.id.about:
			Intent intAbout = new Intent(getApplicationContext(), About.class);
			startActivity(intAbout);
			return true;
		case main.namespace.R.id.settings:
			Intent intSettings = new Intent(this, Settings.class);
			startActivityForResult(intSettings,SETTINGS);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	// Function for sending the authentication string to
	// the listening website. The type parameter is whether it's
	// for registering the phone or for logging in(this may or
	// may not change depending on how it gets implemented.
	private void SendAuth(String type, String authString) {
		HttpParams params = new BasicHttpParams();
		params.setParameter("type", type);
		HttpClient client = new DefaultHttpClient();
		
		if (type.equals("login")) {
			try {
				client.execute(new HttpHost(
						"http://cmpt352server.appspot.com/cmpt352_server"),new HttpPost(authString));

			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			
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
									"UN=" + csUsername.toString() + "&" + 
									"PS=" + csPassword.toString() + "&" +
									"AC=" + csAuthCode.toString();

		// To display the UUID
		TextView txtUUID = (TextView) findViewById(main.namespace.R.id.txtUUIDTitle);
		txtUUID.setId(InputType.TYPE_CLASS_TEXT);
		txtUUID.append(csUUID);

		// To display the IMEI
		TextView txtIMEI = (TextView) findViewById(main.namespace.R.id.txtIMEI);
		txtIMEI.setId(InputType.TYPE_CLASS_TEXT);
		txtIMEI.append(csIMEI);

		// To display the phone number
		TextView txtPhoneNo = (TextView) findViewById(main.namespace.R.id.txtPhoneNo);
		txtPhoneNo.setId(InputType.TYPE_CLASS_TEXT);
		txtPhoneNo.append(csPhoneNo);
		
		// To display the Auth String
		TextView txtAuthString = (TextView) findViewById(main.namespace.R.id.txtAuthString);
		txtAuthString.setId(InputType.TYPE_CLASS_TEXT);
		txtAuthString.setText("Auth String: " + csAuthString);
		
		return csAuthString.toString();
	}
	
	// Encrypts the parameter string (will be the Auth String) with AES
	// encryption and returns it as a byte array.
	private byte[] Encrypt(String newString) throws Exception{
		System.out.println("TEST Original: " + newString);
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(128);
		
		SecretKey sKey = keyGen.generateKey();
		SecretKeySpec sKeySpec = new SecretKeySpec(sKey.getEncoded(),"AES");
		
		Cipher cipher = Cipher.getInstance("AES");
		cipher.init(Cipher.ENCRYPT_MODE, sKeySpec);
		
		byte[] encString = cipher.doFinal(newString.getBytes());
		System.out.println("TEST Encrypted: " + encString.toString());
		
		// Decrypt it for testing purposes. Will be removed upon release/handin
		cipher.init(Cipher.DECRYPT_MODE, sKeySpec);
		byte[] original = cipher.doFinal(encString);
		
		String output = new String(original);
		System.out.println("TEST Decrypted: " + output);

		return encString;
	}

}