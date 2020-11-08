package com.example.cookingapp.Model;



public class Users {

    private String device_token;
    private String user_id;
    private String email;
    private String username;
    private String profile_photo;
    private int follower,following;
    private String logintime;
    private String vip;
    public Users() {
    }

    public Users(String user_id, String email, String username) {
        this.user_id = user_id;
        this.email = email;
        this.username = username;
    }

    public Users(String device_token, String user_id, String email, String username, String profile_photo) {
        this.device_token = device_token;
        this.user_id = user_id;
        this.email = email;
        this.username = username;
        this.profile_photo = profile_photo;
    }

    public Users(String user_id, String email, String username, String profile_photo) {
        this.user_id = user_id;
        this.email = email;
        this.username = username;
        this.profile_photo = profile_photo;
    }

    public Users(String device_token, String user_id, String email, String username, String profile_photo, int follower, int following) {
        this.device_token = device_token;
        this.user_id = user_id;
        this.email = email;
        this.username = username;
        this.profile_photo = profile_photo;
        this.follower = follower;
        this.following = following;
    }

    public Users(String device_token, String user_id, String email, String username, String profile_photo, int follower, int following, String logintime) {
        this.device_token = device_token;
        this.user_id = user_id;
        this.email = email;
        this.username = username;
        this.profile_photo = profile_photo;
        this.follower = follower;
        this.following = following;
        this.logintime = logintime;
    }

    public Users(String device_token, String user_id, String email, String username, String profile_photo, int follower, int following, String logintime, String vip) {
        this.device_token = device_token;
        this.user_id = user_id;
        this.email = email;
        this.username = username;
        this.profile_photo = profile_photo;
        this.follower = follower;
        this.following = following;
        this.logintime = logintime;
        this.vip = vip;
    }

    public int getFollower() {
        return follower;
    }

    public void setFollower(int follower) {
        this.follower = follower;
    }

    public int getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfile_photo() {
        return profile_photo;
    }

    public void setProfile_photo(String profile_photo) {
        this.profile_photo = profile_photo;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getLogintime() {
        return logintime;
    }

    public void setLogintime(String logintime) {
        this.logintime = logintime;
    }



    public String getVip() {
        return vip;
    }

    public void setVip(String vip) {
        this.vip = vip;
    }
}
