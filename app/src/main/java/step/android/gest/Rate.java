package step.android.gest;

import androidx.annotation.NonNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/*  ORM for currency rate :
r030: 191,
txt: "Куна",
rate: 4.2934,
cc: "HRK",
exchangedate: "15.02.2022"
 */
public class Rate {
    private int    r030 ;
    private String txt ;
    private double rate ;
    private String cc ;
    private Date   exchangeDate ;

    private static final SimpleDateFormat parser = new SimpleDateFormat("dd.MM.yyyy",  Locale.UK ) ;

    public Rate(int r030, String txt, double rate, String cc, String exchangeDate)
            throws ParseException {
        this.setR030( r030 ) ;
        this.setTxt( txt ) ;
        this.setRate( rate ) ;
        this.setCc( cc ) ;
        this.setExchangeDate( exchangeDate ) ;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format( Locale.getDefault(), "%s %f", this.txt, this.rate ) ;
    }

    public int getR030() {
        return r030;
    }

    public void setR030(int r030) {
        this.r030 = r030;
    }

    public String getTxt() {
        return txt;
    }

    public void setTxt(String txt) {
        this.txt = txt;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public String getCc() {
        return cc;
    }

    public void setCc(String cc) {
        this.cc = cc;
    }

    public Date getExchangeDate() {
        return exchangeDate;
    }

    public void setExchangeDate(Date exchangeDate) {
        this.exchangeDate = exchangeDate;
    }

    public void setExchangeDate(String dateString) throws ParseException {
        this.exchangeDate = parser.parse( dateString ) ;
    }
}
