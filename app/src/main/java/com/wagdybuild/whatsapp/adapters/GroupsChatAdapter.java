package com.wagdybuild.whatsapp.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.wagdybuild.whatsapp.R;
import com.wagdybuild.whatsapp.models.DBGroupMessage;
import com.wagdybuild.whatsapp.models.Message;
import com.wagdybuild.whatsapp.ui.ViewImageActivity;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupsChatAdapter extends RecyclerView.Adapter {
    private final Context context;
    private ArrayList<DBGroupMessage> messageList;
    private final int SENDER_VIEW_TYPE = 1;
    private final int RECEIVER_VIEW_TYPE = 2;
    private String receivers_ids = "";
    private MediaController mc;
    private String group_id;

    private final FirebaseAuth mAuth;
    private final FirebaseDatabase db;

    public GroupsChatAdapter(Context context, ArrayList<DBGroupMessage> messageList, String group_id) {
        this.context = context;
        this.messageList = messageList;
        this.group_id = group_id;

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        mc = new MediaController(context);
    }

    public GroupsChatAdapter(Context context, String group_id) {
        this.context = context;
        this.group_id = group_id;

        messageList = new ArrayList<>();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        mc = new MediaController(context);
    }

    public void setMessageList(ArrayList<DBGroupMessage> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == SENDER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.group_sender_view, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.group_receiver_view, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        DBGroupMessage message = messageList.get(position);
        if (holder.getClass() == SenderViewHolder.class) {
            Date date = new Date(message.getTime());
            SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
            String time_stamp = dateFormat.format(date);

            ((SenderViewHolder) holder).sender_civ.setOnClickListener(view -> {
                Intent intent = new Intent(context, ViewImageActivity.class);
                intent.putExtra("imageUri", message.getSender_imageUri());
                context.startActivity(intent);
            });
            //calculating differences between dates
            //long diff = calculateDateDiff(message.getTime());
            if (message.getMessage_type().equals("image")) {
                ((SenderViewHolder) holder).sender_layout_message.setVisibility(View.GONE);
                ((SenderViewHolder) holder).sender_layout_video.setVisibility(View.GONE);
                ((SenderViewHolder) holder).sender_layout_music.setVisibility(View.GONE);
                ((SenderViewHolder) holder).sender_layout_file.setVisibility(View.GONE);

                ((SenderViewHolder) holder).sender_image_time.setText(time_stamp);

                Glide.with(context.getApplicationContext()).load(message.getImageUri())
                        .placeholder(R.drawable.ic_person)
                        .into(((SenderViewHolder) holder).sender_image);

            } else if (message.getMessage_type().equals("video")) {
                ((SenderViewHolder) holder).sender_layout_image.setVisibility(View.GONE);
                ((SenderViewHolder) holder).sender_layout_message.setVisibility(View.GONE);
                ((SenderViewHolder) holder).sender_layout_music.setVisibility(View.GONE);
                ((SenderViewHolder) holder).sender_layout_file.setVisibility(View.GONE);

                ((SenderViewHolder) holder).sender_video_time.setText(time_stamp);

            } else if (message.getMessage_type().equals("music")) {
                ((SenderViewHolder) holder).sender_layout_image.setVisibility(View.GONE);
                ((SenderViewHolder) holder).sender_layout_message.setVisibility(View.GONE);
                ((SenderViewHolder) holder).sender_layout_video.setVisibility(View.GONE);
                ((SenderViewHolder) holder).sender_layout_file.setVisibility(View.GONE);

                ((SenderViewHolder) holder).sender_music_time.setText(time_stamp);

            } else if (message.getMessage_type().equals("file")) {
                ((SenderViewHolder) holder).sender_layout_image.setVisibility(View.GONE);
                ((SenderViewHolder) holder).sender_layout_message.setVisibility(View.GONE);
                ((SenderViewHolder) holder).sender_layout_music.setVisibility(View.GONE);
                ((SenderViewHolder) holder).sender_layout_video.setVisibility(View.GONE);

                ((SenderViewHolder) holder).sender_file_time.setText(time_stamp);

            } else {
                ((SenderViewHolder) holder).sender_layout_image.setVisibility(View.GONE);
                ((SenderViewHolder) holder).sender_layout_video.setVisibility(View.GONE);
                ((SenderViewHolder) holder).sender_layout_music.setVisibility(View.GONE);
                ((SenderViewHolder) holder).sender_layout_file.setVisibility(View.GONE);

                ((SenderViewHolder) holder).sender_message.setText(message.getMessage());
                ((SenderViewHolder) holder).sender_message_time.setText(time_stamp);
                ((SenderViewHolder) holder).sender_name.setText(message.getSender_name());

                if (message.getSender_imageUri() != null) {
                    Glide.with(context.getApplicationContext()).load(message.getSender_imageUri())
                            .placeholder(R.drawable.ic_person)
                            .into(((SenderViewHolder) holder).sender_civ);
                }
            }
        } else {
            Date date = new Date(message.getTime());
            SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
            String time_stamp = dateFormat.format(date);

            ((ReceiverViewHolder) holder).receiver_civ.setOnClickListener(view -> {
                Intent intent = new Intent(context, ViewImageActivity.class);
                intent.putExtra("imageUri", message.getSender_imageUri());
                context.startActivity(intent);
            });

            if (message.getMessage_type().equals("image")) {
                ((ReceiverViewHolder) holder).receiver_layout_message.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receiver_layout_video.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receiver_layout_music.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receiver_layout_file.setVisibility(View.GONE);

                ((ReceiverViewHolder) holder).receiver_image_time.setText(time_stamp);

                Glide.with(context.getApplicationContext()).load(message.getImageUri())
                        .placeholder(R.drawable.ic_person)
                        .into(((ReceiverViewHolder) holder).receiver_image);

            } else if (message.getMessage_type().equals("video")) {
                ((ReceiverViewHolder) holder).receiver_layout_message.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receiver_layout_image.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receiver_layout_music.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receiver_layout_file.setVisibility(View.GONE);

                ((ReceiverViewHolder) holder).receiver_video_time.setText(time_stamp);

            } else if (message.getMessage_type().equals("music")) {
                ((ReceiverViewHolder) holder).receiver_layout_message.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receiver_layout_image.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receiver_layout_video.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receiver_layout_file.setVisibility(View.GONE);

                ((ReceiverViewHolder) holder).receiver_music_time.setText(time_stamp);

            } else if (message.getMessage_type().equals("file")) {
                ((ReceiverViewHolder) holder).receiver_layout_message.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receiver_layout_image.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receiver_layout_music.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receiver_layout_video.setVisibility(View.GONE);

                ((ReceiverViewHolder) holder).receiver_file_time.setText(time_stamp);

            } else {
                ((ReceiverViewHolder) holder).receiver_layout_image.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receiver_layout_video.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receiver_layout_music.setVisibility(View.GONE);
                ((ReceiverViewHolder) holder).receiver_layout_file.setVisibility(View.GONE);

                ((ReceiverViewHolder) holder).receiver_message_time.setText(time_stamp);
                ((ReceiverViewHolder) holder).receiver_message.setText(message.getMessage());
                ((ReceiverViewHolder) holder).receiver_name.setText(message.getSender_name());

                if (message.getSender_imageUri() != null) {
                    Glide.with(context.getApplicationContext()).load(message.getSender_imageUri())
                            .placeholder(R.drawable.ic_person)
                            .into(((ReceiverViewHolder) holder).receiver_civ);
                }
            }
        }

        holder.itemView.setOnLongClickListener(view -> {
            new AlertDialog.Builder(context).setTitle("Delete").setMessage("Are you sure you want to delete -> " + message.getMessage())
                    .setPositiveButton("Yes", (dialogInterface, i) -> {
                        db.getReference().child("Groups_Chat").child(group_id).child(message.getMessage_id()).removeValue().addOnSuccessListener(unused -> {
                            Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                        });

                    }).setNegativeButton("No", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    }).show();
            return false;
        });

        holder.itemView.setOnClickListener(view -> {
            if (message.getMessage_type().equals("image")) {
                Intent intent = new Intent(context, ViewImageActivity.class);
                intent.putExtra("imageUri", message.getImageUri());
                context.startActivity(intent);
            } else if (message.getMessage_type().equals("video")) {
                /*Intent intent=new Intent(context, ViewVideoActivity.class);
                intent.putExtra("videoUri",message.getImageUri());
                context.startActivity(intent);*/

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getImageUri()));
                intent.setDataAndType(Uri.parse(message.getImageUri()), "video/mp4");
                context.startActivity(intent);
            } else if (message.getMessage_type().equals("music")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getImageUri()));
                intent.setDataAndType(Uri.parse(message.getImageUri()), "audio/*");
                context.startActivity(intent);
            } else if (message.getMessage_type().equals("file")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getImageUri()));
                //intent.setDataAndType(Uri.parse(message.getImageUri()), "application/pdf");
                intent.setDataAndType(Uri.parse(message.getImageUri()), "application/pdf");
                context.startActivity(intent);
            }

        });


    }

    @SuppressLint("SimpleDateFormat")
    private long calculateDateDiff(long created_date) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("dd:MM:yyyy HH:mm a");
        Date date_old = sdf.parse(String.valueOf(created_date));
        Date date_new = sdf.parse(String.valueOf(new Date().getTime()));
        long def_millis = date_new.getTime() - date_old.getTime();

        long days = def_millis / 1000 / 60 / 60 / 24;
        long hours = def_millis / 1000 / 60 / 60;
        long minutes = def_millis / 1000 / 60;

        return days;

        /* val sdf: String = SimpleDateFormat("dd:MM:yyyy HH:mm a")
        val date1: `val` = sdf.parse("10:10:2023 10:30 PM")
        val date2: `val` = sdf.parse("12:10:2023 10:40 PM")
        val def_millis: `val` = date2!!.time - date1!!.time*/

        /*SimpleDateFormat df = new SimpleDateFormat("dd:MM:yyyy HH:mm a");
                Date date_now_format = df.format(String.valueOf(new Date().getTime()));
                Date date_then_format = new Date(df.format(message.getTime())).getTime();

                long def=date_now_format-date_then_format;
                ((SenderViewHolder) holder).sender_message_time1.setText(
                        time_stamp + "<-- real \n"
                                + date_now_format + "<-- date now \n"
                                + date_then_format + "\n date then"
                                + date_then_format + "\n date then");*/
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getSender_id().equals(FirebaseAuth.getInstance().getUid())) {
            return SENDER_VIEW_TYPE;
        } else {
            return RECEIVER_VIEW_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        if (messageList == null) {
            return 0;
        } else {
            return messageList.size();
        }
    }

    public static class SenderViewHolder extends RecyclerView.ViewHolder {
        TextView sender_message, sender_message_time, sender_image_time, sender_video_time, sender_music_time, sender_file_time, sender_name;
        ImageView sender_image, sender_video, sender_music, sender_file;
        CircleImageView sender_civ;
        ConstraintLayout sender_layout_message;
        CardView sender_layout_image, sender_layout_video, sender_layout_music, sender_layout_file;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            sender_message_time = itemView.findViewById(R.id.group_sender_view_message_time);
            sender_image_time = itemView.findViewById(R.id.group_sender_view_image_time);
            sender_video_time = itemView.findViewById(R.id.group_sender_view_video_time);
            sender_music_time = itemView.findViewById(R.id.group_sender_view_music_time);
            sender_file_time = itemView.findViewById(R.id.group_sender_view_file_time);

            sender_layout_message = itemView.findViewById(R.id.group_sender_view_message_layout);
            sender_layout_image = itemView.findViewById(R.id.group_sender_view_image_layout);
            sender_layout_video = itemView.findViewById(R.id.group_sender_view_video_layout);
            sender_layout_music = itemView.findViewById(R.id.group_sender_view_music_layout);
            sender_layout_file = itemView.findViewById(R.id.group_sender_view_file_layout);

            sender_civ = itemView.findViewById(R.id.group_sender_view_civ);
            sender_name = itemView.findViewById(R.id.group_sender_view_name);

            sender_message = itemView.findViewById(R.id.group_sender_view_message);
            sender_image = itemView.findViewById(R.id.group_sender_view_messageImage);
            sender_video = itemView.findViewById(R.id.group_sender_view_messageVideo);
            sender_music = itemView.findViewById(R.id.group_sender_view_messageMusic);
            sender_file = itemView.findViewById(R.id.group_sender_view_messageFile);


        }
    }

    public static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        TextView receiver_message, receiver_message_time, receiver_image_time, receiver_video_time, receiver_music_time, receiver_file_time, receiver_name;
        ImageView receiver_image, receiver_video, receiver_music, receiver_file;
        CircleImageView receiver_civ;
        ConstraintLayout receiver_layout_message;
        CardView receiver_layout_image, receiver_layout_video, receiver_layout_music, receiver_layout_file;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            receiver_message_time = itemView.findViewById(R.id.group_receiver_view_message_time);
            receiver_image_time = itemView.findViewById(R.id.group_receiver_view_image_time);
            receiver_video_time = itemView.findViewById(R.id.group_receiver_view_video_time);
            receiver_music_time = itemView.findViewById(R.id.group_receiver_view_music_time);
            receiver_file_time = itemView.findViewById(R.id.group_receiver_view_file_time);

            receiver_layout_message = itemView.findViewById(R.id.group_receiver_view_message_layout);
            receiver_layout_image = itemView.findViewById(R.id.group_receiver_view_image_layout);
            receiver_layout_video = itemView.findViewById(R.id.group_receiver_view_video_layout);
            receiver_layout_music = itemView.findViewById(R.id.group_receiver_view_music_layout);
            receiver_layout_file = itemView.findViewById(R.id.group_receiver_view_file_layout);

            receiver_civ = itemView.findViewById(R.id.group_receiver_view_civ);
            receiver_name = itemView.findViewById(R.id.group_receiver_view_name);

            receiver_message = itemView.findViewById(R.id.group_receiver_view_message);
            receiver_image = itemView.findViewById(R.id.group_receiver_view_messageImage);
            receiver_video = itemView.findViewById(R.id.group_receiver_view_messageVideo);
            receiver_music = itemView.findViewById(R.id.group_receiver_view_messageMusic);
            receiver_file = itemView.findViewById(R.id.group_receiver_view_messageFile);

        }
    }
}
