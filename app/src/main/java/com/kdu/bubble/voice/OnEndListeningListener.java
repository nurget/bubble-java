package com.kdu.bubble.voice;

import java.util.EventListener;

public interface OnEndListeningListener extends EventListener {
    void onEndListening(ListeningEvent listeningEvent) throws InterruptedException;

}