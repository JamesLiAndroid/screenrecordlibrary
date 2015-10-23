package com.elife.videocpature;

import android.animation.LayoutTransition;
import android.content.ActivityNotFoundException;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.eversince.screenrecord.R;

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
    private ArrayList<Integer> mSelectedPos = new ArrayList<>();
    public boolean isInSelectionMode = false;


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
            holder.playView = convertView.findViewById(R.id.play_icon);
            holder.selectView = convertView.findViewById(R.id.select_icon);
            holder.thumbContainer = (RelativeLayout)convertView.findViewById(R.id.thumb_container);
            LayoutTransition layoutTransition = new LayoutTransition();
            layoutTransition.setStartDelay(LayoutTransition.CHANGE_DISAPPEARING, 0);
            holder.thumbContainer.setLayoutTransition(new LayoutTransition());


            convertView.setTag(holder);
        }

        final Holder holder = (Holder) convertView.getTag();
        if (mSelectedPos.contains(position)) {

            holder.selectView.setVisibility(View.VISIBLE);
            holder.playView.setVisibility(View.GONE);
        } else {
            holder.selectView.setVisibility(View.GONE);
            holder.playView.setVisibility(View.VISIBLE);
        }
        File file = new File(dirPath + mFileNames.get(position));
        if (file.exists()) {
            c.setTimeInMillis(file.lastModified());
            String dateStr = String.format("%s-%d-%d",c.get(Calendar.YEAR),
                    (c.get(Calendar.MONTH) + 1),c.get(Calendar.DAY_OF_MONTH));
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
                try {
                    Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                    Uri videoUri = Uri.parse(dirPath + mFileNames.get(position));
                    viewIntent.setDataAndType(videoUri, "video/*");
                    mContext.startActivity(viewIntent);
                } catch (ActivityNotFoundException ae) {
                    Toast.makeText(mContext, "你没有安装任务视频播放app", Toast.LENGTH_SHORT).show();
                }
            }
        });

       return convertView;
    }

    public void addSelectedPos(int pos) {
        if (null != mSelectedPos) {
            mSelectedPos.add(pos);
        }else {
            mSelectedPos = new ArrayList<>();
            mSelectedPos.add(pos);
        }

    }

    public void removeSelectedPos(int pos) {
        if (null != mSelectedPos) {
            int index = mSelectedPos.indexOf(pos);
            if (index != -1) {
                mSelectedPos.remove(index);
            }
        }
    }

    public void deSelectAll() {
        if (null != mSelectedPos) {
            mSelectedPos.clear();
        }
    }

    public int getSelectedCount() {
        if (null != mSelectedPos) {
            return mSelectedPos.size();
        }
        return 0;
    }


    public ArrayList<String> getSelectedFileName() {
        ArrayList<String> selectedFiles = new ArrayList<>();

        if (null != mFileNames && null != mSelectedPos) {
            for (int i = 0; i < mSelectedPos.size(); ++i) {
                if (mSelectedPos.get(i) < mFileNames.size()) {
                    String name = dirPath + mFileNames.get(mSelectedPos.get(i));
                    selectedFiles.add(name);
                }
            }
            return selectedFiles;
        }
        return null;
    }

    private static class Holder {
        public AsyncImageView thumbImg;
        public TextView resolution;
        public TextView size;
        public TextView create_time;
        public View playView;
        public View selectView;
        public RelativeLayout thumbContainer;
    }
}
