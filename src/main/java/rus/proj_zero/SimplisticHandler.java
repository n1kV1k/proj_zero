package rus.proj_zero;

import java.nio.file.Path;
import java.nio.file.Paths;

//import rus.proj_zero.Server.Dispetcher;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.io.Tcp.ConnectionClosed;
import akka.io.Tcp.Event;
import akka.io.Tcp.Received;
import akka.io.TcpMessage;
import akka.util.ByteString;

public class SimplisticHandler extends UntypedActor {
	// public ByteString buffer = ByteString.empty();
	// private String PATH = "C:\\";

	final Event ACK = new Event() {
	};

	Dispetcher disp;

	ActorRef connection;
	private int CHUNK_SIZE = 250000;
	// private String PATH;

	boolean transmissionHasBegun = false;

	class Commands {
		String readyToReceive = "READY_TO_RECEIVE";
		String readyToSend = "READY_TO_SEND";
		ByteString rdToSnd = ByteString.fromString(readyToSend);
		ByteString rdToRcv = ByteString.fromString(readyToReceive);
	}

	// Dispetcher disp = new
	// Dispetcher(CHUNK_SIZE,"/home/nex/Actor_sender/IMG.JPG");
	Commands cmd = new Commands();

	public SimplisticHandler() {
		this.disp = new Dispetcher(CHUNK_SIZE, "/home/nex/Actor_sender/IMG.JPG");
		this.cmd = new Commands();
		//System.out.println("Handler complete");
	}

	@Override
	public void onReceive(Object message) throws Exception {
		// System.out.println("HANDLER: " + message);
		if (message instanceof ByteString) {
			System.out.println("HANDLER.ByteString:  " + message);
		}

		if (message instanceof Received) {
			connection = getSender();
			System.out.println("HANDLER.Received:  " + message);
			final ByteString data = ((Received) message).data();

			if (!transmissionHasBegun) {
				if (data.length() == cmd.rdToRcv.length()
						&& data.equals(cmd.rdToRcv)) {

					transmissionHasBegun = true;

					System.out.println("HANDLER.Received: "
							+ cmd.readyToReceive);

					transmission(false);
				}
			} else {
				transmission(false);
				// TimeUnit.MILLISECONDS.sleep(100);
				// System.out.println(data);
				if (data.equals(ByteString.fromString("DELIVERED"))) {
					transmission(false);
					System.out.println("transmission(false)");
				} else {
					transmission(true);
					System.out.println("transmission(true)");
				}
			}
			// transmission(false);
			// TimeUnit.MILLISECONDS.sleep(100);
			// transmission(false);

		} else if (message.equals("close")) {
			getSender().tell(TcpMessage.close(), getSelf());

		} else if (message instanceof ConnectionClosed) {
			System.out.println("HANDLER.ConnectionClosed:  "
					+ message);
			getContext().stop(getSelf());

		}
	}

	public void transmission(boolean RESENDING) {

		if (disp.OFFSET < disp.FILE_LENGTH) {

			if (disp.FILE_LENGTH < (disp.OFFSET + disp.CHUNK_SIZE)) {
				disp.CURRENT_CHUNK_SIZE = (int) (disp.FILE_LENGTH - disp.OFFSET);
			}

			if (disp.CURRENT_CHUNK_SIZE > 0) {

				if (RESENDING) {
					connection.tell(
							TcpMessage.writeFile(disp.STRING_PATH,
									disp.PREVIOUS_OFFSET,
									disp.CURRENT_CHUNK_SIZE, ACK), getSelf());
				} else {
					connection.tell(TcpMessage.writeFile(disp.STRING_PATH,
							disp.OFFSET, disp.CURRENT_CHUNK_SIZE, ACK),
							getSelf());
					disp.MESSAGE_NUMBER++;
				}
			}

			disp.PREVIOUS_OFFSET = disp.OFFSET;
			disp.OFFSET += disp.CURRENT_CHUNK_SIZE;

		}
	}

	class Dispetcher {
		int MESSAGE_NUMBER;
		private long OFFSET;
		private long PREVIOUS_OFFSET;
		private int CHUNK_SIZE;
		private int CURRENT_CHUNK_SIZE;
		private Path PATH;
		private String STRING_PATH;
		private long FILE_LENGTH;

		public Dispetcher(int CHUNK_SIZE, String STRING_PATH) {
			// getSender().tell(TcpMessage(Write()), sender);
			this.MESSAGE_NUMBER = 0;

			this.OFFSET = 0;
			this.PREVIOUS_OFFSET = 0;
			this.CHUNK_SIZE = CHUNK_SIZE;
			this.CURRENT_CHUNK_SIZE = this.CHUNK_SIZE;

			this.STRING_PATH = STRING_PATH;
			this.PATH = Paths.get(this.STRING_PATH);
			this.FILE_LENGTH = PATH.toFile().length();

			//System.out.println("Dispetcher complete");
		}
	}

}
