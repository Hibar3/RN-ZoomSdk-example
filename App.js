import React, {Component, useEffect, useState} from 'react';
import {
  StyleSheet,
  Text,
  View,
  TextInput,
  TouchableOpacity,
  NativeModules,
  Alert,
  NativeEventEmitter,
} from 'react-native';

import RNZoomUsBridge, {
  RNZoomUsBridgeEventEmitter,
} from '@mokriya/react-native-zoom-us-bridge';

const ZOOM_APP_KEY = 'NWLQKZMr5mQfP7X7BkEvVXIY9OIhnhNZEPu9';
const ZOOM_APP_SECRET = 'P4h5IdJdloZAqiIKva4Sod6g4BfbZ25J8Ywj';
const ZOOM_JWT_APP_KEY = 'Yb2jTA6wRXWhokBBfcpllg';
const ZOOM_JWT_APP_SECRET = 'dWSYjhoFT8hosXR7ZYhhBrFy9CXAVRDgJjYJ';

console.log(NativeModules, 'sien');
const App: () => React$Node = () => {
  const [state, setState] = useState({
    meetingPassword: '',
    meetingTitle: '',
    userName: 'demo',
    userEmail: '',
    userId: '',
    accessToken: '',
    userZoomAccessToken: '',
    meetingCreated: false,
  });
  const [meetingId, setMeetingId] = useState('94991086357');

  useEffect(() => {
    initializeZoomSDK();
  }, []);

  const initializeZoomSDK = () => {
    if (!ZOOM_APP_KEY || !ZOOM_APP_SECRET) {
      return false;
    } else {
      // init sdk
      console.log('init');
      RNZoomUsBridge.initialize(ZOOM_APP_KEY, ZOOM_APP_SECRET)
        .then(() => console.log('init success'))
        .catch((err) => {
          console.warn(err);
          Alert.alert('error!', err.message);
        });
    }
  };

  const joinMeeting = async () => {
    const {userName, meetingPassword} = state;

    RNZoomUsBridge.joinMeeting('94991086357', userName, meetingPassword)
      .then(() => console.log('joining'))
      .catch((err) => {
        console.warn(err);
        Alert.alert('error!', err.message);
      });
  };

  const createAccessToken = async () => {
    // to talk to ZOOM API you will need access token
    if (!ZOOM_JWT_APP_KEY || !ZOOM_JWT_APP_SECRET) return false;
    const accessToken = await RNZoomUsBridge.createJWT(
      ZOOM_JWT_APP_KEY,
      ZOOM_JWT_APP_SECRET,
    )
      .then()
      .catch((err) => console.log(err));

    console.log(`createAccessToken ${accessToken}`);

    if (state.accessToken) setState({accessToken});
  };

  const getUserID = async (userEmail, accessToken) => {
    const fetchURL = `https://api.zoom.us/v2/users/${userEmail}`;
    const userResult = await fetch(fetchURL, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
    })
      .then((response) => response.json())
      .then((json) => {
        return json;
      })
      .catch((error) => {
        console.error(error);
      });

    console.log('userResult', userResult);

    if (userResult && userResult.code === 429) {
      // rate error try again later
      Alert.alert('API Rate error try again in a few seconds');
    }

    if (userResult && userResult.id && userResult.status === 'active') {
      // set user id
      const {id: userId} = userResult;

      setState({userId});

      return userId;
    }

    return false;
  };

  const createUserZAK = async (userId, accessToken) => {
    const fetchURL = `https://api.zoom.us/v2/users/${userId}/token?type=zak`;
    const userZAKResult = await fetch(fetchURL, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${accessToken}`,
        'Content-Type': 'application/json',
      },
    })
      .then((response) => response.json())
      .then((json) => {
        return json;
      })
      .catch((error) => {
        console.error(error);
      });

    console.log('userZAKResult', userZAKResult);

    if (userZAKResult && userZAKResult.code === 429) {
      // rate error try again later
      Alert.alert('API Rate error try again in a few seconds');
    }

    if (userZAKResult && userZAKResult.token) {
      // set user id
      const {token} = userZAKResult;

      setState({
        userZoomAccessToken: token,
      });

      return token;
    }

    return false;
  };

  return (
    <View style={styles.container}>
      <Text style={styles.welcome}>☆RNZoomUsBridge example☆</Text>
      <View>
        <TextInput
          value={meetingId}
          placeholder="Meeting ID"
          onChangeText={(text) => setMeetingId(text)}
          style={styles.input}
        />
        <TextInput
          value={state.userName}
          placeholder="Your name"
          onChangeText={(text) => setState({userName: text})}
          style={styles.input}
        />
        <TouchableOpacity
          onPress={async () => joinMeeting()}
          style={styles.button}>
          <Text style={styles.buttonText}>Join Meeting</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  welcome: {
    fontSize: 20,
    textAlign: 'center',
    margin: 10,
  },
  input: {
    width: 200,
    padding: 10,
    borderRadius: 5,
    borderWidth: 1,
    borderColor: 'grey',
    color: `black`,
    margin: 3,
  },
  button: {
    width: 200,
    padding: 10,
    borderRadius: 5,
    backgroundColor: 'salmon',
    alignItems: 'center',
    justifyContent: 'center',
    margin: 3,
  },
  buttonText: {
    color: `black`,
  },
});

export default App;
