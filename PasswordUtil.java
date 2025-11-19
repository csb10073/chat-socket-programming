package chatpro;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {

    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16; // 16바이트 솔트 사용

    /**
     * 임의의 솔트를 생성합니다.
     * @return Base64 인코딩된 솔트 문자열
     */
    public static String getNewSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        // 바이트 배열을 문자열로 저장하기 위해 Base64 인코딩
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 비밀번호와 솔트를 사용하여 해시를 생성합니다.
     * @param password 해시할 비밀번호
     * @param salt 해시 솔트
     * @return Base64 인코딩된 해시 문자열
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance(ALGORITHM);
            // 솔트와 비밀번호를 함께 해싱
            md.update(Base64.getDecoder().decode(salt)); 
            byte[] hashedPassword = md.digest(password.getBytes());
            
            return Base64.getEncoder().encodeToString(hashedPassword);

        } catch (NoSuchAlgorithmException e) {
            // 이 예외는 SHA-256이 지원되지 않을 때 발생 (거의 일어나지 않음)
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }
    
    /**
     * 입력된 비밀번호가 저장된 해시와 일치하는지 검증합니다.
     * @param inputPassword 사용자 입력 비밀번호
     * @param storedHash DB에 저장된 해시
     * @param storedSalt DB에 저장된 솔트
     * @return 일치하면 true, 아니면 false
     */
    public static boolean verifyPassword(String inputPassword, String storedHash, String storedSalt) {
        String inputHash = hashPassword(inputPassword, storedSalt);
        return inputHash.equals(storedHash);
    }
}