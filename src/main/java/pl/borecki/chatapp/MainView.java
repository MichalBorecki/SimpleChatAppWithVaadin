package pl.borecki.chatapp;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

import java.time.format.DateTimeFormatter;

@Route("chat")
@Push
@CssImport("./styles/style.css")
@PWA(name = "SimplyChatApp", shortName = "Chat")
public class MainView extends VerticalLayout {

    private String username;
    private UnicastProcessor<ChatMessage> publisher;
    private Flux<ChatMessage> messages;

    public MainView(UnicastProcessor<ChatMessage> publisher, Flux<ChatMessage> messages) {
        this.publisher = publisher;
        this.messages = messages;
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        setSizeFull();
        addClassName("main-view");

        H1 header = new H1("Simple Chat App");
        header.getElement().getThemeList().add("dark");

        add(header);

        askUsername();
    }

    private void askUsername() {
        HorizontalLayout usernameLayout = new HorizontalLayout();

        TextField usernameField = new TextField();
        usernameField.setPlaceholder("Enter name");

        Button startButton = new Button("Start chatting");
        usernameLayout.add(usernameField, startButton);
        add(usernameLayout);

        startButton.addClickListener(click -> {
            username = usernameField.getValue();
            remove(usernameLayout);
            showChat();
        });

    }

    private void showChat() {
        MessageList messageList = new MessageList();
        messageList.setClassName("message-list");

        add(messageList, createInputLayout());

        messages.subscribe(m -> {
            getUI().ifPresent(ui ->
                    ui.access(() ->
                            messageList.add(new Paragraph(
                                    m.getFrom()
                                            + " ("
                                            + m.getTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                                            + "): "
                                            + m.getMessage())
                            )));
        });

        expand(messageList);
    }

    private Component createInputLayout() {
        HorizontalLayout inputLayout = new HorizontalLayout();
        inputLayout.setWidth("100%");

        TextField messageFiled = new TextField();
        messageFiled.setPlaceholder("Enter message..");

        Button sendMessageButton = new Button("Send");
        sendMessageButton.getElement().getThemeList().add("primary");

        sendMessageButton.addClickListener(click -> {
            publisher.onNext((new ChatMessage(username, messageFiled.getValue())));
            messageFiled.clear();
            messageFiled.focus();
        });
        messageFiled.focus();

        inputLayout.add(messageFiled, sendMessageButton);
        inputLayout.expand(messageFiled);

        return inputLayout;
    }
}
