import React, {useEffect, useState} from 'react';
import {
  StyleSheet,
  View,
  Text,
  TouchableOpacity,
  TextInput,
  Alert,
  Platform,
} from 'react-native';
import RNZoomUsBridge from '@mokriya/react-native-zoom-us-bridge';
import {initZoom, joinMeeting} from './zoomSdk';

const ZOOM_APP_KEY = '';
const ZOOM_APP_SECRET = '';
const ZOOM_JWT_APP_KEY = '';
const ZOOM_JWT_APP_SECRET = '';
const ZOOM_DOMAIN = 'zoom.us';

const App: () => React$Node = () => {
  const [meetingId, setMeetingId] = useState('');
  const [pwd, setPwd] = useState('');
  const [username, setUsername] = useState('');
  const [userId, setUserId] = useState('');

  const initializeZoomSDK = async () => {
    if (!ZOOM_APP_KEY || !ZOOM_APP_SECRET) {
      return false;
    } else {
      // init sdk
      console.log('init');
      await RNZoomUsBridge.initialize(ZOOM_APP_KEY, ZOOM_APP_SECRET)
        .then(() => console.log('init success'))
        .catch((err) => {
          console.warn(err);
          Alert.alert('error!', err.message);
        });
    }
  };

  const onJoinMeeting = async () => {
    if (Platform.OS === 'ios') {
      await RNZoomUsBridge.joinMeeting(meetingId, username, pwd)
        .then(() => console.log('joining'))
        .catch((err) => {
          console.warn(err);
          Alert.alert('error!', err.message);
        });
    } else {
      joinMeeting(username, meetingId, pwd);
    }
  };

  useEffect(() => {
    if (Platform.OS === 'ios') initializeZoomSDK();
    else initZoom(ZOOM_APP_KEY, ZOOM_APP_SECRET, ZOOM_DOMAIN);
  });

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
          value={username}
          placeholder="Your name"
          onChangeText={(text) => setUsername(text)}
          style={styles.input}
        />
        <TouchableOpacity
          onPress={async () => onJoinMeeting()}
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
    color: 'black',
  },
});

export default App;
