package chatpro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    /**
     * [CHECK_ID] 사용자 ID 중복 여부를 확인합니다.
     * @param userId 확인할 사용자 ID
     * @return 사용 가능하면 true, 중복되면 false
     */
    public boolean isUserIdAvailable(String userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT user_id FROM users WHERE user_id = ?";
        
        try {
            conn = DBManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();
            
            // 결과가 있으면 중복 (사용 불가능)
            return !rs.next(); 
            
        } catch (SQLException e) {
            System.err.println("ID 중복 체크 중 DB 오류: " + e.getMessage());
            return false; // 오류 발생 시 안전하게 사용 불가능으로 처리
        } finally {
            DBManager.close(conn, pstmt, rs);
        }
    }

    /**
     * [REGISTER] 새 사용자를 데이터베이스에 등록합니다.
     * @param userId, password, name, email 사용자 정보
     * @return 성공하면 true, 실패하면 false
     */
    public boolean registerUser(String userId, String password, String name, String email) {
        // 1. 솔트 생성 및 비밀번호 해시
        String salt = PasswordUtil.getNewSalt();
        String passwordHash = PasswordUtil.hashPassword(password, salt);
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        String sql = "INSERT INTO users (user_id, password_hash, salt, name, email) VALUES (?, ?, ?, ?, ?)";
        
        try {
            conn = DBManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            pstmt.setString(2, passwordHash);
            pstmt.setString(3, salt);
            pstmt.setString(4, name);
            pstmt.setString(5, email);
            
            // 쿼리 실행 (1보다 크면 성공)
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("사용자 등록 중 DB 오류: " + e.getMessage());
            return false;
        } finally {
            DBManager.close(conn, pstmt);
        }
    }

    /**
     * [LOGIN] 사용자를 인증합니다.
     * @param userId 사용자 ID
     * @param password 사용자 입력 비밀번호
     * @return 인증 성공 시 사용자 ID, 실패 시 null
     */
    public String authenticateUser(String userId, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        // 로그인 시 is_logged_in 상태 업데이트는 서버 핸들러에서 처리할 수도 있습니다.
        String sql = "SELECT password_hash, salt FROM users WHERE user_id = ?";
        
        try {
            conn = DBManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // 1. DB에 저장된 해시와 솔트를 가져옴
                String storedHash = rs.getString("password_hash");
                String storedSalt = rs.getString("salt");
                
                // 2. 입력 비밀번호와 저장된 솔트로 해시를 생성하여 저장된 해시와 비교
                if (PasswordUtil.verifyPassword(password, storedHash, storedSalt)) {
                    return userId; // 인증 성공
                }
            }
            return null; // ID가 없거나 비밀번호 불일치
            
        } catch (SQLException e) {
            System.err.println("사용자 인증 중 DB 오류: " + e.getMessage());
            return null;
        } finally {
            DBManager.close(conn, pstmt, rs);
        }
    }
}