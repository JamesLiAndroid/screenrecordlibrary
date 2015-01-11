package com.elife.videocpature;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by duanjin on 1/2/15.
 */
public class AudioRecordThread extends Thread{

    private final int SAMPLE_RATE = 44100;
    private final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private final int SIZE_PER_FRAME = 2048;

    private int bufferSize;
    private AudioRecord mRecord;
    private AtomicBoolean mIsRecording = new AtomicBoolean(false);
    private ArrayBlockingQueue<byte[]> mQueue;

    public AudioRecordThread(ArrayBlockingQueue<byte[]> queue) {
        mQueue = queue;
        android.os.Process.setThreadPriority(-19);
    }

    @Override
    public void run() {
        bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT);
        mRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG,
                AUDIO_FORMAT, bufferSize);
        if (mRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            return;
        }
        mRecord.startRecording();
        mIsRecording.set(true);

        while (mIsRecording.get()) {
            byte[] audioBuffer = new byte[SIZE_PER_FRAME];
            int result = mRecord.read(audioBuffer, 0, SIZE_PER_FRAME);
            if (result == AudioRecord.ERROR_BAD_VALUE || result == AudioRecord.ERROR_INVALID_OPERATION) {
            } else {
                try {
                    mQueue.put(audioBuffer);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        if (null != mRecord) {
            mRecord.setRecordPositionUpdateListener(null);
            mRecord.stop();
            mRecord.release();
        }
    }

    public void stopRecord() {
        mIsRecording.set(false);
    }
}
