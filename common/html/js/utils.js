function getBaseDomain() {
    let host = window.location.host;
    let parts = host.split(".");
    if (parts.length <= 3) {
        // level 3 domains, such as ulake.sontg.net
        return host;
    }
    parts.splice(0, 1);
    return parts.join(".");
}

function getUserUrl() {
    return window.location.protocol + "//user." + getBaseDomain();
}

function getFolderUrl() {
    return window.location.protocol + "//folder." + getBaseDomain();
}

function getCoreUrl() {
    return window.location.protocol + "//core." + getBaseDomain();
}

function getToken() {
    // todo: don't use session storage to prevent XSS attacks
    return sessionStorage.getItem('jwt');
}

function setToken(token) {
    sessionStorage.setItem("jwt", token);
}

function getUid() {
    if (jwt_decode) {
        try {
            const jwt = jwt_decode(getToken());
            if (jwt.sub) {
                return jwt.sub;
            }
        }
        catch (e) {
        }
    }
    return -1;
}

ajax = function (param){
    let headers;
    if (param.headers) {
        headers = param.headers;
    }
    else {
        headers = {};
    }
    // headers["Authorization-Key"] = apiKey;
    let token = getToken()
    if (token) {
        headers.Authorization = "Bearer " + token;
    }
    let oldSuccess = param.success;
    param.headers = headers;
    if (!param.method) {
        param.method = "GET";
    }
    param.success = function (data) {
        if (!data) {
            window.alert("No response from server.");
            return;
        }
        if (data.code === 400) {
            window.alert(data.msg);
            return;
        }
        oldSuccess(data);
    };
    param.error = function (error) {
        window.alert(`No response from server. Error: ${JSON.stringify(error)}`);
    };
    return $.ajax(param);
};