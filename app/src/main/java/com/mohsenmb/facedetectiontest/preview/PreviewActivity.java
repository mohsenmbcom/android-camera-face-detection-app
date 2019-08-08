package com.mohsenmb.facedetectiontest.preview;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.mohsenmb.facedetectiontest.R;

public class PreviewActivity extends AppCompatActivity implements FacePreviewViewModel.OperationStateCallback {

	private FacePreviewViewModel viewModel;
	private View viewProgress;

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

		viewProgress = findViewById(R.id.view_progress);

		viewProgress.post(() -> {
			viewModel = new FacePreviewViewModel(this, findViewById(R.id.image_cropped_face), this);
			viewModel.cropFace(imageUri);
		});
	}

	@Override
	public void onOperationStateChanged(FacePreviewViewModel.OperationState state) {
		switch (state) {
			case InProgress:
				viewProgress.setVisibility(View.VISIBLE);
				break;
			case Success:
				viewProgress.setVisibility(View.GONE);
				break;
			case Failure:
				Toast.makeText(this, R.string.error_faild_cropping, Toast.LENGTH_LONG).show();
				break;
		}
	}

	@Override
	protected void onDestroy() {
		viewModel.destroy();
		super.onDestroy();
	}
}
