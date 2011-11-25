package main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Register extends Activity {

	 public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(com.qrmor.R.layout.register);
	        
	        Button btnRegister = (Button) findViewById(com.qrmor.R.id.btnRegister);
	        
	        btnRegister.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					// Perform action on clicks
					Intent newIntent = new Intent();
					
					// Assign textviews to the appropriate textboxes
					TextView tvUsername = (TextView) findViewById(com.qrmor.R.id.tboxRegUsername);
					TextView tvPassword = (TextView) findViewById(com.qrmor.R.id.tboxRegPassword);
					TextView tvRegCode = (TextView) findViewById(com.qrmor.R.id.tboxRegCode);
					
					// Put the textbox values into the activity Extras so we can pass them back
					// to the main screen.
					newIntent.putExtra("Register.username", tvUsername.getText().toString().trim());
					newIntent.putExtra("Register.password", tvPassword.getText().toString());
					newIntent.putExtra("Register.regcode", tvRegCode.getText().toString().trim());
					
					// Pass the itent back to the caller and signal that everything went OK
					setResult(RESULT_OK, newIntent);
					
					// There should be some sort of check to make sure it's really sent
					// so this will probably be moved or removed
					Toast.makeText(Register.this,"Phone Registration Sent!",Toast.LENGTH_LONG).show();
					
					finish();
				}
			});
	 }
}
