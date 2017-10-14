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
    private int whereClick = -1;

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
        IntentSetting();
        load();
        initView();
        checkPlayer();
    }

    private void IntentSetting() {
        serviceIntent = new Intent(this, PlayerService.class);
    }

    private void load() {
        music = Music.getInstance();
        Intent intent = getIntent();
        if (intent != null) {
            current = intent.getIntExtra(Const.KEY_POSITION, 0);
            whereClick = intent.getIntExtra(Const.KEY_CLICK, 0);
        }
    }

    private void initView() {
        setContentView(R.layout.activity_player);

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

    private void checkPlayer() {
        Log.e("PlayerActivity","checkPlayer() 호출");
        Log.e("PlayerActivity","whereClick : " + whereClick);
        Log.e("PlayerActivity","current : " + current);
        // Play 중에 APP 을 실행하면 Button 과 View 를 변경해 줘야 한다.
        // current 설정 및 Button 설정
        // viewPager.setCurrentItem(current);

        if (whereClick == 0) {
            // LIST 에서 클릭했을 경우
            initViewPager();
        } else {
            // APP 을 클릭했을 경우
            // 그 페이지로 시작하고
            initViewPager(current);
            // 버튼 만 바꿔주면 된다.
            if (Player.getInstance().getStatus() == Const.STAT_PLAY) {
                // 시작 중이라면
                togglePlayButton(Const.STAT_PLAY);
            } else if (Player.getInstance().getStatus() == Const.STAT_PAUSE) {
                // 일시정지 중이라면
                seekBar.setProgress(Player.getInstance().getCurrentPosition());
                textCurrentTime.setText(milliToSec(Player.getInstance().getCurrentPosition()));
                togglePlayButton(Const.STAT_PAUSE);
            }
        }
    }


    private void initViewPager() {
        Log.e("PlayerActivity" , "initViewPager() 호출");
        PlayerPageAdapter playerPageAdapter = new PlayerPageAdapter(this, music.getItemList());
        viewPager.setAdapter(playerPageAdapter);
        // 첫 실행에서는 절대로 이뤄지지 않음;;
        // 0 page 일때는 실행이 안됨...
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
        });

        if(current > 0) {
            Player.getInstance().setStatus(Const.STAT_PLAY);
            viewPager.setCurrentItem(current);
        }else{
            initPlayerView();
            playerSet();
            playerStart();
        }
    }

    private void initViewPager(int number){
        Log.e("PlayerActivity" , "initViewPager(int number) 호출");
        PlayerPageAdapter playerPageAdapter = new PlayerPageAdapter(this, music.getItemList());
        viewPager.setAdapter(playerPageAdapter);
        if(number > 0 ){
            viewPager.setCurrentItem(number);
        }
        // 첫 실행에서는 절대로 이뤄지지 않음;;
        // 0 page 일때는 실행이 안됨...
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
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
        });
        initPlayerView();
    }

    /**
     * Player 에 관련된 View Setting
     */
    private void initPlayerView() {
        Music.Item item = music.getItemList().get(current);

        seekBar.setMax(item.duration);
        textDuration.setText(milliToSec(item.duration));
        textCurrentTime.setText("00:00");

        // Title, Artist 세팅
        textTitle.setText(item.title);
        textArtist.setText(item.artist);
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
        // viewPager 의 Listener 도 걸려있기 때문에 viewPager.setCurrentItem() 메소드를 사용할 수 없다.

        Player.getInstance().addListener(this);
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
        Log.e("PlayerActivity", "playerStart() 호출");
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
        viewPager.setCurrentItem(current, true);
    }

}

