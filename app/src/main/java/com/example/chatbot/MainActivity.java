package com.example.chatbot;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.JsonElement;

import java.util.Map;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;




public class MainActivity extends AppCompatActivity  implements AIListener {

    private Button listenButton,send;
    private TextView resultTextView;
    EditText mess;
    private AIRequest aiRequest;
    private AIService aiService;
    AIDataService aiDataService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        final AIConfiguration config = new AIConfiguration("<CLIENT-ACCESS-TOKEN>",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);  //Replace with your client token


        aiService = AIService.getService(this, config);
        aiService.setListener(this);
        aiDataService = new AIDataService(config);
        aiRequest = new AIRequest();
        listenButton = (Button) findViewById(R.id.listenButton);
        resultTextView = (TextView) findViewById(R.id.resultTextView);
        send = (Button)findViewById(R.id.send);
        mess = (EditText)findViewById(R.id.mess);


    }
    @Override
    public void onResult(AIResponse response) {
        Result result = response.getResult();
        // Get parameters
        String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }

        // Show results in TextView.
        resultTextView.setText("Query:" + result.getResolvedQuery() +
                "\nAction: " + result.getAction() +
                "\nParameters: " + parameterString
        + "\nTO speech " + result.getFulfillment().getSpeech());
        listenButton.setEnabled(true);

    }
    @Override
    public void onError(AIError error) {
        resultTextView.setText(error.toString());
    }
    @Override
    public void onAudioLevel(float level) {

    }
    @Override
    public void onListeningStarted() {

    }
    @Override
    public void onListeningCanceled() {

    }
    @Override
    public void onListeningFinished() {

    }

    public void listenButtonOnClick(View view) {
        aiService.startListening();
        listenButton.setEnabled(false);
    }

    public void send(View view) {

        aiRequest.setQuery(mess.getText().toString());
        new AsyncTask<AIRequest, Void, AIResponse>() {
            @Override
            protected AIResponse doInBackground(AIRequest... requests) {
                final AIRequest request = requests[0];
                try {
                    final AIResponse response = aiDataService.request(aiRequest);
                    return response;
                } catch (AIServiceException e) {
                }
                return null;
            }
            @Override
            protected void onPostExecute(AIResponse aiResponse) {
                if (aiResponse != null) {
                    onResult(aiResponse);// process aiResponse here
                }
            }
        }.execute(aiRequest);
        mess.setText("");
    }
}
