package com.quanto.extrace;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.SignatureException;
import java.util.Date;
import java.util.Iterator;

import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.RijndaelEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.paddings.ZeroBytePadding;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.openpgp.PGPCompressedData;
import org.bouncycastle.openpgp.PGPCompressedDataGenerator;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPLiteralDataGenerator;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPSignatureSubpacketGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.util.encoders.Base64;

/**
 * 
 * @author tarun
 * @since 10-Apr-2019
 */
public class GPGEncrypDecryp extends BufferedWriter {

	static final ClassLoader loader = GPGEncrypDecryp.class.getClassLoader();
	/**
	 * 
	 * @param args
	 */

	private static final int LINE_LENGTH = 64;
	private static char[] buf = new char[LINE_LENGTH];
	private final int nlLength;

	public GPGEncrypDecryp(Writer out) {
		super(out);

		String nl = System.getProperty("line.separator");
		if (nl != null) {
			nlLength = nl.length();
		} else {
			nlLength = 2;
		}
	}

	public static void main(String[] args) throws DataLengthException, IllegalStateException, InvalidCipherTextException {
		// get some input
		String message = "";
		System.out.println("The input is : " + message);

		// add Bouncy JCE Provider, http://bouncycastle.org/latest_releases.html
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

		// hardcode our private key password **NOT A GOOD IDEA...duh**
		String privateKeyPassword = "Dota@123";

		PGPPublicKey pubKey = null;
		// Load public key
		try {
			pubKey = readPublicKey(loader.getResourceAsStream("DB3DC4E59E3E337E52D1F98927E1F7EC3119CE6D.asc"));
		} catch (IOException | PGPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (pubKey != null) {
			System.out.println("Successfully read public key: ");
			// System.out.println("Key Owner: "+pubKey.getUserIDs());
			// System.out.println("Key Stength: "+pubKey.getBitStrength());
			// System.out.println("Key Algorithm: "+pubKey.getAlgorithm()+"\n\n");
		}

		// Load private key, **NOTE: still secret, we haven't unlocked it yet**
		PGPSecretKey pgpSec = null;
		try {
			pgpSec = readSecretKey(loader.getResourceAsStream("key_DB3DC4E59E3E337E52D1F98927E1F7EC3119CE6D.asc"));

		} catch (IOException | PGPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// sign our message
		String messageSignature = null;
		try {
			messageSignature = signMessageByteArray(message, pgpSec, privateKeyPassword.toCharArray());
			//testEncryptRijndael(messageSignature, privateKeyPassword.toString());
		} catch (NoSuchAlgorithmException | NoSuchProviderException | SignatureException | IOException
				| PGPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (messageSignature != null) {
			System.out.println("Successfully signed your message with the private key.\n\n");
			System.out.println(messageSignature + "\n\n");
		}

		System.out.println("Now Encrypting it.");

		String encryptedMessage = null;
		try {
			encryptedMessage = encryptByteArray(message.getBytes(), pubKey, true, true);
		} catch (NoSuchProviderException | IOException | PGPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (encryptedMessage != null) {
			System.out.println("PGP Encrypted Message: ");
			System.out.println(encryptedMessage);
		}

	}

	private static byte[] signMessageByteArrayConversion(String message, PGPSecretKey pgpSec, char pass[])
			throws PGPException, IOException, SignatureException {
		byte[] messageCharArray = message.getBytes();

		ByteArrayOutputStream encOut = new ByteArrayOutputStream();
		OutputStream out = encOut;
		out = new ArmoredOutputStream(out);

		// Unlock the private key using the password
		PGPPrivateKey pgpPrivKey = pgpSec
				.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(pass));

		// Signature generator, we can generate the public key from the private
		// key! Nifty!
		PGPSignatureGenerator sGen = new PGPSignatureGenerator(
				new JcaPGPContentSignerBuilder(pgpSec.getPublicKey().getAlgorithm(), PGPUtil.SHA512).setProvider("BC"));

		sGen.init(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);

		Iterator it = pgpSec.getPublicKey().getUserIDs();
		if (it.hasNext()) {
			PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
			spGen.setSignerUserID(false, (String) it.next());
			sGen.setHashedSubpackets(spGen.generate());
		}

		PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedData.ZLIB);

		BCPGOutputStream bOut = new BCPGOutputStream(comData.open(out));

		sGen.generateOnePassVersion(false).encode(bOut);

		PGPLiteralDataGenerator lGen = new PGPLiteralDataGenerator();
		OutputStream lOut = lGen.open(bOut, PGPLiteralData.BINARY, PGPLiteralData.CONSOLE, messageCharArray.length,
				new Date());

		for (byte c : messageCharArray) {
			lOut.write(c);
			sGen.update(c);
		}

		lOut.close();
		/*
		 * while ((ch = message.toCharArray().read()) >= 0) { lOut.write(ch);
		 * sGen.update((byte) ch); }
		 */
		lGen.close();

		sGen.generate().encode(bOut);

		comData.close();

		out.close();

		return messageCharArray;

	}

	/**
	 * @param message
	 * @param pgpSec
	 * @param pass
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws PGPException
	 * @throws SignatureException
	 */
	@SuppressWarnings("rawtypes")
	private static String signMessageByteArray(String message, PGPSecretKey pgpSec, char pass[])
			throws IOException, NoSuchAlgorithmException, NoSuchProviderException, PGPException, SignatureException {
		byte[] messageCharArray = message.getBytes();

		ByteArrayOutputStream encOut = new ByteArrayOutputStream();
		OutputStream out = encOut;
		out = new ArmoredOutputStream(out);

		// Unlock the private key using the password
		PGPPrivateKey pgpPrivKey = pgpSec
				.extractPrivateKey(new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(pass));

		// Signature generator, we can generate the public key from the private
		// key! Nifty!
		PGPSignatureGenerator sGen = new PGPSignatureGenerator(
				new JcaPGPContentSignerBuilder(pgpSec.getPublicKey().getAlgorithm(), PGPUtil.SHA512).setProvider("BC"));

		sGen.init(PGPSignature.BINARY_DOCUMENT, pgpPrivKey);

		Iterator it = pgpSec.getPublicKey().getUserIDs();
		if (it.hasNext()) {
			PGPSignatureSubpacketGenerator spGen = new PGPSignatureSubpacketGenerator();
			spGen.setSignerUserID(false, (String) it.next());
			sGen.setHashedSubpackets(spGen.generate());
		}

		PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedData.ZLIB);

		BCPGOutputStream bOut = new BCPGOutputStream(comData.open(out));

		sGen.generateOnePassVersion(false).encode(bOut);

		PGPLiteralDataGenerator lGen = new PGPLiteralDataGenerator();
		OutputStream lOut = lGen.open(bOut, PGPLiteralData.BINARY, PGPLiteralData.CONSOLE, messageCharArray.length,
				new Date());

		for (byte c : messageCharArray) {
			lOut.write(c);
			sGen.update(c);
		}

		lOut.close();
		/*
		 * while ((ch = message.toCharArray().read()) >= 0) { lOut.write(ch);
		 * sGen.update((byte) ch); }
		 */
		lGen.close();

		sGen.generate().encode(bOut);

		comData.close();

		out.close();

		return encOut.toString();
	}

	/**
	 * 
	 * @param clearData
	 * @param encKey
	 * @param withIntegrityCheck
	 * @param armor
	 * @return
	 * @throws IOException
	 * @throws PGPException
	 * @throws NoSuchProviderException
	 */
	@SuppressWarnings("deprecation")
	public static String encryptByteArray(byte[] clearData, PGPPublicKey encKey, boolean withIntegrityCheck,
			boolean armor) throws IOException, PGPException, NoSuchProviderException {

		ByteArrayOutputStream encOut = new ByteArrayOutputStream();

		OutputStream out = encOut;
		if (armor) {
			out = new ArmoredOutputStream(out);
		}

		ByteArrayOutputStream bOut = new ByteArrayOutputStream();

		PGPCompressedDataGenerator comData = new PGPCompressedDataGenerator(PGPCompressedDataGenerator.ZIP);
		OutputStream cos = comData.open(bOut); // open it with the final

		PGPLiteralDataGenerator lData = new PGPLiteralDataGenerator();

		OutputStream pOut = lData.open(cos, PGPLiteralData.BINARY, PGPLiteralData.CONSOLE, clearData.length, // length
																												// of
																												// clear
																												// data
				new Date() // current time
		);
		pOut.write(clearData);

		lData.close();
		comData.close();

		PGPEncryptedDataGenerator cPk = new PGPEncryptedDataGenerator(PGPEncryptedData.CAST5, withIntegrityCheck,
				new SecureRandom(), "BC");

		cPk.addMethod(encKey);

		byte[] bytes = bOut.toByteArray();

		OutputStream cOut = cPk.open(out, bytes.length);

		cOut.write(bytes); // obtain the actual bytes from the compressed stream

		cOut.close();

		out.close();

		return encOut.toString();
	}

	/**
	 * A simple routine that opens a key ring file and loads the first available key
	 * suitable for encryption.
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 * @throws PGPException
	 */
	@SuppressWarnings("rawtypes")
	public static PGPPublicKey readPublicKey(InputStream input) throws IOException, PGPException {
		PGPPublicKeyRingCollection pgpPub = new PGPPublicKeyRingCollection(PGPUtil.getDecoderStream(input));

		Iterator keyRingIter = pgpPub.getKeyRings();
		while (keyRingIter.hasNext()) {
			PGPPublicKeyRing keyRing = (PGPPublicKeyRing) keyRingIter.next();

			Iterator keyIter = keyRing.getPublicKeys();
			while (keyIter.hasNext()) {
				PGPPublicKey key = (PGPPublicKey) keyIter.next();

				if (key.isEncryptionKey()) {
					return key;
				}
			}
		}

		throw new IllegalArgumentException("Can't find encryption key in key ring.");
	}

	/**
	 * A simple routine that opens a key ring file and loads the first available key
	 * suitable for signature generation.
	 * 
	 * @param input
	 *            stream to read the secret key ring collection from.
	 * @return a secret key.
	 * @throws IOException
	 *             on a problem with using the input stream.
	 * @throws PGPException
	 *             if there is an issue parsing the input stream.
	 */
	@SuppressWarnings("rawtypes")
	public static PGPSecretKey readSecretKey(InputStream input) throws IOException, PGPException {
		PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(input));

		//
		// we just loop through the collection till we find a key suitable for
		// encryption, in the real
		// world you would probably want to be a bit smarter about this.
		//

		Iterator keyRingIter = pgpSec.getKeyRings();
		while (keyRingIter.hasNext()) {
			PGPSecretKeyRing keyRing = (PGPSecretKeyRing) keyRingIter.next();

			Iterator keyIter = keyRing.getSecretKeys();
			while (keyIter.hasNext()) {
				PGPSecretKey key = (PGPSecretKey) keyIter.next();

				if (key.isSigningKey()) {
					return key;
				}
			}
		}

		throw new IllegalArgumentException("Can't find signing key in key ring.");
	}

	public static String testEncryptRijndael(String value, String key)
			throws DataLengthException, IllegalStateException, InvalidCipherTextException {
		BlockCipher engine = new RijndaelEngine(256);
		BufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(engine), new ZeroBytePadding());

		byte[] keyBytes = key.getBytes();
		cipher.init(true, new KeyParameter(keyBytes));

		byte[] input = value.getBytes();
		byte[] cipherText = new byte[cipher.getOutputSize(input.length)];

		int cipherLength = cipher.processBytes(input, 0, input.length, cipherText, 0);
		cipher.doFinal(cipherText, cipherLength);

		String result = new String(Base64.encode(cipherText));
		// Log.e("testEncryptRijndael : " , result);
		return result;
	}

}
