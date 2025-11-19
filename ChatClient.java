package chatpro;

// ... (기존 import 유지) ...
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import javax.swing.*;

public class ChatClient {

    String serverAddress;
    Scanner in;
    PrintWriter out;
    
    // 채팅창 관련 UI
    JFrame chatframe = new JFrame("Chatter");
    JTextField textField = new JTextField(40); // 메시지 입력칸
    JTextArea messageArea = new JTextArea(16, 50);
    
    // ⭐️ 귓속말용 UI 추가
    JTextField targetField = new JTextField(10); // 받는 사람 ID 입력칸
    JLabel targetLabel = new JLabel("귓속말 대상(비우면 전체):");

    // ... (로그인 UI 변수들은 기존과 동일) ...
    JFrame loginFrame = new JFrame("Login");
    JTextField loginIdField = new JTextField(15);
    JPasswordField loginPwField = new JPasswordField(15);
    JButton registerButton = new JButton("Register");

    public ChatClient(String serverAddress) {
        this.serverAddress = serverAddress;
        textField.setEditable(false);
        messageArea.setEditable(false);
        
        // ⭐️ 하단 패널 구성 (귓속말 대상 + 메시지 입력)
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        JPanel targetPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        targetPanel.add(targetLabel);
        targetPanel.add(targetField);
        
        bottomPanel.add(targetPanel, BorderLayout.WEST);
        bottomPanel.add(textField, BorderLayout.CENTER);
        
        chatframe.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
        chatframe.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        chatframe.pack();
        chatframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        createLoginGUI();
        
        // 메시지 전송 리스너
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String msg = textField.getText();
                String target = targetField.getText().trim();
                
                // ⭐️ 귓속말 대상이 적혀있으면 /w 명령어로 변환해서 전송
                if (!target.isEmpty()) {
                    out.println("/w " + target + " " + msg);
                } else {
                    out.println(msg);
                }
                textField.setText("");
            }
        });
    }

    // ... (createLoginGUI, showRegisterDialog, run 메소드 등은 기존 코드 그대로 유지) ...
    // (이 부분은 수정할 필요 없습니다. 다만 run() 내부의 Login Accepted 처리는 유지하세요)

    // ⭐️ main 메소드 수정 (설정 파일 읽기)
public static void main(String[] args) throws Exception {
        
        String serverIp = "127.0.0.1"; // 파일이 없을 때를 대비한 기본값
        
        // 1. 설정 파일 읽기 시도
        try {
            // 프로젝트 폴더 바로 아래에 있는 serverinfo.dat 파일을 찾음
            java.io.File configFile = new java.io.File("serverinfo.dat");
            
            if (configFile.exists()) {
                Scanner fileScanner = new Scanner(configFile);
                if (fileScanner.hasNextLine()) {
                    String readIp = fileScanner.nextLine().trim(); // 공백 제거
                    if (!readIp.isEmpty()) {
                        serverIp = readIp; // 파일에서 읽은 IP로 교체
                        System.out.println("설정 파일(serverinfo.dat)에서 IP를 로드했습니다: " + serverIp);
                    }
                }
                fileScanner.close();
            } else {
                System.out.println("설정 파일이 없습니다. 기본 IP(" + serverIp + ")를 사용합니다.");
            }
        } catch (Exception e) {
            System.out.println("설정 파일 읽기 중 오류 발생: " + e.getMessage());
        }

        // 2. 결정된 IP로 클라이언트 실행
        ChatClient client = new ChatClient(serverIp);
        client.run();
    }
    
    // ... (나머지 메소드 복사해서 넣으세요) ...
    
    private void createLoginGUI() {
        // ... (기존에 작성하신 코드 그대로 사용) ...
        // 주의: loginFrame.getContentPane().add(...) 사용하는 부분 유지
        // ...
        
        // (편의를 위해 기존 코드 복붙하세요. 바뀐 건 main과 ChatClient 생성자 부분뿐입니다.)
        
    	loginFrame.setLayout(new FlowLayout());
    	loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	
    	JPanel panel = new JPanel(new GridLayout(3,2));
    	
    	//아이디 입력
    	panel.add(new JLabel("Username:"));
    	panel.add(loginIdField);
    	
    	//비밀번호 입력
    	panel.add(new JLabel("password:"));
    	panel.add(loginPwField);
    	
    	//로그인 버튼
    	JButton loginButton = new JButton("Login");
    	panel.add(loginButton);
    	panel.add(new JLabel(""));// 빈공간
    	
    	loginFrame.add(panel,BorderLayout.NORTH);
    	loginFrame.add(registerButton, BorderLayout.SOUTH);//회원가입 버튼
    	
    	loginFrame.pack();
    	loginFrame.setVisible(true);// 프로그램 시작과 함께 로그인 창 표시
    	
    	//로그인 버튼 리스너
    	loginButton.addActionListener(new ActionListener(){
    		public void actionPerformed(ActionEvent e) {
    			String id = loginIdField.getText();
    			String pw = new String(loginPwField.getPassword());
    			 
    			//서버로 LOGIN 명령 전송
    			out.println("LOGIN:" + id + ":" + pw);
    		}
    	});
    	//회원가입 리스터
        registerButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                showRegisterDialog();
            }
        });
    }

    private void showRegisterDialog() {
         // ... (기존 작성하신 코드 그대로 사용) ...
         // ... 
        JTextField idField = new JTextField(10);
        JPasswordField pwField = new JPasswordField(10);
        JTextField nameField = new JTextField(10);
        JTextField emailField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("ID:"));
        panel.add(idField);
        panel.add(new JLabel("Password:"));
        panel.add(pwField);
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);

        int result = JOptionPane.showConfirmDialog(
            loginFrame, panel, "회원가입", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        
        if (result == JOptionPane.OK_OPTION) {
            String id = idField.getText();
            String pw = new String(pwField.getPassword());
            String name = nameField.getText();
            String email = emailField.getText();

            if (id.isEmpty() || pw.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(loginFrame, "필수 항목을 모두 입력하세요.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            out.println("REGISTER:" + id + ":" + pw + ":" + name + ":" + email);
        }
    }

    private void run() throws IOException {
        // ... (기존 작성하신 코드 그대로 사용, Login Accepted 대소문자 주의) ...
        try {
        	Socket socket = new Socket(serverAddress, 60000);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {
                String line = in.nextLine();
                if (line.startsWith("welcome")) {
                    continue;
                } 
                else if (line.startsWith("Login Accepted")) {
                    String name = line.substring(14);
                    loginFrame.setVisible(false);
                    chatframe.setTitle("Chatter - " + name);
                    textField.setEditable(true);
                    chatframe.setVisible(true);
                    messageArea.append("로그인 성공 채팅을 시작하세요 \n");
                }
                 else if (line.startsWith("Login Failed")) {
                	 String reason = line.substring(12);
                	 JOptionPane.showMessageDialog(loginFrame, "로그인 실패: " + reason, "실패", JOptionPane.ERROR_MESSAGE);
                 }
                 else if (line.startsWith("Register Accepted")) {
                     JOptionPane.showMessageDialog(loginFrame, "회원가입 성공! 로그인해주세요.", "성공", JOptionPane.INFORMATION_MESSAGE);
                 } 
                 else if (line.startsWith("Register Failed")) {
                     String reason = line.substring(15);
                     JOptionPane.showMessageDialog(loginFrame, "회원가입 실패: " + reason, "실패", JOptionPane.ERROR_MESSAGE);
                 }
                 else if (line.startsWith("MESSAGE")) {
                     messageArea.append(line.substring(8) + "\n");
                 }
            }
        }finally {
            if (chatframe.isVisible()) {
                chatframe.setVisible(false);
                chatframe.dispose();
            }
            if (loginFrame.isVisible()) {
                loginFrame.setVisible(false);
                loginFrame.dispose();
            }
        }
    }
}