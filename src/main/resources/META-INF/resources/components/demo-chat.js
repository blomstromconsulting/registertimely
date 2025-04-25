import {LitElement} from 'lit';
import '@vaadin/icon';
import '@vaadin/button';
import '@vaadin/text-field';
import '@vaadin/text-area';
import '@vaadin/form-layout';
import '@vaadin/progress-bar';
import '@vaadin/checkbox';
import '@vaadin/horizontal-layout';
import '@vaadin/grid';
import '@vaadin/grid/vaadin-grid-sort-column.js';
import {marked} from 'https://cdn.jsdelivr.net/npm/marked/lib/marked.esm.js';

export class DemoChat extends LitElement {

    _stripHtml(html) {
        const div = document.createElement("div");
        div.innerHTML = html;
        return div.textContent || div.innerText || "";
    }

    _renderMarkdown(text) {
        if (!text) return '';
        // Set options for marked to ensure proper rendering
        marked.setOptions({
            breaks: true,    // Convert line breaks to <br>
            gfm: true        // Enable GitHub Flavored Markdown
        });
        return marked.parse(text);
    }

    connectedCallback() {
        const chatBot = document.getElementsByTagName("chat-bot")[0];

        const protocol = (window.location.protocol === 'https:') ? 'wss' : 'ws';
        const socket = new WebSocket(protocol + '://' + window.location.host + '/timereport-agent');

        const that = this;
        socket.onmessage = function (event) {
            chatBot.hideLastLoading();
            // LLM response
            let lastMessage;
            if (chatBot.messages.length > 0) {
                lastMessage = chatBot.messages[chatBot.messages.length - 1];
            }
            if (lastMessage && lastMessage.sender.name === "Bot" && !lastMessage.loading) {
                if (!lastMessage.msg) {
                    lastMessage.msg = "";
                }
                lastMessage.msg += event.data;
                let bubbles = chatBot.shadowRoot.querySelectorAll("chat-bubble");
                let bubble = bubbles.item(bubbles.length - 1);
                if (lastMessage.message) {
                    bubble.innerHTML = that._renderMarkdown(lastMessage.message + lastMessage.msg);
                } else {
                    bubble.innerHTML = that._renderMarkdown(lastMessage.msg);
                }
                chatBot.body.scrollTo({top: chatBot.body.scrollHeight, behavior: 'smooth'})
            } else {
                // For the initial message, we need to create a new message and set its HTML content directly
                chatBot.sendMessage("", {
                    right: false,
                    sender: {
                        name: "Bot"
                    }
                });

                // Now get the bubble we just created and set its innerHTML
                let bubbles = chatBot.shadowRoot.querySelectorAll("chat-bubble");
                let bubble = bubbles.item(bubbles.length - 1);
                bubble.innerHTML = that._renderMarkdown(event.data);
            }
        }

        chatBot.addEventListener("sent", function (e) {
            if (e.detail.message.sender.name !== "Bot") {
                // User message - strip HTML but don't render markdown for outgoing messages
                const msg = that._stripHtml(e.detail.message.message);
                socket.send(msg);
                chatBot.sendMessage("", {
                    right: false,
                    loading: true
                });
            }
        });
    }


}

customElements.define('demo-chat', DemoChat);
