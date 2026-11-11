package com.jarvispro.assistant;

import android.app.*;
import android.os.*;
import android.content.*;
import android.media.projection.*;
import android.speech.*;
import android.speech.tts.TextToSpeech;
import android.view.*;
import android.widget.*;
import android.graphics.Color;
import android.net.Uri;
import java.net.*;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
    private LinearLayout root, messages;
    private EditText input;
    private TextToSpeech tts;
    private static final int SPEECH=101, SCREEN=102;
    private MediaProjectionManager projectionManager;

    public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(8,6,17));
        getWindow().setNavigationBarColor(Color.rgb(8,6,17));
        projectionManager=(MediaProjectionManager)getSystemService(MEDIA_PROJECTION_SERVICE);
        tts=new TextToSpeech(this, status -> { if(status==TextToSpeech.SUCCESS) tts.setLanguage(new Locale("en","US")); });
        buildUI();
    }
    int dp(int n){ return (int)(n*getResources().getDisplayMetrics().density); }
    TextView label(String s,int size,int color){ TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); t.setPadding(dp(12),dp(9),dp(12),dp(9)); return t; }
    void buildUI(){
        root=new LinearLayout(this); root.setOrientation(1); root.setPadding(dp(16),dp(18),dp(16),dp(12)); root.setBackgroundColor(Color.rgb(8,6,17));
        TextView title=label("J.A.R.V.I.S. PRO",25,Color.rgb(211,174,255)); root.addView(title);
        root.addView(label("GLOBAL AI • VOICE • CAREER • BROWSER",11,Color.LTGRAY));
        TextView status=label("Assistant ready • Connect your secure Gemini backend",12,Color.rgb(160,220,190)); root.addView(status);
        HorizontalScrollView hsv=new HorizontalScrollView(this); LinearLayout actions=new LinearLayout(this);
        addAction(actions,"🎙 Speak",()->listen()); addAction(actions,"📱 Screen share",()->screenShare());
        addAction(actions,"🌐 Browser",()->openUrl("https://www.google.com")); addAction(actions,"🇲🇾 Malaysia jobs",()->jobSearch("Malaysia")); addAction(actions,"🇨🇦 Canada jobs",()->jobSearch("Canada"));
        hsv.addView(actions); root.addView(hsv);
        ScrollView scroll=new ScrollView(this); messages=new LinearLayout(this); messages.setOrientation(1);
        messages.addView(label("JARVIS: Hello jaan. I am ready. Type a command or use Speak.",14,Color.WHITE));
        scroll.addView(messages); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        input=new EditText(this); input.setSingleLine(false); input.setHint("Ask JARVIS anything..."); input.setTextColor(Color.WHITE); input.setHintTextColor(0xff998eaa); input.setBackgroundColor(0xff211833); root.addView(input,new LinearLayout.LayoutParams(-1,dp(54)));
        Button send=new Button(this); send.setText("SEND TO JARVIS"); send.setOnClickListener(v->{String q=input.getText().toString().trim(); if(!q.isEmpty()){input.setText(""); ask(q);}}); root.addView(send);
        setContentView(root);
    }
    void addAction(LinearLayout row,String name,Runnable r){ Button b=new Button(this); b.setText(name); b.setOnClickListener(v->r.run()); row.addView(b); }
    void addMessage(String who,String msg){ runOnUiThread(()->messages.addView(label(who+": "+msg,14,Color.WHITE))); }
    void ask(String q){
        addMessage("YOU",q);
        if(q.toLowerCase().contains("open youtube")){openUrl("https://youtube.com"); return;}
        if(q.toLowerCase().contains("open browser")){openUrl("https://google.com"); return;}
        if(q.toLowerCase().contains("malaysia")){jobSearch("Malaysia");}
        if(q.toLowerCase().contains("canada")){jobSearch("Canada");}
        new Thread(()->{
            try{
                URL url=new URL("http://127.0.0.1:8787/chat");
                HttpURLConnection c=(HttpURLConnection)url.openConnection(); c.setRequestMethod("POST"); c.setDoOutput(true); c.setConnectTimeout(10000); c.setReadTimeout(60000); c.setRequestProperty("Content-Type","application/json");
                String body="{\"message\":\""+q.replace("\\","\\\\").replace("\"","\\\"").replace("\n"," ")+"\"}";
                c.getOutputStream().write(body.getBytes("UTF-8"));
                String result=new String(c.getInputStream().readAllBytes(),"UTF-8");
                org.json.JSONObject obj=new org.json.JSONObject(result);
                String answer=obj.optString("reply","No response");
                addMessage("JARVIS",answer); speak(answer);
            }catch(Exception e){addMessage("JARVIS","AI backend is not connected. Start the Termux backend and check your Gemini API key. ("+e.getMessage()+")");}
        }).start();
    }
    void speak(String s){ if(tts!=null) tts.speak(s,TextToSpeech.QUEUE_FLUSH,null,"jarvis"); }
    void listen(){
        try{
            Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"bn-BD");
            i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to JARVIS");
            startActivityForResult(i,SPEECH);
        }catch(Exception e){addMessage("SYSTEM","Speech recognition is unavailable on this device.");}
    }
    void screenShare(){ if(projectionManager!=null) startActivityForResult(projectionManager.createScreenCaptureIntent(),SCREEN); }
    protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==SPEECH && resultCode==RESULT_OK && data!=null){ ArrayList<String> a=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS); if(a!=null&&!a.isEmpty()) ask(a.get(0));}
        if(requestCode==SCREEN){ if(resultCode==RESULT_OK) addMessage("SYSTEM","Screen capture permission granted for this session. This starter shows permission flow; live frame streaming is not yet implemented."); else addMessage("SYSTEM","Screen sharing cancelled.");}
    }
    void jobSearch(String country){ openUrl("https://www.google.com/search?q="+Uri.encode(country+" jobs foreign workers official career portal")); }
    void openUrl(String u){ try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(u)));}catch(Exception e){addMessage("SYSTEM","Cannot open link.");} }
    protected void onDestroy(){ if(tts!=null){tts.stop();tts.shutdown();} super.onDestroy(); }
}
