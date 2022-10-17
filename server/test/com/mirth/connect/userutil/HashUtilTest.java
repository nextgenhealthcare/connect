package com.mirth.connect.userutil;

import static org.junit.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.server.userutil.HashUtil;

public class HashUtilTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void generateHashForString() throws Exception {

        String data = "testdata";
        String hashValue = HashUtil.generate(data);
        assertNotNull(hashValue);
    }

    @Test
    public void generateHashForEmptyString() throws Exception {

        String data = "";
        String hashValue = HashUtil.generate(data);
        assertNotNull(hashValue);
    }

    @Test
    public void generateHashForByte() throws Exception {

        String data = "testdata for byte array";
        String hashValue = HashUtil.generate(data.getBytes());
        assertNotNull(hashValue);
    }

    @Test
    public void generateHashForByteAndAlgo() throws Exception {

        String data = "testdata for byte array";
        String hashValue = HashUtil.generate(data.getBytes(), "SHA-256");
        assertNotNull(hashValue);
    }

    @Test
    public void generateHashBadAlgo() throws Exception {

        String data = "testdata for byte array";
        thrown.expect(NoSuchAlgorithmException.class);
        HashUtil.generate(data.getBytes(), "SHA256");

    }

    @Test
    public void generateHashForEncodeStringAndAlgo() throws Exception {

        String data = "testdata for byte array";
        String encoding = "UTF-8";
        String hashValue = HashUtil.generate(data, encoding, "SHA-256");
        assertNotNull(hashValue);

    }

    @Test
    public void generateHashForEncodeStringAndBadAlgo() throws Exception {

        String data = "testdata for byte array";
        String encoding = "UTF-8";
        thrown.expect(NoSuchAlgorithmException.class);
        HashUtil.generate(data, encoding, "SHA256");
    }

    @Test
    public void generateHashForBadEncodeStringAndAlgo() throws Exception {

        String data = "testdata for byte array";
        String encoding = "UTF";
        thrown.expect(UnsupportedEncodingException.class);
        HashUtil.generate(data, encoding, "SHA-256");
    }
}
