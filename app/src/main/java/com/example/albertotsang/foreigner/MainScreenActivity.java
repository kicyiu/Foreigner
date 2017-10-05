package com.example.albertotsang.foreigner;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import android.app.ProgressDialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.albertotsang.foreigner.swipecard.FlingCardListener;
import com.example.albertotsang.foreigner.swipecard.SwipeFlingAdapterView;

import java.lang.annotation.Target;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class MainScreenActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, FlingCardListener.ActionDownInterface {

    protected static final String TAG = "MainScreenActivity";

    private TextView info;
    private JSONObject json_user;
    private String stringUrl;
    private Context context;

    private ProgressDialog progressDialog;


    // Keys for storing activity state in the Bundle.
    protected final static String LOCATION_KEY = "location-key";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest mLocationRequest;

    /**
     * Represents a geographical location.
     */
    protected Location mLastLocation;
   // protected Location mCurrentLocation;

    private final int SPLASH_DISPLAY_LENGTH = 5000;

    //These variable are for Swipe Card use
    public static MyAppAdapter myAppAdapter;
    public static ViewHolder viewHolder;
    private ArrayList<SwipeCardData> al;
    private SwipeFlingAdapterView flingContainer;

    static public User loginUser = new User(null, null, null, null, null, null, null);
    static public List<User> users = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        //info = (TextView) findViewById(R.id.info);

        //stringUrl = "http://www.andreseloysv.com/proyectos_alberto/Foreigner_Scripts/login_pdo.php";
        stringUrl = "http://192.168.0.10/Foreigner_Scripts/login.php";

        //init progress dialog
        progressDialog = ProgressDialog.show(this, "Conectando", "Iniciando sesion", true, true);



        // Kick off the process of building a GoogleApiClient and requesting the LocationServices
        // API.
        buildGoogleApiClient();

        //this handler delays execution for 5 seconds
        new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                //nothing
            }

            public void onFinish() {

                //store cunrrent AccessToken in json_user object
                GraphRequest request = GraphRequest.newMeRequest(
                        AccessToken.getCurrentAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {


                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                try {
                                    object.put("latitude", String.valueOf(mLastLocation.getLatitude()));
                                    object.put("longitude", String.valueOf(mLastLocation.getLongitude()));

                                    loginUser.setIdUser(object.getString("id"));
                                    loginUser.setName(object.getString("first_name"));
                                    loginUser.setGender(object.getString("gender"));
                                    loginUser.setDob(convertStringTimeStamp(object.getString("birthday")));


                                    JSONObject json_edu;
                                    //Read Object in order to get user education
                                    for(int i = 0; i < object.getJSONArray("education").length(); i++) {
                                        json_edu = object.getJSONArray("education").getJSONObject(i);
                                        if(json_edu.getString("type").equals("College")) {
                                            loginUser.setEducation(json_edu.getJSONObject("school").getString("name"));
                                        }
                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Log.d("Prueba", "Objeto de Facebook Json: " + object);
                                json_user = object;

                                //After I get my user object I going to connect to my dabatabase and check user existence
                                new DownloadWebpageTask("string_decode").execute(stringUrl);

                                //stringUrl = "http://www.andreseloysv.com/proyectos_alberto/Foreigner_Scripts/get_users.php";
                                stringUrl = "http://192.168.0.10/Foreigner_Scripts/get_users.php";
                                //Connect to server to get users data to show on swipe card
                                new DownloadWebpageTask("json_decode").execute(stringUrl);

                            }
                        }
                );

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id, first_name, gender, birthday, education");
                request.setParameters(parameters);
                request.executeAsync();
            }

        }.start();


        /**
         * Once I have user data I going to start to load swipe card data
         */

        flingContainer = (SwipeFlingAdapterView) findViewById(R.id.frame);

        //hide Swipe Card ultil load user data
        flingContainer.setVisibility(View.GONE);





    }


    protected void initSwipeCard() {
        String photo_url_str;

        //Create calendar object in order to calculate age
        Calendar c = Calendar.getInstance();
        int age;

        //This array will contain swipe cards
        al = new ArrayList<>();

        for (User user : users) {

            photo_url_str = "https://graph.facebook.com/" + user.getIdUser() + "/picture?height=600&?width=600";


            c.setTime(user.getDob());
            age = getAge(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DATE));

            Log.d("Prueba", "A mostrar en swipe card name: " + user.getName());
            //Add parameters to show on Swipe Card
            al.add(new SwipeCardData(user.getIdUser(), user.getName(), user.getEducation(), age, photo_url_str));


        }

        //al.add(new SwipeCardData("http://i.ytimg.com/vi/PnxsTxV8y3g/maxresdefault.jpg", "But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain was born and I will give you a complete account of the system, and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness."));
        //al.add(new SwipeCardData("http://switchboard.nrdc.org/blogs/dlashof/mission_impossible_4-1.jpg", "But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain was born and I will give you a complete account of the system, and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness."));
        //al.add(new SwipeCardData("http://i.ytimg.com/vi/PnxsTxV8y3g/maxresdefault.jpg", "But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain was born and I will give you a complete account of the system, and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness."));
        //al.add(new SwipeCardData("http://switchboard.nrdc.org/blogs/dlashof/mission_impossible_4-1.jpg", "But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain was born and I will give you a complete account of the system, and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness."));
        //al.add(new SwipeCardData("http://i.ytimg.com/vi/PnxsTxV8y3g/maxresdefault.jpg", "But I must explain to you how all this mistaken idea of denouncing pleasure and praising pain was born and I will give you a complete account of the system, and expound the actual teachings of the great explorer of the truth, the master-builder of human happiness."));


        myAppAdapter = new MyAppAdapter(al, MainScreenActivity.this);
        flingContainer.setAdapter(myAppAdapter);
        flingContainer.setFlingListener(new SwipeFlingAdapterView.onFlingListener() {
            @Override
            public void removeFirstObjectInAdapter() {

            }

            @Override
            public void onLeftCardExit(Object dataObject) {

                Log.d("Prueba", "El SwipeCard es: " + myAppAdapter.parkingList.get(0).getName());

                al.remove(0);
                myAppAdapter.notifyDataSetChanged();
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
            }

            @Override
            public void onRightCardExit(Object dataObject) {

                Log.d("Prueba", "El SwipeCard es: " + myAppAdapter.parkingList.get(0).getName());

                al.remove(0);
                myAppAdapter.notifyDataSetChanged();

            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {

            }

            @Override
            public void onScroll(float scrollProgressPercent) {

                View view = flingContainer.getSelectedView();
                view.findViewById(R.id.background).setAlpha(0);
                view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
                view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(new SwipeFlingAdapterView.OnItemClickListener() {

            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {

                //View view = flingContainer.getSelectedView();
                //view.findViewById(R.id.background).setAlpha(0);

                myAppAdapter.notifyDataSetChanged();

                final ImageView imageView = (ImageView) findViewById(R.id.cardImage);
                //Set selected image to Swipe image view because is the common element to use in
                // shared element activity transition
                Glide.with(MainScreenActivity.this).load(myAppAdapter.parkingList.get(itemPosition)
                        .getImagePath()).into(imageView);

                Log.d("Prueba", "La longitud de Swipe Card es: " + al.size());

                //Init Shared Element Activity Transition
                Intent intent = new Intent(MainScreenActivity.this, DetailsActivity.class);
                // Pass data object in the bundle and populate details activity.
                intent.putExtra("iduser", myAppAdapter.parkingList.get(itemPosition).getIdUser());
                intent.putExtra("name", myAppAdapter.parkingList.get(itemPosition).getName());
                intent.putExtra("occupation", myAppAdapter.parkingList.get(itemPosition).getOccupation());
                intent.putExtra("age", myAppAdapter.parkingList.get(itemPosition).getAge());

                ActivityOptionsCompat options = ActivityOptionsCompat.
                        makeSceneTransitionAnimation(MainScreenActivity.this, imageView, "profile");
                startActivity(intent, options.toBundle());

                //if it is next to last swipe card I set last swipe card image on imageView
                if(al.size() == 2) {
                    //this handler delays execution for 1.2 seconds
                    new CountDownTimer(1200, 1000) {

                        public void onTick(long millisUntilFinished) {
                        }

                        public void onFinish() {
                            Glide.with(MainScreenActivity.this).load(myAppAdapter.parkingList.get(1)
                                    .getImagePath()).into(imageView);
                        }

                    }.start();
                }



            }
        });

    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the
     * LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "Building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        try {
            Log.d("Prueba", "Latitud: " + String.valueOf(mLastLocation.getLatitude()));
            Log.d("Prueba", "Longitud: " + String.valueOf(mLastLocation.getLongitude()));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");


    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    public static void removeBackground() {
        viewHolder.background.setVisibility(View.GONE);
        myAppAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActionDownPerform() {
        Log.e("action", "bingo");
    }

    public static class ViewHolder {
        public static FrameLayout background;
        public TextView DataText;
        public TextView DataText2;
        public ImageView cardImage;


    }

    public class MyAppAdapter extends BaseAdapter {


        public List<SwipeCardData> parkingList;
        public Context context;

        private MyAppAdapter(List<SwipeCardData> apps, Context context) {
            this.parkingList = apps;
            this.context = context;
        }

        @Override
        public int getCount() {
            return parkingList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View rowView = convertView;


            if (rowView == null) {

                LayoutInflater inflater = getLayoutInflater();
                rowView = inflater.inflate(R.layout.item, parent, false);
                // configure view holder
                viewHolder = new ViewHolder();
                viewHolder.DataText = (TextView) rowView.findViewById(R.id.bookText);
                viewHolder.DataText2 = (TextView) rowView.findViewById(R.id.bookText2);
                viewHolder.background = (FrameLayout) rowView.findViewById(R.id.background);
                viewHolder.cardImage = (ImageView) rowView.findViewById(R.id.cardImage);
                rowView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.DataText.setText(parkingList.get(position).getName() + ", " + parkingList.get(position).getAge() + "");
            viewHolder.DataText2.setText(parkingList.get(position).getOccupation() + "");

            Glide.with(MainScreenActivity.this).load(parkingList.get(position).getImagePath()).into(viewHolder.cardImage);

            return rowView;
        }
    }


    // Uses AsyncTask to create a task away from the main UI thread. This task takes a
    // URL string and uses it to create an HttpUrlConnection. Once the connection
    // has been established, the AsyncTask downloads the contents of the webpage as
    // an InputStream. Finally, the InputStream is converted into a string, which is
    // displayed in the UI by the AsyncTask's onPostExecute method.
    private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

        String status = null;

        DownloadWebpageTask(String status) {
            this.status = status;
        }

        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            return loadFromNetwork(urls[0]);

        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            //info.setText(result);
            if(status == "json_decode") {

                //close progress dialog
                progressDialog.dismiss();

                //Set visible Swipe Card
                flingContainer.setVisibility(View.VISIBLE);

                initSwipeCard();
            }


            Log.d("Prueba", "El resultado del post en PostExecute: " + result);

        }

        private String loadFromNetwork(String urlString) {

            InputStream is = null;
            String contentAsString = null;


            try {
                HTTPConnection httpcon = new HTTPConnection();
                is = httpcon.downloadUrl(urlString, json_user);

                if (status == "string_decode") {
                    contentAsString = httpcon.inputStreamToString(is).toString();

                    Log.d("Prueba", "Resultado del post es: " + contentAsString);
                    Log.d("Prueba", "la longitud del post es: " + contentAsString.length());
                }

                //if input Stream is a Json it will be decode
                if (status == "json_decode") {
                    JsonDecode jsonDecode = new JsonDecode ();
                    users = jsonDecode.readJsonStream(is);

                    for (User user : users) {
                        Log.d("Prueba", "usuario a mostrar en swipe card id: " + user.getIdUser());
                        Log.d("Prueba", "usuario a mostrar en swipe card name: " + user.getName());
                        Log.d("Prueba", "usuario a mostrar en swipe card about: " + user.getAbout());
                        Log.d("Prueba", "usuario a mostrar en swipe card gender: " + user.getGender());
                        Log.d("Prueba", "usuario a mostrar en swipe card dob: " + user.getDob());
                        Log.d("Prueba", "usuario a mostrar en swipe card education: " + user.getEducation());
                        Log.d("Prueba", "usuario a mostrar en swipe card work: " + user.getWork());
                    }

                    contentAsString = status;
                }

                // Makes sure that the InputStream is closed after the app is finished using it.
                if(is != null) {
                    is.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return contentAsString;

        }

    }

    public Timestamp convertStringTimeStamp(String newDate) {

        try {
            DateFormat formatter;
            Date date;
            formatter = new SimpleDateFormat("MM/dd/yyyy");
            date = (Date)formatter.parse(newDate);
            java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
            return timeStampDate;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getAge (int _year, int _month, int _day) {

        GregorianCalendar cal = new GregorianCalendar();
        int y, m, d, a;

        y = cal.get(Calendar.YEAR);
        m = cal.get(Calendar.MONTH);
        d = cal.get(Calendar.DAY_OF_MONTH);
        cal.set(_year, _month, _day);
        a = y - cal.get(Calendar.YEAR);
        if ((m < cal.get(Calendar.MONTH))
                || ((m == cal.get(Calendar.MONTH)) && (d < cal
                .get(Calendar.DAY_OF_MONTH)))) {
            --a;
        }
        if(a < 0)
            throw new IllegalArgumentException("Age < 0");
        return a;
    }
}
