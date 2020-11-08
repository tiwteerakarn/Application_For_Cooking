package com.example.cookingapp.Adapter;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingapp.Model.ModelChat;

import com.example.cookingapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MyHolder> {

    FirebaseUser fUser;
    private static final int MSG_LEFT = 0;
    private static final int MSG_RIGHT= 1;

    private List<ModelChat> modelChats ;
    private String imgChat;

    public ChatAdapter(List<ModelChat> modelChats, String imgChat) {
        this.modelChats = modelChats;
        this.imgChat = imgChat;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MSG_RIGHT){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat_right,parent, false);
            return new MyHolder(view);
        }else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chat_left,parent, false);
            return new MyHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        String message = modelChats.get(position).getMessage();
        String time = modelChats.get(position).getTimestamp();

        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(Long.parseLong(time));
        String dateTime = DateFormat.format("dd/MM/yyyy HH:mm:ss",calendar).toString();

        holder.message.setText(message);

       holder.time.setText(dateTime);

        try {
            Picasso.get().load(imgChat).placeholder(R.drawable.ic_user).into(holder.imageView);
        }catch (Exception e){

        }
    }

    @Override
    public int getItemViewType(int position) {
        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if (modelChats.get(position).getSender().equals(fUser.getUid())){
            return MSG_RIGHT;

        }else {
            return MSG_LEFT;
        }

    }

    @Override
    public int getItemCount() {
        return modelChats.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView message, time;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.profile_icon);
            message = itemView.findViewById(R.id.chat_message);
            time = itemView.findViewById(R.id.time);
        }
    }
}