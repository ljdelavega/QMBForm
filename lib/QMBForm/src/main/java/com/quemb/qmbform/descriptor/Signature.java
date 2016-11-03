package com.quemb.qmbform.descriptor;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ljdelavega on 16-08-15.
 */
public class Signature implements Externalizable {
    private static final Logger LOGGER = Logger.getLogger(Signature.class.getName());

    private JSONObject json;

    public Signature() {
        // used by serialization
    }

    public Signature(String name, String date, String base64Signature) {
        this(new JSONObject());
        try {
            this.json.put("_string", name);
            this.json.put("name", name);
            this.json.put("date", date);
            this.json.put("signature", base64Signature);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to create Signature", e);
        }
    }

    public Signature(JSONObject json) {
        this.json = json;
    }

    public JSONObject unwrap() {
        return this.json;
    }

    public String getDisplayText() {
        try {
            return (String) json.get("_string");
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, "Unable to access display text", e);
            return "";
        }
    }

    public String getName() {
        try {
            return (String) json.get("name");
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, "Unable to access name", e);
            return "";
        }
    }

    public String getDate() {
        try {
            return (String) json.get("date");
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, "Unable to access date", e);
            return null;
        }
    }

    public String getBase64Signature() {
        try {
            return (String) json.get("signature");
        } catch (JSONException e) {
            LOGGER.log(Level.SEVERE, "Unable to access signature", e);
            return "";
        }
    }

    @Override
    public void readExternal(ObjectInput input) throws IOException, ClassNotFoundException {
        final String string = (String) input.readObject();
        if (string != null) {
            try {
                json = new JSONObject(string);
            } catch (JSONException e) {
                throw new IOException(e);
            }
        }
    }

    @Override
    public void writeExternal(ObjectOutput output) throws IOException {
        if (this.json != null) {
            output.writeObject(this.json.toString());
        }
    }
}