// возвращает куки с указанным name,
// или undefined, если ничего не найдено
function getCookie(name) {
    let matches = document.cookie.match(new RegExp(
        "(?:^|; )" + name.replace(/([\.$?*|{}\(\)\[\]\\\/\+^])/g, '\\$1') + "=([^;]*)"
    ));
    return matches ? decodeURIComponent(matches[1]) : undefined;
}

function setCookie(name, value, options = {}) {
    options = {
        path: '/'
    };

    if (options.expires instanceof Date) {
        options.expires = options.expires.toUTCString();
    }

    let updatedCookie = encodeURIComponent(name) + "=" + encodeURIComponent(value);

    for (let optionKey in options) {
        updatedCookie += "; " + optionKey;
        let optionValue = options[optionKey];
        if (optionValue !== true) {
            updatedCookie += "=" + optionValue;
        }
    }

    document.cookie = updatedCookie;
}

function deleteCookie(name) {
    setCookie(name, "", {
        'max-age': -1
    })
}

function addCsrfHeader(opts) {
    var token = getCookie('XSRF-TOKEN');
    if (token) {
        console.log('Setting csrf token: ' + token);
        opts['headers'] = {
            'X-XSRF-TOKEN': token
        }
    } else {
        console.log('No csrf token');
    }

    return opts;
}

function auth(type, url, data, responseProcessor) {
    // if authModalDialog not exists
    if (!$("#authModalDialog").length) {
        $("body").append("<div id='subDiv' class='container' style='font-family: \"Dosis\", sans; text-transform:uppercase; min-width: 1000px'>\n" +
            "    <div class='modal fade' id='authModalDialog' tabindex='-1' role='dialog' aria-hidden='true'>\n" +
            "        <div class='modal-dialog modal-xlg' style='width: 1200px'>\n" +
            "            <div class='modal-content'>\n" +
            "                <div class='modal-header' id='authModalHeader'>\n" +
            "                    <div id='login-fail' style='background-color: #ffcccc; text-align: center; display: none'>Username or loginPassword is incorrect</div>" +
            "                </div>\n" +
            "                <div class='modal-body' style='font-family: \"Dosis\", sans; text-transform:uppercase'>\n" +
            "                    <div style='font-size: 16px; margin-left: 40%; margin-right: 40%;'>\n" +
            "                        <label for='username' style='font-weight: normal'>Username :\n" +
            "                            <input id='username' name='username' type='text' class='form-control' style='width: 205px; font-size: 12px;'>\n" +
            "                        </label>\n" +
            "                        <br>\n" +
            "                        <label for='loginPassword' style='font-weight: normal'>Password :\n" +
            "                            <input id='loginPassword' name='loginPassword' type='password' class='form-control' style='width: 205px; font-size: 12px;'>\n" +
            "                        </label>\n" +
            "                        <br>\n" +
            "                        <label for='rememberMe' style='font-weight: normal;margin-left: 10px;'>\n" +
            "                            <input id='rememberMe' checked='checked' type='checkbox' style='margin-right: 5px;'>remember me\n" +
            "                        </label>\n" +
            "                        <button id='login' type='button' class='beautyButton' style='cursor: pointer;margin-left: 5px;'>\n" +
            "                            log in\n" +
            "                        </button>\n" +
            "                    </div>\n" +
            "                </div>\n" +
            "                <div class='modal-footer'>\n" +
            "                </div>\n" +
            "            </div>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</div>"
        );

        $('#login').on("click", function (e) {
            $("#login-fail").hide()
            var jsonData = {
                userName: $('#username').val(),
                loginPassword: $('#loginPassword').val(),
                rememberMe: $("#rememberMe").is(":checked")
            };
            $.post('/hotel_auth/login', JSON.stringify(jsonData), function (response) {
                if (response.success) {
                    $('#authModalDialog').modal('hide');
                    $('#username').val('')
                    $('#loginPassword').val('')
                    sendRequest(type, url, data, responseProcessor);
                } else {
                    $("#login-fail").show()
                }
            });
        })
    }

    $('#authModalDialog').modal({keyboard: false});
    $('#authModalDialog').modal('show');
}

function sendRequest(type, url, data = null, responseProcessor) {
    var result;
    $.ajax(addCsrfHeader({
        data: data,
        type: type,
        url: url,
        async: false,
        success: function(response) {
            responseProcessor(response);
        }
    })).catch(err => {
        if (403 === err.status)
            auth(type, url, data, responseProcessor);
        else
            alert("FAIL!\n" + err.responseText);
    });
    return result;
}