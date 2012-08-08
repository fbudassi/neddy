/**
 * Neddy 1.0.0
 *
 * Neddy Listener implementation.
 *
 * Author: fbudassi
 */

/**
 * General variables.
 */
var subscriptions;

/**
 * HTML Document is ready. Start executing javascript code.
 */
$(document).ready(function() {
    //Open a Websocket to the server.
    openSocket(getWsUrl("listener"), onOpen, onClose, onMessage);

    //Register some event listeners.
    jQuery('#subscriptions').click(showSubscriptions);
    jQuery('#reloadCategories').click(reloadCategories);
    jQuery('#subscribe').click(subscribe);
    jQuery('#unsubscribe').click(unsubscribe);
    jQuery('#clean').click(cleanMessageArea);
    jQuery('.alert-error .close').click(hideError);
});

/**
 * On websocket open event function.
 */
function onOpen(event) {
    addToResponseText("Web Socket opened.\n");

    // Initializes the subscriptions array.
    subscriptions = new Array();

    //Load categories for the first time.
    reloadCategories();
}

/**
 * On websocket close event function.
 */
function onClose(event) {
    addToResponseText("Web Socket closed.\n");

    // Reinitializes the subscriptions array.
    subscriptions = new Array();

    // Clean the categories panel.
    jQuery('#categories').empty();
    jQuery('#categories').append('<option value="" disabled selected style="display: none;">Choose Category...</option>');
}

/**
 * On websocket message event function.
 */
function onMessage(event) {
    var message = jQuery.parseJSON(event.data);

    // Check if message success is false.
    if (message.success == false) {
        showError(message.message);
        return;
    }

    // Switch to the correct reason value.
    switch(message.reason) {
        case 'MESSAGE_NEW':
            addToResponseText(message.category + ": " + message.message + "\n");
            break;
        case 'MESSAGE_CATEGORY_REMOVED':
            // Remove the category from the subscriptions array, if the listener is subscribed.
            var idx = subscriptions.indexOf(message.category);
            if(idx != -1) {
                subscriptions.splice(idx, 1);
            }
            addToResponseText(message.message + "\n");
            reloadCategories();
            break;
        case 'MESSAGE_CATEGORY_ADDED':
            addToResponseText(message.message + "\n");
            reloadCategories();
            break;
        case 'MESSAGE_CATEGORY_SUBSCRIBED':
            addToResponseText(message.message + "\n");
            break;
        case 'MESSAGE_CATEGORY_UNSUBSCRIBED':
            addToResponseText(message.message + "\n");
            break;
        case 'MESSAGE_CATEGORY_UNSUBSCRIBED_ALL':
            addToResponseText(message.message + "\n");
            break;
        case 'MESSAGE_CATEGORY_LIST':
            jQuery.each(jQuery.parseJSON(message.message), showCategory);
            break;
        default:
            showError("Unrecognized reason in request: " + message.reason)
    }
}

/**
 * Reload the category list.
 */
function reloadCategories() {
    var message = {
        action: 'GET_CATEGORIES'
    }
    sendJson(message);

    // Clean the categories panel.
    jQuery('#categories').empty();
    jQuery('#categories').append('<option value="" disabled selected style="display: none;">Choose Category...</option>');
}

/**
 *
 */
function showSubscriptions() {
    var list = "";
    jQuery.each(subscriptions, function(index, value) {
        list += value + "\n"
    });
    if (list == "") {
        list = "No categories.\n";
    }
    addToResponseText("You are currently subscribed to:\n" + list);
}

/**
 * Subscribes to a new category.
 */
function subscribe() {
    var selected = jQuery('#categories').val();
    if (selected == "") {
        return;
    }

    addToResponseText("Subscribing to category: " + selected + "\n");

    // Add the category to the subscriptions array.
    if (subscriptions.indexOf(selected) == -1) {
        subscriptions.push(selected);
    } else {
        addToResponseText("You are already subscribed to category " + selected + "\n");
        return;
    }

    // Send subscription request to server.
    var message = {
        action: 'SUBSCRIBE',
        category: selected
    }
    sendJson(message);
}

/**
 * Unsubscribes from a category.
 */
function unsubscribe() {
    var selected = jQuery('#categories').val();
    addToResponseText("Unsubscribing from category: " + selected + "\n");

    // Remove the category from the subscriptions array.
    var idx = subscriptions.indexOf(selected);
    if(idx != -1) {
        subscriptions.splice(idx, 1);
    } else {
        addToResponseText("You are not currently subscribed to category " + selected + "\n");
        return;
    }

    // Send unsubscription request to server.
    var message = {
        action: 'UNSUBSCRIBE',
        category: selected
    }
    sendJson(message);
}

/**
 * It cleans the message area.
 */
function cleanMessageArea() {
    jQuery('#responseText').val("");
}

/**
 * It adds a category to the Categories combobox.
 */
function showCategory(index, value) {
    var category = '<option value="' + value + '">' + value + '</option>';
    jQuery('#categories').append(category);
}

/**
 * It gets the correct Websocket url.
 */
function getWsUrl(resource) {
    var wsURL = "ws://" + document.URL.split("/")[2] + "/" + resource;
    return wsURL;
}