package com.smilhone.doordashdemo.transport.serialization;

import com.google.gson.annotations.SerializedName;
/**
 * Created by smilhone on 12/6/2017.
 */

public class TokenRequestBody {
    @SerializedName("email")
    public String Email;

    @SerializedName("password")
    public String Password;
}
