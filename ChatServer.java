package chatpro;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap; // ⭐️ 중요: 스레드 안전한 Map 사용
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    
    private static final UserDAO userDao = new UserDAO();

    // ⭐️ 중요: [ID]를 키로, [출력스트림]을 값으로 저장하는 Map으로 변경
    // 이렇게 해야 "user1에게 보내줘"라고 했을 때 user1의 스트림을 바로 찾을 수 있습니다.
    private static Map<String, PrintWriter> clientWriters = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running...");
        ExecutorService pool = Executors.newFixedThreadPool(500);
        try (ServerSocket listener = new ServerSocket(60000)) {
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        }
    }

    private static class Handler implements Runnable {
        private String name;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                while (true) {
                    out.println("welcome");
                    if (!in.hasNextLine()) return;
                    
                    String line = in.nextLine();
                    String[] parts = line.split(":", 2);
                    String command = parts[0].toUpperCase();
                    String data = parts.length > 1 ? parts[1] : "";
                    
                    // ... (Check ID, Register 로직은 기존과 동일하게 두셔도 됩니다) ...
                    // ... (지면 관계상 Register 부분은 기존 코드 유지) ...
                    
                    // ⭐️ 로그인 부분 수정
                    if(command.equals("LOGIN")) {
                        String[] credentials = data.split(":", 2);
                        if(credentials.length == 2) {
                            String userId = credentials[0];
                            String password = credentials[1];
                            
                            String authenticatedId = userDao.authenticateUser(userId, password);
                            
                            // ⭐️ Map을 사용하여 중복 체크 및 등록
                            if(authenticatedId != null) {
                                if(!clientWriters.containsKey(authenticatedId)) {
                                    name = authenticatedId;
                                    // 성공 시 Map에 등록
                                    clientWriters.put(name, out); 
                                    out.println("Login Accepted:" + name);
                                    break;
                                } else {
                                    out.println("Login Failed: already logged in");
                                }
                            } else {
                                out.println("Login Failed: invalid id or password");
                            }
                        } else {
                            out.println("Login Failed: invalid format");
                        }
                    }
                    // ⭐️ 회원가입 로직 (기존 코드 붙여넣으시면 됩니다)
                    else if(command.equals("REGISTER")) {
                        String[] fields = data.split(":", 4);
                        if(fields.length == 4) {
                            if(userDao.registerUser(fields[0], fields[1], fields[2], fields[3])) {
                                out.println("Register Accepted");
                            } else {
                                out.println("Register Failed:db error or id unavailable");
                            }
                        } else {
                            out.println("Register Failed: invalid format");
                        }
                    }
                    else if(command.equals("CHECK ID")) {
                        if(userDao.isUserIdAvailable(data)) out.println("id_available");
                        else out.println("id_unavailable");
                    }
                }

                // 입장 알림 Broadcast
                for (PrintWriter writer : clientWriters.values()) {
                    writer.println("MESSAGE " + name + " has joined");
                }

                // ⭐️ 채팅 메시지 처리 (귓속말 기능 추가)
                while (true) {
                    String input = in.nextLine();
                    if (input.toLowerCase().startsWith("/quit")) {
                        return;
                    }
                    
                    // ⭐️ 귓속말 처리: /w [상대ID] [메시지]
                    if (input.startsWith("/w ")) {
                        String[] split = input.split(" ", 3); // /w, id, msg 3개로 분리
                        if (split.length >= 3) {
                            String targetName = split[1];
                            String msg = split[2];
                            
                            PrintWriter targetWriter = clientWriters.get(targetName);
                            if (targetWriter != null) {
                                // 상대방에게 전송
                                targetWriter.println("MESSAGE [귓속말 from " + name + "]: " + msg);
                                // 나에게도 확인 메시지 표시
                                out.println("MESSAGE [귓속말 to " + targetName + "]: " + msg);
                            } else {
                                out.println("MESSAGE Server: 사용자를 찾을 수 없습니다.");
                            }
                        }
                    } 
                    // ⭐️ 전체 메시지 (기존과 동일)
                    else {
                        for (PrintWriter writer : clientWriters.values()) {
                            writer.println("MESSAGE " + name + ": " + input);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                if (name != null) {
                    System.out.println(name + " is leaving");
                    clientWriters.remove(name); // ⭐️ Map에서 제거
                    for (PrintWriter writer : clientWriters.values()) {
                        writer.println("MESSAGE " + name + " has left");
                    }
                }
                try { socket.close(); } catch (IOException e) {}
            }
        }
    }
}