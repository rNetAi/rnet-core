package io.github.rNetAi.rnetCore.rNetProtocol.objWrapper;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RNetProtocolHttpRequestWrapper extends HttpServletRequestWrapper {

    private final byte[] newBody;
    private final Map<String, String> customHeaders = new HashMap<>();
    private final String method;

    public RNetProtocolHttpRequestWrapper(HttpServletRequest request,
                                          String newBody,
                                          String method) {
        super(request);
        this.newBody = newBody.getBytes(StandardCharsets.UTF_8);
        this.method = method;
    }

    // 🔹 Override HTTP Method
    @Override
    public String getMethod() {
        return method;
    }

    // 🔹 Override Content-Type
    public void setHeader(String name, String value) {
        customHeaders.put(name.toLowerCase(), value);
    }

    @Override
    public String getHeader(String name) {
        String value = customHeaders.get(name.toLowerCase());
        return value != null ? value : super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        String value = customHeaders.get(name.toLowerCase());
        if (value != null) {
            return Collections.enumeration(List.of(value));
        }
        return super.getHeaders(name);
    }

    @Override
    public String getContentType() {
        String value = customHeaders.get("content-type");
        return value != null ? value : super.getContentType();
    }

    // 🔹 Override Body
    @Override
    public ServletInputStream getInputStream() {
        ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(newBody);

        return new ServletInputStream() {

            @Override
            public boolean isFinished() {
                return byteArrayInputStream.available() == 0;
            }

            @Override
            public boolean isReady() {
                return true;
            }

            @Override
            public void setReadListener(ReadListener readListener) {}

            @Override
            public int read() {
                return byteArrayInputStream.read();
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }
}
