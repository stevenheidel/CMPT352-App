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
		setContentView(main.namespace.R.layout.settings);

		Button btnSave = (Button) findViewById(main.namespace.R.id.btnSave);

		btnSave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Perform action on clicks
				Intent newIntent = new Intent();
				
				TextView tvUsername = (TextView) findViewById(main.namespace.R.id.tboxUsername);
				TextView tvPassword = (TextView) findViewById(main.namespace.R.id.tboxPassword);
				
				newIntent.putExtra("Settings.username", tvUsername.getText().toString().trim());
				newIntent.putExtra("Settings.password", tvPassword.getText().toString());
				
				setResult(RESULT_OK, newIntent);
				
				Toast.makeText(Settings.this,"Username/Password Saved!",Toast.LENGTH_SHORT).show();
			}
		});
		
	}
}
