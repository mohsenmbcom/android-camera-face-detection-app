package com.mohsenmb.facedetectiontest.preview;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mohsenmb.facedetectiontest.R;

public class PreviewActivity extends AppCompatActivity implements FacePreviewViewModel.OperationStateCallback {

	private FacePreviewViewModel viewModel;

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

		viewModel = new FacePreviewViewModel(this, findViewById(R.id.image_cropped_face), this);
		viewModel.cropFace(imageUri);
	}

	@Override
	public void onOperationStateChanged(FacePreviewViewModel.OperationState state) {
		String message = "";
		switch (state) {
			case InProgress:
				message = "In Progress";
				break;
			case Success:
				message = "Success!";
				break;
			case Failure:
				message = "Failed cropping the face!";
				break;
		}

		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onDestroy() {
		viewModel.destroy();
		super.onDestroy();
	}
}
