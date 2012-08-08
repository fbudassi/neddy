/**
 * Neddy 1.0.0
 *
 * Utility functions for Neddy.
 *
 * Author: fbudassi
 */

/**
 * Utility function to show an error in the error section of the page.
 */
function showError(message){
    jQuery(".alert-error #message").html(message);
    jQuery(".alert-error").fadeIn();
}

/**
 * Utility function to hide the error section of the page.
 */
function hideError() {
    jQuery(".alert-error").fadeOut();
}

/**
 * It adds some message at the bottom of the responseText area.
 */
function addToResponseText(message) {
    jQuery('#responseText').val(Date().toString() + " - " + message + jQuery('#responseText').val());
}