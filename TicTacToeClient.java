import java.net.Socket;
import java.util.Scanner;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class TicTacToeClient {

	static Scanner kb = new Scanner(System.in);
	public static void main(String[] args) throws Exception {
		try (Socket socket = new Socket("codebank.xyz", 38006)) {
			InputStream is = socket.getInputStream();
			OutputStream os = socket.getOutputStream();
			ObjectInputStream ois = new ObjectInputStream(is);
			ObjectOutputStream oos = new ObjectOutputStream(os);
			System.out.println("Welcome to Tic-Tac-Toe");
			System.out.print("Enter username: ");
			String username = kb.nextLine();
			ConnectMessage connect = new ConnectMessage(username);
			oos.writeObject(connect);

			CommandMessage startgame = new CommandMessage(
					CommandMessage.Command.NEW_GAME);
			oos.writeObject(startgame);

			Object message;
			BoardMessage boardmessage = null;
			ErrorMessage error;
			if ((message = ois.readObject()) instanceof ErrorMessage) {
				error = (ErrorMessage) message;
				System.out.println(error.getError());
			} else {
				boardmessage = (BoardMessage) message;
			}
			byte[][] board = boardmessage.getBoard();
			System.out.println("-BOARD-");
			for (int i = 0; i < board.length; i++) {
				for (int j = 0; j < board.length; j++) {
					if (board[i][j] == 0) {
						System.out.print("[ ]");
					} else {
						System.out.print("[X]");
					}
				}
				System.out.println();
			}
			System.out.println();
			while (boardmessage.getStatus() == BoardMessage.Status.IN_PROGRESS) {
				int selection = menu();
				if (selection == 1) {
					System.out.print("Make your move. \nRow: ");
					byte row = kb.nextByte();
					System.out.print("Collumn: ");
					byte col = kb.nextByte();
					MoveMessage newmove = new MoveMessage(row, col);
					oos.writeObject(newmove);
				}
				
				else if (selection == 2){
					CommandMessage surrender = new CommandMessage(CommandMessage.Command.SURRENDER);
					oos.writeObject(surrender);
					error = (ErrorMessage) ois.readObject();
					System.out.println(error.getError());
					System.out.println("....loading new game...");
					oos.writeObject(startgame);
				}
				
				else if (selection == 3){
					CommandMessage exit = new CommandMessage(CommandMessage.Command.EXIT);
					oos.writeObject(exit);
					error = (ErrorMessage) ois.readObject();
					System.out.println(error.getError());
					break;
				}
				if ((message = ois.readObject()) instanceof ErrorMessage) {
					error = (ErrorMessage) message;
					System.out.println(error.getError());
				} else {
					boardmessage = (BoardMessage) message;
				}
				board = boardmessage.getBoard();
				System.out.println("\n-BOARD- " + "Turn: " + boardmessage.getTurn());
				for (int i = 0; i < board.length; i++) {
					for (int j = 0; j < board.length; j++) {
						if (board[i][j] == 0) {
							System.out.print("[ ]");
						} else if (board[i][j] == 1) {
							System.out.print("[X]");
						} else {
							System.out.print("[O]");
						}
					}
					System.out.println();
				}
				System.out.println(boardmessage.getStatus());
				System.out.println();
			}
		}
	}

	private static int menu(){
		System.out.println("1.Move\n2.Surrender\n3.Exit");
		System.out.print("Select: ");
		int selection = kb.nextInt();
		if (selection < 1 || selection > 3){
			System.out.println("Invalid option, choose again!\n");
			selection = menu();
		}
		return selection;
	}
}
