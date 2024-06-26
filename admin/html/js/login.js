import { authApi, userApi } from "http://common.dev.ulake.usth.edu.vn/js/api.js";

window.login = async () => {
    const uid = document.querySelector("#uid").value,
          pwd = document.querySelector("#pwd").value;


    console.log(`Logging in as ${uid}, ${pwd}, domain ${getBaseDomain()}`);
    const button = $("form button");
    button.text("").append($(`<i class="fas fa-spinner fa-spin"></i>`));
    const data = await authApi.login(uid, pwd);
    if (data && Object.keys(data).length > 0) {
        console.log(`Login ok, token=${data}`);
        setToken(data);
        const name = await userApi.getName(getUid());
        setUserName(name);
        // if (!getGroups().includes("Admin")) {
        //     showModal("Error", "Sorry, you are not an admin.");
        //     setToken(null);
        //     return;
        // }
        window.location = "/folders";
    }
    else {
        showModal("Error", "Incorrect username/password.");
        button.text("Login")
    }
}


window.addEventListener('load', () => {
    const pwd = document.querySelector("#pwd");
    pwd.addEventListener('keypress', function (e) {
        if (e.key === "Enter") {
            e.preventDefault();
            window.login();
        }
    });
})
