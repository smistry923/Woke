package com.example.smistry.woke;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.smistry.woke.fragments.ViewPagerFragment;
import com.example.smistry.woke.fragments.goals;
import com.example.smistry.woke.fragments.stats;
import com.example.smistry.woke.models.Day;
import com.example.smistry.woke.models.Free;
import com.example.smistry.woke.models.Task;

import java.io.File;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Date;

public class bottomNav extends AppCompatActivity {

    ArrayList<Task> tasks;
    ArrayList<Free> freeBlocks;
    ArrayList<Day> days;
    final FragmentManager fragmentManager = getSupportFragmentManager();


    ArrayList<Task> tasks2;
    ArrayList<Free> freeBlocks2;
    ArrayList<Task> tasks3;
    ArrayList<Free> freeBlocks3;
    ArrayList<Task> tasks4;
    ArrayList<Free> freeBlocks4;
    ArrayList<Task> tasks5;
    ArrayList<Free> freeBlocks5;
    ArrayList<Task> tasks6;
    ArrayList<Free> freeBlocks6;

    // define your fragments here
    final Fragment fragment2= new goals();
    final Fragment fragment3= new stats();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_nav);

        //Fill the Day Array with information
        //TODO to be replaced with the information from the Files
        days=new ArrayList<>();
        freeBlocks=new ArrayList<>();
        tasks=new ArrayList<>();
        Task task1 = new Task("Work in the app", "internship",30, new Date());
        task1.setTime(new Time(10,00,00));
        tasks.add(task1);
        freeBlocks.add(new Free(tasks, new Time(10,00,00), new Time(14,00,00), 240));

        freeBlocks2=new ArrayList<>();
        tasks2=new ArrayList<>();
        Task task2 = new Task("Another task", "work",30, new Date());
        task2.setTime(new Time(10,00,00));
        tasks2.add(task2);
        freeBlocks2.add(new Free(tasks2, new Time(10,00,00), new Time(14,00,00), 240));

        freeBlocks3=new ArrayList<>();
        tasks3=new ArrayList<>();
        Task task3 = new Task("A3", "work",30, new Date());
        task3.setTime(new Time(10,00,00));
        tasks3.add(task3);
        freeBlocks3.add(new Free(tasks3, new Time(10,00,00), new Time(14,00,00), 240));

        freeBlocks4=new ArrayList<>();
        tasks4=new ArrayList<>();
        Task task4 = new Task("A4", "work",30, new Date());
        task4.setTime(new Time(10,00,00));
        tasks4.add(task4);
        freeBlocks4.add(new Free(tasks4, new Time(10,00,00), new Time(14,00,00), 240));

        freeBlocks5=new ArrayList<>();
        tasks5=new ArrayList<>();
        Task task5 = new Task("A5", "work",30, new Date());
        task5.setTime(new Time(10,00,00));
        tasks5.add(task5);
        freeBlocks5.add(new Free(tasks5, new Time(10,00,00), new Time(14,00,00), 240));

        freeBlocks6=new ArrayList<>();
        tasks6=new ArrayList<>();
        Task task6 = new Task("A6", "work",30, new Date());
        task6.setTime(new Time(10,00,00));
        tasks6.add(task6);
        freeBlocks6.add(new Free(tasks6, new Time(10,00,00), new Time(14,00,00), 240));


        days.add(new Day(freeBlocks,"Sunday", new Time(22,0,0),new Time(6,00,00)));
        days.add(new Day(freeBlocks2,"Monday", new Time(22,0,0),new Time(6,00,00)));
        days.add(new Day(freeBlocks3,"Tuesday", new Time(22,0,0),new Time(6,00,00)));
        days.add(new Day(freeBlocks4,"Wednesday", new Time(22,0,0),new Time(6,00,00)));
        days.add(new Day(freeBlocks5,"Thursday", new Time(22,0,0),new Time(6,00,00)));
        days.add(new Day(freeBlocks6,"Friday", new Time(22,0,0),new Time(6,00,00)));
        days.add(new Day(freeBlocks,"Saturday", new Time(22,0,0),new Time(6,00,00)));


        //Shows Setting Activity if this is the FIRST time the app is running
        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);
        if (isFirstRun) {
            //show sign up activity
            startActivity(new Intent(this, SettingsActivity.class));
        }
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putBoolean("isFirstRun", false).commit();

        //begins fragment transaction_ViewPagerFragment is shown as the default view
       FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.add(R.id.flContainer, ViewPagerFragment.newInstance(days)).commit();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    // returns the file in which the data is stored
    public File getDataFile() {
        return new File(this.getFilesDir(), "tasks.txt");
    }

    // read the items from the file system
 /*   private void readItems() {
        try {

            ArrayList<String> taskStrings;
            // create the array using the content in the file
            taskStrings  = new ArrayList<String>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
            tasks.clear();

            for(String task : taskStrings){
                Log.d("TASK", task);

                String [] params = task.split(",");
                String category = params [0];
                int duration = Integer.parseInt(params [1]);
                boolean automated = Boolean.parseBoolean(params[2]);
                int priority = Integer.parseInt(params[3]);
                DateFormat format = new SimpleDateFormat("E MMM dd HH:mm:ss ZZZ yyyy", Locale.ENGLISH);
                Date date = format.parse(params[4]);
                boolean day = Boolean.parseBoolean(params[5]);
                Task newTask = new Task ("Ending, something",category, duration,  date);

                tasks.add(newTask);
                Log.d("TASK", "added new task: " + newTask.toString());

            }

        } catch (IOException e) {
            // print the error to the console
            e.printStackTrace();
            // just load an empty list
            tasks = new ArrayList<>();
        } catch (ParseException e) {
            e.printStackTrace();
            Log.d("Home Activity", "Error with date to String");
        }
    }

    // write the items to the filesystem
    private void writeItems() {
        try {
            // save the item list as a line-delimited text file
            FileUtils.writeLines(getDataFile(), tasks);
        } catch (IOException e) {
            // print the error to the console
            e.printStackTrace();
        }
    }*/


}
