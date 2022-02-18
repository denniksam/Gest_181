package step.android.gest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;

public class PortalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_portal);

        Button game15Button = findViewById( R.id.game15_button ) ;
        if( game15Button == null ) {
            Log.e( "PortalActivity onCreate", "game15_button no found" ) ;
            return ;
        }
        game15Button.setOnClickListener( v -> startGame15() ) ;

        findViewById( R.id.inet_button ).setOnClickListener( v -> {
            startActivity(
                    new Intent(
                            PortalActivity.this,
                            CurrencyActivity.class ) ) ;
        } ) ;

        findViewById( R.id.chat_button ).setOnClickListener( v -> {
            startActivity(
                    new Intent(
                            PortalActivity.this,
                            ChatActivity.class ) ) ;
        } ) ;
    }

    private void startGame15() {
        Intent game15Intent = new Intent(
                PortalActivity.this,
                MainActivity.class ) ;

        startActivity( game15Intent ) ;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater() ;
        menuInflater.inflate( R.menu.portal_menu, menu ) ;
        return true ;
    }

    @Override
    public boolean onOptionsItemSelected( @NonNull MenuItem item ) {
        int id = item.getItemId() ;
        if( id == R.id.item_exit ) {
            finish() ;
        }
        else if( id == R.id.item_game15 ) {
            startGame15() ;
        }
        else
            return super.onOptionsItemSelected( item ) ;
        return true ;
    }
}
/*
Реализовать меню в "пятнашках":
    начать заново,
    Выбрать сложность
      Высокая
      Низкая
    закончить
 */