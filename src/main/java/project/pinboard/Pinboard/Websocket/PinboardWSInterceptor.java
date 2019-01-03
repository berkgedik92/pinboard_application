package project.pinboard.Pinboard.Websocket;

import project.pinboard.Pinboard.Models.User.AdminUser;
import project.pinboard.Pinboard.Models.Pinboard;
import project.pinboard.Pinboard.Repository.PinboardRepository;
import project.pinboard.Services.TokenManager;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptorAdapter;

import java.util.Objects;

public class PinboardWSInterceptor extends ChannelInterceptorAdapter {

    private final TokenManager tokenManager;
    private final PinboardRepository pinboardRepository;

    public PinboardWSInterceptor(TokenManager tokenManager, PinboardRepository pinboardRepository) {
        this.tokenManager = tokenManager;
        this.pinboardRepository = pinboardRepository;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) throws IllegalArgumentException {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        String token;

        switch (Objects.requireNonNull(headerAccessor.getCommand())) {
            case CONNECT:
                token = Objects.requireNonNull(headerAccessor.getNativeHeader("token")).get(0);
                validateConnection(token);
                break;

            case SUBSCRIBE:
                token = Objects.requireNonNull(headerAccessor.getNativeHeader("token")).get(0);
                String destination = headerAccessor.getDestination();
                validateSubscription(token, destination);
                break;

            default: break;
        }
        return message;
    }

    //For a connection, user must have a valid token
    private void validateConnection(String token) throws IllegalArgumentException {
        try {
            tokenManager.check(token);
        }catch (Exception e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    //For subscription, user must have a valid token, token must be for this particular username
    //and this user must have access to the pinboard
    private void validateSubscription(String token, String destination) {
        AdminUser user;

        //
        try {
            user = tokenManager.check(token);
        }catch (Exception e) {
            throw new IllegalArgumentException(e.toString());
        }

        String[] parts = destination.split("/");

        //parts must have four parts : "", "wspinbsubs", pinboardID, userName, check if
        //this is the case
        if (parts.length != 4)
            throw new IllegalArgumentException("Invalid subscription URL");

        if (!parts[3].equals(user.getUsername()))
            throw new IllegalArgumentException("Username and token does not match");

        //Check if this user have access to this pinboard
        Pinboard p = pinboardRepository.findById(parts[2]).orElse(null);

        if (p == null)
            throw new IllegalArgumentException("There is no such pinboard");

        if (!p.getUsernames().contains(user.getUsername()))
            throw new IllegalArgumentException("This user does not have the right to subscribe for this pinboard");
    }
}
