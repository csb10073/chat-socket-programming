package chatpro;

public class DBConfig {
    
    // 데이터베이스 접속 URL
    // ⭐️ 수정: 3306/ 뒤에 본인의 데이터베이스 이름을 추가하세요.
    // ⭐️ 예: public static final String URL = "jdbc:mysql://127.0.0.1:3306/chat_db?serverTimezone=UTC";
	
    // ✅✅✅ 이렇게 수정하세요 ✅✅✅
	public static final String URL = "jdbc:mysql://127.0.0.1:3306/chatpro?serverTimezone=UTC";
    
    // MySQL 사용자 이름
    public static final String USER = "root"; 
    
    // MySQL 비밀번호
    public static final String PASSWORD = "tnqlsdl12!@"; 

}