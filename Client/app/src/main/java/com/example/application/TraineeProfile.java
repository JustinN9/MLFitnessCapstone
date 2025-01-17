package com.example.application;

import static com.example.application.SocketFunctions.getWorkouts;
import static com.example.application.SocketFunctions.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.TimeUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class TraineeProfile extends AppCompatActivity{

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    ImageView add_friend;
    ImageView unfriend;
    ImageView edit_button;
    User trainee_obj;
    ImageView userProfilePicture;

    private ListView workoutsListView;
    private ArrayList<Workout> workouts;
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trainee_profile);

        double[] helpTwo = new double[6];

        workoutsListView = findViewById(R.id.recentWorkoutList);
        workouts = getWorkouts(getApplicationContext(), workoutsListView, SocketFunctions.user.getId());

        //Set objects in the page
        //Once db is implemented change from test/example cases to the version that get the data from the db

        //Init fields in xml
        TextView trainee_profile_username_textview = (TextView) findViewById(R.id.userNameTraineeProfile);
        TextView trainee_profile_email_textview = (TextView) findViewById(R.id.userEmailTraineeProfile);

        //Assigns values to the fields in the xml
        //String trainee_profile_username = user.getUsername();
        String trainee_profile_username = SocketFunctions.user.getUserName();
        trainee_profile_username_textview.setText(trainee_profile_username);
        //String trainee_profile_email = user.getEmail();
        String trainee_profile_email = SocketFunctions.user.getEmail();
        trainee_profile_email_textview.setText(trainee_profile_email);


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        add_friend = findViewById(R.id.add_friend_trainee);
        unfriend = findViewById(R.id.sub_friend_trainee);
        edit_button = findViewById(R.id.edit_trainee_profile_button);
        userProfilePicture = findViewById(R.id.profile_picture);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (SocketFunctions.user.isTrainer() == false) {
            String name = SocketFunctions.user.getName();
            TextView textView = findViewById(R.id.TraineeProfileTitle);
            textView.setText(name);

            add_friend.setVisibility(View.GONE);
            unfriend.setVisibility(View.GONE);
            edit_button.setVisibility(View.VISIBLE);

            navigationView.getMenu().findItem(R.id.trainers).setVisible(true);
            navigationView.getMenu().findItem(R.id.trainees).setVisible(false);
            if(SocketFunctions.user.hasPfp()){
                userProfilePicture.setImageBitmap(SocketFunctions.user.getPfp());
            }else{
                Picasso.get().load("http://162.246.157.128/MLFitness/pfps/"+ SocketFunctions.user.getId() +".jpg").into(userProfilePicture);
                Drawable pfp = userProfilePicture.getDrawable();
                if (pfp != null) {
                    SocketFunctions.user.setPfp(Bitmap.createBitmap(pfp.getIntrinsicWidth(), pfp.getIntrinsicHeight(), Bitmap.Config.ARGB_8888));
                }
            }
        }
        else{
            Intent trainee_profile = getIntent();
            trainee_obj = (User) trainee_profile.getSerializableExtra("traineeObj");
            navigationView.getMenu().findItem(R.id.trainers).setVisible(false);
            navigationView.getMenu().findItem(R.id.trainees).setVisible(true);
            try{
                if(trainee_obj.hasPfp()){
                    userProfilePicture.setImageBitmap(trainee_obj.getPfp());
                }else{
                    Picasso.get().load("http://162.246.157.128/MLFitness/pfps/"+ trainee_obj.getId() +".jpg").into(userProfilePicture);
                    Drawable pfp = userProfilePicture.getDrawable();
                    trainee_obj.setPfp(Bitmap.createBitmap(pfp.getIntrinsicWidth(), pfp.getIntrinsicHeight(), Bitmap.Config.ARGB_8888));
                }
            }catch (Exception e){
                int trainee_profile_image = R.drawable.ic_baseline_tag_faces_24;
                userProfilePicture.setImageResource(trainee_profile_image);
            }
            TextView name = findViewById(R.id.TraineeProfileTitle);
            TextView username = findViewById(R.id.userNameTraineeProfile);
            TextView email = findViewById(R.id.userEmailTraineeProfile);
            name.setText(trainee_obj.getName());
            username.setText(trainee_obj.getUserName());
            email.setText(trainee_obj.getEmail());
            is_subbed(trainee_obj);

        }


        invalidateOptionsMenu();
        invalidateMenu();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.home: {
                        //Go to homepage
                        if(!SocketFunctions.user.isTrainer()){
                            Intent i = new Intent(getApplicationContext(), TraineeHomePage.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(i);
                            finish();
                            break;
                        }else {
                            Intent i = new Intent(getApplicationContext(), TrainerHomePage.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(i);
                            finish();
                            break;
                        }
                    }
                    case R.id.workouts: {
                        //Go to workouts
                        Intent i = new Intent(getApplicationContext(), TraineeWorkouts.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(i);
                        finish();
                        break;
                    }
                    case R.id.message: {
                        //Go to message
                        Intent i = new Intent(getApplicationContext(), TraineeMessages.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(i);
                        //finish();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    }
                    case R.id.setting: {
                        //Go to setting
                        Intent i = new Intent(getApplicationContext(), TraineeSettings.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(i);
                        finish();
                        break;
                    }
                    case R.id.trainees: {
                        //Go to trainees
                        Intent i = new Intent(getApplicationContext(), TrainerTraineeProfile.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(i);
                        finish();
                        break;
                    }
                    case R.id.trainers:{
                        Intent i = new Intent(getApplicationContext(), TraineeTrainerProfile.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(i);
                        finish();
                        break;
                    }
                    case R.id.friends: {
                        //Go to friends
                        Intent i = new Intent(getApplicationContext(), FriendsPage.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(i);
                        finish();
                        break;
                    }
                    case R.id.profile: {
                        //Already selected
                        if(SocketFunctions.user.isTrainer()){
                            Intent i = new Intent(getApplicationContext(), TrainerProfile.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(i);
                            finish();
                            break;
                        }else {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        }
                        break;
                    }
                    case R.id.logout: {
                        //Add a confirmation pop up

                        //Once completed logout/remove locally stored user cred

                        //End all activities and go to welcome screen
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        break;
                    }
                    default: {
                        drawerLayout.closeDrawer(GravityCompat.START);
                    }
                }
                return false;
            }
        });
    }

    private void is_subbed(User trainee_obj) {
        Context context = getApplicationContext();
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        String url = "http://162.246.157.128/MLFitness/get_relationships.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    //this is async reyes didn't tell me NO ONE TOLD ME
                    //this runs on a different thread than the main
                    @Override
                    public void onResponse(String response) {
                        ArrayList<User> users = new ArrayList<>();
                        Log.d("Response subbed: ", response.toString());
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            if (status.equals("success")) {
                                Log.d("this trainee friends: ", jsonResponse.getString("relationships"));
                                JSONArray relationship = new JSONArray(jsonResponse.getString("relationships"));
                                if (relationship.length() == 0){
                                    add_friend.setVisibility(View.VISIBLE);
                                    unfriend.setVisibility(View.GONE);
                                    edit_button.setVisibility(View.GONE);
                                    add_friend.setOnClickListener(clickListenerAdd);

                                }
                                else{
                                    add_friend.setVisibility(View.GONE);
                                    unfriend.setVisibility(View.VISIBLE);
                                    edit_button.setVisibility(View.GONE);
                                    unfriend.setOnClickListener(onClickListenerSub);
                                }

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("User id: ", error.getLocalizedMessage());
            }
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> paramV = new HashMap<>();
                paramV.put("id", String.valueOf(SocketFunctions.user.getId()));
                paramV.put("apiKey", SocketFunctions.apiKey);
                paramV.put("id2",String.valueOf(trainee_obj.getId()));
                paramV.put("type","2");
                return paramV;
            }
        };
        queue.add(stringRequest);
    }

    public void goToEdit(View view) {
        Intent i;
        i = new Intent(getApplicationContext(), TraineeEditProfile.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            //Go to homepage
            if (user.isTrainer()) {
                Intent i = new Intent(getApplicationContext(), TrainerHomePage.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                finish();
                super.onBackPressed();
            }
            else{
                Intent i = new Intent(getApplicationContext(), TraineeHomePage.class);
                i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(i);
                finish();
                super.onBackPressed();
            }
        }
    }

    View.OnClickListener clickListenerAdd = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Context context = getApplicationContext();
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String url = "http://162.246.157.128/MLFitness/add_relationship.php";

            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        //this is async reyes didn't tell me NO ONE TOLD ME
                        //this runs on a different thread than the main
                        @Override
                        public void onResponse(String response) {
                            ArrayList<User> users = new ArrayList<>();
                            Log.d("Response friended: ", response.toString());
                            Log.d("before if", "da fuq ");
                            if (response.toString().equals("success")) {

                                add_friend.setVisibility(View.GONE);
                                Log.d("TO FRONT", "onResponse: "+"made it");
                                unfriend.setVisibility(View.VISIBLE);
                                unfriend.bringToFront();
                                edit_button.setVisibility(View.GONE);
                                is_subbed(trainee_obj);

    ListView workoutsListView;

                            }


                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("User id: ", error.getLocalizedMessage());
                }
            }) {
                protected Map<String, String> getParams() {
                    Map<String, String> paramV = new HashMap<>();
                    //trainee
                    paramV.put("id",String.valueOf(SocketFunctions.user.getId()));
                    paramV.put("apiKey", SocketFunctions.apiKey);
                    //trainer
                    paramV.put("id2", String.valueOf(trainee_obj.getId()));
                    Log.d("THE IDS", "getParams: "+String.valueOf(SocketFunctions.user.getId())+" pain " +String.valueOf(trainee_obj.getId()));
                    if(SocketFunctions.user.isTrainer() == false){
                        paramV.put("type", "1");
                    }
                    else{
                        paramV.put("type","0");
                    }
                    return paramV;
                }
            };
            queue.add(stringRequest);
        }
    };

    View.OnClickListener onClickListenerSub = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Context context = getApplicationContext();
            Log.d("unfriend1", "onClick: ");
            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            String url = "http://162.246.157.128/MLFitness/add_relationship.php";
            Log.d("unfriend", "onClick: ");
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        //this is async reyes didn't tell me NO ONE TOLD ME
                        //this runs on a different thread than the main
                        @Override
                        public void onResponse(String response) {
                            ArrayList<User> users = new ArrayList<>();
                            Log.d("Response unfriended: ", response.toString());
                            Log.d("before if un", "da fuq ");
                            if (response.toString().equals("success")) {

                                add_friend.setVisibility(View.VISIBLE);
                                Log.d("TO FRONT", "onResponse: "+"made it");
                                unfriend.setVisibility(View.GONE);
                                edit_button.setVisibility(View.GONE);
                                is_subbed(trainee_obj);

                            }


                        }

                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("User id: ", error.getLocalizedMessage());
                }
            }) {
                protected Map<String, String> getParams() {
                    Map<String, String> paramV = new HashMap<>();
                    //trainee
                    paramV.put("id", String.valueOf(SocketFunctions.user.getId()));
                    paramV.put("apiKey", SocketFunctions.apiKey);
                    //trainer
                    paramV.put("id2",String.valueOf(trainee_obj.getId()));
                    Log.d("THE IDS", "getParams: "+String.valueOf(SocketFunctions.user.getId())+" pain " +String.valueOf(trainee_obj.getId()));
                    if(SocketFunctions.user.isTrainer() == false){
                        paramV.put("type", "1");
                    }
                    else{
                        paramV.put("type","0");
                    }
                    return paramV;
                }
            };
            queue.add(stringRequest);
        }
    };

}