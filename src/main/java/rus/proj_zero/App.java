package rus.proj_zero;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

//import org.zeromq.ZMQ.Context;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.AddressFromURIString;
import akka.actor.Deploy;
import akka.actor.Props;
import akka.actor.Scope;

/**
 * Hello world!
 *
 */

class SystemConfiguration {
	private Path FILE_PATH;
	private int CHUNK_SIZE;
	private Config config;
	private String configName;
	 
	public SystemConfiguration(String configName, String FILE_PATH,
			int CHUNK_SIZE) {

		this.config = ConfigFactory.load();
		this.configName = configName;
		this.FILE_PATH = Paths.get(FILE_PATH);
		this.CHUNK_SIZE = CHUNK_SIZE;
	}

	public SystemConfiguration(String configName) {

		this.config = ConfigFactory.load();
		this.configName = configName;
	}

	public Config getConfig() {
		return config.getConfig(configName).withFallback(config);
	}

	public Path getFilePath() {
		return FILE_PATH;
	}

	public int getChunkSize() {
		return CHUNK_SIZE;
	}

	public String getConfigName() {
		return configName;
	}
}

public class App {

	static SystemConfiguration confA = new SystemConfiguration(
			"akka_remote_tcp", "/home/nex/Actor_sender/Gran.mkv", 250000);
	// /home/nex/Actor_sender/Gran.mkv
//	static SystemConfiguration confB = new SystemConfiguration(
//			"akka_remote_tcp", "/home/nex/Actor_receiver/Gran.mkv", 250000);

	static ActorSystem system = ActorSystem.create("systemA", confA.getConfig());
	// static Config regularConfig = ConfigFactory.load();
	//
	// static ActorSystem system = ActorSystem.create("learning2hard",
	// regularConfig.getConfig("akka_remote_tcp").withFallback(regularConfig));
	//
//	public static final ActorRef actor1 = system.actorOf(
//			Props.create(Actor_sender.class, confA), "actorA");
//	public static final ActorRef actor2 = system.actorOf(
//			Props.create(Actor_receiver.class, confB), "actorB");

	//
	// public static void send(ActorRef sender, ActorRef receiver, int
	// chunk_size)
	// throws Exception {
	// receiver.tell("READY_TO_SEND" + chunk_size, sender);
	// }

	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception {

		//system.actorSelection("akka://systemA/user/actorB").tell(
			//	"READY_TO_SEND", actor1);
		InetSocketAddress ip = InetSocketAddress.createUnresolved("127.0.0.1", 2552);//"193.169.0.101"
		
		//ActorRef client = system.actorOf(Props.create(Client.class, ip), "CLIENT");
		
		
		//ActorRef server = system.actorOf(Props.create(Server.class), "SERVER");
		
		ActorRef S = system.actorOf(Props.create(ZMQManager.class, SimpleEchoHandler.class), "S");
		
		//ActorRef SERVER = system.actorOf(Props.create(EchoManager.class, SimpleEchoHandler.class), "SERVER");
		
		//ActorRef CLIENT = system.actorOf(Props.create(EchoClient.class, ip), "CLIENT");
		
		TimeUnit.SECONDS.sleep(1);
		
		//system.actorSelection("akka.tcp://systemA@127.0.0.1:2552/user/SERVER").tell("READY_TO_SEND", client);
		TimeUnit.SECONDS.sleep(300);
		
		 system.shutdown();
	}
}
