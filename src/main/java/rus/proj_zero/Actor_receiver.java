package rus.proj_zero;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import akka.actor.UntypedActor;
import akka.util.ByteString;

public class Actor_receiver extends UntypedActor {
	
	SystemConfiguration conf;
	private static long OFFSET = 0;
	private static long CURRENT_OFFSET = 0;
	public static ByteString buffer = ByteString.empty();

	 public Actor_receiver(SystemConfiguration conf) {
	 this.conf = conf;
	 }
	
	 public Actor_receiver() {
	 System.out.println("ERROR:usage_of_empty_constructor");
	 }

	public void onReceive(Object message) throws Exception {
	
		if (message instanceof String) {
			String str = (String) message;
			{
				if (str.equals("READY_TO_SEND")) {
					getSender().tell("READY", getSelf());
				}

				if (str.equals("TRANSMISSION_COMPLETE")) {
					getSender().tell("OK", getSelf());
				}

			}
			System.out.println(getSender() + ": " + str);
		}

		if (message instanceof ByteString) {
			buffer = (ByteString) message;
			// FILE_PATH = Paths.get("Gran2.mpg");
			try (FileChannel fileChannel = FileChannel.open(conf.getFilePath(),
					StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {

				CURRENT_OFFSET = buffer.length();
				if (CURRENT_OFFSET > 0) {
					fileChannel.write(buffer.asByteBuffer(), OFFSET);
					OFFSET += CURRENT_OFFSET;
				}
				fileChannel.close();
			}
		}
		unhandled(message);
	}
}
