package com.example.cookingapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cookingapp.Chats.ChatsActivity;
import com.example.cookingapp.Model.Users;

import com.example.cookingapp.R;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;

public class ChatlistAdapter extends RecyclerView.Adapter<ChatlistAdapter.MyHolder> {
    private Context context;
    private LinearLayout linearLayout;


    List<Users> usersList;
    private HashMap<String, String> lastmessageMap;



    public ChatlistAdapter(Context context, List<Users> usersList) {
        this.context = context;
        this.usersList = usersList;
        lastmessageMap = new HashMap<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_chatlist, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MyHolder holder, final int position) {

        final String Hisuid = usersList.get(position).getUser_id();
        final String userImg = usersList.get(position).getProfile_photo();
        String username = usersList.get(position).getUsername();
        String lastmess = lastmessageMap.get(Hisuid);

        holder.name.setText(username);
        holder.lastMessage.setText(lastmess);

        try {
            Picasso.get().load(userImg).placeholder(R.drawable.ic_user).into(holder.imgpro);
        } catch (Exception e) {

        }



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Intent intent = new Intent(context, ChatsActivity.class);
                intent.putExtra("hisid",Hisuid);
                intent.putExtra("img",userImg);
                intent.putExtra("name",username);
                context.startActivity(intent);


            }
        });
    }

    public void setLastmessageMap(String uId, String lastMessage) {
        lastmessageMap.put(uId, lastMessage);
    }

    @Override
    public int getItemCount() {
        return usersList.size();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        ImageView imgpro, onlinestatus;
        TextView name, lastMessage;

        public MyHolder(@NonNull View itemView) {
            super(itemView);

            imgpro = itemView.findViewById(R.id.profile_iv);
            name = itemView.findViewById(R.id.hisname);
            lastMessage = itemView.findViewById(R.id.histext);

        }
    }
}
