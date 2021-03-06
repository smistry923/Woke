package com.example.smistry.woke;

import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.smistry.woke.fragments.ViewPagerFragment;
import com.example.smistry.woke.fragments.goals;
import com.example.smistry.woke.fragments.stats;
import com.example.smistry.woke.models.Day;
import com.example.smistry.woke.models.Free;
import com.example.smistry.woke.models.MessageEvent;
import com.example.smistry.woke.models.Task;
import com.example.smistry.woke.models.Weather;
import com.facebook.soloader.SoLoader;
import com.facebook.sonar.android.AndroidSonarClient;
import com.facebook.sonar.android.utils.SonarUtils;
import com.facebook.sonar.core.SonarClient;
import com.facebook.sonar.plugins.inspector.DescriptorMapping;
import com.facebook.sonar.plugins.inspector.InspectorSonarPlugin;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.commons.io.FileUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;


public class bottomNav extends AppCompatActivity {
    final DescriptorMapping descriptorMapping = DescriptorMapping.withDefaults();
    ArrayList<Day> days;
    HashMap<String, ArrayList<Free>> settings;
    final FragmentManager fragmentManager = getSupportFragmentManager();
    private NotificationManagerCompat notificationManager;
    public final static String API_BASE_URL = "http://dataservice.accuweather.com/forecasts/v1/daily/1day/337153";
    public final static String API_KEY_PARAM = "apikey";
    public final static String TAG = "TestActivity";
    public boolean bringJacket = false;
    int jacketTemp;  //Temperature specified by user

    AsyncHttpClient client;

    // define your fragments here
    final Fragment fragment2= new goals();
    final Fragment fragment3= new stats();

    ViewPagerFragment viewPager;

    HashMap<String,Integer>  months = new HashMap<>();

    public ArrayList<Day> getDays() {
        return days;
    }

    //Setting fragments
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.flContainer, ViewPagerFragment.newInstance(days)).commit();
                    return true;
                case R.id.navigation_goals:
                    FragmentTransaction fragmentTransaction2 = fragmentManager.beginTransaction();
                    fragmentTransaction2.replace(R.id.flContainer, fragment2).commit();
                    return true;
                case R.id.navigation_statistics:
                    FragmentTransaction fragmentTransaction3 = fragmentManager.beginTransaction();
                    fragmentTransaction3.replace(R.id.flContainer, fragment3).commit();
                    return true;
            }
            return false;
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_nav);
        //sonar Setup
        SoLoader.init(this, false);
        if (BuildConfig.DEBUG && SonarUtils.shouldEnableSonar(this)) {
            final SonarClient client = AndroidSonarClient.getInstance(this);
            client.addPlugin(new InspectorSonarPlugin(getApplicationContext(), descriptorMapping));
            client.start();
        }

        //Setting months_used for reading items from files
        months.put("Jan",0);
        months.put("Feb",1);
        months.put("Mar",2);
        months.put("Apr",3);
        months.put("May",4);
        months.put("Jun",5);
        months.put("Jul",6);
        months.put("Aug",7);
        months.put("Sep",8);
        months.put("Oct",9);
        months.put("Nov",10);
        months.put("Dec",11);

        readItems();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        jacketTemp=Integer.valueOf(prefs.getString("temp", "0F").toString().replaceAll("[^0-9]", ""));

        notificationManager = NotificationManagerCompat.from(this);
        client = new AsyncHttpClient();

        getWeather();

        //Shows Setting Activity if this is the FIRST time the app is running
        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);

        if (isFirstRun) {
            //show sign up activity
            startActivityForResult(new Intent(this, SettingsActivity.class), 0);
        }
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putBoolean("isFirstRun", false).commit();

       Intent data = getIntent();
       settings = (HashMap<String, ArrayList<Free>>) data.getSerializableExtra("FreeMap");

        //Fill the Day Array with information
        days=new ArrayList<>();

       viewPager= ViewPagerFragment.newInstance(days);
        Log.d("EventBus", days.toString());


        //begins fragment transaction_ViewPagerFragment is shown as the default view
       FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(R.id.flContainer, viewPager).commit();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        if (days!=null){
            for (Day day: days) {
                if (day.getDayOfWeek().equals(Calendar.getInstance().DAY_OF_WEEK)){
                    for (Free free:day.getFreeBlocks()){
                        for (Task task:free.getTasks()){
                            Time taskEnd=new Time (task.getTime().getHours(),task.getTime().getMinutes()+task.getDuration(),0);
                            if (taskEnd.before(Calendar.getInstance().getTime()) && !task.isCompleted() ){
                                    //reschedule new task
                                   // setTaskWithinFreeBlock;
                                    task.setCompleted(true);
                            }
                        }
                    }

                }
            }
        }
    }


    @Subscribe (sticky = true,threadMode = ThreadMode.BACKGROUND)
    public void onEvent(MessageEvent event){
        days= event.getmDaysList();
        viewPager.setDaysA(days);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    //Setting toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_toolbar, menu);
        return true;
    }

    //open settings with the toolbar icon
    public void openSettings(MenuItem item) {
        Intent intent = new Intent(bottomNav.this,SettingsActivity.class);
        intent.putExtra("days", Parcels.wrap(days));
        startActivityForResult(intent,2);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    //Sends a notification when jacket icon is clicked (Weather)
    public void sendOnChannel1(MenuItem menuItem) {
        Boolean jacketPref= PreferenceManager.getDefaultSharedPreferences(this).getBoolean("jacket", false);
        getWeather();
        if(jacketPref && bringJacket) {
            Notification notification = new NotificationCompat.Builder(this, WokeApp.Channel_1_ID)
                    .setSmallIcon(R.drawable.ic_wb_sunny_black_24dp)
                    .setContentTitle("Bring Jacket")
                    .setContentText("Bring Jacket")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build();

            notificationManager.notify(1, notification);
        }
        else {
            Notification notification = new NotificationCompat.Builder(this, WokeApp.Channel_1_ID)
                    .setSmallIcon(R.drawable.ic_wb_sunny_black_24dp)
                    .setContentTitle("No Jacket Needed")
                    .setContentText("No Jacket Needed")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build();

            notificationManager.notify(1, notification);
        }

    }

    //Sends a notification when moon icon is clicked. Sleep recommendation depending on the age of the user
    public void sendOnChannel2(MenuItem menuItem) {
        String ageStr = PreferenceManager.getDefaultSharedPreferences(this).getString("age", "19");
        int age = Integer.parseInt(ageStr);
        if(age > 5 && age < 14) {
            Notification notification = new NotificationCompat.Builder(this, WokeApp.Channel_1_ID)
                    .setSmallIcon(R.drawable.moon)
                    .setContentTitle("National Sleep Foundation Recommendation")
                    .setContentText("Your sleep time should be between 10-11 hours")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build();

            notificationManager.notify(1, notification);
        }
        else if(age > 13 && age <18) {
            Notification notification = new NotificationCompat.Builder(this, WokeApp.Channel_1_ID)
                    .setSmallIcon(R.drawable.moon)
                    .setContentTitle("National Sleep Foundation Recommendation")
                    .setContentText("Your sleep time should be between 8-10 hours")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build();

            notificationManager.notify(1, notification);
        }
        else if(age > 18 && age < 65) {
            Notification notification = new NotificationCompat.Builder(this, WokeApp.Channel_1_ID)
                    .setSmallIcon(R.drawable.moon)
                    .setContentTitle("National Sleep Foundation Recommendation")
                    .setContentText("Your sleep time should be between 7-9 hours")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build();

            notificationManager.notify(1, notification);
        }

        else {
            Notification notification = new NotificationCompat.Builder(this, WokeApp.Channel_1_ID)
                    .setSmallIcon(R.drawable.moon)
                    .setContentTitle("National Sleep Foundation Recommendation")
                    .setContentText("Your sleep time should be between 7-8 hours")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build();

            notificationManager.notify(1, notification);
        }

    }

    //Request information from Accuweather's API
    public void getWeather(){
        String url = API_BASE_URL;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        jacketTemp=Integer.valueOf(prefs.getString("temp", "0F").toString().replaceAll("[^0-9]", ""));

        RequestParams params = new RequestParams();
        params.put(API_KEY_PARAM, getString(R.string.apikey)); //API key, always required
        //execute a GET Request expecting a JSON object response
        client.get(url,params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try {
                    JSONArray results = response.getJSONArray("DailyForecasts");
                    Weather weather = new Weather(results.getJSONObject(0));
                    if(Integer.parseInt(weather.getMinTemp()) <= jacketTemp) bringJacket = true;
                } catch (JSONException e) {
                    logError("Failed to parse now playing movies", e, true);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                logError("Failed to get data from now playing endpoint", throwable, true );
            }
        });
    }

    private void logError(String message, Throwable error, boolean alertUser){
        //always log the error
        Log.e(TAG, message,error);
        //alert the user to avoid silent errors
        if(alertUser){
            //show a toast with the error message
            Toast.makeText(getApplicationContext(), message,Toast.LENGTH_LONG).show();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onResume() {
        super.onResume();
        readItems();
    }

    // returns the file in which the data is stored
    public File getDataFile() {
        return new File(this.getFilesDir(), "days.txt");
    }

    // read the items from the file system
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void readItems() {
        try {
            ArrayList<String> dayStrings;
           // create the array using the content in the file
            dayStrings = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
            if(days!=null){
                days.clear();}
            else{
                days=new ArrayList<>();
            }

            for (String daysString : dayStrings) {
                Log.d("Day", daysString);

                String[] params = daysString.split("_");
                String dayOfWeek = params[0];
                String[] wakeTimeSplit = params[1].split(":");
                String[] sleepTimeSplit = params[2].split(":");
                Time wakeTime = new Time(Integer.valueOf(wakeTimeSplit[0]), Integer.valueOf(wakeTimeSplit[1]), 0);
                Time sleepTime = new Time(Integer.valueOf(sleepTimeSplit[0]), Integer.valueOf(sleepTimeSplit[1]), 0);


                String freeBlocks=params[3];
                //remove brackets from array toStirng
                freeBlocks=freeBlocks.substring(1,freeBlocks.length()-1);
                String [] freeBlocksSplit=freeBlocks.split(",");
                ArrayList<Free>frees=new ArrayList<>();

                if (!freeBlocks.equals("")) {
                    for (String free : freeBlocksSplit) {
                        String[] splitFree = free.split(";");
                        String[] freeStartSplit = splitFree[0].split(":");
                        String[] freeEndSplit = splitFree[1].split(":");
                        Time freeStart = new Time(Integer.valueOf(freeStartSplit[0].replaceAll("\\s+", "")), Integer.valueOf(freeStartSplit[1].replaceAll("\\s+", "")), 0);
                        Time freeEnd = new Time(Integer.valueOf(freeEndSplit[0].replaceAll("\\s+", "")), Integer.valueOf(freeEndSplit[1].replaceAll("\\s+", "")), 0);
                        int duration = Integer.valueOf(splitFree[2]);
                        ArrayList<Task> tasks = new ArrayList<>();

                        if (splitFree.length == 4) {

                            String[] tasksStrings = splitFree[3].split("/");
                            for (String taskString : tasksStrings) {

                                String[] taskStringSplit = taskString.split("-");
                                String[] tasktimeSplit = taskStringSplit[4].split(":");

                                String title = taskStringSplit[0];
                                String category = taskStringSplit[1];
                                int durationTask = Integer.parseInt(taskStringSplit[2].replaceAll("\\s+", ""));

                                String[] dateSplit=taskStringSplit[3].split(" ");

                                Date date= new Date(Integer.valueOf(dateSplit[5])-1900,months.get(dateSplit[1]) ,Integer.valueOf(dateSplit[2]));
                                Time time = new Time(Integer.valueOf(tasktimeSplit[0].replaceAll("\\s+", "")), Integer.valueOf(tasktimeSplit[1].replaceAll("\\s+", "")), 0);
                                Boolean completed= Boolean.valueOf(taskStringSplit[5]);
                                Task newTask = new Task(title, category, durationTask, date, time,completed);
                                tasks.add(newTask);
                            }
                        }
                        frees.add(new Free(tasks, freeStart, freeEnd));
                    }
                }
                Day newDay= new Day(frees,dayOfWeek,wakeTime, sleepTime);
                days.add(newDay);
            }
        }
        catch (IOException e) {
            // print the error to the console
            e.printStackTrace();
            // just load an empty list
            days = new ArrayList<>();
        }

    }

}


