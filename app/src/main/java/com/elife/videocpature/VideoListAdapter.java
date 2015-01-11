package com.elife.videocpature;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by duanjin on 1/2/15.
 */
public class VideoListAdapter extends BaseAdapter {

    private ArrayList<String> mFileNames;
    private String dirPath;
    private Context mContext;
    Calendar c = Calendar.getInstance();
    private int mSelectedPos = -1;


    public VideoListAdapter(Context context, ArrayList<String> files, String dir) {
        mContext = context;
        dirPath = dir;
        setData(files);

    }

    public void setData(ArrayList<String> files) {
        if (null == mFileNames) {
            mFileNames = new ArrayList<>();
        }
        mFileNames.clear();
        mFileNames.addAll(files);

    }
    @Override
    public int getCount() {
        return mFileNames.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileNames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (null == convertView) {
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.video_item, null, false);
            Holder holder = new Holder();
            holder.thumbImg = (AsyncImageView) convertView.findViewById(R.id.thumb);
            holder.resolution = (TextView) convertView.findViewById(R.id.resolution);
            holder.size = (TextView) convertView.findViewById(R.id.size);
            holder.create_time = (TextView) convertView.findViewById(R.id.time);

            convertView.setTag(holder);
        }

        final Holder holder = (Holder) convertView.getTag();

        File file = new File(dirPath + mFileNames.get(position));
        if (file.exists()) {
            c.setTimeInMillis(file.lastModified());
            String dateStr = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1)
                    + "-" + c.get(Calendar.DAY_OF_MONTH);
            holder.create_time.setText(dateStr);
            holder.size.setText(String.format("%.2f", file.length() / 1000000.0) + "M");

            final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(dirPath + mFileNames.get(position));

            holder.thumbImg.setImagePath(dirPath + mFileNames.get(position));
            String videoHeight = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String videoWidth = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            holder.resolution.setText(videoWidth + " x " + videoHeight);
        }
        holder.thumbImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                Uri videoUri = Uri.parse(dirPath + mFileNames.get(position));
                viewIntent.setDataAndType(videoUri, "video/*");
                mContext.startActivity(viewIntent);
            }
        });

       return convertView;
    }

    public void setSelectedPos(int pos, View view) {
        mSelectedPos = pos;
        showViewAsSelected(view);
    }

    private void showViewAsSelected(View view) {
//        if (view.getTag() != null) {
//            if (view.getTag() instanceof Holder) {
//                Holder holder =(Holder) view.getTag();
//                holder.coverView.setVisibility(View.VISIBLE);
//            }
//        }
    }

    public String getSelectedFileName() {
        if (null != mFileNames && mSelectedPos < mFileNames.size()) {
            return dirPath + mFileNames.get(mSelectedPos);
        }

        return "";
    }

    private static class Holder {
        public AsyncImageView thumbImg;
        public TextView resolution;
        public TextView size;
        public TextView create_time;
        public View coverView;
    }
}
