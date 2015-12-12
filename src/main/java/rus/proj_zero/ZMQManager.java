package rus.proj_zero;

/**
 * Copyright (C) 2009-2014 Typesafe Inc. <http://www.typesafe.com>
 */

import java.net.InetSocketAddress;

import org.zeromq.*;

import akka.actor.ActorRef;
import akka.actor.IO.Connected;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
//import akka.io.Tcp;
//import akka.io.Tcp.Bind;
//import akka.io.Tcp.Bound;
//import akka.io.Tcp.CommandFailed;
//import akka.io.Tcp.Connected;
//import akka.io.TcpMessage;
import akka.util.ByteString;
import akka.zeromq.SocketOption;
import akka.zeromq.Bind;
import akka.zeromq.Identity;
import akka.zeromq.Listener;
import akka.zeromq.ZeroMQExtension;
import akka.zeromq.ZeroMQVersion;
//import scala.concurrent.duration.Duration;

public class ZMQManager extends UntypedActor {

	
	//System.out.println();
	ActorRef dealer = ZeroMQExtension.get(getContext().system()).newPubSocket(new Bind("tcp://127.0.0.1:2552"));
	
	
//	ActorRef dealer = ZeroMQExtension.get(getContext().system())
//			.newDealerSocket(
//					new SocketOption[] {new Listener(getSelf()),new Bind("tcp://127.0.0.1:2552"),
//							new Identity("S".getBytes()) });

	final LoggingAdapter log = Logging.getLogger(getContext().system(),
			getSelf());

	final Class<?> handlerClass;

	public ZMQManager(Class<?> handlerClass) {
		this.handlerClass = handlerClass;
		System.out.println(ZeroMQExtension.class.toString());
		//System.out.println(ZeroMQLibrary.OPTION_TYPE_MAPPER);
	}

	

	@Override
	public void preStart() {
		// #manager
		//final ActorRef tcpManager = Tcp.get(getContext().system()).manager();
		// #manager
//		tcpManager.tell(TcpMessage.bind(getSelf(), new InetSocketAddress(
//				"127.0.0.1", 2552), 100), getSelf());
//	
	
	}

	@Override
	public void postRestart(Throwable arg0) {
		// do not restart
		getContext().stop(getSelf());
	}

	@Override
	public void onReceive(Object msg) throws Exception {
		System.out.println("EchoMan: " + msg);
		
		 

		}
	}



