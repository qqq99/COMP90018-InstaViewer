package unimelb.comp90018_instaviewer.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import unimelb.comp90018_instaviewer.R;
import unimelb.comp90018_instaviewer.utilities.Authentication;
import unimelb.comp90018_instaviewer.utilities.Redirection;

public class LauncherActivity extends AppCompatActivity{

    private final String TAG = "launcher";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.login_status), this.MODE_PRIVATE);
        boolean defaultLoginStatus = getResources().getBoolean(R.bool.default_login_status);
        boolean loginStatus = sharedPref.getBoolean(getString(R.string.login_status), defaultLoginStatus);

        if (loginStatus) {
            Redirection.redirectToHome(LauncherActivity.this);
        } else {
            Redirection.redirectToLogin(LauncherActivity.this);
        }

    }

}
