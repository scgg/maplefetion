/**
 * Project  : MapleFetion
 * Package  : test
 * File     : AuthGenerator.java
 * Author   : solosky < solosky772@qq.com >
 * Date     : 2009-11-16
 * Modified : 2009-11-16
 * License  : Apache License 2.0 
 */

package net.solosky.maplefetion.util;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.UUID;

import javax.crypto.Cipher;

/**
 *  验证信息生成类
 *  参照了reflector反编译的飞信源代码以及用HaozesFx测试结果^_^!我折腾了一个下午才搞出来。。
 */
public class AuthGeneratorV4
{
	
	/**
     * 使用RSA加密字节数组
     * @param publicKey  RSA公钥
     * @param obj  要加密的字节数组
     * @return byte[] 加密后的字节数组
     */
    protected byte[] encrypt(RSAPublicKey publicKey, byte[] obj) {
        if (publicKey != null) {
            try {
                Cipher cipher = Cipher.getInstance("RSA");
                cipher.init(Cipher.ENCRYPT_MODE, publicKey);
                return cipher.doFinal(obj);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }
    /** *//**
     * Basic decrypt method
     * @return byte[]
     */
    protected byte[] decrypt(RSAPrivateKey privateKey, byte[] obj) {
        if (privateKey != null) {
                try{
                    Cipher cipher = Cipher.getInstance("RSA");

                    cipher.init(Cipher.DECRYPT_MODE, privateKey);
                    return cipher.doFinal(obj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
    
        return null;
    }
    
    /**
     * 生成加密结果
     * @param publicKey		RSA公钥，从返回的W头部的key获取值，16进制表示的字节数组 67Bytes(134Chars)
     * @param password		V4加密的密码，指用userid和明文密码加密过后的结果（两次sha1），16进制表示的字节数组 20Bytes(40Chars)
     * @param nonce			服务器返回的随机字符串，看做字符串 16Bytes(32Chars)
     * @param aeskey		AES算法的密钥，估计是加密或者解密用户配置的 32Bytes(64Chars)
     * @return				生成的结果，16进制表示的字节数组
     */
    public String generate(String publicKey, String password, String nonce, String aeskey)
    {
    	byte[] pb = ConvertHelper.hexString2ByteNoSpace(password);
    	byte[] nb = ConvertHelper.string2Byte(nonce);
    	byte[] ab = ConvertHelper.hexString2ByteNoSpace(aeskey);
    	
    	byte[] res = new byte[pb.length+nb.length+ab.length];
    	System.arraycopy(nb, 0, res, 0, nb.length);
    	System.arraycopy(pb, 0, res, nb.length, pb.length);
    	System.arraycopy(ab, 0, res, pb.length+nb.length, ab.length);

    	try {
	        byte[] some = encrypt(parsePublicKey(publicKey), res);
	        
	        return ConvertHelper.byte2HexStringWithoutSpace(some);
        } catch (Exception e) {
	      throw new RuntimeException(e);
        }
    }

    /**
     * 从服务器返回的key字符串解析出RSA公钥
     * @param publicKey		服务器返回的key字符串
     * @return	解析出来的RSA公钥，可以用这个公钥加密数据
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
	public RSAPublicKey parsePublicKey(String publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException
    {
		String modulusText  = publicKey.substring(0,0x100);
        String exponentText = publicKey.substring(0x100);
        BigInteger modulus  = new BigInteger(1, ConvertHelper.hexString2ByteNoSpace(modulusText));
        BigInteger exponent = new BigInteger(1, ConvertHelper.hexString2ByteNoSpace(exponentText));
    	KeyFactory keyFactory = KeyFactory.getInstance("RSA");   
    	RSAPublicKeySpec bobPubKeySpec = new RSAPublicKeySpec(modulus, exponent);   
    	RSAPublicKey rsapublicKey = (RSAPublicKey) keyFactory.generatePublic(bobPubKeySpec);   
        return  rsapublicKey;
    }	
	/**
	 * 返回客户端生成的随机字符串
	 * @return
	 */
	public static String getCnonce()
    {
		String uuid = UUID.randomUUID().toString();
    	return ConvertHelper.byte2HexStringWithoutSpace( DigestHelper.MD5( uuid.getBytes()));
    }
}
