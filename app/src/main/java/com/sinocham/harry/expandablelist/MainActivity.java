package com.sinocham.harry.expandablelist;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aldebaran.qi.CallError;
import com.aldebaran.qi.Session;
import com.aldebaran.qi.helper.EventCallback;
import com.aldebaran.qi.helper.proxies.ALAnimatedSpeech;
import com.aldebaran.qi.helper.proxies.ALAudioDevice;
import com.aldebaran.qi.helper.proxies.ALAutonomousMoves;
import com.aldebaran.qi.helper.proxies.ALBehaviorManager;
import com.aldebaran.qi.helper.proxies.ALDiagnosis;
import com.aldebaran.qi.helper.proxies.ALFaceDetection;
import com.aldebaran.qi.helper.proxies.ALLeds;
import com.aldebaran.qi.helper.proxies.ALMemory;
import com.aldebaran.qi.helper.proxies.ALMotion;
import com.aldebaran.qi.helper.proxies.ALRobotPosture;
import com.aldebaran.qi.helper.proxies.ALSensors;
import com.aldebaran.qi.helper.proxies.ALSpeechRecognition;
import com.aldebaran.qi.helper.proxies.ALTextToSpeech;
import com.aldebaran.qi.helper.proxies.ALTouch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

public class MainActivity extends AppCompatActivity {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    ALTextToSpeech alTextToSpeech;
    ALBehaviorManager alBehaviorManager;
    ALAnimatedSpeech alAnimatedSpeech;
    ALAudioDevice alAudioDevice;
    ALMotion alMotion;
    ALRobotPosture alRobotPosture;
    ALSpeechRecognition alSpeechRecognition;
    ALMemory alMemory;
    ALAutonomousMoves aLAutonomousMoves;
    ALFaceDetection aLFaceDetection;
    ALDiagnosis alDiagnosis;
    ALLeds aLLeds;
    ALTouch alTouch;
    ALSensors alSensors;

    private GameFragment gameFragment;
    private QuestionnaireFragment questionnaireFragment;
    public Session session;

    private ProgressDialog loading;
    private Button langBtnCan;
    private Button langBtnCn;
    private Button langBtnEng;
    private Button mainMenuBtn;
    private Button pauseBtn;
    private Button okBtn;
    private EditText iptext;
    private Button softerBtn;
    private Button louderBtn;
    private Button sitBtn;
    private Button standBtn;
    private Button offBtn;
    private Button newsBtn;
    private Button eduBtn;
    private Button funMotionBtn;
    private Button gameBtn;
    private Button questBtn;

    private TextView statusText;
    private LinearLayout connectedLayout;
    public boolean speechLoopEnable;
    public boolean playingSpeech = false;

    public boolean isPlayingMenu = false; //record the state of playing menu

    public boolean isInterrupt = false;
    public List<String> abc = new ArrayList<String>();
//    private Handler handler;
//    private Runnable runnable;
//    public String lastFaces = "[]";
//    public int noPeopleTime = 31;
//    public int longLeaveTime = 29;
//    public int shortLeaveTime = 5;
//    public boolean noPeopleTimeEnable = false;

    public Object lockForStopAll = new Object();
    public Object lockForTouch = new Object();

    public boolean touchIsInProgress = false;

    private int touchCounter = 0; //counter for touch
    private int touchBackCounter = 0; //counter for touch

    public final static int DO_QUESTIONNAIRE = 3;
    public final static int YES_NO_QUESTION = 4;
    public final static int COMMAND = 2;
    public final static int PLAY_GAMES = 1;
    public final static int LANGUAGE = 0;

    public final static int SERVICES_EDUCATION = 1;
    public final static int SERVICES_NEWS = 2;
    public final static int SERVICES_FUN = 0;


    public final static int LANGUAGE_CANTONESE = 0;
    public final static int LANGUAGE_CHINESE = 1;
    public final static int LANGUAGE_ENGLISH = 2;

    public final static int REST = 0;
    public final static int WAKE = 1;

    private class ConnectRobot extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {

            Application.getmInstance().saveIp(params[0]);
            try {
                session = new Session();
                session.connect("tcp://" + params[0] + ":9559").sync(1000, TimeUnit.MILLISECONDS);
                alAudioDevice = new ALAudioDevice(session);
                alTextToSpeech = new ALTextToSpeech(session);

                alTextToSpeech.setLanguage("English");

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            alTextToSpeech.say("mobile connected");
                            changeLanguage(MainActivity.LANGUAGE_CANTONESE, false, false, true);
                        } catch (CallError callError) {
                            callError.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                alRobotPosture = new ALRobotPosture(session);
                alMotion = new ALMotion(session);
                alBehaviorManager = new ALBehaviorManager(session);
                alAnimatedSpeech = new ALAnimatedSpeech(session);
                alSpeechRecognition = new ALSpeechRecognition(session);
                alSpeechRecognition.setLanguage("English");
                aLAutonomousMoves = new ALAutonomousMoves(session);
                aLFaceDetection = new ALFaceDetection(session);
                alTouch = new ALTouch(session);
                aLLeds = new ALLeds(session);
                alMemory = new ALMemory(session);
                alDiagnosis = new ALDiagnosis(session);
                alDiagnosis.setEnableNotification(false);
                alSpeechRecognition.setAudioExpression(false);
                alSpeechRecognition.setVisualExpression(true);
//                alSpeechRecognition.setAudioExpression(false);
                aLAutonomousMoves.setExpressiveListeningEnabled(true);
//                aLAutonomousMoves.setBackgroundStrategy("none");

                alSensors = new ALSensors(session);

                alRobotPosture.goToPosture("Stand", 0.5f);
                unSubcribeASR("connectRobot");
                subscribeLeftBumper();
                subscribeRightBumper();
                subscribeMiddle();
//                subscribeFace();
                lockForStopAll = new Object();

                try {
                    alMotion.setFallManagerEnabled(false);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            } catch (CallError callError) {
                callError.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }


        @Override
        protected void onPostExecute(Boolean result) {
            // execution of result of Long time consuming operation
            if (result) {
                offBtn.setClickable(true);
                statusText.setText("status: robot connected");
                connectedLayout.setVisibility(View.VISIBLE);
                offBtn.setEnabled(true);
            } else {
                statusText.setText("Status: robot connect fail");
                offBtn.setEnabled(false);
            }

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        langBtnCan = (Button) findViewById(R.id.langBtnCan);
        langBtnCn = (Button) findViewById(R.id.langBtnCn);
        langBtnEng = (Button) findViewById(R.id.langBtnEng);
        pauseBtn = (Button) findViewById(R.id.pauseBtn);
        okBtn = (Button) findViewById(R.id.okBtn);
        iptext = (EditText) findViewById(R.id.iptext);
        softerBtn = (Button) findViewById(R.id.softerBtn);
        louderBtn = (Button) findViewById(R.id.louderBtn);
        connectedLayout = (LinearLayout) findViewById(R.id.connected_layout);
        sitBtn = (Button) findViewById(R.id.sitBtn);
        standBtn = (Button) findViewById(R.id.standBtn);
        offBtn = (Button) findViewById(R.id.offBtn);
        statusText = (TextView) findViewById(R.id.statusText);
        newsBtn = (Button) findViewById(R.id.newBtn);
        eduBtn = (Button) findViewById(R.id.eduBtn);
        funMotionBtn = (Button) findViewById(R.id.funmotionBtn);
        gameBtn = (Button) findViewById(R.id.gameBtn);
        questBtn = (Button) findViewById(R.id.questBtn);
        mainMenuBtn = (Button) findViewById(R.id.mainMenuBtn);

        iptext.setText(Application.getmInstance().loadIp());
        expListView = (ExpandableListView) findViewById(R.id.lvExp);

        loading = null;


//        startPollingThread();
        // preparing list data
        offBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (session == null) {
                    return;
                }

                okBtn.setEnabled(false);

                try {

                    if (session.isConnected()) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        connectedLayout.setVisibility(View.INVISIBLE);
                                    }
                                });

                                try {
                                    stopAll();
                                    alMemory.unsubscribeToEvent(Application.getmInstance().loadHeadMiddleTouch());
                                    alMemory.unsubscribeToEvent(Application.getmInstance().loadHeadBackTouch());
                                    alMotion.rest();
                                    Application.getmInstance().saveHeadMiddleTouch(0);
                                    Application.getmInstance().saveHeadBackTouch(0);
                                    changeLanguage(MainActivity.LANGUAGE_ENGLISH, false, false, true);
//                                    sayText("mobile disconnected", SayCallBackEvent.AFTER_COMMON_LANGUAGE, false);
                                    alTextToSpeech.say("mobile disconnected");
                                } catch (CallError callError) {
                                    Log.d(Application.TAG, "Session already closed, un run command can be ignored");
//                                    callError.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                EventBus.getDefault().post(new CloseAllThenCloseSession());
//                                try {
//                                    session.close();
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                } finally {
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            statusText.setText("status: robot disconnected");
//                                            okBtn.setEnabled(true);
//                                        }
//                                    });
//                                }
                            }
                        }).start();

                    } else {
                        connectedLayout.setVisibility(View.INVISIBLE);
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                offBtn.setEnabled(false);

            }
        });

        sitBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (session.isConnected()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                alBehaviorManager.stopAllBehaviors();
                                alMotion.rest();
                            } catch (CallError callError) {
                                callError.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });
        standBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (session.isConnected()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                alBehaviorManager.stopAllBehaviors();
                                alMotion.stopMove();
                                alRobotPosture.goToPosture("Stand", 0.5f);
                                Log.d(Application.TAG, "stand: 1");
//                                    isStanding=true;
                            } catch (CallError callError) {
                                callError.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });

        prepareListData();

        softerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (session.isConnected()) {
                    try {
                        int vol = alAudioDevice.getOutputVolume();
                        vol -= 10f;
                        if (vol <= 0) {
                            vol = 0;
                        }
                        alAudioDevice.setOutputVolume(vol);
                        Toast.makeText(getApplicationContext(), String.valueOf(vol), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        louderBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (session.isConnected()) {
                    try {
                        int vol = alAudioDevice.getOutputVolume();
                        vol += 10f;
                        if (vol >= 100) {
                            vol = 100;
                        }
                        alAudioDevice.setOutputVolume(vol);
                        Toast.makeText(getApplicationContext(), String.valueOf(vol), Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        newsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                robotService(MainActivity.SERVICES_NEWS);
            }
        });

        eduBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                robotService(MainActivity.SERVICES_EDUCATION);
            }
        });

        funMotionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                robotService(MainActivity.SERVICES_FUN);
            }
        });

        gameBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (session.isConnected()) {
                    enterGame();
                }
            }
        });
        questBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (session.isConnected()) {
                enterQuestionnaire();
//                }
            }
        });
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager mImMan = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                mImMan.hideSoftInputFromWindow(iptext.getWindowToken(), 0);
                new ConnectRobot().execute(iptext.getText().toString());
                statusText.setText("connecting robot");
            }
        });

        langBtnCan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (session.isConnected()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                stopAll();
                                changeLanguage(MainActivity.LANGUAGE_CANTONESE, true, true, true);
                                alTextToSpeech.say("廣東話");
                            } catch (CallError callError) {
                                callError.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });

        langBtnCn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (session.isConnected()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                stopAll();
                                changeLanguage(MainActivity.LANGUAGE_CHINESE, true, true, true);
                                alTextToSpeech.say("普通话");
                            } catch (CallError callError) {
                                callError.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });
        langBtnEng.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (session.isConnected()) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                stopAll();
                                changeLanguage(MainActivity.LANGUAGE_ENGLISH, true, true, true);
                                alTextToSpeech.say("English");
                            } catch (CallError callError) {
                                callError.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });
        mainMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAll();
                askServices(Application.getmInstance().loadLanguage());
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAll();
            }
        });

        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        // setting list adapter
        expListView.setAdapter(listAdapter);
    }

    public void dismissProgressDialog() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            loading.dismiss();
                            loading = null;
                        } catch (Exception ex) {
                            Log.d(Application.TAG, "dismissProgressDialog null point ex correctly ");
                        }
                    }
                });
            }
        }).start();
    }

    public void showProgressDialog(final String value) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        loading = ProgressDialog.show(MainActivity.this, "", value, true);
                    }
                });
            }
        }).start();
    }

    public void stopSpeech() {
        try {
            isPlayingMenu = false;
            speechLoopEnable = false;
            alTextToSpeech.stopAll();
//            playingSpeech = false;
            setPlayingSpeech(false, "stopSpeech()");

        } catch (CallError callError) {
            callError.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void changeLanguage(final int language, final boolean isShowProgressDialog, boolean isUILanguageChange, boolean isRealChangeLanguage) {


        if (isRealChangeLanguage) {
            Application.getmInstance().saveLanguage(language);
        }

        try {

            if (language == MainActivity.LANGUAGE_CANTONESE) {
                Log.d(Application.TAG, "NAO正在轉用廣東話");
                if (isShowProgressDialog) {
                    showProgressDialog("NAO正在轉用廣東話");
                }

                alTextToSpeech.setLanguage("CantoneseHK");
                alTextToSpeech.setParameter("speed", 80f);

                if (isUILanguageChange) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainMenuBtn.setText("主頁");
                            pauseBtn.setText("暫停");
                            sitBtn.setText("坐");
                            standBtn.setText("站");
                            gameBtn.setText("互動遊戲");
                            newsBtn.setText("最新消息");
                            eduBtn.setText("教育資訊");
                            questBtn.setText("問卷調查");
                            funMotionBtn.setText("趣怪動作");
                            upadateListData(language);
                        }
                    });
                }

            } else if (language == MainActivity.LANGUAGE_CHINESE) {
                Log.d(Application.TAG, "NAO正在轉用普通話");
                if (isShowProgressDialog) {
                    showProgressDialog("NAO正在轉用普通話");
                }

                alTextToSpeech.setLanguage("Chinese");
                alTextToSpeech.setParameter("speed", 80f);

                if (isUILanguageChange) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainMenuBtn.setText("主頁");
                            pauseBtn.setText("暂停");
                            sitBtn.setText("坐");
                            standBtn.setText("站");
                            gameBtn.setText("互动游戏");
                            newsBtn.setText("最新消息");
                            eduBtn.setText("教育资讯");
                            questBtn.setText("问卷调查");
                            funMotionBtn.setText("趣怪动作");
                            upadateListData(language);
                        }
                    });
                }


            } else if (language == MainActivity.LANGUAGE_ENGLISH) {
                Log.d(Application.TAG, "NAO is using English");
                if (isShowProgressDialog) {
                    showProgressDialog("NAO is using English");
                }

                alTextToSpeech.setLanguage("English");
                alTextToSpeech.setParameter("speed", 90f);

                if (isUILanguageChange) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mainMenuBtn.setText("Home");
                            pauseBtn.setText("Pause");
//                                        sitBtn.setText(getResources().getString(R.string.sit));
                            sitBtn.setText("Sit");
                            standBtn.setText("Stand");
                            gameBtn.setText("GAMES");
                            newsBtn.setText("NEWS");
                            eduBtn.setText("EDUCATION");
                            questBtn.setText("QUESTIONNAIRE");
                            funMotionBtn.setText("FUN MOTIONS");
                            upadateListData(language);
                        }
                    });
                }
            }


        } catch (CallError callError) {
            callError.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (isShowProgressDialog) {
            dismissProgressDialog();
        }

    }

    public void robotService(final int services) {

        if (session.isConnected()) {
            stopAll();
            speechLoopEnable = true;
            //                alAnimatedSpeech.setBodyLanguageMode(2);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int length = Application.speech.get(services).get(Application.getmInstance().loadLanguage()).length;
                    for (int j = 0; j < length; j++) {
                        final String a = Application.speech.get(services).get(Application.getmInstance().loadLanguage())[j];
                        try {
                            if (speechLoopEnable) {
                                if (services == MainActivity.SERVICES_FUN) {
                                    changeLanguage(MainActivity.LANGUAGE_ENGLISH, false, false, false);
                                }
                                alAnimatedSpeech.say(a);
                                if (services == MainActivity.SERVICES_FUN) {
                                    alBehaviorManager.runBehavior("boc/" + a);
                                }
                                if (j == length - 1) {
                                    alRobotPosture.goToPosture("Stand", 0.5f);
                                    askServices(Application.getmInstance().loadLanguage());
//                                    subscribeASR(MainActivity.COMMAND, "robotServices");
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }

    public void enterGame() {

        showProgressDialog("Loading games");

        new Thread(new Runnable() {
            @Override
            public void run() {
                stopAll();
                changeLanguage((int) Application.getmInstance().loadLanguage(), false, false, false);
                gameFragment = GameFragment.newInstance();
                gameFragment.show(getSupportFragmentManager(), "game");
                dismissProgressDialog();
            }
        }).start();
    }

    public void exitGameFragment() {
        stopAll();
        isInterrupt = true;
        gameFragment.stopCounter();
        gameFragment.dismiss();
        gameFragment = null;
    }

    public void enterQuestionnaire() {

        showProgressDialog("Loading quesetionnaire");

        new Thread(new Runnable() {
            @Override
            public void run() {
                stopAll();
                changeLanguage((int) Application.getmInstance().loadLanguage(), false, false, true);
                questionnaireFragment = QuestionnaireFragment.newInstance();
                questionnaireFragment.show(getSupportFragmentManager(), "question");
                dismissProgressDialog();
            }
        }).start();
    }

    public void exitQuestFragment() {
        stopAll();
        isInterrupt = true;
//      questionnaireFragment.stopCounter();
        questionnaireFragment.dismiss();
        questionnaireFragment = null;
    }

    @Override
    public void onBackPressed() {

        if (gameFragment != null) {
            exitGameFragment();
            askServices(Application.getmInstance().loadLanguage());
            return;
        }
        if (questionnaireFragment != null) {
            exitQuestFragment();
            askServices(Application.getmInstance().loadLanguage());

        }
        if (gameFragment == null && questionnaireFragment == null) {

            System.out.println("game questionnaireFragment is null");

            super.onBackPressed();
        }
    }


    /*
         * Preparing the list data
         */
    private void upadateListData(int value) {

        listDataHeader.clear();

        if (value == MainActivity.LANGUAGE_CANTONESE) {
            listDataHeader.add("趣怪動作");
            listDataHeader.add("教育資訊");
            listDataHeader.add("最新消息");
            listDataHeader.add("互動遊戲");
        } else if (value == MainActivity.LANGUAGE_CHINESE) {
            listDataHeader.add("趣怪动作");
            listDataHeader.add("教育资讯");
            listDataHeader.add("最新消息");
            listDataHeader.add("互动游戏");
        } else if (value == MainActivity.LANGUAGE_ENGLISH) {
            listDataHeader.add("fun movement");
            listDataHeader.add("education");
            listDataHeader.add("news");
            listDataHeader.add("Games");
        }
        // Adding child data
        List<String> fun = new ArrayList<String>();

        for (int i = 0; i < Constant.jokes.length; i++) {
            fun.add((i + 1) + ". " + Application.speech.get(0).get(Application.getmInstance().loadLanguage())[i]);
        }
        List<String> edu = new ArrayList<String>();
        for (int i = 0; i < Constant.educations.length; i++) {
            edu.add((i + 1) + ". " + "edu" + (i + 1));
        }


        List<String> news = new ArrayList<String>();
        for (int i = 0; i < Constant.commercial.length; i++) {
            news.add((i + 1) + ". " + "news" + (i + 1));
        }

        List<String> games = new ArrayList<String>();
        for (int i = 0; i < Constant.games.length; i++) {
            games.add((i + 1) + ". " + Application.speech.get(3).get(Application.getmInstance().loadLanguage())[i]);
        }

        listDataChild.put(listDataHeader.get(0), fun); // Header, Child data
        listDataChild.put(listDataHeader.get(1), edu);
        listDataChild.put(listDataHeader.get(2), news);
        listDataChild.put(listDataHeader.get(3), games);


        listAdapter = new ExpandableListAdapter(MainActivity.this, listDataHeader, listDataChild);
        // setting list adapter
        expListView.setAdapter(listAdapter);

    }

    private void prepareListData() {

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        upadateListData(MainActivity.LANGUAGE_CANTONESE);
        expListView.setOnChildClickListener(new OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                if (session.isConnected()) {

                    final String a = Application.speech.get(groupPosition).get(Application.getmInstance().loadLanguage())[childPosition];

                    if (groupPosition > 0 && groupPosition < 3) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    alMotion.setStiffnesses("Body", 1f);
                                    alRobotPosture.goToPosture("Stand", 0.5f);
                                    Log.d(Application.TAG, "stand: 6");
                                    alAnimatedSpeech.setBodyLanguageMode(2);
                                    alAnimatedSpeech.say(a);
                                    //alMotion.setStiffnesses("Body", 0f);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    } else if (groupPosition == 0) {
                        try {
                            alMotion.setStiffnesses("Body", 1f);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    alBehaviorManager.runBehavior("boc/" + a);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    } else if (groupPosition == 3) {
                        if (childPosition == 0) {
                            enterGame();
                        }
                    }
                }

                return false;
            }
        });
    }

    public void subscribeMiddle() {

        try {
            alMemory.unsubscribeToEvent(Application.getmInstance().loadMiddleId());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {

            long middleHead = alMemory.subscribeToEvent("MiddleTactilTouched", new EventCallback() {
                @Override
                public void onEvent(Object o) throws InterruptedException, CallError {
                    stopAll();
                }
            });
            Application.getmInstance().saveMiddleId(middleHead);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void subscribeLeftBumper() {

        try {
            alMemory.unsubscribeToEvent(Application.getmInstance().loadSubscribeId());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {

            long middleHead = alMemory.subscribeToEvent("LeftBumperPressed", new EventCallback() {
                @Override
                public void onEvent(Object o) throws InterruptedException, CallError {

                    synchronized (lockForTouch) {

                        Log.d(Application.TAG, "onEvent: touch: " + touchIsInProgress);

                        if (touchIsInProgress) {
                            return;
                        } else {
                            touchIsInProgress = true;
                        }

                        if (loading != null) {
                            Log.d(Application.TAG, "onEvent: load is not null");
                            return;
                        } else {
                            Log.d(Application.TAG, "onEvent: load is null");
                        }

                        if (gameFragment != null) {
                            exitGameFragment();
                        }
                        if (questionnaireFragment != null) {
                            exitQuestFragment();
                        }
                        if (!isPlayingMenu) {
                            askServices(Application.getmInstance().loadLanguage());
                        }
                    }
                }
            });

            Application.getmInstance().saveHeadMiddleTouch(middleHead);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void checkFlag(String location) {

        Log.d(Application.TAG, "checkFlag: current location is: " + location + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Log.d(Application.TAG, "checkFlag: isInterrupt: " + this.isInterrupt);
        Log.d(Application.TAG, "checkFlag: playingSpeech: " + this.playingSpeech);
        Log.d(Application.TAG, "checkFlag: speechLoopEnable: " + this.speechLoopEnable);


    }

    public void subscribeRightBumper() {

        try {
            alMemory.unsubscribeToEvent(Application.getmInstance().loadHeadBackTouch());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            long headBack = alMemory.subscribeToEvent("RightBumperPressed", new EventCallback() {
                @Override
                public void onEvent(Object o) throws InterruptedException, CallError {

                    synchronized (lockForTouch) {

                        Log.d(Application.TAG, "onEvent: touch: " + touchIsInProgress);

                        if (touchIsInProgress) {
                            return;
                        } else {
                            touchIsInProgress = true;
                        }

                        if (loading != null) {
                            Log.d(Application.TAG, "onEvent: loading is not null");
                            return;
                        } else {
                            Log.d(Application.TAG, "onEvent: loading is null");
                        }

                        if (gameFragment != null) {
                            exitGameFragment();
                        }
                        if (questionnaireFragment != null) {
                            exitQuestFragment();
                        }
                        if (!isPlayingMenu) {
                            askLanguage();
                        }
                    }

                }
            });

            Application.getmInstance().saveHeadBackTouch(headBack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void unsubscribeFace() {
//
//        try {
//            // TODO: 4/12/16
//            noPeopleTimeEnable = false;
//
//            Log.d(Application.TAG, "unsubscribeFace: " + aLFaceDetection.getSubscribersInfo().toString());
//
//            ArrayList<ArrayList<String>> list = (ArrayList<ArrayList<String>>) aLFaceDetection.getSubscribersInfo();
//
//            for (ArrayList<String> item : list) {
//                aLFaceDetection.unsubscribe(item.get(0));
//            }
//
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

//    public void subscribeFace() {
//        try {
//            Log.d(Application.TAG, "subscribeFace ");
//
//            try {
//
//                aLFaceDetection.subscribe("Test_Face", 1000, 0.0f);
//                Log.d(Application.TAG, "subscribeFace: " + aLFaceDetection.getSubscribersInfo().toString());
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//
//            try {
//                alMemory.unsubscribeToEvent(Application.getmInstance().loadSubscribeId());
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//
//            long faceid = alMemory.subscribeToEvent("FaceDetected", new EventCallback() {
//                @Override
//                public void onEvent(Object o) throws InterruptedException, CallError {
//
//                    Log.d(Application.TAG, "onEvent: enter!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//
//
//                    Log.d(Application.TAG, noPeopleTime + " " + startSpeechStatus + " " + lastFaces);
//                    String faceData = alMemory.getData("FaceDetected", 0).toString();
//                    if (lastFaces.equals("[]") && !faceData.equals("[]") && noPeopleTime > longLeaveTime) { // from 0 to n after 10s
//
//                        Log.d(Application.TAG, "First meet people");
//                        noPeopleTime = 0;
//                        lastFaces = faceData;
//                        startSpeechStatus = 0;
//                        askServices(Application.getmInstance().loadLanguage());
//                    } else if (lastFaces.equals("[]") && !faceData.equals("[]") && noPeopleTime <= longLeaveTime) {
//                        noPeopleTime = 0;
//                        noPeopleTimeEnable = false;
//                        lastFaces = faceData;
//                        startSpeechStatus = 1;
//                        if (noPeopleTime > shortLeaveTime) {
//                            Log.d(Application.TAG, "People back from long leave");
//                            askServices(Application.getmInstance().loadLanguage());
//                        } else {
//                            Log.d(Application.TAG, "People back from short leave");
//                        }
//                    } else if (faceData.equals("[]")) {//no people , start timer
//                        Log.d(Application.TAG, "I see no people");
//                        lastFaces = "[]";
//                        noPeopleTimeEnable = true;
//                        noPeopleTime = 0;
//
//                        runnable = new Runnable() {
//                            @Override
//                            public void run() {
//                                Log.d(Application.TAG, "run: hander!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//                                if (noPeopleTimeEnable) {
//                                    noPeopleTime = noPeopleTime + 1;
//                                    Log.d("tests", "noPeopleTime: " + noPeopleTime);
//                                    if (noPeopleTime <= longLeaveTime) {
//                                        handler.postDelayed(runnable, 1000);
//                                    } else if (noPeopleTime == 31) {
//                                        Log.d(Application.TAG, "First meet people");
//                                        noPeopleTime = 0;
//                                        startSpeechStatus = 0;
//                                        askServices(Application.getmInstance().loadLanguage());
//                                    } else {
//                                        startSpeechStatus = 0;
//                                        noPeopleTimeEnable = false;
//                                    }
//                                }
//                            }
//                        };
//                        handler.postDelayed(runnable, 1000);
//                    }
//                }
//            });
//
////            Log.d(Application.TAG, "subscribeFace: " + faceRecogID);
//            Application.getmInstance().saveSubscribeId(faceid);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public void stopAll() {

        synchronized (lockForStopAll) {
            try {
                Log.d(Application.TAG, "stopAll: stopAll is run");
                unSubcribeASR("stopAll");
                speechLoopEnable = false;
                isPlayingMenu = false;
                setPlayingSpeech(false, "stopAll()");

//            playingSpeech = false;
                isInterrupt = false;
                alMotion.stopMove();
                alTextToSpeech.stopAll();
                alBehaviorManager.stopAllBehaviors();
                alMotion.setStiffnesses("Body", 1f);
                alRobotPosture.goToPosture("Stand", 0.5f);
            } catch (CallError callError) {
                callError.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public void askLanguage() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                stopAll();
                isPlayingMenu = true;
                speechLoopEnable = true;
                for (int j = 0; j < 7; j++) {
                    if (speechLoopEnable) {
                        switch (j) {
                            case 0:
                                changeLanguage(MainActivity.LANGUAGE_CANTONESE, false, false, false);
                                break;
                            case 1:
                                sayTextAnimated("你好， 選擇廣東話.請說.A.", SayCallBackEvent.AFTER_COMMON_LANGUAGE, true);
                                break;
                            case 2:
                                changeLanguage(MainActivity.LANGUAGE_CHINESE, false, false, false);
                                break;
                            case 3:
                                sayTextAnimated("你好， 選擇普通话.請說.B.", SayCallBackEvent.AFTER_COMMON_LANGUAGE, true);
                                break;
                            case 4:
                                changeLanguage(MainActivity.LANGUAGE_ENGLISH, false, false, false);
                                break;
                            case 5:
                                sayTextAnimated("Hello. Use English. please say.C.", SayCallBackEvent.AFTER_COMMON_LANGUAGE, true);
                                break;
                        }
                    }
                }

                touchIsInProgress = false;
                subscribeASR(MainActivity.LANGUAGE, "askLanguage");
                EventBus.getDefault().post(new SayCallBackEvent(SayCallBackEvent.AFTER_CHOOSEN_LANGUAGE));
                EventBus.getDefault().post(new PlayingMenuEvent(false));
            }
        }).start();
    }

    public void askServices(final int language) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                stopAll();
                isPlayingMenu = true;
                speechLoopEnable = true;
                try {
                    if (language == MainActivity.LANGUAGE_CANTONESE) {

                        for (int j = 0; j < 2; j++) {
                            if (speechLoopEnable) {
                                switch (j) {
                                    case 0:
                                        changeLanguage(MainActivity.LANGUAGE_CANTONESE, false, false, false);
                                        break;
                                    case 1:
                                        sayTextAnimated(Constant.startSpeech[0], SayCallBackEvent.AFTER_SERVICE_SPEECH, true);
                                        break;
                                }
                            }
                        }

                    } else if (language == MainActivity.LANGUAGE_CHINESE) {
                        for (int j = 0; j < 2; j++) {
                            if (speechLoopEnable) {
                                switch (j) {
                                    case 0:
                                        changeLanguage(MainActivity.LANGUAGE_CHINESE, false, false, false);

                                        break;
                                    case 1:

                                        sayTextAnimated(Constant.startSpeechCn[0], SayCallBackEvent.AFTER_SERVICE_SPEECH, true);
                                        break;
                                }
                            }
                        }
                    } else if (language == MainActivity.LANGUAGE_ENGLISH) {

                        for (int j = 0; j < 2; j++) {
                            if (speechLoopEnable) {
                                switch (j) {
                                    case 0:
                                        changeLanguage(MainActivity.LANGUAGE_ENGLISH, false, false, false);

                                        break;
                                    case 1:
                                        sayTextAnimated(Constant.startSpeechEng[0], SayCallBackEvent.AFTER_SERVICE_SPEECH, true);
                                        break;
                                }
                            }
                        }
                    }

                    touchIsInProgress = false;
                    EventBus.getDefault().post(new PlayingMenuEvent(false));

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }


    public void unSubcribeASR(String location) {

        Log.d(Application.TAG, "unSubcribeASR: unsubcribe from: " + location);

        try {
//            noPeopleTimeEnable = false;
            try {
                alMemory.unsubscribeToEvent(Application.getmInstance().loadASRId());
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            ArrayList<ArrayList<String>> list = (ArrayList<ArrayList<String>>) alSpeechRecognition.getSubscribersInfo();

            for (ArrayList<String> item : list) {
                alSpeechRecognition.unsubscribe(item.get(0));
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        alRobotPosture.goToPosture("Stand", 1f);
                    } catch (CallError callError) {
                        callError.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (CallError callError) {
            callError.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void setPlayingSpeech(boolean isPlaying, String location) {

        Log.d(Application.TAG, "setPlayingSpeech: " + location + " with value: " + isPlaying);

        playingSpeech = isPlaying;

    }

    public void sayText(final String text, final int callbackType, boolean isBlock) {

        setPlayingSpeech(true, "sayText()");
//      playingSpeech = true;

        try {

            if (!isBlock) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            alTextToSpeech.say(text);
                            EventBus.getDefault().post(new SayCallBackEvent(callbackType));
                        } catch (CallError callError) {
                            callError.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } else {
                alTextToSpeech.say(text);
                EventBus.getDefault().post(new SayCallBackEvent(callbackType));
            }

        } catch (CallError callError) {
            callError.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }


    public void sayTextAnimated(final String text, final int callbackType, boolean isBlock) {

        setPlayingSpeech(true, "sayTextAnimated()");
//        playingSpeech = true;

        try {
            if (!isBlock) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        try {
                            alAnimatedSpeech.say(text);
                            EventBus.getDefault().post(new SayCallBackEvent(callbackType));
                        } catch (CallError callError) {
                            callError.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } else {
                alAnimatedSpeech.say(text);
                EventBus.getDefault().post(new SayCallBackEvent(callbackType));
            }
        } catch (CallError callError) {
            callError.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void blockingAction(final int action) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (action == REST) {
                        alMotion.rest();
                    } else if (action == WAKE) {
                        alMotion.wakeUp();
                    }
                } catch (CallError callError) {
                    callError.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void onEvent(PlayingMenuEvent event) {

        isPlayingMenu = event.isPlaying;

    }

    public void onEvent(SayCallBackEvent event) {

        setPlayingSpeech(false, "onEvent(SayCallBackEvent)");

//        playingSpeech = false;

        if (event.event == SayCallBackEvent.CHOSEN_LANGUAGE) {
            System.out.println("enter chosen language event");
            askServices(Application.getmInstance().loadLanguage());
//            subscribeASR(MainActivity.COMMAND, "chosen_language event");
        } else if (event.event == SayCallBackEvent.AFTER_SERVICE_SPEECH) {
            subscribeASR(COMMAND, "after service event");
            dismissProgressDialog();
        } else if (event.event == SayCallBackEvent.AFTER_CHOOSEN_LANGUAGE) {
            //do nothing
        } else if (event.event == SayCallBackEvent.AFTER_COMMON_LANGUAGE) {
            //do nothing
        }
    }

    public void onEvent(CloseAllThenCloseSession event) {

        try {
            session.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    statusText.setText("status: robot disconnected");
                    okBtn.setEnabled(true);
                }
            });
        }
    }

    public void subscribeASR(final int action, String location) {

        synchronized (lockForStopAll) {


            Log.d(Application.TAG, "subscribeASR: Start subscribe processess from location: " + location);

            Log.d(Application.TAG, "subscribeASR: unSubscribe the ASR for safety");

            unSubcribeASR("subscribeASR");

            Log.d(Application.TAG, "subscribeASR: " + "starting subscribe after unsubscribe asr");

            try {
                int choiceNum = 0;
                if (action == MainActivity.YES_NO_QUESTION) {
                    choiceNum = 2;
                } else if (action == MainActivity.LANGUAGE) {
                    choiceNum = 3;
                } else if (action == MainActivity.COMMAND) {
                    choiceNum = 5;
                } else if (action == MainActivity.PLAY_GAMES) {
                    choiceNum = 4;
                }
                abc.clear();
                for (int k = 1; k <= choiceNum; k++) {
                    switch (k) {
                        case 1:
                            abc.add("a");
                            break;
                        case 2:
                            abc.add("b");
                            break;
                        case 3:
                            abc.add("c");
                            break;
                        case 4:
                            abc.add("d");
                            break;
                        case 5:
                            abc.add("e");
                            break;
                        case 6:
                            abc.add("f");
                            break;
                        case 7:
                            abc.add("g");
                            break;
                        case 8:
                            abc.add("h");
                            break;
                        case 9:
                            abc.add("i");
                            break;
                        case 10:
                            abc.add("j");
                            break;
                    }
                }
                Log.d(Application.TAG, "subscribeASR: listen to"+ abc);
                alSpeechRecognition.setVocabulary(abc, false);

                alSpeechRecognition.subscribe("Main_ASR", 1000, 0.0f);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (CallError callError) {
                callError.printStackTrace();
            }

            try {

                long asrid = alMemory.subscribeToEvent("WordRecognized", new EventCallback() {

                    @Override
                    public void onEvent(Object o) throws InterruptedException, CallError {

                        Log.d(Application.TAG, "onEvent: " + o.toString());
                        Log.d(Application.TAG, "onEvent: " + alMemory.getData("WordRecognized"));
                        ArrayList a = (ArrayList) (alMemory.getData("WordRecognized"));

                        String receivedText = a.get(0).toString();
                        Float confident = Float.parseFloat(a.get(1).toString());

                        if (confident >= 0.4f) {

                            unSubcribeASR("subscribeASR");

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        alRobotPosture.goToPosture("Stand", 0.6f);
                                    } catch (CallError callError) {
                                        callError.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();

                            alTextToSpeech.say(receivedText);

                            if (action == MainActivity.LANGUAGE) {
                                switch (receivedText) {
                                    case "a":
                                        changeLanguage(MainActivity.LANGUAGE_CANTONESE, true, true, true);
                                        sayText("你選擇廣東話...", SayCallBackEvent.CHOSEN_LANGUAGE, true);
                                        break;
                                    case "b":
                                        changeLanguage(MainActivity.LANGUAGE_CHINESE, true, true, true);
                                        sayText("你選擇普通话...", SayCallBackEvent.CHOSEN_LANGUAGE, true);
                                        break;
                                    case "c":
                                        changeLanguage(MainActivity.LANGUAGE_ENGLISH, true, true, true);
                                        sayText("You have chosen English...", SayCallBackEvent.CHOSEN_LANGUAGE, true);
                                        break;
                                }
                            } else if (action == MainActivity.PLAY_GAMES) {
                                switch (receivedText) {
                                    case "a":
                                        EventBus.getDefault().post(new ChooseAnswer(0));
                                        break;
                                    case "b":
                                        EventBus.getDefault().post(new ChooseAnswer(1));
                                        break;
                                    case "c":
                                        EventBus.getDefault().post(new ChooseAnswer(2));
                                        break;
                                    case "d":
                                        EventBus.getDefault().post(new ChooseAnswer(3));
                                        break;
                                }
                            } else if (action == MainActivity.COMMAND) {
                                switch (receivedText) {
                                    case "a":
                                        enterQuestionnaire();
                                        break;
                                    case "b":
                                        robotService(MainActivity.SERVICES_NEWS);
                                        break;
                                    case "c":
                                        enterGame();
                                        break;
                                    case "d":
                                        robotService(MainActivity.SERVICES_FUN);
                                        break;
                                    case "e":
                                        robotService(MainActivity.SERVICES_EDUCATION);
                                        break;
                                }
                            } else if (action == MainActivity.YES_NO_QUESTION) {
                                switch (receivedText) {
                                    case "a":
                                        EventBus.getDefault().post(new TrueFalseAnswer(true));
                                        break;
                                    case "b":
                                        EventBus.getDefault().post(new TrueFalseAnswer(false));
                                        break;
                                }
                            }
                        }
                    }
                });

                Application.getmInstance().saveASRId(asrid);


                //putting up the hand for listening

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        puttingUpHand();
                    }
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void subscribeASR(final int action, String location, int questionQID) {

        synchronized (lockForStopAll) {


            Log.d(Application.TAG, "subscribeASR: Start subscribe processess from location: " + location);

            Log.d(Application.TAG, "subscribeASR: unSubscribe the ASR for safety");

            unSubcribeASR("subscribeASR");

            Log.d(Application.TAG, "subscribeASR: " + "starting subscribe after unsubscribe asr");

            try {
                int choiceNum = 0;
                if (action == MainActivity.YES_NO_QUESTION) {
                    choiceNum = 2;
                } else if (action == MainActivity.LANGUAGE) {
                    choiceNum = 3;
                } else if (action == MainActivity.COMMAND) {
                    choiceNum = 5;
                } else if (action == MainActivity.PLAY_GAMES) {
                    choiceNum = 4;
                } else if (action == MainActivity.DO_QUESTIONNAIRE) {
                    choiceNum = Application.questionCN.get(questionQID).length - 1;
                }
                abc.clear();
                for (int k = 1; k <= choiceNum; k++) {
                    switch (k) {
                        case 1:
                            abc.add("a");
                            break;
                        case 2:
                            abc.add("b");
                            break;
                        case 3:
                            abc.add("c");
                            break;
                        case 4:
                            abc.add("d");
                            break;
                        case 5:
                            abc.add("e");
                            break;
                        case 6:
                            abc.add("f");
                            break;
                        case 7:
                            abc.add("g");
                            break;
                        case 8:
                            abc.add("h");
                            break;
                        case 9:
                            abc.add("i");
                            break;
                        case 10:
                            abc.add("j");
                            break;
                    }
                }
                Log.d(Application.TAG, "subscribeASR choiceNum: " + choiceNum);
                alSpeechRecognition.setVocabulary(abc, false);
                alSpeechRecognition.subscribe("Main_ASR", 1000, 0.0f);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (CallError callError) {
                callError.printStackTrace();
            }

            try {

                long asrid = alMemory.subscribeToEvent("WordRecognized", new EventCallback() {

                    @Override
                    public void onEvent(Object o) throws InterruptedException, CallError {

                        Log.d(Application.TAG, "onEvent: " + o.toString());
                        Log.d(Application.TAG, "onEvent: " + alMemory.getData("WordRecognized"));
                        ArrayList a = (ArrayList) (alMemory.getData("WordRecognized"));

                        String receivedText = a.get(0).toString();
                        Float confident = Float.parseFloat(a.get(1).toString());

                        if (confident >= 0.4f) {

                            unSubcribeASR("subscribeASR");

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        alRobotPosture.goToPosture("Stand", 0.6f);
                                    } catch (CallError callError) {
                                        callError.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();

                            //alTextToSpeech.say(receivedText);

                            if (action == MainActivity.LANGUAGE) {
                                switch (receivedText) {
                                    case "a":
                                        changeLanguage(MainActivity.LANGUAGE_CANTONESE, true, true, true);
                                        sayText("你選擇廣東話...", SayCallBackEvent.CHOSEN_LANGUAGE, true);
                                        break;
                                    case "b":
                                        changeLanguage(MainActivity.LANGUAGE_CHINESE, true, true, true);
                                        sayText("你選擇普通话...", SayCallBackEvent.CHOSEN_LANGUAGE, true);
                                        break;
                                    case "c":
                                        changeLanguage(MainActivity.LANGUAGE_ENGLISH, true, true, true);
                                        sayText("You have chosen English...", SayCallBackEvent.CHOSEN_LANGUAGE, true);
                                        break;
                                }
                            } else if (action == MainActivity.PLAY_GAMES) {
                                switch (receivedText) {
                                    case "a":
                                        EventBus.getDefault().post(new ChooseAnswer(0));
                                        break;
                                    case "b":
                                        EventBus.getDefault().post(new ChooseAnswer(1));
                                        break;
                                    case "c":
                                        EventBus.getDefault().post(new ChooseAnswer(2));
                                        break;
                                    case "d":
                                        EventBus.getDefault().post(new ChooseAnswer(3));
                                        break;
                                }
                            } else if (action == MainActivity.COMMAND) {
                                switch (receivedText) {
                                    case "a":
                                        enterQuestionnaire();
                                        break;
                                    case "b":
                                        robotService(MainActivity.SERVICES_NEWS);
                                        break;
                                    case "c":
                                        enterGame();
                                        break;
                                    case "d":
                                        robotService(MainActivity.SERVICES_FUN);
                                        break;
                                    case "e":
                                        robotService(MainActivity.SERVICES_EDUCATION);
                                        break;
                                }
                            } else if (action == MainActivity.DO_QUESTIONNAIRE) {
                                switch (receivedText) {
                                    case "a":
                                        EventBus.getDefault().post(new ChooseChoice(0));
                                        break;
                                    case "b":
                                        EventBus.getDefault().post(new ChooseChoice(1));
                                        break;
                                    case "c":
                                        EventBus.getDefault().post(new ChooseChoice(2));
                                        break;
                                    case "d":
                                        EventBus.getDefault().post(new ChooseChoice(3));
                                        break;
                                    case "e":
                                        EventBus.getDefault().post(new ChooseChoice(4));
                                        break;
                                    case "f":
                                        EventBus.getDefault().post(new ChooseChoice(5));
                                        break;
                                    case "g":
                                        EventBus.getDefault().post(new ChooseChoice(6));
                                        break;
                                    case "h":
                                        EventBus.getDefault().post(new ChooseChoice(7));
                                        break;
                                    case "i":
                                        EventBus.getDefault().post(new ChooseChoice(8));
                                        break;
                                    case "j":
                                        EventBus.getDefault().post(new ChooseChoice(9));
                                        break;
                                }
                            } else if (action == MainActivity.YES_NO_QUESTION) {
                                switch (receivedText) {
                                    case "a":
                                        EventBus.getDefault().post(new TrueFalseAnswer(true));
                                        break;
                                    case "b":
                                        EventBus.getDefault().post(new TrueFalseAnswer(false));
                                        break;
                                }
                            }

                        }
                    }
                });

                Application.getmInstance().saveASRId(asrid);


                //putting up the hand for listening

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        puttingUpHand();
                    }
                }).start();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public void puttingUpHand() {
        try {
            Log.d(Application.TAG, "puttingUpHand: putting up hand is called");
            alBehaviorManager.stopAllBehaviors();
            alBehaviorManager.runBehavior("boc/listen");
        } catch (CallError callError) {
            callError.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}