package com.mirth.connect.donkey.server;

public interface Encryptor {
    public String encrypt(String text);
    public String decrypt(String text);
}
