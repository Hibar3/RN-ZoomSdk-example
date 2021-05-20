import {NativeModules, NativeEventEmitter} from 'react-native';
import RNZoomUsBridge, {
  RNZoomUsBridgeEventEmitter,
} from '@mokriya/react-native-zoom-us-bridge';

//to see what is loaded
console.log(NativeModules);
const meetingEventEmitter = new NativeEventEmitter(RNZoomUsBridgeEventEmitter);

async function initZoom(publicKey, privateKey, domain) {
  console.log('calling zoom', RNZoomUsBridge);
  const response = await RNZoomUsBridge.initZoom(publicKey, privateKey, domain);

  console.log('Response', response);
}

async function joinMeeting(displayName = 'Stefan', meetingNo, password) {
  console.log('calling zoom - join meeting', displayName, meetingNo, password);
  const response = await RNZoomUsBridge.joinMeeting(
    displayName,
    meetingNo,
    password,
  );

  console.log('Response - Join Meeting', response);
}

async function startMeeting(
  meetingNumber,
  username,
  userId,
  jwtAccessToken,
  jwtApiKey,
) {
  console.log(
    'calling zoom',
    meetingNumber,
    username,
    userId,
    jwtAccessToken,
    jwtApiKey,
  );
  const response = await RNZoomUsBridge.startMeeting(
    meetingNumber,
    username,
    userId,
    jwtAccessToken,
    jwtApiKey,
  );

  console.log('Response - Start Meeting', response);
}

export {initZoom, joinMeeting, startMeeting};
