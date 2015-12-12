package rus.proj_zero;
//import akka.zeromq.Bind;
import java.io.File;
import java.net.InetSocketAddress;

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

public class EchoClient extends UntypedActor {

	final InetSocketAddress remote;
	final String path = "/home/nex/Actor_receiver/IMG.JPG";
	// final ActorRef listener;

	public EchoClient(InetSocketAddress remote) {
		this.remote = remote;
		// this.listener = listener;

		final ActorRef tcp = Tcp.get(getContext().system()).manager();
		tcp.tell(TcpMessage.connect(remote), getSelf());
	}

	@Override
	public void onReceive(Object msg) throws Exception {

		if (msg instanceof CommandFailed) {
			// listener.tell("failed", getSelf());
			getContext().stop(getSelf());

		} else if (msg instanceof Connected) {
			final ActorRef handler = getContext().actorOf(
					Props.create(WriteHandler.class, getSender(), remote, path));
			getSender().tell(TcpMessage.register(handler, true, // <--
																// keepOpenOnPeerClosed
																// flag
					true), getSelf());
		}
	}
}
