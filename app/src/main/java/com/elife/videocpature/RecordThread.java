package com.elife.videocpature;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.projection.MediaProjection;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by duanjin on 12/28/14.
 */
public class RecordThread extends Thread{

    //video needed variables
    private int width;
    private int height;
    private int density;
    private MediaProjection mProjection;
    private AtomicBoolean mShouldStop = new AtomicBoolean(false);
    private MediaCodec mEncoder;
    private MediaMuxer mMuxer;
    private int mVideoTrackIndex;
    private Surface mInputSurface;
    private final static int KEY_FRAME_INTERVAL = 10;
    private final static int FRAME_RATE = 30;
    private final static int BIT_RATE = 6000000;
    private String mDesDir;
    private VirtualDisplay mVirtualDisplay;
    private MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
    private boolean mIsMutexStarted = false;

    //audio needed variables
    private boolean mNeedAudio = false;
    private boolean mIsLandScapeMode = false;
    private MediaCodec mAudioEncoder;
    public ArrayBlockingQueue<byte[]> audioBuffer = new ArrayBlockingQueue<byte[]>(50);
    private MediaCodec.BufferInfo mAudioBufferInfo = new MediaCodec.BufferInfo();
    private int mMuxerTrackNumber = 0;
    private int mAudioTrackIndex;
    private AudioRecordThread mAudioThread;
    private long mAudioStartTime = 0;
    private long mEncodeStartTime;


    public RecordThread(DisplayMetrics metrics, MediaProjection projection, String dir) {
        init(metrics.widthPixels, metrics.heightPixels, metrics, projection, dir, false, false);

    }

    public RecordThread(int iWidth, int iHeight, DisplayMetrics metrics, MediaProjection projection,
                        String dir, boolean needAudio, boolean landScapeModeOn) {
        init(iWidth, iHeight, metrics, projection, dir, needAudio, landScapeModeOn);
        android.os.Process.setThreadPriority(-19);

    }

    private void init(int iWidth, int iHeight, DisplayMetrics metrics, MediaProjection projection,
                      String dir, boolean needAudio, boolean landScapeModeOn) {
        mIsLandScapeMode = landScapeModeOn;
        if (mIsLandScapeMode) {
            width = iHeight;
            height = iWidth;
            if (width ==0 || height == 0) {
                width = metrics.heightPixels;
                height = metrics.widthPixels;
            }

        } else {
            width = iWidth;
            height = iHeight;
            if (width ==0 || height == 0) {
                width = metrics.widthPixels;
                height = metrics.heightPixels;
            }
        }
        density = metrics.densityDpi;
        mProjection = projection;
        mDesDir = dir;
        mNeedAudio = needAudio;

    }

    private void setupEncoder() {
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
                width, height);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, KEY_FRAME_INTERVAL);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);

        try {
            mEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            mEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mInputSurface = mEncoder.createInputSurface();
            mEncoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (mNeedAudio) {
            MediaFormat audioFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 1);
            audioFormat.setInteger(MediaFormat.KEY_BIT_RATE, 128000);
            audioFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384);
            audioFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);

            try {
                mAudioEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
                mAudioEncoder.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                mAudioEncoder.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    private void setupMediaMuex() {
        Calendar c = Calendar.getInstance();
        Date date = c.getTime();
        //record-2014-12-22-14-23
        String fileName = String.format("record-%s-%s-%s-%s-%s-%s", c.get(Calendar.YEAR),
                c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
        String pathName = Environment.getExternalStorageDirectory().getPath() + "/" +
                mDesDir;
        File dir = new File(pathName);
        boolean result = true;
        if (!dir.exists()) {
           result = dir.mkdir();
        }
        if (result == true) {
            fileName = dir.getPath() + "/" + fileName + ".mp4";
        }
        else {
            fileName = Environment.getExternalStorageDirectory().getPath() + "/" + fileName + ".mp4";

        }
        try {
            mMuxer = new MediaMuxer(fileName, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void run() {
        setupEncoder();
        setupMediaMuex();
        mVirtualDisplay = mProjection.createVirtualDisplay("screenRecorder", width, height, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, mInputSurface, null, null);
        if (mNeedAudio) {
            mAudioThread = new AudioRecordThread(audioBuffer);
            mAudioThread.start();
            encodeVideoAndAudio();
        } else {
            encodeVideo();
        }
    }

    private void encodeVideoAndAudio() {
        mEncodeStartTime = System.nanoTime();
        while (!mShouldStop.get()) {
            long frameTime = System.nanoTime();
            int outBufferIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, 1000L);
            int audioOutBufferIndex = mAudioEncoder.dequeueOutputBuffer(mAudioBufferInfo, 1000L);

            //check Video index and add Video track
            if (outBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
            } else if(outBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (!mIsMutexStarted) {
                    mVideoTrackIndex = mMuxer.addTrack(mEncoder.getOutputFormat());
                    mMuxerTrackNumber ++;
                    if (mMuxerTrackNumber == 2)
                    {
                        mMuxer.start();
                        mIsMutexStarted = true;
                    }
                }
            } else if (outBufferIndex > 0 && mIsMutexStarted) {
                ByteBuffer outputBuffer = mEncoder.getOutputBuffer(outBufferIndex);
                if (outputBuffer != null) {
                    if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        mBufferInfo.size = 0;
                    }
                    if (mBufferInfo.size != 0) {
                        outputBuffer.position(mBufferInfo.offset);
                        outputBuffer.limit(mBufferInfo.offset + mBufferInfo.size);
                        mBufferInfo.presentationTimeUs = (frameTime - mEncodeStartTime) / 1000;
                        mMuxer.writeSampleData(mVideoTrackIndex, outputBuffer, mBufferInfo);
                    }
                    mEncoder.releaseOutputBuffer(outBufferIndex, false);
                }
            }

            //check audio index
            if (audioOutBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {

            } else if (audioOutBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (!mIsMutexStarted) {
                    mAudioTrackIndex = mMuxer.addTrack(mAudioEncoder.getOutputFormat());
                    mMuxerTrackNumber++;
                    if (mMuxerTrackNumber == 2) {
                        mMuxer.start();
                        mIsMutexStarted = true;
                    }
                }
            } else if (audioOutBufferIndex > 0 && mIsMutexStarted) {
                ByteBuffer outputBuffer = mAudioEncoder.getOutputBuffer(audioOutBufferIndex);
                if (outputBuffer != null) {
                    if ((mAudioBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        mAudioBufferInfo.size = 0;
                    }
                    if (mAudioBufferInfo.size != 0) {
                        outputBuffer.position(mAudioBufferInfo.offset);
                        outputBuffer.limit(mAudioBufferInfo.offset + mAudioBufferInfo.size);
                        mAudioBufferInfo.presentationTimeUs = (frameTime - mEncodeStartTime) / 1000;
                        mMuxer.writeSampleData(mAudioTrackIndex, outputBuffer, mAudioBufferInfo);
                    }
                    mAudioEncoder.releaseOutputBuffer(audioOutBufferIndex, false);
                }
            }

            //put audio buffer to audioEncode input buffer
            byte[] audioData = audioBuffer.peek();
            if (audioData != null) {
                audioBuffer.remove();
                int inputBufferIdx = mAudioEncoder.dequeueInputBuffer(-1);
                if (inputBufferIdx >= 0) {
                    ByteBuffer inputBuffer =  mAudioEncoder.getInputBuffer(inputBufferIdx);
                    inputBuffer.clear();
                    inputBuffer.put(audioData);
                    mAudioEncoder.queueInputBuffer(inputBufferIdx,0, audioData.length,
                            (System.nanoTime() - mAudioStartTime) /1000, 0 );
                }
            }
        }
        mMuxer.stop();
        mMuxer.release();
        mEncoder.stop();
        mEncoder.release();
        mAudioEncoder.stop();
        mAudioEncoder.release();
        if (null != mProjection) {
            mProjection.stop();
        }
        if (null != mVirtualDisplay) {
            mVirtualDisplay.release();
        }
    }

    private void encodeVideo() {
        while (!mShouldStop.get()) {
            int outBufferIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, 1000L);
            if (outBufferIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
            } else if(outBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (!mIsMutexStarted) {
                    mVideoTrackIndex = mMuxer.addTrack(mEncoder.getOutputFormat());
                    mMuxer.start();
                    mIsMutexStarted = true;
                }
            } else if (outBufferIndex > 0) {
                ByteBuffer outputBuffer = mEncoder.getOutputBuffer(outBufferIndex);
                if (outputBuffer != null) {
                    if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        mBufferInfo.size = 0;
                    }
                    if (mBufferInfo.size != 0) {
                        outputBuffer.position(mBufferInfo.offset);
                        outputBuffer.limit(mBufferInfo.offset + mBufferInfo.size);
                        mMuxer.writeSampleData(mVideoTrackIndex, outputBuffer, mBufferInfo);
                    }
                    mEncoder.releaseOutputBuffer(outBufferIndex, false);
                }
            }
        }
        mMuxer.stop();
        mMuxer.release();
        mEncoder.stop();
        mEncoder.release();
        if (mProjection != null) {
            mProjection.stop();
        }
        if (mVirtualDisplay != null) {
            mVirtualDisplay.release();
        }
    }

    public void quit() {
        mShouldStop.set(true);
        if (mNeedAudio && null != mAudioThread) {
            mAudioThread.stopRecord();
        }
    }
}
