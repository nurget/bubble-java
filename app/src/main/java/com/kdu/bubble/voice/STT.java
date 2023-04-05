package com.kdu.bubble.voice;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.kdu.bubble.MainActivity;
import com.kdu.bubble.R;

import java.util.ArrayList;

import static com.kdu.bubble.MainActivity.getAppContext;
import static com.kdu.bubble.MainActivity.vibrator;

public class STT {
    Intent intent;
    SpeechRecognizer mRecognizer;
    final Context context;
    static ArrayList<String> result = new ArrayList<String>();

    public STT(Activity activity) {
        this.context = MainActivity.getAppContext();
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        mRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                MediaPlayer mediaPlayer = MediaPlayer.create(getAppContext(), R.raw.pling);
                mediaPlayer.start();
                vibrator.vibrate(100);
            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                String key = "";
                key = SpeechRecognizer.RESULTS_RECOGNITION;
                result = bundle.getStringArrayList(key);
                String[] rs = new String[result.size()];
                result.toArray(rs);
                try {
                    Command.onEndListeningListener.onEndListening(new ListeningEvent(this));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
    }

    public void startListening() {
        mRecognizer.startListening(intent);
    }


    public void shutdownSTT() {
        if (mRecognizer != null) {
            mRecognizer.destroy();
            mRecognizer.cancel();
            mRecognizer = null;
        }
    }
}
