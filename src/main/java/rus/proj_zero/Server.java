package rus.proj_zero;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.io.Tcp;
import akka.io.Tcp.Bound;
import akka.io.Tcp.CommandFailed;
import akka.io.Tcp.Connected;
import akka.io.Tcp.Event;
import akka.io.Tcp.Received;
import akka.io.TcpMessage;
import akka.util.ByteString;

public class Server extends UntypedActor {
	//final ActorRef manager;
	ActorRef connection;
	final Event ACK = new Event() {
	};
	//boolean transmissionHasBegun = false;
	//public Dispetcher disp = new Dispetcher(250000,"/home/nex/Actor_sender/IMG.JPG");
	Commands cmd = new Commands();

	class Commands {
		String readyToReceive = "READY_TO_RECEIVE";
		String readyToSend = "READY_TO_SEND";
		ByteString rdToSnd = ByteString.fromString(readyToSend);
		ByteString rdToRcv = ByteString.fromString(readyToReceive);
	}
	
	@Override
	public void preStart() throws Exception {
		final ActorRef tcp = Tcp.get(getContext().system()).manager();
		tcp.tell(TcpMessage.bind(getSelf(), new InetSocketAddress("127.0.0.1",// "193.169.0.101"
				2552), 100), getSelf());
		
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		if (msg instanceof ByteString) {
			//manager.tell(msg, getSelf());
			System.out.println("SERVER.ByteString:  " + msg);
		}
		if (msg instanceof Bound) {
			System.out.println("SERVER.Bound:  " + msg);

			// manager.tell(msg, getSelf());
		} else if (msg instanceof CommandFailed) {
			System.out.println("SERVER.CommandFailed:  " + msg);
			getContext().stop(getSelf());
		} else if (msg instanceof Connected) {
			System.out.println("SERVER.Connected:  " + msg);
			connection = getSender();
			
			final ActorRef handler = getContext().actorOf(
					Props.create(SimplisticHandler.class));
			getSender().tell(TcpMessage.register(handler), getSelf());
			TimeUnit.MILLISECONDS.sleep(100);
			connection.tell(TcpMessage.write(cmd.rdToSnd), getSelf());
		} else if (msg instanceof Received) {
			System.out.println("SERVER.Received:  " + msg);
		}
		if (msg instanceof ByteString) {
			System.out.println("SERVER.ByteString:  " + msg);
		}
//		if (msg instanceof Event) {
//			System.out.println("SERVER.Event:  " + msg);
//		}

	}

	}

