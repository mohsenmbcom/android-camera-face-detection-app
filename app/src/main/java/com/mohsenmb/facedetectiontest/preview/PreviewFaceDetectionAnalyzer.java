package com.mohsenmb.facedetectiontest.preview;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;

import io.reactivex.Single;

public class PreviewFaceDetectionAnalyzer {
	// This factor is used to make the detecting image smaller, to make the process faster
	private static final int SCALING_FACTOR = 10;

	private FirebaseVisionFaceDetector detector;

	public PreviewFaceDetectionAnalyzer() {
		FirebaseVisionFaceDetectorOptions visionRealtimeOptions = new FirebaseVisionFaceDetectorOptions.Builder()
				.setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
				.setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
				.setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
				.build();

		detector = FirebaseVision.getInstance()
				.getVisionFaceDetector(visionRealtimeOptions);
	}

	public Single<BitmapDetectionResult> analyzePhoto(final Bitmap bitmap) {
		return Single.create(emitter -> {
			Bitmap smallerBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / SCALING_FACTOR, bitmap.getHeight() / SCALING_FACTOR, false);
			FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(smallerBitmap);

			detector.detectInImage(image)
					.addOnFailureListener(emitter::onError)
					.addOnSuccessListener(faces -> {
						for (FirebaseVisionFace face : faces) {
							Rect rect = face.getBoundingBox();
							rect.set(rect.left * SCALING_FACTOR, rect.top * SCALING_FACTOR, rect.right * SCALING_FACTOR, rect.bottom * SCALING_FACTOR);
						}
						emitter.onSuccess(new BitmapDetectionResult(bitmap, faces));
					});
		});
	}

	static class BitmapDetectionResult {
		final Bitmap bitmap;
		final List<FirebaseVisionFace> faces;

		public BitmapDetectionResult(Bitmap bitmap, List<FirebaseVisionFace> faces) {
			this.bitmap = bitmap;
			this.faces = faces;
		}
	}
}
