package io.javabrains.tinder_ai_backend.conversations;

import io.javabrains.tinder_ai_backend.profiles.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@RestController
public class ConversationController {

    private final ConversationRepository conversationRepository;

    private final ProfileRepository profileRepository;

    public ConversationController(ConversationRepository conversationRepository, ProfileRepository profileRepository) {
        this.conversationRepository = conversationRepository;
        this.profileRepository = profileRepository;
    }

    @PostMapping("/conversations")
    public Conversation createNewConversation(@RequestBody CreateConversationRequest request) {
        profileRepository.findById(request.profileId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "unable to find profile with the ID" + request.profileId()));
        Conversation conversation = new Conversation(
                UUID.randomUUID().toString(),
                request.profileId,
                new ArrayList<>()
        );
        conversationRepository.save(conversation);
        return conversation;
    }


    @GetMapping("/conversations/{conversationId}")
    public Conversation getConversation(@PathVariable String conversationId) {
        return conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "unable to find conversation with the ID" + conversationId));
    }


    @PostMapping("/conversations/{conversationId}")
    public Conversation addMessageToConversation(@PathVariable String conversationId,
                                                 @RequestBody ChatMessage chatMessage
    ) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "unable to find conversation with the ID" + conversationId));

        profileRepository.findById(chatMessage.authorId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "unable to find profile with the ID" + chatMessage.authorId()));

        // TODO : Need to validate that the author of a message happens to be only the profile associated with the conversation

        ChatMessage messageWithTime = new ChatMessage(
                chatMessage.messageText(),
                chatMessage.authorId(),
                LocalDateTime.now()
        );
        conversation.messages().add(messageWithTime);
        conversationRepository.save(conversation);
        return conversation;
    }

    public record CreateConversationRequest(
            String profileId
    ) {
    }

}
