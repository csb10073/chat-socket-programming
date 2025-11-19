package chatpro;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;
import java.util.HashSet;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A multithreaded chat room server. When a client connects the server requests a screen
 * name by sending the client the text "SUBMITNAME", and keeps requesting a name until
 * a unique one is received. After a client submits a unique name, the server acknowledges
 * with "NAMEACCEPTED". Then all messages from that client will be broadcast to all other
 * clients that have submitted a unique screen name. The broadcast messages are prefixed
 * with "MESSAGE".
 *
 * This is just a teaching example so it can be enhanced in many ways, e.g., better
 * logging. Another is to accept a lot of fun commands, like Slack.
 */


public class ChatServer {
	
	private static final UserDAO userDao = new UserDAO();

	// All client names, so we can check for duplicates upon registration.
	private static Set<String> names = new HashSet<>();//중복,순서없이 저장되게됨 names.add("su")하면댐

	// The set of all the print writers for all the clients, used for broadcast.
	private static Set<PrintWriter> writers = new HashSet<>();

	public static void main(String[] args) throws Exception {
		System.out.println("The chat server is running...");
		ExecutorService pool = Executors.newFixedThreadPool(500);
		try (ServerSocket listener = new ServerSocket(60000)) {
			while (true) {
				pool.execute(new Handler(listener.accept()));
			}
		}
	}

	/**
	 * The client handler task.
	 */
	private static class Handler implements Runnable {
		private String name;
		private Socket socket;
		private Scanner in;
		private PrintWriter out;

		/**
		 * Constructs a handler thread, squirreling away the socket. All the interesting
		 * work is done in the run method. Remember the constructor is called from the
		 * server's main method, so this has to be as short as possible.
		 */
		public Handler(Socket socket) {
			this.socket = socket;
		}

		/**
		 * Services this thread's client by repeatedly requesting a screen name until a
		 * unique one has been submitted, then acknowledges the name and registers the
		 * output stream for the client in a global set, then repeatedly gets inputs and
		 * broadcasts them.
		 */
		public void run() {
			try {
				in = new Scanner(socket.getInputStream());
				out = new PrintWriter(socket.getOutputStream(), true);

				// Keep requesting a name until we get a unique one. ->로그인 로직으로 대체
				while (true) {
					out.println("welcome");
					
					if (!in.hasNextLine()) {
						return;
					}
					
					String line = in.nextLine();
					String[] parts = line.split(":", 2);// 첫번쨰 콜론만기준분리
					
					String command  = parts[0].toUpperCase();
					String data = parts.length >1 ? parts[1] : "";
					
					//id 중복체크 처리//
					if (command.equals("check id")) {
						String userId = data;
						if(userDao.isUserIdAvailable(userId)) {
							out.println("id_availabe");}
						else {
							out.println("id_unavailable");
					
						}
					}
					///회원가입 처리///
					else if(command.equals("REGISTER")) { // 👈 "REGISTER" (대문자)로 수정
						
	                    // ⭐️ 이전에 수정했던 내용도 반영 ⭐️
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
				///로그인 처리//
				else if(command.equals("LOGIN")) {
					String[] credentials = data.split(":", 2);//userid:password
					if(credentials.length == 2) {
						String userId = credentials[0];
						String password = credentials[1];
						
						String authenticatedId = userDao.authenticateUser(userId,password);
						synchronized(names) {
							if(authenticatedId != null && !names.contains(authenticatedId)) {
								//중복이 아니라 로그인 성공할떄
								name = authenticatedId; //name = userid
								names.add(name);
								
								//성공시 응답, 루프탈출
								out.println("Login Accepted:" + name);
								break;
							}else if(authenticatedId !=null && names.contains(authenticatedId)) {
								//이미 접속중
								out.println("Login Failed: already logged in");
							}else {
								//인증 실패
								out.println("Login Failed: invalid id or password");
								}
						}
					}else {
						out.println("Login Failed: invalid format");
					}
				}
				else {
					out.println("Unkown_Command");
				}
				}
	
				// Now that a successful name has been chosen, add the socket's print writer
				// to the set of all writers so this client can receive broadcast messages.
				// But BEFORE THAT, let everyone else know that the new person has joined!
				
				//로그인 성공 후 모든 클라이언트에게 접속 알림
			
				for (PrintWriter writer : writers) {
					writer.println("MESSAGE " + name + " has joined");
				}
				writers.add(out);

				// Accept messages from this client and broadcast them.
				while (true) {
					String input = in.nextLine();
					if (input.toLowerCase().startsWith("/quit")) {
						return;
					}
					for (PrintWriter writer : writers) {
						writer.println("MESSAGE " + name + ": " + input);
					}
				}
			} catch (Exception e) {
				System.out.println(e);
			} finally {
				if (out != null) {
					writers.remove(out);
				}
				if (name != null) {
					System.out.println(name + " is leaving");
					names.remove(name);
					for (PrintWriter writer : writers) {
						writer.println("MESSAGE " + name + " has left");
					}
				}
				try { socket.close(); } catch (IOException e) {}
			}
		}
	}
}