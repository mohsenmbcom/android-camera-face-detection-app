package com.mohsenmb.facedetectiontest;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;

public class CameraActivity extends AppCompatActivity implements ActivityResolver, LifecycleOwner {

	private PermissionResultListener permissionListener;
	private CameraManager cameraManager;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		cameraManager = new CameraManager(this, findViewById(R.id.texture_view));
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
}
