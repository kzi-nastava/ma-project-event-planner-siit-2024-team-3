package com.example.eveant;

import android.util.Log;

import com.example.eveant.event.EventService;
import com.example.eveant.event.agenda.ActivityService;
import com.example.eveant.event.invitations.InvitationService;
import com.example.eveant.eventType.EventTypeService;
import com.example.eveant.service.ServiceService;
import com.example.eveant.user.UserService;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
   public static final String SERVICE_API_PATH = "http://"+ BuildConfig.IP_ADDR +":8080/api/";

   public static Retrofit retrofit=new Retrofit.Builder()
           .baseUrl(SERVICE_API_PATH)
           .addConverterFactory(GsonConverterFactory.create())
           .client(test())
           .build();


   public static OkHttpClient test(){
      Log.d("RetrofitClient", "Base URL: " + SERVICE_API_PATH);
      HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
      interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

      OkHttpClient client = new OkHttpClient.Builder()
              .connectTimeout(30, TimeUnit.SECONDS)
              .readTimeout(30, TimeUnit.SECONDS)
              .writeTimeout(30, TimeUnit.SECONDS)
              .addInterceptor(interceptor).build();

      return client;
   }

   public static ServiceService serviceService =retrofit.create(ServiceService.class);
   public static CategoryService categoryService =retrofit.create(CategoryService.class);
   public static EventTypeService eventTypeService = retrofit.create(EventTypeService.class);
   public static EventService eventService = retrofit.create(EventService.class);
   public static UserService userService = retrofit.create(UserService.class);
   public static ActivityService activityService = retrofit.create(ActivityService.class);
   public static InvitationService invitationService = retrofit.create(InvitationService.class);
}
