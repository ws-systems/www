function contactFormPreFlight() {
    var status = true;
    var $form = $("#contact-form");

    // Name
    var $name = $form.find('input[name="name"]');
    if ($name.val() === "") {
        $name.siblings('.validation-help-text').show();
        $name.addClass('validation-error');
        status = false;
    }

    // Email
    var $email = $form.find('input[name="email"]');
    if ($email.val() === "" || !validate($email.val())) {
        $email.siblings('.validation-help-text').show();
        $email.addClass('validation-error');
        status = false;
    }

    // Message
    var $message = $form.find('textarea[name="message"]');
    if ($message.val() === "") {
        $message.siblings('.validation-help-text').show();
        $message.addClass('validation-error');
        status = false;
    }

    // Human Verification
    var $check = $form.find('input[name="check"]');
    if ($check.val() === "") {
        $check.siblings('.validation-help-text').show();
        $check.addClass('validation-error');
        status = false;
    }

    return status;
}

function submitContact() {
    // Disable Send Button
    var $contact = $('#contact-form');
    var $submitButton = $contact.find('button[type="submit"]');

    $submitButton.addClass('disabled');
    $submitButton.attr("disabled", "disabled");
    $submitButton.data("previous-text", $submitButton.text());
    $submitButton.html('<i class="fa fa-circle-o-notch fa-spin fa-fw"></i>');

    if (contactFormPreFlight()) // Only Submit form if passes Pre Flight
        $.ajax({
            type: "POST",
            url: "/api/contact",
            data: $contact.serialize(), // serializes the form's elements.
            error: function (data) {
                if (data.status === 406) {
                    // Human Verification Failed
                    var $verificationField = $('#contact-form').find('input[name="check"]');
                    $verificationField.siblings('.validation-help-text').show();
                    $verificationField.addClass('validation-error');
                    $verificationField.val('');
                } else if (data.status === 400) {
                    // Incomplete Form
                    swal("Not Quite", "Please complete all fields on the contact form before sending in your message", "warning");
                } else {
                    // Something went wrong unexpectedly
                    swal("Oh No!", "Something went wrong trying to send in your message!", "error");
                    console.error(data)
                }
            },
            success: function (data) {
                // Show Success Message and Hide Form
                var $form = $("#contact-form");
                var $conf_message = $form.siblings(".completed_message");

                $form.stop(true).fadeOut("fast", function () {
                    $conf_message.fadeIn("slow");
                    $form[0].reset(); // Reset Form Contents
                });

                return false;
            },
            always: renableSend()
        });
    else renableSend();
}

function renableSend() {
    var $contact = $('#contact-form');
    var $submitButton = $contact.find('button[type="submit"]');

    $submitButton.removeClass('disabled');
    $submitButton.removeAttr("disabled");
    $submitButton.text($submitButton.data("previous-text"));
}

function validate(email) {
    var tester = /^[-!#$%&'*+\/0-9=?A-Z^_a-z{|}~](\.?[-!#$%&'*+\/0-9=?A-Z^_a-z`{|}~])*@[a-zA-Z0-9](-?\.?[a-zA-Z0-9])*\.[a-zA-Z](-?[a-zA-Z0-9])+$/;

    if (!email)
        return false;

    if (email.length > 254)
        return false;

    var valid = tester.test(email);
    if (!valid)
        return false;

    // Further checking of some things regex can't handle
    var parts = email.split("@");
    if (parts[0].length > 64)
        return false;

    var domainParts = parts[1].split(".");
    return !domainParts.some(function (part) {
        return part.length > 63;
    });
}