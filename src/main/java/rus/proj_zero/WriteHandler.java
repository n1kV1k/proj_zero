package rus.proj_zero;

/**
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.Queue;

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
public class WriteHandler extends UntypedActor {

	final LoggingAdapter log = Logging.getLogger(getContext().system(),
			getSelf());

	final ActorRef connection;
	final InetSocketAddress remote;

	private Path PATH;
	private long OFFSET;
	private long CURRENT_OFFSET;
	boolean transmissionHasBegun = false;
	String readyToReceive = "READY_TO_RECEIVE";
	String readyToSend = "READY_TO_SEND";
	ByteString rdToSnd = ByteString.fromString(readyToSend);
	ByteString rdToRcv = ByteString.fromString(readyToReceive);

	private final Event ACK = new Event() {
	};

	public WriteHandler(ActorRef connection, InetSocketAddress remote, String STRING_PATH) {
		this.connection = connection;
		this.remote = remote;
		this.PATH = Paths.get(STRING_PATH);
		File f = new File(STRING_PATH);
		//f.createNewFile();
		getContext().watch(connection);
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		// System.out.println("cl " + msg);
		if (msg instanceof Received) {
			if (((Received) msg).data().equals(rdToSnd)) {
				getContext().become(writing, false);
				connection.tell(rdToRcv, getSelf());
			}

		} else if (msg instanceof ConnectionClosed) {
			getContext().stop(getSelf());
		}
	}

	int i, j = 0;
	private final Procedure<Object> writing = new Procedure<Object>() {
		//@Override
		public void apply(Object msg) throws Exception {
			if (msg instanceof Received) {
				write(((Received) msg).data());

			} else if (msg instanceof ConnectionClosed) {
				if (((ConnectionClosed) msg).isPeerClosed()) {
				} else {
					// could also be ErrorClosed, in which case we just give up
					getContext().stop(getSelf());
				}
			}
		}
	};

	public void postStop() {
		log.info("transferred {} bytes from/to [{}]", transferred, remote);
	}

	private long transferred;

	protected void write(ByteString data) {
		try (FileChannel fileChannel = FileChannel.open(PATH,
				StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

			CURRENT_OFFSET = data.length();
			if (CURRENT_OFFSET > 0) {
				fileChannel.write(data.asByteBuffer(), OFFSET);
				OFFSET += CURRENT_OFFSET;
				// MESSAGE_NUMBER++;
			}
			fileChannel.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
// #simple-echo-handler