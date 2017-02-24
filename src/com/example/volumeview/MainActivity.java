package com.example.volumeview;

import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {
    private Button mMoveViweStartButton;

    private Button mMoveViewStopButton;

    private VolumeViewMoveWave mVolumeViewMoveWave;

    private Button mDoubleMoveViweStartButton;

    private Button mDoubleMoveViewStopButton;

    private VolumeViewDoubleMoveWave mVolumeViewDoubleMoveWave;

    private Button mDoubleMoveViweStartOptButton;

    private Button mDoubleMoveViewStopOptButton;

    private VolumeViewDoubleMoveWaveOpt mVolumeViewDoubleMoveWaveOpt;

    private VolumeView mVolumeView;

    private Button mVolumeViewRandomStartButton;

    private Button mVolumeViewRandomStopButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        mMoveViweStartButton = (Button) findViewById(R.id.move_wave_start);
        mMoveViweStartButton.setOnClickListener(this);
        mMoveViewStopButton = (Button) findViewById(R.id.move_wave_stop);
        mMoveViewStopButton.setOnClickListener(this);
        mVolumeViewMoveWave = (VolumeViewMoveWave) findViewById(R.id.move_wave);

        mDoubleMoveViweStartButton = (Button) findViewById(R.id.move_wave_double_start);
        mDoubleMoveViweStartButton.setOnClickListener(this);
        mDoubleMoveViewStopButton = (Button) findViewById(R.id.move_wave_double_stop);
        mDoubleMoveViewStopButton.setOnClickListener(this);
        mVolumeViewDoubleMoveWave = (VolumeViewDoubleMoveWave) findViewById(R.id.move_wave_double);

        mDoubleMoveViweStartOptButton = (Button) findViewById(R.id.move_wave_opt_double_start);
        mDoubleMoveViweStartOptButton.setOnClickListener(this);
        mDoubleMoveViewStopOptButton = (Button) findViewById(R.id.move_wave_opt_double_stop);
        mDoubleMoveViewStopOptButton.setOnClickListener(this);
        mVolumeViewDoubleMoveWaveOpt = (VolumeViewDoubleMoveWaveOpt) findViewById(R.id.move_wave_opt_double);

        mVolumeView = (VolumeView) findViewById(R.id.volume_view);
        mVolumeViewRandomStartButton = (Button) findViewById(R.id.volume_view_random_start);
        mVolumeViewRandomStartButton.setOnClickListener(this);
        mVolumeViewRandomStopButton = (Button) findViewById(R.id.volume_view_random_stop);
        mVolumeViewRandomStopButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.move_wave_start:
                mVolumeViewMoveWave.start();
                break;
            case R.id.move_wave_stop:
                mVolumeViewMoveWave.stop();
                break;

            case R.id.move_wave_double_start:
                mVolumeViewDoubleMoveWave.start();
                break;
            case R.id.move_wave_double_stop:
                mVolumeViewDoubleMoveWave.stop();
                break;

            case R.id.move_wave_opt_double_start:
                mVolumeViewDoubleMoveWaveOpt.start();
                break;
            case R.id.move_wave_opt_double_stop:
                mVolumeViewDoubleMoveWaveOpt.stop();
                break;

            case R.id.volume_view_random_start:
                startRandomVolumeView();
                break;
            case R.id.volume_view_random_stop:
                stopRandmVolumeView();
                break;
            default:
                break;
        }
    }

    private void startRandomVolumeView() {
        mVolumeView.start();
        
        //随机设置音量大小.
        if (mRandomThreand != null) {
            mRandomThreand.stopRunning();
            mRandomThreand = null;
        }
        mRandomThreand = new RandomThreand();
        mRandomThreand.start();
    }

    private void stopRandmVolumeView() {
        if (mRandomThreand != null) {
            mRandomThreand.stopRunning();
            mRandomThreand = null;
        }
        mVolumeView.stop();
    }

    private RandomThreand mRandomThreand;

    private class RandomThreand extends Thread {
        private static final int MOVE_STOP = 1;

        private static final int MOVE_START = 0;

        private int state;

        @Override
        public void run() {
            state = MOVE_START;

            while (true) {
                if (state == MOVE_STOP) {
                    break;
                }
                try {
                    sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mVolumeView.setVolume(getRandom(0, 100));
            }
        }

        public void stopRunning() {
            state = MOVE_STOP;
        }
    }

    private int getRandom(int min, int max) {
        Random random = new Random();
        int r = random.nextInt(max) % (max - min + 1) + min;
        return r;
    }

}
