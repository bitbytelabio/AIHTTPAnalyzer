package ai;

import burp.api.montoya.ai.Ai;
import burp.api.montoya.ai.chat.PromptResponse;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.ui.UserInterface;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class BurpAITab {
    private final JPanel mainPanel;
    private final JTabbedPane tabbedPane;
    private final Map<Component, HttpRequestResponse> tabRequests;
    private int tabCounter = 1;
    private final Ai montoyaAi;
    private final Logging logging;
    private final MyPromptMessage myPromptMessage;
    private final ExecutorService executorService;
    private final UserInterface userInterface;

    public BurpAITab(UserInterface userInterface, Ai montoyaAi, Logging logging, MyPromptMessage myPromptMessage, ExecutorService executorService) {
        this.userInterface = userInterface;
        this.montoyaAi = montoyaAi;
        this.logging = logging;
        this.myPromptMessage = myPromptMessage;
        this.executorService = executorService;

        tabRequests = new HashMap<>();

        mainPanel = new JPanel(new BorderLayout());
        tabbedPane = new JTabbedPane();
        
        // Add initial empty tab
        createNewTab("BurpAI", null);
        
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
    }

    private Component createTabContent(HttpRequestResponse requestResponse) {
        JPanel tabPanel = new JPanel(new BorderLayout());
        
        // Create split panes for layout with specific orientation
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
        JSplitPane horizontalSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
        
        // Initialize editors using class field
        HttpRequestEditor requestEditor = this.userInterface.createHttpRequestEditor();
        HttpResponseEditor responseEditor = this.userInterface.createHttpResponseEditor();
        
        if (requestResponse != null) {
            requestEditor.setRequest(requestResponse.request());
            responseEditor.setResponse(requestResponse.response());
        }
        
        // Create AI response area with minimum size
        JEditorPane aiResponseArea = new JEditorPane();
        aiResponseArea.setContentType("text/html");
        aiResponseArea.setEditable(false);
        JScrollPane aiScrollPane = new JScrollPane(aiResponseArea);
        aiScrollPane.setPreferredSize(new Dimension(800, 200));
        
        // Add components to splits and set preferred sizes
        horizontalSplit.setLeftComponent(requestEditor.uiComponent());
        horizontalSplit.setRightComponent(responseEditor.uiComponent());
        horizontalSplit.setResizeWeight(0.5);
        
        verticalSplit.setTopComponent(horizontalSplit);
        verticalSplit.setBottomComponent(aiScrollPane);
        verticalSplit.setResizeWeight(0.7);
        
        // Set divider locations
        horizontalSplit.setDividerLocation(0.5);
        verticalSplit.setDividerLocation(0.7);
        
        tabPanel.add(verticalSplit, BorderLayout.CENTER);
        
        // Create a bottom panel with a FlowLayout and uniform spacing
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        // New checkbox
        JCheckBox includeReqResp = new JCheckBox("Send Request and Response");

        // Create a custom input field for additional user prompt
        JTextField customInputField = new JTextField(20);
        customInputField.setPreferredSize(new Dimension(200, 35));

        // Add analyze button with custom styling
        JButton analyzeButton = new JButton("Analyze with BurpAI");
        analyzeButton.setBackground(Color.decode("#ff6633"));
        analyzeButton.setForeground(Color.WHITE);
        analyzeButton.setFont(new Font("Segoe UI Emoji", Font.BOLD, 13));
        analyzeButton.setFocusPainted(false);
        analyzeButton.setBorderPainted(false);
        analyzeButton.setOpaque(true);
        
        // Make button thicker
        analyzeButton.setPreferredSize(new Dimension(analyzeButton.getPreferredSize().width, 35));
        analyzeButton.setMargin(new Insets(5, 10, 5, 10));
        
        // Add new checkbox, input field, and button
        bottomPanel.add(includeReqResp);
        bottomPanel.add(customInputField);
        bottomPanel.add(analyzeButton);
        tabPanel.add(bottomPanel, BorderLayout.SOUTH);

        // Modify button action to pass customInputField text
        analyzeButton.addActionListener(e -> {
            analyzeRequest(requestEditor,
                           responseEditor,
                           aiResponseArea,
                           includeReqResp.isSelected(),
                           customInputField.getText());
            // Clear the input field
            customInputField.setText("");
        });

        return tabPanel;
    }

    private Component createTabComponent(String title) {
        JPanel tabComponent = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        tabComponent.setOpaque(false);

        // Create label instead of text field
        JLabel titleLabel = new JLabel(title);
        titleLabel.setPreferredSize(new Dimension(70, 30));
        
        // Create text field for editing (initially invisible)
        JTextField titleField = new JTextField(title);
        titleField.setPreferredSize(new Dimension(100, 20));
        titleField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        titleField.setVisible(false);

        // Add mouse listener to the entire tab component for selection
        tabComponent.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Select this tab when clicked
                int index = tabbedPane.indexOfTabComponent(tabComponent);
                if (index != -1) {
                    tabbedPane.setSelectedIndex(index);
                }
            }
        });
        
        // Handle double click on label
        titleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    titleLabel.setVisible(false);
                    titleField.setText(titleLabel.getText());
                    titleField.setVisible(true);
                    titleField.requestFocus();
                } else if (e.getClickCount() == 1) {
                    // Select this tab on single click too
                    int index = tabbedPane.indexOfTabComponent(tabComponent);
                    if (index != -1) {
                        tabbedPane.setSelectedIndex(index);
                    }
                }
            }
        });
        
        // Handle editing complete
        titleField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                finishEditing(titleLabel, titleField);
            }
        });
        
        titleField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    finishEditing(titleLabel, titleField);
                }
            }
        });

        // Improved close button styling
        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font(closeButton.getFont().getName(), Font.PLAIN, 12));
        closeButton.setPreferredSize(new Dimension(12, 12));
        closeButton.setMargin(new Insets(0, 0, 0, 0));
        closeButton.setFocusable(false);
        closeButton.addActionListener(e -> {
            int index = tabbedPane.indexOfTabComponent(tabComponent);
            if (index != -1 && tabbedPane.getTabCount() > 1) { // Prevent closing last tab
                Component content = tabbedPane.getComponentAt(index);
                tabRequests.remove(content);
                tabbedPane.remove(index);
            }
        });

        tabComponent.add(titleLabel);
        tabComponent.add(titleField);
        tabComponent.add(closeButton);
        return tabComponent;
    }

    private void finishEditing(JLabel label, JTextField textField) {
        label.setText(textField.getText());
        label.setVisible(true);
        textField.setVisible(false);
    }

    private void createNewTab(String title, HttpRequestResponse requestResponse) {
        Component tabContent = createTabContent(requestResponse);
        tabbedPane.addTab(title, tabContent);
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, createTabComponent(title));
        
        if (requestResponse != null) {
            logging.logToOutput("Creating new tab with request: " + requestResponse.request().toString());
            tabRequests.put(tabContent, requestResponse);
        }
        
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }

    private void updateTabContent(Component tabContent, HttpRequestResponse requestResponse) {
        if (requestResponse == null) {
            logging.logToError("Request/Response is null");
            return;
        }

        logging.logToOutput("Updating tab content with request: " + requestResponse.request().toString());
        
        if (tabContent instanceof JPanel) {
            JSplitPane verticalSplit = (JSplitPane) ((JPanel) tabContent).getComponent(0);
            JSplitPane horizontalSplit = (JSplitPane) verticalSplit.getTopComponent();
            
            Component leftComponent = horizontalSplit.getLeftComponent();
            Component rightComponent = horizontalSplit.getRightComponent();
            
            if (leftComponent instanceof Component) {
                HttpRequestEditor reqEditor = (HttpRequestEditor) SwingUtilities.getAncestorOfClass(HttpRequestEditor.class, leftComponent);
                if (reqEditor != null) {
                    reqEditor.setRequest(requestResponse.request());
                }
            }
            
            if (rightComponent instanceof Component) {
                HttpResponseEditor respEditor = (HttpResponseEditor) SwingUtilities.getAncestorOfClass(HttpResponseEditor.class, rightComponent);
                if (respEditor != null) {
                    respEditor.setResponse(requestResponse.response());
                }
            }
        }
    }

    private void analyzeRequest(HttpRequestEditor requestEditor,
                                HttpResponseEditor responseEditor,
                                JEditorPane aiResponseArea,
                                boolean includeRequestResponse,
                                String customInput) {
        // Get the current tab's content and its associated request/response
        int selectedIndex = tabbedPane.getSelectedIndex();
        if (selectedIndex == -1) {
            aiResponseArea.setText("No tab selected.");
            return;
        }

        Component selectedTab = tabbedPane.getComponentAt(selectedIndex);
        HttpRequestResponse currentRequestResponse = tabRequests.get(selectedTab);
        
        HttpRequest request = currentRequestResponse != null ? currentRequestResponse.request() : null;
        HttpResponse response = currentRequestResponse != null ? currentRequestResponse.response() : null;

        String promptText = buildPromptText(includeRequestResponse, customInput, request, response);

        if (promptText == null) {
            aiResponseArea.setText("Empty custom prompt or HTTP request.");
            return;
        }

        aiResponseArea.setText("Analyzing request/response...");

        // Set AI response text to 12px
        aiResponseArea.setFont(new Font(aiResponseArea.getFont().getFamily(), Font.PLAIN, 12));

        // Execute the AI prompt in a separate thread
        executorService.execute(() -> {
            try {
                if (!montoyaAi.isEnabled()) {
                    throw new Exception("Burp AI features are not enabled");
                }

                // Create messages array with system + user prompt
                burp.api.montoya.ai.chat.Message[] messages = myPromptMessage.build(promptText);

                // Execute using messages array rather than raw string
                PromptResponse aiResponse = montoyaAi.prompt().execute(messages);

                String content = aiResponse.content();
                
                // Remove any backticks before converting to HTML
                if (content.contains("`")) {
                    content = content.replaceAll("`+", "");
                }
                
                // Convert Markdown to HTML
                Parser parser = Parser.builder().build();
                HtmlRenderer renderer = HtmlRenderer.builder().build();
                String htmlContent = renderer.render(parser.parse(content));
                
                logging.logToOutput("AI response received successfully");
                SwingUtilities.invokeLater(() ->
                    aiResponseArea.setText(htmlContent)
                );
            } catch (Exception error) {
                String errorDetails = "An unexpected error occurred: " + error.getMessage() + "\n\n" +
                        "Please check the Burp Suite extension logs for more details.";
                logging.logToError(errorDetails);
                SwingUtilities.invokeLater(() ->
                    aiResponseArea.setText(errorDetails)
                );
            }
        });
    }

    private static String buildPromptText(boolean includeRequestResponse, String customInput, HttpRequest request, HttpResponse response) {
        boolean analyzeRequest = includeRequestResponse && request != null;

        if (!analyzeRequest && customInput.isEmpty()) {
            return null;
        }

        // Build the prompt conditionally
        StringBuilder promptBuilder = new StringBuilder();

        if (analyzeRequest) {
            promptBuilder.append("Analyze this HTTP request");

            if (response != null) {
                promptBuilder.append(" and response");
            }

            promptBuilder
                    .append(" for security issues:\n")
                    .append("REQUEST:\n")
                    .append(request);

            if (response != null) {
                promptBuilder
                        .append("\n\nRESPONSE:\n")
                        .append(response);
            }

            promptBuilder.append("\n\n");
        }

        // Always append the custom prompt, regardless of checkbox
        promptBuilder.append(customInput);

        return promptBuilder.toString();
    }

    public void setRequestResponse(HttpRequestResponse requestResponse) {
        String tabTitle = "Request " + tabCounter++;
        createNewTab(tabTitle, requestResponse);
    }

    public Component getUiComponent() {
        return mainPanel;
    }
}
