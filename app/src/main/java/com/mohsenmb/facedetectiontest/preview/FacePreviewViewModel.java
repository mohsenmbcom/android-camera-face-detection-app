package com.mohsenmb.facedetectiontest.preview;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mohsenmb.facedetectiontest.R;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FacePreviewViewModel {
	private final PreviewFaceDetectionAnalyzer faceDetectionHandler;

	private Activity activity;
	private ImageView previewImageView;
	private OperationStateCallback operationStateCallback;

	private int screenWidth;

	private Disposable operationDisposable;

	public FacePreviewViewModel(@NonNull Activity activity, @NonNull ImageView previewImageView, @Nullable OperationStateCallback operationStateCallback) {
		this.activity = activity;
		this.previewImageView = previewImageView;
		this.operationStateCallback = operationStateCallback;

		faceDetectionHandler = new PreviewFaceDetectionAnalyzer();

		screenWidth = activity.getResources().getDisplayMetrics().widthPixels;
	}

	public void cropFace(Uri imageUri) {
		if (operationStateCallback != null) {
			operationStateCallback.onOperationStateChanged(OperationState.InProgress);
		}

		if (operationDisposable != null && !operationDisposable.isDisposed()) {
			operationDisposable.dispose();
		}
		operationDisposable = Single.just(imageUri)
				.map(uri -> {
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), uri);
					Matrix matrix = new Matrix();
					matrix.postRotate(90, bitmap.getWidth() / 2F, bitmap.getHeight() / 2F);
					matrix.postScale(1, -1, bitmap.getWidth() / 2F, bitmap.getHeight() / 2F);
					return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
				})
				.subscribeOn(Schedulers.io())
				.flatMap(faceDetectionHandler::analyzePhoto)
				.subscribeOn(Schedulers.io())
				.map(result -> {
					if (result.faces.isEmpty()) {
						return BitmapFactory.decodeResource(activity.getResources(), R.drawable.img_oops_not_found);
					} else {
						Bitmap bitmap = result.bitmap;
						Rect rect = result.faces.get(0).getBoundingBox();

						int x = (rect.left > 0) ? rect.left : 0;
						int y = (rect.top > 0) ? rect.top : 0;
						int width = rect.width();
						int height = rect.height();
						return Bitmap.createBitmap(bitmap,
								x,
								y,
								(x + width > bitmap.getWidth()) ? bitmap.getWidth() - x : width,
								(y + height > bitmap.getHeight()) ? bitmap.getHeight() - y : height);
					}
				})
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(bitmap -> {
					if (operationStateCallback != null) {
						operationStateCallback.onOperationStateChanged(OperationState.Success);
					}
					previewImageView.setImageBitmap(bitmap);
				}, throwable -> {
					throwable.printStackTrace();
					if (operationStateCallback != null) {
						operationStateCallback.onOperationStateChanged(OperationState.Failure);
					}
				});
	}


	void destroy() {
		if (operationDisposable != null && !operationDisposable.isDisposed()) {
			operationDisposable.dispose();
		}
	}

	enum OperationState {
		InProgress, Success, Failure
	}

	interface OperationStateCallback {
		void onOperationStateChanged(OperationState state);
	}
}
