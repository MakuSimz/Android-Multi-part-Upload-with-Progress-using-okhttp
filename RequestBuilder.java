package com.okhttp.root.myapplication;

import android.webkit.MimeTypeMap;

import java.io.File;

import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * Created by root on 6/16/17.
 */

public class RequestBuilder {

    //Login request body
    public static RequestBody LoginBody(String username, String password, String token) {
        return new FormBody.Builder()
                .add("action", "login")
                .add("format", "json")
                .add("username", username)
                .add("password", password)
                .add("logintoken", token)
                .build();
    }

    public static HttpUrl buildURL() {
        return new HttpUrl.Builder()
                .scheme("http") //http
                .host("41.63.53.35")
                .addPathSegment("hugefile/save_file.php")//adds "/pathSegment" at the end of hostname
                .addQueryParameter("param1", "value1") //add query parameters to the URL
                .addEncodedQueryParameter("encodedName", "encodedValue")//add encoded query parameters to the URL
                .build();
        /**
         * The return URL:
         *  https://www.somehostname.com/pathSegment?param1=value1&encodedName=encodedValue
         */
    }
    public static MultipartBody uploadRequestBody(String title, String imageFormat, String token, File file) {
        String content_type = getMimeType(file.getPath());
        String file_path = file.getAbsolutePath();
        RequestBody file_body = RequestBody.create(MediaType.parse(content_type), file);
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("type", content_type)
                .addFormDataPart("uploaded_file", file_path.substring(file_path.lastIndexOf("/") + 1), file_body).build();
    }
    private static String getMimeType(String path) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

    }
}
