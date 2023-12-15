package com.ashishsaranshakya.chateasy.services;

import com.ashishsaranshakya.chateasy.models.http.AddUserToGroupRequest;
import com.ashishsaranshakya.chateasy.models.http.AuthRequest;
import com.ashishsaranshakya.chateasy.models.http.CreateChatResponse;
import com.ashishsaranshakya.chateasy.models.http.CreateGroupRequest;
import com.ashishsaranshakya.chateasy.models.http.DeleteChatResponse;
import com.ashishsaranshakya.chateasy.models.http.GenericResponse;
import com.ashishsaranshakya.chateasy.models.http.LoginResponse;
import com.ashishsaranshakya.chateasy.models.socket.SearchUserResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface HttpService {
    @POST("api/v1/auth/login")
    Call<LoginResponse> login(@Body AuthRequest authRequest);

    @POST("api/v1/auth/register")
    Call<GenericResponse> register(@Body AuthRequest authRequest);

    @POST("api/v1/chat/user/{id}")
    Call<CreateChatResponse> createChat(@Header("Authorization") String authorization, @Path("id") String userId);

    @DELETE("api/v1/chat/{id}")
    Call<DeleteChatResponse> deleteChat(@Header("Authorization") String authorization, @Path("id") String chatId);

    @DELETE("api/v1/chat/group/{id}")
    Call<DeleteChatResponse> deleteGroup(@Header("Authorization") String authorization, @Path("id") String chatId);

    @PUT("api/v1/chat/group/{id}")
    Call<GenericResponse> addUserToGroup(@Header("Authorization") String authorization, @Path("id") String chatId, @Body AddUserToGroupRequest addUserToGroupRequest);

    @POST("api/v1/chat/group")
    Call<CreateChatResponse> createGroup(@Header("Authorization") String authorization, @Body CreateGroupRequest createGroupRequest);

    @GET("api/v1/chat/group/{id}")
    Call<SearchUserResponse> getGroupMembers(@Header("Authorization") String authorization, @Path("id") String chatId);

    @PATCH("api/v1/chat/group/{id}")
    Call<GenericResponse> removeUserFromGroup(@Header("Authorization") String authorization, @Path("id") String chatId, @Body AddUserToGroupRequest addUserToGroupRequest);
}
