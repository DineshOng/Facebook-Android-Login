package com.example.f4s;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private CallbackManager callbackManager;
    private static final String EMAIL = "email";
    private LoginButton loginButton;
    private FirebaseAuth mAuth;
    private TextView tv_name;
    private TextView tv_email;
    private TextView tv_id;
    private ImageView iv_pic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_name = findViewById(R.id.tv_name);
        tv_email = findViewById(R.id.tv_email);
        tv_id = findViewById(R.id.tv_id);

        mAuth = FirebaseAuth.getInstance();

        FacebookSdk.sdkInitialize(getApplicationContext());
        //AppEventsLogger.activateApp(this);

        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton) findViewById(R.id.login_button);
        //loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));
        //loginButton.getInslogInWithReadPermissions
        checkLoginStatus();

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile","user_friends","email"));

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                handleFacebookToken(loginResult.getAccessToken());
                //getFriendsList();
                //if (AccessToken.getCurrentAccessToken() != null) {
                //    RequestData();
                //}
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    AccessTokenTracker tokenTracker = new AccessTokenTracker() {
        @Override
        protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
            if(currentAccessToken != null) {
                loaduserProfile(currentAccessToken);
            } else {
                tv_name.setText("");
                tv_email.setText("");
                tv_id.setText("");
            }
        }
    };

    private  void loaduserProfile(AccessToken newAccessToken) {
        GraphRequest request = GraphRequest.newMeRequest(newAccessToken, new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                try {
                    String first_name = object.getString("first_name");
                    String last_name = object.getString("last_name");
                    String email = object.getString("email");
                    String id = object.getString("id");

                    String image_url = "graph.facebook.com/" + id +"/picture?type=normal";

                    tv_name.setText(first_name + " " + last_name);
                    tv_email.setText(email);
                    tv_id.setText(id);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        Bundle params = new Bundle();
        params.putString("fields", "first_name,last_name,email,id");
        request.setParameters(params);
        request.executeAsync();
    }

    private void handleFacebookToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    
                }
            }
        });
    }

    private void checkLoginStatus() {
        if(AccessToken.getCurrentAccessToken()!=null) {
            loaduserProfile(AccessToken.getCurrentAccessToken());
        }
    }

    private List<String> getFriendsList() {
        final List<String> friendslist = new ArrayList<String>();
        new GraphRequest(AccessToken.getCurrentAccessToken(), "/me/friends", null, HttpMethod.GET, new GraphRequest.Callback() {
            public void onCompleted(GraphResponse response) {
                /* handle the result */
                try {
                    JSONObject responseObject = response.getJSONObject();
                    JSONArray dataArray = responseObject.getJSONArray("data");

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObject = dataArray.getJSONObject(i);
                        String fbId = dataObject.getString("id");
                        String fbName = dataObject.getString("name");
                        friendslist.add(fbId);
                    }
                    List<String> list = friendslist;
                    String friends = "";
                    if (list != null && list.size() > 0) {
                        friends = list.toString();
                        if (friends.contains("[")) {
                            friends = friends.substring(1, friends.length()-1);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    //hideLoadingProgress();
                }
            }
        }).executeAsync();
        Log.e("FFFFFFFFF", String.valueOf(friendslist));
        return friendslist;
    }

    private void RequestData() {
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object,GraphResponse response) {
                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        // "/me/friends",
                        //"me/taggable_friends",
                        "me/invitable_friends",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                try {
                                    JSONArray rawName = response.getJSONObject().getJSONArray("data");
                                    //Log.e(TAG,"Json Array Length "+rawName.length());
                                    Log.e("FFFFF","Json Array "+rawName.toString());
                                    for (int i = 0; i < rawName.length(); i++) {
                                        JSONObject c = rawName.getJSONObject(i);
                                        String name = c.getString("name");
                                        Log.e("FFFFF", "JSON NAME :"+name);
                                        JSONObject phone = c.getJSONObject("picture");
                                        Log.e("FFFFF",""+phone.getString("data"));
                                        JSONObject jsonObject = phone.getJSONObject("data");
                                        String url = jsonObject.getString("url").toString();
                                        Log.e("FFFFF","@@@@"+jsonObject.getString("url").toString());
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }
                        }
                ).executeAsync();
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,email,picture");
        request.setParameters(parameters);
        request.executeAsync();
    }
}
