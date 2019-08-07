package com.mohsenmb.facedetectiontest.preview;

import android.graphics.Bitmap;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.util.List;

import io.reactivex.Single;

public class PreviewFaceDetectionAnalyzer {

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

			FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

			detector.detectInImage(image)
					.addOnFailureListener(emitter::onError)
					.addOnSuccessListener(faces -> emitter.onSuccess(new BitmapDetectionResult(bitmap, faces)));
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
