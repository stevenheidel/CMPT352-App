package main;

import java.io.IOException;

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

	CharSequence csAuthString = "";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(main.namespace.R.layout.main);

		// Retrieving the Android UUID
		TelephonyManager tManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		// Setting the phone number, IMEI, and UUID into variables
		CharSequence csPhoneNo = tManager.getLine1Number();
		CharSequence csIMEI = tManager.getDeviceId();
		CharSequence csUUID = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID);
		// This csAuthString will eventually be an encrypted string
		// of some length. For prototype purposes it's in plaintext.
		csAuthString = "UUID:" + csUUID.toString() + 
									"IMEI:" + csIMEI.toString() + 
									"PHONENUM:" + csPhoneNo.toString();
		
		// Button to launch the ZXing scanner
		Button button = (Button) findViewById(main.namespace.R.id.android_button);

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
		txtAuthString.append(csAuthString);

		// Starts the ZXing scanning library which sends the user into the
		// Barcode Scanner app.
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				IntentIntegrator.initiateScan(AndroidAuthentication.this);
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
		if (result != null) {
			CharSequence csContent = result.getContents();
			TextView text = (TextView) findViewById(main.namespace.R.id.txtqrResults);
			text.setInputType(InputType.TYPE_CLASS_TEXT);
			if (csContent.toString() != null) {
				Toast.makeText(AndroidAuthentication.this, csContent.toString(), Toast.LENGTH_LONG).show();
				text.setText(csContent);
				SendAuth("login",csAuthString.toString());
				Toast.makeText(AndroidAuthentication.this, csAuthString.toString(), Toast.LENGTH_LONG).show();

			} else {
				Toast.makeText(AndroidAuthentication.this,
						"Failed to read QR Code or no code scanned!",
						Toast.LENGTH_LONG).show();
			}
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
			Intent intSettings = new Intent(getApplicationContext(), Settings.class);
			startActivity(intSettings);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	// Function for sending the authentication string to
	// the listening website. The type parameter is wheth it's
	// for registering the phone or for logging in(this may or
	// may not get changed depending on how it gets implemented.
	private void SendAuth(String type, String authString) {
		HttpParams params = new BasicHttpParams();
		params.setParameter("type", type);
		HttpClient client = new DefaultHttpClient();
		
		try {
			client.execute(new HttpHost("http://127.0.0.1"),new HttpPost(authString));
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}