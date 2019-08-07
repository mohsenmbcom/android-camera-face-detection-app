package com.mohsenmb.facedetectiontest.detection;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;

import com.mohsenmb.facedetectiontest.R;

public class CameraActivity extends AppCompatActivity implements ActivityResolver, LifecycleOwner, FaceDetectionImageAnalyzer.FaceDetectionListener {

	private PermissionResultListener permissionListener;
	private CameraManager cameraManager;
	private View viewSnapPanel;

	@Override
	protected void onStart() {
		super.onStart();
		viewSnapPanel = findViewById(R.id.view_snap_panel);
		cameraManager = new CameraManager(this, findViewById(R.id.texture_view));
		cameraManager.setFaceDetectionListener(this);

		findViewById(R.id.view_snap_image).setOnClickListener(view -> {
			cameraManager.captureImage(image -> {
				StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
				StrictMode.setVmPolicy(builder.build());
				Intent myIntent = new Intent(Intent.ACTION_VIEW);
				myIntent.setDataAndType(Uri.fromFile(image), "image/jpg");
				startActivity(Intent.createChooser(myIntent, "Open image"));
			});
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		cameraManager.setup();
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onPause() {
		cameraManager.stop();
		super.onPause();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (permissionListener != null) {
			permissionListener.onPermissionResult(requestCode, permissions, grantResults);
		}
	}

	@Override
	public Activity resolveActivity() {
		return this;
	}

	@Override
	public LifecycleOwner resolveLifecycleOwner() {
		return this;
	}

	@Override
	public void setPermissionResultListener(PermissionResultListener listener) {
		this.permissionListener = listener;
	}

	@Override
	public void onFaceDetected(int faces) {
		findViewById(R.id.view_snap_image).setEnabled(true);
		viewSnapPanel.animate().alpha(1F).start();
	}

	@Override
	public void onNoFaceDetected() {
		findViewById(R.id.view_snap_image).setEnabled(false);
		viewSnapPanel.animate().alpha(0F).start();
	}
}