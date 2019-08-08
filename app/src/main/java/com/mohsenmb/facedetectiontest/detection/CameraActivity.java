package com.mohsenmb.facedetectiontest.detection;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;

import com.mohsenmb.facedetectiontest.R;
import com.mohsenmb.facedetectiontest.preview.PreviewActivity;

public class CameraActivity extends AppCompatActivity implements ActivityResolver, LifecycleOwner, FaceDetectionImageAnalyzer.FaceDetectionListener {

	private PermissionResultListener permissionListener;
	private CameraManager cameraManager;
	private View viewSnapPanel;
	private View viewCameraOverlay;

	@Override
	protected void onStart() {
		super.onStart();

		viewCameraOverlay = findViewById(R.id.frame_camera_overlay);

		viewSnapPanel = findViewById(R.id.view_snap_panel);
		cameraManager = new CameraManager(this, findViewById(R.id.texture_view));
		cameraManager.setFaceDetectionListener(this);

		View viewSnap = findViewById(R.id.view_snap_image);
		viewSnap.setOnClickListener(view -> {
			viewCameraOverlay.setVisibility(View.VISIBLE);
			cameraManager.captureImage(image -> runOnUiThread(() -> {
				Intent myIntent = new Intent(CameraActivity.this, PreviewActivity.class);
				myIntent.setData(Uri.fromFile(image));
				startActivity(myIntent);
			}));
		});
		cameraManager.setup();
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onPause() {
		cameraManager.pause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		viewCameraOverlay.setVisibility(View.GONE);
		cameraManager.resume();
	}

	@Override
	protected void onStop() {
		cameraManager.stop();
		super.onStop();
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
