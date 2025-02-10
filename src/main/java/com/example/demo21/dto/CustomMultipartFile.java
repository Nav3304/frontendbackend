package com.example.demo21.dto;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class CustomMultipartFile implements MultipartFile {

    private final ByteArrayInputStream byteArrayInputStream;
    private final String name;
    private final int size;

    public CustomMultipartFile(ByteArrayInputStream byteArrayInputStream, String name, int size) {
        this.byteArrayInputStream = byteArrayInputStream;
        this.name = name;
        this.size = size;
    }

    @Override
    public String getName () {
        return name;
    }

    @Override
    public String getOriginalFilename () {
        return name;
    }

    @Override
    public String getContentType () {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    @Override
    public boolean isEmpty () {
        return size==0;
    }

    @Override
    public long getSize () {
        return size;
    }

    @Override
    public byte[] getBytes () throws IOException {
        return byteArrayInputStream.readAllBytes();
    }

    @Override
    public InputStream getInputStream () throws IOException {
        return byteArrayInputStream;
    }

    @Override
    public void transferTo (File dest) throws IOException, IllegalStateException {

    }
}
