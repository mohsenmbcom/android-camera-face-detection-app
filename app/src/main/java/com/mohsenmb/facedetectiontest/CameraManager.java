package com.mohsenmb.facedetectiontest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.camera.core.CameraX;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;

public class CameraManager implements ActivityResolver.PermissionResultListener {
	// region Constants
	private static final int REQUEST_CODE_PERMISSION = 1122;
	private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
	// endregion Constants

	private ActivityResolver activityResolver;
	private TextureView cameraView;
	private Size previewResolution = new Size(720, 1080);

	CameraManager(@NonNull ActivityResolver activityResolver, @NonNull TextureView cameraView) {
		this.activityResolver = activityResolver;
		activityResolver.setPermissionResultListener(this);

		this.cameraView = cameraView;
	}

	private void updateTransform() {
		Matrix matrix = new Matrix();

		// Compute the center of the view finder
		float centerX = cameraView.getWidth() / 2f;
		float centerY = cameraView.getHeight() / 2f;

		// Correct preview output to account for display rotation
		int rotation;
		switch (cameraView.getDisplay().getRotation()) {
			case Surface.ROTATION_0:
				rotation = 0;
				break;
			case Surface.ROTATION_90:
				rotation = 90;
				break;
			case Surface.ROTATION_180:
				rotation = 180;
				break;
			case Surface.ROTATION_270:
				rotation = 270;
				break;
			default:
				return;
		}

		float viewRatio = cameraView.getWidth() * 1F / cameraView.getHeight();
		float prevRatio = previewResolution.getWidth() * 1F / previewResolution.getHeight();

		// If the preview ratio is not the same as the view aspect ratio
		// this scale modification will fix that
		float scaleX;
		scaleX = 1F + Math.abs(viewRatio - prevRatio);
		matrix.postScale(scaleX, 1F, centerX, centerY);

		matrix.postRotate(-rotation, centerX, centerY);

		// Finally, apply transformations to our TextureView
		cameraView.setTransform(matrix);
	}

	void setup() {
		if (ActivityCompat.checkSelfPermission(activityResolver.resolveActivity(), CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
			cameraView.post(this::startCamera);
		} else {
			ActivityCompat.requestPermissions(
					activityResolver.resolveActivity(), new String[]{CAMERA_PERMISSION}, REQUEST_CODE_PERMISSION);
		}
	}

	void stop() {
		CameraX.unbindAll();
	}

	private void startCamera() {
		// Create configuration object for the viewfinder use case
		PreviewConfig.Builder previewConfigBuilder = new PreviewConfig.Builder();
		previewConfigBuilder
				.setLensFacing(CameraX.LensFacing.FRONT)
				.setTargetResolution(previewResolution);

		PreviewConfig previewConfig = previewConfigBuilder.build();

		// Build the viewfinder use case
		Preview preview = new Preview(previewConfig);

		// Every time the viewfinder is updated, recompute layout
		preview.setOnPreviewOutputUpdateListener(output -> {
			// To update the SurfaceTexture, we have to remove it and re-add it
			ViewGroup parent = (ViewGroup) cameraView.getParent();
			parent.removeView(cameraView);
			parent.addView(cameraView, 0);


			cameraView.setSurfaceTexture(output.getSurfaceTexture());
			updateTransform();
		});

		// Bind use cases to lifecycle
		CameraX.bindToLifecycle(activityResolver.resolveLifecycleOwner(), preview);
	}

	@Override
	public void onPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_CODE_PERMISSION) {
			if (ActivityCompat.checkSelfPermission(activityResolver.resolveActivity(), CAMERA_PERMISSION) == PackageManager.PERMISSION_GRANTED) {
				permissionGranted();
			} else {
				Toast.makeText(
						activityResolver.resolveActivity(),
						R.string.error_permission_not_granted,
						Toast.LENGTH_LONG).show();
			}
		}
	}

	private void permissionGranted() {
		setup();
	}

}
