package ai;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BurpAIContextMenu implements ContextMenuItemsProvider {
    private final BurpAITab burpAITab;

    public BurpAIContextMenu(BurpAITab burpAITab) {
        this.burpAITab = burpAITab;
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        if (!event.isFromTool(ToolType.PROXY) && !event.isFromTool(ToolType.REPEATER) && !event.isFromTool(ToolType.TARGET)) {
            return null;
        }

        List<Component> menuItems = new ArrayList<>();
        JMenuItem sendToBurpAI = new JMenuItem("Send to BurpAI");
        sendToBurpAI.addActionListener(e -> {
            if (event.messageEditorRequestResponse().isPresent()) {
                HttpRequestResponse requestResponse = event.messageEditorRequestResponse().get().requestResponse();
                burpAITab.setRequestResponse(requestResponse);
            }
        });
        menuItems.add(sendToBurpAI);
        return menuItems;
    }
}
