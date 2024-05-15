import { type Frame, VisionCameraProxy } from 'react-native-vision-camera';

const plugin = VisionCameraProxy.initFrameProcessorPlugin('detectFaces', {});

export type Point = { x: number; y: number };
export interface FaceInterface {
  leftEyeOpenProbability: number;
  rollAngle: number;
  pitchAngle: number;
  yawAngle: number;
  rightEyeOpenProbability: number;
  smilingProbability: number;
  bounds: {
    y: number;
    x: number;
    height: number;
    width: number;
    boundingCenterX: number;
    boundingCenterY: number;
    boundingExactCenterX?: number;
    boundingExactCenterY?: number;
  };
  contours: {
    FACE: Array<Point>;
    NOSE_BOTTOM: Array<Point>;
    LOWER_LIP_TOP: Array<Point>;
    RIGHT_EYEBROW_BOTTOM: Array<Point>;
    LOWER_LIP_BOTTOM: Array<Point>;
    NOSE_BRIDGE: Array<Point>;
    RIGHT_CHEEK: Array<Point>;
    RIGHT_EYEBROW_TOP: Array<Point>;
    LEFT_EYEBROW_TOP: Array<Point>;
    UPPER_LIP_BOTTOM: Array<Point>;
    LEFT_EYEBROW_BOTTOM: Array<Point>;
    UPPER_LIP_TOP: Array<Point>;
    LEFT_EYE: Array<Point>;
    RIGHT_EYE: Array<Point>;
    LEFT_CHEEK: Array<Point>;
  };
}

export function detectFaces(frame: Frame): Array<FaceInterface> {
  'worklet';
  if (plugin == null)
    throw new Error('Failed to load Frame Processor Plugin "detectFaces"!');
  return plugin.call(frame) as unknown as Array<FaceInterface>;
}
