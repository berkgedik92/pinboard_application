package project.pinboard.Pinboard.Websocket;

import project.pinboard.Pinboard.Repository.PinboardRepository;
import project.pinboard.Services.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class PinboardWSConfig extends AbstractWebSocketMessageBrokerConfigurer {

    @Autowired private TokenManager tokenManager;
    @Autowired private PinboardRepository pinboardRepository;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.setInterceptors(new PinboardWSInterceptor(tokenManager, pinboardRepository));
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        /* Subscription URL: wspinbsubs/{pinID}/{userID} */
        config.enableSimpleBroker("/wspinbsubs/");

        /* Send URLs: no prefix... */
        config.setApplicationDestinationPrefixes("");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        /* Connection URL: /wspinbconn */
        registry.addEndpoint("/wspinbconn").withSockJS();
    }
}
