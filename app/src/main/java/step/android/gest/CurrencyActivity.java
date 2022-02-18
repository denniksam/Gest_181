package step.android.gest;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class CurrencyActivity extends AppCompatActivity {

    private String bankUrl ;

    private TextView tvContent ;
    private String contentBuffer ;
    private final Runnable displayContent = () -> tvContent.setText( contentBuffer );

    private ArrayList<Rate> rates ;
    private final String[] ccs = new String[] { "USD", "EUR" } ;

    private final Runnable selectCurrencies = () -> {
        StringBuilder sb = new StringBuilder() ;
        for( Rate rate : rates ) {
            for( String cc : ccs ) {
                if( rate.getCc().equals( cc ) ) {
                    sb.append( rate ) ;
                    sb.append( '\n' ) ;
                }
            }
        }
        contentBuffer = sb.toString() ;
        runOnUiThread( displayContent ) ;
    } ;

    private final Runnable parseContent = () -> {
        try {
            JSONArray currencyArray = new JSONArray( contentBuffer ) ;
            rates = new ArrayList<>() ;
            for( int i = 0; i < currencyArray.length(); ++i ) {
                JSONObject rate = currencyArray.getJSONObject( i ) ;
                rates.add( new Rate(
                        rate.getInt( "r030" ),
                        rate.getString( "txt" ),
                        rate.getDouble( "rate" ),
                        rate.getString( "cc" ),
                        rate.getString( "exchangedate" )
                ) ) ;
            }
            new Thread( selectCurrencies ).start() ;
        }
        catch( Exception ex ) {
            Log.e( "Open URL: ", ex.getMessage() ) ;
            runOnUiThread( () -> Toast.makeText(CurrencyActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show() ) ;
        }
    } ;

    private final Runnable openUrl = () -> {
        try( InputStream stream = new URL( bankUrl ).openStream() ) {
            // Read from stream
            StringBuilder sb = new StringBuilder() ;
            int sym ;
            while( ( sym = stream.read() ) != -1 ) {
                sb.append( (char) sym ) ;
            }
            // contentBuffer = sb.toString() ;
            contentBuffer = new String(
                    sb.toString().getBytes( StandardCharsets.ISO_8859_1 ),
                    StandardCharsets.UTF_8
            ) ;
            // runOnUiThread( displayContent ) ;
            new Thread( parseContent ).start() ;
        }
        catch( Exception ex ) {
            Log.e( "Open URL: ", ex.getMessage() ) ;
            runOnUiThread( () -> Toast.makeText(CurrencyActivity.this, ex.getMessage(), Toast.LENGTH_SHORT).show() ) ;
        }
    } ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency);

        tvContent = findViewById( R.id.tvContent ) ;
        tvContent.setMovementMethod( new ScrollingMovementMethod() ) ;

        bankUrl = getString( R.string.bank_url, "20220216" ) ;
        new Thread( openUrl ).start() ;

        findViewById( R.id.btnDate ).setOnClickListener( v -> {
            DatePicker dp = findViewById( R.id.datePicker ) ;
            int m = dp.getMonth() + 1 ;
            int d = dp.getDayOfMonth() ;
            String comboDate = "" + dp.getYear()
                    + ( m < 10 ? "0" : "" ) + m
                    + ( d < 10 ? "0" : "" ) + d ;
            bankUrl = getString( R.string.bank_url, comboDate ) ;
            new Thread( openUrl ).start() ;
        } ) ;
    }
}
/*
Работа с Интернет
Приложению необходимо разрешение, заявленное в манифесте
<uses-permission android:name="android.permission.INTERNET"/>
если начальное приложение было установлено без разрешения, возможно,
 его придется переустановить (удалить из устройства и поставить заново)

Для связи с ресурсом используется класс URL
Особенности:
 создание объекта URL не открывает соединения (аналог - FILE)
 для открытия соединения используется .openStream() и рекомендуется
  блок с автозакрытием try(){}
 открытие соединений не разрешено в UI потоке, необходимо создавать новый
 обращение к UI элементам разрешено только из UI потока, для делегирования
   к UI потоку предусмотрен метод runOnUiThread( Runnable )
 по стандарту, Интернет данные передаются в кодировке ISO 8859.1
  однако, большинство JSON ресурсов используют UTF-8
  для перекодирования используется конструктор String( byte[], Charset )

------------
О скроллинге:
для реализации прокрутки необходимо
а) в разметке указать направления прокрутки
 android:scrollbars="vertical"
б) в коде установить действие при перетягивании в режим "прокрутка"
 tvContent.setMovementMethod( new ScrollingMovementMethod() ) ;

------------
Д.З. Обеспечить (после загрузки контента URL) его парсинг
 и добавить отображение количества загруженных "курсов" -
 размер коллекции rates
 В момент старта активности на текстовом поле число - кол-во курсов
 */
/*
Задание: отобразить курсы наиболее популярных валют: Евро и Доллар
- в начале работы приложения определить сегодняшнюю дату, подставить в bankUrl
- при отображении курса указать дату:
   16.02.2022
   Доллар - ...
   Евро - ....
- реализовать конструктор Rate, принимающий JSONObject; перенести разбор полей в него
- если по запросу возвращается пустой ответ (дата будущая), выводить соотв. сообщение:
   16.02.3022
   На эту дату курсов нет

 */