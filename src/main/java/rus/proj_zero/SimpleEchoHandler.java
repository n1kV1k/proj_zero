package rus.proj_zero;

/**
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.io.Tcp.ConnectionClosed;
import akka.io.Tcp.Event;
import akka.io.Tcp.Received;
import akka.io.TcpMessage;
import akka.japi.Procedure;
import akka.util.ByteString;

//#simple-echo-handler
public class SimpleEchoHandler extends UntypedActor {

	final LoggingAdapter log = Logging.getLogger(getContext().system(),
			getSelf());

	final ActorRef connection;
	final InetSocketAddress remote;
	private int MESSAGE_NUMBER;
	private int ACK_NUMBER;
	private long OFFSET;
	private int CHUNK_SIZE = 2500000;
	private int CURRENT_CHUNK_SIZE;
	private Path PATH;
	private String STRING_PATH;
	private long FILE_LENGTH;

	String readyToReceive = "READY_TO_RECEIVE";
	String readyToSend = "READY_TO_SEND";
	ByteString rdToSnd = ByteString.fromString(readyToSend);
	ByteString rdToRcv = ByteString.fromString(readyToReceive);

	public SimpleEchoHandler(ActorRef connection, InetSocketAddress remote,
			String STRING_PATH) {
		this.connection = connection;
		this.remote = remote;
		this.MESSAGE_NUMBER = 0;
		this.ACK_NUMBER = 0;
		this.OFFSET = 0;
		// this.CHUNK_SIZE = CHUNK_SIZE;
		this.CURRENT_CHUNK_SIZE = this.CHUNK_SIZE;

		this.STRING_PATH = STRING_PATH;
		this.PATH = Paths.get(this.STRING_PATH);
		this.FILE_LENGTH = PATH.toFile().length();
		// sign death pact: this actor stops when the connection is closed
		getContext().watch(connection);
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof ByteString) {
			if (msg.equals(ByteString.fromString("START"))) {
				getContext().become(buffering, false);
				connection.tell(TcpMessage.write(rdToSnd, ACK), getSelf());
			}
		}

		if (msg instanceof Received) {

		} else if (msg instanceof ConnectionClosed) {
			getContext().stop(getSelf());
		}
	}

	private final Procedure<Object> buffering = new Procedure<Object>() {
		public void apply(Object msg) throws Exception {
			if (msg instanceof Received) {
				if (((Received) msg).data().equals(rdToRcv)) {
					transmission();
				}
			} else if (msg == ACK) {
				System.out.println("MESSAGE_NUMBER: " + MESSAGE_NUMBER);
				System.out.println("ACK_NUMBER: " + ACK_NUMBER++ );
				transmission();

			} else if (msg instanceof ConnectionClosed) {
				if (((ConnectionClosed) msg).isPeerClosed()) {
				} else {
					// could also be ErrorClosed, in which case we just give up
					getContext().stop(getSelf());
				}
			}
		}
	};

	// #storage-omitted
	public void postStop() {
		log.info("transferred {} bytes from/to [{}]", transferred, remote);
	}

	private long transferred;
	private final Event ACK = new Event() {
	};

	public boolean transmission() {

		if (OFFSET < FILE_LENGTH) {

			if (FILE_LENGTH < (OFFSET + CHUNK_SIZE)) {
				CURRENT_CHUNK_SIZE = (int) (FILE_LENGTH - OFFSET);
			}

			if (CURRENT_CHUNK_SIZE > 0) {

				connection.tell(TcpMessage.writeFile(STRING_PATH, OFFSET,
						CURRENT_CHUNK_SIZE, ACK), getSelf());
				MESSAGE_NUMBER++;
			}
			OFFSET += CURRENT_CHUNK_SIZE;
			return true;
		}
		return false;
	}

}
// #simple-echo-handler