package rus.proj_zero;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp;
import akka.io.Tcp.CommandFailed;
import akka.io.Tcp.Connected;
import akka.io.Tcp.ConnectionClosed;
import akka.io.Tcp.Event;
import akka.io.Tcp.Received;
import akka.io.TcpMessage;
import akka.japi.Procedure;
import akka.util.ByteString;

public class Client extends UntypedActor {
	final InetSocketAddress remote;
	final LoggingAdapter log = Logging
		      .getLogger(getContext().system(), getSelf());

	// final ActorRef listener;
	private String STRING_PATH;
	private Path PATH;
	private long OFFSET;
	private long CURRENT_OFFSET;
	private int MESSAGE_NUMBER;
	ActorRef connection;
	boolean transmissionHasBegun = false;

	Commands cmd = new Commands();

	private final Event ACK = new Event() {};
	
	public Client(InetSocketAddress remote) {

		this.remote = remote;
		this.STRING_PATH = "/home/nex/Actor_receiver/IMG.JPG";
		this.PATH = Paths.get(STRING_PATH);

		// this.listener = listener;
		final ActorRef tcp = Tcp.get(getContext().system()).manager();
		tcp.tell(TcpMessage.connect(remote), getSelf());
	}

	class Commands {
		String readyToReceive = "READY_TO_RECEIVE";
		String readyToSend = "READY_TO_SEND";
		ByteString rdToSnd = ByteString.fromString(readyToSend);
		ByteString rdToRcv = ByteString.fromString(readyToReceive);
	}

	@Override
	public void onReceive(Object message) throws Exception {
		 System.out.println("CLIENT: " + message);
		if (message instanceof Received) {
			System.out.println("CLIENT.Received:  " + message + " ! "
					+ getSender().toString());
			
			
		} else if (message instanceof ConnectionClosed) {
			System.out.println("CLIENT.ConnectionClosed:  " + message);

			getContext().stop(getSelf());
		}

		if (message instanceof Event){
			System.out.println("Event: " + ((Event)message).toString());
		}
		
		if (message instanceof CommandFailed) {
			System.out.println("CLIENT.CommandFailed:  " + message);
			// listener.tell("failed", getSelf());
			getContext().stop(getSelf());
		} else if (message instanceof Connected) {
			System.out.println("CLIENT.Connected:  " + message);
			connection = getSender();
			//getContext().become(connected(connection));
			connection.tell(TcpMessage.write(ByteString.fromString("I am client")), getSelf());
			
		}
		// getContext().actorSelection("akka.tcp://systemA@193.169.0.101:2552/user/SERVER").tell((String)"READY_TO_SEND",
		// getSelf());
	}

	private Procedure<Object> connected(final ActorRef connection) {
		return new Procedure<Object>() {
			public void apply(Object message) throws Exception {
				if (message instanceof ByteString) {
					System.out.println("CLIENT.Procedure.ByteString:  "
							+ message);

				} else if (message instanceof CommandFailed) {
					System.out.println("CLIENT.Procedure.CommandFailed:  "
							+ message);

				} else if (message instanceof Received) {
					System.out.println("CLIENT.Received:  " + message + " ! "
							+ getSender().toString());
			
					final ByteString data = ((Received) message).data();
					connection.tell(TcpMessage.write(data), getSender());
					// buffer = data;

					if (!transmissionHasBegun) {
						if (data.length() == cmd.rdToSnd.length()
								&& data.equals(cmd.rdToSnd)) {

							transmissionHasBegun = true;
							connection.tell(TcpMessage.write(cmd.rdToRcv),
									getSelf());
							System.out.println("CLIENT.Received: "
									+ cmd.readyToSend);

						}
					} else {
						try (FileChannel fileChannel = FileChannel.open(PATH,
								StandardOpenOption.CREATE,
								StandardOpenOption.APPEND)) {

							CURRENT_OFFSET = data.length();
							if (CURRENT_OFFSET > 0) {
								fileChannel.write(data.asByteBuffer(), OFFSET);
								OFFSET += CURRENT_OFFSET;

								MESSAGE_NUMBER++;
								
								System.out.println("HANDLER.Message_number:  "
										+ MESSAGE_NUMBER);//MESSAGE_NUMBER = 0;
								connection.tell(
										TcpMessage.write(ByteString
												.fromString("DELIVERED")),//(MESSAGE_NUMBER)),
										getSelf());
							}
							fileChannel.close();
						}
					}
				} else if (message instanceof ConnectionClosed) {
					getContext().stop(getSelf());

				}
			}
		};
	}
}
