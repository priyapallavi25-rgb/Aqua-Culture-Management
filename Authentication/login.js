const loginForm = document.getElementById("loginForm");

loginForm.addEventListener("submit", async function(event) {

    event.preventDefault();

    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;

    const message = document.getElementById("message");

    message.textContent = "Logging in...";
    message.style.color = "blue";

    try {

        const response = await fetch("http://localhost:8080/api/login", {

            method: "POST",

            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },

            body: new URLSearchParams({
                email: email,
                password: password
            })

        });

        const result = await response.json();

        if (result.success) {

            message.textContent = "Login successful!";
            message.style.color = "green";

            // Redirect to dashboard
            setTimeout(() => {
                window.location.href = "../Website/index.html";
            }, 1000);

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
