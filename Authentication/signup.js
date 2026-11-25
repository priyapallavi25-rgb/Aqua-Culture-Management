const signupForm = document.getElementById("signupForm");

signupForm.addEventListener("submit", async function(event) {

    event.preventDefault();

    const username = document.getElementById("username").value;
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const confirmPassword = document.getElementById("confirmPassword").value;

    const message = document.getElementById("message");

    // Check passwords
    if (password !== confirmPassword) {
        message.textContent = "Passwords do not match!";
        message.style.color = "red";
        return;
    }

    message.textContent = "Creating account...";
    message.style.color = "blue";

    try {

        // Send details to Java server
        const response = await fetch("http://localhost:8080/api/signup", {

            method: "POST",

            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },

            body: new URLSearchParams({
                username: username,
                email: email,
                password: password
            })

        });

        const result = await response.json();

        if (result.success) {
            message.textContent = "Account created successfully!";
            message.style.color = "green";

            signupForm.reset();

        } else {
            message.textContent = result.message;
            message.style.color = "red";
        }

    } catch (error) {

        message.textContent = "Cannot connect to server!";
        message.style.color = "red";

        console.error(error);
    }

});

