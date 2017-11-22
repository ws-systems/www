function submitContact() {
    $.ajax({
        type: "POST",
        url: "/api/contact",
        data: $("#contact-form").serialize(), // serializes the form's elements.
        error: function (data) {
            if (data.status === 406) {
                // Human Verification Failed

                // Highlight box red, clear value, show message
                alert("You ain't human!");
            } else if (data.status === 400) {
                // Incomplete Form
            } else {
                // Something went wrong unexpectedly
            }
        },
        success: function (data) {
            // Show Success Message and Hide Form
            alert("Cool!");
        }
    });
}