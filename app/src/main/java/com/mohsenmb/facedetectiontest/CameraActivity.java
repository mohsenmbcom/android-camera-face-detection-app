package com.mohsenmb.facedetectiontest;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;

public class CameraActivity extends AppCompatActivity implements ActivityResolver, LifecycleOwner, FaceDetectionImageAnalyzer.FaceDetectionListener {

	private PermissionResultListener permissionListener;
	private CameraManager cameraManager;
	private View viewSnapPanel;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		viewSnapPanel = findViewById(R.id.view_snap_panel);
		cameraManager = new CameraManager(this, findViewById(R.id.texture_view));
		cameraManager.setFaceDetectionListener(this);
		cameraManager.setup();
	}

	@Override
	protected void onDestroy() {
		cameraManager.stop();
		super.onDestroy();
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
		viewSnapPanel.animate().alpha(1F).start();
	}

	@Override
	public void onNoFaceDetected() {
		viewSnapPanel.animate().alpha(0F).start();
	}
}
