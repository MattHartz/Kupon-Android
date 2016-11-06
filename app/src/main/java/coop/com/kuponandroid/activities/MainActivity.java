package coop.com.kuponandroid.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import coop.com.kuponandroid.R;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient mGoogleApiClient;
    private ImageView mImageView;

    private static final String TAG = "MainActivity";
    public static final int MEDIA_TYPE_IMAGE = 1;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 0;

    // image capture uri
    private Uri fileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        mImageView = (ImageView) findViewById(R.id.testImage);

        setSupportActionBar(toolbar);

        // Add camera floating button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivityForResult(myIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        });

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // [END build_client]

        // Loads Picasso to handle profile image
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }
        });

        // Load user account
        Intent intent = getIntent();
        GoogleSignInAccount account = intent.getParcelableExtra("account");

        // Get user variables
        String user = account.getDisplayName();
        String email = account.getEmail();
        Uri photoUri = account.getPhotoUrl();

        // Create the AccountHeader
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(new ColorDrawable(getResources().getColor(R.color.colorPrimaryDark)))
                .addProfiles(
                        new ProfileDrawerItem().withName(user).withEmail(email).withIcon(photoUri)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();

        // Build the sliding drawer
        SecondaryDrawerItem settings = new SecondaryDrawerItem()
                .withIcon(R.drawable.ic_settings_black_24dp)
                .withName(R.string.action_settings);
        SecondaryDrawerItem logout = new SecondaryDrawerItem()
                .withIcon(R.drawable.ic_exit_to_app_black_24dp)
                .withName(R.string.action_sign_out_short);
        SecondaryDrawerItem helpAndFeedback = new SecondaryDrawerItem()
                .withIcon(R.drawable.ic_help_black_24dp)
                .withName(R.string.action_help_and_feedback);

        new DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(headerResult)
                .withToolbar(toolbar)
                .withSelectedItem(-1)
                .addDrawerItems(
                        settings,
                        logout,
                        helpAndFeedback
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item
                        switch (position) {
                            case 0:
                                break;
                            case 1:
                                // sign out
                                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                                        new ResultCallback<Status>() {
                                            @Override
                                            public void onResult(Status status) {
                                                // Re-routes back to the signin page
                                                //Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                                                //mGoogleApiClient.clearDefaultAccountAndReconnect();
                                                mGoogleApiClient.disconnect();
                                                Intent myIntent = new Intent(getApplicationContext(), SignInActivity.class);
                                                startActivity(myIntent);
                                                finish();
                                            }
                                        });
                                break;
                            case 2:
                                // sign out
                                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                                        new ResultCallback<Status>() {
                                            @Override
                                            public void onResult(Status status) {
                                                // Re-routes back to the signin page
                                                //Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                                                //mGoogleApiClient.clearDefaultAccountAndReconnect();
                                                mGoogleApiClient.disconnect();
                                                Intent myIntent = new Intent(getApplicationContext(), SignInActivity.class);
                                                startActivity(myIntent);
                                                finish();
                                            }
                                        });
                                break;
                        }
                        return true;
                    }
                })
                .build();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // [START onActivityResult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from camera access
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                byte[] map = data.getByteArrayExtra("image");

                Bitmap bmp = BitmapFactory.decodeByteArray(map, 0, map.length);
                mImageView.setImageBitmap(bmp);
            }
        }

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    @Override
    public void onBackPressed() {
        //Include the code here
        super.onBackPressed();
        return;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item= menu.findItem(R.id.action_settings);
        item.setVisible(false);
        super.onPrepareOptionsMenu(menu);
        return true;
    }

    /** Create a file Uri for saving an image or video */
    private static Uri getOutputMediaFileUri(int type){
        File mediaFile = getOutputMediaFile(type);

        if (mediaFile != null) {
            return Uri.fromFile(mediaFile);
        }
        return null;
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }
}
