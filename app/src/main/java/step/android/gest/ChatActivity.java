package step.android.gest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;


public class ChatActivity extends AppCompatActivity {

    private EditText etAuthor ;
    private EditText etMessage ;
    private LinearLayout chatContainer ;

    private Handler handler ;

    // Data Context
    private final ArrayList<ChatMessage> messages = new ArrayList<>() ;

    // URL:
    String chatUrl ;
    // URL response buffer
    private String urlResponse ;

    // URL response mapper:  String -> JSON -> messages
    private final Runnable mapUrlResponse = () -> {
        try {
            JSONObject response = new JSONObject( urlResponse ) ;
            int status = response.getInt( "status" ) ;
            if( status == 1 ) {
                JSONArray arr = response.getJSONArray( "data" ) ;
                boolean isUpdated = false ;
                for( int i = 0; i < arr.length(); ++i ) {
                    JSONObject obj = arr.getJSONObject( i ) ;
                    if( ! messagesContain( obj ) ) {
                        messages.add(
                                new ChatMessage( obj ) ) ;
                        isUpdated = true ;
                    }
                }
                if( isUpdated ) {
                    Collections.sort( messages ) ;
                    runOnUiThread( this::showMessagesInScroll ) ;
                }
            }
            else {
                Log.e( "mapUrlResponse: ", "Bad response status " + status ) ;
            }
        }
        catch( Exception ex ) {
            Log.e( "mapUrlResponse: ", ex.getMessage() ) ;
        }
    } ;


    // URL response loader: URL -> String
    private final Runnable loadUrlResponse = () -> {
        try( InputStream stream =
                 new URL( chatUrl )
                .openStream()
        ) {
            StringBuilder sb = new StringBuilder() ;
            int sym ;
            while( ( sym = stream.read() ) != -1 ) {
                sb.append( (char) sym ) ;
            }
            urlResponse = new String(
                    sb.toString().getBytes(StandardCharsets.ISO_8859_1),
                    StandardCharsets.UTF_8
            ) ;
            new Thread( mapUrlResponse ).start() ;
        }
        catch( android.os.NetworkOnMainThreadException ignored ) {
            Log.e( "loadUrlResponse: ", "NetworkOnMainThreadException" ) ;
        }
        catch( Exception ex ) {
            Log.e( "loadUrlResponse: ", ex.getMessage() ) ;
        }
    } ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        etAuthor  = findViewById( R.id.etAuthor ) ;
        etMessage = findViewById( R.id.etMessage ) ;
        chatContainer = findViewById( R.id.chatContainer ) ;

        handler = new Handler() ;

        findViewById( R.id.chatLayout ).setOnTouchListener( (v, event) -> {
            if( event.getAction() == MotionEvent.ACTION_UP ) {
                v.performClick() ;
            }
            else {
                hideSoftKeyboard() ;
            }
            return true ;
        } ) ;
        findViewById( R.id.buttonSend ).setOnClickListener( this::sendButtonClick ) ;

        handler.post( this::updateChat ) ;
    }

    private void updateChat() {
        chatUrl = getString( R.string.chat_url_get ) ;
        new Thread( loadUrlResponse ).start() ;
        handler.postDelayed( this::updateChat, 1000 ) ;
    }
    
    private void sendButtonClick( View v ) {
        String author = etAuthor.getText().toString() ;
        if( author.length() == 0 ) {
            Toast.makeText(this, R.string.chat_author_empty, Toast.LENGTH_SHORT).show();
            return ;
        }
        String message = etMessage.getText().toString() ;
        if( message.length() == 0 ) {
            Toast.makeText(this, R.string.chat_message_empty, Toast.LENGTH_SHORT).show();
            return ;
        }
        // message += new String(Character.toChars(0x1F349));
        chatUrl = getString(
                R.string.chat_url_send,
                author,
                message ) ;
        new Thread( loadUrlResponse ).start() ;
    }

    private void hideSoftKeyboard() {
        View focusedView = getCurrentFocus() ;
        if( focusedView != null )
        ( (InputMethodManager)
            getSystemService( INPUT_METHOD_SERVICE ) )
                .hideSoftInputFromWindow(
                        focusedView.getWindowToken(), 0 ) ;
    }

    private void showMessagesInScroll() {
        LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT ) ;
        layoutParams.setMargins( 5,5,5,5 ) ;

        LinearLayout.LayoutParams myLayoutParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT ) ;
        myLayoutParams.setMargins( 5,5,5,5 ) ;
        myLayoutParams.gravity = Gravity.END ;

        Drawable myBackground = AppCompatResources.getDrawable(
                getApplicationContext(),
                R.drawable.chat_msg_my ) ;
        Drawable otherBackground = AppCompatResources.getDrawable(
                getApplicationContext(),
                R.drawable.chat_msg_other ) ;

        // chatContainer.removeAllViews() ;  // clear
        for( ChatMessage message : messages ) {
            if( ! message.isDisplayed() ) {
                TextView txt = new TextView(this);
                txt.setTag( message ) ;
                txt.setText( message.toChatString()
                        .replaceAll( ":\\)", new String(Character.toChars(0x1F600))) ) ;
                txt.setPadding(5, 5, 5, 5);
                if (message.getAuthor().contentEquals(etAuthor.getText())) {
                    txt.setLayoutParams(myLayoutParams);
                    txt.setBackground(myBackground);
                } else {
                    txt.setLayoutParams(layoutParams);
                    txt.setBackground(otherBackground);
                }
                txt.setOnClickListener( this::messageClick ) ;
                txt.setOnLongClickListener( this::messageLongClick ) ;
                chatContainer.addView( txt ) ;
                message.setDisplayed( true ) ;
            }
        }

        new Thread( () ->
            runOnUiThread( () ->
                ((ScrollView)chatContainer.getParent()).fullScroll(
                        ScrollView.FOCUS_DOWN
                ) ) ).start() ;
    }

    private boolean messageLongClick( View v ) {
        chatContainer.removeView( v ) ;
        return true ;
    }

    private void messageClick( View v ) {
        ChatMessage msg = (ChatMessage) v.getTag() ;
        if( msg == null ) return ;
        TextView txt = (TextView) v ;
        // if( txt == null ) return ;

        txt.setText( msg.toFullChatString() ) ;
    }

    private boolean messagesContain( JSONObject obj ) throws JSONException {
        for( ChatMessage message : messages ) {
            if( message.getId() == obj.getInt( "id" ) ) {
                return true ;
            }
        }
        return false ;
    }
}
/*
Удаление сообщений: будем считать сообщение удаленным
 если его дата == "2000-01-01". Реализовать возможность
 удаления, отображения "Удалено" в чате (нашем)
 Ограничить возможность удаления только своих сообщений
 Выводить диалог подтверждение удаления

* Символы эмоций: в сообщение добавляются "коды" эмоций ":)"
 а при выводе они заменяются на Юникод-символы
 .replaceAll(
    ":\\)",  // с учетом экранирования ")" в регулярном выражении
    new String(Character.toChars(0x1F600))
  )
 */
/*
Задания:
Поменять порядок вывода ScrollView: последние сообщения снизу
При получении сообщений (после отправки) проверять:
 есть ли они уже в коллекции (messages) -
  вместо очистки и пересборки проверять на наличие и добавлять,
  если сообщение новое
 также поступать при отображении - если сообщение отображено,
  то пропускать его, не пересоздавать текст
* Сообщения, в которых автор-отправитель (свои сообщения)
 выравнивать по другому краю

 */

/*
    Д.З. По нажатию на кнопку "Отправить" сформировать запрос (строку)
    к API http://chat.momentfor.fun/?author=...&msg=...
    Использовать ресурс с плейсхолдерами
    Выводить строку в tvChat с новой строки при каждом нажатии кнопки
 */

/*
Старый проект                 Новый проект
- создаем активность          Создаем проект
   ChatActivity                 Chat

- добавляем в Портал            -----
   кнопку перехода на эту активность
   задаем обработчик со стартом активности

- манифест:
   указываем родительскую     Задаем разрешение для
    активность                 работы с Интернет

---------------------------------
http://chat.momentfor.fun/
API чата
{
status: 1,
data: [
    {
        id: "1444",
        author: "DNS",
        text: "Hello",
        moment: "2022-02-18 08:37:00"
    }, ... 20 messages ] }

http://chat.momentfor.fun/?author=DNS&msg=Всем привет
 - добавление нового сообщения
--------------------------------

Автор: [DNS]
Сообщение: [Всем привет]
[Отправить]
 */