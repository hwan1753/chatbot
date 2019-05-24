package com.example.chatbot;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;

import java.util.Locale;
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




public class MainActivity extends AppCompatActivity  implements AIListener, TextToSpeech.OnInitListener {

    private Button send;
    ImageButton listenButton;
    private TextView resultTextView;
    private final int MY_PERMISSIONS_RECORD_AUDIO = 1;
    TextToSpeech textToSpeech;
    boolean granted = false;
    EditText mess;
    private AIRequest aiRequest;
    private AIService aiService;
    AIDataService aiDataService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        while (!granted) {
            requestAudioPermissions();
        }

        final AIConfiguration config = new AIConfiguration("<CLIENT-TOKEN>",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System); //Enter your token in <CLIENT-TOKEN>
        textToSpeech = new TextToSpeech(this,  this);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);
        aiDataService = new AIDataService(config);
        aiRequest = new AIRequest();
        listenButton = (ImageButton) findViewById(R.id.listenButton);
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
        speakOut(result.getFulfillment().getSpeech());
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


    private void requestAudioPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            //When permission is not granted by user, show them message why this permission is needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                Toast.makeText(this, "Please grant permissions to record audio", Toast.LENGTH_LONG).show();

                //Give user option to still opt-in the permissions
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);

            } else {
                // Show user dialog to grant permission to record audio
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        MY_PERMISSIONS_RECORD_AUDIO);
            }
        }
        //If permission is granted, then go ahead recording audio
        else if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {

            granted = true;
        }
    }

    /**
     * Called to signal the completion of the TextToSpeech engine initialization.
     *
     *
     *
     */

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
        super.onDestroy();
    }

    private void speakOut(String out) {
        textToSpeech.speak(out, TextToSpeech.QUEUE_ADD, null);

    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {

            int result = textToSpeech.setLanguage(Locale.ENGLISH);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This Language is not supported");
            } else {
                speakOut("Permission Granted");
            }

        } else {
            Log.e("TTS", "Initilization Failed!");
        }
    }
}
