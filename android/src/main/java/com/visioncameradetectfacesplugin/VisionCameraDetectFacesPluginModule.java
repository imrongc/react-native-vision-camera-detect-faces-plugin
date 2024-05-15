package com.visioncameradetectfacesplugin;

import static java.lang.Math.ceil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrousavy.camera.core.FrameInvalidError;
import com.mrousavy.camera.core.types.Orientation;
import com.mrousavy.camera.frameprocessors.Frame;
import com.mrousavy.camera.frameprocessors.FrameProcessorPlugin;
import com.mrousavy.camera.frameprocessors.SharedArray;
import com.mrousavy.camera.frameprocessors.VisionCameraProxy;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import android.annotation.SuppressLint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.Image;
import android.util.Log;

public class VisionCameraDetectFacesPluginModule extends FrameProcessorPlugin {
  SharedArray _sharedArray;
  public static final String NAME = "VisionCameraDetectFacesPluginModule";
  public static final String TAG = "VisionCameraDetectFacesPluginModule";

  FaceDetectorOptions options =
    new FaceDetectorOptions.Builder()
      .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
      .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
      .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
      .setMinFaceSize(0.15f)
      .build();

  FaceDetector faceDetector = FaceDetection.getClient(options);

  VisionCameraDetectFacesPluginModule(@NonNull VisionCameraProxy proxy, @Nullable Map<String, Object> options) {
    super();
    _sharedArray = new SharedArray(proxy, 5);
    Log.d(TAG, "Successfully allocated SharedArray! Size: " + _sharedArray.getSize());

    ByteBuffer buffer = ByteBuffer.allocateDirect(10);
    SharedArray testArray = new SharedArray(proxy, buffer);
    Log.d(TAG, "Successfully wrapped SharedArray in ByteBuffer! Size: " + testArray.getSize());

    Log.d(TAG, "ExampleFrameProcessorPlugin initialized with options: " + options);
  }

  private Map<String, Object> processBoundingBox(Rect boundingBox) {
    Map<String, Object> bbox = new HashMap<>();

    // Calculate offset (we need to center the overlay on the target)
    Double offsetX =  (boundingBox.exactCenterX() - ceil(boundingBox.width())) / 2.0f;
    Double offsetY =  (boundingBox.exactCenterY() - ceil(boundingBox.height())) / 2.0f;

    Double x = boundingBox.right + offsetX;
    Double y = boundingBox.top + offsetY;

    bbox.put("x", boundingBox.centerX() + (boundingBox.centerX() - x));
    bbox.put("y", boundingBox.centerY() + (y - boundingBox.centerY()));
    bbox.put("width", convertToDouble(boundingBox.width()));
    bbox.put("height", convertToDouble(boundingBox.height()));

    bbox.put("boundingCenterX", convertToDouble(boundingBox.centerX()));
    bbox.put("boundingCenterY", convertToDouble(boundingBox.centerY()));
    bbox.put("boundingExactCenterX", convertToDouble(boundingBox.exactCenterX()));
    bbox.put("boundingExactCenterY", convertToDouble(boundingBox.exactCenterY()));

    return bbox;
  }

  private Map<String, Object> processFaceContours(Face face) {
    int[] faceContoursTypes =
      new int[] {
        FaceContour.FACE,
        FaceContour.LEFT_EYEBROW_TOP,
        FaceContour.LEFT_EYEBROW_BOTTOM,
        FaceContour.RIGHT_EYEBROW_TOP,
        FaceContour.RIGHT_EYEBROW_BOTTOM,
        FaceContour.LEFT_EYE,
        FaceContour.RIGHT_EYE,
        FaceContour.UPPER_LIP_TOP,
        FaceContour.UPPER_LIP_BOTTOM,
        FaceContour.LOWER_LIP_TOP,
        FaceContour.LOWER_LIP_BOTTOM,
        FaceContour.NOSE_BRIDGE,
        FaceContour.NOSE_BOTTOM,
        FaceContour.LEFT_CHEEK,
        FaceContour.RIGHT_CHEEK
      };

    String[] faceContoursTypesStrings = {
      "FACE",
      "LEFT_EYEBROW_TOP",
      "LEFT_EYEBROW_BOTTOM",
      "RIGHT_EYEBROW_TOP",
      "RIGHT_EYEBROW_BOTTOM",
      "LEFT_EYE",
      "RIGHT_EYE",
      "UPPER_LIP_TOP",
      "UPPER_LIP_BOTTOM",
      "LOWER_LIP_TOP",
      "LOWER_LIP_BOTTOM",
      "NOSE_BRIDGE",
      "NOSE_BOTTOM",
      "LEFT_CHEEK",
      "RIGHT_CHEEK"
    };

    Map<String, Object> faceContoursTypesMap = new HashMap<>();

    for (int i = 0; i < faceContoursTypesStrings.length; i++) {
      FaceContour contour = face.getContour(faceContoursTypes[i]);
      List<PointF> points = contour.getPoints();
      List<Object> pointsArray = new ArrayList<>();

      for (int j = 0; j < points.size(); j++) {
        Map<String, Object> currentPointsMap = new HashMap<>();
        
        currentPointsMap.put("x", convertToDouble(points.get(j).x));
        currentPointsMap.put("y", convertToDouble(points.get(j).y));

        pointsArray.add(currentPointsMap);
      }
      faceContoursTypesMap.put(faceContoursTypesStrings[contour.getFaceContourType() - 1], pointsArray);
    }

    return faceContoursTypesMap;
  }

  private int getFrameRotation(Orientation orientation) {
    switch (orientation) {
      case PORTRAIT:
        return 0;
      case LANDSCAPE_LEFT:
        return 90;
      case PORTRAIT_UPSIDE_DOWN:
        return 180;
      case LANDSCAPE_RIGHT:
        return 270;
      default:
        return 0; // Default to 0 degrees if orientation is unknown
    }
  }

  private Double convertToDouble(float value) {
    return (double) value;
  }

  @SuppressLint("NewApi")
  @Override
  public Object callback(@NonNull Frame frame, @Nullable Map<String, Object> params) throws FrameInvalidError {
    @SuppressLint("UnsafeOptInUsageError")
    Image mediaImage = frame.getImage();

    // Log.d(TAG, mediaImage.getWidth() + " x " + mediaImage.getHeight() + " Image with format #" + mediaImage.getFormat() + " Orientation" + frame.getOrientation());

    if (mediaImage != null) {
      InputImage image = InputImage.fromMediaImage(mediaImage, getFrameRotation(frame.getOrientation()));
      Task<List<Face>> task = faceDetector.process(image);
      // List<Map<String, Object>> facesList = new ArrayList<>();
      List<Object> facesList = new ArrayList<>();

      try {
        List<Face> faces = Tasks.await(task);
        for (Face face : faces) {
          Map<String, Object> map = new HashMap<>();
          map.put("rollAngle", convertToDouble(face.getHeadEulerAngleZ()));
          map.put("pitchAngle", convertToDouble(face.getHeadEulerAngleX()));
          map.put("yawAngle", convertToDouble(face.getHeadEulerAngleY()));
          map.put("leftEyeOpenProbability", convertToDouble(face.getLeftEyeOpenProbability()));
          map.put("rightEyeOpenProbability", convertToDouble(face.getRightEyeOpenProbability()));
          map.put("smilingProbability", convertToDouble(face.getSmilingProbability()));

          Map<String, Object> boundingBox = processBoundingBox(face.getBoundingBox());
          map.put("bounds", boundingBox);
          Map<String, Object> contours = processFaceContours(face);
          map.put("contours", contours);

          facesList.add(map);
        }
      } catch (Exception e) {
        e.printStackTrace();
      }

      return facesList;
    }

    return null;
  }
}
