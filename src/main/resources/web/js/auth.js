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

function mongoAuth(mainDiv) {
    mainDiv.append("<div id='subDiv' class='container' style='font-family: Exo2Regular,serif; min-width: 1000px'>\n" +
        "    <div class='modal fade' id='mongoAuth' tabindex='-1' role='dialog' aria-hidden='true'>\n" +
        "        <div class='modal-dialog modal-xlg' style='width: 1200px'>\n" +
        "            <div class='modal-content'>\n" +
        "                <div class='modal-header' id='mongoAuthHeader'>\n" +
        "                </div>\n" +
        "                <div class='modal-body'>\n" +
        "                    <form action='/hotel_auth/login' id='mongoAuthForm' method='post' enctype='multipart/form-data' style='font-size: 16px;margin-left: 40%;margin-right: 40%;'>\n" +
        "                        <label for='username' style='font-weight: normal'>Username :\n" +
        "                            <input id='username' name='username' type='text' value='' class='form-control' style='width: 205px; font-size: 12px;'>\n" +
        "                        </label>\n" +
        "                        <br>\n" +
        "                        <label for='password' style='font-weight: normal'>Password :\n" +
        "                            <input id='password' name='password' type='password' value='' class='form-control' style='width: 205px; font-size: 12px;'>\n" +
        "                        </label>\n" +
        "                        <br>\n" +
        "                        <label for='rememberMe' style='font-weight: normal;margin-left: 10px;'>\n" +
        "                            <input id='rememberMe' name='rememberMe' checked='checked' type='checkbox' style='margin-right: 5px;'>remember me\n" +
        "                        </label>\n" +
        "                        <button id='login' type='submit' class='btn btn-success' style='cursor: pointer;margin-left: 5px;'>\n" +
        "                            log in\n" +
        "                        </button>\n" +
        "                    </form>\n" +
        "                </div>\n" +
        "                <div class='modal-footer'>\n" +
        "                </div>\n" +
        "            </div>\n" +
        "        </div>\n" +
        "    </div>\n" +
        "</div>");

    function showMongoAuthModalDilog() {
        $('#mongoAuth').modal({backdrop: 'static', keyboard: false});
        $('#mongoAuth').modal('show');
        $('#login').on("click", function (e) {
            $('#mongoAuthForm').submit(function (e) {
                $.ajax(addCsrfHeader({
                    data: new FormData($(this)[0]),
                    type: $(this).attr('method'),
                    url: $(this).attr('action'),
                    async: false,
                    cache: false,
                    contentType: false,
                    processData: false,
                    success: function (response) {
                        if (response.success) {
                            $('#mongoAuth').modal('hide');
                            $('#subDiv').remove();
                            window.location.reload(true);
                        } else {
                            $('#mongoAuthHeader').append("<div id='login-fail' style='font-family: Exo2Regular,serif; text-align:center; background-color: #ffcccc; display: block'>Username or Password is incorrect</div>")
                            showMongoAuthModalDilog();
                        }
                    }
                }));
                e.preventDefault();
                return false;
            })
        })
    }
    showMongoAuthModalDilog();
}

function authCheck(mainDiv) {
    $.ajax(addCsrfHeader({
        type: 'get',
        url: '/hotel_auth/check',
        async: false,
        cache: false,
        contentType: false,
        processData: false,
        success: function(response) {
            if (response.success) {
                console.log("Auth success");
            } else {
                mongoAuth(mainDiv);
            }
        }
    })).catch(err => {
        if (403 === err.status)
            mongoAuth(mainDiv);
        else
            alert("FAIL!\n" + err.responseText);
    });
}