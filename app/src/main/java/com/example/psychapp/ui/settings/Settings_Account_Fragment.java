package com.example.psychapp.ui.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.psychapp.ExitActivity;
import com.example.psychapp.applications.PsychApp;
import com.example.psychapp.ui.questions.QuestionnaireActivity;
import com.example.psychapp.R;
import com.example.psychapp.ui.login.LoginActivity;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static com.example.psychapp.ui.questions.QuestionnaireActivity.QUESTIONNAIRE_STATE;
import static com.example.psychapp.ui.questions.QuestionnaireActivity.setEnabled;

public class Settings_Account_Fragment extends Fragment {
    private int STOP_BUTTON_DISABLED_COLOR =  Color.argb((int)(.4f * 255), 0 ,0, 0);
    private int STOP_BUTTON_ENABLED_COLOR = Color.argb((int)(.8f * 255), 0 ,0, 0);

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.settings_account_fragment, container, false);

        Button logoutButton = root.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.confirm_logout))
                    .setMessage(getString(R.string.logout_text))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            logout();
                        }})
                    .setNegativeButton(android.R.string.no, null)
                    .show();
            }
        });

        final Button stopButton = root.findViewById(R.id.stop_button);
        final EditText reasoning = root.findViewById(R.id.stop_reasoning);
        final TextView reasoningLabel = root.findViewById(R.id.reasoning_label);
        if(!LoginActivity.user.getAllowUserTermination()){
            stopButton.setVisibility(View.GONE);
            reasoning.setVisibility(View.GONE);
            reasoningLabel.setVisibility(View.GONE);
            return root;
        }

        reasoning.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().length() > 0){
                    stopButton.setEnabled(true);
                    stopButton.setBackgroundColor(STOP_BUTTON_ENABLED_COLOR);
                } else{
                    stopButton.setEnabled(false);
                    stopButton.setBackgroundColor(STOP_BUTTON_DISABLED_COLOR);
                }
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity())
                        .setTitle(getString(R.string.confirm_termination))
                        .setMessage(getString(R.string.termination_text))
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                stopResearch(reasoning.getText().toString());
                            }})
                        .setNegativeButton(android.R.string.no, null)
                        .show();
            }
        });

        return root;
    }

    private void logout(){
        PsychApp.getContext().deleteFile(QuestionnaireActivity.QUESTIONS);
        PsychApp.getContext().deleteFile(QuestionnaireActivity.ANSWERS);
        setEnabled(false);
        LoginActivity.clearInfo();
        PsychApp.clearNotifications();
        ExitActivity.exitApplication(getActivity());
    }

    private void stopResearch(final String reason){
        String url = PsychApp.serverUrl + "participant/status/" + LoginActivity.user.getUserId();

        Map<String, String> params = new HashMap<>();
        params.put("value", ""+false);
        params.put("reasoning", ""+reason);
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        PsychApp.clearNotifications();
                        setEnabled(false);
                        LoginActivity.clearInfo();
                        ExitActivity.exitApplication(getActivity());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), error.toString(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-access-token", LoginActivity.user.getToken());
                return params;
            }
        };

        // add it to the RequestQueue
        PsychApp.queue.add(postRequest);
    }
}