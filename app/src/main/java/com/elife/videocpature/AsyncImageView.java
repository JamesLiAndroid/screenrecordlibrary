package com.elife.videocpature;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by duanjin on 1/5/15.
 */
public class AsyncImageView extends ImageView{
    private LoadImgTask mLoadTask;
    public AsyncImageView(Context context) {
        super(context);
    }

    public AsyncImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AsyncImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AsyncImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setImagePath(String filePath) {
        if (null != mLoadTask) {
            mLoadTask.cancel(true);
        }
        mLoadTask = new LoadImgTask();
        mLoadTask.execute(filePath);

    }

    private  class LoadImgTask extends AsyncTask<String, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(String[] params) {
            final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(params[0]);
            Bitmap bmp = retriever.getFrameAtTime();
            return bmp;
        }
        @Override
        protected void onPostExecute(Bitmap o) {
            if (o != null) {
                if (!isCancelled())
                    AsyncImageView.this.setImageBitmap(o);
            }
        }
    }
}
