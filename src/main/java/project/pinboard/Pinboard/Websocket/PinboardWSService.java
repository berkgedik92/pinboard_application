package project.pinboard.Pinboard.Websocket;

import project.pinboard.Pinboard.PostitWSMessages.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PinboardWSService {

    private final Map<String, List<String>> pinboardUsers = new HashMap<>();

    @Autowired private SimpMessageSendingOperations messagingTemplate;

    @SubscribeMapping("/wspinbsubs/{pinID}/{userID}")
    public void subscribe(@DestinationVariable String userID,
                          @DestinationVariable String pinID) {

        List<String> users;

        synchronized (pinboardUsers) {
            if (pinboardUsers.containsKey(pinID))
                users = pinboardUsers.get(pinID);
            else
                users = new ArrayList<>();

            users.add(userID);
            pinboardUsers.put(pinID, users);
        }
    }

    public void SendData(Action action, String sender, String pinboardID) {

        List<String> users;

        synchronized (pinboardUsers) {
            users = pinboardUsers.get(pinboardID);
            for (String user : users) {
                if (!user.equals(sender))
                    messagingTemplate.convertAndSend("/wspinbsubs/" + pinboardID + "/" + user, action);
            }
        }
    }
}
