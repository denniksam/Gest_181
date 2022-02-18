package step.android.gest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    Random random;

    public MainActivity() {
        random = new Random() ;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.fieldLayout).setOnTouchListener(
                new OnSwipeTouchListener(getApplicationContext()) {
                    @Override
                    public void onSwipeRight() {
                        userMove( MoveDirection.RIGHT ) ;
                    }

                    @Override
                    public void onSwipeLeft() {
                        userMove( MoveDirection.LEFT ) ;
                    }

                    @Override
                    public void onSwipeTop() {
                        userMove( MoveDirection.TOP ) ;
                    }

                    @Override
                    public void onSwipeBottom() {
                        userMove( MoveDirection.BOTTOM ) ;
                    }
                }
        );
        if( savedInstanceState == null ) shuffle( 3 ) ;
    }

    @Override
    public void onSaveInstanceState( @NonNull Bundle outState ) {
        super.onSaveInstanceState( outState ) ;
        // current field state:
        CharSequence[] cellText = new CharSequence[ 16 ] ;
        for( int i = 0; i < 16; ++i ) {
            cellText[i] = getCellByIndex( i ).getText() ;
        }
        outState.putCharSequenceArray( "fieldState", cellText ) ;
    }

    @Override
    protected void onRestoreInstanceState( @NonNull Bundle savedInstanceState ) {
        super.onRestoreInstanceState( savedInstanceState ) ;
        CharSequence[] cellText = savedInstanceState.getCharSequenceArray( "fieldState" ) ;
        for( int i = 0; i < 16; ++i ) {
            TextView cell = getCellByIndex( i ) ;
            cell.setText( cellText[ i ] ) ;
            if( cellText[ i ].equals( "" ) )
                cell.setBackground(
                        AppCompatResources.getDrawable(
                                getApplicationContext(), R.drawable.cell_0_shape ) ) ;
            else
                cell.setBackground(
                        AppCompatResources.getDrawable(
                                getApplicationContext(), R.drawable.cell_shape ) ) ;
        }
    }

    /**
     * Move after swipe and game over checking
     * @param direction swipe direction
     */
    private void userMove( MoveDirection direction ) {
        if( moveCell( direction ) ) {
            if( isGameOver() ) {
                // Alert Dialog
                new AlertDialog.Builder( MainActivity.this )
                        .setTitle( "Game finished" )
                        .setMessage( "Congratulations! Play again?" )
                        .setIcon( android.R.drawable.ic_dialog_dialer )
                        .setPositiveButton("Yes", (dialog, which) -> {
                            shuffle( 3 ) ;
                        })
                        .setNegativeButton( "No", (dialog, which) -> {
                            finish() ;
                        })
                        .setNeutralButton( "Rnd", (dialog, which) -> {
                            if( random.nextBoolean() ) {
                                shuffle( 3 ) ;
                            }
                            else finish() ;
                        })
                        .show() ;
            }
        }
        else {  // Если ход невозможен, то выводится Toast
            Toast.makeText(
                    MainActivity.this,
                    R.string.invalid_move,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Replaces cells in field
     * @param n random moves count
     */
    private void shuffle( int n ) {
        int cnt = 0 ;
        MoveDirection[] moveDirectionValues = MoveDirection.values() ;
        while( cnt < n ) {
            if( moveCell(
                    moveDirectionValues[
                            random.nextInt(moveDirectionValues.length) ] ) ) {
                ++cnt;
            }
        }
    }

    private boolean moveCell( MoveDirection direction ) {
        int emptyCellIndex = getEmptyCellIndex() ;
        // Log.d( "Gest:onSwipeRight", emptyCellIndex + "" ) ;
        int otherCellIndex = -1 ;
        switch( direction ) {
            case BOTTOM:
                if( emptyCellIndex > 0 && emptyCellIndex < 5 ) {   // 1,2,3,4 : ( 0 < n < 5 )
                    return false ;
                }
                otherCellIndex = emptyCellIndex == 0 ? 12 : emptyCellIndex - 4 ;
                break;
            case LEFT:
                if( emptyCellIndex % 4 == 0 ) {   // 4,8,12,0 : ( n % 4 == 0 )
                    return false ;
                }
                otherCellIndex = emptyCellIndex == 15 ? 0 : emptyCellIndex + 1 ;
                break;
            case RIGHT:
                // Если пустая ячейка в левом столбце, то свайп вправо игнорируется
                if( emptyCellIndex % 4 == 1 ) {   // 1,5,9,13 : ( n % 4 == 1 )
                    return false ;
                }
                otherCellIndex = emptyCellIndex == 0 ? 15 : emptyCellIndex - 1 ;
                break;
            case TOP:
                if( emptyCellIndex > 12 || emptyCellIndex == 0 ) {   // 13,14,15,0
                    return false ;
                }
                otherCellIndex = emptyCellIndex == 12 ? 0 : emptyCellIndex + 4 ;
                break;
        }
        if( otherCellIndex == -1 ) return false ;
        SwapCells( otherCellIndex, emptyCellIndex ) ;
        return true ;
    }

    private void SwapCells( int index1, int index2 ) {
        TextView cell  = getCellByIndex( index1 ) ;
        TextView cell0 = getCellByIndex( index2 ) ;

        Drawable bg  = cell.getBackground();
        Drawable bg0 = cell0.getBackground();
        cell.setBackground(bg0);
        cell0.setBackground(bg);

        CharSequence txt  = cell.getText();
        CharSequence txt0 = cell0.getText();
        cell.setText(txt0);
        cell0.setText(txt);
    }

    private TextView getCellByIndex( int index ) {
        return findViewById(
                getResources().getIdentifier(
                        "cell_" + index,
                        "id",
                        getPackageName()
                ) ) ;
    }

    private int getEmptyCellIndex() {
        for( int i = 0; i < 16; ++i ) {
            TextView cell = getCellByIndex( i ) ;
            if( cell.getText().equals("") ) return i ;
        }
        return -1 ;
    }

    private boolean isGameOver() {
        for( int i = 1; i < 16; i++ ) {
            if( ! getCellByIndex(i).getText().equals( "" + i ) ) {
                return false ;
            }
        }
        return true ;
    }

    enum MoveDirection {
        BOTTOM,
        LEFT,
        RIGHT,
        TOP
    }
}

/*
Manifest
Файл манифеста соединяет в себе "декларации", касающиеся всего приложения:
 - активности и их связи
 - переключение / фиксация ориентации экрана
 - требуемые разрешения / доступ к модулям устройства (Интернет, Контакты, камера)
----
Активности
добавляем через меню
в манифесте указываем атрибуты (опционально)
для перехода на новую активность
 а) если хотим сделать главной - меняем в манифесте <intent-filter>
 б) если просто запустить (по нажатию кнопки) - создаем Intent, указывая
    класс новой активности и вызываем startActivity( intent )
для указания отношений между активностями в манифесте записываем
 android:parentActivityName="..." в атрибутах соответствующей активности
 в таком случае на панели приложения будет автоматически формироваться "<-",
 возвращающая на родительскую активность
Жизненный цикл активности предусматривает пересоздание (вызов onCreate) при
 изменениях ориентации устройства / размеров приложения
 определить первая ли это сборка или реакция на изменения можно при помощи
  параметра, передаваемого в onCreate - savedInstanceState :
  null - первая сборка
  !null - не первая
Средствами манифеста можно зафиксировать ориентацию активности, указав
 android:screenOrientation="portrait"

----
единицы размеров
dp - density pixel - привязан к физическому размеру, разное кол-во реалных пикселей
     для разной плотности экрана
sp - scalable pixel - с действием масштаба (в основном для шрифтов)
 */
/*
Задание: добавить "переключатель" сложности: простой/сложный
в зависимости от него использовать разное кол-во перемешивания (ходов)
Добавить счетчик ходов, сделанных игроком / в финальное сообщение включить эту инфо
* "подсвечивать" правильно собранные ряды / столбцы
** реализовать вариант со сбором слова из 15 букв
 */