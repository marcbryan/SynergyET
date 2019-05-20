package com.synergy.synergyet.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAA2r-vFy8:APA91bFo5EY9lKQ1E-jzUMyg8YXg8cw8I4YGNHzRoSO8iGYoNt90JE5XXzpcfwxlP_2E68rGlBX4z9ReLEYKR9liHprgWUbIQgXzyuoD5WT2-5PeLcN4WPrLVvG-vOk9S-sMyHr9BsX6"
            }
    )
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
