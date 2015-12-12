package rus.proj_zero;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.UntypedActor;
import akka.util.ByteString;

public class Actor_sender extends UntypedActor {

	private static SystemConfiguration conf;
	private static long OFFSET;
	private static long CURRENT_OFFSET;
	private static ActorRef receiver;

	public Actor_sender(SystemConfiguration conf) {
		this.conf = conf;
	}

	public Actor_sender() {
		System.out.println("ERROR:usage_of_empty_constructor");
	}

	@Override
	public void onReceive(Object message) throws Exception {
		
		
		//final ByteString data = ((Received) message).data();
		
		// TODO Auto-generated method stub
		receiver = getSender();
		// FILE_LENGTH = 9999999990l;
		if (message instanceof String) {
			String str = (String) message;
			System.out.println(getSender() + ": " + str);
			
			if (str.equals("OK")) {
				receiver.tell(PoisonPill.getInstance(), ActorRef.noSender());
				getContext().stop(getSelf());
			}
			
			if (str.equals("READY")) {
				try (FileChannel fileChannel = FileChannel.open(
						conf.getFilePath(), StandardOpenOption.READ)) {

					while (OFFSET < conf.getFilePath().toFile().length()
							&& !Thread.currentThread().isInterrupted()) {

						int currentChunkSize = conf.getChunkSize();

						if (conf.getFilePath().toFile().length() < OFFSET
								+ conf.getChunkSize()) {
							currentChunkSize = (int) (conf.getFilePath()
									.toFile().length() - OFFSET);
						}

						if (currentChunkSize > 0) {

							ByteBuffer byteBuffer = ByteBuffer
									.allocate(currentChunkSize);
							fileChannel.read(byteBuffer, OFFSET);
							byteBuffer.flip();

							receiver.tell(
									ByteString.fromByteBuffer(byteBuffer),
									getSelf());
							byteBuffer.clear();
							OFFSET += currentChunkSize;
						}
					}
					fileChannel.close();
				}
				receiver.tell("TRANSMISSION_COMPLETE", getSelf());
			}

			unhandled(message);
		}

	}
}


