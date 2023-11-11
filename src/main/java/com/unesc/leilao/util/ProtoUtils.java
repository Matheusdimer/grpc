package com.unesc.leilao.util;

import java.time.LocalDateTime;

public class ProtoUtils {

    private ProtoUtils() {}

    public static LocalDateTime toDateTime(String datetime) {
        return LocalDateTime.parse(datetime);
    }
}
