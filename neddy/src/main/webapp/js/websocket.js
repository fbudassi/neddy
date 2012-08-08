/**
 * Neddy 1.0.0
 *
 * Websocket communication functions.
 *
 * Author: fbudassi
 */

var socket;

/**
 * Open a websocket to the corresponding wsURL.
 */
function openSocket(wsURL, onOpen, onClose, onMessage) {
    //Open a Websocket to the server.
    if (!window.WebSocket) {
        window.WebSocket = window.MozWebSocket;
    }
    if (window.WebSocket) {
        socket = new WebSocket(wsURL);
        socket.onmessage = onMessage;
        socket.onopen =  onOpen;
        socket.onclose = onClose;
    } else {
        showError("Your browser does not support Web Socket.");
    }
}


/**
 * General Websocket send function, with some socket checks and json conversion to string.
 */
function sendJson(json) {
    send(JSON.stringify(json));
}

/**
 * General Websocket send function, with some socket checks.
 */
function send(message) {
    if (!window.WebSocket) {
        return;
    }
    if (socket.readyState == WebSocket.OPEN) {
        socket.send(message);
    } else {
        showError("The socket is not opened.")
    }
}

