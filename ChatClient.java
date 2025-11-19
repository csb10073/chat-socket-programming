package chatpro;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * A simple Swing-based client for the chat server. Graphically it is a frame with a text
 * field for entering messages and a textarea to see the whole dialog.
 *
 * The client follows the following Chat Protocol. When the server sends "SUBMITNAME" the
 * client replies with the desired screen name. The server will keep sending "SUBMITNAME"
 * requests as long as the client submits screen names that are already in use. When the
 * server sends a line beginning with "NAMEACCEPTED" the client is now allowed to start
 * sending the server arbitrary strings to be broadcast to all chatters connected to the
 * server. When the server sends a line beginning with "MESSAGE" then all characters
 * following this string should be displayed in its message area.
 */
public class ChatClient {

    String serverAddress;
    Scanner in;
    PrintWriter out;
    
    //채팅창 관련 ui
    JFrame chatframe = new JFrame("Chatter");
    JTextField textField = new JTextField(50);
    JTextArea messageArea = new JTextArea(16, 50);

    /**
     * Constructs the client by laying out the GUI and registering a listener with the
     * textfield so that pressing Return in the listener sends the textfield contents
     * to the server. Note however that the textfield is initially NOT editable, and
     * only becomes editable AFTER the client receives the NAMEACCEPTED message from
     * the server.
     */
    
    //로그인 창 관련 ui
    JFrame loginFrame = new JFrame("login/register");
    JTextField loginIdField = new JTextField(15);
    JPasswordField loginPwField = new JPasswordField(15);
    JButton registerButton = new JButton("click here to register");
    
    
    public ChatClient(String serverAddress) {
        this.serverAddress = serverAddress;
        //채팅창 ui 설정,로그인 전에는 비활성화
        textField.setEditable(false);
        messageArea.setEditable(false);
        chatframe.getContentPane().add(textField, BorderLayout.SOUTH);
        chatframe.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        chatframe.pack();
        chatframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //로그인 창 ui 설정
        createLoginGUI();
        
        // Send on enter then clear to prepare for next message
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
    }

    private void createLoginGUI() {
        // ⭐️ 수정: loginFrame.setLayout(new FlowLayout()); 삭제
        // JFrame의 기본 레이아웃(BorderLayout)을 사용합니다.
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
        
        // ⭐️ 수정: 패널을 프레임의 중앙(CENTER)에 추가합니다.
        // (NORTH로 해도 괜찮습니다.)
        loginFrame.getContentPane().add(panel, BorderLayout.CENTER);
        
        // ⭐️ 수정: 선언만 되고 추가되지 않았던 registerButton을 프레임 하단(SOUTH)에 추가합니다.
        loginFrame.getContentPane().add(registerButton, BorderLayout.SOUTH);
        
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

        //회원가입 리스너
        registerButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                showRegisterDialog();
            }
        });
    }
    
    private void showRegisterDialog() {
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

            // 필드 유효성 검사 (간단하게)
            if (id.isEmpty() || pw.isEmpty() || name.isEmpty()) {
                JOptionPane.showMessageDialog(loginFrame, "필수 항목을 모두 입력하세요.", "오류", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 서버로 REGISTER 명령 전송
            out.println("REGISTER:" + id + ":" + pw + ":" + name + ":" + email);
        }
    }
    
    
    
 

    private void run() throws IOException {
        try {
        	Socket socket = new Socket(serverAddress, 60000);
            in = new Scanner(socket.getInputStream());
            out = new PrintWriter(socket.getOutputStream(), true);

            while (in.hasNextLine()) {
                String line = in.nextLine();
                //서버 시작 시 환영 메시지
                if (line.startsWith("welcome")) {
                    continue;
                } 
                //로그인 성공 처리
                else if (line.startsWith("Login Accepted")) {
                    String name = line.substring(14);
                    //채팅 창으로 전환
                    loginFrame.setVisible(false);
                    chatframe.setTitle("Chatter - " + name);
                    textField.setEditable(true);
                    chatframe.setVisible(true);
                    messageArea.append("로그인 성공 채팅을 시작하세요 \n");
                }
                    
                //로그인 실패처리
                 else if (line.startsWith("Login Failed")) {
                	 String reason = line.substring(12);
                	 JOptionPane.showMessageDialog(loginFrame, "로그인 실패: " + reason, "실패", JOptionPane.ERROR_MESSAGE);
                	 
                 }
             // 4. 회원가입 성공 처리
                 else if (line.startsWith("Register Accepted")) {
                     JOptionPane.showMessageDialog(loginFrame, "회원가입 성공! 로그인해주세요.", "성공", JOptionPane.INFORMATION_MESSAGE);
                 } 
                 
                 // 5. 회원가입 실패 처리
                 else if (line.startsWith("Register Failed")) {
                     String reason = line.substring(15);
                     JOptionPane.showMessageDialog(loginFrame, "회원가입 실패: " + reason, "실패", JOptionPane.ERROR_MESSAGE);
                 }

                 // 6. 일반 채팅 메시지 처리 (기존과 동일)
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
   
                
                

    public static void main(String[] args) throws Exception {
        
        ChatClient client = new ChatClient("127.0.0.1");
        client.run();
    }
}