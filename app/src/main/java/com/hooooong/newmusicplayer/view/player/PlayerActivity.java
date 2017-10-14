package com.hooooong.newmusicplayer.view.player;

import android.Manifest;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hooooong.newmusicplayer.R;
import com.hooooong.newmusicplayer.data.Const;
import com.hooooong.newmusicplayer.data.model.Music;
import com.hooooong.newmusicplayer.util.Player;
import com.hooooong.newmusicplayer.util.PlayerService;
import com.hooooong.newmusicplayer.view.BaseActivity;
import com.hooooong.newmusicplayer.view.player.adapter.PlayerPageAdapter;

public class PlayerActivity extends BaseActivity implements View.OnClickListener, Player.Listener {

    private Music music;
    private int current = -1;

    private Toolbar toolbar;
    private ViewPager viewPager;
    private ImageView imgAlbum;
    private RelativeLayout controller;
    private SeekBar seekBar;
    private TextView textCurrentTime;
    private TextView textDuration;
    private TextView textTitle;
    private TextView textArtist;
    private ImageView imgPlay;
    private ImageView imgFf;
    private ImageView imgRew;
    private ImageView imgNext;
    private ImageView imgPrev;

    private Intent serviceIntent;

    public PlayerActivity() {
        super(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE});
    }

    @Override
    public void init() {
        setContentView(R.layout.activity_player);

        intentSetting();
        load();
        initView();
        initViewPager();
        initPlayer();
    }

    // Service Intent 설정
    private void intentSetting() {
        serviceIntent = new Intent(this, PlayerService.class);
    }

    private void load() {
        music = Music.getInstance();
        Intent intent = getIntent();
        if (intent != null) {
            current = intent.getIntExtra(Const.KEY_POSITION, 0);
        }
    }

    private void initView() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textTitle = toolbar.findViewById(R.id.textTitle);
        textTitle.setSelected(true);
        textArtist = toolbar.findViewById(R.id.textArtist);

        viewPager = (ViewPager) findViewById(R.id.viewPager);

        controller = (RelativeLayout) findViewById(R.id.controller);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        textCurrentTime = (TextView) findViewById(R.id.textCurrentTime);
        textDuration = (TextView) findViewById(R.id.textDuration);
        imgPlay = (ImageView) findViewById(R.id.imgPlay);
        imgFf = (ImageView) findViewById(R.id.imgFf);
        imgRew = (ImageView) findViewById(R.id.imgRew);
        imgPrev = (ImageView) findViewById(R.id.imgPrev);
        imgNext = (ImageView) findViewById(R.id.imgNext);

        imgAlbum = (ImageView) findViewById(R.id.imgAlbum);

        imgPlay.setOnClickListener(this);
        imgFf.setOnClickListener(this);
        imgRew.setOnClickListener(this);
        imgPrev.setOnClickListener(this);
        imgNext.setOnClickListener(this);
    }

    private void initViewPager() {
        PlayerPageAdapter playerPageAdapter = new PlayerPageAdapter(this, music.getItemList());
        viewPager.setAdapter(playerPageAdapter);
    }

    private void initPlayer(){
        viewPager.addOnPageChangeListener(onPageChangeListener);
        if(current > 0){
            // 0번째 current 를 실행한 경우
            Player.getInstance().setStatus(Const.STAT_PLAY);
            viewPager.setCurrentItem(current);
        }else {
            // 0번째 current 를 실행한 경우
            initPlayerView();
            playerSet();
            playerStart();
        }
    }

    private void playerCheck() {
        Log.e("PlayerActivity", "playerCheck() 호출");
        Log.e("PlayerActivity", "current : " + current);
        int playerCurrent = Player.getInstance().getCurrent();
        Log.e("PlayerActivity", "playerCurrent :" + playerCurrent);

        // onResume() 에서만 호출되는 메소드
        // initPlayer 와 중복이 되지 않고
        // 목록에서 눌렀을 때 중복이 되지 않도록
        // 현재 Service 에서 실행하고 있는 current 와 현재 current 가 다를 경우
        // ViewPager 이동한다.

        /*
        if(playerCurrent != -1){
            if(current != playerCurrent){
                Log.e("변환", "변환");
                viewPager.clearOnPageChangeListeners();
                initPlayerView();
                viewPager.setCurrentItem(playerCurrent);
                viewPager.addOnPageChangeListener(onPageChangeListener);
            }
        }*/
    }

    ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            current = position;
            initPlayerView();
            playerSet();
            if (Player.getInstance().getStatus() == Const.STAT_PLAY) {
                playerStart();
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    /**
     * Player 에 관련된 View Setting
     */
    private void initPlayerView() {
        Music.Item item = music.getItemList().get(current);

        seekBar.setMax(item.duration);
        textDuration.setText(milliToSec(item.duration));

        // seekBar, 현재 초 초기화
        textCurrentTime.setText("00:00");
        seekBar.setProgress(0);

        // Title, Artist 세팅
        textTitle.setText(item.title);
        textArtist.setText(item.artist);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgPlay:
                if (Player.getInstance().getStatus() == Const.STAT_PLAY) {
                    playerPause();
                } else {
                    playerStart();
                }
                break;
            case R.id.imgFf:
                break;
            case R.id.imgRew:
                break;
            case R.id.imgPrev:
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
                break;
            case R.id.imgNext:
                viewPager.setCurrentItem(viewPager.getCurrentItem() + 1, true);
                break;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_back, menu);
        return true;
    }

    /**
     * Appbar 메뉴 선택 이벤트
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_back) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("PlayerActivity", "onResume() 호출");
        Player.getInstance().addListener(this);
        playerCheck();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Player.getInstance().removeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void playerSet() {
        serviceIntent.setAction(Const.ACTION_SET);
        serviceIntent.putExtra(Const.KEY_POSITION, current);
        startService(serviceIntent);
    }

    private void playerStart() {
        serviceIntent.setAction(Const.ACTION_START);
        startService(serviceIntent);
        togglePlayButton(Const.STAT_PLAY);
    }

    private void playerPause() {
        serviceIntent.setAction(Const.ACTION_PAUSE);
        startService(serviceIntent);
        togglePlayButton(Const.STAT_PAUSE);
    }

    private void playerStop() {
        serviceIntent.setAction(Const.ACTION_STOP);
        startService(serviceIntent);
    }

    private void togglePlayButton(int status) {
        if (status == Const.STAT_PLAY) {
            imgPlay.setBackgroundResource(R.drawable.ic_stop);
        } else if (status == Const.STAT_PAUSE) {
            imgPlay.setBackgroundResource(R.drawable.ic_play_arrow);
        }
    }

    @Override
    public void setProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // 화면 세팅
                seekBar.setProgress(Player.getInstance().getCurrentPosition());
                textCurrentTime.setText(milliToSec(Player.getInstance().getCurrentPosition()));
            }
        });
    }

    @Override
    public void setMusic(int current) {
        this.current = current;

        viewPager.clearOnPageChangeListeners();
        initPlayerView();
        viewPager.setCurrentItem(current);
        viewPager.addOnPageChangeListener(onPageChangeListener);
    }

    /**
     * 1110101 -> 04:00 으로 변환하는 메소드
     *
     * @param milli
     * @return
     */
    private String milliToSec(int milli) {
        int sec = milli / 1000;
        int min = sec / 60;
        sec = sec % 60;

        return java.lang.String.format("%02d", min) + ":" + java.lang.String.format("%02d", sec);
    }
}

