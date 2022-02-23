package step.android.gest;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ChatMessage
        implements Comparable<ChatMessage> {

    // ORM fields
    private int    id;
    private String author;
    private String text;
    private Date   moment;

    // Inner fields
    private boolean isDisplayed ;


    private final static SimpleDateFormat
        dtParser = new SimpleDateFormat(
                "yyyy-MM-dd hh:mm:ss", Locale.UK ) ;

    public ChatMessage(JSONObject obj)
            throws JSONException, ParseException {
        setId( obj.getInt( "id" ) ) ;
        setAuthor( obj.getString( "author" ) ) ;
        setText( obj.getString( "text" ) ) ;
        setMoment( obj.getString( "moment" ) ) ;
        setDisplayed( false ) ;
    }

    @Override
    public int compareTo( ChatMessage other ) {
        return this.moment.compareTo( other.getMoment() ) ;
    }

    @Override
    public String toString() {
        return "{" +
                "id=" + id +
                ", author='" + author + '\'' +
                ", text='" + text + '\'' +
                ", moment=" + moment +
                '}';
    }

    public String toChatString() {
        String txt = getText() ;
        if( txt.length() > 20 )
            txt = txt.substring( 0, 20 ) + "..." ;

        return dtParser.format( moment )
                + " " + getAuthor()
                + ": " + txt ;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getMoment() {
        return moment;
    }

    public void setMoment(Date moment) {
        this.moment = moment;
    }

    public void setMoment(String moment) throws ParseException {
        this.setMoment( dtParser.parse( moment ) ) ;
    }

    public boolean isDisplayed() {
        return isDisplayed;
    }

    public void setDisplayed(boolean displayed) {
        isDisplayed = displayed;
    }
}
