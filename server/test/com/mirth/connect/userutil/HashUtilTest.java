package com.mirth.connect.userutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.mirth.connect.model.ServerEvent;
import com.mirth.connect.server.userutil.HashUtil;

public class HashUtilTest {
    private String expectedHashValue = "810ff2fb242a5dee4220f2cb0e6a519891fb67f2f828a6cab4ef8894633b1f50" ;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void generateHashForString() throws Exception {

        String data = "testdata";
        String hashValue = HashUtil.generate(data);
        assertNotNull(hashValue);
        assertEquals(hashValue,expectedHashValue);
    }

    @Test
    public void generateHashForEmptyString() throws Exception {

        String data = "";
        String hashValue = HashUtil.generate(data);
        assertNotNull(hashValue);
        assertEquals(hashValue, "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }

    @Test
    public void generateHashForByte() throws Exception {

        String data = "testdata";
        String hashValue = HashUtil.generate(data.getBytes());
        assertNotNull(hashValue);
        assertEquals(hashValue, expectedHashValue);

    }

    @Test
    public void generateHashForByteAndAlgo() throws Exception {

        String data = "testdata";
        String hashValue = HashUtil.generate(data.getBytes(), "SHA-256");
        assertNotNull(hashValue);
        assertEquals(hashValue, expectedHashValue);
    }

    @Test
    public void generateHashBadAlgo() throws Exception {

        String data = "testdata";
        thrown.expect(NoSuchAlgorithmException.class);
        HashUtil.generate(data.getBytes(), "SHA256");

    }

    @Test
    public void generateHashForEncodeStringAndAlgo() throws Exception {

        String data = "testdata";
        String encoding = "UTF-8";
        String hashValue = HashUtil.generate(data, encoding, "SHA-256");
        assertNotNull(hashValue);
        assertEquals(hashValue, expectedHashValue);

    }

    @Test
    public void generateHashForEncodeStringAndBadAlgo() throws Exception {

        String data = "testdata";
        String encoding = "UTF-8";
        thrown.expect(NoSuchAlgorithmException.class);
        HashUtil.generate(data, encoding, "SHA256");
    }

    @Test
    public void generateHashForBadEncodeStringAndAlgo() throws Exception {

        String data = "testdata";
        String encoding = "UTF";
        thrown.expect(UnsupportedEncodingException.class);
        HashUtil.generate(data, encoding, "SHA-256");
        
    }

}
