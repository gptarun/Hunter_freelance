package com.quanto.extrace;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

import org.bouncycastle.bcpg.ArmoredInputStream;
import org.bouncycastle.bcpg.ArmoredOutputStream;
import org.bouncycastle.bcpg.BCPGOutputStream;
import org.bouncycastle.bcpg.HashAlgorithmTags;
import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPSignature;
import org.bouncycastle.openpgp.PGPSignatureGenerator;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.bc.BcPGPContentSignerBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.springframework.stereotype.Component;

@Component
public class GpgSign {

	private static final int BUFFER_SIZE = 4096;
	private static int signatureAlgo = HashAlgorithmTags.SHA512;
	private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
	static final ClassLoader loader = GpgSign.class.getClassLoader();

	// Copied Main to create signature
	public Map<String, String> createSignature(String message) {

		System.out.println("The input is : " + message);

		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

		String privateKeyPassword = "Dota@123";

		String filePath = "src\\main\\resources\\key_DB3DC4E59E3E337E52D1F98927E1F7EC3119CE6D.asc";

		readLineByLineJava8(filePath);
		Map<String, String> signatureMap = signData(readLineByLineJava8(filePath), privateKeyPassword, message);
		return signatureMap;
	}

	// Read file content into string with - Files.lines(Path path, Charset cs)

	private static String readLineByLineJava8(String filePath) {
		StringBuilder contentBuilder = new StringBuilder();

		try (Stream<String> stream = Files.lines(Paths.get(filePath), StandardCharsets.UTF_8)) {
			stream.forEach(s -> contentBuilder.append(s).append("\n"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		return contentBuilder.toString();
	}

	public static Map<String, String> signData(final String privKeyData, final String password, final String data) {
		// region Decode Private Key
		Map<String, String> resultMap = new HashMap<>();
		PGPSecretKey secKey;
		try {
			secKey = getSecretKey(privKeyData);
			PGPPrivateKey privKey = decryptArmoredPrivateKey(secKey, password);
			// endregion
			// region Sign Data
			String signature = signArmoredAscii(privKey, data, signatureAlgo);

			resultMap.put("asciiArmoredSignature", signature);
			resultMap.put("hashingAlgo", hashAlgoToString(signatureAlgo));
			resultMap.put("fingerPrint", shortFingerprint(bytesToHex(secKey.getPublicKey().getFingerprint())));
			// endregion
		} catch (IOException | PGPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultMap;
	}

	private static String shortFingerprint(String fingerprint) {
		String shortFingerprint = "";
		if(fingerprint != null && !fingerprint.isEmpty()) {
			
		}
		return shortFingerprint;
	}

	static PGPSecretKey getSecretKey(String privateKeyData) throws IOException, PGPException {
		PGPPrivateKey privKey = null;
		try (InputStream privStream = new ArmoredInputStream(
				new ByteArrayInputStream(privateKeyData.getBytes("UTF-8")))) {
			PGPSecretKeyRingCollection pgpSec = new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(privStream),
					new JcaKeyFingerprintCalculator());
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
		}
		throw new IllegalArgumentException("Can't find signing key in key ring.");
	}

	static PGPPrivateKey decryptArmoredPrivateKey(PGPSecretKey secretKey, String password)
			throws IOException, PGPException {
		return secretKey.extractPrivateKey(
				new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(password.toCharArray()));
	}

	static String signArmoredAscii(PGPPrivateKey privateKey, String data, int signatureAlgo)
			throws IOException, PGPException {
		String signature = null;
		final PGPSignatureGenerator signatureGenerator = new PGPSignatureGenerator(
				new BcPGPContentSignerBuilder(privateKey.getPublicKeyPacket().getAlgorithm(), signatureAlgo));
		signatureGenerator.init(PGPSignature.BINARY_DOCUMENT, privateKey);
		ByteArrayOutputStream signatureOutput = new ByteArrayOutputStream();
		try (BCPGOutputStream outputStream = new BCPGOutputStream(new ArmoredOutputStream(signatureOutput))) {
			processStringAsStream(data, new StreamHandler() {
				@Override
				public void handleStreamBuffer(byte[] buffer, int offset, int length) throws IOException {
					signatureGenerator.update(buffer, offset, length);
				}
			});
			signatureGenerator.generate().encode(outputStream);
		}

		signature = new String(signatureOutput.toByteArray(), "UTF-8");

		return signature;
	}

	static String bytesToHex(byte[] bytes) {
		char[] hexChars = new char[bytes.length * 2];
		for (int j = 0; j < bytes.length; j++) {
			int v = bytes[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		return new String(hexChars);
	}

	static void processStream(InputStream is, StreamHandler handler) throws IOException {
		int read;
		byte[] buffer = new byte[BUFFER_SIZE];
		while ((read = is.read(buffer)) != -1) {
			handler.handleStreamBuffer(buffer, 0, read);
		}
	}

	static void processStringAsStream(String data, StreamHandler handler) throws IOException {
		InputStream is = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8.name()));
		processStream(is, handler);
	}

	static void processByteArrayAsStream(byte[] data, StreamHandler handler) throws IOException {
		InputStream is = new ByteArrayInputStream(data);
		processStream(is, handler);
	}

	static String hashAlgoToString(int hashAlgo) {
		switch (hashAlgo) {
		case HashAlgorithmTags.DOUBLE_SHA:
			return "DOUBLESHA";
		case HashAlgorithmTags.HAVAL_5_160:
			return "HAVAL5_160";
		case HashAlgorithmTags.MD2:
			return "MD2";
		case HashAlgorithmTags.MD5:
			return "MD5";
		case HashAlgorithmTags.RIPEMD160:
			return "RIPEMD160";
		case HashAlgorithmTags.SHA1:
			return "SHA1";
		case HashAlgorithmTags.SHA224:
			return "SHA224";
		case HashAlgorithmTags.SHA256:
			return "SHA256";
		case HashAlgorithmTags.SHA384:
			return "SHA384";
		case HashAlgorithmTags.SHA512:
			return "SHA512";
		case HashAlgorithmTags.TIGER_192:
			return "TIGER192";
		default:
			return "Unknown";
		}
	}

}
