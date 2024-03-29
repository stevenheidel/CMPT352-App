package main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Settings extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(com.qrmor.R.layout.settings);

		Button btnSave = (Button) findViewById(com.qrmor.R.id.btnSave);

		btnSave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				Intent newIntent = new Intent();
				
				// Assign textviews to the appropriate textboxes so we can take values 
				// out of them.
				TextView tvUsername = (TextView) findViewById(com.qrmor.R.id.tboxUsername);
				TextView tvPassword = (TextView) findViewById(com.qrmor.R.id.tboxPassword);
				
				// Put the textbox values into the activity Extras so we can pass them back
				// to the main screen.
				newIntent.putExtra("Settings.username", tvUsername.getText().toString().trim());
				newIntent.putExtra("Settings.password", tvPassword.getText().toString());
				
				// Pass the itent back to the caller and signal that everything went OK
				setResult(RESULT_OK, newIntent);
				
				Toast.makeText(Settings.this,"Username/Password Saved!",Toast.LENGTH_SHORT).show();
			}
		});
		
	}
}
