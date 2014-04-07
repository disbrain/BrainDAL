package akkatemplate.actors;

import akka.actor.Props;
import akka.actor.UntypedActor;

public class SimpleTestActor extends UntypedActor {

    @Override
    public void onReceive(Object message) {
        getContext().actorOf(Props.create(SimpleTestWorker.class, getSender(), message));
    }
}
