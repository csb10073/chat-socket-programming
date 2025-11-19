package chatpro;

import java.sql.Connection;
import java.sql.SQLException;

public class DBConnectionTest {

    public static void main(String[] args) {
        Connection conn = null;
        System.out.println("--- DB 연결 테스트 시작 ---");
        
        try {
            // DBManager를 사용하여 Connection 객체를 얻어옵니다.
            conn = DBManager.getConnection();
            
            if (conn != null) {
                System.out.println("✅ JDBC 연결 성공!");
                System.out.println("데이터베이스: " + conn.getCatalog());
            } else {
                System.out.println("❌ JDBC 연결 실패: Connection 객체가 null입니다.");
            }
            
        } catch (SQLException e) {
            // 연결 실패 시 발생하는 예외를 처리합니다.
            System.err.println("❌ JDBC 연결 실패: SQL 예외 발생");
            System.err.println("오류 메시지: " + e.getMessage());
            
            // 일반적인 실패 원인 안내
            if (e.getErrorCode() == 1045) {
                System.err.println("-> 원인: 사용자 이름 또는 비밀번호가 잘못되었습니다 (Access denied).");
            } else if (e.getErrorCode() == 0 && e.getMessage().contains("refused")) {
                System.err.println("-> 원인: MySQL 서버가 실행 중인지, 포트(3306)가 올바른지 확인하세요.");
            } else {
                 System.err.println("-> 기타 예외가 발생했습니다.");
            }
            
        } finally {
            // 연결 자원 해제 (DBManager의 close 메소드 사용)
            DBManager.close(conn, null, null);
            System.out.println("--- DB 연결 테스트 종료 ---");
        }
    }
}