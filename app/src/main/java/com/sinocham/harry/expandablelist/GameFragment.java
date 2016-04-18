package com.sinocham.harry.expandablelist;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.aldebaran.qi.CallError;

import java.util.ArrayList;
import java.util.Random;

import de.greenrobot.event.EventBus;


public class GameFragment extends DialogFragment {

    //Data format:              {show question , a,b,c,d, ans,speech of nao}
    public final static String[] q1 = {"1+1=?", "27", "2", "34", "5", "2", "What is the result of one plus one", "一加一等於多少"};
    public final static String[] q2 = {"1+7=?", "2", "53", "89", "8", "8", "What is the result of one plus seven", "一加七等於多少"};
    public final static String[] q3 = {"2+8=?", "24", "15", "10", "90", "10", "what is the result of 2 plus eight", "二加八等於多少"};

    public final static String[] correctSpeechEng = {"Excellent! You are correct", "Correct, Very good", "Correct! It is too easy for you.", "Great! You are very smart!"};
    public final static String[] wrongSpeechEng = {"Oh no! you are wrong", "Not correct!", "You should practise more!", "Wrong! Practise makes perfect!"};
    public final static String[] correctSpeechCn = {"對你來說太簡單了", "你好聰明", "你好利害", "答對了"};
    public final static String[] wrongSpeechCn = {"加油！加油！", "努力！努力！", "答錯了", "太可惜了"};


    public static ArrayList<String[]> question = new ArrayList<>();
    public static int[] ansButtons = {-1, -1, -1, -1};
    private int currentQID = 2;
    private int lastQID = 2;
    public ProgressDialog loading;
    MainActivity mainActivity;
    View view;
    TextView questionText;
    Button buttonA;
    Button buttonB;
    Button buttonC;
    Button buttonD;

    private Object counterLock; //for counter lock synchronization
    private Thread counterThread; //for counter thread interruption
    private boolean isCounting; //for counter status

    private Random rand = new Random();

    public int counterActionSpeech = 0;


    @Override
    public void onStart() {
        super.onStart();
//        getDialog().getWindow().setLayout(Application.width, Application.height);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        question.add(q1);
        question.add(q2);
        question.add(q3);

        mainActivity = (MainActivity) getActivity();

        counterThread = new Thread();

        view = inflater.inflate(R.layout.game_fragment, container, false);
        questionText = (TextView) view.findViewById(R.id.textView2);
        buttonA = (Button) view.findViewById(R.id.button1);
        buttonB = (Button) view.findViewById(R.id.button2);
        buttonC = (Button) view.findViewById(R.id.button3);
        buttonD = (Button) view.findViewById(R.id.button4);

        counterLock = new Object();

        buttonA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.unSubcribeASR("a button");
                chooseAns(0);
            }
        });

        buttonB.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mainActivity.unSubcribeASR("b button");
                chooseAns(1);
            }
        });

        buttonC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.unSubcribeASR("c button");
                chooseAns(2);
            }
        });

        buttonD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.unSubcribeASR("d button");
                chooseAns(3);
            }
        });

        askQuestion();

        return view;
    }

    public void onEvent(ChooseAnswer event) {
        chooseAns(event.answer);
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

    public void askQuestion() {

        mainActivity.isInterrupt = false;

        while (currentQID == lastQID) {
            currentQID = rand.nextInt(question.size());
        }

        lastQID = currentQID;

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                questionText.setText(question.get(currentQID)[0]);

                for (int i = 0; i < 4; i++) {
                    ansButtons[i] = i + 1;
                    switch (i) {
                        case 0:
                            buttonA.setText(question.get(currentQID)[1]);
                            break;
                        case 1:
                            buttonB.setText(question.get(currentQID)[2]);
                            break;
                        case 2:
                            buttonC.setText(question.get(currentQID)[3]);
                            break;
                        case 3:
                            buttonD.setText(question.get(currentQID)[4]);
                            break;
                    }
                }
            }
        });

        try {
            String currentLanguage = mainActivity.alTextToSpeech.getLanguage().toString();
            String resultText = "";

            switch (currentLanguage) {
                case "CantoneseHK":
                    resultText = question.get(currentQID)[7] + ". 答案. ''. 'ae'. " + "' '. " + "'" + question.get(currentQID)[1] + "'. ' '. " + "'B'. ' '. " + "'" + question.get(currentQID)[2] + "'. ' '. " + "'c'. ' '. " + "'" + question.get(currentQID)[3] + "'. ' '. " + "'d'. ' '. " + "'" + question.get(currentQID)[4] + "'. ' '. ";
                    break;
                case "Chinese":
                    resultText = question.get(currentQID)[7] + ".   答案' '. 'ae'. " + "' '. " + "'" + question.get(currentQID)[1] + "'. ' '. " + "'B'. ' '. " + "'" + question.get(currentQID)[2] + "'. ' '. " + "'c'. ' '. " + "'" + question.get(currentQID)[3] + "'. ' '. " + "'d'. ' '. " + "'" + question.get(currentQID)[4] + "'. ' '. ";
                    break;
                case "English":
                    resultText = question.get(currentQID)[6] + ".   Answer' '. 'A'. " + "'' " + "'" + question.get(currentQID)[1] + "'. '' " + "'B'. '' " + "'" + question.get(currentQID)[2] + "'. '' " + "'c'. '' " + "'" + question.get(currentQID)[3] + "'. '' " + "'d'. '' " + "'" + question.get(currentQID)[4] + "'. '' ";
                    break;
            }

            mainActivity.sayText(resultText, SayCallBackEvent.QUESTION_START, false);

        } catch (CallError callError) {
            callError.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                mainActivity.isInterrupt = true;
                mainActivity.exitGameFragment();

                //do your stuff
            }
        };
    }

    public void onEvent(SayCallBackEvent event) {

        if (event.event == SayCallBackEvent.GAME_TIME_IS_UP) {
            mainActivity.unSubcribeASR("Game_TIME_IS_UP event");
            askQuestion();
        } else if (event.event == SayCallBackEvent.QUESTION_START) {

            Log.d(Application.TAG, "onEvent: enter question start");
            if (!mainActivity.isInterrupt) {
                mainActivity.subscribeASR(MainActivity.PLAY_GAMES, "question_start event");
                startCounter();
            } else {
                System.out.println("interrupt is true");
                mainActivity.isInterrupt = false;
            }
        } else if (event.event == SayCallBackEvent.GAME_ANSWER_FEEDBACK) {
            EventBus.getDefault().post(new FinishActionSpeech());
        }
    }

    public void onEvent(FinishActionSpeech event) {
        counterActionSpeech++;
        if (counterActionSpeech >= 2) {
            counterActionSpeech = 0;
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loading.dismiss();
                    askQuestion();
                }
            });
        }
    }

    public void onEventMainThread(ProgressDialogEvent event) {
        String result;
        if (event.check) {
            result = "              Correct!\n loading next question";
        } else {
            result = "              Wrong!\n Loading next question";
        }

        loading = ProgressDialog.show(getActivity(), "", result, true);

    }


    public void chooseAns(int ans) {

        stopCounter();
        mainActivity.isInterrupt = true;

        Log.d(Application.TAG, "chooseAns: entered" + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

        try {
            mainActivity.alTextToSpeech.stopAll();
        } catch (CallError callError) {
            callError.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (question.get(currentQID)[ansButtons[ans]].equals(question.get(currentQID)[5])) {
            //Toast.makeText(getActivity(), "correct", Toast.LENGTH_SHORT).show();
            mcCorrect();
        } else {
            //Toast.makeText(getActivity(), "wrong", Toast.LENGTH_SHORT).show();
            mcWrong();
        }
    }

    public void stopCounter() {
        Log.d(Application.TAG, "stopCounter: stop counter is called");
        synchronized (counterLock) {
            isCounting = false;
        }
        counterThread.interrupt();
    }

    public void startCounter() {

        synchronized (counterLock) {
            isCounting = true;
        }

        counterThread = new Thread(new Runnable() {
            @Override
            public void run() {

                int counter = 0;

                while (isCounting) {

                    Log.d(Application.TAG, "run: counter is countering");

                    if (getDialog() == null) {
                        isCounting = false;
                        Log.d(Application.TAG, "run: close counter successful");
                        continue;
                    }

                    counter++;

                    if (counter == 20) {
                        try {
                            String tempLanguage = mainActivity.alTextToSpeech.getLanguage();
                            switch (tempLanguage) {
                                case "CantoneseHK":
                                    tempLanguage = "夠鐘";
                                    break;
                                case "Chinese":
                                    tempLanguage = "時間到了";
                                    break;
                                case "English":
                                    tempLanguage = "Time is up!";
                                    break;
                            }
                            final String temp = tempLanguage;
                            mainActivity.sayText(temp, SayCallBackEvent.GAME_TIME_IS_UP, false);
                            synchronized (counterLock) {
                                isCounting = false;
                            }
                        } catch (CallError callError) {
                            callError.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        System.out.println("stop conuter exception caught correctly");
                    }
                }
            }
        });

        counterThread.start();
    }

    public void mcCorrect() {
        if (mainActivity.session.isConnected()) {
            Log.d(" loading correct", "");
//                loading = ProgressDialog.show(getActivity(), "", "              Correct!\n loading next question", true);
            EventBus.getDefault().post(new ProgressDialogEvent(true));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mainActivity.alBehaviorManager.runBehavior("boc/correct0");
                        EventBus.getDefault().post(new FinishActionSpeech());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String nowLang = mainActivity.alTextToSpeech.getLanguage().toString();
                        if (nowLang.equals("English")) {
                            Log.d("tests", "running: " + nowLang);
                            mainActivity.sayText(correctSpeechEng[rand.nextInt(4)], SayCallBackEvent.GAME_ANSWER_FEEDBACK, false);
                        } else {
                            if (nowLang.equals("CantoneseHK") || nowLang.equals("Chinese")) {
                                Log.d("tests", "running: " + nowLang);
                                mainActivity.sayText(correctSpeechCn[rand.nextInt(4)], SayCallBackEvent.GAME_ANSWER_FEEDBACK, false);
                            } else {
                                Log.d("tests", "running: unknown language");
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (CallError callError) {
                        callError.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public void mcWrong() {
        if (mainActivity.session.isConnected()) {
            Log.d("Loading wrong", "");
            EventBus.getDefault().post(new ProgressDialogEvent(false));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        mainActivity.alBehaviorManager.runBehavior("boc/wrong0");
                        EventBus.getDefault().post(new FinishActionSpeech());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String nowLang = mainActivity.alTextToSpeech.getLanguage().toString();
                        if (nowLang.contains("English")) {
                            mainActivity.sayText(wrongSpeechEng[rand.nextInt(4)], SayCallBackEvent.GAME_ANSWER_FEEDBACK, false);
                        } else if (nowLang.contains("CantoneseHK") || nowLang.contains("Chinese")) {
                            mainActivity.sayText(wrongSpeechCn[rand.nextInt(4)], SayCallBackEvent.GAME_ANSWER_FEEDBACK, false);
                        } else {
                            Log.d("tests", "running: unknown language");
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (CallError callError) {
                        callError.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static GameFragment newInstance() {
        GameFragment f = new GameFragment();
        return f;
    }
}