# react-native-vision-camera-detect-faces-plugin

detect face plugin for react-native-vision-camera

## Prequisites

Install `react-native-vision-camera` and `react-native-worklets-core`

```sh
npm install react-native-vision-camera react-native-worklets-core
```
or using yarn
```sh
yarn add react-native-vision-camera react-native-worklets-core
```

Please refer to this [Documentation](https://react-native-vision-camera.com/docs/guides/frame-processors) and follow the installation instructions.


## Installation

```sh
npm install react-native-vision-camera-detect-faces-plugin
```
or using yarn
```sh
yarn add react-native-vision-camera-detect-faces-plugin
```

## Usage

```js
import React, { useEffect, useRef } from 'react';
import {
  Camera,
  useCameraDevice,
  useCameraPermission,
  useFrameProcessor
} from 'react-native-vision-camera';
import { detectFaces } from 'react-native-vision-camera-detect-faces-plugin';

// ...

const cameraRef = useRef<Camera>(null);

const { hasPermission, requestPermission } = useCameraPermission();
const cameraDevice = useCameraDevice('front');

const frameProcessor = useFrameProcessor((frame) => {
  'worklet';
  const faces = detectFaces(frame);

  if (faces.length > 0) {
    console.log(`faces in frame: ${faces[0].smilingProbability}`);
  }
}, []);

useEffect(() => {
  if (hasPermission) return;
  requestPermission();
}, []);

return (
  <Camera
    ref={cameraRef}
    style={StyleSheet.absoluteFill}
    isActive={true}
    device={cameraDevice}
    photo={true}
    frameProcessor={frameProcessor}
  />
);
```

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
