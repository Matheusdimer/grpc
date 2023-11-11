package com.unesc.leilao.util;

import com.unesc.leilao.proto.APIResponse;

public class Response {

    public static APIResponse ok(String message) {
        return APIResponse.newBuilder()
                .setOk(true)
                .setMessage(message)
                .build();
    }

    public static APIResponse notOk(String message) {
        return APIResponse.newBuilder()
                .setOk(false)
                .setMessage(message)
                .build();
    }
}
