package com.hamza.fruitsappbackend.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamza.fruitsappbackend.modules.product.dto.ProductDTO;
import com.hamza.fruitsappbackend.security.JwtTokenProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
public class WebSocketProductHandler extends TextWebSocketHandler {
    private final JwtTokenProvider jwtTokenProvider;
    private final List<WebSocketSession> sessions = new ArrayList<>();
    private final ObjectMapper objectMapper;
    private static final Logger logger = LogManager.getLogger(WebSocketProductHandler.class);


    @Autowired
    public WebSocketProductHandler(JwtTokenProvider jwtTokenProvider, @Qualifier("objectMapper") ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        if (uri != null) {
            List<String> tokenParam = UriComponentsBuilder.fromUri(uri)
                    .build()
                    .getQueryParams()
                    .get("token");

            String token = (tokenParam != null && !tokenParam.isEmpty()) ? tokenParam.get(0) : null;

            if (token != null && !token.isEmpty()) {
                if (jwtTokenProvider.validateToken(token)) {
                    sessions.add(session);
                    logger.info("Token is valid. WebSocket session established with session ID: {}", session.getId());
                } else {
                    System.out.println("Invalid token. Closing WebSocket session.");
                    session.close(CloseStatus.NOT_ACCEPTABLE);
                }
            } else {
                logger.info("No token found in WebSocket request. Closing session.");
                session.close(CloseStatus.BAD_DATA);
            }
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        sessions.remove(session);
        logger.info("WebSocket session closed: {}", status);
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws JsonProcessingException {
        logger.info("Received message: {}", message.getPayload());
    }

    public void sendProductUpdate(ProductDTO productDTO) {
        logger.info("Sending ProductDTO: {}", productDTO);

        TextMessage message = createWebSocketMessage(productDTO);
        List<WebSocketSession> closedSessions = new ArrayList<>();

        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(message);
            } catch (IOException e) {
                closedSessions.add(session);
            }
        }

        sessions.removeAll(closedSessions);
    }

    private TextMessage createWebSocketMessage(ProductDTO productDTO) {
        try {
            String payload = objectMapper.writeValueAsString(productDTO);
            return new TextMessage(payload);
        } catch (IOException e) {
            throw new RuntimeException("Failed to convert product to JSON", e);
        }
    }
}
