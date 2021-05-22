package com.rnzoomusbridgeexample;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;

import us.zoom.sdk.JoinMeetingOptions;
import us.zoom.sdk.JoinMeetingParams;
import us.zoom.sdk.MeetingError;
import us.zoom.sdk.MeetingService;
import us.zoom.sdk.MeetingServiceListener;
import us.zoom.sdk.MeetingStatus;
import us.zoom.sdk.StartMeetingOptions;
import us.zoom.sdk.StartMeetingParamsWithoutLogin;
import us.zoom.sdk.ZoomError;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdk.ZoomSDKInitParams;
import us.zoom.sdk.ZoomSDKInitializeListener;

public class ZoomManager extends ReactContextBaseJavaModule  implements ZoomSDKInitializeListener, MeetingServiceListener {

    private final ReactApplicationContext reactContext;
    private Promise initPromise;
    private Promise meetingPromise;

    ZoomManager(ReactApplicationContext context) {
        super(context);
        this.reactContext = context;
    }

    @NonNull
    @Override
    public String getName() {
        // this defines name of how we will import this module in JS
        return "LavaxZoomSDK";
    }


    @ReactMethod
    public void initZoom(String publicKey, String privateKey, String domain, Promise promise) {
        Log.d(this.getName(), "Init zoom: " + publicKey
                + " and privateKey: " + privateKey + " domain: " + domain);

        try {
            initPromise = promise;
            this.getReactApplicationContext().getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    ZoomSDK zoomSDK = ZoomSDK.getInstance();
                    ZoomSDKInitParams initParams = new ZoomSDKInitParams();
                    initParams.appKey = publicKey;
                    initParams.appSecret = privateKey;
                    initParams.enableGenerateDump = true;
                    initParams.enableLog = true;
                    initParams.domain = domain;
                    zoomSDK.initialize(reactContext.getCurrentActivity(),ZoomManager.this, initParams);
                }
            });
        } catch (Exception e) {
            Log.e("ERR_UNEXPECTED_EXCEPTIO", e.getMessage());
            promise.reject("ERR_UNEXPECTED_EXCEPTIO", e);
        }

    }

    @Override
    public void onZoomSDKInitializeResult(int errorCode, int internalErrorCode) {
        Log.d(this.getName(), "Init Zoom Result with : errorCode " + errorCode
                + " and internalErrorCode: " + internalErrorCode);

        if(errorCode == ZoomError.ZOOM_ERROR_SUCCESS) {
            Log.d(this.getName(), "Initializing meeting service SUCCESSFUL");
            ZoomSDK zoomSDK = ZoomSDK.getInstance();
            MeetingService meetingService = zoomSDK.getMeetingService();
            if(meetingService != null) {
                Log.d(this.getName(), "Adding listener for meeting service ");
                meetingService.addListener(this);
            }
            //here we should notify JS
            initPromise.resolve("Zoom initialized");
        }
    }

    @Override
    public void onZoomAuthIdentityExpired() {

    }

    @Override
    public void onMeetingStatusChanged(MeetingStatus meetingStatus, int errorCode, int internalErrorCode) {
        Log.d(this.getName(), "Meeting Status Changed  meetingStatus : " + meetingStatus
                + "errorCode: "  + errorCode + " and internalErrorCode: " + internalErrorCode);

        // custom handler for meeting status
        switch(meetingStatus) {
            case MEETING_STATUS_FAILED: {
                break;
            }
            case MEETING_STATUS_DISCONNECTING:
            case MEETING_STATUS_UNKNOWN:
            case MEETING_STATUS_WEBINAR_DEPROMOTE:
            case MEETING_STATUS_WEBINAR_PROMOTE:
            case MEETING_STATUS_RECONNECTING:
            case MEETING_STATUS_IN_WAITING_ROOM:
            case MEETING_STATUS_INMEETING:
            case MEETING_STATUS_WAITINGFORHOST:
            case MEETING_STATUS_CONNECTING:
            case MEETING_STATUS_IDLE:
                break;
        }
    }

    @ReactMethod
    public void joinMeeting(String displayName, String meetingNumber, String password, Promise promise) {
        Log.d("ou shit", "Join meeting called : displayName " + displayName
                + " and meetingNumber: " + meetingNumber + " password" + password);

        ZoomSDK zoomSDK = ZoomSDK.getInstance();

        if(!zoomSDK.isInitialized()) {
            promise.reject("ERR_ZOOM_START", "ZoomSDK has not been initialized successfully");
            return;
        }

        MeetingService meetingService = zoomSDK.getMeetingService();
        if(meetingService == null) {
            promise.reject("ERR_ZOOM_START", "Zoom MeetingService has not been initialized successfully");
            return;
        }

        JoinMeetingOptions opts = new JoinMeetingOptions();

        JoinMeetingParams params = new JoinMeetingParams();

        params.displayName = displayName;
        params.meetingNo = meetingNumber;
        params.password = password;
        try {
            int joinMeetingResult = meetingService.joinMeetingWithParams(this.reactContext, params,opts);
            Log.i(this.getName(), "joinMeeting, joinMeetingResult=" + joinMeetingResult);
            if (joinMeetingResult != MeetingError.MEETING_ERROR_SUCCESS) {
                promise.reject("ERR_ZOOM_JOIN", "joinMeeting, errorCode=" + joinMeetingResult);
            }
            meetingPromise = promise;

        } catch (Exception e) {
            promise.reject("JoinMeetingException", e);
        }
    }


    @ReactMethod
    public void startMeeting(String meetingNumber, String username, String userId, String jwtAccessToken, String jwtApiSecretKey, Promise promise) {
        Log.d(this.getName(), "Start meeting called  with  meetingNumber: " + meetingNumber
                + " and username: " + username + " userId " + userId + "jwtAccessToken" + jwtAccessToken + "jwtApiKey" + jwtApiSecretKey);
        try {
            meetingPromise = promise;

            ZoomSDK zoomSDK = ZoomSDK.getInstance();
            if(!zoomSDK.isInitialized()) {
                promise.reject("ERR_ZOOM_START", "ZoomSDK has not been initialized successfully");
                return;
            }

            final MeetingService meetingService = zoomSDK.getMeetingService();
            if(meetingService.getMeetingStatus() != MeetingStatus.MEETING_STATUS_IDLE) {
                long lMeetingNo = 0;
                try {
                    lMeetingNo = Long.parseLong(meetingNumber);
                } catch (NumberFormatException e) {
                    promise.reject("ERR_ZOOM_START", "Invalid meeting number: " + meetingNumber);
                    return;
                }

                if(meetingService.getCurrentRtcMeetingNumber() == lMeetingNo) {
                    meetingService.returnToMeeting(reactContext.getCurrentActivity());
                    promise.resolve("Already joined zoom meeting");
                    return;
                }
            }

            StartMeetingOptions opts = new StartMeetingOptions();
            StartMeetingParamsWithoutLogin params = new StartMeetingParamsWithoutLogin();
            params.displayName = username;
            params.meetingNo = meetingNumber;
            params.userId = userId;
            params.userType = MeetingService.USER_TYPE_API_USER;
            // ZAK - Zoom Access Token
            String zak = this.getZak(userId, jwtAccessToken, jwtApiSecretKey);

            if(zak == null){
                promise.reject("StartMeetingFail", "ZAK is null or was not retrieved");
                return;
            }

            params.zoomAccessToken = zak;

            int startMeetingResult = meetingService.startMeetingWithParams(reactContext.getCurrentActivity(), params, opts);
            Log.i(this.getName(), "startMeeting, startMeetingResult=" + startMeetingResult);

            if (startMeetingResult != MeetingError.MEETING_ERROR_SUCCESS) {
                promise.reject("ERR_ZOOM_START", "startMeeting, errorCode=" + startMeetingResult);
            }
            promise.resolve("Meeting Started");
        } catch (Exception ex) {
            promise.reject("ERR_UNEXPECTED_EXCEPTION", ex);
        }
    }


    // put in into something like ZakUtils class to make it clean ??
    private String getZak(String userId, String jwtApiKey, String jwtApiSecret){

        String jwtAccessToken = this.createJWTAccessToken(jwtApiKey, jwtApiSecret);
        String zak = this.getZoomAccessToken(userId, jwtAccessToken);
        return zak;
    }

    public String getZoomAccessToken(String userId, String jwtAccessToken) {
        // String jwtAccessToken = createJWTAccessToken();

        if(jwtAccessToken == null || jwtAccessToken.isEmpty())
            return null;
        // Create connection
        try {
            URL zoomTokenEndpoint = new URL("https://api.zoom.us/v2/users/" + userId + "/token?type=zak&access_token=" + jwtAccessToken);
            HttpsURLConnection connection = (HttpsURLConnection) zoomTokenEndpoint.openConnection();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                InputStream responseBody = connection.getInputStream();
                InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                BufferedReader streamReader = new BufferedReader(responseBodyReader);
                StringBuilder responseStrBuilder = new StringBuilder();

                //get JSON String
                String inputStr;
                while ((inputStr = streamReader.readLine()) != null)
                    responseStrBuilder.append(inputStr);

                connection.disconnect();
                JSONObject jsonObject = new JSONObject(responseStrBuilder.toString());
                return jsonObject.getString("token");
            } else {
                Log.d(this.getName(), "error in connection");
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e){
            e.printStackTrace();
        }
        return null;
    }

    public String createJWTAccessToken(final String API_KEY, final String API_SECRET) {

        long EXPIRED_TIME= 3600 * 2;
        long time=System.currentTimeMillis()/1000  + EXPIRED_TIME;

        String header = "{\"alg\": \"HS256\", \"typ\": \"JWT\"}";
        String payload = "{\"iss\": \"" + API_KEY + "\"" + ", \"exp\": " + String.valueOf(time) + "}";
        try {
            String headerBase64Str = Base64.encodeToString(header.getBytes("utf-8"), Base64.NO_WRAP| Base64.NO_PADDING | Base64.URL_SAFE);
            String payloadBase64Str = Base64.encodeToString(payload.getBytes("utf-8"), Base64.NO_WRAP| Base64.NO_PADDING | Base64.URL_SAFE);
            final Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(API_SECRET.getBytes(), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] digest = mac.doFinal((headerBase64Str + "." + payloadBase64Str).getBytes());

            return headerBase64Str + "." + payloadBase64Str + "." + Base64.encodeToString(digest, Base64.NO_WRAP| Base64.NO_PADDING | Base64.URL_SAFE);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }
}