package chatpro;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBManager {

    /**
     * 데이터베이스 연결(Connection) 객체를 반환합니다.
     * @return Connection 객체
     * @throws SQLException 연결 실패 시 예외 발생
     */
    public static Connection getConnection() throws SQLException {
        // DBConfig에 설정된 정보를 사용하여 연결을 시도합니다.
        // JDBC 4.0 이상부터는 드라이버 로드 (Class.forName)가 자동으로 처리됩니다.
        return DriverManager.getConnection(
            DBConfig.URL, 
            DBConfig.USER, 
            DBConfig.PASSWORD
        );
    }

    /**
     * JDBC 자원 (Connection, Statement, ResultSet)을 안전하게 해제합니다.
     * @param conn Connection 객체
     * @param pstmt PreparedStatement 객체
     * @param rs ResultSet 객체
     */
    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        // ResultSet 해제
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println("ResultSet 닫기 오류: " + e.getMessage());
            }
        }
        
        // PreparedStatement 해제
        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                System.err.println("PreparedStatement 닫기 오류: " + e.getMessage());
            }
        }

        // Connection 해제
        if (conn != null) {
            try {
                // Connection을 닫기 전에 auto-commit 모드를 확인하고 필요한 경우 롤백/커밋을 고려할 수 있습니다.
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Connection 닫기 오류: " + e.getMessage());
            }
        }
    }
    
    // 오버로딩: Statement와 Connection만 닫을 때
    public static void close(Connection conn, PreparedStatement pstmt) {
        close(conn, pstmt, null);
    }
}