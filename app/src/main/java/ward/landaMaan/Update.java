package ward.landaMaan;

import org.jsoup.Jsoup;

import java.io.Serializable;

import utils.Utilities;

public class Update implements Serializable, Comparable<Update> {
    /**
     *
     */
    private static final long serialVersionUID = -4704709704844438730L;
    private String text;
    private String html_text;
    private String update_id;
    private boolean active;
    private String subject;
    private String dateTime;
    private String url;
    private String urlToJason;
    private boolean pinned;
    private boolean popUpOpend;

    public
    Update(String subject, String dateTime, String text) {

        setSubject(subject);
        this.dateTime = dateTime;
        this.text = text;
    }

    public Update(String id, String urlToJSon) {
        this.update_id = id;
        this.setUrlToJason(urlToJSon);
    }

    public Update(String id, String subject, String dateTime, String text,
                  String html_text) {
        this.update_id=id;
        setSubject(subject);
        this.dateTime=dateTime;
        this.text=text;
        this.html_text = html_text;
        this.pinned = false;
       this.popUpOpend=false;

    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof Update) {
            Update t = (Update) o;
            return t.update_id.equals(update_id);
        }
        return false;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        // String tmp = Utilities.removeTableFirstTrHtml(text);
        //tmp = Utilities.html2Text(tmp == null ? text : tmp);
        this.text = text;
        //Utilities.html2Text(text);

        // Html.fromHtml(text).toString();
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {

        String mamn = Jsoup.parse(subject).text();
        this.subject = Utilities.replacer(new StringBuffer(mamn));

    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUpdate_id() {
        return update_id;
    }

    void setUpdate_id(String update_id) {
        this.update_id = update_id;
    }

    public String getUrlToJason() {
        return urlToJason;
    }

    void setUrlToJason(String urlToJason) {
        this.urlToJason = urlToJason;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    @Override
    public int compareTo(Update another) {
        if (isPinned() && !another.isPinned()) {
            return -1;
        } else if (!isPinned() && another.isPinned()) {
            return 1;
        } else
            return another.getDateTime().compareTo(dateTime);
    }

    public boolean isPopUpOpend() {
        return popUpOpend;
    }

    public void setPopUpOpend(boolean popUpOpend) {
        this.popUpOpend = popUpOpend;
    }

    public String getHtml_text() {
        return html_text;
    }

    public void setHtml_text(String html_text) {
        this.html_text = html_text;
    }
}
