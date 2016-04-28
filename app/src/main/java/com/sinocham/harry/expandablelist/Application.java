package com.sinocham.harry.expandablelist;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.Display;
import android.view.WindowManager;

import java.util.ArrayList;

/**
 * Created by Harry on 1/25/2016.
 */
public class Application extends android.app.Application {
    public static ArrayList<ArrayList<String[]>> speech = new ArrayList<>();
    public static ArrayList<String[]> jokesAll = new ArrayList<>();
    public static ArrayList<String[]> educationsAll = new ArrayList<>();
    public static ArrayList<String[]> commercialAll = new ArrayList<>();
    public static ArrayList<String[]> gamesAll = new ArrayList<>();
    public static String TAG = "harry2";

    public static int width;
    public static int height;


    //edit by david
    private SharedPreferences settings;
    private static Application application;

    public static Application getmInstance() {
        return application;
    }

    public void saveSubscribeId(long id) {
        settings.edit().putLong("id", id).apply();
    }

    public long loadSubscribeId() {
        return settings.getLong("id", 0);
    }

    public void saveASRId(long id) {
        settings.edit().putLong("asr_id", id).apply();
    }

    public long loadASRId() {
        return settings.getLong("asr_id", 0);
    }

    public void saveMiddleId(long id) {
        settings.edit().putLong("middle_id", id).apply();
    }

    public long loadMiddleId() {
        return settings.getLong("middle_id", 0);
    }

    public void saveHeadMiddleTouch(long id) {
        settings.edit().putLong("head_middle", id).apply();
    }

    public long loadHeadMiddleTouch() {
        return settings.getLong("head_middle", 0);
    }

    public void saveHeadBackTouch(long id) {
        settings.edit().putLong("head_back", id).apply();
    }

    public long loadHeadBackTouch() {
        return settings.getLong("head_back", 0);
    }


    public void saveIp(String ip) {
        settings.edit().putString("ip", ip).apply();
    }

    public String loadIp() {
        return settings.getString("ip", "192.168.5.");
    }


    public void saveLanguage(int id) {
        settings.edit().putInt("language2", id).apply();
    }

    public int loadLanguage() {
        return settings.getInt("language2", 0);
    }


    public static ArrayList<String[]> questionCN = new ArrayList<>();
    public static ArrayList<String[]> questionENG = new ArrayList<>();
    public static ArrayList<int[]> questionScore = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight();
        speech.add(jokesAll);
        speech.add(educationsAll);
        speech.add(commercialAll);
        speech.add(gamesAll);

        jokesAll.add(Constant.jokes);
        jokesAll.add(Constant.jokesCn);
        jokesAll.add(Constant.jokesEng);

        educationsAll.add(Constant.educations);
        educationsAll.add(Constant.educationsCn);
        educationsAll.add(Constant.educationsEng);

        commercialAll.add(Constant.commercial);
        commercialAll.add(Constant.commercialCn);
        commercialAll.add(Constant.commercialEng);

        gamesAll.add(Constant.games);
        gamesAll.add(Constant.gamesCn);
        gamesAll.add(Constant.gamesEng);

        //by david
        settings = getSharedPreferences("setting", Context.MODE_PRIVATE);

        questionCN.add(Constant.q1CN);
        questionCN.add(Constant.q2CN);
        questionCN.add(Constant.q3CN);
        questionCN.add(Constant.q4CN);
        questionCN.add(Constant.q5CN);
        questionCN.add(Constant.q6CN);
        questionCN.add(Constant.q7CN);
        questionCN.add(Constant.q8CN);
        questionCN.add(Constant.q9CN);
        questionCN.add(Constant.q10CN);
        questionCN.add(Constant.q11CN);
        questionCN.add(Constant.q12CN);
        questionCN.add(Constant.q13CN);
        questionCN.add(Constant.q14CN);
        questionCN.add(Constant.q15CN);
        questionCN.add(Constant.q16CN);
        questionCN.add(Constant.q17CN);
        questionENG.add(Constant.q1ENG);
        questionENG.add(Constant.q2ENG);
        questionENG.add(Constant.q3ENG);
        questionENG.add(Constant.q4ENG);
        questionENG.add(Constant.q5ENG);
        questionENG.add(Constant.q6ENG);
        questionENG.add(Constant.q7ENG);
        questionENG.add(Constant.q8ENG);
        questionENG.add(Constant.q9ENG);
        questionENG.add(Constant.q10ENG);
        questionENG.add(Constant.q11ENG);
        questionENG.add(Constant.q12ENG);
        questionENG.add(Constant.q13ENG);
        questionENG.add(Constant.q14ENG);
        questionENG.add(Constant.q15ENG);
        questionENG.add(Constant.q16ENG);
        questionENG.add(Constant.q17ENG);
        questionScore.add(Constant.q1score);
        questionScore.add(Constant.q2score);
        questionScore.add(Constant.q3score);
        questionScore.add(Constant.q4score);
        questionScore.add(Constant.q5score);
        questionScore.add(Constant.q6score);
        questionScore.add(Constant.q7score);
        questionScore.add(Constant.q8score);
        questionScore.add(Constant.q9score);
        questionScore.add(Constant.q10score);
        questionScore.add(Constant.q11score);
        questionScore.add(Constant.q12score);
        questionScore.add(Constant.q13score);
        questionScore.add(Constant.q14score);
        questionScore.add(Constant.q15score);
        questionScore.add(Constant.q16score);
        questionScore.add(Constant.q17score);
    }
}
