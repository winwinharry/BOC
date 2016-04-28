package com.sinocham.harry.expandablelist;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.aldebaran.qi.CallError;

import java.util.Random;

import de.greenrobot.event.EventBus;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class QuestionnaireFragment extends DialogFragment {
    View view;
    MainActivity mainActivity;

    LinearLayout startPage;
    LinearLayout questionnairePage;
    LinearLayout endPage;
    TextView startText;
    Button startQuestion;
    Button quitQuest;
    LinearLayout questionLayout;
    LinearLayout choiceLayout;
    TextView resultText;
    Button resultBackBtn;
    FrameLayout frameLayoutID;
    TextView title;

    public ProgressDialog loading;
    private int currentQID = 0;
    private int lastQID = 0;

    private int questionnaireScore = 0;
    private String scoreHistory = "";
    private boolean zeroScore = false;
    private int q8choice = -1;
    private int q9Score = 0;

    private Object counterLock; //for counter lock synchronization
    private Object yesNoQLock;
    private Thread counterThread; //for counter thread interruption
    private boolean isCounting; //for counter status

    private Random rand = new Random();

    public int counterActionSpeech = 0;


    public QuestionnaireFragment() {
        // Required empty public constructor
    }


    public static QuestionnaireFragment newInstance(String param1, String param2) {
        QuestionnaireFragment fragment = new QuestionnaireFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        Log.d(Application.TAG, "onCreateView: change size");
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        view = inflater.inflate(R.layout.questionnaire_fragment, container, false);

        frameLayoutID = (FrameLayout) view.findViewById(R.id.frameLayoutID);
        frameLayoutID.setMinimumHeight(Application.height);
        frameLayoutID.setMinimumWidth(Application.width);

        startPage = (LinearLayout) view.findViewById(R.id.startPage);
        questionnairePage = (LinearLayout) view.findViewById(R.id.questionnairePage);
        endPage = (LinearLayout) view.findViewById(R.id.resultPage);
        startText = (TextView) view.findViewById(R.id.startText);
        startQuestion = (Button) view.findViewById(R.id.startQuest);
        quitQuest = (Button) view.findViewById(R.id.quitQuest);
        questionLayout = (LinearLayout) view.findViewById(R.id.question);
        choiceLayout = (LinearLayout) view.findViewById(R.id.answerList);
        resultText = (TextView) view.findViewById(R.id.resultText);
        title = (TextView) view.findViewById(R.id.title);


        if (getLanguageID() == mainActivity.LANGUAGE_CHINESE || getLanguageID() == mainActivity.LANGUAGE_CANTONESE) {
            Log.d(Application.TAG, "questfragment cn" + getLanguageID());
            title.setText("個人客戶投資取向問卷");
            startText.setText(Constant.QIPstartScriptCN);
            startQuestion.setText(Constant.QIPstartChoiceCN[0]);
            quitQuest.setText(Constant.QIPstartChoiceCN[1]);
        } else if (getLanguageID() == mainActivity.LANGUAGE_ENGLISH) {
            Log.d(Application.TAG, "questfragment eng" + getLanguageID());
            title.setText("Questionnaire on Inverstment Preference");
            startText.setText(Constant.QIPstartScriptENG);
            startQuestion.setText(Constant.QIPstartChoiceENG[0]);
            quitQuest.setText(Constant.QIPstartChoiceENG[1]);
        } else {
            Log.d(Application.TAG, "onCreateView: change language error" + getLanguageID());
        }

        resultBackBtn = (Button) view.findViewById(R.id.resultBackBtn);

        startQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startPage.setVisibility(View.INVISIBLE);
                    }
                });
                mainActivity.isInterrupt = true;
                EventBus.getDefault().post(new ProgressDialogEvent(true));//always true for questionnaire, dont change
                Log.d(Application.TAG, "QIP start!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                try {
                    mainActivity.alTextToSpeech.stopAll();
                } catch (CallError callError) {
                    callError.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mainActivity.unSubcribeASR("startQuestion button");

                if (getLanguageID() == mainActivity.LANGUAGE_CHINESE || getLanguageID() == mainActivity.LANGUAGE_CANTONESE) {
                    mainActivity.sayTextAnimated("問卷開始", SayCallBackEvent.GAME_ANSWER_FEEDBACK, false);
                } else if (getLanguageID() == mainActivity.LANGUAGE_ENGLISH) {
                    mainActivity.sayTextAnimated("Let's start", SayCallBackEvent.GAME_ANSWER_FEEDBACK, false);
                }


            }
        });
        quitQuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitQuestOnClick();
            }
        });
        resultBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quitQuestOnClick();
            }
        });
        String z ="";
        if (getLanguageID() == mainActivity.LANGUAGE_CHINESE || getLanguageID() == mainActivity.LANGUAGE_CANTONESE) {
            z = Constant.QIPstartScriptCN + " " + Constant.QIPstartChoiceCN[0] + " " + Constant.QIPstartChoiceCN[1];
        } else if (getLanguageID() == mainActivity.LANGUAGE_ENGLISH) {
            z = Constant.QIPstartScriptENG + " " + Constant.QIPstartChoiceENG[0] + " " + Constant.QIPstartChoiceENG[1];
        }

        mainActivity.sayTextAnimated(signToSpeech(z), SayCallBackEvent.ASK_TRUE_FALSE_QUESTION, false);


        return view;
    }

    public int getLanguageID() {
        String currentLanguage = "";
        try {
            currentLanguage = mainActivity.alTextToSpeech.getLanguage().toString();
        } catch (CallError callError) {
            callError.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int id = 0;
        switch (currentLanguage) {
            case "CantoneseHK":
                id = 0;
                break;
            case "Chinese":
                id = 1;
                break;
            case "English":
                id = 2;
                break;
        }
        return id;
    }

    public void quitQuestOnClick() {
        mainActivity.isInterrupt = true;
        mainActivity.unSubcribeASR("quitQuest button");
        try {
            mainActivity.alTextToSpeech.stopAll();
        } catch (CallError callError) {
            callError.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        EventBus.getDefault().post(new ProgressDialogEvent(true));//always true for questionnaire, dont change
        if (getLanguageID() == mainActivity.LANGUAGE_CHINESE || getLanguageID() == mainActivity.LANGUAGE_CANTONESE) {
            mainActivity.sayTextAnimated("返回主目錄", SayCallBackEvent.TRUE_FALSE_FEEDBACK, true);
        } else if (getLanguageID() == mainActivity.LANGUAGE_ENGLISH) {
            mainActivity.sayTextAnimated("Back", SayCallBackEvent.TRUE_FALSE_FEEDBACK, true);
        }
    }

    public boolean getInterrupt() {
        synchronized (yesNoQLock) {
            return mainActivity.isInterrupt;
        }
    }

    public void setInterrupt(boolean bool) {

        synchronized (yesNoQLock) {
            mainActivity.isInterrupt = bool;
        }

    }

    public void askQuestion() {
        mainActivity.isInterrupt = false;
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                questionLayout.removeAllViews();
                choiceLayout.removeAllViews();
                TextView tv = new TextView(view.getContext());
                TextView tv2 = new TextView(view.getContext());
                tv2.setText((currentQID + 1) + " / " + Application.questionCN.size());
                tv.setText("not show");
                if (getLanguageID() == mainActivity.LANGUAGE_CHINESE || getLanguageID() == mainActivity.LANGUAGE_CANTONESE) {
                    String y = Application.questionCN.get(currentQID)[0];
                    tv.setText(Constant.questionTitle[currentQID] + y);
                } else {// if (getLanguageID() == mainActivity.LANGUAGE_ENGLISH) {
                    String y = Application.questionENG.get(currentQID)[0];
                    tv.setText(Constant.questionTitle[currentQID] + y);
                }
                tv2.setTextSize(30f);
                tv.setTextSize(30f);
                questionLayout.addView(tv2);
                questionLayout.addView(tv);
                for (int j = 1; j < Application.questionCN.get(currentQID).length; j++) {
                    Button btn = new Button(view.getContext());
                    final int k = j - 1;
                    String z ="";
                    if (getLanguageID() == mainActivity.LANGUAGE_CHINESE || getLanguageID() == mainActivity.LANGUAGE_CANTONESE) {
                        z = Application.questionCN.get(currentQID)[j];
                    } else {// if (getLanguageID() == mainActivity.LANGUAGE_ENGLISH) {
                        z = Application.questionENG.get(currentQID)[j];
                    }
                    btn.setText(z);
                    btn.setTextSize(30f);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        btn.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                    }
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mainActivity.unSubcribeASR("choose " + Integer.toString(k));
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        for (int i = 0; i < choiceLayout.getChildCount(); i++) {
                                            View child = choiceLayout.getChildAt(i);
                                            child.setClickable(false);
                                        }
                                        mainActivity.alRobotPosture.goToPosture("Stand", 0.5f);
                                    } catch (CallError callError) {
                                        callError.printStackTrace();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                            chooseAns(k);
                        }
                    });
                    choiceLayout.addView(btn);
                }
            }
        });
        String z="";
        if (getLanguageID() == mainActivity.LANGUAGE_CHINESE || getLanguageID() == mainActivity.LANGUAGE_CANTONESE) {
            z = Application.questionCN.get(currentQID)[0];
        } else {// if (getLanguageID() == mainActivity.LANGUAGE_ENGLISH) {
            z = Application.questionENG.get(currentQID)[0];
        }
        for (int j = 1; j < Application.questionCN.get(currentQID).length; j++) {
            if (getLanguageID() == mainActivity.LANGUAGE_CHINESE || getLanguageID() == mainActivity.LANGUAGE_CANTONESE) {
                z += " " + Application.questionCN.get(currentQID)[j];
            } else {// if (getLanguageID() == mainActivity.LANGUAGE_ENGLISH) {
                z += " " + Application.questionENG.get(currentQID)[j];
            }


        }

        mainActivity.sayTextAnimated(signToSpeech(z), SayCallBackEvent.QUESTION_START, false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Dialog(getActivity(), getTheme()) {
            @Override
            public void onBackPressed() {
                mainActivity.isInterrupt = true;
                mainActivity.exitQuestFragment();

                //do your stuff
            }
        };
    }

    public void onEvent(SayCallBackEvent event) {

        if (event.event == SayCallBackEvent.GAME_TIME_IS_UP) {
            mainActivity.unSubcribeASR("Game_TIME_IS_UP event");
            askQuestion();
        } else if (event.event == SayCallBackEvent.QUESTION_START) {
            Log.d(Application.TAG, "onEvent: mc question asked, wait respond");
            if (!mainActivity.isInterrupt) {
                mainActivity.subscribeASR(MainActivity.DO_QUESTIONNAIRE, "Questionnaire_question_start event", currentQID);
//                startCounter();
                //// TODO: 4/26/16
            } else {
                System.out.println("interrupt is true");
                mainActivity.isInterrupt = false;
            }
        } else if (event.event == SayCallBackEvent.GAME_ANSWER_FEEDBACK) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loading.dismiss();
                    if (currentQID < Application.questionCN.size()) {
                        askQuestion();
                    } else {
                        showQuestionnaireResult();
                    }
                }
            });
        } else if (event.event == SayCallBackEvent.ASK_TRUE_FALSE_QUESTION) {
            Log.d(Application.TAG, "onEvent: yes-no question asked, wait respond");
            if (!mainActivity.isInterrupt && view != null) {
                mainActivity.subscribeASR(MainActivity.YES_NO_QUESTION, "yes_no_question_start event");
//                startCounter();
                //// TODO: 4/26/16
            } else {
                System.out.println("interrupt is true");
                mainActivity.isInterrupt = false;
            }
        } else if (event.event == SayCallBackEvent.TRUE_FALSE_FEEDBACK) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        loading.dismiss();
                        mainActivity.exitQuestFragment();
                        Log.d(Application.TAG, "QIP quit!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    public void showQuestionnaireResult() {
        endPage.setVisibility(View.VISIBLE);
        questionnairePage.setVisibility(View.INVISIBLE);
        int finalScore = questionnaireScore + q9Score;
        int rating = 0;
        if (finalScore == 0) {
            rating = 0;
        } else {
            if (q8choice == 5) {
                rating = 1;
            } else {
                if (finalScore > 6 && finalScore <= 16) {
                    rating = 2;
                } else if (finalScore > 17 && finalScore <= 26) {
                    rating = 3;
                } else if (finalScore > 27 && finalScore <= 36) {
                    rating = 4;
                } else if (finalScore > 37 && finalScore <= 46) {
                    rating = 5;
                } else {
                    Log.d(Application.TAG, "countScore error:" + finalScore);
                }
            }
        }
        String x = "";
        if (getLanguageID() == mainActivity.LANGUAGE_CHINESE || getLanguageID() == mainActivity.LANGUAGE_CANTONESE) {
            Log.d(Application.TAG, "showQuestionnaireResult: "+"您的分數是 " + finalScore);
            resultText.setText(Constant.ratingCN[rating] + " \n" + Constant.ratingCNDescription[rating]);
            x = "根據您提供的答案，您的投資風險取向屬於" + Constant.ratingCN[rating] + "\\pau=1000\\" + Constant.ratingCNDescription[rating];
        } else if (getLanguageID() == mainActivity.LANGUAGE_ENGLISH) {
            Log.d(Application.TAG, "showQuestionnaireResult: "+"Your score is " + finalScore);
            resultText.setText(Constant.ratingENG[rating] + " \n" + Constant.ratingENGDescription[rating]);
            x = "Based on the answers you have provided, your investment risk profile is " + Constant.ratingCN[rating] + "\\pau=1000\\" + Constant.ratingCNDescription[rating];
        }
        Log.d(Application.TAG, "History: " + scoreHistory);
        mainActivity.sayTextAnimated(signToSpeech(x), -1, false);
    }

    public String signToSpeech(String z) {
        if (getLanguageID() == mainActivity.LANGUAGE_CHINESE ||getLanguageID() == mainActivity.LANGUAGE_CANTONESE) {
            z = z.replace("/", "或");
            z=z.replace("媲","比");
            z=z.replace("了","鳥");
        } else if (getLanguageID() == mainActivity.LANGUAGE_ENGLISH) {
            z = z.replace("/", " or ");
        }
        z = z.replace("%", "percent").replace("A ", "\\pau=500\\ \\readmode=char\\a\\pau=200\\ \\readmode=sent\\").replace("B ", "\\pau=500\\ \\readmode=char\\b\\pau=200\\ \\readmode=sent\\").replace("C ", "\\pau=500\\ \\readmode=char\\c\\pau=200\\ \\readmode=sent\\").replace("D ", "\\pau=500\\ \\readmode=char\\d\\pau=200\\ \\readmode=sent\\").replace("E ", "\\pau=500\\ \\readmode=char\\e\\pau=200\\ \\readmode=sent\\").replace("F ", "\\pau=500\\ \\readmode=char\\f\\pau=200\\ \\readmode=sent\\").replace("G ", "\\pau=500\\ \\readmode=char\\g\\pau=200\\ \\readmode=sent\\");
        return z;
    }

    public void onEventMainThread(ProgressDialogEvent event) {//always true for questionnaire, dont change
        String result;

        result = " Loading...";


        loading = ProgressDialog.show(getActivity(), "", result, true);

    }

    public void onEvent(ChooseChoice event) {
        chooseAns(event.answer);
    }

    public void onEvent(TrueFalseAnswer event) {
        if (event.answer) {
            startQuestion.callOnClick();
        } else {
            quitQuest.callOnClick();
        }

    }

    public void chooseAns(int ans) {
//      stopCounter();
        mainActivity.isInterrupt = true;
        EventBus.getDefault().post(new ProgressDialogEvent(true));//always true for questionnaire, dont change
        Log.d(Application.TAG, "chooseAns: entered" + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        try {
            mainActivity.alTextToSpeech.stopAll();
        } catch (CallError callError) {
            callError.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        countScore(ans);
        if (getLanguageID() == mainActivity.LANGUAGE_CHINESE ||getLanguageID() == mainActivity.LANGUAGE_CANTONESE) {
            mainActivity.sayTextAnimated(signToSpeech(Application.questionCN.get(currentQID)[(ans + 1)]), SayCallBackEvent.GAME_ANSWER_FEEDBACK, false);
        } else if (getLanguageID() == mainActivity.LANGUAGE_ENGLISH) {
            mainActivity.sayTextAnimated(signToSpeech(Application.questionENG.get(currentQID)[(ans + 1)]), SayCallBackEvent.GAME_ANSWER_FEEDBACK, false);
        }

        currentQID++;
//        if (currentQID >= Application.questionCN.size()) {
//            currentQID = 0;
//        }
        lastQID = currentQID;
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

                    if (counter == 300) {
                        try {
                            String tempLanguage = mainActivity.alTextToSpeech.getLanguage();
                            switch (tempLanguage) {
                                case "CantoneseHK":
                                    tempLanguage = "時間過長，返回主頁";
                                    break;
                                case "Chinese":
                                    tempLanguage = "時間過長，返回主頁";
                                    break;
                                case "English":
                                    tempLanguage = "Back to menu";
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

    public void countScore(int ans) {
//        private boolean zeroScore = false;
//        private int q8choice=-1;
//        private int q9Score=0;
        if (currentQID == 0) {
            if (ans == 0) {
                zeroScore = true;
                scoreHistory+=" 1:T ";
            }else{
                scoreHistory+=" 1:F ";
            }
        } else if (currentQID == 1) {
            if (ans == 1) {
                zeroScore = true;
                scoreHistory+=" 2:T ";
            }else{
                scoreHistory+=" 2:F ";
            }
        } else {
            if (zeroScore) {
                questionnaireScore = 0;
            } else {
                if (currentQID < 7) {
                    Log.d(Application.TAG, "chooseAns: " + ans + ", score: " + questionnaireScore + " + " + Application.questionScore.get(currentQID)[ans]);
                    questionnaireScore += Application.questionScore.get(currentQID)[ans];
                    scoreHistory += " + (Q "+(currentQID+1)+" " +Constant.ansChoice[ans]+":"+ Application.questionScore.get(currentQID)[ans]+")" ;
                } else if (currentQID == 7) {
                    q8choice = ans;
                    questionnaireScore += Application.questionScore.get(currentQID)[ans];
                    scoreHistory += " + (Q 8 "+q8choice+"~" +Constant.ansChoice[ans]+":"+ Application.questionScore.get(currentQID)[ans]+")" ;
                } else if (currentQID > 7 && currentQID < 17) {
                    q9Score = max(Application.questionScore.get(currentQID)[ans], q9Score);
                    scoreHistory += " + (Q "+(currentQID+1)+ " try " + Application.questionScore.get(currentQID)[ans]+")";
                    if (q8choice == 2) {//c
                        q9Score = min(9, q9Score);
                        scoreHistory += " but limit 9";
                    } else if (q8choice == 3) {//D
                        q9Score = min(6, q9Score);
                        scoreHistory += " but limit 6";
                    } else if (q8choice == 4) {//E
                        q9Score = min(4, q9Score);
                        scoreHistory += " but limit 4";
//                  }else if(q8choice==5) {//F

                    } else {

                    }
                } else {
                    Log.d(Application.TAG, "countScore: currentQID error:" + currentQID);
                }

            }
        }

    }

    @Override
    public void onStart() {
        super.onStart();
//        getDialog().getWindow().setLayout(Application.width, Application.height);
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


    public int questionId = 0;

    public static QuestionnaireFragment newInstance() {
        QuestionnaireFragment f = new QuestionnaireFragment();
        return f;
    }
}
