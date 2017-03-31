package louis.surfacewaveview.ui.managers;

import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.ConcurrentLinkedQueue;

import louis.surfacewaveview.ui.SurfaceWaveView;


/**
 * Created by louis on 2017/3/30.
 */


public class WaveTransitionManager {

    private SurfaceWaveView mSurfaceWaveView;
    private boolean isListening = false;

    private int minSpeedOne = 7;
    private int maxSpeedOne = 14;

    private int minSpeedTwo = 5;
    private int maxSpeedTwo = 10;

    private float minA = 0;
    private float maxA = 40;

    private int minWaterDepth = 50;
    private int maxWaterDepth = 100;

    private ConcurrentLinkedQueue<Float> mTransitionAQueue;
    private Float mCurrentA = (float)(20); //当前波纹幅度值
    private Float mTargetA = (float)(20); //当前波纹设置目标值

    private ConcurrentLinkedQueue<Integer> mTransitionSpeedOneQueue;
    private Integer mCurrentSpeedOne = 10; //当前快波纹速度
    private Integer mTargetSpeedOne = 10;   //当前快波纹设置目标值

    private ConcurrentLinkedQueue<Integer> mTransitionSpeedTwoQueue;
    private Integer mCurrentSpeedTwo = 7; //当前慢波纹速度
    private Integer mTargetSpeedTwo = 7;   //当前慢波纹设置目标值

    private ConcurrentLinkedQueue<Integer> mTransitionWaterDepth;
    private Integer mCurrentWaterDepth = 100; //当前波纹水深
    private Integer mTargetWaterDepth = 100; //当前波纹水深目标值

    private ListenerThread mListenerThread;

    public WaveTransitionManager(SurfaceWaveView surfaceWaveView) {
        mSurfaceWaveView = surfaceWaveView;
        mTransitionAQueue = new ConcurrentLinkedQueue<>();
        mTransitionSpeedOneQueue = new ConcurrentLinkedQueue<>();
        mTransitionSpeedTwoQueue = new ConcurrentLinkedQueue<>();
        mTransitionWaterDepth = new ConcurrentLinkedQueue<>();
        isListening = true;
        mListenerThread = new ListenerThread();
    }

    public void startThread () {
        isListening = true;
        mListenerThread.start();
    }

    public void stopThread() {
        isListening = false;
    }

    private class ListenerThread extends Thread {

        @Override
        public void run() {
            while(isListening) {
                if (!mSurfaceWaveView.getIsAttributeChange()) {
                    if (!mTransitionAQueue.isEmpty()) {
                        mCurrentA = mTransitionAQueue.poll();
                        mSurfaceWaveView.setStretchFactorA(mCurrentA);
                    }

                    if (!mTransitionSpeedOneQueue.isEmpty()) {
                        mCurrentSpeedOne = mTransitionSpeedOneQueue.poll();
                        mSurfaceWaveView.setTranslateXSpeedOne(mCurrentSpeedOne);
                    }

                    if (!mTransitionSpeedTwoQueue.isEmpty()) {
                        mCurrentSpeedTwo = mTransitionSpeedTwoQueue.poll();
                        mSurfaceWaveView.setTranslateXSpeedTwo(mCurrentSpeedTwo);
                    }

                    if (!mTransitionWaterDepth.isEmpty()) {
                        mCurrentWaterDepth = mTransitionWaterDepth.poll();
                        mSurfaceWaveView.setWaterDepth(mCurrentWaterDepth);
                    }
                }

                SystemClock.sleep(100);
            }
        }
    }


    public void powerOff() {
        transitionA(minA);
        transitionSpeedOne(minSpeedOne);
        transitionSpeedTwo(minSpeedTwo);
    }

    public void changeLevel(int level) {
        transitionA((float) (maxA * level / 100.0));
        transitionSpeedOne((int) (minSpeedOne + (maxSpeedOne - minSpeedOne) * level / 100.0));
        transitionSpeedTwo((int) (minSpeedTwo + (maxSpeedTwo - minSpeedTwo) * level / 100.0));
    }

    private void transitionA(Float targetA) {
        if (mTargetA.equals(targetA)) {
            Log.d("WaveTransitionManager", "mTargetA equal.");
            return;
        }
        mTargetA = targetA;
        mTransitionAQueue.clear();
        double tempA = mCurrentA;
        double err = (tempA - targetA) / 20.0;
        for (int i = 0; i < 20; i++) {
            tempA -= err;
            mTransitionAQueue.offer((float) tempA);
        }
        mTransitionAQueue.offer(targetA);
    }

    private void transitionSpeedOne(Integer targetSpeedOne) {
        if (mTargetSpeedOne.equals(targetSpeedOne)) {
            Log.d("WaveTransitionManager", "mTargetSpeedOne equal.");
            return;
        }
        mTargetSpeedOne = targetSpeedOne;
        mTransitionSpeedOneQueue.clear();
        double tempSpeedOne = mCurrentSpeedOne;
        double err = (tempSpeedOne - targetSpeedOne) / 20.0;
        for (int i = 0; i < 20; i++) {
            tempSpeedOne -= err;
            mTransitionSpeedOneQueue.offer((int) Math.floor(tempSpeedOne));
        }
        mTransitionSpeedOneQueue.offer(targetSpeedOne);
    }

    private void transitionSpeedTwo(Integer targetSpeedTwo) {
        if (mTargetSpeedTwo.equals(targetSpeedTwo)) {
            Log.d("WaveTransitionManager", "mTargetSpeedTwo equal.");
            return;
        }
        mTargetSpeedTwo = targetSpeedTwo;
        mTransitionSpeedTwoQueue.clear();
        double tempSpeedTwo = mCurrentSpeedTwo;
        double err = (tempSpeedTwo - targetSpeedTwo) / 20.0;
        for (int i = 0; i < 20; i++) {
            tempSpeedTwo -= err;
            mTransitionSpeedTwoQueue.offer((int) Math.floor(tempSpeedTwo));
        }
        mTransitionSpeedTwoQueue.offer(targetSpeedTwo);
    }

    // TODO 优化渐变过程，从停止到启动或从启动到停止
    private void transitionWaterDepth (Integer targetWaterDepth) {
        if (mTargetWaterDepth.equals(targetWaterDepth)) {
            Log.d("WaveTransitionManager", "mTargetWaterDepth equal.");

            return;
        }
        mTargetWaterDepth = targetWaterDepth;
        mTransitionWaterDepth.clear();
        double tempWaterDepth = mCurrentWaterDepth;
        double err = (tempWaterDepth - targetWaterDepth) / 20;
        for (int i = 0; i < 20; i ++) {
            tempWaterDepth -= err;
            mTransitionWaterDepth.offer((int) Math.floor(tempWaterDepth));
        }
        mTransitionWaterDepth.offer(targetWaterDepth);
    }
}
