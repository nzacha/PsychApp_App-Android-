package com.example.psychapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.psychapp.R;
import com.example.psychapp.applications.PsychApp;
import com.example.psychapp.data.Question;
import com.example.psychapp.ui.login.ConsentActivity;
import com.example.psychapp.ui.login.LoginActivity;
import com.example.psychapp.ui.questions.QuestionnaireActivity;
import com.example.psychapp.ui.settings.NotificationReceiver;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class IntroductionActivity extends AppCompatActivity {
    public static final String DESCRIPTION = "Description";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);

        Bundle b = getIntent().getExtras();
        boolean exitParam = false;
        if(b != null)
            exitParam = b.getBoolean("new_user");
        final boolean newUser = exitParam;

        try {
            loadDescription((TextView) findViewById(R.id.descriptionText),(TextView) findViewById(R.id.nameText),(TextView) findViewById(R.id.emailText),(TextView) findViewById(R.id.phoneText));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if(!newUser) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } else {
            CollapsingToolbarLayout toolbar_layout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
            float dip = 32f;
            Resources r = getResources();
            float px = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
            toolbar_layout.setExpandedTitleMarginStart((int) px);
        }

        Button okButton = findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(newUser){
                    Intent intent= new Intent(getApplicationContext(), ConsentActivity.class);
                    startActivity(intent);
                }
                finish();
            }
        });
    }

    public void loadDescription(TextView description, TextView name, TextView email, TextView phone) throws IOException, ClassNotFoundException {
        if(PsychApp.isNetworkConnected(this)) {
            setDescriptionFromDB(description, name, email, phone);
        } else {
            loadDescriptionFromFile(description, name, email, phone);
        }
        String text = description.getText().toString();
        if(text.equals("null") || text.equals("")){
            description.setText("No description available");
        }
    }

    private void loadDescriptionFromFile(TextView description, TextView name, TextView email, TextView phone) throws IOException, ClassNotFoundException {
        FileInputStream fis = PsychApp.getContext().openFileInput(DESCRIPTION);
        ObjectInputStream is = new ObjectInputStream(fis);
        String[] data = (String[]) is.readObject();
        description.setText(data[0]);
        name.setText(data[1]);
        email.setText(data[2]);
        phone.setText(data[3]);
        is.close();
        fis.close();
        Log.d("wtf", "Description loaded from Phone");
    }

    public void saveDescriptionLocally(String[] data) throws IOException {
        FileOutputStream fos = PsychApp.getContext().openFileOutput(DESCRIPTION, Context.MODE_PRIVATE);
        ObjectOutputStream os = new ObjectOutputStream(fos);
        os.writeObject(data);
        os.close();
        fos.close();
        Log.d("wtf", "Description saved on Phone");
    }

    public void setDescriptionFromDB(final TextView description,final TextView name,final TextView email,final TextView phone) throws IOException {
        String url = PsychApp.serverUrl + "project/"+ LoginActivity.user.getProjectId();
        Log.i("wtf", url);

        // prepare the Request
        final String[] text = new String[4];
        final JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
            new Response.Listener<JSONObject>(){
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        JSONObject project = response.getJSONObject("data").getJSONObject("response");
                        //Log.i("wtf", project.toString());

                        JSONObject director = project.getJSONObject("director");
                        //Log.i("wtf", director.toString());
                        text[0] = project.getString("description");
                        if(!text[0].equals("null")){
                            description.setText(text[0]);
                        }
                        text[1] = director.getString("first_name") + " " + director.getString("last_name");
                        if(!text[1].equals("null")){
                            name.setText(text[1]);
                        }
                        text[2] = director.getString("email");
                        if(!text[2].equals("null")){
                            email.setText(text[2]);
                        }
                        text[3] = director.getString("phone");
                        if(!text[3].equals("null")){
                            phone.setText(text[3]);
                        }
                        saveDescriptionLocally(text);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            },
            new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("wtf", "error occurred at setDescriptionFromDB()");
                }
            }
        ){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-access-token", LoginActivity.user.getToken());
                return params;
            }
        };

        // add it to the RequestQueue
        PsychApp.queue.add(getRequest);
    }
}