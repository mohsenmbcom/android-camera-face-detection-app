package com.mohsenmb.facedetectiontest.detection;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Rational;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.core.app.ActivityCompat;

import com.mohsenmb.facedetectiontest.R;

import java.io.File;

public class CameraManager implements ActivityResolver.PermissionResultListener {
	// region Constants
	private static final int REQUEST_CODE_PERMISSION = 1122;
	private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;

	public static final String SAVING_IMAGE_NAME = "temp_captured_image.jpg";
	// endregion Constants

	private ActivityResolver activityResolver;
	private TextureView cameraView;

	private FaceDetectionImageAnalyzer.FaceDetectionListener faceDetectionListener;

	private ImageCapture imageCapture;


	CameraManager(@NonNull ActivityResolver activityResolver, @NonNull TextureView cameraView) {
		this.activityResolver = activityResolver;
		activityResolver.setPermissionResultListener(this);

		this.cameraView = cameraView;
	}

	private void updateTransform(Size textureSize) {
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

		int previewHeight = Math.max(textureSize.getWidth(), textureSize.getHeight());
		int previewWidth = Math.min(textureSize.getWidth(), textureSize.getHeight());

		// If the preview ratio is not the same as the view aspect ratio
		// this scale modification will fix that
		int outputWidth, outputHeight;
		if (previewWidth * cameraView.getHeight() > previewHeight * cameraView.getWidth()) {
			outputWidth = cameraView.getWidth() * previewHeight / cameraView.getHeight();
			outputHeight = previewHeight;
		} else {
			outputWidth = previewWidth;
			outputHeight = cameraView.getHeight() * previewWidth / cameraView.getWidth();
		}

		matrix.setScale(previewWidth * 1F / outputWidth, previewHeight * 1F / outputHeight, centerX, centerY);


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

	void captureImage(@NonNull final ImageCaptureCallback imageCaptureCallback) {
		if (imageCapture != null) {

			File file = new File(activityResolver.resolveActivity().getCacheDir(), SAVING_IMAGE_NAME);
			imageCapture.takePicture(file, new ImageCapture.OnImageSavedListener() {
				@Override
				public void onImageSaved(@NonNull File file) {
					imageCaptureCallback.onImageCaptured(file);
				}

				@Override
				public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
					Toast.makeText(
							activityResolver.resolveActivity(),
							message,
							Toast.LENGTH_LONG).show();
				}
			});
		} else {
			Toast.makeText(
					activityResolver.resolveActivity(),
					"You may need to grant the camera permission!",
					Toast.LENGTH_LONG).show();
		}
	}

	void stop() {
		CameraX.unbindAll();
	}

	private void startCamera() {
		// Bind use cases to lifecycle
		CameraX.bindToLifecycle(activityResolver.resolveLifecycleOwner(), createPreviewUseCase(), createImageCaptureUseCase(), createAnalyzerUseCase());
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

	private Preview createPreviewUseCase() {
		PreviewConfig.Builder previewConfigBuilder = new PreviewConfig.Builder();
		previewConfigBuilder
				.setLensFacing(CameraX.LensFacing.FRONT);

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
			updateTransform(output.getTextureSize());
		});
		return preview;
	}

	private ImageCapture createImageCaptureUseCase() {
		ImageCaptureConfig.Builder imageCaptureConfig = new ImageCaptureConfig.Builder();
		imageCaptureConfig.setCaptureMode(ImageCapture.CaptureMode.MAX_QUALITY);
		imageCaptureConfig.setLensFacing(CameraX.LensFacing.FRONT);

		// Build the image capture use case and attach button click listener
		imageCapture = new ImageCapture(imageCaptureConfig.build());

		return imageCapture;
	}

	private ImageAnalysis createAnalyzerUseCase() {
		ImageAnalysisConfig.Builder analyzerConfig = new ImageAnalysisConfig.Builder();
		// Use a worker thread for image analysis to prevent glitches
		HandlerThread analyzerThread = new HandlerThread("FaceDetectionAnalyzer");
		analyzerThread.start();
		analyzerConfig.setCallbackHandler(new Handler(analyzerThread.getLooper()));
		// In our analysis, we care more about the latest image than
		// analyzing *every* image
		analyzerConfig.setImageReaderMode(
				ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE);

		// To have a pretty quick analysis a resolution is enough.
		Size analyzeResolution = new Size(108, 192);
		analyzerConfig.setTargetResolution(analyzeResolution);
		analyzerConfig.setTargetAspectRatio(new Rational(analyzeResolution.getWidth(), analyzeResolution.getHeight()));
		analyzerConfig.setLensFacing(CameraX.LensFacing.FRONT);

		FaceDetectionImageAnalyzer analyzer = new FaceDetectionImageAnalyzer();
		analyzer.setFaceDetectionListener(new FaceDetectionImageAnalyzer.FaceDetectionListener() {
			// This variable saves the last amount of the faces
			// has seen by the analyzer and avoids calling the callback every time
			int lastDetectedFaces = 0;

			@Override
			public void onFaceDetected(int faces) {
				if (faceDetectionListener != null) {
					if (lastDetectedFaces != faces) {
						lastDetectedFaces = faces;
						faceDetectionListener.onFaceDetected(faces);
					}
				}
			}

			@Override
			public void onNoFaceDetected() {
				if (faceDetectionListener != null) {
					if (lastDetectedFaces > 0) {
						lastDetectedFaces = 0;
						faceDetectionListener.onNoFaceDetected();
					}
				}
			}
		});
		ImageAnalysis analyzerUseCase = new ImageAnalysis(analyzerConfig.build());
		analyzerUseCase.setAnalyzer(analyzer);
		return analyzerUseCase;
	}

	public void removeFaceDetectionListener() {
		this.faceDetectionListener = null;
	}

	public void setFaceDetectionListener(FaceDetectionImageAnalyzer.FaceDetectionListener faceDetectionListener) {
		this.faceDetectionListener = faceDetectionListener;
	}

	interface ImageCaptureCallback {
		void onImageCaptured(File image);
	}
}
