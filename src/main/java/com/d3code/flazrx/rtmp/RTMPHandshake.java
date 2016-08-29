package com.d3code.flazrx.rtmp;

import com.d3code.flazrx.rtmp.client.ClientOptions;
import com.d3code.flazrx.util.Utils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.omg.PortableInterceptor.INACTIVE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Nottyjay on 2016/8/29 0029.
 */
public class RTMPHandshake {

    private static final Logger LOG = LoggerFactory.getLogger(RTMPHandshake.class);

    public static final int HANDSHAKE_SIZE = 1536;

    /* SHA 256 digest length */
    private static final int DIGEST_SIZE= 32;
    private static final int PUBLIC_KEY_SIZE = 128;

    private static final byte[] SERVER_CONST = "Genuine Adobe Flash Media Server 001".getBytes();

    public static final byte[] CLIENT_CONST = "Genuine Adobe Flash Player 001".getBytes();

    private static final byte[] RANDOM_CRUD = Utils.fromHex("F0EEC24A8068BEE82E00D0D1029E7E576EEC5D2D29806FAB93B8E636CFEB31AE");

    private static final byte[] SERVER_CONST_CRUD = concat(SERVER_CONST, RANDOM_CRUD);

    private static final byte[] CLIENT_CONST_CRUD = concat(CLIENT_CONST, RANDOM_CRUD);

    private static final byte[] DH_MODULUS_BYTES = Utils.fromHex(
            "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B80DC1CD129024E088A67CC74"
                    + "020BBEA63B139B22514A08798E3404DDEF9519B3CD3A431B302B0A6DF25F1437"
                    + "4FE1356D6D51C245E485B576625E7EC6F44C42E9A637ED6B0BFF5CB6F406B7ED"
                    + "EE386BFB5A899FA5AE9F24117C4B1FE649286651ECE65381FFFFFFFFFFFFFFFF"
    );

    private static final BigInteger DH_MODULUS = new BigInteger(1, DH_MODULUS_BYTES);

    private static final BigInteger DH_BASE = BigInteger.valueOf(2);

    private KeyAgreement keyAgreement;
    private byte[] ownPublicKey;
    private byte[] peerPublicKey;
    private byte[] ownPartOneDigest;
    private byte[] peerPartOneDigest;
    private Cipher cipherIn;
    private Cipher cipherOut;
    private byte[] peerTime;

    private boolean rtmpe;
    private int validationType;

    private byte[] swfHash;
    private int swfSize;
    private byte[] swfvBytes;

    private ByteBuf peerPartOne;
    private ByteBuf ownPartOne;

    private static byte[] concat(byte[] a, byte[] b){
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    private static int calculateOffset(ByteBuf in, int pointerIndex, int modulus, int increment){
        byte[] pointer = new byte[4];
        in.getBytes(pointerIndex, pointer);
        int offset = 0;
        // sum the 4 bytes of the pointer
        for(int i = 0; i < pointer.length; i++){
            offset += pointer[i] & 0xff;
        }
        offset %= modulus;
        offset += increment;
        return offset;
    }

    private static byte[] digestHandshake(ByteBuf in, int digestOffset, byte[] key){
        final byte[] message = new byte[HANDSHAKE_SIZE - DIGEST_SIZE];
        in.getBytes(0, message, 0, digestOffset);
        final int afterDigestOffset = digestOffset + DIGEST_SIZE;
        in.getBytes(afterDigestOffset, message, digestOffset, HANDSHAKE_SIZE - afterDigestOffset);
        return Utils.sha256(message, key);
    }

    private static ByteBuf generateRandomHandshake(){
        byte[] randomBytes = new byte[HANDSHAKE_SIZE];
        Random random = new Random();
        random.nextBytes(randomBytes);
        return Unpooled.wrappedBuffer(randomBytes);
    }

    private static final Map<Integer, Integer> clientVersionToValidationTypeMap;

    static{
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        map.put(0x09007c02, 1);
        map.put(0x09009702, 1);
        map.put(0x09009f02, 1);
        map.put(0x0900f602, 1);
        map.put(0x0a000202, 1);
        map.put(0x0a000c02, 1);
        map.put(0x80000102, 1);
        map.put(0x80000302, 1);
        map.put(0x0a002002, 1);
        clientVersionToValidationTypeMap = map;
    }

    protected static int getValidatiionTypeForClientVersion(byte[] version){
        final int intValue = Unpooled.wrappedBuffer(version).getInt(0);
        Integer type = clientVersionToValidationTypeMap.get(intValue);
        if(type == null) {
            return 0;
        }
        return type;
    }

    private byte[] clientVersionToUse = new byte[]{0x09, 0x00, 0x7c, 0x02};

    private byte[] serverVersionToUse = new byte[]{0x03, 0x05, 0x01, 0x01};

    private static int digestOffset(ByteBuf in, int validationType){
        switch (validationType){
            case 1: return calculateOffset(in, 8, 728, 12);
            case 2: return calculateOffset(in, 772, 728, 776);
            default: throw new RuntimeException("cannot get digest offset for type: " + validationType);
        }
    }

    private static int publishKeyOffset(ByteBuf in, int validationType){
        switch (validationType){
            case 1: return calculateOffset(in, 1532, 632, 772);
            case 2: return calculateOffset(in, 768, 632, 8);
            default: throw new RuntimeException("cannot get public key offset for type: " + validationType);
        }
    }

    public RTMPHandshake(){}

    public RTMPHandshake(ClientOptions session){
        this.rtmpe = session.isRtmpe();
        this.swfHash = session.getSwfHash();
        this.swfSize = session.getSwfSize();
        if(session.getClientVersionToUse() != null){
            this.clientVersionToUse = session.getClientVersionToUse();
        }
    }

    public Cipher getCipherIn() {
        return cipherIn;
    }

    public Cipher getCipherOut() {
        return cipherOut;
    }

    public byte[] getSwfvBytes() {
        return swfvBytes;
    }

    public boolean isRtmpe() {
        return rtmpe;
    }

    //========================== ENCRYPT / DECRYPT =======================

    private void cipherUpdate(final ByteBuf in, final Cipher cipher){
        final int size = in.readableBytes();
        if(size == 0){
            return;
        }
        final int position = in.readerIndex();
        final byte[] bytes = new byte[size];
        in.getBytes(position, bytes);
        in.setBytes(position, cipher.update(bytes));
    }

    public void cipherUpdateIn(final ByteBuf in){
        cipherUpdate(in, cipherIn);
    }

    public void cipherUpdateOut(final ByteBuf in){
        cipherUpdate(in, cipherOut);
    }

    //========================== PKI =====================================
    private void initKeyPair(){
        final DHParameterSpec keySpec = new DHParameterSpec(DH_MODULUS, DH_BASE);
        final KeyPair keyPair;
        try{
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
            keyGen.initialize(keySpec);
            keyPair = keyGen.generateKeyPair();
            keyAgreement = KeyAgreement.getInstance("DH");
            keyAgreement.init(keyPair.getPrivate());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        // extract public key bytes
        DHPublicKey publicKey = (DHPublicKey) keyPair.getPublic();
        BigInteger dh_Y = publicKey.getY();
        ownPublicKey = dh_Y.toByteArray();
        byte[] temp = new byte[PUBLIC_KEY_SIZE];
        if(ownPublicKey.length < PUBLIC_KEY_SIZE){
            // pad zeros on left
            System.arraycopy(ownPublicKey, 0, temp, PUBLIC_KEY_SIZE - ownPublicKey.length, ownPublicKey.length);
            ownPublicKey = temp;
        }else if(ownPublicKey.length > PUBLIC_KEY_SIZE){
            // truncate zeros from left
            System.arraycopy(ownPublicKey, ownPublicKey.length - PUBLIC_KEY_SIZE, temp, 0, PUBLIC_KEY_SIZE);
            ownPublicKey = temp;
        }
    }

}
