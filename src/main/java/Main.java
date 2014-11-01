import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.pattern.Patterns;
import akka.util.Timeout;
import akkatemplate.actors.SimpleTestActor;
import akkatemplate.messages.DummyOutputReply;
import akkatemplate.messages.Messages;
import com.disbrain.dbmslayer.DbmsLayer;
import com.disbrain.dbmslayer.DbmsLayerProvider;
import com.disbrain.dbmslayer.descriptors.RequestModes;
import com.disbrain.dbmslayer.exceptions.DbmsException;
import com.disbrain.dbmslayer.messages.QueryRequest;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws Exception {

        Future<Object> generic_reply;
        Object generic_response;
        ArrayList<Future<Object>> output_storage = new ArrayList<Future<Object>>();
        int storage_size;
        Config user_cfg = ConfigFactory.load();
        ActorSystem your_app_actorsystem = akka.actor.ActorSystem.create("YourAppAS", user_cfg.getConfig("YourAppConfig"));

        DbmsLayerProvider db_layer = DbmsLayer.DbmsLayerProvider.get(your_app_actorsystem);

        ActorRef actor = your_app_actorsystem.actorOf(Props.create(SimpleTestActor.class));


        /* Here we try a simple request to the dbs trough a future */


        generic_reply = Patterns.ask(db_layer.getQueriesBroker(), //is outside from an actor context, we use this broker to communicate with dbms querying system
                new QueryRequest("SELECT COUNT(*) FROM Activities;", //SQL query
                        new RequestModes(RequestModes.RequestTypology.READ_ONLY), //Query typology
                        DummyOutputReply.class, //Output object. We use its constructor to properly decode output
                        true //autocommit?
                ),
                4096000);
        output_storage.add(generic_reply);

        generic_reply = Patterns.ask(db_layer.getQueriesBroker(),
                new QueryRequest("INSERT INTO Tags(Tag_Name) VALUES (?);", //SQL query
                        new RequestModes(RequestModes.RequestTypology.WRITE),
                        DummyOutputReply.class,
                        true,
                        new Object[]{"Computer Science"}
                ),
                4096000);
        output_storage.add(generic_reply);

        generic_reply = Patterns.ask(db_layer.getQueriesBroker(),
                new QueryRequest("INSERT INTO Languages(Lang_Name) VALUES (\"DE\");", //SQL query
                        new RequestModes(RequestModes.RequestTypology.WRITE),
                        DummyOutputReply.class,
                        false //automatic rollback on close wo commit
                ),
                4096000);
        output_storage.add(generic_reply);


        generic_reply = Patterns.ask(db_layer.getQueriesBroker(),
                new QueryRequest("SELECT GET_LOCK(321,-1);", //SQL query
                        new RequestModes(RequestModes.RequestTypology.READ_WRITE,
                                RequestModes.RequestBehaviour.RESOURCE_GETTER //locking operation, use dedicate dispatcher
                        ),
                        DummyOutputReply.class,
                        false
                ),
                4096000);
        output_storage.add(generic_reply);

        generic_reply = Patterns.ask(db_layer.getQueriesBroker(),
                new QueryRequest("SELECT RELEASE_LOCK(321);", //SQL query
                        new RequestModes(RequestModes.RequestTypology.READ_WRITE,
                                RequestModes.RequestBehaviour.RESOURCE_RELEASER //unlocking operation, use resource releasing dedicate dispatcher
                        ),
                        DummyOutputReply.class,
                        false
                ),
                4096000);
        output_storage.add(generic_reply);


        // To exploit the full power and flexibility of the dbms layer we must use if from within an actor 
        generic_reply = Patterns.ask(actor, Messages.TestRequest.newBuilder().setMessage("Who's there?").build(), 4096000);

        output_storage.add(generic_reply);


        storage_size = output_storage.size();

        for (int cur_elem = 0; cur_elem < storage_size; cur_elem++) {
            generic_response = Await.result(output_storage.remove(0), (new Timeout(Duration.create(4096, "seconds"))).duration());

            System.out.println(String.format("Query %d result: ", cur_elem));

            if (generic_response instanceof DummyOutputReply) {
                DummyOutputReply reply = (DummyOutputReply) generic_response;
                if (reply.output != null)
                    for (Object element : reply.output) {
                        if (element != null)
                            System.out.print(element.toString() + " ");
                    }
                else
                    System.out.print("None!");
                System.out.println();
                continue;
            }
            if (generic_response instanceof DbmsException) {
                DbmsException error = (DbmsException) generic_response;
                System.err.println("Unexpected Return Code: " + ((DbmsException) generic_response).getErrorCode() + " Message: " + error.getRealMessage());
                continue;
            }
            if (generic_response instanceof Messages.TestReply) {

                Messages.TestReply reply = (Messages.TestReply) generic_response;
                System.out.println("Return code for complex actor-based task is: " + reply.getReturnCode() + "\nData found: ");
                if (reply.getReturnCode() == 0)
                    for (Long element : reply.getOutDataList())
                        System.out.println(String.format("\t%d", element));
                else
                    System.out.println("Error found: " + reply.getReturnMsg());
                continue;
            }
            System.err.println(generic_response.getClass().getName());
        }

        System.out.print("Simple test finished!\nPress return to exit:\\>");
        System.out.flush();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            br.readLine();
        } catch (Exception exc) {
            System.err.println("Crtitic! Shutdown and exiting");
            your_app_actorsystem.shutdown();
            System.exit(1234);
        }

        your_app_actorsystem.shutdown();

        System.out.println("Shutting down...");


        System.exit(0);

    }


}
