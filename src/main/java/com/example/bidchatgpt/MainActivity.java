package com.example.bidchatgpt;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView welcomeTextView;
    EditText messageEditText;
    ImageButton sendButton;



    List<Message> messageList;

    MessageAdapter messageAdapter;


    public static final MediaType JSON
            = MediaType.get("application/json; charset=utf-8");
    OkHttpClient client = new OkHttpClient().newBuilder().readTimeout(60, TimeUnit.SECONDS)
            .build();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        messageList=new ArrayList<>();


        recyclerView =findViewById(R.id.recycler_view);
        welcomeTextView=findViewById(R.id.welcome_text);
        messageEditText=findViewById(R.id.message_edit_text);
        sendButton=findViewById(R.id.send_btn);


        messageAdapter=new MessageAdapter(messageList);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager llm=new LinearLayoutManager(this);
        llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        sendButton.setOnClickListener((v) -> {
            String question=messageEditText.getText().toString().trim();
          addToChat(question,Message.SENT_BY_ME);
          messageEditText.setText("");
          callAPI(question);
          welcomeTextView.setVisibility(View.GONE);
        });
    }

    void addToChat(String message,String sentBy){
        runOnUiThread(new Runnable() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void run() {
                messageList.add(new Message(message,sentBy));
                messageAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount());

            }
        });

    }

    void  addResponse(String response){
        addToChat(response,Message.SENT_BY_BOT);
    }

    void callAPI(String question){
       messageList.add(new Message("Typing...",Message.SENT_BY_BOT));
        JSONObject jsonBody=new JSONObject();
        try {
            jsonBody.put("model","gpt-3.5-turbo");
            JSONArray messageArr=new JSONArray();
            JSONObject obj=new JSONObject();
            obj.put("role","user");
            obj.put("content",question);
            messageArr.put(obj);

            jsonBody.put("messages",messageArr);
        } catch (JSONException e) {
          e.printStackTrace();
        }

        RequestBody body=RequestBody.create(jsonBody.toString(),JSON);
        Request request=new Request.Builder()
                .url("https_api_openai_com_v1_chat_completions")
                .header("Authorization","Bearer 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")
                .post(body)
                .build();

client.newCall(request).enqueue(new Callback() {
    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException e) {
        addResponse("Failed load response due to "+e.getMessage());
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
if (response.isSuccessful()){
    JSONObject jsonObject= null;
    try {

        jsonObject = new JSONObject(response.body().string());
        JSONArray jsonArray=jsonObject.getJSONArray("choices");
        String result=jsonArray.getJSONObject(0)
                        .getJSONObject("message")
                                .getString("content");
        addResponse(result.trim());
    } catch (JSONException e) {
        e.printStackTrace();
    }

}else {

    addResponse("Failed load response due to "+response.body().toString());
}
    }
});
    }

}