package com.hooooong.newmusicplayer.util;

import android.content.Context;
import android.media.MediaPlayer;

import com.hooooong.newmusicplayer.data.Const;
import com.hooooong.newmusicplayer.data.model.Music;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by Android Hong on 2017-10-12.
 */

public class Player {

    private List<Listener> listenerList = new CopyOnWriteArrayList<>();
    private MediaPlayer mediaPlayer;
    private PlayerThread playerThread;
    private boolean loop = false;

    private int status = Const.STAT_STOP;
    private int current = -1;

    // Singleton
    private static Player player;
    private Context context;

    public static Player getInstance() {
        if (player == null) {
            player = new Player();
        }
        return player;
    }

    private Player() {
    }

    // 음원 세팅
    public void set(Context context, int current) {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        this.current = current;
        this.context = context;

        mediaPlayer = MediaPlayer.create(context, Music.getInstance().getItemList().get(current).musicUri);
        mediaPlayer.setLooping(loop);
        mediaPlayer.setOnCompletionListener(completionListener);

    }

    MediaPlayer.OnCompletionListener completionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            // loop check;
            if(current < Music.getInstance().getItemList().size() ){
                current += 1;

                set(context, current);
                start();

                for(Listener listener : listenerList){
                    listener.setMusic(current);
                }
            }
        }
    };

    // mediaPlayer 실행
    public void start() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
            if (playerThread != null) {
                playerThread.setStop();
                playerThread = null;
            }
            playerThread = new PlayerThread();
            playerThread.start();
            status = Const.STAT_PLAY;
        }
    }

    // mediaPlayer 일시정지
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            if (playerThread != null) {
                playerThread.setStop();
                playerThread = null;
            }
            status = Const.STAT_PAUSE;
        }
    }

    // mediaPlayer 멈춤
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            if (playerThread != null) {
                playerThread.setStop();
                playerThread = null;
            }
            status = Const.STAT_STOP;
        }
    }

    public void addListener(Listener listener) {
        listenerList.add(listener);
    }

    public void removeListener(Listener listener) {
        listenerList.remove(listener);
    }

    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getCurrent() {
        return current;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public class PlayerThread extends Thread {
        boolean check = true;
        @Override
        public void run() {
            while (check) {
                for (Listener listener : listenerList) {
                    listener.setProgress();
                }
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void setStop(){
            check = false;
        }
    }

    public interface Listener {
        void setProgress();
        void setMusic(int current);
    }
}
