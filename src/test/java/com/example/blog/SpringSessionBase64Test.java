package com.example.blog;

import org.junit.jupiter.api.Test;

import java.util.Base64;

public class SpringSessionBase64Test {

    @Test
    public void test() {
        var uuid = "e38dc9ed-691c-4827-a6dd-8e2762cc001f";
        var base64Bytes = Base64.getEncoder().encode(uuid.getBytes());
        var base64String = new String(base64Bytes);
        System.out.println(base64String);
        // => ZTM4ZGM5ZWQtNjkxYy00ODI3LWE2ZGQtOGUyNzYyY2MwMDFm
        // => ZTM4ZGM5ZWQtNjkxYy00ODI3LWE2ZGQtOGUyNzYyY2MwMDFm

    }
}
