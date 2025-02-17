# AI HTTP ANALYZER: An AI-Powered Security Analysis Assistant for Burp Suite

AI HTTP ANALYZER revolutionizes web application security testing by bringing artificial intelligence capabilities to Burp Suite. This innovative extension harnesses the power of AI to automate vulnerability detection, provide intelligent analysis, and assist security professionals in identifying complex security issues. Whether you're a penetration tester, security researcher, or web security enthusiast, AI HTTP ANALYZER enhances your workflow with smart, context-aware security analysis and real-time vulnerability assessments.

## Overview

AI HTTP ANALYZER is an advanced security analysis assistant integrated into Burp Suite. It examines HTTP requests and responses for potential security vulnerabilities such as SQL injection, XSS, CSRF, and other threats. The extension provides focused technical analysis, including quick identification of detected vulnerabilities, clear technical steps for exploitation, and PoC examples and payloads where applicable.

## Features

- 🔍 **Analyze HTTP requests and responses for security vulnerabilities**
- 🛠️ **Provide technical analysis and exploitation steps**
- 📄 **Include PoC examples and payloads**
- 🖥️ **Integrate with Burp Suite's UI and context menu**
- 🚀 **Real-time vulnerability assessments**
- 🤖 **AI-powered context-aware analysis**

## Installation

1. Clone the repository:

    ```sh
    git clone https://github.com/alpernae/AIHTTPAnalyzer.git
    ```

2. Navigate to the project directory:

    ```sh
    cd AIHTTPAnalyzer
    ```

3. Build the project using Gradle:

    ```sh
    ./gradlew build
    ```

4. Locate the generated JAR file in the `build/libs` directory.

5. Open Burp Suite and go to the `Extender` tab.

6. Click on the `Add` button and select the generated JAR file.

## Usage

1. Once the extension is loaded, you will see a new tab named `AIHTTPAnalyzer` in Burp Suite.

2. You can analyze HTTP requests and responses by selecting them and using the context menu option `Send to AIHTTPAnalyzer`.

3. In the `AIHTTPAnalyzer` tab, you can:
   - Use checkboxes to include/exclude the request and response in your analysis
   - Enter custom prompts in the text field for specific analysis requirements

4. Custom Prompt Examples:
   ```
   "Check for IDOR vulnerabilities in this endpoint"
   "Analyze the authentication mechanism in this request"
   "Suggest possible SQL injection points in this request"
   "Generate bypass payloads for the WAF patterns in this response"
   ```

5. Prompt Best Practices:
   - Be specific about what you want to analyze
   - Include the type of vulnerability you're looking for
   - Ask for specific payload suggestions when needed
   - Request exploitation steps if applicable

6. The AI will analyze:
   - The selected request/response (if checked)
   - Your custom prompt
   - The context of the HTTP interaction
   
7. Click the `Analyze with AIHTTPAnalyzer` button to send the prompt and view the results.

## Extensibility with AI

AI-powered extensibility opens up new possibilities for solving challenges that were previously difficult or even impossible with traditional code alone. Now, you can leverage AI to enhance security testing, automate tedious tasks, and gain deeper insights into web application vulnerabilities.

## Version

**2025.1.0**

## Author

**ALPEREN ERGEL (@alpernae)**

## License

This project is licensed under the MIT License. You may use, modify, and distribute this code under the terms of the MIT License. For more details, see the [LICENSE](LICENSE) file.