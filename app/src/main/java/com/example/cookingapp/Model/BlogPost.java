package com.example.cookingapp.Model;


import android.os.Parcel;
import android.os.Parcelable;

public class BlogPost extends BlogPostId implements Parcelable {

    public String user_id, image_url, desc;
    public String timestamp;

    public BlogPost() {
    }

    public BlogPost(String user_id, String image_url, String desc, String timestamp) {
        this.user_id = user_id;
        this.image_url = image_url;
        this.desc = desc;
        this.timestamp = timestamp;
    }

    protected BlogPost(Parcel in) {
        user_id = in.readString();
        image_url = in.readString();
        desc = in.readString();
        timestamp = in.readString();
    }

    public static final Creator<BlogPost> CREATOR = new Creator<BlogPost>() {
        @Override
        public BlogPost createFromParcel(Parcel in) {
            return new BlogPost(in);
        }

        @Override
        public BlogPost[] newArray(int size) {
            return new BlogPost[size];
        }
    };

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(user_id);
        parcel.writeString(image_url);
        parcel.writeString(desc);
        parcel.writeString(timestamp);

    }
}
