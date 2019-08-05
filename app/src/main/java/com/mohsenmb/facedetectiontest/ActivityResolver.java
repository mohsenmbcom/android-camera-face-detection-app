package com.mohsenmb.facedetectiontest;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;

public interface ActivityResolver {
	Activity resolveActivity();
	LifecycleOwner resolveLifecycleOwner();

	void setPermissionResultListener(PermissionResultListener listener);

	interface PermissionResultListener{
		void onPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults);
	}
}
