package com.example.cookingapp.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIServer {
    @Headers({
            "Authorization:key=AAAAK_jJGA0:APA91bFZ9i2ghKROEuDp2zf7M0uI04InH-F9x9BpXXKIDhVD97GRBvYk6enkbsBnzZAsLF_7JBzfXV5XQphnzSJxZmYyVleTAm1BJWFKFbheXqJABSH6xMbgNwi7sWBn-EIObBU2Tbeu",
            "Content-Type:application/json"
    })

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);

}
