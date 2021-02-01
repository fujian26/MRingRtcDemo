package com.jacob.mringrtcdemo;

import androidx.annotation.NonNull;

public interface CameraEventListener {
  void onCameraSwitchCompleted(@NonNull CameraState newCameraState);
}
