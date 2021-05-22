import {NativeModules} from 'react-native';

const {LavaxZoomSDK} = NativeModules;

//to see what is loaded
console.log(NativeModules);

async function initZoom(publicKey, privateKey, domain) {
  console.log('calling zoom', LavaxZoomSDK);
  const response = await LavaxZoomSDK.initZoom(publicKey, privateKey, domain);

  console.log('Response', response);
}

async function joinMeeting(displayName = 'Stefan', meetingNo, password) {
  console.log('calling zoom - join meeting', displayName, meetingNo, password);
  const response = await LavaxZoomSDK.joinMeeting(
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
  const response = await LavaxZoomSDK.startMeeting(
    meetingNumber,
    username,
    userId,
    jwtAccessToken,
    jwtApiKey,
  );

  console.log('Response - Start Meeting', response);
}

export {initZoom, joinMeeting, startMeeting};
