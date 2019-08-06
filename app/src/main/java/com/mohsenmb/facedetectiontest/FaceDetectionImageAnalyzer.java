package com.mohsenmb.facedetectiontest;

import android.media.Image;
import android.util.Log;

import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

public class FaceDetectionImageAnalyzer implements ImageAnalysis.Analyzer {

	private FirebaseVisionFaceDetector detector;

	private FaceDetectionListener faceDetectionListener;

	public FaceDetectionImageAnalyzer() {
		FirebaseVisionFaceDetectorOptions visionRealtimeOptions = new FirebaseVisionFaceDetectorOptions.Builder()
				.setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
				.setLandmarkMode(FirebaseVisionFaceDetectorOptions.NO_LANDMARKS)
				.setClassificationMode(FirebaseVisionFaceDetectorOptions.NO_CLASSIFICATIONS)
				.build();
		detector = FirebaseVision.getInstance()
				.getVisionFaceDetector(visionRealtimeOptions);
	}

	private int degreesToFirebaseRotation(int degrees) {
		switch (degrees) {
			case 0:
				return FirebaseVisionImageMetadata.ROTATION_0;
			case 90:
				return FirebaseVisionImageMetadata.ROTATION_90;
			case 180:
				return FirebaseVisionImageMetadata.ROTATION_180;
			case 270:
				return FirebaseVisionImageMetadata.ROTATION_270;
			default:
				throw new IllegalArgumentException(
						"Rotation must be 0, 90, 180, or 270.");
		}
	}

	@Override
	public void analyze(ImageProxy imageProxy, int degrees) {
		if (imageProxy == null || imageProxy.getImage() == null) {
			return;
		}
		Image mediaImage = imageProxy.getImage();
		int rotation = degreesToFirebaseRotation(degrees);
		FirebaseVisionImage image =
				FirebaseVisionImage.fromMediaImage(mediaImage, rotation);


		detector.detectInImage(image)
				.addOnSuccessListener(firebaseVisionFaces -> {
					Log.d("FaceTracking", "Found " + firebaseVisionFaces.size() + " faces!");
					if (faceDetectionListener != null) {
						if (!firebaseVisionFaces.isEmpty()) {
							faceDetectionListener.onFaceDetected(firebaseVisionFaces.size());
						} else {
							faceDetectionListener.onNoFaceDetected();
						}
					}
				})
				.addOnFailureListener(Throwable::printStackTrace);
	}

	public void setFaceDetectionListener(FaceDetectionListener faceDetectionListener) {
		this.faceDetectionListener = faceDetectionListener;
	}

	interface FaceDetectionListener {
		void onFaceDetected(int faces);

		void onNoFaceDetected();
	}
}
