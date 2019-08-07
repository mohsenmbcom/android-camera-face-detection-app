package com.mohsenmb.facedetectiontest.preview;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mohsenmb.facedetectiontest.R;

public class PreviewActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);

		Uri imageUri = getIntent().getData();
		if (imageUri == null) {
			Toast.makeText(
					this,
					R.string.cant_find_anything_to_display,
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		ImageView imageView = findViewById(R.id.image_cropped_face);
		imageView.setImageURI(imageUri);
	}
}
