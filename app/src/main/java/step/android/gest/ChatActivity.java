package step.android.gest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class ChatActivity extends AppCompatActivity {

    private TextView tvChat ;
    private EditText etAuthor ;
    private EditText etMessage ;

    // Data Context
    private ArrayList<ChatMessage> messages ;

    // URL response buffer
    private String urlResponse ;

    // URL response loader
    private final Runnable loadUrlResponse = () -> {
        try( InputStream stream =
                 new URL( "http://chat.momentfor.fun/" )
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
            runOnUiThread( () -> { tvChat.setText( urlResponse ) ; } ) ;
        }
        catch( Exception ex ) {
            Log.e( "loadUrlResponse: ", ex.getMessage() ) ;
        }
    } ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        tvChat    = findViewById( R.id.tvChat ) ;
        etAuthor  = findViewById( R.id.etAuthor ) ;
        etMessage = findViewById( R.id.etMessage ) ;

        findViewById( R.id.chatLayout ).setOnTouchListener( (v, event) -> {
            if( event.getAction() == MotionEvent.ACTION_UP ) {
                v.performClick() ;
            }
            else {
                hideSoftKeyboard() ;
            }
            return true ;
        } ) ;

        new Thread( loadUrlResponse ).start() ;
    }

    private void hideSoftKeyboard() {
        ( (InputMethodManager)
            getSystemService( INPUT_METHOD_SERVICE ) )
                .hideSoftInputFromWindow(
                        getCurrentFocus().getWindowToken(), 0 ) ;
    }
}
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