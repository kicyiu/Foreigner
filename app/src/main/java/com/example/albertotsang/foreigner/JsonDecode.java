package com.example.albertotsang.foreigner;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by AlbertoTsang on 12/28/15.
 */
public class JsonDecode {

    public List readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readUsersArray(reader);
        }
        finally{
            reader.close();
        }

    }

    //Read first Array from Json
    public List readUsersArray(JsonReader reader) throws IOException {
        List users = new ArrayList();

        reader.beginArray();
        while (reader.hasNext()) {
            users.add(readUser(reader));
        }
        reader.endArray();
        return users;
    }

    //Read Object inside Array from Json in order to decode user data
    public User readUser(JsonReader reader) throws IOException {
        String idUser = null;
        String name = null;
        String about = null;
        String gender = null;
        Timestamp dob = null;
        String education = null;
        String work = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String item = reader.nextName();
            if (item.equals("iduser")) {
                idUser = reader.nextString();
            } else if (item.equals("name")) {
                name = reader.nextString();
            } else if (item.equals("about") && reader.peek() != JsonToken.NULL) {
                about = reader.nextString();
            } else if (item.equals("gender")) {
                gender = reader.nextString();
            } else if (item.equals("dob") && reader.peek() != JsonToken.NULL) {
                dob = convertStringTimeStamp(reader.nextString());
            } else if (item.equals("education") && reader.peek() != JsonToken.NULL) {
                education = reader.nextString();
            } else if (item.equals("work") && reader.peek() != JsonToken.NULL) {
                work = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new User(idUser, name, about, gender, dob, education, work);
    }

    public Timestamp convertStringTimeStamp(String newDate) {

        try {
            DateFormat formatter;
            Date date;
            formatter = new SimpleDateFormat("yyyy-MM-dd");
            date = (Date)formatter.parse(newDate);
            java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
            return timeStampDate;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
