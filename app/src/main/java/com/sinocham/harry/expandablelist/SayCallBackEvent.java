package com.sinocham.harry.expandablelist;

/**
 * Created by harry on 4/13/16.
 */
public class SayCallBackEvent {

    public static final int CHOSEN_LANGUAGE = 0;
    public static final int GAME_TIME_IS_UP = 1;
    public static final int QUESTION_START = 2;
    public static final int GAME_ANSWER_FEEDBACK = 3;
    public static final int AFTER_SERVICE_SPEECH = 4;
    public static final int AFTER_CHOOSEN_LANGUAGE = 5;
    public static final int AFTER_COMMON_LANGUAGE = 6;

    public int event;

    public SayCallBackEvent(int event) {
        this.event = event;
    }
}
